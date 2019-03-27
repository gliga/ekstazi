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
import org.junit.Test;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Integration tests with Maven plugin (and ScalaTest).
 */
public class AbstractScalaTestIT extends AbstractMavenPluginIT {

    /** Pattern for the number of tests reported by Maven surefire */
    private static final Pattern TESTS_RUN_LINE = Pattern.compile("Tests: succeeded (\\d+), failed \\d+, canceled \\d+, ignored \\d+, pending \\d+");

    @Override
    protected int getNumOfTests(String[] lines) {
        int numOfTests = 0;
        for (String line : lines) {
            Matcher matcher = TESTS_RUN_LINE.matcher(line);
            if (matcher.find()) {
                numOfTests += Integer.parseInt(matcher.group(1));
            }
        }
        return numOfTests;
    }
}
