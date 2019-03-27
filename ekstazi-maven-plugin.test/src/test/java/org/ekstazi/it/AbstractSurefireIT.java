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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public abstract class AbstractSurefireIT extends AbstractMavenPluginIT {

    /** Pattern for the number of tests */
    protected static final Pattern TESTS_RUN_LINE = Pattern.compile(".*Tests run: (\\d+), Failures: \\d+, Errors: \\d+, Skipped: \\d+");

    /**
     * Returns the number of tests based on the maven output.
     */
    @Override
    protected int getNumOfTests(String[] lines) {
        int numOfTests = 0;
        for (String line : lines) {
            Matcher matcher = TESTS_RUN_LINE.matcher(line);
            if (matcher.matches()) {
                numOfTests += Integer.parseInt(matcher.group(1));
            }
        }
        return numOfTests;
    }
}
