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

package org.ekstazi.it;

import org.junit.Assert;
import org.junit.Test;

import org.ekstazi.Names;
import org.ekstazi.it.util.EkstaziPaths;

public class VersionSurefireIT extends AbstractSurefireIT {

    @Test
    public void testLightekstaziWithSurefire2_17() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.17";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4, surefireVersion);
        executeCleanTestStep(testName, 0, 0, surefireVersion);
    }

    @Test
    public void testLightekstaziWithSurefire2_10() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.10";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4, surefireVersion);
        executeCleanTestStep(testName, 0, 0, surefireVersion);
    }

    @Test
    public void testLightekstaziWithSurefire2_7() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.7";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4, surefireVersion);
        executeCleanTestStep(testName, 0, 0, surefireVersion);
    }

    @Test
    public void testLightekstaziWithSurefire2_6() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.6";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4, surefireVersion);
        executeCleanTestStep(testName, 0, 0, surefireVersion);
    }

    @Test
    public void testLightekstaziWithSurefire2_4() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.4";
        
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4, surefireVersion);
        executeCleanTestStep(testName, 0, 0, surefireVersion);
    }

    @Test
    public void testLightekstaziWithSurefire2_3() throws Exception {
        String testName = "lightekstazi";
        String surefireVersion = "-DsurefireVersion=2.3";

        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 1, 0, surefireVersion);
    }
}
