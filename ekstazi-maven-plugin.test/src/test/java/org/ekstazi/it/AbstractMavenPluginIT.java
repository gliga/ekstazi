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

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.ekstazi.Names;

/**
 * Integration tests with Maven plugin.
 */
public abstract class AbstractMavenPluginIT extends AbstractIT {

    private void executeStep(String testName, int expectedExitStatus, int expectedNumOfTests, MavenCmd.Phase[] phases, String... options) throws Exception {
        MavenCmd maven = new MavenCmd(getTestDir(testName), phases, options);
        maven.execute();
        Assert.assertEquals(expectedExitStatus, maven.getExitStatus());
        int actualNumOfTests = getNumOfTests(maven.getOutput());
        Assert.assertEquals(expectedNumOfTests, actualNumOfTests);
    }

    protected void executeCleanTestStep(String testName, int expectedExitStatus, int expectedNumOfTests, String... options) throws Exception {
        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.CLEAN, MavenCmd.Phase.TEST };
        executeStep(testName, expectedExitStatus, expectedNumOfTests, phases, options);
    }

    protected void executeCleanVerifyStep(String testName, int expectedExitStatus, int expectedNumOfTests, String... options) throws Exception {
        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.CLEAN, MavenCmd.Phase.VERIFY };
        executeStep(testName, expectedExitStatus, expectedNumOfTests, phases, options);
    }

    protected void executeCleanStep(String testName, String... options) throws Exception {
        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.CLEAN };
        executeStep(testName, 0, 0, phases, options);
    }
}
