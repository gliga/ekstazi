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

package org.ekstazi.dynamic;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.ekstazi.Names;
import org.ekstazi.agent.EkstaziAgent;
import org.ekstazi.log.Log;
import org.ekstazi.monitor.CoverageMonitor;
import org.ekstazi.research.Research;
import org.ekstazi.util.FileUtil;
import org.ekstazi.util.Types;

@Research
public class DynamicEkstazi {

    /**
     * Initializes an agent at runtime and adds it to VM. Returns true if the
     * initialization was successful, false otherwise.
     */
    public static Instrumentation initAgentAtRuntimeAndReportSuccess() {
        try {
            setSystemClassLoaderClassPath();
        } catch (Exception e) {
            Log.e("Could not set classpath. Tool will be off.", e);
            return null;
        }
        try {
            return addClassfileTransformer();
        } catch (Exception e) {
            Log.e("Could not add transformer. Tool will be off.", e);
            return null;
        }
    }

    private static Instrumentation addClassfileTransformer() throws Exception {
        Log.d("Setting class transformer");
        // Add classfile transformer.
        Class<?> instrumentationFactoryClass = ClassLoader.getSystemClassLoader().loadClass(
                Names.DYNAMIC_AGENT_BIN);
        Method getInstrumentation = instrumentationFactoryClass.getDeclaredMethod("getInstrumentation");
        getInstrumentation.setAccessible(true);
        Instrumentation instrumentation = (Instrumentation) getInstrumentation.invoke(null);

        // Use our instrumentation object for OpenJpa instrumentation too.
        setOpenJpaInstrumentation(instrumentation);

        // Note that we first have to load monitor class.
        ClassLoader.getSystemClassLoader().loadClass(CoverageMonitor.class.getName());
        // We have to instrument all classes that have been
        // already loaded. Alternative would be to instrument in
        // TestSuite and ParentRunner.
        instrumentAllLoadedClasses(instrumentation);

        return instrumentation;
    }

    /**
     * This hack is needed probably in 0.00001% of cases when tests are run with
     * OpenJpa javaagent. If we do not set our instrumentation object for
     * OpenJpa InstrumentationFactory we would get: libattach.so already loaded
     * in another classloader.
     * 
     * This was needed (so far) only for Apache Camel project. I was running (in
     * camel-jpa module) mvn clean install -fn.
     */
    private static void setOpenJpaInstrumentation(Instrumentation instrumentation) {
        try {
            Class<?> instrumentationFactoryClass = Class.forName("org.apache.openjpa.enhance.InstrumentationFactory");
            Method getInstrumentation = instrumentationFactoryClass.getMethod("setInstrumentation",
                    Class.forName("java.lang.instrument.Instrumentation"));
            getInstrumentation.setAccessible(true);
            getInstrumentation.invoke(null, new Object[] { instrumentation });
        } catch (Exception ex) {
            // Nothing.
        }
    }

    private static void instrumentAllLoadedClasses(Instrumentation instrumentation) {
        Class<?>[] classes = instrumentation.getAllLoadedClasses();
        Log.d("Number of classes to instrument: ", classes.length);
        for (Class<?> clz : classes) {
            if (instrumentation.isModifiableClass(clz) && !Types.isRetransformIgnorable(clz)) {
                try {
                    Log.d("Retransforming", clz);
                    instrumentation.retransformClasses(clz);
                } catch (UnmodifiableClassException ex) {
                    ex.printStackTrace();
                } catch (Throwable t) {
                    // We encountered "java.lang.InternalError: null"
                    // when retransforming
                    // "org.kohsuke.stapler.AptCompiler". Namely, we
                    // were running Jenkins light build, i.e., "mvn
                    // -Plight-test install -DfailIfNoTests=false
                    // -Dtest=LogRecorderTest". The reason for the
                    // failure seems to be a class
                    // (com/sun/mirror/apt/AnnotationProcessorFactory)
                    // that was removed after Java 6 (there is no this
                    // problem if we run with Java 6); therefore the
                    // class, clz, could have not been properly
                    // retransformed. (Note that an exception,
                    // although different, would happen if we tried to
                    // instantiate an object from clz.) See
                    // http://jenkins-ci.361315.n4.nabble.com/Hudson-Issues-JIRA-Created-HUDSON-8942-Build-failure-with-Java-7-td3687415.html
                    // for more details.
                    Log.e("Could not retransform class: " + clz);
                }
            }
        }
    }

    /**
     * Set paths from the current class loader to the path of system class
     * loader. This method should be invoked only once.
     */
    private static void setSystemClassLoaderClassPath() throws Exception {
        Log.d("Setting classpath");
        // We use agent class as we do not extract this class in newly created
        // jar (otherwise we may end up creating one -magic jar from another);
        // also it will exist even without JUnit classes).
        URL url = EkstaziAgent.class.getResource(EkstaziAgent.class.getSimpleName() + ".class");
        String origPath = url.getFile().replace("file:", "").replaceAll(".jar!.*", ".jar");
        File junitJar = new File(origPath);
        File xtsJar = new File(origPath.replaceAll(".jar", "-magic.jar"));

        boolean isCreated = false;
        // If extracted (Tool) jar is newer than junit*.jar, there is no reason
        // to extract files again, so just return in that case.
        if (FileUtil.isSecondNewerThanFirst(junitJar, xtsJar)) {
            // We cannot return here, as we have to include jar on the path.
            isCreated = true;
        } else {
            // Extract new jar as junit.jar is newer.
            String[] includePrefixes = { Names.EKSTAZI_PACKAGE_BIN };
            String[] excludePrefixes = { EkstaziAgent.class.getName() };
            isCreated = JarXtractor.extract(junitJar, xtsJar, includePrefixes, excludePrefixes);
        }

        // Add jar to classpath if it was successfully created, otherwise throw
        // an exception.
        if (isCreated) {
            addURL(xtsJar.toURI().toURL());
        } else {
            throw new RuntimeException("Could not extract Tool classes in separate jar.");
        }
    }

    /**
     * Adds URL to system ClassLoader. This is not a nice approach as we use
     * reflection, but it seems the only available in Java. This will be gone if
     * we decided to go with javaagent and boot classloader.
     * 
     * @param url
     *            URL to include on classpath.
     * @throws IOException
     *             If URL could not be added.
     */
    private static void addURL(URL url) throws Exception {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(sysloader, new Object[] { url });
    }
}
