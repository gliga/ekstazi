package org.ekstazi.junit5Extension;

import org.ekstazi.agent.Instr;
import org.ekstazi.asm.*;
import org.ekstazi.log.Log;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class JUnit5ExtensionCFT implements ClassFileTransformer {
    private static int count = 0;

    public static class ExtensionClassVisitor extends ClassVisitor {
        protected Boolean hasExtendWithAnnotation;
        public ExtensionClassVisitor (ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
            hasExtendWithAnnotation = false;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor av = super.visitAnnotation(desc, visible);
            if(desc.equals("Lorg/junit/jupiter/api/extension/ExtendWith;")) {
                hasExtendWithAnnotation = true;
                av = new ExtensionAnnotationVisitor(av);
            }
            return av;
        }

        @Override
        public void visitEnd() {
            if (!hasExtendWithAnnotation) {
                AnnotationVisitor av = this.visitAnnotation("Lorg/junit/jupiter/api/extension/ExtendWith;", false);
                av = av.visitArray("value");
                av.visitEnd();
                hasExtendWithAnnotation = true;
            }
            super.visitEnd();
        }

        private static class ExtensionAnnotationVisitor extends AnnotationVisitor {
            public ExtensionAnnotationVisitor (AnnotationVisitor av) {
                super(Instr.ASM_API_VERSION, av);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                AnnotationVisitor av = super.visitArray(name);
                if (name.equals("value")) {
                    av = new ExtensionArrayAnnotationVisitor(av);
                }
                return av;
            }

            private static class ExtensionArrayAnnotationVisitor extends AnnotationVisitor {
                public ExtensionArrayAnnotationVisitor (AnnotationVisitor av) {
                    super(Instr.ASM_API_VERSION, av);
                }

                @Override
                public void visitEnd() {
                    visit("null", Type.getObjectType("org/ekstazi/junit5Extension/CoverageExtension"));
                    visit("null", Type.getObjectType("org/ekstazi/junit5Extension/ResultExtension"));
                    super.visitEnd();
                }
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.contains("Test") &&
                !className.contains("org/apache/tools/ant") &&
                !className.contains("maven") &&
                !className.contains("junit") && !className.contains("jupiter") &&
                !className.contains("opentest4j") &&
                !className.contains("ekstazi")) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ExtensionClassVisitor visitor = new ExtensionClassVisitor(classWriter);
            classReader.accept(visitor, 0);
            Log.write("/Users/alenwang/Documents/xlab/junit5_demo/Shuai_debug" + count++ + ".class", classWriter.toByteArray());
            return classWriter.toByteArray();
        }
        return null;
    }
}
