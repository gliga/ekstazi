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

import java.util.Arrays;

import org.ekstazi.Config;

/**
 * Cache of recently seen classes.
 * 
 * In history this class used to contain only several static fields. Several
 * profiles showed that the current approach with hashes is faster.
 */
public class ClassesCache {

    /** Cache size */
    protected static final int CACHE_SIZE = 1024;

    /** Mask to make sure that we index in cache */
    private static final int SIZE_MASK = CACHE_SIZE - 1;

    /** Cache */
    private static final Class<?>[] CACHE = new Class<?>[CACHE_SIZE];

    /**
     * Checks if the given class is in cache. If not, puts the class in the
     * cache and returns false; otherwise it returns true.
     * 
     * @param clz
     *            Class object to search for in cache.
     * @return true if the given argument is in cache, false otherwise.
     */
    public static boolean check(Class<?> clz) {
        if (Config.CACHE_SEEN_CLASSES_V) {
            int index = hash(clz);
            if (CACHE[index] == clz) {
                return true;
            }
            CACHE[index] = clz;
        }
        return false;
    }

    /**
     * Cleans cache.
     */
    public static void clean() {
        if (Config.CACHE_SEEN_CLASSES_V) {
            Arrays.fill(CACHE, null);
        }
    }

    // For testing only.
    protected static boolean isFull() {
        if (Config.CACHE_SEEN_CLASSES_V) {
            for (int i = 0; i < CACHE.length; i++) {
                if (CACHE[i] == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // For testing only.
    protected static Class<?>[] getClasses() {
        return CACHE;
    }

    // INTERNAL

    /**
     * Hashes the object, but hash is not more than allowed by size/mask.
     */
    protected static int hash(Object obj) {
        return System.identityHashCode(obj) & SIZE_MASK;
    }

    // MAIN

    /**
     * This (silly) method has been used (long back) to measure
     * performance.
     * 
     * @param args
     *            arguments.
     */
    public static void main(String[] args) {
        for (long l = 0L; l < 2000000000L; l++) {
            check(ClassesCache.class);
            // clean();
        }
    }
}
