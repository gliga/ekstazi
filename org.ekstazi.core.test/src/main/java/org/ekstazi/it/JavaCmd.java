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

import java.io.File;

import org.ekstazi.Config;
import org.ekstazi.it.util.EkstaziPaths;

public class JavaCmd extends AbstractCmd {

    /** Name of Java executable */
    public static final String JAVA = "java";

    /** Classpath */
    private final String[] mClasspath;

    /** Main class */
    private final String mMain;

    private final Config.AgentMode mEkstaziMode;

    private final String mEkstaziOptions;

    /**
     * Constructor.
     */
    public JavaCmd(File cwd, String[] classpath, String main, Config.AgentMode ekstaziMode, String ekstaziOptions) {
        super(cwd);
        this.mClasspath = classpath;
        this.mMain = main;
        this.mEkstaziMode = ekstaziMode;
        this.mEkstaziOptions = ekstaziOptions;
    }

    protected String[] getCommand() {
        List<String> command = new ArrayList<String>();
        command.add(JAVA);
        String ekstaziOptions = mEkstaziOptions;
        if (!ekstaziOptions.equals("")) {
            ekstaziOptions = Config.OPTION_SEPARATOR + ekstaziOptions;
        }
        command.add("-javaagent:" + EkstaziPaths.getEkstaziCoreJarPath() + "=mode=" + mEkstaziMode + ekstaziOptions);
        if (mClasspath != null) {
            command.add("-cp");
            command.add(join(mClasspath, System.getProperty("path.separator")));
        }
        command.add(mMain);

        return command.toArray(new String[command.size()]);
    }
}
