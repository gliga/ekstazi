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
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.ekstazi.asm.ClassReader;
import org.ekstazi.asm.ClassVisitor;
import org.ekstazi.asm.ClassWriter;
import org.ekstazi.asm.Label;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;

/**
 * Classfile transformer that removes bodies of main methods. This class should
 * be used when agent is started before the application (and agent discovers
 * that run is not affected).
 */
class RemoveMainCFT implements ClassFileTransformer {

    static class RemoveMainClassVisitor extends ClassVisitor {
        /**
         * Constructor.
         */
        public RemoveMainClassVisitor(ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals("main") && desc.equals("([Ljava/lang/String;)V") && (access & Opcodes.ACC_PUBLIC) != 0
                    && (access & Opcodes.ACC_STATIC) != 0) {
                mv = new RemoveMainMethodVisitor(mv);
            }
            return mv;
        }
    }

    static class RemoveMainMethodVisitor extends MethodVisitor {
        /**
         * Constructor.
         */
        public RemoveMainMethodVisitor(MethodVisitor mv) {
            super(Instr.ASM_API_VERSION, mv);
        }

        @Override
        public void visitCode() {
            // Add the following statement at the beginning: if (true) return;
            mv.visitInsn(Opcodes.ICONST_1);
            Label l0 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 0, null, 0, null);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new RemoveMainClassVisitor(classWriter);
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}
