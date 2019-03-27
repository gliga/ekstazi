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

package org.ekstazi.maven;

public final class MavenNames {
    // Surefire.
    public static final String SUREFIRE_PLUGIN_VM = "org/apache/maven/plugin/surefire/SurefirePlugin";
    public static final String SUREFIRE_PLUGIN_BIN = "org.apache.maven.plugin.surefire.SurefirePlugin";

    public static final String FAILSAFE_PLUGIN_VM = "org/apache/maven/plugin/failsafe/IntegrationTestMojo";
    public static final String FAILSAFE_PLUGIN_BIN = "org.apache.maven.plugin.failsafe.IntegrationTestMojo";

    public static final String ABSTRACT_SUREFIRE_MOJO_VM = "org/apache/maven/plugin/surefire/AbstractSurefireMojo";
    public static final String SUREFIRE_INTERCEPTOR_CLASS_VM = "org/ekstazi/maven/SurefireMojoInterceptor";

    // ScalaTest.
    public static final String TESTMOJO_VM = "org/scalatest/tools/maven/TestMojo";
    public static final String SCALATEST_INTERCEPTOR_CLASS_VM = "org/ekstazi/maven/ScalaTestMojoInterceptor";

    public static final String MOJO_EXECUTION_EXCEPTION_BIN = "org.apache.maven.plugin.MojoExecutionException";

    public static final String EXECUTE_MNAME = "execute";
    public static final String EXECUTE_MDESC = "()V";
}
