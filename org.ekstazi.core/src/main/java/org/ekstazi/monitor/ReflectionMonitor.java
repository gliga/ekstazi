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

package org.ekstazi.monitor;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.ProtectionDomain;

import org.ekstazi.instrument.CoverageMethodVisitor;
import org.ekstazi.util.Types;

/**
 * Dependency monitor for reflection methods. Namely, each reflection
 * method is replaced by the invocation to this class (see {@link
 * CoverageMethodVisitor}). The instrumented version of each method
 * records covered classes and invokes the original reflection method.
 * 
 * CONSIDER: Invoke this class without expecting return value. Namely only
 * record class.
 */
public final class ReflectionMonitor {

    /**
     * Records that the given class is covered.
     * 
     * @param clz
     *            Covered class.
     */
    private static void recordClass(Class<?> clz) {
        if (!Types.isIgnorable(clz)) {
            CoverageMonitor.t(clz);
        }
    }

    public static String toString(Class<?> clz) {
        recordClass(clz);
        return clz.toString();
    }

    // We do not need to instrument newInstance as constructor should be already
    // instrumented and class will be recorded from there; in addition, we avoid
    // dealing with IllegalAccessException.
    // public static Object newInstance(Class<?> clz)
    // throws InstantiationException, IllegalAccessException {
    // recordClass(clz);
    // return clz.newInstance();
    // }

    public static boolean isAnnotation(Class<?> clz) {
        recordClass(clz);
        return clz.isAnnotation();
    }

    public static boolean isSynthetic(Class<?> clz) {
        recordClass(clz);
        return clz.isSynthetic();
    }

    public static String getName(Class<?> clz) {
        recordClass(clz);
        return clz.getName();
    }

    public static ClassLoader getClassLoader(Class<?> clz) throws SecurityException {
        recordClass(clz);
        return clz.getClassLoader();
    }

    public static TypeVariable<?>[] getTypeParameters(Class<?> clz) {
        recordClass(clz);
        return clz.getTypeParameters();
    }

    public static Type getGenericSuperclass(Class<?> clz) {
        recordClass(clz);
        return clz.getGenericSuperclass();
    }

    public static Package getPackage(Class<?> clz) {
        recordClass(clz);
        return clz.getPackage();
    }

    public static Type[] getGenericInterfaces(Class<?> clz) {
        recordClass(clz);
        return clz.getGenericInterfaces();
    }

    public static Method getEnclosingMethod(Class<?> clz) {
        recordClass(clz);
        return clz.getEnclosingMethod();
    }

    public static Constructor<?> getEnclosingConstructor(Class<?> clz) {
        recordClass(clz);
        return clz.getEnclosingConstructor();
    }

    public static Class<?> getDeclaringClass(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaringClass();
    }

    public static Class<?> getEnclosingClass(Class<?> clz) {
        recordClass(clz);
        return clz.getEnclosingClass();
    }

    public static String getSimpleName(Class<?> clz) {
        recordClass(clz);
        return clz.getSimpleName();
    }

    public static String getCanonicalName(Class<?> clz) {
        recordClass(clz);
        return clz.getCanonicalName();
    }

    public static boolean isAnonymousClass(Class<?> clz) {
        recordClass(clz);
        return clz.isAnonymousClass();
    }

    public static boolean isLocalClass(Class<?> clz) {
        recordClass(clz);
        return clz.isLocalClass();
    }

    public static boolean isMemberClass(Class<?> clz) {
        recordClass(clz);
        return clz.isMemberClass();
    }

    public static Class<?>[] getClasses(Class<?> clz) {
        recordClass(clz);
        return clz.getClasses();
    }

    public static Field[] getFields(Class<?> clz) {
        recordClass(clz);
        return clz.getFields();
    }

    public static Method[] getMethods(Class<?> clz) {
        recordClass(clz);
        return clz.getMethods();
    }

    public static Constructor<?>[] getConstructors(Class<?> clz) {
        recordClass(clz);
        return clz.getConstructors();
    }

    public static Field getField(Class<?> clz, String name) throws SecurityException, NoSuchFieldException {
        recordClass(clz);
        return clz.getField(name);
    }

    public static Method getMethod(Class<?> clz, String name, Class<?>... parameterTypes) throws SecurityException,
            NoSuchMethodException {
        recordClass(clz);
        return clz.getMethod(name, parameterTypes);
    }

    public static Constructor<?> getConstructor(Class<?> clz, Class<?>... parameterTypes) throws SecurityException,
            NoSuchMethodException {
        recordClass(clz);
        return clz.getConstructor(parameterTypes);
    }

    public static Class<?>[] getDeclaredClasses(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaredClasses();
    }

    public static Field[] getDeclaredFields(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaredFields();
    }

    public static Method[] getDeclaredMethods(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaredMethods();
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaredConstructors();
    }

    public static Field getDeclaredField(Class<?> clz, String name) throws SecurityException, NoSuchFieldException {
        recordClass(clz);
        return clz.getDeclaredField(name);
    }

    public static Method getDeclaredMethod(Class<?> clz, String name, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException {
        recordClass(clz);
        return clz.getDeclaredMethod(name, parameterTypes);
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> clz, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException {
        recordClass(clz);
        return clz.getDeclaredConstructor(parameterTypes);
    }

    public static InputStream getResourceAsStream(Class<?> clz, String name) {
        recordClass(clz);
        return clz.getResourceAsStream(name);
    }

    public static URL getResource(Class<?> clz, String name) {
        recordClass(clz);
        return clz.getResource(name);
    }

    public static ProtectionDomain getProtectionDomain(Class<?> clz) {
        recordClass(clz);
        return clz.getProtectionDomain();
    }

    public static boolean desiredAssertionStatus(Class<?> clz) {
        recordClass(clz);
        return clz.desiredAssertionStatus();
    }

    public static boolean isEnum(Class<?> clz) {
        recordClass(clz);
        return clz.isEnum();
    }

    public static Object[] getEnumConstants(Class<?> clz) {
        recordClass(clz);
        return clz.getEnumConstants();
    }

    public static Object cast(Class<?> clz, Object obj) {
        recordClass(clz);
        return clz.cast(obj);
    }

    public static Class<?> asSubclass(Class<?> clz, Class<?> clazz) {
        recordClass(clz);
        return clz.asSubclass(clazz);
    }

    public static <A extends Annotation> A getAnnotation(Class<?> clz, Class<A> annotationClass) {
        recordClass(clz);
        return clz.getAnnotation(annotationClass);
    }

    public static boolean isAnnotationPresent(Class<?> clz, Class<? extends Annotation> annotationClass) {
        recordClass(clz);
        return clz.isAnnotationPresent(annotationClass);
    }

    public static Annotation[] getAnnotations(Class<?> clz) {
        recordClass(clz);
        return clz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Class<?> clz) {
        recordClass(clz);
        return clz.getDeclaredAnnotations();
    }

    public static boolean isArray(Class<?> clz) {
        recordClass(clz);
        return clz.isArray();
    }

    public static boolean isAssignableFrom(Class<?> clz, Class<?> cls) {
        recordClass(clz);
        return clz.isAssignableFrom(cls);
    }

    public static Object[] getSigners(Class<?> clz) {
        recordClass(clz);
        return clz.getSigners();
    }

    public static int getModifiers(Class<?> clz) {
        recordClass(clz);
        return clz.getModifiers();
    }

    public static boolean isInstance(Class<?> clz, Object obj) {
        recordClass(clz);
        return clz.isInstance(obj);
    }

    public static Class<?> getSuperclass(Class<?> clz) {
        recordClass(clz);
        return clz.getSuperclass();
    }

    public static boolean isInterface(Class<?> clz) {
        recordClass(clz);
        return clz.isInterface();
    }

    public static Class<?> getComponentType(Class<?> clz) {
        recordClass(clz);
        return clz.getComponentType();
    }

    public static boolean isPrimitive(Class<?> clz) {
        recordClass(clz);
        return clz.isPrimitive();
    }

    public static Class<?>[] getInterfaces(Class<?> clz) {
        recordClass(clz);
        return clz.getInterfaces();
    }

    // Ignore static methods in Class (TODO: we may have to consider
    // these methods as they may trigger static initializers)
    // public static Class<?> forName(Class<?> clz, String className)
    // public static Class<?> forName(Class<?> clz, String name, boolean
    // initialize, ClassLoader loader)
    
    // Java 8

    public static String toGenericString(Class<?> clz) {
        recordClass(clz);
        throw new UnsupportedOperationException();
        // return clz.toGenericString(clz);
    }

    public static String getTypeName(Class<?> clz) throws Exception {
        recordClass(clz);
        Method m = Class.class.getDeclaredMethod("getTypeName");
        return (String) m.invoke(clz);
        // when we move to Java 8:
        // return clz.getTypeName();
    }

    public static <A extends Annotation> A[] getAnnotationsByType(Class<?> clz, Class<A> annotationClass) throws Exception {
        recordClass(clz);
        Method m = Class.class.getDeclaredMethod("getAnnotationsByType", Class.class);
        return (A[]) m.invoke(clz, annotationClass);
        // when we move to Java 8:
        // return clz.getAnnotationsByType(annotationClass);
    }
    
    public static <A extends Annotation> A getDeclaredAnnotation(Class<?> clz, Class<A> annotationClass) throws Exception {
        recordClass(clz);
        Method m = Class.class.getDeclaredMethod("getDeclaredAnnotation", Class.class);
        return (A) m.invoke(clz, annotationClass);
        // when we move to Java 8:
        // return clz.getDeclaredAnnotation(annotationClass);
    }
    
    public static <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<?> clz, Class<A> annotationClass) throws Exception {
        recordClass(clz);
        Method m = Class.class.getDeclaredMethod("getDeclaredAnnotationsByType", Class.class);
        return (A[]) m.invoke(clz, annotationClass);
        // when we move to Java 8:
        // return clz.getDeclaredAnnotationsByType(annotationClass);
    }
    
    // public static AnnotatedType getAnnotatedSuperclass(Class<?> clz) {
    // recordClass(clz);
    // return clz.getAnnotatedSuperclass();
    // }
    
    // public static AnnotatedType[] getAnnotatedInterfaces(Class<?> clz) {
    // recordClass(clz);
    // return clz.getAnnotatedInterfaces();
    // }
}
