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

package org.ekstazi.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ekstazi.Names;
import org.ekstazi.research.Research;

/**
 * Utility class to help with type filtering and transformations.
 */
public final class Types {

    private static final String JDK_VM = "jdk/";
    private static final String JDK_BIN = internalToBinName(JDK_VM);

    private static final String JAVA_VM = "java/";
    private static final String JAVA_BIN = internalToBinName(JAVA_VM);
    
    private static final String JAVAX_VM = "javax/";
    private static final String JAVAX_BIN = internalToBinName(JAVAX_VM);

    private static final String SUN_VM = "sun/";
    private static final String SUN_BIN = internalToBinName(SUN_VM);
    
    private static final String COM_SUN_VM = "com/sun/";
    private static final String COM_SUN_BIN = internalToBinName(COM_SUN_VM);

    private static final String ORG_XML_SAX_VM = "org/xml/sax/";
    private static final String ORG_XML_SAX_BIN = internalToBinName(ORG_XML_SAX_VM);

    private static final String ORG_IETF_JGSS_VM = "org/ietf/jgss/";
    private static final String ORG_IETF_JGSS_BIN = internalToBinName(ORG_IETF_JGSS_VM);

    private static final String ORG_OMG_VM = "org/omg/";
    private static final String ORG_OMG_BIN = internalToBinName(ORG_OMG_VM);

    private static final String ORG_W3C_VM = "org/w3c/";
    private static final String ORG_W3C_BIN = internalToBinName(ORG_W3C_VM);

    private static final String ORG_MOCKITO_VM = "org/mockito/";
    private static final String ORG_MOCKITO_BIN = internalToBinName(ORG_MOCKITO_VM);

    private static final String ORG_JACOCO_AGENT_VM = "org/jacoco/agent/";
    private static final String ORG_JACOCO_AGENT_BIN = internalToBinName(ORG_JACOCO_AGENT_VM);

    private static final String XTS_VM = Names.EKSTAZI_PACKAGE_VM.concat("/");
    private static final String XTS_BIN = internalToBinName(XTS_VM);

    /**
     * Checks if class should be ignored when collecting dependencies.
     */
    public static boolean isIgnorable(Class<?> clz) {
        return clz.isArray() || clz.isPrimitive() || isIgnorableBinName(clz.getName());
    }

    /**
     * Checks if (VM) class name is in one of the packages that should not be
     * instrumented.
     */
    public static boolean isIgnorableInternalName(String className) {
        return (className.startsWith("[", 0)
                || className.startsWith(JAVA_VM, 0)
                || className.startsWith(JAVAX_VM, 0)
                || className.startsWith(JDK_VM, 0)
                || className.startsWith(SUN_VM, 0)
                || className.startsWith(COM_SUN_VM, 0)
                || className.startsWith(ORG_XML_SAX_VM, 0)
                || className.startsWith(ORG_IETF_JGSS_VM, 0)
                || className.startsWith(ORG_OMG_VM, 0)
                || className.startsWith(ORG_W3C_VM, 0)
                || className.startsWith(XTS_VM, 0)
                || className.startsWith(ORG_MOCKITO_VM, 0)
                || className.startsWith(ORG_JACOCO_AGENT_VM, 0));
    }

    /**
     * Checks if (binary) class name is in one of the packages that should not
     * be instrumented.
     */
    public static boolean isIgnorableBinName(String className) {
        return (className.startsWith("[", 0)
                || className.startsWith(JAVA_BIN, 0)
                || className.startsWith(JAVAX_BIN, 0)
                || className.startsWith(JDK_BIN, 0)
                || className.startsWith(SUN_BIN, 0)
                || className.startsWith(COM_SUN_BIN, 0)
                || className.startsWith(ORG_XML_SAX_BIN, 0)
                || className.startsWith(ORG_IETF_JGSS_BIN, 0)
                || className.startsWith(ORG_OMG_BIN, 0)
                || className.startsWith(ORG_W3C_BIN, 0)
                || className.startsWith(XTS_BIN, 0)
                || className.startsWith(ORG_MOCKITO_BIN, 0)
                || className.startsWith(ORG_JACOCO_AGENT_BIN, 0));
    }

    /**
     * Checks if the given class should be instrumented. Returns true if the
     * class should not be instrumented, false otherwise.
     */
    @Research
    public static boolean isRetransformIgnorable(Class<?> clz) {
        String className = clz.getName();
        return (isIgnorableBinName(className)
                || className.startsWith(Names.ORG_APACHE_MAVEN_BIN, 0)
                || className.startsWith(Names.ORG_JUNIT_PACKAGE_BIN, 0)
                || className.startsWith(Names.JUNIT_FRAMEWORK_PACKAGE_BIN, 0));
    }

    @Research
    public static Class<?>[] getThisAndSuperclassesFiltered(Class<?> clz) {
        Class<?>[] classes = getThisAndSuperclasses(clz);
        return filterClassesToRetransform(classes);
    }

    @Research
    public static Class<?>[] getThisAndSuperclasses(Class<?> clz) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        while (true) {
            Class<?> parent = clz.getSuperclass();
            if (parent == null) break;
            classes.add(clz);
            clz = parent;
        }
        return classes.toArray(new Class<?>[0]);
    }

    @Research
    public static Class<?>[] filterClassesToRetransform(Class<?>[] classes) {
        List<Class<?>> newClasses = new ArrayList<Class<?>>();
        for (Class<?> clz : classes) {
            if (!isRetransformIgnorable(clz)) {
                newClasses.add(clz);
            }
        }
        return newClasses.toArray(new Class<?>[0]);
    }

    public static String internalName(Class<?> clz) {
        return binToInternalName(clz.getName());
    }

    public static String binToInternalName(String className) {
        return className.replace('.', '/');
    }

    protected static String internalToBinName(String className) {
        return className.replace('/', '.');
    }

    /**
     * Transforming internal name to vm name. Note that in case of array, e.g.,
     * "[L/java/lang/Type;", the method returns element type, i.e.,
     * "java/lang/Type".
     */
    public static String descToInternalName(String name) {
        if (name.endsWith(";")) {
            int lIndex = name.indexOf("L");
            name = name.substring(lIndex + 1, name.length() - 1);
        }
        return name;
    }

    /**
     * Returns url of the resource.
     */
    public static URL getResource(Class<?> clz) {
        return clz.getResource("/" + clz.getName().replace('.', '/') + ".class");
    }

    /**
     * Returns true if the given URL is jar URL.
     */
    public static boolean isJarURL(URL url) {
        return url.getProtocol().equals("jar");
    }

    /**
     * Extract URL part that corresponds to jar portion of the given url.
     */
    public static URL extractJarURL(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    /**
     * Extract URL part that corresponds to jar portion of url for the given
     * class.
     */
    public static URL extractJarURL(Class<?> clz) throws IOException {
        return extractJarURL(getResource(clz));
    }
    
    /**
     * Returns true if descriptor describes one of the primitive types.
     */
    public static boolean isPrimitiveDesc(String desc) {
        if (desc.length() > 1) return false;
        char c = desc.charAt(0);
        return c == 'Z'
               || c == 'B'
               || c == 'C'
               || c == 'D'
               || c == 'F'
               || c == 'I'
               || c == 'J'
               || c == 'S';
    }
}
