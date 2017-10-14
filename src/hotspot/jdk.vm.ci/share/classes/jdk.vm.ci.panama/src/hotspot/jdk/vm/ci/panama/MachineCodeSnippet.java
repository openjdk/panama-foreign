/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.vm.ci.panama;

import jdk.internal.misc.Unsafe;
import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.panama.amd64.CPUID;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Cleaner;
import java.util.*;

import static jdk.vm.ci.amd64.AMD64.*;

public class MachineCodeSnippet {
    static native void registerNatives();
    static {
        registerNatives();
    }


    private static boolean DEBUG = Boolean.getBoolean("panama.CodeSnippet.DEBUG");
    private static Effect[] DEFAULT_EFFECTS = mask2eff(Integer.getInteger("panama.CodeSnippet.DEFAULT_EFFECTS",
                                                                          eff2mask(Effect.READ_MEMORY, Effect.WRITE_MEMORY)));
    static {
        if (DEBUG) {
            System.out.printf("CodeSnippet.DEFAULT_EFFECTS: %s\n", Arrays.deepToString(DEFAULT_EFFECTS));
        }
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @FunctionalInterface
    public interface SnippetGenerator {
        int[] getCode(Register... regs);
    }

    public enum Effect {
        READ_MEMORY,
        WRITE_MEMORY,
        CONTROL;

        public static Effect[] effects(Effect... effects) {
            return effects;
        }
    }

    // FIXME: check consistency with JVM
    static int eff2mask(Effect... effects) {
        int result = 0;
        for (Effect eff : effects) {
            result |= (1 << eff.ordinal());
        }
        return result;
    }

    static Effect[] mask2eff(int mask) {
        Set<Effect> effects = new HashSet<>();
        int idx = 0;
        while (mask != 0) {
            if ((mask & 1) != 0) {
                effects.add(Effect.values()[idx]);
            }
            mask >>>= 1;
            idx++;
        }
        return effects.toArray(new Effect[effects.size()]);
    }

    // FIXME: x86-64 for now. Should be factored into platform-dependent code.
    public static boolean requires(AMD64.CPUFeature... features) {
        for (AMD64.CPUFeature f : features) {
            if (!CPUID.has(f)) {
                return false;
            }
        }
        return true;
    }

    interface Location {}

    enum TYPE {
        //I8(1), I16(1),
        I32(1) /*RegI*/, I64(2) /*RegL*/,
        F32(1) /*RegF*/, F64(2) /*RegD*/,
        PTR(Unsafe.ADDRESS_SIZE / 4) /*RegP*/,
        // RegN(1),
        // VecS(1), VecD(2)
        V128(4) /*VecX*/, V256(8)/*VecY*/, V512(16) /*VecZ*/,
        SPECIAL(1);

        public int size; // size in 32-bit slots
        TYPE(int size) {
            this.size = size;
        }
    }

    static class RegisterLocation implements Location {
        public final Register register;
        public final TYPE type;

        RegisterLocation(Register register, TYPE type) {
            this.register = register;
            this.type = type;
        }

        public RegisterMask toRegisterMask() {
            return new RegisterMask(type, register);
        }
    }

    static class RegisterMask {
        public final Set<Register> registers;
        public final TYPE type;

        RegisterMask(TYPE type, Register... rs) {
            this(type, List.of(rs));
        }

        RegisterMask(TYPE type, Collection<Register> rs) {
            this.registers = Collections.unmodifiableSet(new HashSet<>(rs));
            this.type = type;
        }

        public static RegisterMask make(Class<?> holder, Register... rs) {
            TYPE t;
            if (holder.isPrimitive()) {
                if (holder == float.class) {
                    t = TYPE.F32;
                } else if (holder == double.class) {
                    t = TYPE.F64;
                } else if (holder == long.class) {
                    t = TYPE.I64;
                } else {
                    // boolean, byte, short, char, int
                    t = TYPE.I32;
                }
            } else {
                if (holder == Long2.class) {
                    t = TYPE.V128;
                } else if (holder == Long4.class) {
                    t = TYPE.V256;
                } else if (holder == Long8.class) {
                    t = TYPE.V512;
                } else {
                    t = TYPE.PTR;
                }
            }
            return new RegisterMask(t, rs);
        }

        public String toString() {
            return String.format("RM{%s}%s", registers.toString(), type);
        }

        private static TYPE checkTypes(RegisterMask rm1, RegisterMask rm2) {
            if (rm1.type != rm2.type) {
                throw new IllegalArgumentException(rm1.type + " != " + rm2.type);
            }
            return rm1.type;
        }

        public static RegisterMask union(RegisterMask rm1, RegisterMask rm2) {
            TYPE t = checkTypes(rm1, rm2);
            Set<Register> union = new HashSet<>(rm1.registers);
            union.addAll(rm2.registers);
            return new RegisterMask(t, union);
        }

        public static RegisterMask intersect(RegisterMask rm1, RegisterMask rm2) {
            TYPE t = checkTypes(rm1, rm2);
            Set<Register> intersect = new HashSet<>();
            for (Register r : rm1.registers) {
                if (rm2.registers.contains(r)) {
                    intersect.add(r);
                }
            }
            return new RegisterMask(t, intersect);
        }

        int[] toRawMask() {
            int[] result = new int[2*registers.size()];
            int pos = 0;
            for (Register r : registers) {
                result[pos++] = reg2opto[r.number];
                result[pos++] = type.size;
            }
            return result;
        }
    }

    public static class Builder {
        private final String name;

        private MethodType mt;
        private MethodType tempMT = MethodType.methodType(void.class);

        private Effect[] effects;
        private List<RegisterMask> argumentRegMasks = new ArrayList<>(); // (args)ret: (reg,size)*
        private RegisterMask returnRegMask;
        private List<RegisterMask> killedRegMask = new ArrayList<>(); // (reg,size)*
        private AMD64.CPUFeature[] features;
        private SnippetGenerator generator;
        private int[] code;

        private Builder(String name) {
            this.name = name;
        }

        public Builder type(MethodType mt) {
            this.mt = mt;
            return this;
        }
        public Builder effects(Effect... effects) {
            this.effects = effects;
            return this;
        }

        public Builder argument(Class<?> argClass, Register... registers) {
            if (mt != null) {
                int pos = argumentRegMasks.size();
                if (mt.parameterType(pos) != argClass) {
                    String errMsg = String.format("Snippet type doesn't agree with argument info: mt=%s; #%d; arg=%s",
                                                  mt, pos, argClass);
                    throw new IllegalArgumentException(errMsg);
                }
            } else {
                tempMT = tempMT.appendParameterTypes(argClass);
            }
            RegisterMask rm = RegisterMask.make(argClass, registers);
            argumentRegMasks.add(rm);
            return this;
        }

        public Builder returns(Class<?> retClass, Register... registers) {
            if (returnRegMask != null) {
                throw new IllegalStateException("return value is already specified");
            }
            if (mt != null) {
                if (mt.returnType() != retClass) {
                    String errMsg = String.format("Snippet type doesn't agree with return value info: mt=%s; return=%s",
                            mt, retClass);
                    throw new IllegalArgumentException(errMsg);
                }
            } else {
                tempMT = tempMT.changeReturnType(retClass);
            }
            if (retClass == void.class) {
                if (registers.length > 0) {
                    throw new IllegalArgumentException("register mask is not allowed for void return");
                }
            }
            returnRegMask = RegisterMask.make(retClass, registers);
            return this;
        }

        public Builder kills(Register... registers) {
            for (Register r : registers) {
                RegisterMask rm;
                if (r.getRegisterCategory() == XMM) {
                    rm = new RegisterMask(TYPE.V256, r);
                } else if (r.getRegisterCategory() == Register.SPECIAL) {
                    rm = new RegisterMask(TYPE.SPECIAL, r);
                } else {
                    rm = RegisterMask.make(long.class, r);
                }
                killedRegMask.add(rm);
            }
            return this;
        }

        public Builder kills(RegisterMask... registers) {
            for (RegisterMask rm : registers) {
                killedRegMask.add(rm);
            }
            return this;
        }

        public Builder code(int... code) {
            if (generator != null) {
                throw new IllegalStateException("generator is already present");
            }
            this.code = code;
            return this;
        }

        public Builder code(SnippetGenerator g) {
            if (code != null) {
                throw new IllegalStateException("code is already present");
            }
            this.generator = g;
            return this;
        }

        // FIXME: x86-64 for now. Should be factored into platform-dependent code.
        public Builder requires(AMD64.CPUFeature... features) {
            this.features = features.clone();
            return this;
        }

        private SnippetGenerator wrapCode() {
            Objects.requireNonNull(name, "snippet name is missing");
            Objects.requireNonNull(mt,   "snippet type is missing");
            Objects.requireNonNull(code, "snippet code is missing");

            Register[] mtRegs = argRegisters(mt);
            if (argumentRegMasks.size() > 0) {
                if (mt.parameterCount() != argumentRegMasks.size()) {
                    throw new IllegalStateException("mismatch between type and argument reg masks");
                }
                for (int i = 0; i < argumentRegMasks.size(); i++) {
                    RegisterMask rm = argumentRegMasks.get(i);
                    if (rm.registers.size() != 1 || !rm.registers.contains(mtRegs[i+1])) {
                        String errMsg = String.format("mismatch: arg#%d: mt=%s %s != %s", i, mt, mtRegs[i+1], rm);
                        throw new IllegalStateException(errMsg);
                    }
                }

            }
            if (returnRegMask == null && mt.returnType() != void.class) {
                String errMsg = String.format("mismatch: return value: mt=%s; rm=%s", mt, returnRegMask);
                throw new IllegalStateException(errMsg);
            }
            if (returnRegMask != null && returnRegMask.registers.size() != 1 || !returnRegMask.registers.contains(mtRegs[0])) {
                String errMsg = String.format("mismatch: return value: mt=%s; rm=%s", mt, returnRegMask);
                throw new IllegalStateException(errMsg);
            }
            return (Register[] regs) -> {
                assert Arrays.deepEquals(regs, argRegisters(mt))
                        : name + mt + ": " + Arrays.deepToString(regs) + " != " + Arrays.deepToString(argRegisters(mt));
                return code;
            };
        }

        public MethodHandle make() {
            Objects.requireNonNull(name, "snippet name is missing");

            boolean isSupported = true;
            if (features != null) {
                isSupported = MachineCodeSnippet.requires(features);
            }
            if (effects == null) {
                effects = DEFAULT_EFFECTS;
            }
            if (mt == null) {
                mt = tempMT;
            } else {
                if (!Objects.equals(mt, tempMT)) {
                    throw new IllegalStateException("mismatched types: " + mt + " vs " + tempMT);
                }
            }
            if (code != null) {
                if (generator == null) {
                    generator = wrapCode();
                }
            } else {
                Objects.requireNonNull(generator, "snippet generator is missing");
                code = generator.getCode(argRegisters(mt));
            }

            Objects.requireNonNull(code, "snippet code is missing");
            Objects.requireNonNull(generator, "snippet generator is missing");

            int[][] rawRMs = toRawRegMasks(argumentRegMasks, returnRegMask);
            int[] killedRegs = computeKilledRegs(killedRegMask);
            int effMask = eff2mask(effects);

            return makeWithGenerator(name, mt, isSupported, code, rawRMs, killedRegs, generator, effMask);
        }

        private int[] computeKilledRegs(List<RegisterMask> rms) {
            ArrayList<Integer> interim = new ArrayList<>();
            for (RegisterMask rm : rms) {
                int[] a = rm.toRawMask();
                for (int i : a) {
                    interim.add(i);
                }
            }
            int[] result = new int[interim.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = interim.get(i);
            }
            return result;
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    // FIXME: x86-64 for now. Should be factored into platform-dependent code.
    static final Register[]    intArgs = new Register[] { rdi, rsi, rdx, rcx, r8, r9 };
    static final Register[]  floatArgs = new Register[] { xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7 };
    static final Register[] vectorArgs = xmmRegistersSSE;

    static final Register    intReturn = rax;
    static final Register  floatReturn = xmm0;
    static final Register vectorReturn = xmm0;

    static final Register[] calleeSavedGPRegs = new Register[] { rax, rcx, rdx, rsi, rdi, r8, r9, r10, r11 };
    static final Register[] calleeSavedFPRegs = new Register[] { xmm0, xmm1, xmm2, xmm3, xmm4, xmm5, xmm6, xmm7, xmm8,
                                                                 xmm9, xmm10, xmm11, xmm12, xmm13, xmm14, xmm15 };

    static Register[] argRegisters(MethodType mt) {
        Register[] result = new Register[mt.parameterCount()+1];
        int   intCount = isVectorType(mt.returnType()) ? 1 : 0; // box for the result is passed as 1st arg
        int   floatCnt = 0;
        for (int i = 0; i < mt.parameterCount(); i++) {
            Class<?> cls = mt.parameterType(i);
            if (isIntArg(cls)) {
                result[i+1] = intArgs[intCount++];
            } else if (isFloatArg(cls)) {
                result[i+1] = floatArgs[floatCnt++];
            } else if (isVectorType(cls)) {
                result[i+1] = vectorArgs[floatCnt++];
                intCount++; // for stand-alone version box is passed
            } else {
                throw new Error("Unknown arg type: "+cls.getName());
            }
        }
        Class<?> retCls = mt.returnType();
        if (retCls == void.class) {
            result[0] = null;
        } else if (isIntArg(retCls)) {
            result[0] = intReturn;
        } else if (isFloatArg(retCls)) {
            result[0] = floatReturn;
        } else if (isVectorType(retCls)) {
            result[0] = vectorReturn;
        } else {
            throw new Error("Unknown ret type: "+retCls.getName());
        }
        return result;
    }

    static boolean isIntArg(Class<?> c) {
        if (isFloatArg(c))   return false;
        if (isVectorType(c))  return false;
        //if (c == void.class) return false;
        return true;
    }

    static boolean isFloatArg(Class<?> c) {
        if (c == float.class || c == double.class) {
            return true;
        }
        return false;
    }

    private static boolean isVectorType(Class<?> cls) {
        if (cls == Long2.class) return true;
        if (cls == Long4.class) return true;
        if (cls == Long8.class) return true;
        return false;
    }

    private static int[][] toRawRegMasks(List<RegisterMask> argRMs, RegisterMask retRM) {
        boolean hasReturn = (retRM != null);
        int size = argRMs.size() + 1;
        int[][] result = new int[size][0];
        if (hasReturn) {
            result[0] = retRM.toRawMask();
        }
        for (int i = 0; i <  argRMs.size(); i++) {
            result[i+1] = argRMs.get(i).toRawMask();
        }
        return result;
    }

    private static int[][] toRawRegMasks(Register[][] regMasks, MethodType mt) {
        boolean hasReturn = (mt.returnType() != void.class);
        int size = mt.parameterCount() + 1;
        int[][] result = new int[size][0];
        if (hasReturn) {
            result[0] = computeRegMask(regMasks[0], mt.returnType());
        }
        for (int i = 1; i < size; i++) {
            result[i] = computeRegMask(regMasks[i], mt.parameterType(i-1));
        }
        return result;
    }

    private static int[] computeRegMask(Register[] regMask, Class<?> type) {
        ArrayList<Integer> ranges = new ArrayList<>();
        for (Register r : regMask) {
            ranges.add(reg2opto[r.number]);
            ranges.add(regSize(r, type));
        }
        int[] result = new int[ranges.size()];
        for (int i = 0; i < ranges.size(); i++) {
            result[i] = ranges.get(i);
        }
        return result;
    }

    private static int[] computeRegMask(Register[] regMask, int[] sizes) {
        assert(regMask.length == sizes.length);
        int[] result = new int[2*regMask.length];
        for (int i = 0; i < regMask.length; i++) {
            Register r = regMask[i];
            result[2*i]   = reg2opto[r.number];
            result[2*i+1] = sizes[i];
        }
        return result;
    }

    // Compute size in 32-bit slots
    private static int regSize(Register r, Class<?> type) {
        if (type.isPrimitive()) {
            if (type == long.class)   return 2;
            if (type == double.class) return 2;
            return 1; // boolean, byte, short, char, int, float
        } else {
            if (type == Long2.class) return 4;  //  4*32 = 128-bit
            if (type == Long4.class) return 8;  //  8*32 = 256-bit
            if (type == Long8.class) return 16; // 16*32 = 512-bit
            return 2; // 2 * 32-bit slots by default FIXME: assumes 64-bit arch
        }
    }

    // FIXME: dynamically determine the number of registers
    static int[]      reg2opto = new int[AMD64.allRegisters.size()];
    static Register[] opto2reg = new Register[1000];
    static {
        if (DEBUG) {
            printRegisters();
        }
        for (Register r : AMD64.allRegisters) {
            int idx = reg2opto(r.name.toUpperCase());
            if (idx >= 0) {
                opto2reg[idx] = r;
                reg2opto[r.number] = idx;
            } else {
                if (DEBUG) {
                    System.err.println("Snippets: " + r.name + " register not found.");
                }
            }
        }
    }

    static final int END_MARKER = -2;

    private static Register[] decode(int[] regs) {
        int len = 0;
        // Compute length
        for (; (len < regs.length) && (regs[len] != END_MARKER); len++);
        Register[] res = new Register[len];
        for (int i = 0; i < len; i++) {
            int idx = regs[i];
            if (idx >= 0) {
                res[i] = opto2reg[regs[i]];
            }
        }
        return res;
    }

    private static byte[] toByteArray(int[] arr) {
        byte[] res = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            int v = arr[i];
            if ((v & 0xFF) != v) {
                String msg = String.format("Not a byte (%d) at %d in %s", v, i, Arrays.toString(arr));
                throw new Error(msg);
            }
            res[i] = (byte)v;
        }
        return res;
    }

    private static Register[][] toRegMasks(Register[] regs) {
        Register[][] registerMasks = new Register[regs.length][0];
        for (int i = 0; i < regs.length; i++) {
            if (regs[i] != null) {
                registerMasks[i] = new Register[] { regs[i] }; // mask contains a single register
            }
        }
        return registerMasks;
    }

    private static MethodHandle makeWithGenerator(String name, MethodType mt, boolean isSupported, int[] code,
                                                 int[][] regMasks, int[] killedRegMask, Object generator, int effMask) {
        byte[] snippetCode = new byte[code.length];
        for (int i = 0; i < code.length; i++) {
            snippetCode[i] = (byte) code[i];
        }
        return makeWithGenerator(name, mt, isSupported, snippetCode, regMasks, killedRegMask, generator, effMask);
    }


    private static final Cleaner CLEANER = Cleaner.create();

    private static MethodHandle makeWithGenerator(String name, MethodType mt, boolean isSupported, byte[] code,
                                                 int[][] regMasks, int[] killedRegMask, Object generator, int effMask) {
        MethodHandle mh = null;
        if (isSupported) {
            printCode(name, mt, code);
            boolean vectorReturn = isVectorType(mt.returnType());

            MethodType rawMT = mt;
            if (vectorReturn) {
                rawMT = mt.insertParameterTypes(0, Object.class);
            }
            long addr = install(name, code, rawMT);
            if (DEBUG) {
                System.out.printf("%s: Address: 0x%x\n", name, addr);
            }
            try {
                mh = LOOKUP.makeSnippet(name, addr, rawMT, regMasks, killedRegMask, generator, effMask);

                if (vectorReturn) {
                    // findConstructor() + collectArguments():
                    //   T snippet(Object, A...);
                    //   Long* makeLong*(); // findConstructor
                    //   T adapter(A... a) {
                    //     Long* b = makeLong*();
                    //     return target(b,a...);
                    //   }
                    try {
                        Class<?> vectorClass = mt.returnType();
                        MethodHandle ctor = LOOKUP.findStatic(vectorClass, "make", MethodType.methodType(vectorClass))
                                .asType(MethodType.methodType(Object.class));
                        mh = MethodHandles.collectArguments(mh, 0, ctor);
                    } catch (IllegalAccessException | NoSuchMethodException e) {
                        throw new Error(e);
                    }
                }
            } finally {
                if (mh != null) {
                    CLEANER.register(mh, () -> freeStub(addr));
                } else {
                    freeStub(addr);
                }
            }
        } else {
            mh = MethodHandles
                    .throwException(mt.returnType(), UnsupportedOperationException.class)
                    .bindTo(new UnsupportedOperationException("Not supported code snippet: "+name));
            mh = MethodHandles.dropArguments(mh, 0, mt.parameterArray());
        }
        assert mh.type() == mt : "expected: "+mt+"; actual: "+mh.type();
        return mh;
    }

    /* ============================================================================= */

    public static MethodHandle make(String name, MethodType mt, boolean isSupported, int... code) {
        return make(name, mt, DEFAULT_EFFECTS, isSupported, code);
    }

    public static MethodHandle make(String name, MethodType mt, Effect[] effect, boolean isSupported, int... code) {
        // Assume same calling conventions as for stand-alone version.
        Register[] stdRegs = argRegisters(mt);
        Register[][] regMasks = toRegMasks(stdRegs);
        SnippetGenerator g = (Register[] regs) -> {
            assert Arrays.deepEquals(regs, stdRegs)
                   : name + mt + ": " + Arrays.deepToString(regs) + " != " + Arrays.deepToString(stdRegs);
            return code;
        };
        return make(name, mt, effect, isSupported, regMasks, g);
    }

    public static MethodHandle make(String name, MethodType mt, boolean isSupported,
                                    Register[][] regMasks, SnippetGenerator codeProducer) {
        return make(name, mt, DEFAULT_EFFECTS, isSupported, regMasks, codeProducer);
    }

    public static MethodHandle make(String name,
                                    MethodType mt,
                                    Effect[] effect,
                                    boolean isSupported,
                                    Register[][] regMasks,
                                    SnippetGenerator codeProducer) {
        int[] code = codeProducer.getCode(argRegisters(mt));
        int[][] rawRegMasks = toRawRegMasks(regMasks, mt);

        // FIXME: x86-64
        int[] killedRegMaskGP = computeRegMask(calleeSavedGPRegs, long.class);
        int[] killedRegMaskFP = computeRegMask(calleeSavedFPRegs, Long4.class);
        int[] killedRegMask = new int[killedRegMaskGP.length + killedRegMaskFP.length];
        System.arraycopy(killedRegMaskGP, 0, killedRegMask,                      0, killedRegMaskGP.length);
        System.arraycopy(killedRegMaskFP, 0, killedRegMask, killedRegMaskGP.length, killedRegMaskFP.length);

        int effMask = eff2mask(effect);
        return makeWithGenerator(name, mt, isSupported, code, rawRegMasks, killedRegMask, codeProducer, effMask);
    }

    // FIXME
    public static MethodHandle make(String name,
                                    MethodType mt,
                                    Effect[] effect,
                                    boolean isSupported,
                                    Register[][] regMasks,
                                    Register[] killedRegs, int[] killedRegSize, // FIXME
                                    SnippetGenerator codeProducer) {
        int[] code = codeProducer.getCode(argRegisters(mt));
        int[][] rawRegMasks = toRawRegMasks(regMasks, mt);
        int[] killedRegMask = computeRegMask(killedRegs, killedRegSize);
        int effMask = eff2mask(effect);
        return makeWithGenerator(name, mt, isSupported, code, rawRegMasks, killedRegMask, codeProducer, effMask);
    }

    static String codeString(byte[] code) {
        StringBuilder sb = new StringBuilder("{");
        for (byte b : code) {
            sb.append(String.format(" %02x", b));
        }
        sb.append("}");
        return sb.toString();
    }

    /* ============================================================================= */

    static class CodeSnippetHandler extends Thread {
        @Override
        public void run() {
            int[] regs = new int[16];
            Object[] info = new Object[2];
            while (true) {
                byte[] code = null;
                try {
                    Arrays.fill(regs, END_MARKER);
                    Object g = processTask(regs, info);
                    if (g != null) {
                        try {
                            Register[] decodedRegs = decode(regs);
                            if (DEBUG) {
                                System.out.println("CodeSnippetHandler: " + ((String)info[0]));
                                System.out.println("\ttype: " + ((MethodType)info[1]));
                                System.out.println("\ttask: " + g);
                                System.out.printf( "\tregs: %s -> %s\n", Arrays.toString(regs), Arrays.toString(decodedRegs));
                            }
                            int[] res = ((SnippetGenerator)g).getCode(decodedRegs);
                            code = toByteArray(res);

                            if (DEBUG) {
                                System.out.println("\tcode: " + codeString(code));
                            }
                            printCode("CodeSnippetHandler: code", (MethodType)info[1], code);
                        } catch(Throwable e) {
                            e.printStackTrace(); // FIXME
                        } finally {
                            finishTask(code);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch(Throwable e) {
                    e.printStackTrace(); // FIXME
                }
            }
        }
    }

    private static native long install(String name, byte[] snippet, MethodType mt);
    private static native void freeStub(long addr);

    private static native Object processTask(int[] allocatedRegs, Object[] mt);
    private static native boolean finishTask(byte[] code);
    private static native int reg2opto(String name);
    private static native void printRegisters();

    static {
        Thread t = new CodeSnippetHandler();
        t.setName("Patchable Code Snippet Processor");
        t.setDaemon(true);
        t.start();
    }

    private static void printCode(String name, MethodType mt, byte[] code) {
        if (DEBUG) {
            System.out.printf("%s (MT%s) Code (%d) { ", name, mt, code.length);
            for (int pos = 0; pos < code.length; pos++) {
                System.out.printf("%02x ", code[pos]);
            }
            System.out.println("}");
        }
    }
}
