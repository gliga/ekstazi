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

public class MavenCmd extends AbstractCmd {

    public static enum Phase {
        CLEAN("clean"),
        EKSTAZIEKSTAZI("ekstazi:ekstazi"),
        EKSTAZIPREDICT("ekstazi:predict"),
        TEST("test"),
        TESTCOMPILE("test-compile"),
        VERIFY("verify");

        private String mName;

        Phase(String name) {
            this.mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    /** Constant */
    public static final String CMD = "mvn";

    /** Maven phase to run */
    private final Phase[] mPhases;

    /** Options passed to maven command */
    private final String[] mOptions;

    public MavenCmd(File cwd, Phase phase) {
        this(cwd, phase, null);
    }

    public MavenCmd(File cwd, Phase phase, String[] options) {
        this(cwd, new Phase[] { phase }, options);
    }

    /**
     * Constructor.
     */
    public MavenCmd(File cwd, Phase[] phases) {
        this(cwd, phases, null);
    }

    /**
     * Constructor.
     */
    public MavenCmd(File cwd, Phase[] phases, String[] options) {
        super(cwd);
        this.mPhases = phases;
        this.mOptions = options;
    }

    @Override
    protected String[] getCommand() {
        List<String> command = new ArrayList<String>();
        command.add(CMD);
        command.add("-DekstaziVersion=" + Names.TOOL_VERSION);
        if (mOptions != null) {
            command.addAll(Arrays.asList(mOptions));
        }
        for (Phase phase : mPhases) {
            command.add(phase.getName());            
        }
        return command.toArray(new String[command.size()]);
    }
}
