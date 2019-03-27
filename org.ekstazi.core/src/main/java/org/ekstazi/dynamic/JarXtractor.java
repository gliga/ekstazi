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

package org.ekstazi.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.ekstazi.Names;
import org.ekstazi.log.Log;
import org.ekstazi.research.Research;

/**
 * Utility class that helps with extracting classes (from a jar) with the given
 * prefix(es) to a separate jar.
 * 
 * @author Milos Gligoric
 * @author Andrey Zaytsev
 */
@Research
public final class JarXtractor {

    /**
     * Creates a jar file that contains only classes with specified prefix. Note
     * that new jar is created in the same directory as the original jar
     * (hopefully the user has write permission).
     * 
     * Returns true if classes with the given prefix are detected.
     * 
     * @throws IOException
     *             if a file is not found or there is some error during
     *             read/write operations
     */
    public static boolean extract(File jarFile, File newJar, String[] includePrefixes, String[] excludePrefixes) throws IOException {
        boolean isSomethingExtracted = false;
        if (!jarFile.exists()) {
            Log.w("Jar file does not exists at: " + jarFile.getAbsolutePath());
            return isSomethingExtracted;
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(newJar));
        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            String name = entry.getName().replaceAll("/", ".").replace(".class", "");
            boolean isStartsWith = startsWith(name, includePrefixes) && !startsWith(name, excludePrefixes);
            isSomethingExtracted = isSomethingExtracted || isStartsWith;
            if (isStartsWith || name.startsWith("META-INF")) {
                zos.putNextEntry(new ZipEntry(entry.getName()));
                int data;
                while ((data = zis.read()) != -1) {
                    zos.write(data);
                }
                zos.closeEntry();
            }
        }
        zis.close();
        try {
            zos.close();
        } catch (ZipException e) {
            System.out.println("No classes in the original jar matched the prefix");
        }
        return isSomethingExtracted;
    }

    // INTERNAL

    /**
     * Checks if the given string starts with any of the given prefixes.
     * 
     * @param str
     *            String to check for prefix.
     * 
     * @param prefixes
     *            Potential prefixes of the string.
     * 
     * @return True if the string starts with any of the prefixes.
     */
    private static boolean startsWith(String str, String... prefixes) {
        for (String prefix : prefixes) {
            if (str.startsWith(prefix)) return true;
        }
        return false;
    }

    /**
     * Extract class names from the given jar. This method is for debugging
     * purpose.
     * 
     * @param jarName
     *            path to jar file
     * @throws IOException
     *             in case file is not found
     */
    private static List<String> extractClassNames(String jarName) throws IOException {
        List<String> classes = new LinkedList<String>();
        ZipInputStream orig = new ZipInputStream(new FileInputStream(jarName));
        for (ZipEntry entry = orig.getNextEntry(); entry != null; entry = orig.getNextEntry()) {
            String fullName = entry.getName().replaceAll("/", ".").replace(".class", "");
            classes.add(fullName);
        }
        orig.close();
        return classes;
    }

    // MAIN
    
    /**
     * Simple test to print all classes in the given jar.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("There should be an argument: path to a jar.");
            System.exit(0);
        }
        String jarInput = args[0];
        extract(new File(jarInput), new File("/tmp/junit-ekstazi-agent.jar"),
                new String[] { Names.EKSTAZI_PACKAGE_BIN }, new String[] {});
        for (String name : extractClassNames("/tmp/junit-ekstazi-agent.jar")) {
            System.out.println(name);
        }
    }
}
