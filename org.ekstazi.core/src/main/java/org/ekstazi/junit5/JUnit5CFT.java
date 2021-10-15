package org.ekstazi.junit5;

import org.ekstazi.agent.Instr;
import org.ekstazi.asm.*;
import org.ekstazi.log.Log;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class JUnit5CFT implements ClassFileTransformer {
    private static int count = 0;

    private static class TestClassVisitor extends ClassVisitor {
        protected static String mClassName;
        protected static Boolean seenBeforeAll;
        protected static Boolean setBeforeAll;
        protected static Boolean seenAfterAll;
        protected static Boolean setAfterAll;
        public TestClassVisitor(String className, ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
            this.mClassName = className;
            seenBeforeAll = false;
            seenAfterAll = false;
            setBeforeAll = false;
            setAfterAll = false;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            mv = new TestMethodVisitor(mClassName, mv);
            return mv;
        }

        @Override
        public void visitEnd() {
            if (!seenBeforeAll) {
                insertStartCoverage();
                seenBeforeAll = true;
                setBeforeAll = true;
            }
            if (!seenAfterAll) {
                insertEndCoverage();
                seenAfterAll = true;
                setAfterAll = true;
            }
            super.visitEnd();
        }

        private void insertStartCoverage() {
            MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "startCoverage", "()V", null, null);
            AnnotationVisitor av = mv.visitAnnotation("Lorg/junit/jupiter/api/BeforeAll;", true);
            av.visitEnd();
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 0);
            mv.visitEnd();
        }

        private void insertEndCoverage() {
            MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "endCoverage", "()V", null, null);
            AnnotationVisitor av = mv.visitAnnotation("Lorg/junit/jupiter/api/AfterAll;", true);
            av.visitEnd();
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(3, 0);
            mv.visitEnd();
        }

        private static class TestMethodVisitor extends MethodVisitor {

            public TestMethodVisitor (String className, MethodVisitor mv) {
                super(Instr.ASM_API_VERSION, mv);
            }

            @Override
            public void visitCode() {
                if (seenBeforeAll && !setBeforeAll) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/ekstazi/Ekstazi", "inst", "()Lorg/ekstazi/Ekstazi;", false);
                    mv.visitLdcInsn(mClassName);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/ekstazi/Ekstazi", "beginClassCoverage", "(Ljava/lang/String;)V", false);
                    setBeforeAll = true;
                }
                super.visitCode();
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW){
                    if (seenAfterAll && !setAfterAll) {
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/ekstazi/Ekstazi", "inst", "()Lorg/ekstazi/Ekstazi;", false);
                        mv.visitLdcInsn(mClassName);
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/ekstazi/Ekstazi", "endClassCoverage", "(Ljava/lang/String;Z)V", false);
                        setAfterAll = true;
                    }
                }
                mv.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b) {
                if (s.equals("Lorg/junit/jupiter/api/BeforeAll;")) {
                    seenBeforeAll = true;
                } else if (s.equals("Lorg/junit/jupiter/api/AfterAll;")) {
                    seenAfterAll = true;
                }
                return super.visitAnnotation(s, b);
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //Log.d2f("I have entered JUNIT5 CFT: " + className);
        if (className.contains("Test") &&
                !className.contains("org/apache/tools/ant") &&
                !className.startsWith("org/apache/maven") &&
                !className.contains("junit") &&
                !className.contains("opentest4j") &&
                !className.contains("ekstazi")) {
            //Log.d2f("I'm truely modifying JUNIT5 test class: " + className);
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            TestClassVisitor visitor = new TestClassVisitor(className.replace('/','.'), classWriter);
            classReader.accept(visitor, 0);
            Log.write("/Users/alenwang/Documents/xlab/junit5_demo/Shuai_debug" + count++ +".class", classWriter.toByteArray());
            return classWriter.toByteArray();
        }
        return null;
    }
}

