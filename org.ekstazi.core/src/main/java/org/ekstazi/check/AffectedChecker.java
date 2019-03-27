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

package org.ekstazi.check;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ekstazi.Config;
import org.ekstazi.Names;
import org.ekstazi.data.DependencyAnalyzer;
import org.ekstazi.data.Storer;
import org.ekstazi.hash.Hasher;

/**
 * Checks all files in coverage directory based on configuration and print the
 * list of classes that are NOT affected. Note the following:
 * 1) if a single test is affected (in case when we collect coverage per test),
 * we assume that entire class is affected, and 2) we have problem with
 * deleted/renamed test methods, as we may claim that a class is affected (due
 * to stale coverage) and not exclude class (however note that we will catch
 * that something does not have to be executed in runtime if we check again).
 */
public class AffectedChecker {

    /** user.dir property name */
    private static final String USER_DIR = "user.dir";
    
    /** Print non affected classes in ant mode */
    private static final String ANT_MODE = "--ant";

    /** Print non affected classes in maven mode enclosed in <excludes> */
    private static final String MAVEN_MODE = "--mvn";

    /** Print non affected classes in maven mode without surrounding <excludes> */
    private static final String MAVEN_SIMPLE_MODE = "--mvn-simple";

    /** Print (non) affected classes in debug mode */
    private static final String DEBUG_MODE = "--debug";
    
    /** Forces cache use */
    private static final String FORCE_CACHE_USE = "--force-cache-use";

    /**
     * The user has to specify directory that keep coverage and optionally
     * mode that should be used to print non affected classes.
     */
    public static void main(String[] args) {
        // Parse arguments.
        String coverageDirName = null;
        if (args.length == 0) {
            System.out.println("Incorrect arguments.  Directory with coverage has to be specified.");
            System.exit(1);
        }
        coverageDirName = args[0];
        String mode = null;
        if (args.length > 1) {
            mode = args[1];
        }
        boolean forceCacheUse = false;
        if (args.length > 2) {
            forceCacheUse = args[2].equals(FORCE_CACHE_USE);
        }

        Set<String> allClasses = new HashSet<String>();
        Set<String> affectedClasses = new HashSet<String>();
        if (args.length > 3) {
            String options = args[3];
            Config.loadConfig(options, true);
        } else {
            Config.loadConfig();
        }

        List<String> nonAffectedClasses = findNonAffectedClasses(coverageDirName, forceCacheUse, allClasses, affectedClasses);
        
        // Print non affected classes.
        printNonAffectedClasses(allClasses, affectedClasses, nonAffectedClasses, mode);
    }

    /**
     * Finds the list of non-affected test classes. This method is intented to
     * be invoked from build plugins/tasks.
     * 
     * @param parentDir
     *            Parent directory of .ekstazi directory
     * @param options
     *            Ekstazi options
     * @return List of non-affected test classes.
     */
    public static List<String> findNonAffectedClasses(File parentDir, String options) {
        // Return if Ekstazi directory does not exist.
        if (!Config.createRootDir(parentDir).exists()) {
            return Collections.<String>emptyList();
        }
        Config.loadConfig(options, true);
        return findNonAffectedClasses(parentDir.getAbsolutePath());
    }

    /**
     * Returns list of non affected classes as discovered from the given
     * directory with dependencies.
     */
    private static List<String> findNonAffectedClasses(String workingDirectory) {
        Set<String> allClasses = new HashSet<String>();
        Set<String> affectedClasses = new HashSet<String>();
        loadConfig(workingDirectory);
        // Find non affected classes.
        List<String> nonAffectedClasses = findNonAffectedClasses(Config.ROOT_DIR_V, true, allClasses,
                affectedClasses);
        // Format list to include class names in expected format for Ant and Maven.
        return formatNonAffectedClassesForAntAndMaven(nonAffectedClasses);
    }

    /**
     * Finds the list of test classes that had at least one failing test method
     * last time when they were run. This method is intended to be invoked from
     * build plugins/tasks.
     * 
     * @param parentDir
     *            Parent directory of .ekstazi
     * @param options
     *            Ekstazi options
     * @return List of test classes that had at least one failing test in the
     *         latest run
     */
    public static List<String> findRecentlyFailingClasses(File parentDir, String options) {
        if (!Config.createRootDir(parentDir).exists()) {
            return Collections.<String>emptyList();
        }
        Config.loadConfig(options, true);
        return findRecentlyFailingClasses(parentDir.getAbsolutePath());
    }

    private static List<String> findRecentlyFailingClasses(String workingDirectory) {
        loadConfig(workingDirectory);
        File testResultsDir = new File(Config.ROOT_DIR_V, Names.TEST_RESULTS_DIR_NAME);
        // All files correspond to classes that have been failing.
        List<String> allFailing = new ArrayList<String>();
        if (testResultsDir.exists()) {
            for (File file : testResultsDir.listFiles()) {
                allFailing.add(file.getName());
            }
        }
        return formatNonAffectedClassesForAntAndMaven(allFailing);
    }
    
    // INTERNAL

    private static void loadConfig(String workingDirectory) {
        String oldWorkingDirectory = System.getProperty(USER_DIR);
        if (workingDirectory != null) {
            System.setProperty(USER_DIR, workingDirectory);
        }
        try {
            Config.loadConfig();
        } finally {
            System.setProperty(USER_DIR, oldWorkingDirectory);
        }
    }
    
    private static List<String> formatNonAffectedClassesForAntAndMaven(List<String> nonAffectedClasses) {
        List<String> formatted = new ArrayList<String>();
        for (String binClassName : nonAffectedClasses) {
            formatted.add(binClassName.replaceAll("\\.", "/") + ".java");
        }
        return formatted;
    }

    private static List<String> findNonAffectedClasses(String depsDirName, boolean forceCacheUse, Set<String> allClasses,
            Set<String> affectedClasses) {
        if (!forceCacheUse) {
            Config.CACHE_SIZES_V = 0;
        }
        
        Config.ROOT_DIR_V = depsDirName;
        File depsDir = new File(Config.ROOT_DIR_V);
        
        if (checkIfDoesNotExist(depsDir)) {
            return Collections.emptyList();
        }

        // Find affected test classes.
        includeAffected(allClasses, affectedClasses, getSortedFiles(depsDir));

        // Find test classes that are not affected.
        List<String> nonAffectedClasses = new ArrayList<String>(new HashSet<String>(allClasses));
        nonAffectedClasses.removeAll(affectedClasses);
        Collections.sort(nonAffectedClasses);
        return nonAffectedClasses;
    }

    private static boolean checkIfDoesNotExist(File coverageDir) {
        return coverageDir == null || !coverageDir.exists();
    }

    private static List<File> getSortedFiles(File coverageDir) {
        List<File> sortedFiles = new ArrayList<File>();
        File[] files = coverageDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // Exclude tool files.
                return (!name.contains(Names.VERBOSE_FILE_NAME) && !name.contains(Names.RUN_INFO_FILE_NAME));
            }
        });
        // It can be null when directory still does not exists.
        if (files == null) return sortedFiles;
        sortedFiles = new ArrayList<File>(Arrays.asList(files));
        Collections.sort(sortedFiles, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedFiles;
    }
    
    /**
     * Prints non affected classes in the given mode. If mode is not specified,
     * one class is printed per line.
     */
    private static void printNonAffectedClasses(Set<String> allClasses, Set<String> affectedClasses,
            List<String> nonAffectedClasses, String mode) {
        if (mode != null && mode.equals(ANT_MODE)) {
            StringBuilder sb = new StringBuilder();
            for (String className : nonAffectedClasses) {
                className = className.replaceAll("\\.", "/");
                sb.append("<exclude name=\"" + className + ".java\"/>");
            }
            System.out.println(sb);
        } else if (mode != null && mode.equals(MAVEN_SIMPLE_MODE)) {
            StringBuilder sb = new StringBuilder();
            for (String className : nonAffectedClasses) {
                className = className.replaceAll("\\.", "/");
                sb.append("<exclude>");
                sb.append(className).append(".java");
                sb.append("</exclude>");
            }
            System.out.println(sb);
        } else if (mode != null && mode.equals(MAVEN_MODE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("<excludes>");
            for (String className : nonAffectedClasses) {
                className = className.replaceAll("\\.", "/");
                sb.append("<exclude>");
                sb.append(className).append(".java");
                sb.append("</exclude>");
            }
            sb.append("</excludes>");
            System.out.println(sb);
        } else if (mode != null && mode.equals(DEBUG_MODE)) {
            System.out.println("AFFECTED: " + affectedClasses);
            System.out.println("NONAFFECTED: " + nonAffectedClasses);
        } else {
            for (String className : nonAffectedClasses) {
                System.out.println(className);
            }
        }
    }

    /**
     * Find all non affected classes.
     */
    private static void includeAffected(Set<String> allClasses, Set<String> affectedClasses, List<File> sortedFiles) {
        Storer storer = Config.createStorer();
        Hasher hasher = Config.createHasher();

        NameBasedCheck classCheck = Config.DEBUG_MODE_V != Config.DebugMode.NONE ?
            new DebugNameCheck(storer, hasher, DependencyAnalyzer.CLASS_EXT) :
            new NameBasedCheck(storer, hasher, DependencyAnalyzer.CLASS_EXT);
        NameBasedCheck covCheck = new NameBasedCheck(storer, hasher, DependencyAnalyzer.COV_EXT);
        MethodCheck methodCheck = new MethodCheck(storer, hasher);
        String prevClassName = null;
        for (File file : sortedFiles) {
            String fileName = file.getName();
            String dirName = file.getParent();
            String className = null;
            if (file.isDirectory()) {
                continue;
            }
            if (fileName.endsWith(DependencyAnalyzer.COV_EXT)) {
                className = covCheck.includeAll(fileName, dirName);
            } else if (fileName.endsWith(DependencyAnalyzer.CLASS_EXT)) {
                className = classCheck.includeAll(fileName, dirName);
            } else {
                className = methodCheck.includeAll(fileName, dirName);
            }
            // Reset after some time to free space.
            if (prevClassName != null && className != null && !prevClassName.equals(className)) {
                methodCheck.includeAffected(affectedClasses);
                methodCheck = new MethodCheck(Config.createStorer(), Config.createHasher());
            }
            if (className != null) {
                allClasses.add(className);
                prevClassName = className;
            }
        }
        classCheck.includeAffected(affectedClasses);
        covCheck.includeAffected(affectedClasses);
        methodCheck.includeAffected(affectedClasses);
    }
}
