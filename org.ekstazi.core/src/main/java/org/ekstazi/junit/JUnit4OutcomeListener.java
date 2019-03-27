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

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Separate from JUnit3 because of class loading.
 */
public class JUnit4OutcomeListener extends RunListener implements OutcomeListener {

    /** Outcome of the execution */
    private Outcome mOutcome = Outcome.PASS;

    // RunListener Interface.

    @Override
    public void testFailure(Failure failure) throws Exception {
        mOutcome = Outcome.FAIL;
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        // Not sure if we want to mark test as fail or not.
        mOutcome = Outcome.PASS;
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
        return false;
    }

    @Override
    public boolean isFailOrError() {
        return isFail() || isError();
    }
}
