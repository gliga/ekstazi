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

public class ParameterizedJUnitIT extends AbstractJUnitIT {

    @Test
    public void testParameterized() throws Exception {
        String testName = "parameterized";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);

        int expectedNumOfTests = 2;
        javacJUnit(testName, expectedNumOfTests, "CTest.java", "C1.java");
        javacJUnit(testName, expectedNumOfTests, "CTest.java", "C2.java");
        expectedNumOfTests = 0;
        javacJUnit(testName, expectedNumOfTests, "CTest.java", "C3.java");
    }
}
