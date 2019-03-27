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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.ekstazi.Config;
import org.ekstazi.agent.EkstaziAgent;
import org.ekstazi.util.Types;

/**
 * Parent class of all interceptors. Interceptors are inserted before Mojo
 * execute method is invoked.
 */
public abstract class AbstractMojoInterceptor {

    /** Property key used to pass argLine from plugin to interceptor */
    public static final String ARGLINE_INTERNAL_PROP = "ekstazi.argline.internal";
    /** Property key used to pass excludes list from plugin to interceptor */
    public static final String EXCLUDES_INTERNAL_PROP = "ekstazi.excludes.internal";

    /** getLog method in Mojo */
    private static final String GET_LOG_METHOD = "getLog";
    /** warn method on logger */
    private static final String WARN_METHOD = "warn";

    protected static final String SET_ARG_LINE = "setArgLine";
    protected static final String GET_ARG_LINE = "getArgLine";
    /** argLine field name in ScalaTest mojo */
    protected static final String ARGLINE_FIELD = "argLine";
    
    // UPDATES

    protected static String makeArgLine(Object mojo, Config.AgentMode junitMode, String currentArgLine)
            throws Exception {
        URL agentJarURL = Types.extractJarURL(EkstaziAgent.class);
        String agentAbsolutePath = new File(agentJarURL.toURI().getSchemeSpecificPart()).getAbsolutePath();
        String more = "-javaagent:" + agentAbsolutePath + "=mode=" + junitMode + ",";

        // Get argLine as prepared by Ekstazi plugin.
        String ekstaziArgLine = System.getProperty(ARGLINE_INTERNAL_PROP);
        String newArgLine = currentArgLine == null || currentArgLine.equals("") ? ekstaziArgLine : ekstaziArgLine + " "
                + currentArgLine;
        return more + newArgLine;
    }

    // UTIL

    /**
     * Throws MojoExecutionException.
     * 
     * @param mojo
     *            Surefire plugin
     * @param message
     *            Message for the exception
     * @param cause
     *            The actual exception
     * @throws Exception
     *             MojoExecutionException
     */
    protected static void throwMojoExecutionException(Object mojo, String message, Exception cause) throws Exception {
        Class<?> clz = mojo.getClass().getClassLoader().loadClass(MavenNames.MOJO_EXECUTION_EXCEPTION_BIN);
        Constructor<?> con = clz.getConstructor(String.class, Exception.class);
        Exception ex = (Exception) con.newInstance(message, cause);
        throw ex;
    }

    /**
     * Gets String field value from the given mojo based on the given method
     * name.
     */
    protected static String invokeAndGetString(String methodName, Object mojo) throws Exception {
        return (String) invokeGetMethod(methodName, mojo);
    }

    /**
     * Gets boolean field value from the given mojo based on the given method
     * name.
     * 
     * @param methodName
     *            Method name to be used to get the value
     * @param mojo
     *            Mojo from which to extract the value
     * @return Boolean value by invoking method on Mojo
     * @throws Exception
     *             If reflection invocation goes wrong
     */
    protected static boolean invokeAndGetBoolean(String methodName, Object mojo) throws Exception {
        return (Boolean) invokeGetMethod(methodName, mojo);
    }

    /**
     * Gets List field value from the given mojo based on the given method name.
     */
    @SuppressWarnings("unchecked")
    protected static List<String> invokeAndGetList(String methodName, Object mojo) throws Exception {
        return (List<String>) invokeGetMethod(methodName, mojo);
    }

    protected static void invokeSetMethod(String methodName, Object mojo, Object value, Class<?> type) throws Exception {
        Method method = mojo.getClass().getMethod(methodName, type);
        method.setAccessible(true);
        method.invoke(mojo, value);
    }

    private static Object invokeGetMethod(String methodName, Object mojo) throws Exception {
        Method method = mojo.getClass().getMethod(methodName);
        method.setAccessible(true);
        return method.invoke(mojo);
    }

    /**
     * Sets the given field to the given value. This is an alternative to
     * invoking a set method.
     * 
     * @param fieldName
     *            Name of the field to set
     * @param mojo
     *            Mojo
     * @param value
     *            New value for the field
     * @throws Exception
     *             If setting the field using reflection goes wrong
     */
    protected static void setField(String fieldName, Object mojo, Object value) throws Exception {
        Field field = null;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            // Ignore exception and try superclass.
            field = mojo.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        field.set(mojo, value);
    }

    /**
     * Gets the value of the field. This is an alternative to invoking a get
     * method.
     * 
     * @param fieldName
     *            Name of the field to get
     * @param mojo
     *            Mojo
     * @return Value of the field
     * @throws Exception
     *             If getting the field value using relection goes wrong
     */
    protected static Object getField(String fieldName, Object mojo) throws Exception {
        Field field = null;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            // Ignore exception and try superclass.
            field = mojo.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        return field.get(mojo);
    }

    @SuppressWarnings("unchecked")
    protected static List<String> getListField(String fieldName, Object mojo) throws Exception {
        return (List<String>) getField(fieldName, mojo);
    }

    protected static String getStringField(String fieldName, Object mojo) throws Exception {
        return (String) getField(fieldName, mojo);
    }

    /**
     * mojo.getLog().warn(msg)
     */
    protected static void warn(Object mojo, String msg) throws Exception {
        Method method = mojo.getClass().getMethod(GET_LOG_METHOD);
        method.setAccessible(true);
        Object logObject = method.invoke(mojo);
        method = logObject.getClass().getMethod(WARN_METHOD, CharSequence.class);
        method.setAccessible(true);
        method.invoke(logObject, msg);
    }
}
