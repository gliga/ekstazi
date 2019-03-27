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

package org.ekstazi.junit;

import junit.framework.TestSuite;

/**
 * Has to be separate from {@link JUnit4Monitor} because of class loading.
 */
public class JUnit3Monitor {

    /**
     * JUnit3 support.
     * 
     * Intercept "addTestsFromTestCase" in {@link TestSuite}. Instead of adding
     * methods from the class it return a wrapped that will create test methods
     * when test run is initiated. We also check that the invocation is not from
     * one of the builders, in which case we do not want to change the behavior
     * (as it may interfer with support for JUnit4).
     */
    public static boolean addTestsFromTestCase(final TestSuite testSuite, final Class<?> testClass) {
        if (isOnStack(0, "org.junit.internal.builders.") || isOnStack(0, JUnit3TestSuite.class.getCanonicalName())) {
            return true;
        } else {
            testSuite.addTest(new JUnit3TestSuite(testClass));
            return false;
        }
    }

    /**
     * Intentionally duplicate.
     */
    private static boolean isOnStack(int moreThan, String canonicalName) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int count = 0;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith(canonicalName)) {
                count++;
            }
        }
        return count > moreThan;
    }
}
