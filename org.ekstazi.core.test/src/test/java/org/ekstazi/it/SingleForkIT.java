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

import java.io.File;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.ekstazi.Config;
import org.ekstazi.data.DependencyAnalyzer;
import org.ekstazi.it.util.EkstaziPaths;

public class SingleForkIT extends AbstractIT {

    @Override
    protected int getNumOfTests(String[] lines) {
        // No test is executed, as we run java command.
        return 0;
    }

    @Test
    public void testSinglefork() throws Exception {
        String testName = "singlefork";
        EkstaziPaths.removeEkstaziDirectories(getClass(), testName);

        javacJava(testName, "CTest.java", "C1.java", "");
        Assert.assertTrue(dependencyExists(testName, Config.SINGLE_NAME_V + "." + DependencyAnalyzer.COV_EXT, "C1.class"));
        Assert.assertTrue(dependencyExists(testName, Config.SINGLE_NAME_V + "." + DependencyAnalyzer.COV_EXT, "CTest.class"));
    }

    protected void javacJava(String testName, String testClass, String cut, String ekstaziOptions) throws Exception {
        File testdir = getTestDir(testName);
        String[] classpath = new String[] { "." };
        String[] files = new String[] { testClass, cut };
        JavacCmd javac = new JavacCmd(testdir, classpath, files);
        javac.execute();
        Assert.assertTrue(javac.isSuccess());

        String main = testClass.replace(".java", "");
        JavaCmd java = new JavaCmd(testdir, classpath, main, Config.AgentMode.SINGLEFORK, ekstaziOptions);
        java.execute();
        Assert.assertTrue(java.isSuccess());
    }
}
