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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.ekstazi.agent.Instr;
import org.ekstazi.asm.ClassReader;
import org.ekstazi.asm.ClassVisitor;
import org.ekstazi.asm.ClassWriter;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;
import org.ekstazi.research.Research;

@Research
public final class MavenCFT implements ClassFileTransformer {

    /**
     * Method visitor that inserts a method invocation to the interceptor before
     * Mojo.execute.
     */
    private static class MavenMethodVisitor extends MethodVisitor {
        private final String mMethodName;
        private final String mMethodDesc;

        /** Name of the class that is invoked before execute method on mojo */
        private final String mInterceptorName;

        public MavenMethodVisitor(String interceptorName, String methodName, String methodDesc, MethodVisitor mv) {
            super(Instr.ASM_API_VERSION, mv);
            this.mInterceptorName = interceptorName;
            this.mMethodName = methodName;
            this.mMethodDesc = methodDesc;
        }

        @Override
        public void visitCode() {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mInterceptorName, mMethodName,
                    mMethodDesc.replace("(", "(Ljava/lang/Object;"), false);
            mv.visitCode();
        }
    }

    private static class MavenClassVisitor extends ClassVisitor {
        @SuppressWarnings("unused")
        private final String mClassName;

        /** Name of the class that is invoked before execute method on mojo */
        private final String mInterceptorName;

        /**
         * Constructor.
         */
        public MavenClassVisitor(String className, String interceptorName, ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
            this.mClassName = className;
            this.mInterceptorName = interceptorName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals(MavenNames.EXECUTE_MNAME) && desc.equals(MavenNames.EXECUTE_MDESC)) {
                mv = new MavenMethodVisitor(mInterceptorName, name, desc, mv);
            }
            return mv;
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals(MavenNames.ABSTRACT_SUREFIRE_MOJO_VM)
                || className.equals(MavenNames.SUREFIRE_PLUGIN_VM)
                || className.equals(MavenNames.FAILSAFE_PLUGIN_VM)) {
            // If class has no execute method, nothing will change.
            return addInterceptor(className, classfileBuffer, MavenNames.SUREFIRE_INTERCEPTOR_CLASS_VM);
        } else if (className.equals(MavenNames.TESTMOJO_VM)) {
            return addInterceptor(className, classfileBuffer, MavenNames.SCALATEST_INTERCEPTOR_CLASS_VM);
        } else {
            return null;
        }
    }

    // INTERNAL

    private byte[] addInterceptor(String className, byte[] classfileBuffer, String interceptorName) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new MavenClassVisitor(className, interceptorName, classWriter);
        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}
