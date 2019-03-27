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

package org.ekstazi.maven;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import org.ekstazi.agent.EkstaziAgent;

/**
 * This Mojo is used only to define Ekstazi lifecycle.  As Ekstazi has
 * to be run before test phase, there is a need for a Mojo that is
 * executed in test phase to active execution of tests.
 */
@Mojo(name = "ekstazi", defaultPhase = LifecyclePhase.TEST)
@Execute(goal = "ekstazi", phase = LifecyclePhase.TEST, lifecycle = "ekstazi")
public class EkstaziCycleMojo extends StaticSelectEkstaziMojo {

    public void execute() throws MojoExecutionException {
        // Nothing.
    }
}
