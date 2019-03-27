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

import org.ekstazi.Config;

/**
 * Interceptor for ScalaTest mojos. The execute method of this class is invoked
 * before execute of test mojo in ScalaTest. This is used to check if
 * configuration is supported by Ekstazi, and also to configure Ekstazi agent.
 */
public class ScalaTestMojoInterceptor extends AbstractMojoInterceptor {

    /** parallel field name in ScalaTest mojo */
    private static final String PARALLEL_FIELD = "parallel";
    /** forkMode field name in ScalaTest mojo */
    private static final String FORKMODE_FIELD = "forkMode";

    public static void execute(Object mojo) throws Exception {
        // Check version.
        checkVersion(mojo);
        // Check configuration
        checkConfiguration(mojo);

        try {
            // Update argLine.
            updateArgLine(mojo);
            // Update parallel.
            updateParallel(mojo);
        } catch (Exception ex) {
            throwMojoExecutionException(mojo, "Usupported ScalaTest version", ex);
        }

        // TODO: There is no excludes in ScalaTest, but we may use
        // 'suffixes' to filter suites. The following would be a starting point
        // (where AB and CD are names of test suites to be excluded):

        // Pattern p = Pattern.compile("(?<!^AB)(?<!^CD)$");
        // Matcher m = p.matcher(args[0]);
    }

    // INTERNAL

    private static void checkVersion(Object mojo) throws Exception {
        try {
            mojo.getClass().getSuperclass().getDeclaredField(ARGLINE_FIELD);
            mojo.getClass().getSuperclass().getDeclaredField(PARALLEL_FIELD);
            mojo.getClass().getSuperclass().getDeclaredField(FORKMODE_FIELD);
        } catch (NoSuchFieldException ex) {
            throwMojoExecutionException(mojo, "Unsupported ScalaTest maven plugin version.", ex);
        }
    }

    private static void checkConfiguration(Object mojo) throws Exception {
        // forkMode has to be 'once'.
        String forkMode = (String) getField(FORKMODE_FIELD, mojo);
        if (forkMode == null || !forkMode.equals("once")) {
            throwMojoExecutionException(mojo,
                    "Fork has to be enabled when running tests with Ekstazi; check forkMode parameter.", null);
        }
    }

    private static void updateParallel(Object mojo) throws Exception {
        Boolean currentParallel = (Boolean) getField(PARALLEL_FIELD, mojo);
        if (currentParallel) {
            warn(mojo,
                    "Ekstazi does not support parallel parameter.  This parameter will be set to false for this run.");
            setField(PARALLEL_FIELD, mojo, false);
        }
    }

    private static void updateArgLine(Object mojo) throws Exception {
        String currentArgLine = (String) getField(ARGLINE_FIELD, mojo);
        // Note that ScalaTest maven plugin does not support
        // fork option at this point.
        String newArgLine = makeArgLine(mojo, Config.AgentMode.SCALATEST, currentArgLine);
        setField(ARGLINE_FIELD, mojo, newArgLine);
    }
}
