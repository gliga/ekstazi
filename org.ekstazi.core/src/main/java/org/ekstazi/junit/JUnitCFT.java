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

package org.ekstazi.junit;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.ekstazi.agent.Instr;
import org.ekstazi.asm.ClassReader;
import org.ekstazi.asm.ClassVisitor;
import org.ekstazi.asm.ClassWriter;
import org.ekstazi.asm.Label;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;

/**
 * {@link ClassFileTransformer} to instrument JUnit to support collecting
 * coverage and finding (non) affected classes.
 */
public class JUnitCFT implements ClassFileTransformer {

    private static class JUnitClassVisitor extends ClassVisitor {
        private final String mClassName;
        
        public JUnitClassVisitor(String className, ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
            this.mClassName = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            mv = new JUnitMethodVisitor(mClassName, mv);
            return mv;
        }
    }

    private static class JUnitMethodVisitor extends MethodVisitor {
        @SuppressWarnings("unused")
        private final String mClassName;
        
        public JUnitMethodVisitor(String className, MethodVisitor mv) {
            super(Instr.ASM_API_VERSION, mv);
            this.mClassName = className;
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            // TODO: Check the owner.
            if (opcode == Opcodes.INVOKEVIRTUAL && name.equals(JUnitNames.RUNNER_FOR_CLASS_METHOD)
                    && desc.equals("(Ljava/lang/Class;)Lorg/junit/runner/Runner;")) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    JUnitNames.JUNIT4_MONITOR_VM,
                    JUnitNames.RUNNER_FOR_CLASS_METHOD,
                    "(L" + JUnitNames.RUNNER_BUILDER_VM + ";Ljava/lang/Class;)Lorg/junit/runner/Runner;",
                    false);
            } else {
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }
    
    private static class TestSuiteClassVisitor extends ClassVisitor {

        public TestSuiteClassVisitor(ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals("<init>") && desc.equals("(Ljava/lang/Class;)V")) {
                mv = new TestSuiteMethodVisitor(mv);
            }
            return mv;
        }
    }
    
    public static class TestSuiteMethodVisitor extends MethodVisitor {
        public TestSuiteMethodVisitor(MethodVisitor mv) {
            super(Instr.ASM_API_VERSION, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            mv.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == Opcodes.PUTFIELD && owner.equals("junit/framework/TestSuite") && name.equals("fTests")
                    && desc.equals("Ljava/util/Vector;")) {
                Label l0 = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, JUnitNames.JUNIT3_MONITOR_VM, "addTestsFromTestCase",
                        "(Ljunit/framework/TestSuite;Ljava/lang/Class;)Z", false);
                mv.visitJumpInsn(Opcodes.IFNE, l0);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitLabel(l0);
            }
        }
    }
    
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("org/apache/tools/ant") ||
                className.startsWith("org/apache/maven") ||
                className.startsWith("org/junit/")) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader,
            /* ClassWriter.COMPUTE_FRAMES | */ClassWriter.COMPUTE_MAXS);
            JUnitClassVisitor visitor = new JUnitClassVisitor(className, classWriter);
            classReader.accept(visitor, 0);
            return classWriter.toByteArray();
        } else if (className.equals("junit/framework/TestSuite")) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader,
            ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            TestSuiteClassVisitor visitor = new TestSuiteClassVisitor(classWriter);
            classReader.accept(visitor, 0);
            return classWriter.toByteArray();
        }
        return null;
    }
}
