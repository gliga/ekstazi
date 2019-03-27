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

package org.ekstazi.data;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link TxtStorer} that tries to save the number of printed characters by
 * detecting common prefix for several urls. Note that we assume sorted lines.
 * 
 * IMPORTANT: This class is *not* stateless.
 */
public class PrefixTxtStorer extends TxtStorer {

    private static class PrefixState extends State {
        /** Last seen prefix */
        private String lastPrefix;
    }

    /**
     * Constructor.
     */
    public PrefixTxtStorer() {
        super(Storer.Mode.PREFIX_TXT);
    }

    /**
     * Constructor.
     */
    public PrefixTxtStorer(Mode mode) {
        super(mode);
    }
    
    // STORE

    @Override
    protected void printLine(State state, Writer pw, String externalForm, String hash) throws IOException {
        PrefixState prefixState = (PrefixState) state;
        int ix = externalForm.lastIndexOf('/');
        String prefix = externalForm.substring(0, ix);
        String name = externalForm.substring(ix);
        // If new prefix, print it.
        if (!prefix.equals(prefixState.lastPrefix)) {
            prefixState.lastPrefix = prefix;
            printPrefix(pw, prefix);
            pw.write('\n');
        }
        printName(pw, name);
        pw.write(SEPARATOR);
        pw.write(hash);
        pw.write('\n');
    }
    
    protected void printName(Writer pw, String name) throws IOException {
        pw.write(name);        
    }

    protected void printPrefix(Writer pw, String prefix) throws IOException {
        pw.write(prefix);
    }

    // LOAD

    @Override
    protected RegData parseLine(State state, String line) {
        PrefixState prefixState = (PrefixState) state;
        int sepIndex = line.indexOf(SEPARATOR);
        if (sepIndex == -1) {
            // We are on prefix line.
            prefixState.lastPrefix = loadPrefix(line);
            return null;
        } else {
            String urlExternalForm = prefixState.lastPrefix + loadName(line.substring(0, sepIndex));
            String hash = line.substring(sepIndex + SEPARATOR_LEN);
            return new RegData(urlExternalForm, hash);
        }
    }
    
    protected String loadName(String name) {
        return name;
    }

    protected String loadPrefix(String prefix) {
        return prefix;
    }

    // STATE
    
    @Override
    protected PrefixState newState() {
        return new PrefixState();
    }
}
