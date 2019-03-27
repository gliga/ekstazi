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

import org.ekstazi.agent.Instr;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;

/**
 * Instrument System.runFinalization invocations to ensure that references to
 * classes are removed before finalization is run. Otherwise we may prevent
 * tests from checking if WeakReference is collected.
 */
public class SystemMethodVisitor extends MethodVisitor {

    /**
     * Constructor.
     * 
     * @param mv
     *            MethodVisitor to which to delegate calls.
     */
    public SystemMethodVisitor(MethodVisitor mv) {
        super(Instr.ASM_API_VERSION, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKESTATIC && owner.equals(Instr.SYSTEM_CLASS_INTERNAL_NAME)
                && name.equals(Instr.RUN_FINALIZATION_MNAME) && desc.equals(Instr.RUN_FINALIZATION_MDESC) && !itf) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instr.SYSTEM_MONITOR_CLASS_INTERNAL_NAME, name, desc, false);
        } else if (opcode == Opcodes.INVOKESTATIC && owner.equals(Instr.SYSTEM_CLASS_INTERNAL_NAME)
                && name.equals(Instr.GC_MNAME) && desc.equals(Instr.GC_MDESC) && !itf) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instr.SYSTEM_MONITOR_CLASS_INTERNAL_NAME, name, desc, false);
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
