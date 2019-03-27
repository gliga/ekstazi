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

package org.ekstazi.instrument;

import org.ekstazi.Names;
import org.ekstazi.agent.Instr;
import org.ekstazi.asm.Label;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;
import org.ekstazi.monitor.LoaderMonitor;

/**
 * Instrument loadClass methods from custom {@link ClassLoader}s. The
 * instrumented code invokes appropriate monitor to return classes that
 * are known to SystemClassLoader. This code is needed as we want to avoid
 * loading classes with multiple class loaders in the same VM. The need for
 * this class was shown during runs of tests in Apache ant, e.g.,
 * org.apache.tools.ant.taskdefs.JavaTest.testResultPropertyZeroNoFork.
 * 
 * The approach that we follow is simple. We insert code that check if a class
 * being loaded is from Ekstazi package. If that is the case, we forward method
 * invocation to our monitor that invokes system class loader. Also see
 * {@link LoaderMonitor}.
 */
public class LoaderMethodVisitor extends MethodVisitor {

    /**
     * Constructor.
     */
    public LoaderMethodVisitor(MethodVisitor mv) {
        super(Instr.ASM_API_VERSION, mv);
    }

    @Override
    public void visitCode() {
        // Check if string/argument starts has our package as prefix.
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitLdcInsn(Names.EKSTAZI_PACKAGE_BIN);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Instr.STRING_CLASS_INTERNAL_NAME, Instr.STARTS_WITH_MNAME,
                Instr.STARTS_WITH_MDESC, false);
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l0);
        // Invoke monitor loader method.
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instr.LOADER_MONITOR_CLASS_INTERNAL_NAME,
                Instr.LOADER_MONITOR_MNAME, Instr.LOADER_MONITOR_MDESC, false);
        // If our loader was used then return from the method (and ignore the
        // original code).
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(l0);
        // Java 7/8 have strict policy about frames. Without the following Java
        // would report: "java.lang.VerifyError: Expecting a stackmap frame at
        // branch target".
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
}
