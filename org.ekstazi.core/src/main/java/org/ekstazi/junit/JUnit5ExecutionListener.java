/*
 * Copyright 2022-present Milos Gligoric
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

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestDescriptor;

/**
 * Separate from JUnit5 because of class loading.
 */
// TODO: TestExecutionListener
// https://github.com/junit-team/junit5/blob/main/junit-platform-launcher/src/main/java/org/junit/platform/launcher/TestExecutionListener.java
public class JUnit5ExecutionListener implements EngineExecutionListener, OutcomeListener {

    /** Outcome of the execution */
    private Outcome mOutcome = Outcome.PASS;

    // EngineExecutionListener Interface.
    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED
                || testExecutionResult.getStatus() == TestExecutionResult.Status.ABORTED) {
            mOutcome = Outcome.FAIL;
        }
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
