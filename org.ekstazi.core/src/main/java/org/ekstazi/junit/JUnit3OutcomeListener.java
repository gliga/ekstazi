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

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

/**
 * Separate from JUnit4 because of class loading.
 */
public class JUnit3OutcomeListener implements OutcomeListener, TestListener {

    /** The result of the execution */
    private Outcome mOutcome = Outcome.PASS;

    // TestListener Interface.

    @Override
    public void addError(Test test, Throwable t) {
        mOutcome = Outcome.ERROR;
    }

    @Override
    public void addFailure(Test test, AssertionFailedError t) {
        mOutcome = Outcome.FAIL;
    }

    @Override
    public void endTest(Test test) {
        // Nothing.
    }

    @Override
    public void startTest(Test test) {
        // Nothing.
    }

    // OutcomeListener Interface.

    @Override
    public boolean isPass() {
        return mOutcome == Outcome.PASS;
    }

    @Override
    public boolean isFail() {
        return mOutcome == Outcome.FAIL;
    }

    @Override
    public boolean isError() {
        return mOutcome == Outcome.ERROR;
    }

    @Override
    public boolean isFailOrError() {
        return isFail() || isError();
    }
}
