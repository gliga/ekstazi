/*
 * Copyright 2017-present Milos Gligoric
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

package org.ekstazi.it;

import org.junit.Test;

import org.ekstazi.it.util.EkstaziPaths;

public class EndToEndFailsafeIT extends AbstractSurefireIT {

    @Test
    public void test() throws Exception {
        String testName = "failsafe-endtoend";
        String surefireVersion = "-DsurefireVersion=2.17";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanVerifyStep(testName, 0, 2, surefireVersion);
        executeCleanVerifyStep(testName, 0, 0, surefireVersion);

        // Due to (kind of) a bug in Maven Surefire, we have to clean
        // our test project.  Here is what happens otherwise: the
        // first time one invokes mvn verify (on Ekstazi project), we
        // build and compile both Ekstazi and test code.  If we run
        // mvn verify again, Maven finds .class file that belongs to
        // this integration test (not to Ekstazi) and tries to run it;
        // of course, the result is NoClassDefFoundError.
        executeCleanStep(testName, surefireVersion);
    }
}
