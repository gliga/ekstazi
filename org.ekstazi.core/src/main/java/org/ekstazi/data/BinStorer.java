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
import java.math.BigInteger;

import org.ekstazi.research.Research;

/**
 * This is a fake binary storer. It only hides text, but does not improve
 * performance. What is more, this storer likely increases overhead.
 */
@Research
public final class BinStorer extends PrefixTxtStorer {

    /**
     * Constructor.
     */
    public BinStorer() {
        super(Mode.BIN);
    }

    @Override
    protected void printName(Writer pw, String name) throws IOException {
        pw.write(new BigInteger(name.getBytes()).toString());
    }

    @Override
    protected void printPrefix(Writer pw, String prefix) throws IOException {
        pw.write(new BigInteger(prefix.getBytes()).toString());
    }

    @Override
    protected String loadName(String name) {
        return new String(new BigInteger(name).toByteArray());
    }

    @Override
    protected String loadPrefix(String prefix) {
        return new String(new BigInteger(prefix).toByteArray());
    }
}
