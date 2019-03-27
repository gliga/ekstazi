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

import junit.framework.TestCase;
import org.ekstazi.Ekstazi;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.RunnerBuilder;

/**
 * Provides support to check early if a class is affected.
 */
public class AffectingBuilder extends RunnerBuilder {
    @Override
    public Runner runnerForClass(Class<?> testClass) {
        // We check the following conditions:
        // 1. It is either TestCase subclass (JUnit3) or class without list of
        // other test classes (JUnit4), and 2. It is not affected with the
        // changes since the last run.
        if ((TestCase.class.isAssignableFrom(testClass) ||
                !testClass.isAnnotationPresent(SuiteClasses.class)) &&
                !Ekstazi.inst().isClassAffected(testClass.getName())) {
            return new AffectingRunner(testClass);
        } else {
            return null;
        }
    }
    
    /**
     * Adapter.
     */
    public static class JUnit4BuilderAffectingBuilder extends JUnit4Builder {
        @Override
        public Runner runnerForClass(Class<?> testClass) {
            return new AffectingBuilder().runnerForClass(testClass);
        }
    }
    

    /**
     * Adapter.
     */
    public static class JUnit3BuilderAffectingBuilder extends JUnit3Builder {
        @Override
        public Runner runnerForClass(Class<?> testClass) {
            return new AffectingBuilder().runnerForClass(testClass);
        }
    }
    
    /**
     * Adapter.
     */
    public static class AnnotatedBuilderAffectingBuilder extends AnnotatedBuilder {
        public AnnotatedBuilderAffectingBuilder(RunnerBuilder suiteBuilder) {
            super(suiteBuilder);
        }

        @Override
        public Runner runnerForClass(Class<?> testClass) {
            return new AffectingBuilder().runnerForClass(testClass);
        }
    }
}
