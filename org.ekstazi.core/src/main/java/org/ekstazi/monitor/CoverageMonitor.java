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

package org.ekstazi.monitor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import org.ekstazi.Config;
import org.ekstazi.Names;
import org.ekstazi.log.Log;
import org.ekstazi.research.Research;
import org.ekstazi.util.Types;

/**
 * Monitor notified in runtime of various dependencies.
 */
public final class CoverageMonitor {

    /** Class name for ClassLoader */
    private static final String CLASS_LOADER_NAME = ClassLoader.class.getName();

    /** Collected classes */
    private static final Set<Class<?>> sClasses = new HashSet<Class<?>>();

    // private static final String TMP_DIR_PATH =
    // System.getProperty("java.io.tmpdir");
    /** Local of temporary files on Unit */
    private static final String TMP_FILE_PREFIX = "file:/tmp/";
    private static final String TMP_JAR_PREFIX = "jar:file:/tmp/";

    /** Is the program running on Unix machine */
    private static final boolean IS_UNIX;

    /** Collected urls */
    private static final Set<String> sURLs = new HashSet<String>();

    /** Collected urls that should not be removed on clean */
    private static final Set<String> sUncleanableURLs = new HashSet<String>();
    
    /** Strings used to identify URL for JUnit */
    private static final String JUNIT_FRAMEWORK_URL_PART = "!/" + Names.JUNIT_FRAMEWORK_PACKAGE_VM;
    private static final String ORG_JUNIT_URL_PART = "!/" + Names.ORG_JUNIT_PACKAGE_VM;
    private static final String ORG_HAMCREST_URL_PART = "!/" + Names.ORG_HAMCREST_VM;
    private static final String ORG_APACHE_MAVEN_URL_PART = "!/" + Names.ORG_APACHE_MAVEN_VM;

    /** Clock for this monitor */
    private static final ReentrantLock sLock = new ReentrantLock();

    /** Size of probe array, used to optimize execution */
    private static final int PROBE_ARRAY_SIZE = 8192;
    private static final int PROBE_SIZE_MASK = PROBE_ARRAY_SIZE - 1;
    /** Probe array */
    private static final Class<?>[] PROBE_ARRAY = new Class<?>[PROBE_ARRAY_SIZE];

    // FRAMEWORK AND RUNTIME

    /**
     * Clean dynamically collected coverage.
     */
    public static void clean() {
        try {
            sLock.lock();
            clean0();
        } finally {
            sLock.unlock();
        }
    }

    public static void cleanCaches() {
        try {
            sLock.lock();
            sClasses.clear();
            ClassesCache.clean();
            Arrays.fill(PROBE_ARRAY, null);
        } finally {
            sLock.unlock();
        }
    }

    // Unsed for tests only.
    public static void cleanUncleanable() {
        try {
            sLock.lock();
            sUncleanableURLs.clear();
        } finally {
            sLock.unlock();
        }
    }

    private static void clean0() {
        sClasses.clear();
        sURLs.clear();
        ClassesCache.clean();
        Arrays.fill(PROBE_ARRAY, null);
    }

    /**
     * Add the given urls to dynamically collected info. Note that this method
     * is not invoked by instrumented code but rather by some other parts of our
     * system to preset some values.
     */
    public static void addURLs(String... urls) {
        try {
            sLock.lock();
            addURLs0(urls);
        } finally {
            sLock.unlock();
        }
    }

    private static void addURLs0(String[] urls) {
        for (String url : urls) {
            sURLs.add(url);
        }
    }

    public static void addUncleanableURLs(String externalForm) {
        try {
            sLock.lock();
            if (!filterURL(externalForm)) {
                sUncleanableURLs.add(externalForm);
            }
        } finally {
            sLock.unlock();
        }
    }
    
    /**
     * Returns the set of dynamically collected urls. Note that this method is
     * invoked from other parts of our system to obtain the collected data.
     */
    public static String[] getURLs() {
        try {
            sLock.lock();
            return getURLs0();
        } finally {
            sLock.unlock();
        }
    }

    private static String[] getURLs0() {
        List<String> result = new ArrayList<String>();
        for (String url : sURLs) {
            result.add(url);
        }
        for (String url : sUncleanableURLs) {
            result.add(url);
        }

        // Uncleanable URLs should only be used in runs that have on
        // test in one JVM, so there is no reason to clean this set.
        // sUncleanableURLs.clear();

        return result.toArray(new String[result.size()]);
    }

    /**
     * Touch method. Instrumented code invokes this method to collect class
     * coverage.
     * 
     * IMPORTANT: "synchronized" on the following method leads to slow execution
     * for a large number of invocation (even in sequential code).
     * {@link Semaphore} is (slightly) faster but it may be too much for the
     * moment.
     */
    public static void t(Class<?> clz) {
        // Must be non null and type of interest.
        if (clz == null || ClassesCache.check(clz) || Types.isIgnorable(clz)) {
            return;
        }

        // Check and assign id to this class (this must be synchronized).
        try {
            sLock.lock();
            if (!sClasses.add(clz)) {
                return;
            }
        } finally {
            sLock.unlock();
        }

        String className = clz.getName();
        String resourceName = className.substring(className.lastIndexOf(".") + 1).concat(".class");
        URL url = null;
        try {
            // Find resource for the class (URL that we will use to extract
            // the path).
            url = clz.getResource(resourceName);
        } catch (SecurityException ex) {
            Log.w("Unable to obtain resource because of security reasons.");
        }
        // If no URL obtained, return.
        if (url == null) {
            return;
        }
        recordURL(url.toExternalForm());
    }

    /**
     * Touch method, which also accepts probe id. The id can be used to optimize
     * execution.
     * 
     * The current optimization is as follows: we check if index at probeId
     * location is set; if yes, then we do nothing, but if not, we set it and
     * invoke original touch method. As we use limited size of probe array, we
     * have to check length as we may have more probes than array allows.
     */
    public static void t(Class<?> clz, int probeId) {
        if (clz != null) {
            int index = probeId & PROBE_SIZE_MASK;
            if (PROBE_ARRAY[index] != clz) {
                PROBE_ARRAY[index] = clz;
                t(clz);
            }
        }
    }

    /**
     * Touch method. This method is invoked when a static field is read. We need
     * this method as the type of the filed may be declared private (e.g., inner
     * class), so we need a way to get that type; unfortunately, we cannot use
     * class literal, as we get java.lang.IllegalAccessError. Note that this
     * method is invoked only for GETFIELD as for test selection PUTFIELD may
     * not matter.
     */
    public static void f(Object o) {
        if (o != null && !(o instanceof Class<?>)) {
            t(o.getClass());
        }
    }

    /**
     * Touch method, which also accepts probe id. The probe id can be used to
     * optimize execution.
     * 
     * See {@link #t(Class, int)} for further comments.
     */
    public static void f(Object o, int probeId) {
        if (o != null && !(o instanceof Class<?>)) {
            t(o.getClass(), probeId);
        }
    }

    // IO SUPPORT

    static {
        IS_UNIX = System.getProperty("file.separator").equals("/");
    }

    /**
     * Records the given file as a dependency after some filtering.
     * 
     * @param f
     *            File to record as a dependency
     */
    public static void addFileURL(File f) {
        String absolutePath = f.getAbsolutePath();
        if (!filterFile(absolutePath)) {
            try {
                recordURL(f.toURI().toURL().toExternalForm());
            } catch (MalformedURLException e) {
                // Never expected.
            }
        }
    }

    // INTERNAL

    /**
     * Records the given external form of URL as a dependency after checking if
     * it should be filtered.
     * 
     * @param externalForm
     */
    protected static void recordURL(String externalForm) {
        if (filterURL(externalForm)) {
            return;
        }

        if (isWellKnownUrl(externalForm)) {
            // Ignore JUnit classes if specified in configuration.
            if (Config.DEPENDENCIES_INCLUDE_WELLKNOWN_V) {
                safeRecordURL(externalForm);
            }
        } else {
            safeRecordURL(externalForm);
        }
    }

    /**
     * Records the given external form of URL as a dependency.
     * 
     * @param externalForm
     */
    private static void safeRecordURL(String externalForm) {
        try {
            sLock.lock();
            sURLs.add(externalForm);
        } finally {
            sLock.unlock();
        }
    }

    // FILTERS

    /**
     * Returns true if likely externalForm is related to one of JUnit classes.
     */
    private static boolean isWellKnownUrl(String externalForm) {
        return externalForm.contains(ORG_JUNIT_URL_PART) ||
                externalForm.contains(JUNIT_FRAMEWORK_URL_PART) ||
                externalForm.contains(ORG_HAMCREST_URL_PART) ||
                externalForm.contains(ORG_APACHE_MAVEN_URL_PART);
    }

    private static boolean filterFile(String absolutePath) {
        // Note that we ignore /dev/* files; this was inspired by /dev/random
        // that cannot be hashed as the file has no end :).
        return (absolutePath.contains(Config.ROOT_DIR_V)
                || absolutePath.contains("/" + Names.EKSTAZI_ROOT_DIR_NAME)
                || absolutePath.contains("junitvmwatcher")
                // || absolutePath.endsWith(".class")
                || absolutePath.startsWith("/dev/"));
    }

    private static boolean filterURL(String externalForm) {
        // Filter tmp files. Note that we filter /tmp directory on Unix. We do
        // not use java.io.tmp as its value can be configured.
        if (IS_UNIX && (externalForm.startsWith(TMP_FILE_PREFIX) || externalForm.startsWith(TMP_JAR_PREFIX))) {
            return true;
        }
        // Ignore paths that are specified in configuration.
        if (Config.DEPENDENCIES_IGNORED_PATHS_V != null) {
            if (Config.DEPENDENCIES_IGNORED_PATHS_V.matcher(externalForm).find()) {
                return true;
            }
        }
        return false;
    }
}
