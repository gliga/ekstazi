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

import org.ekstazi.monitor.CoverageMonitor;
import org.junit.runner.Runner;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.RunnerBuilder;

public class JUnit4Monitor {

    /* Depth of calls in this class to support Suite.class.
     * Unfortunately it is static now, but we may want to do
     * ThreadLocal eventually. */
    private static int recursiveDepth  = 0;

    public static Runner runnerForClass(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        // Support for Suite.class.  We want to ignore it, as we want
        // to run classes that are in the Suite and not Suite itself.
        RunWith anno = testClass.getAnnotation(RunWith.class);
        if (anno != null && anno.value() == Suite.class) {
            return builder.runnerForClass(testClass);
        }
        try {
            recursiveDepth++;
            return runnerForClass0(builder, testClass);
        } finally {
            recursiveDepth--;
        }
    }
    
    /**
     * JUnit4 support.
     */
    public static Runner runnerForClass0(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        if (recursiveDepth > 1 ||
            isOnStack(0, CoverageRunner.class.getCanonicalName())) {
            return builder.runnerForClass(testClass);
        }
        AffectingBuilder affectingBuilder = new AffectingBuilder();
        Runner runner = affectingBuilder.runnerForClass(testClass);
        if (runner != null) {
            return runner;
        }
        CoverageMonitor.clean();
        Runner wrapped = builder.runnerForClass(testClass);
        return new CoverageRunner(testClass, wrapped, CoverageMonitor.getURLs());
    }
    
    /**
     * Checks if the given name is on stack more than the given number of times.
     * This method uses startsWith to check if the given name is on stack, so
     * one can pass a package name too.
     *
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
