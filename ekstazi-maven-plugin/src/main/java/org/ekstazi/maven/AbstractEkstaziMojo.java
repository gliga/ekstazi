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

import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Plugin;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.ekstazi.util.FileUtil;

public abstract class AbstractEkstaziMojo extends AbstractMojo {

    /** Marker printed to excludesFile to set boundaries */
    protected static final String EKSTAZI_LINE_MARKER = "# Ekstazi excluded";

    /** Name of surefire plugin */
    protected static final String SUREFIRE_PLUGIN_KEY = "org.apache.maven.plugins:maven-surefire-plugin";

    /** Name of Ekstazi plugin */
    protected static final String EKSTAZI_PLUGIN_KEY = "org.ekstazi:ekstazi-maven-plugin";

    /** 'exclude' property used by Ekstazi to pass list of classes to exclude */
    protected static final String EKSTAZI_EXCLUDES_PROPERTY = "EkstaziExcludes";

    /** Name of property that is used by surefireplugin to set JVM arguments */
    protected static final String ARG_LINE_PARAM_NAME = "argLine";

    /** Name of 'excludesFile' parmeter in surefire */
    protected static final String EXCLUDES_FILE_PARAM_NAME = "excludesFile";

    /** Name of 'parallel' parameter in surefire */
    protected static final String PARALLEL_PARAM_NAME = "parallel";

    /** Name of 'reuseForks' parameter in surefire */
    protected static final String REUSE_FORKS_PARAM_NAME = "reuseForks";

    /** Name of 'forkCount' parameter in surefire */
    protected static final String FORK_COUNT_PARAM_NAME = "forkCount";

    /** Name of 'forkMode' parameter in surefire */
    protected static final String FORK_MODE_PARAM_NAME = "forkMode";

    /** Name of 'excludes' parameter in surefire */
    protected static final String EXCLUDES_PARAM_NAME = "excludes";

    /** Name of 'exclude' parameter in surefire */
    protected static final String EXCLUDE_PARAM_NAME = "exclude";

    @Parameter(property="project")
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;

    /**
     * Clone of "skipTests" in surefire.  Ekstazi is not executed if
     * this flag is true.  This property should not be set only for
     * Ekstazi configuration.
     */
    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean skipTests;

    /**
     * If set to true, skip using Ekstazi.
     *
     * @since 4.1.0
     */
    @Parameter(property = "ekstazi.skipme", defaultValue = "false")
    private boolean skipme;

    /**
     * Parent of .ekstazi directory.
     *
     * @since 4.5.0
     */
    @Parameter(property = "ekstazi.parentdir", defaultValue = "${basedir}")
    protected File parentdir;

    public boolean getSkipTests() {
        return skipTests;
    }

    public boolean getSkipme() {
        return skipme;
    }

    public File getParentdir() {
        return parentdir;
    }

    /**
     * Find plugin based on the plugin key. Returns null if plugin
     * cannot be located.
     */
    protected Plugin lookupPlugin(String key) {
        List<Plugin> plugins = project.getBuildPlugins();

        for (Iterator<Plugin> iterator = plugins.iterator(); iterator.hasNext();) {
            Plugin plugin = iterator.next();
            if(key.equalsIgnoreCase(plugin.getKey())) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * Returns true if restore goal is present, false otherwise.
     */
    protected boolean isRestoreGoalPresent() {
        Plugin ekstaziPlugin = lookupPlugin(EKSTAZI_PLUGIN_KEY);
        if (ekstaziPlugin == null) {
            return false;
        }
        for (Object execution : ekstaziPlugin.getExecutions()) {
            for (Object goal : ((PluginExecution) execution).getGoals()) {
                if (((String) goal).equals("restore")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Locate paramName in the given (surefire) plugin. Returns value
     * of the file.
     */
    protected String extractParamValue(Plugin plugin, String paramName) throws MojoExecutionException {
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if (configuration == null) {
            return null;
        }
        Xpp3Dom paramDom = configuration.getChild(paramName);
        return paramDom == null ? null : paramDom.getValue();
    }

    protected String extractParamValue(PluginExecution pluginExe, String paramName) throws MojoExecutionException {
        Xpp3Dom configuration = (Xpp3Dom) pluginExe.getConfiguration();
        if (configuration == null) {
            return null;
        }
        Xpp3Dom paramDom = configuration.getChild(paramName);
        return paramDom == null ? null : paramDom.getValue();
    }

    protected List<String> getWorkingDirs(Plugin plugin) throws MojoExecutionException {
        List<String> workingDirs = new ArrayList<String>();
        // Get all executions.
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution execution : executions) {
            // Default phase is "test".
            if (execution.getPhase() == null || execution.getPhase().equals("test")) {
                String value = extractParamValue(execution, "workingDirectory");
                value = value == null ? "." : value;
                if (!workingDirs.contains(value)) {
                    workingDirs.add(value);
                }
            }
        }
        return workingDirs;
    }

    /**
     * Removes lines from excludesFile that are added by Ekstazi.
     */
    private void restoreExcludesFile(File excludesFileFile) throws MojoExecutionException {
        if (!excludesFileFile.exists()) {
            return;
        }

        try {
            String[] lines = FileUtil.readLines(excludesFileFile);
            List<String> newLines = new ArrayList<String>();
            for (String line : lines) {
                if (line.equals(EKSTAZI_LINE_MARKER)) break;
                newLines.add(line);
            }
            FileUtil.writeLines(excludesFileFile, newLines);
        } catch (IOException ex) {
            throw new MojoExecutionException("Could not restore 'excludesFile'", ex);
        }
    }

    protected void restoreExcludesFile(Plugin plugin) throws MojoExecutionException {
        String excludesFileName = extractParamValue(plugin, EXCLUDES_FILE_PARAM_NAME);
        File excludesFileFile = new File(excludesFileName);
        restoreExcludesFile(excludesFileFile);
        removeExcludesFileIfEmpty(excludesFileFile);
    }

    private void removeExcludesFileIfEmpty(File excludesFileFile) throws MojoExecutionException {
        if (!excludesFileFile.exists()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(excludesFileFile));
            if (br.readLine() == null) {
                excludesFileFile.delete();
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Could not remove 'excludesFile'", ex);
        }
    }
}
