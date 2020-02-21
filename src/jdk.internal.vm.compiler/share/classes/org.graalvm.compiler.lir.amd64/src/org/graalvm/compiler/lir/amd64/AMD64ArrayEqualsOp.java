/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package org.graalvm.compiler.lir.amd64;

import static jdk.vm.ci.code.ValueUtil.asRegister;
import static org.graalvm.compiler.asm.amd64.AMD64Assembler.AMD64BinaryArithmetic.XOR;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.CONST;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.ILLEGAL;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.REG;

import java.util.Objects;

import org.graalvm.compiler.asm.Label;
import org.graalvm.compiler.asm.amd64.AMD64Address;
import org.graalvm.compiler.asm.amd64.AMD64Address.Scale;
import org.graalvm.compiler.asm.amd64.AMD64Assembler;
import org.graalvm.compiler.asm.amd64.AMD64Assembler.ConditionFlag;
import org.graalvm.compiler.asm.amd64.AMD64Assembler.SSEOp;
import org.graalvm.compiler.asm.amd64.AMD64BaseAssembler.OperandSize;
import org.graalvm.compiler.asm.amd64.AMD64MacroAssembler;
import org.graalvm.compiler.asm.amd64.AVXKind;
import org.graalvm.compiler.core.common.LIRKind;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.LIRValueUtil;
import org.graalvm.compiler.lir.Opcode;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.amd64.AMD64.CPUFeature;
import jdk.vm.ci.amd64.AMD64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.TargetDescription;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.Value;

/**
 * Emits code which compares two arrays of the same length. If the CPU supports any vector
 * instructions specialized code is emitted to leverage these instructions.
 *
 * This op can also compare arrays of different integer types (e.g. {@code byte[]} and
 * {@code char[]}) with on-the-fly sign- or zero-extension. If one of the given arrays is a
 * {@code char[]} array, the smaller elements are zero-extended, otherwise they are sign-extended.
 */
@Opcode("ARRAY_EQUALS")
public final class AMD64ArrayEqualsOp extends AMD64LIRInstruction {
    public static final LIRInstructionClass<AMD64ArrayEqualsOp> TYPE = LIRInstructionClass.create(AMD64ArrayEqualsOp.class);

    private final JavaKind kind1;
    private final JavaKind kind2;
    private final int arrayBaseOffset1;
    private final int arrayBaseOffset2;
    private final Scale arrayIndexScale1;
    private final Scale arrayIndexScale2;
    private final AVXKind.AVXSize vectorSize;
    private final boolean signExtend;

    @Def({REG}) private Value resultValue;
    @Alive({REG}) private Value array1Value;
    @Alive({REG}) private Value array2Value;
    @Alive({REG, CONST}) private Value lengthValue;
    @Temp({REG, ILLEGAL}) private Value temp1;
    @Temp({REG, ILLEGAL}) private Value temp2;
    @Temp({REG}) private Value temp3;
    @Temp({REG, ILLEGAL}) private Value temp4;

    @Temp({REG, ILLEGAL}) private Value temp5;
    @Temp({REG, ILLEGAL}) private Value tempXMM;

    @Temp({REG, ILLEGAL}) private Value vectorTemp1;
    @Temp({REG, ILLEGAL}) private Value vectorTemp2;
    @Temp({REG, ILLEGAL}) private Value vectorTemp3;
    @Temp({REG, ILLEGAL}) private Value vectorTemp4;

    public AMD64ArrayEqualsOp(LIRGeneratorTool tool, JavaKind kind1, JavaKind kind2, Value result, Value array1, Value array2, Value length,
                    boolean directPointers, int maxVectorSize) {
        super(TYPE);
        this.kind1 = kind1;
        this.kind2 = kind2;
        this.signExtend = kind1 != JavaKind.Char && kind2 != JavaKind.Char;

        assert kind1.isNumericInteger() && kind2.isNumericInteger() || kind1 == kind2;

        this.arrayBaseOffset1 = directPointers ? 0 : tool.getProviders().getMetaAccess().getArrayBaseOffset(kind1);
        this.arrayBaseOffset2 = directPointers ? 0 : tool.getProviders().getMetaAccess().getArrayBaseOffset(kind2);
        this.arrayIndexScale1 = Objects.requireNonNull(Scale.fromInt(tool.getProviders().getMetaAccess().getArrayIndexScale(kind1)));
        this.arrayIndexScale2 = Objects.requireNonNull(Scale.fromInt(tool.getProviders().getMetaAccess().getArrayIndexScale(kind2)));
        this.vectorSize = ((AMD64) tool.target().arch).getFeatures().contains(CPUFeature.AVX2) && (maxVectorSize < 0 || maxVectorSize >= 32) ? AVXKind.AVXSize.YMM : AVXKind.AVXSize.XMM;

        this.resultValue = result;
        this.array1Value = array1;
        this.array2Value = array2;
        this.lengthValue = length;

        // Allocate some temporaries.
        if (supportsSSE41(tool.target()) && canGenerateConstantLengthCompare(tool.target()) && !constantLengthCompareNeedsTmpArrayPointers()) {
            this.temp1 = Value.ILLEGAL;
            this.temp2 = Value.ILLEGAL;
        } else {
            this.temp1 = tool.newVariable(LIRKind.unknownReference(tool.target().arch.getWordKind()));
            this.temp2 = tool.newVariable(LIRKind.unknownReference(tool.target().arch.getWordKind()));
        }
        this.temp3 = tool.newVariable(LIRKind.value(tool.target().arch.getWordKind()));
        if (supportsSSE41(tool.target()) && canGenerateConstantLengthCompare(tool.target())) {
            this.temp4 = Value.ILLEGAL;
            this.temp5 = Value.ILLEGAL;
        } else {
            this.temp4 = tool.newVariable(LIRKind.value(tool.target().arch.getWordKind()));
            this.temp5 = kind1.isNumericFloat() || kind1 != kind2 ? tool.newVariable(LIRKind.value(tool.target().arch.getWordKind())) : Value.ILLEGAL;
        }

        if (kind1 == JavaKind.Float) {
            this.tempXMM = tool.newVariable(LIRKind.value(AMD64Kind.SINGLE));
        } else if (kind1 == JavaKind.Double) {
            this.tempXMM = tool.newVariable(LIRKind.value(AMD64Kind.DOUBLE));
        } else {
            this.tempXMM = Value.ILLEGAL;
        }

        // We only need the vector temporaries if we generate SSE code.
        if (supportsSSE41(tool.target())) {
            if (canGenerateConstantLengthCompare(tool.target())) {
                LIRKind lirKind = LIRKind.value(vectorSize == AVXKind.AVXSize.YMM ? AMD64Kind.V256_BYTE : AMD64Kind.V128_BYTE);
                this.vectorTemp1 = tool.newVariable(lirKind);
                this.vectorTemp2 = tool.newVariable(lirKind);
                this.vectorTemp3 = tool.newVariable(lirKind);
                this.vectorTemp4 = tool.newVariable(lirKind);
            } else {
                this.vectorTemp1 = tool.newVariable(LIRKind.value(AMD64Kind.DOUBLE));
                this.vectorTemp2 = tool.newVariable(LIRKind.value(AMD64Kind.DOUBLE));
                this.vectorTemp3 = Value.ILLEGAL;
                this.vectorTemp4 = Value.ILLEGAL;
            }
        } else {
            this.vectorTemp1 = Value.ILLEGAL;
            this.vectorTemp2 = Value.ILLEGAL;
            this.vectorTemp3 = Value.ILLEGAL;
            this.vectorTemp4 = Value.ILLEGAL;
        }
    }

    private boolean canGenerateConstantLengthCompare(TargetDescription target) {
        return LIRValueUtil.isJavaConstant(lengthValue) && kind1.isNumericInteger() && (kind1 == kind2 || getElementsPerVector(AVXKind.AVXSize.XMM) <= constantLength()) && supportsSSE41(target);
    }

    private int constantLength() {
        return LIRValueUtil.asJavaConstant(lengthValue).asInt();
    }

    @Override
    public void emitCode(CompilationResultBuilder crb, AMD64MacroAssembler masm) {
        Register result = asRegister(resultValue);

        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label done = new Label();

        if (canGenerateConstantLengthCompare(crb.target)) {
            emitConstantLengthArrayCompareBytes(crb, masm, new Register[]{asRegister(vectorTemp1), asRegister(vectorTemp2), asRegister(vectorTemp3), asRegister(vectorTemp4)}, falseLabel);
        } else {
            Register array1 = asRegister(temp1);
            Register array2 = asRegister(temp2);
            // Load array base addresses.
            masm.leaq(array1, new AMD64Address(asRegister(array1Value), arrayBaseOffset1));
            masm.leaq(array2, new AMD64Address(asRegister(array2Value), arrayBaseOffset2));
            Register length = asRegister(temp3);
            // Get array length.
            if (LIRValueUtil.isJavaConstant(lengthValue)) {
                masm.movl(length, constantLength());
            } else {
                masm.movl(length, asRegister(lengthValue));
            }
            // copy
            masm.movl(result, length);
            emitArrayCompare(crb, masm, result, array1, array2, length, trueLabel, falseLabel);
        }

        // Return true
        masm.bind(trueLabel);
        masm.movl(result, 1);
        masm.jmpb(done);

        // Return false
        masm.bind(falseLabel);
        masm.xorl(result, result);

        // That's it
        masm.bind(done);
    }

    private void emitArrayCompare(CompilationResultBuilder crb, AMD64MacroAssembler masm,
                    Register result, Register array1, Register array2, Register length,
                    Label trueLabel, Label falseLabel) {
        if (supportsSSE41(crb.target)) {
            emitVectorCompare(crb, masm, result, array1, array2, length, trueLabel, falseLabel);
        }
        if (kind1 == kind2) {
            emit8ByteCompare(crb, masm, result, array1, array2, length, trueLabel, falseLabel);
            emitTailCompares(masm, result, array1, array2, length, trueLabel, falseLabel);
        } else {
            emitDifferentKindsElementWiseCompare(crb, masm, result, array1, array2, length, trueLabel, falseLabel);
        }
    }

    /**
     * Returns if the underlying AMD64 architecture supports SSE 4.1 instructions.
     *
     * @param target target description of the underlying architecture
     * @return true if the underlying architecture supports SSE 4.1
     */
    private static boolean supportsSSE41(TargetDescription target) {
        AMD64 arch = (AMD64) target.arch;
        return arch.getFeatures().contains(CPUFeature.SSE4_1);
    }

    /**
     * Emits code that uses SSE4.1/AVX1 128-bit (16-byte) or AVX2 256-bit (32-byte) vector compares.
     */
    private void emitVectorCompare(CompilationResultBuilder crb, AMD64MacroAssembler masm,
                    Register result, Register array1, Register array2, Register length,
                    Label trueLabel, Label falseLabel) {
        assert supportsSSE41(crb.target);

        Register vector1 = asRegister(vectorTemp1);
        Register vector2 = asRegister(vectorTemp2);

        int elementsPerVector = getElementsPerVector(vectorSize);

        Label loop = new Label();
        Label compareTail = new Label();

        boolean requiresNaNCheck = kind1.isNumericFloat();
        Label loopCheck = new Label();
        Label nanCheck = new Label();

        // Compare 16-byte vectors
        masm.andl(result, elementsPerVector - 1); // tail count
        masm.andlAndJcc(length, ~(elementsPerVector - 1), ConditionFlag.Zero, compareTail, false);

        masm.leaq(array1, new AMD64Address(array1, length, arrayIndexScale1, 0));
        masm.leaq(array2, new AMD64Address(array2, length, arrayIndexScale2, 0));
        masm.negq(length);

        // Align the main loop
        masm.align(crb.target.wordSize * 2);
        masm.bind(loop);
        emitVectorLoad1(masm, vector1, array1, length, 0, vectorSize);
        emitVectorLoad2(masm, vector2, array2, length, 0, vectorSize);
        emitVectorCmp(masm, vector1, vector2, vectorSize);
        masm.jcc(ConditionFlag.NotZero, requiresNaNCheck ? nanCheck : falseLabel);

        masm.bind(loopCheck);
        masm.addqAndJcc(length, elementsPerVector, ConditionFlag.NotZero, loop, false);

        masm.testlAndJcc(result, result, ConditionFlag.Zero, trueLabel, false);

        if (requiresNaNCheck) {
            Label unalignedCheck = new Label();
            masm.jmpb(unalignedCheck);
            masm.bind(nanCheck);
            emitFloatCompareWithinRange(crb, masm, array1, array2, length, 0, falseLabel, elementsPerVector);
            masm.jmpb(loopCheck);
            masm.bind(unalignedCheck);
        }

        /*
         * Compare the remaining bytes with an unaligned memory load aligned to the end of the
         * array.
         */
        emitVectorLoad1(masm, vector1, array1, result, scaleDisplacement1(-vectorSize.getBytes()), vectorSize);
        emitVectorLoad2(masm, vector2, array2, result, scaleDisplacement2(-vectorSize.getBytes()), vectorSize);
        emitVectorCmp(masm, vector1, vector2, vectorSize);
        if (requiresNaNCheck) {
            masm.jcc(ConditionFlag.Zero, trueLabel);
            emitFloatCompareWithinRange(crb, masm, array1, array2, result, -vectorSize.getBytes(), falseLabel, elementsPerVector);
        } else {
            masm.jcc(ConditionFlag.NotZero, falseLabel);
        }
        masm.jmp(trueLabel);

        masm.bind(compareTail);
        masm.movl(length, result);
    }

    private int getElementsPerVector(AVXKind.AVXSize vSize) {
        return vSize.getBytes() >> Math.max(arrayIndexScale1.log2, arrayIndexScale2.log2);
    }

    private void emitVectorLoad1(AMD64MacroAssembler asm, Register dst, Register src, int displacement, AVXKind.AVXSize size) {
        emitVectorLoad1(asm, dst, src, Register.None, displacement, size);
    }

    private void emitVectorLoad2(AMD64MacroAssembler asm, Register dst, Register src, int displacement, AVXKind.AVXSize size) {
        emitVectorLoad2(asm, dst, src, Register.None, displacement, size);
    }

    private void emitVectorLoad1(AMD64MacroAssembler asm, Register dst, Register src, Register index, int displacement, AVXKind.AVXSize size) {
        emitVectorLoad(asm, dst, src, index, displacement, arrayIndexScale1, arrayIndexScale2, size);
    }

    private void emitVectorLoad2(AMD64MacroAssembler asm, Register dst, Register src, Register index, int displacement, AVXKind.AVXSize size) {
        emitVectorLoad(asm, dst, src, index, displacement, arrayIndexScale2, arrayIndexScale1, size);
    }

    private void emitVectorLoad(AMD64MacroAssembler asm, Register dst, Register src, Register index, int displacement, Scale ownScale, Scale otherScale, AVXKind.AVXSize size) {
        AMD64Address address = new AMD64Address(src, index, ownScale, displacement);
        if (ownScale.value < otherScale.value) {
            if (size == AVXKind.AVXSize.YMM) {
                getAVX2LoadAndExtendOp(ownScale, otherScale, signExtend).emit(asm, size, dst, address);
            } else {
                loadAndExtendSSE(asm, dst, address, ownScale, otherScale, signExtend);
            }
        } else {
            if (size == AVXKind.AVXSize.YMM) {
                asm.vmovdqu(dst, address);
            } else {
                asm.movdqu(dst, address);
            }
        }
    }

    private int scaleDisplacement1(int displacement) {
        return scaleDisplacement(displacement, arrayIndexScale1, arrayIndexScale2);
    }

    private int scaleDisplacement2(int displacement) {
        return scaleDisplacement(displacement, arrayIndexScale2, arrayIndexScale1);
    }

    private static int scaleDisplacement(int displacement, Scale ownScale, Scale otherScale) {
        if (ownScale.value < otherScale.value) {
            return displacement >> (otherScale.log2 - ownScale.log2);
        }
        return displacement;
    }

    private static AMD64Assembler.VexRMOp getAVX2LoadAndExtendOp(Scale ownScale, Scale otherScale, boolean signExtend) {
        switch (ownScale) {
            case Times1:
                switch (otherScale) {
                    case Times2:
                        return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXBW : AMD64Assembler.VexRMOp.VPMOVZXBW;
                    case Times4:
                        return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXBD : AMD64Assembler.VexRMOp.VPMOVZXBD;
                    case Times8:
                        return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXBQ : AMD64Assembler.VexRMOp.VPMOVZXBQ;
                }
                throw GraalError.shouldNotReachHere();
            case Times2:
                switch (otherScale) {
                    case Times4:
                        return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXWD : AMD64Assembler.VexRMOp.VPMOVZXWD;
                    case Times8:
                        return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXWQ : AMD64Assembler.VexRMOp.VPMOVZXWQ;
                }
                throw GraalError.shouldNotReachHere();
            case Times4:
                return signExtend ? AMD64Assembler.VexRMOp.VPMOVSXDQ : AMD64Assembler.VexRMOp.VPMOVZXDQ;
        }
        throw GraalError.shouldNotReachHere();
    }

    private static void loadAndExtendSSE(AMD64MacroAssembler asm, Register dst, AMD64Address src, Scale ownScale, Scale otherScale, boolean signExtend) {
        switch (ownScale) {
            case Times1:
                switch (otherScale) {
                    case Times2:
                        if (signExtend) {
                            asm.pmovsxbw(dst, src);
                        } else {
                            asm.pmovzxbw(dst, src);
                        }
                        return;
                    case Times4:
                        if (signExtend) {
                            asm.pmovsxbd(dst, src);
                        } else {
                            asm.pmovzxbd(dst, src);
                        }
                        return;
                    case Times8:
                        if (signExtend) {
                            asm.pmovsxbq(dst, src);
                        } else {
                            asm.pmovzxbq(dst, src);
                        }
                        return;
                }
                throw GraalError.shouldNotReachHere();
            case Times2:
                switch (otherScale) {
                    case Times4:
                        if (signExtend) {
                            asm.pmovsxwd(dst, src);
                        } else {
                            asm.pmovzxwd(dst, src);
                        }
                        return;
                    case Times8:
                        if (signExtend) {
                            asm.pmovsxwq(dst, src);
                        } else {
                            asm.pmovzxwq(dst, src);
                        }
                        return;
                }
                throw GraalError.shouldNotReachHere();
            case Times4:
                if (signExtend) {
                    asm.pmovsxdq(dst, src);
                } else {
                    asm.pmovzxdq(dst, src);
                }
                return;
        }
        throw GraalError.shouldNotReachHere();
    }

    private static void emitVectorCmp(AMD64MacroAssembler masm, Register vector1, Register vector2, AVXKind.AVXSize size) {
        emitVectorXor(masm, vector1, vector2, size);
        emitVectorTest(masm, vector1, size);
    }

    private static void emitVectorXor(AMD64MacroAssembler masm, Register vector1, Register vector2, AVXKind.AVXSize size) {
        if (size == AVXKind.AVXSize.YMM) {
            masm.vpxor(vector1, vector1, vector2);
        } else {
            masm.pxor(vector1, vector2);
        }
    }

    private static void emitVectorTest(AMD64MacroAssembler masm, Register vector1, AVXKind.AVXSize size) {
        if (size == AVXKind.AVXSize.YMM) {
            masm.vptest(vector1, vector1);
        } else {
            masm.ptest(vector1, vector1);
        }
    }

    /**
     * Vector size used in {@link #emit8ByteCompare}.
     */
    private static final int VECTOR_SIZE = 8;

    /**
     * Emits code that uses 8-byte vector compares.
     */
    private void emit8ByteCompare(CompilationResultBuilder crb, AMD64MacroAssembler masm,
                    Register result, Register array1, Register array2, Register length, Label trueLabel, Label falseLabel) {
        assert kind1 == kind2;
        Label loop = new Label();
        Label compareTail = new Label();

        int elementsPerVector = 8 >> arrayIndexScale1.log2;

        boolean requiresNaNCheck = kind1.isNumericFloat();
        Label loopCheck = new Label();
        Label nanCheck = new Label();

        Register temp = asRegister(temp4);

        masm.andl(result, elementsPerVector - 1); // tail count
        masm.andlAndJcc(length, ~(elementsPerVector - 1), ConditionFlag.Zero, compareTail, false);

        masm.leaq(array1, new AMD64Address(array1, length, arrayIndexScale1, 0));
        masm.leaq(array2, new AMD64Address(array2, length, arrayIndexScale2, 0));
        masm.negq(length);

        // Align the main loop
        masm.align(crb.target.wordSize * 2);
        masm.bind(loop);
        masm.movq(temp, new AMD64Address(array1, length, arrayIndexScale1, 0));
        masm.cmpqAndJcc(temp, new AMD64Address(array2, length, arrayIndexScale2, 0), ConditionFlag.NotEqual, requiresNaNCheck ? nanCheck : falseLabel, false);

        masm.bind(loopCheck);
        masm.addqAndJcc(length, elementsPerVector, ConditionFlag.NotZero, loop, true);

        masm.testlAndJcc(result, result, ConditionFlag.Zero, trueLabel, false);

        if (requiresNaNCheck) {
            // NaN check is slow path and hence placed outside of the main loop.
            Label unalignedCheck = new Label();
            masm.jmpb(unalignedCheck);
            masm.bind(nanCheck);
            // At most two iterations, unroll in the emitted code.
            for (int offset = 0; offset < VECTOR_SIZE; offset += kind1.getByteCount()) {
                emitFloatCompare(masm, array1, array2, length, offset, falseLabel, kind1.getByteCount() == VECTOR_SIZE);
            }
            masm.jmpb(loopCheck);
            masm.bind(unalignedCheck);
        }

        /*
         * Compare the remaining bytes with an unaligned memory load aligned to the end of the
         * array.
         */
        masm.movq(temp, new AMD64Address(array1, result, arrayIndexScale1, -VECTOR_SIZE));
        if (requiresNaNCheck) {
            masm.cmpqAndJcc(temp, new AMD64Address(array2, result, arrayIndexScale2, -VECTOR_SIZE), ConditionFlag.Equal, trueLabel, false);
            // At most two iterations, unroll in the emitted code.
            for (int offset = 0; offset < VECTOR_SIZE; offset += kind1.getByteCount()) {
                emitFloatCompare(masm, array1, array2, result, -VECTOR_SIZE + offset, falseLabel, kind1.getByteCount() == VECTOR_SIZE);
            }
        } else {
            masm.cmpqAndJcc(temp, new AMD64Address(array2, result, arrayIndexScale2, -VECTOR_SIZE), ConditionFlag.NotEqual, falseLabel, true);
        }
        masm.jmpb(trueLabel);

        masm.bind(compareTail);
        masm.movl(length, result);
    }

    /**
     * Emits code to compare the remaining 1 to 4 bytes.
     */
    private void emitTailCompares(AMD64MacroAssembler masm,
                    Register result, Register array1, Register array2, Register length, Label trueLabel, Label falseLabel) {
        assert kind1 == kind2;
        Label compare2Bytes = new Label();
        Label compare1Byte = new Label();

        Register temp = asRegister(temp4);

        if (kind1.getByteCount() <= 4) {
            // Compare trailing 4 bytes, if any.
            masm.testlAndJcc(result, arrayIndexScale1.log2 == 0 ? 4 : 4 >> arrayIndexScale1.log2, ConditionFlag.Zero, compare2Bytes, true);
            masm.movl(temp, new AMD64Address(array1, 0));
            if (kind1 == JavaKind.Float) {
                masm.cmplAndJcc(temp, new AMD64Address(array2, 0), ConditionFlag.Equal, trueLabel, true);
                emitFloatCompare(masm, array1, array2, Register.None, 0, falseLabel, true);
                masm.jmpb(trueLabel);
            } else {
                masm.cmplAndJcc(temp, new AMD64Address(array2, 0), ConditionFlag.NotEqual, falseLabel, true);
            }
            if (kind1.getByteCount() <= 2) {
                // Move array pointers forward.
                masm.leaq(array1, new AMD64Address(array1, 4));
                masm.leaq(array2, new AMD64Address(array2, 4));

                // Compare trailing 2 bytes, if any.
                masm.bind(compare2Bytes);
                masm.testlAndJcc(result, arrayIndexScale1.log2 == 0 ? 2 : 2 >> arrayIndexScale1.log2, ConditionFlag.Zero, compare1Byte, true);
                masm.movzwl(temp, new AMD64Address(array1, 0));
                masm.movzwl(length, new AMD64Address(array2, 0));
                masm.cmplAndJcc(temp, length, ConditionFlag.NotEqual, falseLabel, true);

                // The one-byte tail compare is only required for boolean and byte arrays.
                if (kind1.getByteCount() <= 1) {
                    // Move array pointers forward before we compare the last trailing byte.
                    masm.leaq(array1, new AMD64Address(array1, 2));
                    masm.leaq(array2, new AMD64Address(array2, 2));

                    // Compare trailing byte, if any.
                    // TODO (yz) this can be optimized, i.e., bind after padding
                    masm.bind(compare1Byte);
                    masm.testlAndJcc(result, 1, ConditionFlag.Zero, trueLabel, true);
                    masm.movzbl(temp, new AMD64Address(array1, 0));
                    masm.movzbl(length, new AMD64Address(array2, 0));
                    masm.cmplAndJcc(temp, length, ConditionFlag.NotEqual, falseLabel, true);
                } else {
                    masm.bind(compare1Byte);
                }
            } else {
                masm.bind(compare2Bytes);
            }
        }
    }

    private void emitDifferentKindsElementWiseCompare(CompilationResultBuilder crb, AMD64MacroAssembler masm,
                    Register result, Register array1, Register array2, Register length, Label trueLabel, Label falseLabel) {
        assert kind1 != kind2;
        assert kind1.isNumericInteger() && kind2.isNumericInteger();
        Label loop = new Label();
        Label compareTail = new Label();

        int elementsPerLoopIteration = 4;

        Register tmp1 = asRegister(temp4);
        Register tmp2 = asRegister(temp5);

        masm.andl(result, elementsPerLoopIteration - 1); // tail count
        masm.andlAndJcc(length, ~(elementsPerLoopIteration - 1), ConditionFlag.Zero, compareTail, false);

        masm.leaq(array1, new AMD64Address(array1, length, arrayIndexScale1, 0));
        masm.leaq(array2, new AMD64Address(array2, length, arrayIndexScale2, 0));
        masm.negq(length);

        // clear comparison registers because of the missing movzlq instruction
        masm.xorq(tmp1, tmp1);
        masm.xorq(tmp2, tmp2);

        // Align the main loop
        masm.align(crb.target.wordSize * 2);
        masm.bind(loop);
        for (int i = 0; i < elementsPerLoopIteration; i++) {
            emitMovBytes(masm, tmp1, new AMD64Address(array1, length, arrayIndexScale1, i << arrayIndexScale1.log2), kind1.getByteCount());
            emitMovBytes(masm, tmp2, new AMD64Address(array2, length, arrayIndexScale2, i << arrayIndexScale2.log2), kind2.getByteCount());
            masm.cmpqAndJcc(tmp1, tmp2, ConditionFlag.NotEqual, falseLabel, false);
        }
        masm.addqAndJcc(length, elementsPerLoopIteration, ConditionFlag.NotZero, loop, true);

        masm.bind(compareTail);
        masm.testlAndJcc(result, result, ConditionFlag.Zero, trueLabel, false);
        for (int i = 0; i < elementsPerLoopIteration - 1; i++) {
            emitMovBytes(masm, tmp1, new AMD64Address(array1, length, arrayIndexScale1, 0), kind1.getByteCount());
            emitMovBytes(masm, tmp2, new AMD64Address(array2, length, arrayIndexScale2, 0), kind2.getByteCount());
            masm.cmpqAndJcc(tmp1, tmp2, ConditionFlag.NotEqual, falseLabel, false);
            if (i < elementsPerLoopIteration - 2) {
                masm.incrementq(length, 1);
                masm.decqAndJcc(result, ConditionFlag.Zero, trueLabel, false);
            } else {
                masm.jmpb(trueLabel);
            }
        }
    }

    /**
     * Emits code to fall through if {@code src} is NaN, otherwise jump to {@code branchOrdered}.
     */
    private void emitNaNCheck(AMD64MacroAssembler masm, AMD64Address src, Label branchIfNonNaN) {
        assert kind1.isNumericFloat();
        Register tempXMMReg = asRegister(tempXMM);
        if (kind1 == JavaKind.Float) {
            masm.movflt(tempXMMReg, src);
        } else {
            masm.movdbl(tempXMMReg, src);
        }
        SSEOp.UCOMIS.emit(masm, kind1 == JavaKind.Float ? OperandSize.PS : OperandSize.PD, tempXMMReg, tempXMMReg);
        masm.jcc(ConditionFlag.NoParity, branchIfNonNaN);
    }

    /**
     * Emits code to compare if two floats are bitwise equal or both NaN.
     */
    private void emitFloatCompare(AMD64MacroAssembler masm, Register base1, Register base2, Register index, int offset, Label falseLabel,
                    boolean skipBitwiseCompare) {
        AMD64Address address1 = new AMD64Address(base1, index, arrayIndexScale1, offset);
        AMD64Address address2 = new AMD64Address(base2, index, arrayIndexScale2, offset);

        Label bitwiseEqual = new Label();

        if (!skipBitwiseCompare) {
            // Bitwise compare
            Register temp = asRegister(temp4);

            if (kind1 == JavaKind.Float) {
                masm.movl(temp, address1);
                masm.cmplAndJcc(temp, address2, ConditionFlag.Equal, bitwiseEqual, true);
            } else {
                masm.movq(temp, address1);
                masm.cmpqAndJcc(temp, address2, ConditionFlag.Equal, bitwiseEqual, true);
            }
        }

        emitNaNCheck(masm, address1, falseLabel);
        emitNaNCheck(masm, address2, falseLabel);

        masm.bind(bitwiseEqual);
    }

    /**
     * Emits code to compare float equality within a range.
     */
    private void emitFloatCompareWithinRange(CompilationResultBuilder crb, AMD64MacroAssembler masm,
                    Register base1, Register base2, Register index, int offset, Label falseLabel, int range) {
        assert kind1.isNumericFloat();
        Label loop = new Label();
        Register i = asRegister(temp5);

        masm.movq(i, range);
        masm.negq(i);
        // Align the main loop
        masm.align(crb.target.wordSize * 2);
        masm.bind(loop);
        emitFloatCompare(masm, base1, base2, index, offset, falseLabel, range == 1);
        masm.incrementq(index, 1);
        masm.incqAndJcc(i, ConditionFlag.NotZero, loop, true);
        // Floats within the range are equal, revert change to the register index
        masm.subq(index, range);
    }

    private boolean constantLengthCompareNeedsTmpArrayPointers() {
        AVXKind.AVXSize vSize = vectorSize;
        if (constantLength() < getElementsPerVector(vectorSize)) {
            vSize = AVXKind.AVXSize.XMM;
        }
        int vectorCount = constantLength() & ~(2 * getElementsPerVector(vSize) - 1);
        return vectorCount > 0;
    }

    /**
     * Emits specialized assembly for checking equality of memory regions
     * {@code arrayPtr1[0..nBytes]} and {@code arrayPtr2[0..nBytes]}. If they match, execution
     * continues directly after the emitted code block, otherwise we jump to {@code noMatch}.
     */
    private void emitConstantLengthArrayCompareBytes(
                    CompilationResultBuilder crb,
                    AMD64MacroAssembler asm,
                    Register[] tmpVectors,
                    Label noMatch) {
        if (constantLength() == 0) {
            // do nothing
            return;
        }
        Register arrayPtr1 = asRegister(array1Value);
        Register arrayPtr2 = asRegister(array2Value);
        Register tmp = asRegister(temp3);
        AVXKind.AVXSize vSize = vectorSize;
        if (constantLength() < getElementsPerVector(vectorSize)) {
            vSize = AVXKind.AVXSize.XMM;
        }
        int elementsPerVector = getElementsPerVector(vSize);
        if (elementsPerVector > constantLength()) {
            assert kind1 == kind2;
            int byteLength = constantLength() << arrayIndexScale1.log2;
            // array is shorter than any vector register, use regular XOR instructions
            int movSize = (byteLength < 2) ? 1 : ((byteLength < 4) ? 2 : ((byteLength < 8) ? 4 : 8));
            emitMovBytes(asm, tmp, new AMD64Address(arrayPtr1, arrayBaseOffset1), movSize);
            emitXorBytes(asm, tmp, new AMD64Address(arrayPtr2, arrayBaseOffset2), movSize);
            asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
            if (byteLength > movSize) {
                emitMovBytes(asm, tmp, new AMD64Address(arrayPtr1, arrayBaseOffset1 + byteLength - movSize), movSize);
                emitXorBytes(asm, tmp, new AMD64Address(arrayPtr2, arrayBaseOffset2 + byteLength - movSize), movSize);
                asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
            }
        } else {
            int elementsPerVectorLoop = 2 * elementsPerVector;
            int tailCount = constantLength() & (elementsPerVectorLoop - 1);
            int vectorCount = constantLength() & ~(elementsPerVectorLoop - 1);
            int bytesPerVector = vSize.getBytes();
            if (vectorCount > 0) {
                Label loopBegin = new Label();
                Register tmpArrayPtr1 = asRegister(temp1);
                Register tmpArrayPtr2 = asRegister(temp2);
                asm.leaq(tmpArrayPtr1, new AMD64Address(arrayPtr1, vectorCount << arrayIndexScale1.log2));
                asm.leaq(tmpArrayPtr2, new AMD64Address(arrayPtr2, vectorCount << arrayIndexScale2.log2));
                arrayPtr1 = tmpArrayPtr1;
                arrayPtr2 = tmpArrayPtr2;
                asm.movq(tmp, -vectorCount);
                asm.align(crb.target.wordSize * 2);
                asm.bind(loopBegin);
                emitVectorLoad1(asm, tmpVectors[0], arrayPtr1, tmp, arrayBaseOffset1, vSize);
                emitVectorLoad2(asm, tmpVectors[1], arrayPtr2, tmp, arrayBaseOffset2, vSize);
                emitVectorLoad1(asm, tmpVectors[2], arrayPtr1, tmp, arrayBaseOffset1 + scaleDisplacement1(bytesPerVector), vSize);
                emitVectorLoad2(asm, tmpVectors[3], arrayPtr2, tmp, arrayBaseOffset2 + scaleDisplacement2(bytesPerVector), vSize);
                emitVectorXor(asm, tmpVectors[0], tmpVectors[1], vSize);
                emitVectorXor(asm, tmpVectors[2], tmpVectors[3], vSize);
                emitVectorTest(asm, tmpVectors[0], vSize);
                asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
                emitVectorTest(asm, tmpVectors[2], vSize);
                asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
                asm.addqAndJcc(tmp, elementsPerVectorLoop, AMD64Assembler.ConditionFlag.NotZero, loopBegin, true);
            }
            if (tailCount > 0) {
                emitVectorLoad1(asm, tmpVectors[0], arrayPtr1, arrayBaseOffset1 + (tailCount << arrayIndexScale1.log2) - scaleDisplacement1(bytesPerVector), vSize);
                emitVectorLoad2(asm, tmpVectors[1], arrayPtr2, arrayBaseOffset2 + (tailCount << arrayIndexScale2.log2) - scaleDisplacement2(bytesPerVector), vSize);
                emitVectorXor(asm, tmpVectors[0], tmpVectors[1], vSize);
                if (tailCount > elementsPerVector) {
                    emitVectorLoad1(asm, tmpVectors[2], arrayPtr1, arrayBaseOffset1, vSize);
                    emitVectorLoad2(asm, tmpVectors[3], arrayPtr2, arrayBaseOffset2, vSize);
                    emitVectorXor(asm, tmpVectors[2], tmpVectors[3], vSize);
                    emitVectorTest(asm, tmpVectors[2], vSize);
                    asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
                }
                emitVectorTest(asm, tmpVectors[0], vSize);
                asm.jccb(AMD64Assembler.ConditionFlag.NotZero, noMatch);
            }
        }
    }

    private void emitMovBytes(AMD64MacroAssembler asm, Register dst, AMD64Address src, int size) {
        switch (size) {
            case 1:
                if (signExtend) {
                    asm.movsbq(dst, src);
                } else {
                    asm.movzbq(dst, src);
                }
                break;
            case 2:
                if (signExtend) {
                    asm.movswq(dst, src);
                } else {
                    asm.movzwq(dst, src);
                }
                break;
            case 4:
                if (signExtend) {
                    asm.movslq(dst, src);
                } else {
                    // there is no movzlq
                    asm.movl(dst, src);
                }
                break;
            case 8:
                asm.movq(dst, src);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private static void emitXorBytes(AMD64MacroAssembler asm, Register dst, AMD64Address src, int size) {
        OperandSize opSize = getOperandSize(size);
        XOR.getRMOpcode(opSize).emit(asm, opSize, dst, src);
    }

    private static OperandSize getOperandSize(int size) {
        switch (size) {
            case 1:
                return OperandSize.BYTE;
            case 2:
                return OperandSize.WORD;
            case 4:
                return OperandSize.DWORD;
            case 8:
                return OperandSize.QWORD;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean needsClearUpperVectorRegisters() {
        return true;
    }
}
