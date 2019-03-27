/*
 * Copyright 2014-present Milos Gligoric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ekstazi.agent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.net.URL;
import java.security.ProtectionDomain;

import org.ekstazi.Config;
import org.ekstazi.Names;
import org.ekstazi.asm.ClassReader;
import org.ekstazi.asm.ClassWriter;
import org.ekstazi.instrument.CoverageClassVisitor;
import org.ekstazi.research.Research;
import org.ekstazi.util.FileUtil;
import org.ekstazi.util.LRUMap;
import org.ekstazi.util.Types;

import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.Checksum;
import java.util.zip.Adler32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Transformer that instrument classes to collect dependencies.
 */
public final class EkstaziCFT implements ClassFileTransformer {

    /** Monitor resource; used to check if monitor is available to ClassLoader */
    private static final String COVERAGE_MONITOR_RESOURCE = Names.COVERAGE_MONITOR_VM.concat(".class");

    /** Cache for redefined classes; avoiding to redefine any class twice */
    private final Set<String> mCacheRedefinedClasses;

    /** Save instrumented classes in files based on hashed original byte array */
    private final boolean mIsSaveInstrumentedHash;

    /** Pattern that describes classes to be included */
    private final Pattern mClassesInclude;

    /**
     * Constructor.
     */
    public EkstaziCFT() {
        this.mCacheRedefinedClasses = Collections.newSetFromMap(new LRUMap<String, Boolean>(1000));
        this.mIsSaveInstrumentedHash = Config.X_SAVE_INSTRUMENTED_CODE_V;
        this.mClassesInclude = Config.DEPENDENCIES_CLASSES_INSTRUMENT_V;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        // Ensure that monitor is accessible from the ClassLoader.
        if (!isMonitorAccessibleFromClassLoader(loader)) {
            return null;
        }

        // Check if class should be instrumented/collected.
        if (mClassesInclude != null && !mClassesInclude.matcher(className).find()) {
            return null;
        }

        if (className.contains("$Proxy") || Types.isIgnorableInternalName(className)) {
            return null;
        }

        StorageResult storageResult = loadInstrumentedClassfile(loader, className, classfileBuffer);
        if (storageResult != null && storageResult.mClassfile != null) {
            return storageResult.mClassfile;
        }

        boolean isBeingRedefined = classBeingRedefined != null;
        // Avoid redefining the same class multiple times.
        if (isBeingRedefined) {
            String name = (loader == null ? "null" : loader.toString()) + "." + className;
            if (!mCacheRedefinedClasses.add(name)) {
                return classfileBuffer;
            }
        }

        // Instrument class.
        classfileBuffer = instrumentClass(loader, className, isBeingRedefined, storageResult, classfileBuffer);

        // Line for debugging.
        // saveClassfileBufferForDebugging(className, classfileBuffer);
        return classfileBuffer;
    }

    protected byte[] instrumentClass(ClassLoader loader, String className, boolean isBeingRedefined,
          StorageResult storageResult, byte[] classfileBuffer) {
        // Instrument class.
        ClassReader classReader = new ClassReader(classfileBuffer);
        // Removed COMPUTE_FRAMES as I kept seeing linkage error
        // with Java 7. However for our current instrumentation
        // this argument seems not necessary.
        ClassWriter classWriter = new ClassWriter(classReader,
        /* ClassWriter.COMPUTE_FRAMES | */ClassWriter.COMPUTE_MAXS);
        CoverageClassVisitor visitor = createCoverageClassVisitor(className, classWriter, isBeingRedefined);
        // NOTE: cannot skip debug as some tests depend on these info.
        // classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);
        classReader.accept(visitor, 0);
        if (visitor.isModifiable()) {
            byte[] modifiedClassfileBuffer = classWriter.toByteArray();
            storeInstrumentedClassfile(loader, className, storageResult, classfileBuffer, modifiedClassfileBuffer);
            classfileBuffer = modifiedClassfileBuffer;
        }
        return classfileBuffer;
    }

    // INSTRUMENTED STORAGE

    @Research
    public static final class StorageResult {
        byte[] mClassfile;
        long mHash;

        public StorageResult(byte[] classfile) {
            this(classfile, 0L);
        }

        public StorageResult(byte[] classfile, long hash) {
            this.mClassfile = classfile;
            this.mHash = hash;
        }

        public byte[] getClassfile() {
            return mClassfile;
        }
    }

    @Research
    private void storeInstrumentedClassfile(ClassLoader loader, String className, StorageResult storageResult,
            byte[] classfileBuffer, byte[] modifiedClassfileBuffer) {
        if (!mIsSaveInstrumentedHash) {
            return;
        }
        URL url = loader.getResource(className + ".class");
        if (url == null) {
            return;
        }
        saveBank(loader, className, modifiedClassfileBuffer, storageResult.mHash);
    }

    @Research
    private StorageResult loadInstrumentedClassfile(ClassLoader loader, String className, byte[] classfileBuffer) {
        if (!mIsSaveInstrumentedHash) {
            return null;
        }
        URL url = loader.getResource(className + ".class");
        if (url == null) {
            return null;
        }
        // We hash bytes on disk rather than classfileBuffer, as
        // loading class may not preserve the order of methods.
        // long checksum = CRC64.checksum(classfileBuffer);
        Checksum cksum = new Adler32();
        FileUtil.loadBytes(url, cksum);
        long cksumValue = cksum.getValue();
        return new StorageResult(checkBank(loader, className, cksumValue), cksumValue);
    }

    @Research
    private void saveBank(ClassLoader loader, String className, byte[] classfileBuffer, long checksum) {
        File file = new File(Config.ROOT_DIR_V + File.separator + Names.INSTRUMENTED_CLASSES_DIR_NAME,
                Long.toString(checksum));
        try {
            file.getParentFile().mkdirs();
            FileUtil.writeFile(file, classfileBuffer);
        } catch (IOException e) {
            // Nothing.
        }
    }

    @Research
    private byte[] checkBank(ClassLoader loader, String className, long checksum) {
        File file = new File(Config.ROOT_DIR_V + File.separator + Names.INSTRUMENTED_CLASSES_DIR_NAME,
                Long.toString(checksum));
        if (file.exists()) {
            try {
                return FileUtil.readFile(file);
            } catch (IOException e) {
                // Nothing.
            }
        }
        return null;
    }

    // INTERNAL
    
    /**
     * Creates class visitor to instrument for coverage based on configuration
     * options.
     */
    private CoverageClassVisitor createCoverageClassVisitor(String className, ClassWriter cv, boolean isRedefined) {
        // We cannot change classfiles if class is being redefined.
        return new CoverageClassVisitor(className, cv);
    }

    // Check if loader knows about monitors, otherwise do not
    // instrument; TODO: we should log this problem.
    // TODO: Check if this method is needed after introducing
    // LoaderMethodVisitor and LoaderMonitor.
    private boolean isMonitorAccessibleFromClassLoader(ClassLoader loader) {
        if (loader == null) {
            return false;
        }
        boolean isMonitorAccessible = true;
        InputStream monitorInputStream = null;
        try {
            monitorInputStream = loader.getResourceAsStream(COVERAGE_MONITOR_RESOURCE);
            if (monitorInputStream == null) {
                isMonitorAccessible = false;
            }
        } catch (Exception ex1) {
            isMonitorAccessible = false;
            try {
                if (monitorInputStream != null) {
                    monitorInputStream.close();
                }
            } catch (IOException ex2) {
                // do nothing
            }
        }
        return isMonitorAccessible;
    }
    
    // DEBUGGING

    /**
     * This method is for debugging purposes. So far one of the best way to
     * debug instrumentation was to actually look at the instrumented code. This
     * method let us choose which class to print.
     * 
     * @param className
     *            Name of the class being instrumented.
     * @param classfileBuffer
     *            Byte array with the instrumented class content.
     */
    @SuppressWarnings("unused")
    private void saveClassfileBufferForDebugging(String className, byte[] classfileBuffer) {
        try {
            if (className.contains("CX")) {
                java.io.DataOutputStream tmpout = new java.io.DataOutputStream(new java.io.FileOutputStream("out"));
                tmpout.write(classfileBuffer, 0, classfileBuffer.length);
                tmpout.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Support for static instrumentation.

    private static byte[] instrumentClassFile(byte[] classfileBuffer) {
        String className = new ClassReader(classfileBuffer).getClassName().replace("/", ".");
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        byte[] newClassfileBuffer = new EkstaziCFT().transform(currentClassLoader, className, null, null, classfileBuffer);
        return newClassfileBuffer;
    }

    protected static void instrumentClassFile(String pathToFile) throws IOException {
        File file = new File(pathToFile);
        byte[] classfileBuffer = FileUtil.readFile(file);
        byte[] newClassfileBuffer = instrumentClassFile(classfileBuffer);
        FileUtil.writeFile(file, newClassfileBuffer);
    }

    /**
     * Instrument all classfiles (files that end with .class) inside
     * the given jar and rewrite the existing jar.
     *
     * This method can be simplified if we move Ekstazi code from Java
     * 6 to newer version.  See
     * http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
     */
    protected static void instrumentJarFile(String pathToFile) throws IOException {
        File jarFile = new File(pathToFile);
        // Use tmp file for output (in the same directory).
        File newFile = File.createTempFile("any", ".jar", jarFile.getParentFile());
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new FileInputStream(jarFile));
            zos = new ZipOutputStream(new FileOutputStream(newFile));
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                String name = entry.getName();
                zos.putNextEntry(new ZipEntry(name));
                if (name.endsWith(".class")) {
                    // We instrument classfiles and then write them to
                    // new jar.
                    byte[] classfileBuffer = FileUtil.loadBytes(zis, false);
                    byte[] newClassfileBuffer = instrumentClassFile(classfileBuffer);
                    zos.write(newClassfileBuffer);
                } else {
                    int data;
                    while ((data = zis.read()) != -1) {
                        zos.write(data);
                    }
                }
                zos.closeEntry();
            }
        } finally {
            FileUtil.closeAndIgnoreExceptions(zis);
            FileUtil.closeAndIgnoreExceptions(zos);
        }

        // Move new jar to old jar.
        // Files.move(newFile, jarFile, StandardCopyOption.REPLACE_EXISTING);
        FileUtil.copyBytes(newFile, jarFile);
        newFile.delete();
    }

    // MAIN

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Expecting mode(--file/--jar) and path to the file");
        }

        String mode = args[0];
        String pathToFile = args[1];

        // Check if file exists.
        if (new File(pathToFile).exists()) {
            if (mode.equals("--file")) {
                instrumentClassFile(pathToFile);
            } else if (mode.equals("--jar")) {
                instrumentJarFile(pathToFile);
            }
        } else {
            System.err.println("File does not exist: " + pathToFile);
        }
    }
}
