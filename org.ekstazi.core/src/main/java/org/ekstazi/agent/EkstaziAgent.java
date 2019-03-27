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

package org.ekstazi.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Iterator;
import java.util.Set;

import org.ekstazi.Config;
import org.ekstazi.Ekstazi;
import org.ekstazi.Names;
import org.ekstazi.data.DependencyAnalyzer;
import org.ekstazi.io.FileRecorder;
import org.ekstazi.junit.JUnitCFT;
import org.ekstazi.maven.MavenCFT;
import org.ekstazi.monitor.CoverageMonitor;

public class EkstaziAgent {

    /** Name of the Agent */
    private static Instrumentation sInstrumentation;

    /**
     * Executed if agent is invoked before the application is started (usually
     * when specified as javaagent on command line). Note that this method has
     * two modes: 1) starts only class transformer (multiRun mode), and 2)
     * starts transformer and starts/ends coverage (singleRun mode). Option
     * 2) is executed if javaagent is invoked with singleRun:runName
     * argument, where runName is the name of the file that will be used to save
     * coverage. If the argument is not correct or no argument is specified,
     * option 1) is used.
     * 
     * @param options
     *            Command line options specified for javaagent.
     * @param instrumentation
     *            Instrumentation instance.
     */
    public static void premain(String options, Instrumentation instrumentation) {
        // Load options.
        Config.loadConfig(options, false);

        if (Config.X_ENABLED_V) {
            // Initialize instrumentation instance according to the
            // given mode.
            initializeMode(instrumentation);
        }
    }

    private static void initializeMode(Instrumentation instrumentation) {
        init(instrumentation);
        if (Config.MODE_V == Config.AgentMode.MULTI) {
            // NOTE: Alternative is to set the transformer in main Tool class to
            // initialize Config.
            instrumentation.addTransformer(new EkstaziCFT(), true);
            initMultiCoverageMode(instrumentation);
        } else if (Config.MODE_V == Config.AgentMode.SINGLE) {
            if (initSingleCoverageMode(Config.SINGLE_NAME_V, instrumentation)) {
                instrumentation.addTransformer(new EkstaziCFT(), true);
            }
        } else if (Config.MODE_V == Config.AgentMode.SINGLEFORK) {
            if (initSingleCoverageMode(Config.SINGLE_NAME_V, instrumentation)) {
                instrumentation.addTransformer(new CollectLoadedCFT(), false);
            }
        } else if (Config.MODE_V == Config.AgentMode.JUNIT) {
            instrumentation.addTransformer(new EkstaziCFT(), true);
            initJUnitMode(instrumentation);
        } else if (Config.MODE_V == Config.AgentMode.JUNITFORK) {
            initJUnitForkMode(instrumentation);
        } else if (Config.MODE_V == Config.AgentMode.SCALATEST) {
            initScalaTestMode(instrumentation);
        } else {
            System.err.println("ERROR: Incorrect options to agent. Mode is set to: " + Config.MODE_V);
            System.exit(1);
        }
        Ekstazi.inst();
    }

    /**
     * Invoked for/from Maven.
     * 
     * @param options
     * @param instrumentation
     */
    public static void agentmain(String options, Instrumentation instrumentation) {
        if (Config.X_ENABLED_V) {
            init(instrumentation);
            instrumentation.addTransformer(new MavenCFT(), true);
            instrumentMaven(instrumentation);
        }
    }

    public static Instrumentation getInstrumentation() {
        return sInstrumentation;
    }

    // INTERNAL

    /**
     * Instrument Surefire classes if they are loaded. This may be
     * needed if class has been loaded before a surefire is started
     * (e.g., used by another module that does not use Ekstazi).
     *
     * This code should be in another class/place.
     */
    private static void instrumentMaven(Instrumentation instrumentation) {
        try {
            for (Class<?> clz : instrumentation.getAllLoadedClasses()) {
                String name = clz.getName();
                if (name.equals(Names.ABSTRACT_SUREFIRE_MOJO_CLASS_VM)
                        || name.equals(Names.SUREFIRE_PLUGIN_VM)
                        || name.equals(Names.FAILSAFE_PLUGIN_VM)
                        || name.equals(Names.TESTMOJO_VM)) {
                    instrumentation.retransformClasses(clz);
                }
            }
        } catch (UnmodifiableClassException ex) {
            // Consider something better than doing nothing.
        }
    }

    private static void initScalaTestMode(Instrumentation instrumentation) {
        instrumentation.addTransformer(new EkstaziCFT(), true);
        try {
            Class<?> scalaTestCFT = Class.forName(Names.SCALATEST_CFT_BIN);
            instrumentation.addTransformer((ClassFileTransformer) scalaTestCFT.newInstance(), false);
        } catch (Exception e) {
            System.err.println("ERROR: ScalaTest related classes are not on the path. Check if you specified dependencies on scalatest module.");
            System.exit(1);
        }
    }
    
    private static void initJUnitForkMode(Instrumentation instrumentation) {
        Config.X_INSTRUMENT_CODE_V = false;
        instrumentation.addTransformer(new JUnitCFT(), false);
        instrumentation.addTransformer(new CollectLoadedCFT(), false);
    }

    private static void initJUnitMode(Instrumentation instrumentation) {
        instrumentation.addTransformer(new JUnitCFT(), false);
    }

    /**
     * Initialize MultiMode run. Currently there are no additional
     * steps to be performed here.
     */
    private static void initMultiCoverageMode(Instrumentation instrumentation) {
        // Nothing.
    }
    
    /**
     * Initialize SingleMode run. We first check if run is affected and
     * only in that case start coverage, otherwise we remove bodies of all main
     * methods to avoid any execution.
     */
    private static boolean initSingleCoverageMode(final String runName, Instrumentation instrumentation) {
        // Check if run is affected and if not start coverage.
        if (Ekstazi.inst().checkIfAffected(runName)) {
            Ekstazi.inst().startCollectingDependencies(runName);
            // End coverage when VM ends execution.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Ekstazi.inst().finishCollectingDependencies(runName);
                }
            });
            return true;
        } else {
            instrumentation.addTransformer(new RemoveMainCFT());
            return false;
        }
    }
    
    private static void init(Instrumentation instrumentation) {
        if (sInstrumentation == null) {
            sInstrumentation = instrumentation;
            if (Config.DEPENDENCIES_NIO_V) {
                System.setSecurityManager(new FileRecorder(Config.DEPENDENCIES_NIO_INCLUDES_V,
                        Config.DEPENDENCIES_NIO_EXCLUDES_V));
            }
        }
    }
}
