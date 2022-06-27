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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

/**
 * Support for collecting coverage for test class granularity level.
 */
public class CoverageExtension implements InvocationInterceptor, AfterAllCallback{
    private Class mClz;

    // @Override
    // public void beforeAll(ExtensionContext context) throws Exception {  
    //     if (isIgnoreAllTests() || !Ekstazi.inst().isClassAffected(mClz.getName())) {
            
    //     } 
    // }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        T o = invocation.proceed();
        mClz = o.getClass();
        // System.out.println("interceptTestClassConstructor, class: " +
        // mClz.getName());
        Ekstazi.inst().beginClassCoverage(mClz.getName());
        return o;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("after all methods, class: " + mClz);
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent()) {
            Ekstazi.inst().endClassCoverage(mClz.getName(), true);
        } else {
            Ekstazi.inst().endClassCoverage(mClz.getName(), false);
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

}
