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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ekstazi.Names;
import org.ekstazi.agent.Instr;
import org.ekstazi.asm.Label;
import org.ekstazi.asm.MethodVisitor;
import org.ekstazi.asm.Opcodes;
import org.ekstazi.asm.Type;
import org.ekstazi.monitor.CoverageMonitor;
import org.ekstazi.util.Types;

/**
 * {@link MethodVisitor} that instruments code for collecting class coverage. We
 * instrument code by inserting static method calls to {@link CoverageMonitor}.
 * 
 * This class also implements one optimization. Namely, instead of inserting
 * touch method at each place, we keep set of seen classes in between two
 * labels. If there was no label, there is no reason to insert a touch
 * invocation for the same class.
 */
public final class CoverageMethodVisitor extends MethodVisitor {

    /** Class where the method being visited belongs */
    protected final String mClassName;

    /** Access modifiers */
    private final int mAccess;

    /** Method name */
    protected final String mMethodName;

    /** Indicates that classfile major version is >= 49 */
    private final boolean mIsNewerThanJava4;

    /** Set of classes that has been seen since the last label */
    private final Set<String> mSeenClasses;

    /** Probe id of the class being visited */
    private final int mClassProbeId;
    
    /** Count unique probes */
    private final AtomicInteger mProbeCounter;
    
    /**
     * Constructor.
     */
    public CoverageMethodVisitor(String className, int classProbeId, AtomicInteger probeCounter, int access,
            String methodName, MethodVisitor mv, boolean isNewerThanJava4) {
        super(Instr.ASM_API_VERSION, mv);
        this.mClassName = className;
        this.mClassProbeId = classProbeId;
        this.mProbeCounter = probeCounter;
        this.mAccess = access;
        this.mMethodName = methodName;
        this.mIsNewerThanJava4 = isNewerThanJava4;
        this.mSeenClasses = new HashSet<String>();
    }

    // METHOD VISITOR INTERFACE

    @Override
    public void visitLdcInsn(Object cst) {
        // We use this method to support accesses to .class.
        if (cst instanceof Type) {
            int sort = ((Type) cst).getSort();
            if (sort == Type.OBJECT) {
                String className = Types.descToInternalName(((Type) cst).getDescriptor());
                insertTInvocation0(className, mProbeCounter.incrementAndGet());
            }
        }
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitCode() {
        if (isNonPrivateStaticMethod() || isNonPrivateInit() || isStaticBlock()) {
            insertTInvocation0(mClassName, mClassProbeId);
        }
        mv.visitCode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKEINTERFACE) {
            // Instrument invocation to interface (to ensure that we collect
            // annotations).
            insertTInvocation0(owner, mProbeCounter.incrementAndGet());
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        } else if (owner.equals(Instr.CLASS_CLASS_INTERNAL_NAME)
                && (!name.equals("forName") && (!name.equals("newInstance")))) {
            // We have special treatment for reflection invocation: we insert
            // invocation to our monitor and delegate the invocation to the
            // actual reflection method. Note that we ignore static and
            // non-public methods: forName. Also, we ignore newInstance
            // invocation as constructor is already instrumented, so we want to
            // avoid double coverage monitor invocation.
            insertReflectionInvocation(name, desc);
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        // Instrument accesses to static fields. Note that we do not not
        // instrument PUTSTATIC access as we do not have an example where it is
        // needed for test selection.
        if (opcode == Opcodes.GETSTATIC/* || opcode == Opcodes.PUTSTATIC */) {
            String className = Types.descToInternalName(desc);
            // Collect declaring class.
            if (!Types.isPrimitiveDesc(desc) && !className.equals(mClassName)
                    && !Types.isIgnorableBinName(className)) {
                insertFInvocation(owner, name, desc);
            }
            // Collect the owner of the field.
            if (!owner.equals(mClassName)) {
                insertTInvocation0(owner, mProbeCounter.incrementAndGet());
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    // Reasoning during ICSE14 submission showed that we need not instrument
    // checkcast or instanceof.
    // @Override
    // public void visitTypeInsn(int opcode, String type) {
    // // Support for interfaces/annotations
    // if (opcode == Opcodes.CHECKCAST && !type.startsWith("[")) {
    // /* || opcode == Opcodes.INSTANCEOF */
    // insertTInvocation0(type);
    // }
    // mv.visitTypeInsn(opcode, type);
    // }

    /**
     * This method is part of an optimization. Whenever we encounter a new
     * label, clean the set of seen classes (as the jump may happen to the
     * label).
     */
    @Override
    public void visitLabel(Label label) {
        mSeenClasses.clear();
        super.visitLabel(label);
    }

    // x. (we tried). Previous approach would insert all invocations to the
    // monitor when return or throw are encountered. Note that this
    // approach is not safe: if there is an exception from any of the
    // called methods, classes that are before that method would not
    // be recorded.
    
    // TOUCH INVOCATIONS
    private void loadProbeValue(int probeId) {
        if (probeId <= 127) {
            mv.visitIntInsn(Opcodes.BIPUSH, probeId);
        } else if (probeId <= 32767) {
            mv.visitIntInsn(Opcodes.SIPUSH, probeId);
        } else {
            mv.visitLdcInsn(new Integer(probeId));
        }
    }

    protected void insertTInvocation(String className, int probeId) {
        mv.visitLdcInsn(Type.getType("L" + className + ";"));
        loadProbeValue(probeId);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Names.COVERAGE_MONITOR_VM,
                Instr.COVERAGE_MONITOR_MNAME, Instr.CLASS_I_V_DESC, false);
    }

    protected void insertFInvocation(String owner, String name, String desc) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
        // We need different id for field accesses.
        loadProbeValue(mProbeCounter.incrementAndGet());
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Names.COVERAGE_MONITOR_VM,
                Instr.COVERAGE_MONITOR_FIELD_MNAME, Instr.OBJECT_I_V_DESC, false);
    }

    protected void insertReflectionInvocation(String name, String desc) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instr.REFLECTION_MONITOR_CLASS_INTERNAL_NAME, name,
                desc.replaceFirst("\\(", "(Ljava/lang/Class;"), false);
    }

    /**
     * Checks if the class name belongs to a set of classes that should be
     * ignored; if not, an invocation to coverage monitor is inserted.
     * 
     * @param className
     *            Name of the class to be passed to coverage monitor.
     */
    private void insertTInvocation0(String className, int probeId) {
        // Check if class name has been seen since the last label.
        if (!mSeenClasses.add(className)) return;
        // Check if this class name should be ignored.
        if (Types.isIgnorableInternalName(className)) return;

        // x. (we tried). Surround invocation of monitor with
        // try/finally. This approach did not work in some cases as I
        // was using local variables to save exception exception that
        // has to be thrown; however, the same local variable may be
        // used in finally block, so I would override.

        // NOTE: The following is deprecated and excluded from code
        // (see monitor for new approach and accesses to fields):
        // We check if class contains "$" in which case we assume (without
        // consequences) that the class is inner and we invoke coverage method
        // that takes string as input. The reason for this special treatment is
        // access policy, as the inner class may be private and we cannot load
        // its .class. (See 57f5365935d26d2e6c47ec368612c6b003a2da79 for the
        // test that is throwing an exception without this.) However, note
        // that this slows down the execution a lot.
        // boolean isNonInnerClass = !className.contains("$");
        boolean isNonInnerClass = true;
        // See the class visitor; currently we override
        // the version number to 49 for all classes that have lower version
        // number (so else branch should not be executed for this reason any
        // more). Earlier comment: Note that we have to use class name in case
        // of classes that have classfiles with major version of 48 or lower
        // as ldc could not load .class prior to version 49 (in theory we
        // could generate a method that does it for us, as the compiler
        // would do, but then we would change bytecode too much).
        if (isNonInnerClass && mIsNewerThanJava4) {
            insertTInvocation(className, probeId);
        } else {
            // DEPRECATED: This part is deprecated and should never be executed.
            // Using Class.forName(className) was very slow.
            mv.visitLdcInsn(className.replaceAll("/", "."));
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Names.COVERAGE_MONITOR_VM,
                    Instr.COVERAGE_MONITOR_MNAME, Instr.STRING_V_DESC, false);
        }
    }

    // INTERNAL

    /**
     * Returns true if visited method is non private and name is <init>.
     * 
     * Initially it looked that we may need to collect private constructors too
     * because of reflection calls. However, to get constructor to be invoked,
     * we already collected the class.
     */
    protected boolean isNonPrivateInit() {
        // As inner classes may have private constructor too, we have to
        // instrument these constructors as well; at the moment, we do not
        // optimize for private constructors.
        return /* (mAccess & Opcodes.ACC_PRIVATE) == 0 && */ mMethodName.equals("<init>");
    }

    /**
     * Returns true if visited method is non private and static.
     * 
     * See comment for {@link #isNonPrivateInit()}.
     */
    protected boolean isNonPrivateStaticMethod() {
        return (mAccess & Opcodes.ACC_PRIVATE) == 0 && (mAccess & Opcodes.ACC_STATIC) != 0;
    }

    /**
     * Returns true if static block.
     */
    protected boolean isStaticBlock() {
        return mMethodName.equals("<clinit>");
    }
}
