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

/**
 * Integration tests with Maven plugin.
 */
public class MavenPluginIT extends AbstractSurefireIT {

    @Test
    public void testParentPom() throws Exception {
        String testName = "parentpom";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 7);
        // Check that nothing is selected.
        executeCleanTestStep(testName, 0, 0, "-o");
    }

    @Test
    public void testForcefailing() throws Exception {
        String testName = "forcefailing";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 1, 2, "-Dekstazi.forcefailing=true");
        // Check that tests are selected again.
        executeCleanTestStep(testName, 1, 2, "-Dekstazi.forcefailing=true", "-o");
    }

    @Test
    public void testForceall() throws Exception {
        String testName = "forceall";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 2, "-Dekstazi.forceall=true");
        // Check that tests are selected again.
        executeCleanTestStep(testName, 0, 2, "-Dekstazi.forceall=true", "-o");
    }

    @Test
    public void testChildrenpom() throws Exception {
        String testName = "childrenpom";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 7);
        executeCleanTestStep(testName, 0, 0);
    }

    @Test
    public void testParallelparam() throws Exception {
        String testName = "parallelparam";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4);
        executeCleanTestStep(testName, 0, 0);
        // TODO: Check warning
        // TODO: Check threads
    }

    @Test
    public void testForkmode() throws Exception {
        String testName = "forkmode";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 1, 0);
    }

    @Test
    public void testForkcount() throws Exception {
        String testName = "forkcount";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 1, 0);
    }

    // Tests that Ekstazi excludes inner classes too if there is no
    // excludes specified by the user.
    @Test
    public void testExcludesdefault() throws Exception {
        String testName = "excludesdefault";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 2);
    }

    // Tests that Ekstazi excludes classes properly when user already
    // excludes some classes in pom.xml.
    @Test
    public void testExcludesuser() throws Exception {
        String testName = "excludesuser";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 2);
    }

    // Tests that Ekstazi sets agent mode properly.
    @Test
    public void testReuseforkstrue() throws Exception {
        String testName = "reuseforkstrue";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 1);
    }

    // Tests that Ekstazi sets agent mode properly.
    @Test
    public void testReuseforksfalse() throws Exception {
        String testName = "reuseforksfalse";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 1);
    }

    // Tests that Ekstazi modifies argLine properly.
    @Test
    public void testArgline() throws Exception {
        String testName = "argline";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 1);
    }

    // Tests that Ekstazi instruments Surefire properly even if
    // surefire was run in different module without Ekstazi.
    @Test
    public void testOnechildekstazi() throws Exception {
        String testName = "onechildekstazi";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 7);
        executeCleanTestStep(testName, 0, 4);
    }

    // Tests Ekstazi lifecycle.
    @Test
    public void testEkstazilifecycle() throws Exception {
        String testName = "ekstazilifecycle";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.CLEAN, MavenCmd.Phase.EKSTAZIEKSTAZI };
        // First run.
        MavenCmd maven = new MavenCmd(getTestDir(testName), phases);
        maven.execute();
        Assert.assertTrue(maven.isSuccess());
        int actualNumOfTests = getNumOfTests(maven.getOutput());
        Assert.assertEquals(4, actualNumOfTests);
        // Second run.
        maven = new MavenCmd(getTestDir(testName), phases);
        maven.execute();
        Assert.assertTrue(maven.isSuccess());
        actualNumOfTests = getNumOfTests(maven.getOutput());
        Assert.assertEquals(0, actualNumOfTests);
    }

    @Test
    public void testJavacbug() throws Exception {
        // TODO: depends on Maven version; check that
        String testName = "javacbug";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);

        MavenCmd.Phase[] phases = new MavenCmd.Phase[] { MavenCmd.Phase.CLEAN, MavenCmd.Phase.TESTCOMPILE };
        MavenCmd maven = new MavenCmd(getTestDir(testName), phases);
        maven.execute();
        Assert.assertTrue(maven.isSuccess());
        
        phases = new MavenCmd.Phase[] { MavenCmd.Phase.TEST, MavenCmd.Phase.CLEAN, MavenCmd.Phase.TESTCOMPILE };
        String[] options = new String[] { "-Pekstazi" };
        maven = new MavenCmd(getTestDir(testName), phases, options);
        maven.execute();
        Assert.assertTrue(maven.isSuccess());
    }

    // Tests that parentdir can be changed but also removed when
    // "clean" is invoked.
    @Test
    public void testParentdirtarget() throws Exception {
        String testName = "parentdirtarget";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4);
        // Note that clean will remove .ekstazi directory.
        executeCleanTestStep(testName, 0, 4);
    }

    @Test
    public void testParentdirtmp() throws Exception {
        String testName = "parentdirtmp";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);
        executeCleanTestStep(testName, 0, 4);
        executeCleanTestStep(testName, 0, 0);
    }
}
