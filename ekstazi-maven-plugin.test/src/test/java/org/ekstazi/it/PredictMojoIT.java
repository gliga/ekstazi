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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ekstazi.it.util.EkstaziPaths;

/**
 * Check correctness of the prediction goal.
 */
public class PredictMojoIT extends AbstractSurefireIT {

    /** Pattern for the number of tests */
    protected static final Pattern NON_AFFECTED_CLASS = Pattern.compile(".*NonAffected::.*");

    @Test
    public void test() throws Exception {
        String testName = "ekstazi-predict";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 1);
        executeCleanTestStep(testName, 0, 1);
    }

    protected void executePhases(String testName, int expectedExitStatus, int expectedNumOfNonAffectedClasses, String... options) throws Exception {
        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.EKSTAZIPREDICT };
        MavenCmd maven = new MavenCmd(getTestDir(testName), phases, options);
        maven.execute();
        Assert.assertEquals(expectedExitStatus, maven.getExitStatus());
        int actualNumOfNonAffectedClasses = getNumOfNonAffectedClasses(maven.getOutput());
        Assert.assertEquals(expectedNumOfNonAffectedClasses, actualNumOfNonAffectedClasses);
    }

    /**
     * Returns the number of lines that match a pattern.
     */
    protected int getNumOfNonAffectedClasses(String[] lines) {
        int count = 0;
        for (String line : lines) {
            Matcher matcher = NON_AFFECTED_CLASS.matcher(line);
            if (matcher.matches()) {
                count++;
            }
        }
        return count;
    }
}
