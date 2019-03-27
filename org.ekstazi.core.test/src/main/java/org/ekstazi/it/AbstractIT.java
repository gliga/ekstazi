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
import org.junit.Before;

import org.ekstazi.Config;
import org.ekstazi.Names;
import org.ekstazi.data.RegData;
import org.ekstazi.data.Storer;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * Top of the hierarchy for some integration tests.
 */
public abstract class AbstractIT {

    @Before
    public void setUp() {
        // Make sure that .ekstazirc is not in $HOME because it can
        // impact configuration of our execution.
        File ekstazirc = new File(System.getProperty("user.home") + System.getProperty("file.separator") + Names.EKSTAZI_CONFIG_FILE);
        ekstazirc.delete();
    }

    protected final File getTestDir(String testName) throws Exception {
        URL url = getClass().getResource("/" + testName + "/README.txt");
        File readme = new File(url.toURI());
        return readme.getParentFile();
    }

    protected abstract int getNumOfTests(String[] lines);

    // DEPENDENCIES

    protected boolean dependencyExists(String testName, String dependencyFileName, String dependencyName) throws Exception {
        Storer storer = Config.createStorer();
        Set<RegData> data = storer.load(getTestDir(testName) + System.getProperty("file.separator") + Names.EKSTAZI_ROOT_DIR_NAME, dependencyFileName);
        for (RegData datum : data) {
            if (datum.getURLExternalForm().contains(dependencyName)) {
                return true;
            }
        }
        return false;
    }
}
