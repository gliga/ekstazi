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
import java.io.IOException;
import java.io.InputStreamReader;

import org.ekstazi.util.FileUtil;

public class JavacCmd extends AbstractCmd {

    /** Name of Java compiler executable */
    public static final String JAVAC = "javac";

    /** Classpath */
    public final String[] mClasspath;

    /** Java files to compile */
    public final String[] mFiles;

    /**
     * Constructor.
     */
    public JavacCmd(File cwd, String[] classpath, String[] files) {
        super(cwd);
        mClasspath = classpath;
        mFiles = files;
    }

    protected String[] getCommand() {
        List<String> command = new ArrayList<String>();
        command.add(JAVAC);
        if (mClasspath != null) {
            command.add("-cp");
            command.add(join(mClasspath, System.getProperty("path.separator")));
        }
        command.addAll(Arrays.asList(mFiles));

        return command.toArray(new String[command.size()]);
    }
}
