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

/**
 * Listener that collects outcome of the tests. If at least one test method
 * fail/error, the outcome is fail/error. It may be obvious, but we create one
 * instance of this listener per test class.
 * 
 * Note that we need to have a separate listener classes for JUnit3 and JUnit4,
 * because of class loading when using JUnit3 (when JUnit4 classes are not
 * available).
 */
public interface OutcomeListener {

    /**
     * Test outcomes.
     */
    public static enum Outcome {
        PASS, FAIL, ERROR,
    }

    // GET INFO ABOUT OUTCOMES

    /**
     * Checks if all tests passed.
     * 
     * @return True if all tests passed, false otherwise.
     */
    public boolean isPass();

    /**
     * Checks if any test failed.
     * 
     * @return True if any test failed, false otherwise.
     */
    public boolean isFail();

    /**
     * Checks if any test had an error.
     * 
     * @return True if any test had an error, false otherwise.
     */
    public boolean isError();

    /**
     * Check if method failed or had an error.
     * 
     * @return True if either {@link #isFail} or {@link isError} returned true.
     */
    public boolean isFailOrError();
}
