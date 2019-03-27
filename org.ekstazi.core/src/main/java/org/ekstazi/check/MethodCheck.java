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

package org.ekstazi.check;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ekstazi.data.RegData;
import org.ekstazi.data.Storer;
import org.ekstazi.hash.Hasher;
import org.ekstazi.log.Log;
import org.ekstazi.research.Research;

@Research
final class MethodCheck extends AbstractCheck {

    /** Test Abstraction */
    static class TestAbs {
        private boolean mIsAffected;
        private final Set<RegData> mRegData;
        private final String mFileName;
        private final String mFileDir;
        private final String mClassName;
        
        public TestAbs(boolean isAffected, Set<RegData> regData, String fileName, String fileDir, String className) {
            this.mIsAffected = isAffected;
            this.mRegData = regData;
            this.mFileName = fileName;
            this.mFileDir = fileDir;
            this.mClassName = className;
        }
        
        public boolean isAffected() { return mIsAffected; }
        public void setAffected(boolean b) { mIsAffected = b; }
        public Set<RegData> getRegData() { return mRegData; }
        public String getFileName() { return mFileName; }
        public String getFileDir() { return mFileDir; }
        public String getClassName() { return mClassName; }
    }

    private List<TestAbs> mTests;

    /**
     * Constructor.
     */
    public MethodCheck(Storer storer, Hasher hasher) {
        super(storer, hasher);
        this.mTests = new ArrayList<TestAbs>();
    }

    @Override
    public String includeAll(String fileName, String fileDir) {
        String className = null;
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            // TODO: This is the case when we hash name of the file; if we
            // save name of the original class in some mapping, we can still
            // do precise work.
            Log.w("Currently we do not support files that do not contain class names: " + fileName);
        } else {
            className = fileName.substring(0, index);
            String methodName = fileName.substring(index + 1);
            boolean isAffected = isAffected(fileDir, className, methodName);
            mTests.add(new TestAbs(isAffected, mStorer.load(fileDir, className, methodName), fileName, fileDir, className));
        }
        return className;
    }
    
    @Override
    public void includeAffected(Set<String> affectedClasses) {
        // Check if affected tests are really affected.
        List<TestAbs> affectedTests = getAffectedTests(mTests);
        List<TestAbs> nonAffectedTests = getNonAffectedTests(mTests);

        out: for (int i = 0; i < affectedTests.size(); i++) {
            TestAbs aTest = affectedTests.get(i);
            for (int j = 0; j < nonAffectedTests.size(); j++) {
                TestAbs naTest = nonAffectedTests.get(j);
                // If hash for any dependency between affected and non affected
                // differs, we should remove affected file.
                boolean anyDiff = checkForDifferences(aTest, naTest);
                if (anyDiff) {
                    new File(aTest.getFileDir(), aTest.getFileName()).delete();
                    // We remove flag that the test is affected not to include class later.
                    aTest.setAffected(false);
                    continue out;
                }
            }
        }
        
        for (TestAbs test : affectedTests) {
            if (test.isAffected()) affectedClasses.add(test.getClassName());
        }
    }
    
    private boolean checkForDifferences(TestAbs affected, TestAbs nonAffected) {
        for (RegData affectedDatum : affected.getRegData()) {
            for (RegData nonAffectedDatum : nonAffected.getRegData()) {
                if (affectedDatum.getURLExternalForm().equals(nonAffectedDatum.getURLExternalForm())
                        && !affectedDatum.getHash().equals(nonAffectedDatum.getHash())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private List<TestAbs> getAffectedTests(List<TestAbs> tests) {
        List<TestAbs> affectedTests = new ArrayList<MethodCheck.TestAbs>();
        for (int i = 0; i < tests.size(); i++) {
            TestAbs test = tests.get(i);
            if (test.isAffected()) affectedTests.add(test);
        }
        return affectedTests;
    }
    
    private List<TestAbs> getNonAffectedTests(List<TestAbs> tests) {
        List<TestAbs> nonAffectedTests = new ArrayList<MethodCheck.TestAbs>();
        for (int i = 0; i < tests.size(); i++) {
            TestAbs test = tests.get(i);
            if (!test.isAffected()) nonAffectedTests.add(test);
        }
        return nonAffectedTests;
    }
}
