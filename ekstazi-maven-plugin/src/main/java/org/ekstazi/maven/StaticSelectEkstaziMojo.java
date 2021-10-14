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

import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.net.URL;
import java.net.URISyntaxException;

import org.ekstazi.Config;
import org.ekstazi.agent.EkstaziAgent;
import org.ekstazi.check.AffectedChecker;
import org.ekstazi.log.Log;
import org.ekstazi.util.Types;

import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Creates exclude list and appends it to the exclude file (known to
 * surefire).  We can have cleaner approach when Maven surefire
 * introduced properties for excludes.
 */
public class StaticSelectEkstaziMojo extends AbstractEkstaziMojo {

    /** List of surefire versions that are not supported */
    private static final String[] sNonSupportedVersions = {"2.0-beta-1", "2.0", "2.1", "2.1.1", "2.1.2", "2.1.3", "2.2", "2.3", "2.3.1", "2.4", "2.4.1", "2.4.2", "2.4.3", "2.5", "2.6", "2.7", "2.7.1", "2.7.2", "2.8", "2.8.1", "2.9", "2.10", "2.11", "2.12", "2.12.1", "2.12.2", "2.12.3", "2.12.4"};

    /**
     * Enable/disable forcing run of tests that failed last time they
     * were run.
     *
     * @since 4.1.0
     */
    @Parameter(property = "ekstazi.forcefailing", defaultValue = "false")
    private boolean forcefailing;

    /**
     * Enable/disable forcing run of all tests.
     *
     * @since 4.3.0
     */
    @Parameter(property = "ekstazi.forceall", defaultValue = "false")
    private boolean forceall;

    /**
     * Additional arguments passed to Ekstazi.
     *
     * @since 4.5.1
     */
    @Parameter(property = "ekstazi.xargs", defaultValue = "")
    protected String xargs;

    public boolean getForcefailing() {
        return forcefailing;
    }

    public boolean getForceall() {
        return forceall;
    }

    public String getXargs() {
        return xargs;
    }

    public void execute() throws MojoExecutionException {
        Log.d2f("StaticSelectEkstaziMojo.java line 103");
        // Check if user explicitly requested to not use Ekstazi in
        // this run.
        if (getSkipme()) {
            getLog().info("Ekstazi is skipped.");
            return;
        }

        // Check if tests are skipped, and skip Ekstazi if true.
        if (getSkipTests()) {
            getLog().info("Tests are skipped.");
            return;
        }

        Plugin surefirePlugin = lookupPlugin(SUREFIRE_PLUGIN_KEY);
        // Get all plugin executions.
        Map<String, PluginExecution> id2Execution = surefirePlugin.getExecutionsAsMap();
        // Check plugin and its parameters.
        checkSurefirePlugin(surefirePlugin);
        checkParameters(surefirePlugin);
        // TODO: check executions in case if we cannot dynamically
        // instrument Surefire.

        boolean isForkMode = isForkMode(surefirePlugin);
        // Include agent to be used during test run.
        Log.d2f("Add java agent StaticSelectEkstaziMojo.java line 128");
        if (Config.JUNIT5_INSERTION_ENABLED_V) {
            addJavaAgent(Config.AgentMode.JUNIT5INSERTION);
        } else if (Config.JUNIT5_EXTENSION_ENABLED_V) {
            addJavaAgent(Config.AgentMode.JUNIT5EXTENSION);
        } else {
            addJavaAgent(isForkMode ? Config.AgentMode.JUNITFORK : Config.AgentMode.JUNIT);
        }

        List<String> nonAffectedClasses = computeNonAffectedClasses();
        // Append excludes list to "excludesFile".
        checkParametersInFileMode(surefirePlugin);
        appendExcludesListToExcludesFile(surefirePlugin, nonAffectedClasses);
    }

    // INTERNAL

    protected List<String> computeNonAffectedClasses() {
        List<String> nonAffectedClasses = new ArrayList();
        if (!getForceall()) {
            // Create excludes list; we assume that all files are in
            // the parentdir.
            nonAffectedClasses = AffectedChecker.findNonAffectedClasses(parentdir, getRootDirOption());

            // Do not exclude recently failing tests if appropriate
            // argument is provided.
            if (getForcefailing()) {
                List<String> recentlyFailingClasses = AffectedChecker.findRecentlyFailingClasses(parentdir, getRootDirOption());
                nonAffectedClasses.removeAll(recentlyFailingClasses);
            }
        }
        return nonAffectedClasses;
    }


    /**
     * Sets property to pass Ekstazi agent to surefire plugin.
     */
    private void addJavaAgent(Config.AgentMode junitMode) throws MojoExecutionException {
        try {
            URL agentJarURL = Types.extractJarURL(EkstaziAgent.class);
            if (agentJarURL == null) {
                throw new MojoExecutionException("Unable to locate Ekstazi agent");
            }
            Properties properties = project.getProperties();
            String oldValue = properties.getProperty(ARG_LINE_PARAM_NAME);
            properties.setProperty(ARG_LINE_PARAM_NAME, prepareEkstaziOptions(agentJarURL, junitMode) + " " + (oldValue == null ? "" : oldValue));
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to set path to agent", ex);
        } catch (URISyntaxException ex) {
            throw new MojoExecutionException("Unable to set path to agent", ex);
        }
    }

    private String prepareEkstaziOptions(URL agentJarURL, Config.AgentMode junitMode) throws URISyntaxException {
        String agentAbsolutePath = new File(agentJarURL.toURI().getSchemeSpecificPart()).getAbsolutePath();
        Log.d2f("junitMode = " + junitMode);
        return "-javaagent:" + agentAbsolutePath + "=mode=" + junitMode +
            ",force.all=" + getForceall() +
            ",force.failing=" + getForcefailing() +
            "," + getRootDirOption() +
            (getXargs() == null || getXargs().equals("") ? "" : "," + getXargs());
    }

    protected String getRootDirOption() {
        return "root.dir=" + Config.getRootDirURI(parentdir);
    }

    /**
     * Appends list of classes that should be excluded to the given
     * file.
     */
    private void appendExcludesListToExcludesFile(Plugin plugin, List<String> nonAffectedClasses) throws MojoExecutionException {
        String excludesFileName = extractParamValue(plugin, EXCLUDES_FILE_PARAM_NAME);
        File excludesFileFile = new File(excludesFileName);

        // First restore file in case it has been modified by this
        // plugin before (if 'restore' was not run, or VM crashed).
        restoreExcludesFile(plugin);

        PrintWriter pw = null;
        try {
            // Have to restore original file (on shutdown); see
            // RestoreMojo.
            pw = new PrintWriter(new FileOutputStream(excludesFileFile, true), true);
            pw.println(EKSTAZI_LINE_MARKER);
            for (String exclude : nonAffectedClasses) {
                pw.println(exclude);
            }
            // If "exclude(s)" is not present, we also have to add default value to exclude inner classes.
            if (!isAtLeastOneExcludePresent(plugin)) {
                pw.println("**/*$*");
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Could not access excludesFile", ex);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    @Deprecated
    private boolean hasEkstaziExcludesProperty(Plugin plugin) {
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if (configuration == null) return false;
        Xpp3Dom excludesDom = configuration.getChild(EXCLUDES_PARAM_NAME);
        if (excludesDom == null) return false;
        for (Xpp3Dom excludeDom : excludesDom.getChildren()) {
            if (excludeDom.getName().equals(EXCLUDE_PARAM_NAME) && excludeDom.getValue().equals(EKSTAZI_EXCLUDES_PROPERTY)) {
                return true;
            }
        }
        return true;
    }

    /**
     * Returns true if "parallel" parameter is ON, false
     * otherwise. This parameter is ON if the value is different from
     * NULL.
     *
     * https://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html
     */
    private boolean isParallelOn(Plugin plugin) throws MojoExecutionException {
        String value = extractParamValue(plugin, PARALLEL_PARAM_NAME);
        // If value is set and it is not an empty string.
        return value != null && !value.equals("");
    }

    /**
     * Returns true if there each test class is executed in its own process.
     */
    private boolean isForkMode(Plugin plugin) throws MojoExecutionException {
        String reuseForksValue = extractParamValue(plugin, REUSE_FORKS_PARAM_NAME);
        return reuseForksValue != null && reuseForksValue.equals("false");
    }

    /**
     * Returns true if fork is disabled, i.e., if we cannot set the
     * agent.
     */
    private boolean isForkDisabled(Plugin plugin) throws MojoExecutionException {
        String forkCountValue = extractParamValue(plugin, FORK_COUNT_PARAM_NAME);
        String forkModeValue = extractParamValue(plugin, FORK_MODE_PARAM_NAME);
        return (forkCountValue != null && forkCountValue.equals("0")) ||
            (forkModeValue != null && forkModeValue.equals("never"));
    }

    private boolean isExcludesFilePresent(Plugin plugin) throws MojoExecutionException {
        String excludesFileValue = extractParamValue(plugin, EXCLUDES_FILE_PARAM_NAME);
        return excludesFileValue != null;
    }

    /**
     * Returns true if at least one "exclude" is present (in "excludes").
     */
    private boolean isAtLeastOneExcludePresent(Plugin plugin) throws MojoExecutionException {
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if (configuration == null) return false;
        Xpp3Dom excludesDom = configuration.getChild(EXCLUDES_PARAM_NAME);
        if (excludesDom == null) return false;
        for (Xpp3Dom excludeDom : excludesDom.getChildren()) {
            if (excludeDom.getName().equals(EXCLUDE_PARAM_NAME)) return true;
        }
        return false;
    }

    // CHECKS

    /**
     * Checks that all parameters are set as expected.
     */
    private void checkParameters(Plugin plugin) throws MojoExecutionException {
        // Fail if 'parallel' parameter is used.
        if (isParallelOn(plugin)) {
            throw new MojoExecutionException("Ekstazi currently does not support parallel parameter");
        }
        // Fail if fork is disabled.
        if (isForkDisabled(plugin)) {
            throw new MojoExecutionException("forkCount has to be at least 1");
        }
    }

    /**
     * Checks that proper parameters are set in pom.xml that are
     * needed to pass list of classes to exclude.  The execution is in
     * "File" mode if 'excludesFile' is set in surefire configuration.
     * The problem with this mode is that we may override an already
     * existing file (and only one 'excludeFile' can be specified.
     * Although reverting the file may be an option, we may go to
     * inconsistent state if JVM crashes.
     */
    private void checkParametersInFileMode(Plugin plugin) throws MojoExecutionException {
        if (!isExcludesFilePresent(plugin)) {
            throw new MojoExecutionException("excludesFile must be set when forking JVM for each test class");
        }
    }

    /**
     * Checks that proper parameters are set in pom.xml that are
     * needed to pass list of classes to exclude.  The execution is in
     * "Property" mode if property "ekstazi.surefire.excludes" is
     * available and if that parameter is used in <excludes> of
     * surefire plugin.
     *
     * This method is deprecated as properties are expanded when Maven
     * starts, so we cannot modify.
     */
    @Deprecated
    private void checkParametersInPropertyMode(Plugin plugin) throws MojoExecutionException {
        if (!hasEkstaziExcludesProperty(plugin)) {
            throw new MojoExecutionException("Property " + EKSTAZI_EXCLUDES_PROPERTY + " has to be used in exclude");
        }
    }

    private void checkSurefirePlugin(Plugin plugin) throws MojoExecutionException {
        checkSurefirePlugin(plugin, sNonSupportedVersions, "2.13");
    }

    /**
     * Check if surefire plugin is available and if correct version is
     * used (we require 2.13 or higher, as previous versions do not
     * support excludesFile).
     */
    protected void checkSurefirePlugin(Plugin plugin, String[] nonSupportedVersions, String minSupported) throws MojoExecutionException {
        // Check if plugin is available.
        if (plugin == null) {
            throw new MojoExecutionException("Surefire plugin not avaialble");
        }
        String version = plugin.getVersion();
        for (String nonSupportedVersion : nonSupportedVersions) {
            if (version.equals(nonSupportedVersion)) {
                throw new MojoExecutionException("Not supported surefire version; version has to be " + minSupported + " or higher");
            }
        }
    }
}
