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

import org.ekstazi.Config;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Top of the hierarchy for integration tests that are running JUnit
 * runner(s).
 */
public abstract class AbstractJUnitIT extends AbstractIT {

    private static final String JUNIT_JAR = "../junit-4.10.jar";

    /** Pattern for the number of tests reported by JUnit */
    private static final Pattern TESTS_RUN_LINE = Pattern.compile("OK \\((\\d+) tests?\\)");

    /**
     * Returns the number of tests based on the maven output.
     */
    @Override
    protected int getNumOfTests(String[] lines) {
        int numOfTests = 0;
        for (String line : lines) {
            Matcher matcher = TESTS_RUN_LINE.matcher(line);
            if (matcher.matches()) {
                numOfTests += Integer.parseInt(matcher.group(1));
            }
        }
        return numOfTests;
    }

    protected void javacJUnit(String testName, int expectedNumOfTests, String testClass, String codeUnderTest) throws Exception {
        javacJUnit(testName, expectedNumOfTests, testClass, new String[] { codeUnderTest }, "");
    }

    protected void javacJUnit(String testName, int expectedNumOfTests, String testClass, String codeUnderTest[]) throws Exception {
        javacJUnit(testName, expectedNumOfTests, testClass, codeUnderTest, "");
    }

    protected void javacJUnit(String testName, int expectedNumOfTests, String testClass, String codeUnderTest, String ekstaziOptions) throws Exception {
        javacJUnit(testName, expectedNumOfTests, testClass, new String[] { codeUnderTest }, ekstaziOptions);
    }

    protected void javacJUnit(String testName, int expectedNumOfTests, String testClass, String codeUnderTest[], String ekstaziOptions) throws Exception {
        File testdir = getTestDir(testName);
        String[] classpath = new String[] { JUNIT_JAR, "." };

        ArrayList<String> files = new ArrayList(Arrays.asList(codeUnderTest));
        files.add(testClass);

        JavacCmd javac = new JavacCmd(testdir, classpath, files.toArray(new String[0]));
        javac.execute();
        Assert.assertTrue("Unsuccessful compilation: " +
                          javac.getCommandAsString() + " | " +
                          javac.getOutputAsString(),
                          javac.isSuccess());

        JUnitCmd junit = new JUnitCmd(testdir, classpath, testClass.replace(".java", ""), Config.AgentMode.JUNIT, ekstaziOptions);
        junit.execute();
        Assert.assertTrue("Unsuccessful JUnit run: " +
                          junit.getCommandAsString() + " | " +
                          junit.getOutputAsString(),
                          junit.isSuccess());
        Assert.assertEquals(expectedNumOfTests, getNumOfTests(junit.getOutput()));
    }
}
