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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;

import org.ekstazi.Config;
import org.ekstazi.it.util.EkstaziPaths;

public class JUnitCmd extends JavaCmd {

    private final String mTestClass;

    /**
     * Constructor.
     */
    public JUnitCmd(File cwd, String[] classpath, String testClass, Config.AgentMode ekstaziMode, String ekstaziOptions) {
        super(cwd, classpath, "org.junit.runner.JUnitCore", ekstaziMode, ekstaziOptions);
        this.mTestClass = testClass;
    }

    @Override
    protected String[] getCommand() {
        // TODO: this is not nice; we should have template methods for
        // all arguments in Java that we can override.
        List<String> command = new ArrayList<String>(Arrays.asList(super.getCommand()));
        command.add(mTestClass);
        return command.toArray(new String[command.size()]);
    }
}
