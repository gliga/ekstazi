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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.ekstazi.Names;

public class AntCmd extends AbstractCmd {

    /** Constant */
    public static final String CMD = "ant";

    /** Constant for test task */
    public static final String TEST = "test";

    /** Ant task to run */
    private final String mPhase;

    /** Options passed to maven command */
    private final String[] mOptions;

    /**
     * Constructor.
     */
    public AntCmd(File cwd, String phase) {
        this(cwd, phase, null);
    }

    /**
     * Constructor.
     */
    public AntCmd(File cwd, String phase, String[] options) {
        super(cwd);
        this.mPhase = phase;
        this.mOptions = options;
    }

    @Override
    protected String[] getCommand() {
        List<String> command = new ArrayList<String>();
        command.add(CMD);
        if (mOptions != null) {
            command.addAll(Arrays.asList(mOptions));
        }
        command.add(mPhase);
        return command.toArray(new String[command.size()]);
    }
}
