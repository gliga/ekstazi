/*
 * Copyright 2018-present Milos Gligoric
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

/**
 * Test for custom JUnit runner.
 */
public class MyRunnerJUnitIT extends AbstractJUnitIT {

    @Test
    public void testRunner() throws Exception {
        String testName = "myrunner";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);

        String[] src = new String[] { "MyTestRunner.java", "C1.java" };
        int expectedNumOfTests = 1;
        javacJUnit(testName, expectedNumOfTests, "CTest.java", src);
        expectedNumOfTests = 0;
        javacJUnit(testName, expectedNumOfTests, "CTest.java", src);

        src = new String[] { "MyTestRunner.java", "C2.java" };
        expectedNumOfTests = 1;
        javacJUnit(testName, expectedNumOfTests, "CTest.java", src);
    }
}
