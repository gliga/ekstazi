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

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.ekstazi.Config;
import org.ekstazi.Ekstazi;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * Support for collecting coverage for test class granularity level.
 */
public class CoverageExtension implements InvocationInterceptor, AfterAllCallback, ExecutionCondition {

    @Override
    /** 
     * tell Ekstazi to start collecting coverage for test class granularity level.
     * @param invocation
     * @param invocationContext
     * @param extensionContext
     */
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        T o = invocation.proceed();
        Ekstazi.inst().beginClassCoverage(o.getClass().getName());
        return o;
    }

    @Override
    /**
     * tell Ekstazi the execution result
     * @param context
     * @return
     */
    public void afterAll(ExtensionContext context) throws Exception {
        Optional<Throwable> exception = context.getExecutionException();
        String className = context.getTestClass().get().getName();
        if (exception.isPresent()) {
            Ekstazi.inst().endClassCoverage(className, true);
        } else {
            Ekstazi.inst().endClassCoverage(className, false);
        }
    }

    /**
     * Check if for any reason we want to ignore all the tests. We ignore all
     * the tests if a flag is set for Tool; note however that this flag is only
     * for testing/debugging/weird purposes.
     */
    private boolean isIgnoreAllTests() {
        return Config.X_IGNORE_ALL_TESTS_V;
    }

    @Override
    /**
     * Skip the test if all tests are ignored or test is not affected.
     * @param context
     * @return
     */
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (isIgnoreAllTests() || (context.getTestClass().isPresent()
                && !Ekstazi.inst().isClassAffected(context.getTestClass().get().getName()))) {
            return ConditionEvaluationResult.disabled("Ekstazi deselected");
        } else {
            return ConditionEvaluationResult.enabled("Ekstazi selected");
        }
    }

}
