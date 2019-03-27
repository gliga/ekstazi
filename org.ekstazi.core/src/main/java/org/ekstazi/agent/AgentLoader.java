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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.ekstazi.research.Research;
import org.ekstazi.util.Types;

/**
 * Support for dynamic agent loading.
 */
@Research
public final class AgentLoader {

    /** Name of tools.jar in JDK */
    private static final String TOOLS_JAR_NAME = "tools.jar";

    /** Name of tools.jar on Mac in JDK */
    private static final String CLASSES_JAR_NAME = "classes.jar";

    /** String used as property to indicate that agent is loaded */
    private static final String AGENT_LOADER_INIT = AgentLoader.class.getName() + "Initialize";

    /**
     * Attaches an agent (where this class belongs) to the current
     * VirtualMachine.
     * 
     * TODO: Can we detect if VirtualMachine is loaded by trying to load it
     * again.
     * 
     * @return True if the agent is successfully attached, false otherwise.
     */
    public static boolean loadEkstaziAgent() {
        // TODO: need to synchronize globally, as we may end up running
        // initialization multiple times, which would lead to:
        // "libattach.so already loaded in another classloader".
        try {
            // Note that this class can be loaded by different classloaders, so
            // we cannot simply use static field to check if the class has been
            // initialized.
            if (System.getProperty(AGENT_LOADER_INIT) != null) {
                return true;
            }
            System.setProperty(AGENT_LOADER_INIT, "");

            URL agentJarURL = Types.extractJarURL(EkstaziAgent.class);
            return loadAgent(agentJarURL);
        } catch (Exception ex) {
            if (System.getProperty("java.version").startsWith("9")) {
                throw new RuntimeException("Running with Java 9 requires -Djdk.attach.allowAttachSelf=true");
            }
            return false;
        }
    }

    /**
     * Loads agent from the given URL.
     * 
     * @param agentJarURL
     *            Agent location
     * @return True if loading was successful, false otherwise
     * @throws Exception
     *             If unexpected behavior
     */
    public static boolean loadAgent(URL agentJarURL) throws Exception {
        File toolsJarFile = findToolsJar();
        Class<?> vmClass = null;

        if (toolsJarFile == null || !toolsJarFile.exists()) {
            // likely Java 9+ (when tools.jar is not available).  Java
            // 9 also requires that
            // -Djvm.options=jdk.attach.allowAttachSelf=true is set.
            vmClass = ClassLoader.getSystemClassLoader().loadClass("com.sun.tools.attach.VirtualMachine");
        } else {
            vmClass = loadVirtualMachine(toolsJarFile);
        }

        if (vmClass == null) {
            return false;
        }

        attachAgent(vmClass, agentJarURL);
        return true;
    }

    // INTERNAL

    /**
     * Attaches jar where this class belongs to the current VirtualMachine as an
     * agent.
     * 
     * @param vmClass
     *            VirtualMachine
     * @throws Exception
     *             If unexpected behavior
     */
    private static void attachAgent(Class<?> vmClass, URL agentJarURL) throws Exception {
        String pid = getPID();
        String agentAbsolutePath = new File(agentJarURL.toURI().getSchemeSpecificPart()).getAbsolutePath();

        Object vm = getAttachMethod(vmClass).invoke(null, new Object[] { pid });
        getLoadAgentMethod(vmClass).invoke(vm, new Object[] { agentAbsolutePath });
        getDetachMethod(vmClass).invoke(vm);
    }

    /**
     * Finds attach method in VirtualMachine.
     * 
     * @param vmClass
     *            VirtualMachine class
     * @return 'attach' Method
     * @throws SecurityException
     *             If access is not legal
     * @throws NoSuchMethodException
     *             If no such method is found
     */
    private static Method getAttachMethod(Class<?> vmClass) throws SecurityException, NoSuchMethodException {
        return vmClass.getMethod("attach", new Class<?>[] { String.class });
    }

    /**
     * Finds loadAgent method in VirtualMachine.
     * 
     * @param vmClass
     *            VirtualMachine class
     * @return 'loadAgent' Method
     * @throws SecurityException
     *             If access is not legal
     * @throws NoSuchMethodException
     *             If no such method is found
     */
    private static Method getLoadAgentMethod(Class<?> vmClass) throws SecurityException, NoSuchMethodException {
        return vmClass.getMethod("loadAgent", new Class[] { String.class });
    }

    /**
     * Finds detach method for VirtualMachine.
     * 
     * @param vmClass
     *            VirtualMachine class
     * @return 'detach' Method
     * @throws SecurityException
     *             If access is not legal
     * @throws NoSuchMethodException
     *             If no such method is found
     */
    private static Method getDetachMethod(Class<?> vmClass) throws SecurityException, NoSuchMethodException {
        return vmClass.getMethod("detach");
    }

    /**
     * Loads com.sun.tools.attach.VirtualMachine. (This method was implemented
     * in Hong Kong'14.)
     * 
     * @param toolsJarFile
     *            Location of tools.jar
     * @return com.sun.tools.attach.VirtualMachine class
     * @throws Exception
     *             If execution was not successful
     */
    private static Class<?> loadVirtualMachine(File toolsJarFile) throws Exception {
        // We used to add tools.jar to SystemClassLoader but that was triggering
        // a bug in Java compiler when running some experiments (e.g.,
        // CommonsMath).
        if (toolsJarFile == null) {
            return null;
        }
        URLClassLoader loader = new URLClassLoader(new URL[] { toolsJarFile.toURI().toURL() },
                ClassLoader.getSystemClassLoader());
        return loader.loadClass("com.sun.tools.attach.VirtualMachine");
    }

    /**
     * Returns process id. Note that Java does not guarantee any format for id,
     * so this is just a common heuristic.
     * 
     * @return Current process id
     */
    private static String getPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        return vmName.substring(0, vmName.indexOf("@"));
    }

    /**
     * Finds tools.jar in JDK.
     * 
     * @return File for tools.jar, which may not be valid if tools.jar could
     *         have not been located
     */
    private static File findToolsJar() {
        String javaHome = System.getProperty("java.home");
        File javaHomeFile = new File(javaHome);
        File toolsJarFile = new File(javaHomeFile, "lib" + File.separator + TOOLS_JAR_NAME);

        if (!toolsJarFile.exists()) {
            toolsJarFile = new File(System.getenv("java_home"), "lib" + File.separator + TOOLS_JAR_NAME);
        }

        if (!toolsJarFile.exists() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre")) {
            javaHomeFile = javaHomeFile.getParentFile();
            toolsJarFile = new File(javaHomeFile, "lib" + File.separator + TOOLS_JAR_NAME);
        }

        if (!toolsJarFile.exists() && isMac() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "Home")) {
            javaHomeFile = javaHomeFile.getParentFile();
            toolsJarFile = new File(javaHomeFile, "Classes" + File.separator + CLASSES_JAR_NAME);
        }

        return toolsJarFile;
    }

    /**
     * Checks if process is running on Mac.
     * 
     * @return True if OS is on Mac, false otherwise
     */
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    /**
     * Simple main method used for initial testing.
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        System.out.println(loadEkstaziAgent());
    }
}
