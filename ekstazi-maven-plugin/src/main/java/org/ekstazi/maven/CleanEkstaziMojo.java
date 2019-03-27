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

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

import org.ekstazi.util.FileUtil;
import org.ekstazi.Names;

/**
 * Removes Ekstazi directories.
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class CleanEkstaziMojo extends AbstractEkstaziMojo {

    public void execute() throws MojoExecutionException {
        File dotEkstazi = new File(parentdir, Names.EKSTAZI_ROOT_DIR_NAME);
        if (dotEkstazi.exists()) {
            FileUtil.deleteDirectory(dotEkstazi);
        }
    }
}
