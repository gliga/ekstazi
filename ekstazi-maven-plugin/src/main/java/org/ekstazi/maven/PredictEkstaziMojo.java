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

import java.util.List;

import org.ekstazi.Config;

/**
 * The ultimate goal for this Mojo is to predict execution time.  At
 * the moment this Mojo only prints classes that are not being
 * affected without any other calculation.
 */
@Mojo(name = "predict", defaultPhase = LifecyclePhase.INITIALIZE)
public class PredictEkstaziMojo extends StaticSelectEkstaziMojo {

    /** Prefix non affected classes when printed */
    private static final String NON_AFFECTED_PREFIX = "NonAffected::";

    public void execute() throws MojoExecutionException {
        // Prepare initial list of options and set property.
        System.setProperty(AbstractMojoInterceptor.ARGLINE_INTERNAL_PROP, prepareEkstaziOptions());
        // Find non affected classes and print.
        List<String> nonAffectedClasses = computeNonAffectedClasses();
        for (String name : nonAffectedClasses) {
            getLog().info(NON_AFFECTED_PREFIX + " " + name);
        }
    }

    /**
     * Prepares option for Ekstazi.
     */
    private String prepareEkstaziOptions() {
        return getRootDirOption() +
            (getXargs() == null || getXargs().equals("") ? "" : "," + getXargs());
    }
}
