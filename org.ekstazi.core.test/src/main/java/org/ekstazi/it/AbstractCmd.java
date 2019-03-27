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
import java.io.IOException;
import java.io.InputStreamReader;

import org.ekstazi.util.FileUtil;

public abstract class AbstractCmd {
    
    /** Current working directory */
    private final File mCwd;

    /** Collected output from the *latest* run */
    private String[] mOutput;

    /** Exception during subprocess execution (if any) */
    private Exception mException;

    /** Exit code of the command */
    private int mExitCode = -1;

    public AbstractCmd(File cwd) {
        this.mCwd = cwd;
    }

    /**
     * Executes the command and collect output.
     */
    public final void execute() {
        try {
            String[] command = getCommand();
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(mCwd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            // Read command output.  From Java documentation: "Because
            // some native platforms only provide limited buffer size
            // for standard input and output streams, failure to
            // promptly write the input stream or read the output
            // stream of the subprocess may cause the subprocess to
            // block, and even deadlock."
            mOutput = FileUtil.readLines(new InputStreamReader(p.getInputStream()));
            // Wait for the process to be done.
            p.waitFor();
            mExitCode = p.exitValue();
        } catch (Exception ex) {
            mException = ex;
        }
    }

    public final boolean isSuccess() {
        return (mExitCode == 0 && mException == null);
    }

    public final int getExitStatus() {
        return mExitCode;
    }

    public final String getExceptionMessage() {
        return (mException != null) ? mException.getMessage() : null;
    }

    public final String[] getOutput() {
        return mOutput;
    }

    public final String getOutputAsString() {
        StringBuilder sb = new StringBuilder();
        for (String str : mOutput) {
            sb.append(str);
            sb.append('\n');
        }
        return sb.toString();
    }

    protected abstract String[] getCommand();

    public final String getCommandAsString() {
        StringBuilder sb = new StringBuilder();
        for (String str : getCommand()) {
            sb.append(str);
            sb.append('\n');
        }
        return sb.toString();
    }

    protected String join(String[] args, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
