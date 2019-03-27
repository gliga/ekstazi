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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.ekstazi.agent.EkstaziAgent;
import org.ekstazi.util.FileUtil;

@Mojo(name = "restore", defaultPhase = LifecyclePhase.TEST)
public class RestoreEkstaziMojo extends AbstractEkstaziMojo {

    public void execute() throws MojoExecutionException {
        if (getSkipme()) {
            getLog().info("Ekstazi is skipped.");
            return;
        }
        if (getSkipTests()) {
            getLog().info("Tests are skipped.");
            return;
        }

        Plugin plugin = lookupPlugin(SUREFIRE_PLUGIN_KEY);
        restoreExcludesFile(plugin);
    }
}
