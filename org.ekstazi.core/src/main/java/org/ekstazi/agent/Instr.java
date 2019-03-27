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

import org.ekstazi.Names;
import org.ekstazi.asm.Opcodes;

/**
 * List of commonly used constants.
 */
public final class Instr {

    /** Used ASM API */
    public static final int ASM_API_VERSION = Opcodes.ASM5;
    
    /** Method descriptors */
    public static final String STRING_Z_CLASS_DESC = "(Ljava/lang/String;Z)Ljava/lang/Class;";
    public static final String STRING_CLASS_DESC = "(Ljava/lang/String;)Ljava/lang/Class;";
    public static final String CLASS_V_DESC = "(Ljava/lang/Class;)V";
    public static final String CLASS_I_V_DESC = "(Ljava/lang/Class;I)V";
    public static final String STRING_V_DESC = "(Ljava/lang/String;)V";
    public static final String OBJECT_V_DESC = "(Ljava/lang/Object;)V";
    public static final String OBJECT_I_V_DESC = "(Ljava/lang/Object;I)V";
    public static final String OBJECT_THREAD_V_DESC = "(Ljava/lang/Object;Ljava/lang/Thread;)V";
    public static final String AZ_I_CLASS_V_DESC = "([ZILjava/lang/Class;)V";
    public static final String AZ_I_OBJECT_V_DESC = "([ZILjava/lang/Object;)V";

    /** Internal class names for Tool package */
    public static final String LOADER_MONITOR_CLASS_INTERNAL_NAME = Names.EKSTAZI_PACKAGE_VM + "/monitor/LoaderMonitor";
    public static final String REFLECTION_MONITOR_CLASS_INTERNAL_NAME = Names.EKSTAZI_PACKAGE_VM + "/monitor/ReflectionMonitor";
    public static final String FILE_MONITOR_CLASS_INTERNAL_NAME = Names.EKSTAZI_PACKAGE_VM + "/monitor/FileMonitor";
    public static final String SYSTEM_MONITOR_CLASS_INTERNAL_NAME = Names.EKSTAZI_PACKAGE_VM + "/monitor/SystemMonitor";

    /** Internal class names for SJL */
    public static final String CLASS_CLASS_INTERNAL_NAME = "java/lang/Class";
    public static final String STRING_CLASS_INTERNAL_NAME = "java/lang/String";
    public static final String FILE_CLASS_INTERNAL_NAME = "java/io/File";
    public static final String SYSTEM_CLASS_INTERNAL_NAME = "java/lang/System";

    /** Monitor methods */
    /** Coverage monitor method name for collecting classes */
    public static final String COVERAGE_MONITOR_MNAME = "t";
    /** Coverage monitor method name for field accesses */
    public static final String COVERAGE_MONITOR_FIELD_MNAME = "f";

    /** Loader monitor method name */
    public static final String LOADER_MONITOR_MNAME = "loadClass";
    /** Loader monitor method descriptor */
    public static final String LOADER_MONITOR_MDESC = "(Ljava/lang/String;)Ljava/lang/Class;";

    /** System runFinalization method name */
    public static final String RUN_FINALIZATION_MNAME = "runFinalization";
    /** System runFinalization method descriptor */
    public static final String RUN_FINALIZATION_MDESC = "()V";
    /** System gc method name */
    public static final String GC_MNAME = "gc";
    /** System gc method descriptor */
    public static final String GC_MDESC = "()V";

    /** createTempFile method in File class */
    public static final String CREATE_TEMP_FILE_MNAME = "createTempFile";
    /** deleteOnExit method in File class */
    public static final String DELETE_ON_EXIT_MNAME = "deleteOnExit";
    /** addShutdownHook method in Runtime */
    public static final String ADD_SHUTDOWN_HOOK_MNAME = "addShutdownHook";

    /** String.startsWith method */
    public static final String STARTS_WITH_MNAME = "startsWith";
    public static final String STARTS_WITH_MDESC = "(Ljava/lang/String;)Z";
}
