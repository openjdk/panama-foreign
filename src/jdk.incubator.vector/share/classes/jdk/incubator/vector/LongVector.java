/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package jdk.incubator.vector;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.concurrent.ThreadLocalRandom;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import static jdk.incubator.vector.VectorIntrinsics.*;
import static jdk.incubator.vector.VectorOperators.*;

// -- This file was mechanically generated: Do not edit! -- //

/**
 * A specialized {@link Vector} representing an ordered immutable sequence of
 * {@code long} values.
 */
@SuppressWarnings("cast")  // warning: redundant cast
public abstract class LongVector extends AbstractVector<Long> {

    LongVector() {}

    static final int FORBID_OPCODE_KIND = VO_ONLYFP;

    @ForceInline
    static final int opCode(Operator op) {
        return VectorOperators.opCode(op, VO_OPCODE_VALID, FORBID_OPCODE_KIND);
    }
    @ForceInline
    static final int opCode(Operator op, int requireKind) {
        requireKind |= VO_OPCODE_VALID;
        return VectorOperators.opCode(op, requireKind, FORBID_OPCODE_KIND);
    }
    @ForceInline
    static final boolean opKind(Operator op, int bit) {
        return VectorOperators.opKind(op, bit);
    }

    // Virtualized factories and operators,
    // coded with portable definitions.
    // These are all @ForceInline in case
    // they need to be used performantly.
    // The various shape-specific subclasses
    // also specialize them by wrapping
    // them in a call like this:
    //    return (Byte128Vector)
    //       super.bOp((Byte128Vector) o);
    // The purpose of that is to forcibly inline
    // the generic definition from this file
    // into a sharply type- and size-specific
    // wrapper in the subclass file, so that
    // the JIT can specialize the code.
    // The code is only inlined and expanded
    // if it gets hot.  Think of it as a cheap
    // and lazy version of C++ templates.

    // Virtualized getter

    /*package-private*/
    abstract long[] getElements();

    // Virtualized constructors

    /**
     * Build a vector directly using my own constructor.
     * It is an error if the array is aliased elsewhere.
     */
    /*package-private*/
    abstract LongVector vectorFactory(long[] vec);

    /**
     * Build a mask directly using my species.
     * It is an error if the array is aliased elsewhere.
     */
    /*package-private*/
    @ForceInline
    AbstractMask<Long> maskFactory(boolean[] bits) {
        return vspecies().maskFactory(bits);
    }

    // Constant loader (takes dummy as vector arg)
    interface FVOp {
        long apply(int i);
    }

    /*package-private*/
    @ForceInline
    LongVector vOp(FVOp f) {
        long[] res = new long[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i);
        }
        return vectorFactory(res);
    }

    @ForceInline
    LongVector vOp(VectorMask<Long> m, FVOp f) {
        long[] res = new long[length()];
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            if (mbits[i]) {
                res[i] = f.apply(i);
            }
        }
        return vectorFactory(res);
    }

    // Unary operator

    /*package-private*/
    interface FUnOp {
        long apply(int i, long a);
    }

    /*package-private*/
    @ForceInline
    LongVector uOp(FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    LongVector uOp(VectorMask<Long> m,
                             FUnOp f) {
        long[] vec = getElements();
        long[] res = new long[length()];
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return vectorFactory(res);
    }

    // Binary operator

    /*package-private*/
    interface FBinOp {
        long apply(int i, long a, long b);
    }

    /*package-private*/
    @ForceInline
    LongVector bOp(Vector<Long> o,
                             FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((LongVector)o).getElements();
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    LongVector bOp(Vector<Long> o,
                             VectorMask<Long> m,
                             FBinOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((LongVector)o).getElements();
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return vectorFactory(res);
    }

    // Ternary operator

    /*package-private*/
    interface FTriOp {
        long apply(int i, long a, long b, long c);
    }

    /*package-private*/
    @ForceInline
    LongVector tOp(Vector<Long> o1,
                             Vector<Long> o2,
                             FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((LongVector)o1).getElements();
        long[] vec3 = ((LongVector)o2).getElements();
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    LongVector tOp(Vector<Long> o1,
                             Vector<Long> o2,
                             VectorMask<Long> m,
                             FTriOp f) {
        long[] res = new long[length()];
        long[] vec1 = this.getElements();
        long[] vec2 = ((LongVector)o1).getElements();
        long[] vec3 = ((LongVector)o2).getElements();
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return vectorFactory(res);
    }

    // Reduction operator

    /*package-private*/
    @ForceInline
    long rOp(long v, FBinOp f) {
        long[] vec = getElements();
        for (int i = 0; i < vec.length; i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Memory reference

    /*package-private*/
    interface FLdOp<M> {
        long apply(M memory, int offset, int i);
    }

    /*package-private*/
    @ForceInline
    <M> LongVector ldOp(M memory, int offset,
                                  FLdOp<M> f) {
        //dummy; no vec = getElements();
        long[] res = new long[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(memory, offset, i);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    <M> LongVector ldOp(M memory, int offset,
                                  VectorMask<Long> m,
                                  FLdOp<M> f) {
        //long[] vec = getElements();
        long[] res = new long[length()];
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            if (mbits[i]) {
                res[i] = f.apply(memory, offset, i);
            }
        }
        return vectorFactory(res);
    }

    interface FStOp<M> {
        void apply(M memory, int offset, int i, long a);
    }

    /*package-private*/
    @ForceInline
    <M> void stOp(M memory, int offset,
                  FStOp<M> f) {
        long[] vec = getElements();
        for (int i = 0; i < vec.length; i++) {
            f.apply(memory, offset, i, vec[i]);
        }
    }

    /*package-private*/
    @ForceInline
    <M> void stOp(M memory, int offset,
                  VectorMask<Long> m,
                  FStOp<M> f) {
        long[] vec = getElements();
        boolean[] mbits = ((AbstractMask<Long>)m).getBits();
        for (int i = 0; i < vec.length; i++) {
            if (mbits[i]) {
                f.apply(memory, offset, i, vec[i]);
            }
        }
    }

    // Binary test

    /*package-private*/
    interface FBinTest {
        boolean apply(int cond, int i, long a, long b);
    }

    /*package-private*/
    @ForceInline
    AbstractMask<Long> bTest(int cond,
                                  Vector<Long> o,
                                  FBinTest f) {
        long[] vec1 = getElements();
        long[] vec2 = ((LongVector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(cond, i, vec1[i], vec2[i]);
        }
        return maskFactory(bits);
    }

    /*package-private*/
    @ForceInline
    static boolean doBinTest(int cond, long a, long b) {
        switch (cond) {
        case BT_eq:  return a == b;
        case BT_ne:  return a != b;
        case BT_lt:  return a < b;
        case BT_le:  return a <= b;
        case BT_gt:  return a > b;
        case BT_ge:  return a >= b;
        }
        throw new AssertionError(Integer.toHexString(cond));
    }

    /*package-private*/
    @ForceInline
    @Override
    abstract LongSpecies vspecies();

    /*package-private*/
    @ForceInline
    static long toBits(long e) {
        return  e;
    }

    /*package-private*/
    @ForceInline
    static long fromBits(long bits) {
        return ((long)bits);
    }

    // Static factories (other than memory operations)

    // Note: A surprising behavior in javadoc
    // sometimes makes a lone /** {@inheritDoc} */
    // comment drop the method altogether,
    // apparently if the method mentions an
    // parameter or return type of Vector<Long>
    // instead of Vector<E> as originally specified.
    // Adding an empty HTML fragment appears to
    // nudge javadoc into providing the desired
    // inherited documentation.  We use the HTML
    // comment <!--workaround--> for this.

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public static LongVector zero(VectorSpecies<Long> species) {
        LongSpecies vsp = (LongSpecies) species;
        return vsp.zero();
    }

    /**
     * Returns a vector of the same species as this one
     * where all lane elements are set to
     * the primitive value {@code e}.
     *
     * @param e the value to broadcast
     * @return a vector where all lane elements are set to
     *         the primitive value {@code e}
     * @see #broadcast(VectorSpecies,long)
     * @see Vector#broadcast(long)
     * @see VectorSpecies#broadcast(long)
     */
    public abstract LongVector broadcast(long e);

    /**
     * Returns a vector of the given species
     * where all lane elements are set to
     * the primitive value {@code e}.
     *
     * @param species species of the desired vector
     * @param e the value to broadcast
     * @return a vector where all lane elements are set to
     *         the primitive value {@code e}
     * @see #broadcast(long)
     * @see Vector#broadcast(long)
     * @see VectorSpecies#broadcast(long)
     */
    public static LongVector broadcast(VectorSpecies<Long> species, long e) {
        LongSpecies vsp = (LongSpecies) species;
        return vsp.broadcast(e);
    }

    /*package-private*/
    @ForceInline
    final LongVector broadcastTemplate(long e) {
        LongSpecies vsp = vspecies();
        return vsp.broadcast(e);
    }


    /**
     * Returns a vector where each lane element is set to given
     * primitive values.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * the primitive value at index {@code N} is placed into the resulting
     * vector at lane index {@code N}.
     *
     * @param species species of the desired vector
     * @param es the given primitive values
     * @return a vector where each lane element is set to given primitive
     * values
     * @throws IllegalArgumentException
     *         if {@code es.length != species.length()}
     */
    @ForceInline
    @SuppressWarnings("unchecked")
    public static LongVector fromValues(VectorSpecies<Long> species, long... es) {
        LongSpecies vsp = (LongSpecies) species;
        int vlength = vsp.laneCount();
        VectorIntrinsics.requireLength(es.length, vlength);
        // Get an unaliased copy and use it directly:
        return vsp.vectorFactory(Arrays.copyOf(es, vlength));
    }

    /**
     * Returns a vector where the first lane element is set to the primtive
     * value {@code e}, all other lane elements are set to the default
     * value.
     *
     * @param species species of the desired vector
     * @param e the value
     * @return a vector where the first lane element is set to the primitive
     * value {@code e}
     */
    // FIXME: Does this carry its weight?
    @ForceInline
    public static final LongVector single(VectorSpecies<Long> species, long e) {
        return zero(species).withLane(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextLong()}
     * for each lane, from first to last.
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static LongVector random(VectorSpecies<Long> species) {
        LongSpecies vsp = (LongSpecies) species;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return vsp.vOp(i -> nextRandom(r));
    }
    private static long nextRandom(ThreadLocalRandom r) {
        return r.nextLong();
    }

    // Unary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public abstract LongVector lanewise(VectorOperators.Unary op);

    @ForceInline
    final
    LongVector lanewiseTemplate(VectorOperators.Unary op) {
        if (opKind(op, VO_SPECIAL)) {
            if (op == ZOMO) {
                return blend(broadcast(-1), compare(NE, 0));
            }
            if (op == NEG) {
                // FIXME: Support this in the JIT.
                return broadcast(0).lanewiseTemplate(SUB, this);
            }
        }
        int opc = opCode(op);
        return VectorIntrinsics.unaryOp(
            opc, getClass(), long.class, length(),
            this,
            UN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_NEG: return v0 ->
                        v0.uOp((i, a) -> (long) -a);
                case VECTOR_OP_ABS: return v0 ->
                        v0.uOp((i, a) -> (long) Math.abs(a));
                case VECTOR_OP_NOT: return v0 ->
                        v0.uOp((i, a) -> (long) ~a);
                default: return null;
              }}));
    }
    private static final
    ImplCache<Unary,UnaryOperator<LongVector>> UN_IMPL
        = new ImplCache<>(Unary.class, LongVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public LongVector lanewise(VectorOperators.Unary op,
                                         VectorMask<Long> m) {
        return blend(lanewise(op), m);
    }

    // Binary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Binary,long)
     */
    @Override
    public abstract LongVector lanewise(VectorOperators.Binary op,
                                                  Vector<Long> v);
    @ForceInline
    final
    LongVector lanewiseTemplate(VectorOperators.Binary op,
                                          Vector<Long> v) {
        LongVector that = (LongVector) v;
        that.check(this);
        if (opKind(op, VO_SPECIAL )) {
            if (op == FIRST_NONZERO) {
                // FIXME: Support this in the JIT.
                VectorMask<Long> thisNZ
                    = this.viewAsIntegralLanes().compare(NE, (long) 0);
                that = that.blend((long) 0, thisNZ.cast(vspecies()));
                op = OR_UNCHECKED;
            }
            if (op == ROR || op == ROL) {  // FIXME: JIT should do this
                LongVector neg = that.lanewise(NEG);
                LongVector hi = this.lanewise(LSHL, (op == ROR) ? neg : that);
                LongVector lo = this.lanewise(LSHR, (op == ROR) ? that : neg);
                return hi.lanewise(OR, lo);
            } else if (op == ANDC2) {
                // FIXME: Support this in the JIT.
                that = that.lanewise(NOT);
                op = AND;
            } else if (op == DIV) {
                VectorMask<Long> eqz = that.eq((long)0);
                if (eqz.anyTrue()) {
                    throw that.divZeroException();
                }
            }
        }
        int opc = opCode(op);
        return VectorIntrinsics.binaryOp(
            opc, getClass(), long.class, length(),
            this, that,
            BIN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_ADD: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a + b));
                case VECTOR_OP_SUB: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a - b));
                case VECTOR_OP_MUL: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a * b));
                case VECTOR_OP_DIV: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a / b));
                case VECTOR_OP_MAX: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)Math.max(a, b));
                case VECTOR_OP_MIN: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)Math.min(a, b));
                case VECTOR_OP_FIRST_NONZERO: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> toBits(a) != 0 ? a : b);
                case VECTOR_OP_AND: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a & b));
                case VECTOR_OP_OR: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a | b));
                case VECTOR_OP_ANDC2: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a & ~b));
                case VECTOR_OP_XOR: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (long)(a ^ b));
                case VECTOR_OP_LSHIFT: return (v0, v1) ->
                        v0.bOp(v1, (i, a, n) -> (long)(a << n));
                case VECTOR_OP_RSHIFT: return (v0, v1) ->
                        v0.bOp(v1, (i, a, n) -> (long)(a >> n));
                case VECTOR_OP_URSHIFT: return (v0, v1) ->
                        v0.bOp(v1, (i, a, n) -> (long)((a & LSHR_SETUP_MASK) >>> n));
                case VECTOR_OP_LROTATE: return (v0, v1) ->
                        v0.bOp(v1, (i, a, n) -> (long)((a << n)|(a >> -n)));
                case VECTOR_OP_RROTATE: return (v0, v1) ->
                        v0.bOp(v1, (i, a, n) -> (long)((a >> n)|(a << -n)));
                default: return null;
                }}));
    }
    private static final
    ImplCache<Binary,BinaryOperator<LongVector>> BIN_IMPL
        = new ImplCache<>(Binary.class, LongVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Binary,long,VectorMask)
     */
    @ForceInline
    public final
    LongVector lanewise(VectorOperators.Binary op,
                                  Vector<Long> v,
                                  VectorMask<Long> m) {
        LongVector that = (LongVector) v;
        if (op == DIV) {
            // suppress div/0 exceptions in unset lanes
            that = that.lanewise(NOT, that.eq((long)0));
            return blend(lanewise(DIV, that), m);
        }
        return blend(lanewise(op, v), m);
    }
    // FIXME: Maybe all of the public final methods in this file (the
    // simple ones that just call lanewise) should be pushed down to
    // the X-VectorBits template.  They can't optimize properly at
    // this level, and must rely on inlining.  Does it work?
    // (If it works, of course keep the code here.)

    /**
     * Combines the lane values of this vector
     * with the value of a broadcast scalar.
     * <p>
     * This is a lane-wise binary operation which applies
     * the selected operation to each lane.
     * The return value will be equal to this expression:
     * {@code this.lanewise(op, this.broadcast(e))}.
     *
     * @param e the input scalar
     * @return the result of applying the operation lane-wise
     *         to the two input vectors
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long,VectorMask)
     */
    @ForceInline
    public final
    LongVector lanewise(VectorOperators.Binary op,
                                  long e) {
        int opc = opCode(op);
        if (opKind(op, VO_SHIFT) && (long)(int)e == e) {
            return lanewiseShift(op, (int) e);
        }
        if (op == ANDC2) {
            op = AND; e = (long) ~e;
        }
        return lanewise(op, broadcast(e));
    }

    /**
     * Combines the lane values of this vector
     * with the value of a broadcast scalar,
     * with selection of lane elements controlled by a mask.
     * <p>
     * This is a lane-wise binary operation which applies
     * the selected operation to each lane.
     * The return value will be equal to this expression:
     * {@code this.lanewise(op, this.broadcast(e), m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of applying the operation lane-wise
     *         to the input vector and the scalar
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #lanewise(VectorOperators.Binary,long)
     * @see #lanewise(VectorOperators.Binary,Vector,VectorMask)
     */
    @ForceInline
    public final
    LongVector lanewise(VectorOperators.Binary op,
                                  long e,
                                  VectorMask<Long> m) {
        return blend(lanewise(op, e), m);
    }


    /*package-private*/
    abstract LongVector
    lanewiseShift(VectorOperators.Binary op, int e);

    /*package-private*/
    @ForceInline
    final LongVector
    lanewiseShiftTemplate(VectorOperators.Binary op, int e) {
        // Special handling for these.  FIXME: Refactor?
        int opc = opCode(op);
        assert(opKind(op, VO_SHIFT));
        // As per shift specification for Java, mask the shift count.
        e &= SHIFT_MASK;
        if (op == ROR || op == ROL) {  // FIXME: JIT should do this
            LongVector hi = this.lanewise(LSHL, (op == ROR) ? -e : e);
            LongVector lo = this.lanewise(LSHR, (op == ROR) ? e : -e);
            return hi.lanewise(OR, lo);
        }
        return VectorIntrinsics.broadcastInt(
            opc, getClass(), long.class, length(),
            this, e,
            BIN_INT_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_LSHIFT: return (v, n) ->
                        v.uOp((i, a) -> (long)(a << n));
                case VECTOR_OP_RSHIFT: return (v, n) ->
                        v.uOp((i, a) -> (long)(a >> n));
                case VECTOR_OP_URSHIFT: return (v, n) ->
                        v.uOp((i, a) -> (long)((a & LSHR_SETUP_MASK) >>> n));
                case VECTOR_OP_LROTATE: return (v, n) ->
                        v.uOp((i, a) -> (long)((a << n)|(a >> -n)));
                case VECTOR_OP_RROTATE: return (v, n) ->
                        v.uOp((i, a) -> (long)((a >> n)|(a << -n)));
                default: return null;
                }}));
    }
    private static final
    ImplCache<Binary,VectorBroadcastIntOp<LongVector>> BIN_INT_IMPL
        = new ImplCache<>(Binary.class, LongVector.class);

    // As per shift specification for Java, mask the shift count.
    // We mask 0X3F (long), 0X1F (int), 0x0F (short), 0x7 (byte).
    // The latter two maskings go beyond the JLS, but seem reasonable
    // since our lane types are first-class types, not just dressed
    // up ints.
    private static final int SHIFT_MASK = (Long.SIZE - 1);
    private static final long LSHR_SETUP_MASK = -1;

    // Ternary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Ternary,long,long)
     */
    @Override
    public abstract LongVector lanewise(VectorOperators.Ternary op,
                                                  Vector<Long> v1,
                                                  Vector<Long> v2);
    @ForceInline
    final
    LongVector lanewiseTemplate(VectorOperators.Ternary op,
                                          Vector<Long> v1,
                                          Vector<Long> v2) {
        LongVector that = (LongVector) v1;
        LongVector tother = (LongVector) v2;
        // It's a word: https://www.dictionary.com/browse/tother
        // See also Chapter 11 of Dickens, Our Mutual Friend:
        // "Totherest Governor," replied Mr Riderhood...
        that.check(this);
        tother.check(this);
        if (op == BITWISE_BLEND) {
            // FIXME: Support this in the JIT.
            that = this.lanewise(XOR, that).lanewise(AND, tother);
            return this.lanewise(XOR, that);
        }
        int opc = opCode(op);
        return VectorIntrinsics.ternaryOp(
            opc, getClass(), long.class, length(),
            this, that, tother,
            TERN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_BITWISE_BLEND: return (v0, v1_, v2_) ->
                        v0.tOp(v1_, v2_, (i, a, b, c) -> (long)(a^((a^b)&c)));
                default: return null;
                }}));
    }
    private static final
    ImplCache<Ternary,TernaryOperation<LongVector>> TERN_IMPL
        = new ImplCache<>(Ternary.class, LongVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public final
    LongVector lanewise(VectorOperators.Ternary op,
                                  Vector<Long> v1,
                                  Vector<Long> v2,
                                  VectorMask<Long> m) {
        return blend(lanewise(op, v1, v2), m);
    }

    // There are no broadcasting versions of ternary lanewise,
    // because this class does not implement any ternary operations.

    // LongVector lanewise(VectorOperators.Ternary op,
    //                               long e1,
    //                               long e2) {
    //     return lanewise(op, broadcast(e1), broadcast(e1));
    // }

    // LongVector lanewise(VectorOperators.Ternary op,
    //                               long e1,
    //                               long e2,
    //                               VectorMask<Long> m) {
    //     return blend(lanewise(op, e), m);
    // }

    /// FULL-SERVICE BINARY METHODS: ADD, SUB, MUL, DIV
    //
    // These include masked and non-masked versions.
    // This subclass adds broadcast (masked or not).

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #add(long)
     */
    @Override
    public final LongVector add(Vector<Long> v) {
        return lanewise(ADD, v);
    }

    /**
     * Adds this vector to the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive addition operation ({@code +}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long)
     *    lanewise}{@code (}{@link VectorOperators#ADD
     *    ADD}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of adding each lane of this vector to the scalar
     * @see #add(Vector)
     * @see #broadcast(long)
     * @see #add(int,VectorMask)
     * @see VectorOperators#ADD
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector add(long e) {
        return lanewise(ADD, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #add(long,VectorMask)
     */
    @Override
    public final LongVector add(Vector<Long> v,
                                          VectorMask<Long> m) {
        return lanewise(ADD, v, m);
    }

    /**
     * Adds this vector to the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     *
     * This is a lane-wise binary operation which applies
     * the primitive addition operation ({@code +}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#ADD
     *    ADD}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of adding each lane of this vector to the scalar
     * @see #add(Vector,VectorMask)
     * @see #broadcast(long)
     * @see #add(int)
     * @see VectorOperators#ADD
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector add(long e,
                                          VectorMask<Long> m) {
        return lanewise(ADD, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #sub(long)
     */
    @Override
    public final LongVector sub(Vector<Long> v) {
        return lanewise(SUB, v);
    }

    /**
     * Subtracts an input scalar from this vector.
     *
     * This is a lane-wise binary operation which applies
     * the primitive subtraction operation ({@code -}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long)
     *    lanewise}{@code (}{@link VectorOperators#SUB
     *    SUB}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of subtracting the scalar from each lane of this vector
     * @see #sub(Vector)
     * @see #broadcast(long)
     * @see #sub(int,VectorMask)
     * @see VectorOperators#SUB
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector sub(long e) {
        return lanewise(SUB, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #sub(long,VectorMask)
     */
    @Override
    public final LongVector sub(Vector<Long> v,
                                          VectorMask<Long> m) {
        return lanewise(SUB, v, m);
    }

    /**
     * Subtracts an input scalar from this vector
     * under the control of a mask.
     *
     * This is a lane-wise binary operation which applies
     * the primitive subtraction operation ({@code -}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#SUB
     *    SUB}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of subtracting the scalar from each lane of this vector
     * @see #sub(Vector,VectorMask)
     * @see #broadcast(long)
     * @see #sub(int)
     * @see VectorOperators#SUB
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector sub(long e,
                                          VectorMask<Long> m) {
        return lanewise(SUB, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #mul(long)
     */
    @Override
    public final LongVector mul(Vector<Long> v) {
        return lanewise(MUL, v);
    }

    /**
     * Multiplies this vector by the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive multiplication operation ({@code *}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long)
     *    lanewise}{@code (}{@link VectorOperators#MUL
     *    MUL}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of multiplying this vector by the given scalar
     * @see #mul(Vector)
     * @see #broadcast(long)
     * @see #mul(int,VectorMask)
     * @see VectorOperators#MUL
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector mul(long e) {
        return lanewise(MUL, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #mul(long,VectorMask)
     */
    @Override
    public final LongVector mul(Vector<Long> v,
                                          VectorMask<Long> m) {
        return lanewise(MUL, v, m);
    }

    /**
     * Multiplies this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     *
     * This is a lane-wise binary operation which applies
     * the primitive multiplication operation ({@code *}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#MUL
     *    MUL}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of muling each lane of this vector to the scalar
     * @see #mul(Vector,VectorMask)
     * @see #broadcast(long)
     * @see #mul(int)
     * @see VectorOperators#MUL
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector mul(long e,
                                          VectorMask<Long> m) {
        return lanewise(MUL, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #div(long)
     */
    @Override
    public final LongVector div(Vector<Long> v) {
        return lanewise(DIV, v);
    }

    /**
     * Divides this vector by the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive division operation ({@code /}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long)
     *    lanewise}{@code (}{@link VectorOperators#DIV
     *    DIV}{@code , s)}.
     *
     * <p>
     * If the underlying scalar operator does not support
     * division by zero, but is presented with a zero divisor,
     * an {@code ArithmeticException} will be thrown.
     *
     * @param e the input scalar
     * @return the result of dividing each lane of this vector by the scalar
     * @see #div(Vector)
     * @see #broadcast(long)
     * @see #div(int,VectorMask)
     * @see VectorOperators#DIV
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector div(long e) {
        return lanewise(DIV, e);
    }

    /**
    /**
     * {@inheritDoc} <!--workaround-->
     * @see #div(long,VectorMask)
     */
    @Override
    public final LongVector div(Vector<Long> v,
                                          VectorMask<Long> m) {
        return lanewise(DIV, v, m);
    }

    /**
     * Divides this vector by the broadcast of an input scalar,
     * selecting lane elements controlled by a mask.
     *
     * This is a lane-wise binary operation which applies
     * the primitive division operation ({@code /}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,long,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#DIV
     *    DIV}{@code , s, m)}.
     *
     * <p>
     * If the underlying scalar operator does not support
     * division by zero, but is presented with a zero divisor,
     * an {@code ArithmeticException} will be thrown.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of dividing each lane of this vector by the scalar
     * @see #div(Vector,VectorMask)
     * @see #broadcast(long)
     * @see #div(int)
     * @see VectorOperators#DIV
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,long)
     */
    public final LongVector div(long e,
                                          VectorMask<Long> m) {
        return lanewise(DIV, e, m);
    }

    /// END OF FULL-SERVICE BINARY METHODS

    /// SECOND-TIER BINARY METHODS
    //
    // There are no masked or broadcast versions.

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final LongVector min(Vector<Long> v) {
        return lanewise(MIN, v);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final LongVector max(Vector<Long> v) {
        return lanewise(MAX, v);
    }

    /// UNARY OPERATIONS

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    LongVector neg() {
        return lanewise(NEG);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    LongVector abs() {
        return lanewise(ABS);
    }

    /// COMPARISONS

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    VectorMask<Long> eq(Vector<Long> v) {
        return compare(EQ, v);
    }

    /**
     * Tests if this vector is equal to an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies
     * the primitive equals operation ({@code ==}) to each lane.
     * The result is the same as {@code compare(VectorOperators.Comparison.EQ, e)}.
     *
     * @param e the input scalar
     * @return the result mask of testing if this vector
     *         is equal to {@code e}
     * @see #compare(VectorOperators.Comparison, long)
     */
    public final
    VectorMask<Long> eq(long e) {
        return compare(EQ, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    VectorMask<Long> lt(Vector<Long> v) {
        return compare(LT, v);
    }

    /**
     * Tests if this vector is less than an input scalar.
     * <p>
     * This is a lane-wise binary test operation which applies
     * the primitive less than operation ({@code <}) to each lane.
     * The result is the same as {@code compare(VectorOperators.LT, e)}.
     *
     * @param e the input scalar
     * @return the mask result of testing if this vector
     *         is less than the input scalar
     * @see #compare(VectorOperators.Comparison, long)
     */
    public final
    VectorMask<Long> lt(long e) {
        return compare(LT, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract
    VectorMask<Long> compare(VectorOperators.Comparison op, Vector<Long> v);

    /*package-private*/
    final
    <M extends VectorMask<Long>>
    M compareTemplate(Class<M> maskType, Comparison op, Vector<Long> v) {
        Objects.requireNonNull(v);
        LongSpecies vsp = vspecies();
        int opc = opCode(op);
        return VectorIntrinsics.compare(
            opc, getClass(), maskType, long.class, length(),
            this, (LongVector) v,
            (cond, v0, v1) -> {
                AbstractMask<Long> m
                    = v0.bTest(cond, v1, (cond_, i, a, b)
                               -> compareWithOp(cond, a, b));
                @SuppressWarnings("unchecked")
                M m2 = (M) m;
                return m2;
            });
    }

    private static
    boolean compareWithOp(int cond, long a, long b) {
        switch (cond) {
        case VectorIntrinsics.BT_eq:  return a == b;
        case VectorIntrinsics.BT_ne:  return a != b;
        case VectorIntrinsics.BT_lt:  return a <  b;
        case VectorIntrinsics.BT_le:  return a <= b;
        case VectorIntrinsics.BT_gt:  return a >  b;
        case VectorIntrinsics.BT_ge:  return a >= b;
        }
        throw new AssertionError();
    }

    /**
     * Tests this vector by comparing it with an input scalar,
     * according to the given comparison operation.
     * <p>
     * This is a lane-wise binary test operation which applies
     * the comparison operation to each lane.
     * <p>
     * The result is the same as
     * {@code compare(op, broadcast(species(), s))}.
     * That is, the scalar may be regarded as broadcast to
     * a vector of the same species, and then compared
     * against the original vector, using the selected
     * comparison operation.
     *
     * @param e the input scalar
     * @return the mask result of testing lane-wise if this vector
     *         compares to the input, according to the selected
     *         comparison operator
     * @see #eq(long)
     * @see #lessThan(long)
     */
    public abstract
    VectorMask<Long> compare(Comparison op, long e);

    /*package-private*/
    final
    <M extends VectorMask<Long>>
    M compareTemplate(Class<M> maskType, Comparison op, long e) {
        return compareTemplate(maskType, op, broadcast(e));
    }


    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    LongVector blend(Vector<Long> v, VectorMask<Long> m);

    /*package-private*/
    @ForceInline
    <M extends VectorMask<Long>>
    LongVector
    blendTemplate(Class<M> maskType, LongVector v, M m) {
        v.check(this);
        return VectorIntrinsics.blend(
            getClass(), maskType, long.class, length(),
            this, v, m,
            (v0, v1, m_) -> v0.bOp(v1, m_, (i, a, b) -> b));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract LongVector addIndex(int scale);

    /*package-private*/
    @ForceInline
    final LongVector addIndexTemplate(int scale) {
        LongSpecies vsp = vspecies();
        // make sure VLENGTH*scale doesn't overflow:
        vsp.checkScale(scale);
        return VectorIntrinsics.indexVector(
            getClass(), long.class, length(),
            this, scale, vsp,
            (v, scale_, s)
            -> {
                // If the platform doesn't support an INDEX
                // instruction directly, load IOTA from memory
                // and multiply.
                LongVector iota = s.iota();
                long sc = (long) scale_;
                return v.add(sc == 1 ? iota : iota.mul(sc));
            });
    }

    /**
     * Blends the lane elements of this vector with those of the broadcast of an
     * input scalar, selecting lanes controlled by a mask.
     * <p>
     * For each lane of the mask, at lane index {@code N}, if the mask lane
     * is set then the lane element at {@code N} from the input vector is
     * selected and placed into the resulting vector at {@code N},
     * otherwise the lane element at {@code N} from this input vector is
     * selected and placed into the resulting vector at {@code N}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of blending the lane elements of this vector with
     * those of the broadcast of an input scalar
     */
    public final LongVector blend(long e,
                                            VectorMask<Long> m) {
        return blend(broadcast(e), m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    LongVector slice(int origin, Vector<Long> v1);

    /*package-private*/
    final
    @ForceInline
    LongVector sliceTemplate(int origin, Vector<Long> v1) {
        LongVector that = (LongVector) v1;
        that.check(this);
        long[] a0 = this.getElements();
        long[] a1 = that.getElements();
        long[] res = new long[a0.length];
        int vlen = res.length;
        int firstPart = vlen - origin;
        System.arraycopy(a0, origin, res, 0, firstPart);
        System.arraycopy(a1, 0, res, firstPart, origin);
        return vectorFactory(res);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public final LongVector
    slice(int origin, Vector<Long> w, VectorMask<Long> m) {
        return broadcast(0).blend(slice(origin, w), m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public final LongVector slice(int origin) {
        return slice(origin, broadcast(0));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    LongVector unslice(int origin, Vector<Long> w, int part);

    /*package-private*/
    final
    @ForceInline
    LongVector
    unsliceTemplate(int origin, Vector<Long> w, int part) {
        LongVector that = (LongVector) w;
        that.check(this);
        long[] slice = this.getElements();
        long[] res = that.getElements();
        int vlen = res.length;
        int firstPart = vlen - origin;
        switch (part) {
        case 0:
            System.arraycopy(slice, 0, res, origin, firstPart);
            break;
        case 1:
            System.arraycopy(slice, firstPart, res, 0, origin);
            break;
        default:
            throw wrongPartForSlice(part);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    final
    @ForceInline
    <M extends VectorMask<Long>>
    LongVector
    unsliceTemplate(Class<M> maskType, int origin, Vector<Long> w, int part, M m) {
        LongVector that = (LongVector) w;
        that.check(this);
        LongVector slice = that.sliceTemplate(origin, that);
        slice = slice.blendTemplate(maskType, this, m);
        return slice.unsliceTemplate(origin, w, part);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract LongVector
    unslice(int origin, Vector<Long> w, int part, VectorMask<Long> m);

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public final LongVector unslice(int origin) {
        return unslice(origin, broadcast(0), 0);
    }

    private ArrayIndexOutOfBoundsException
    wrongPartForSlice(int part) {
        String msg = String.format("bad part number %d for slice operation",
                                   part);
        return new ArrayIndexOutOfBoundsException(msg);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract LongVector rearrange(VectorShuffle<Long> m);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Long>>
    LongVector rearrangeTemplate(Class<S> shuffletype, S shuffle) {
        shuffle.checkIndexes();
        return VectorIntrinsics.rearrangeOp(
            getClass(), shuffletype, long.class, length(),
            this, shuffle,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.laneSource(i);
                return v1.lane(ei);
            }));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract LongVector rearrange(VectorShuffle<Long> s,
                                                   VectorMask<Long> m);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Long>>
    LongVector rearrangeTemplate(Class<S> shuffletype,
                                           S shuffle,
                                           VectorMask<Long> m) {
        LongVector unmasked =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, long.class, length(),
                this, shuffle,
                (v1, s_) -> v1.uOp((i, a) -> {
                    int ei = s_.laneSource(i);
                    return ei < 0 ? 0 : v1.lane(ei);
                }));
        VectorMask<Long> valid = shuffle.laneIsValid();
        if (m.andNot(valid).anyTrue()) {
            shuffle.checkIndexes();
            throw new AssertionError();
        }
        return broadcast((long)0).blend(unmasked, valid);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract LongVector rearrange(VectorShuffle<Long> s,
                                                   Vector<Long> v);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Long>>
    LongVector rearrangeTemplate(Class<S> shuffletype,
                                           S shuffle,
                                           LongVector v) {
        VectorMask<Long> valid = shuffle.laneIsValid();
        VectorShuffle<Long> ws = shuffle.wrapIndexes();
        LongVector r1 =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, long.class, length(),
                this, shuffle,
                (v1, s_) -> v1.uOp((i, a) -> {
                    int ei = s_.laneSource(i);
                    return v1.lane(ei);
                }));
        LongVector r2 =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, long.class, length(),
                v, shuffle,
                (v1, s_) -> v1.uOp((i, a) -> {
                    int ei = s_.laneSource(i);
                    return v1.lane(ei);
                }));
        return r2.blend(r1, valid);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract LongVector selectFrom(Vector<Long> v);

    /*package-private*/
    @ForceInline
    final LongVector selectFromTemplate(LongVector v) {
        return v.rearrange(this.toShuffle());
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract LongVector selectFrom(Vector<Long> s, VectorMask<Long> m);

    /*package-private*/
    @ForceInline
    final LongVector selectFromTemplate(LongVector v,
                                                  AbstractMask<Long> m) {
        return v.rearrange(this.toShuffle(), m);
    }

    /// FMA


    // Type specific horizontal reductions

    /**
     * Returns a value accumulated from all the lanes of this vector.
     * <p>
     * This is an associative cross-lane reduction operation which
     * applies the specified operation to all the lane elements.
     *
     * <p>
     * A few reduction operations do not support arbitrary reordering
     * of their operands, yet are included here because of their
     * usefulness.
     *
     * <ul>
     * <li>
     * In the case of {@code FIRST_NONZERO}, the reduction returns
     * the value from the lowest-numbered non-zero lane. As with
     * {@code MAX} and {@code MIN}, floating point {@code -0.0}
     * is treated as a value distinct from the default zero value.
     *
     * <li>
     * In the case of floating point addition and multiplication, the
     * precise result will reflect the choice of an arbitrary order
     * of operations, which may even vary over time.
     *
     * <li>
     * All other reduction operations are fully commutative and
     * associative.  The implementation can choose any order of
     * processing, yet it will always produce the same result.
     *
     * </ul>
     *
     *
     * @param op the operation used to combine lane values
     * @return the accumulated result
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #reduceLanes(VectorOperators.Associative,VectorMask)
     */
    public abstract long reduceLanes(VectorOperators.Associative op);

    /**
     * Returns a value accumulated from selected lanes of this vector,
     * controlled by a mask.
     * <p>
     * This is an associative cross-lane reduction operation which
     * applies the specified operation to the selected lane elements.
     * <p>
     * If no elements are selected, an operation-specific identity
     * value is returned.
     * <ul>
     * <li>
     * If the operation is {@code ADD}, {#if[BITWISE]?{@code XOR}, {@code OR},}
     * or {@code FIRST_NONZERO},
     * then the identity value is zero, the default {@code long} value.
     * <li>
     * If the operation is {@code MUL},
     * then the identity value is one.
     * <li>
     * If the operation is {@code AND},
     * then the identity value is minus one (all bits set).
     * <li>
     * If the operation is {@code MAX},
     * then the identity value is {@code Long.MIN_VALUE}.
     * <li>
     * If the operation is {@code MIN},
     * then the identity value is {@code Long.MAX_VALUE}.
     * </ul>
     *
     * @param op the operation used to combine lane values
     * @param m the mask controlling lane selection
     * @return the reduced result accumulated from the selected lane values
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #reduceLanes(VectorOperators.Associative)
     */
    public abstract long reduceLanes(VectorOperators.Associative op,
                                       VectorMask<Long> m);

    /*package-private*/
    @ForceInline
    long reduceLanesTemplate(VectorOperators.Associative op,
                               VectorMask<Long> m) {
        LongVector v = reduceIdentityVector(op).blend(this, m);
        return v.reduceLanesTemplate(op);
    }

    /*package-private*/
    @ForceInline
    long reduceLanesTemplate(VectorOperators.Associative op) {
        if (op == FIRST_NONZERO) {
            // FIXME:  The JIT should handle this, and other scan ops alos.
            VectorMask<Long> thisNZ
                = this.viewAsIntegralLanes().compare(NE, (long) 0);
            return this.lane(thisNZ.firstTrue());
        }
        int opc = opCode(op);
        return fromBits(VectorIntrinsics.reductionCoerced(
            opc, getClass(), long.class, length(),
            this,
            REDUCE_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
              case VECTOR_OP_ADD: return v ->
                      toBits(v.rOp((long)0, (i, a, b) -> (long)(a + b)));
              case VECTOR_OP_MUL: return v ->
                      toBits(v.rOp((long)1, (i, a, b) -> (long)(a * b)));
              case VECTOR_OP_MIN: return v ->
                      toBits(v.rOp(MAX_OR_INF, (i, a, b) -> (long) Math.min(a, b)));
              case VECTOR_OP_MAX: return v ->
                      toBits(v.rOp(MIN_OR_INF, (i, a, b) -> (long) Math.max(a, b)));
              case VECTOR_OP_FIRST_NONZERO: return v ->
                      toBits(v.rOp((long)0, (i, a, b) -> toBits(a) != 0 ? a : b));
              case VECTOR_OP_AND: return v ->
                      toBits(v.rOp((long)-1, (i, a, b) -> (long)(a & b)));
              case VECTOR_OP_OR: return v ->
                      toBits(v.rOp((long)0, (i, a, b) -> (long)(a | b)));
              case VECTOR_OP_XOR: return v ->
                      toBits(v.rOp((long)0, (i, a, b) -> (long)(a ^ b)));
              default: return null;
              }})));
    }
    private static final
    ImplCache<Associative,Function<LongVector,Long>> REDUCE_IMPL
        = new ImplCache<>(Associative.class, LongVector.class);

    private
    @ForceInline
    LongVector reduceIdentityVector(VectorOperators.Associative op) {
        int opc = opCode(op);
        UnaryOperator<LongVector> fn
            = REDUCE_ID_IMPL.find(op, opc, (opc_) -> {
                switch (opc_) {
                case VECTOR_OP_ADD:
                case VECTOR_OP_OR:
                case VECTOR_OP_XOR:
                case VECTOR_OP_FIRST_NONZERO:
                    return v -> v.broadcast(0);
                case VECTOR_OP_MUL:
                    return v -> v.broadcast(1);
                case VECTOR_OP_AND:
                    return v -> v.broadcast(-1);
                case VECTOR_OP_MIN:
                    return v -> v.broadcast(MAX_OR_INF);
                case VECTOR_OP_MAX:
                    return v -> v.broadcast(MIN_OR_INF);
                default: return null;
                }
            });
        return fn.apply(this);
    }
    private static final
    ImplCache<Associative,UnaryOperator<LongVector>> REDUCE_ID_IMPL
        = new ImplCache<>(Associative.class, LongVector.class);

    private static final long MIN_OR_INF = Long.MIN_VALUE;
    private static final long MAX_OR_INF = Long.MAX_VALUE;

    public @Override abstract long reduceLanesToLong(VectorOperators.Associative op);
    public @Override abstract long reduceLanesToLong(VectorOperators.Associative op,
                                                     VectorMask<Long> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract long lane(int i);

    /**
     * Replaces the lane element of this vector at lane index {@code i} with
     * value {@code e}.
     * <p>
     * This is a cross-lane operation and behaves as if it returns the result
     * of blending this vector with an input vector that is the result of
     * broadcasting {@code e} and a mask that has only one lane set at lane
     * index {@code i}.
     *
     * @param i the lane index of the lane element to be replaced
     * @param e the value to be placed
     * @return the result of replacing the lane element of this vector at lane
     * index {@code i} with value {@code e}.
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract LongVector withLane(int i, long e);

    // Memory load operations

    /**
     * Returns an array of type {@code long[]}
     * containing all the lane values.
     * The array length is the same as the vector length.
     * The array elements are stored in lane order.
     * <p>
     * This method behaves as if it stores
     * this vector into an allocated array
     * (using {@link #intoArray(long[], int) intoArray})
     * and returns the array as follows:
     * <pre>{@code
     *   long[] a = new long[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the lane values of this vector
     */
    @ForceInline
    @Override
    public final long[] toArray() {
        long[] a = new long[vspecies().laneCount()];
        intoArray(a, 0);
        return a;
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * This is an alias for {@link #toArray()}
     * When this method is used on used on vectors
     * of type LongVector,
     * there will be no loss of range or precision.
     */
    @ForceInline
    @Override
    public final long[] toLongArray() {
        return toArray();
    }

    /** {@inheritDoc} <!--workaround-->
     * @implNote
     * When this method is used on used on vectors
     * of type LongVector,
     * up to nine bits of precision may be lost
     * for lane values of large magnitude.
     */
    @ForceInline
    @Override
    public final double[] toDoubleArray() {
        long[] a = toArray();
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = (double) a[i];
        }
        return res;
    }

    /**
     * Loads a vector from a byte array starting at an offset.
     * Bytes are composed into primitive lane elements according
     * to {@linkplain ByteOrder#LITTLE_ENDIAN little endian} ordering.
     * The vector is arranged into lanes according to
     * <a href="Vector.html#lane-order">memory ordering</a>.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * var bo = ByteOrder.LITTLE_ENDIAN;
     * var m = species.maskAll(true);
     * return fromByteBuffer(species, bb, offset, m, bo);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*ESIZE < 0}
     *         or {@code offset+(N+1)*ESIZE > a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    LongVector fromByteArray(VectorSpecies<Long> species,
                                       byte[] a, int offset) {
        return fromByteArray(species, a, offset, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Loads a vector from a byte array starting at an offset.
     * Bytes are composed into primitive lane elements according
     * to the specified byte order.
     * The vector is arranged into lanes according to
     * <a href="Vector.html#lane-order">memory ordering</a>.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * var m = species.maskAll(true);
     * return fromByteBuffer(species, bb, offset, m, bo);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @param bo the intended byte order
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*ESIZE < 0}
     *         or {@code offset+(N+1)*ESIZE > a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    LongVector fromByteArray(VectorSpecies<Long> species,
                                       byte[] a, int offset,
                                       ByteOrder bo) {
        LongSpecies vsp = (LongSpecies) species;
        offset = checkFromIndexSize(offset,
                                    vsp.vectorBitSize() / Byte.SIZE,
                                    a.length);
        return vsp.dummyVector()
            .fromByteArray0(a, offset).maybeSwap(bo);
    }

    /**
     * Loads a vector from a byte array starting at an offset
     * and using a mask.
     * Lanes where the mask is unset are filled with the default
     * value of long (zero).
     * Bytes are composed into primitive lane elements according
     * to {@linkplain ByteOrder#LITTLE_ENDIAN little endian} ordering.
     * The vector is arranged into lanes according to
     * <a href="Vector.html#lane-order">memory ordering</a>.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * var bo = ByteOrder.LITTLE_ENDIAN;
     * return fromByteBuffer(species, bb, offset, bo, m);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @param m the mask controlling lane selection
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*ESIZE < 0}
     *         or {@code offset+(N+1)*ESIZE > a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    LongVector fromByteArray(VectorSpecies<Long> species,
                                       byte[] a, int offset,
                                       VectorMask<Long> m) {
        return fromByteArray(species, a, offset, ByteOrder.LITTLE_ENDIAN, m);
    }

    /**
     * Loads a vector from a byte array starting at an offset
     * and using a mask.
     * Lanes where the mask is unset are filled with the default
     * value of long (zero).
     * Bytes are composed into primitive lane elements according
     * to {@linkplain ByteOrder#LITTLE_ENDIAN little endian} ordering.
     * The vector is arranged into lanes according to
     * <a href="Vector.html#lane-order">memory ordering</a>.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * return fromByteBuffer(species, bb, offset, m, bo);
     * }</pre>
     *
     * @param species species of desired vector
     * @param a the byte array
     * @param offset the offset into the array
     * @param bo the intended byte order
     * @param m the mask controlling lane selection
     * @return a vector loaded from a byte array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*ESIZE < 0}
     *         or {@code offset+(N+1)*ESIZE > a.length}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public static
    LongVector fromByteArray(VectorSpecies<Long> species,
                                       byte[] a, int offset,
                                       ByteOrder bo,
                                       VectorMask<Long> m) {
        LongSpecies vsp = (LongSpecies) species;
        LongVector zero = vsp.zero();
        LongVector iota = zero.addIndex(1);
        ((AbstractMask<Long>)m)
            .checkIndexByLane(offset, a.length, iota, 8);
        LongVector v = zero.fromByteArray0(a, offset);
        return zero.blend(v.maybeSwap(bo), m);
    }

    /**
     * Loads a vector from an array of type {@code long[]}
     * starting at an offset.
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code offset + N} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    LongVector fromArray(VectorSpecies<Long> species,
                                   long[] a, int offset) {
        LongSpecies vsp = (LongSpecies) species;
        offset = checkFromIndexSize(offset,
                                    vsp.laneCount(),
                                    a.length);
        return vsp.dummyVector().fromArray0(a, offset);
    }

    /**
     * Loads a vector from an array of type {@code long[]}
     * starting at an offset and using a mask.
     * Lanes where the mask is unset are filled with the default
     * value of long (zero).
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the array element at
     * index {@code offset + N} is placed into the resulting vector at lane index
     * {@code N}, otherwise the default element value is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array
     * @param m the mask controlling lane selection
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public static
    LongVector fromArray(VectorSpecies<Long> species,
                                   long[] a, int offset,
                                   VectorMask<Long> m) {
        LongSpecies vsp = (LongSpecies) species;
        LongVector zero = vsp.zero();
        LongVector iota = vsp.iota();
        ((AbstractMask<Long>)m)
            .checkIndexByLane(offset, a.length, iota, 1);
        return zero.blend(zero.fromArray0(a, offset), m);
    }

    /**
     * FIXME: EDIT THIS
     * Loads a vector from an array using indexes obtained from an index
     * map.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index, the
     * array element at index {@code offset + indexMap[mapOffset + N]} is placed into the
     * resulting vector at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param indexMap the index map
     * @param mapOffset the offset into the index map
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code mapOffset < 0}, or
     * {@code mapOffset > indexMap.length - species.length()},
     * or for any vector lane index {@code N} the result of
     * {@code offset + indexMap[mapOffset + N]} is {@code < 0} or {@code >= a.length}
     */
    @ForceInline
    public static
    LongVector fromArray(VectorSpecies<Long> species,
                                   long[] a, int offset,
                                   int[] indexMap, int mapOffset) {
        LongSpecies vsp = (LongSpecies) species;
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);
        Class<? extends LongVector> vectorType = vsp.vectorType();

        if (vsp.laneCount() == 1) {
          return LongVector.fromArray(vsp, a, offset + indexMap[mapOffset]);
        }

        // Index vector: vix[0:n] = k -> offset + indexMap[mapOffset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(vsp.indexShape()), indexMap, mapOffset).add(offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap(
            vectorType, long.class, vsp.laneCount(),
            IntVector.species(vsp.indexShape()).vectorType(),
            a, ARRAY_BASE, vix,
            a, offset, indexMap, mapOffset, vsp,
            (long[] c, int idx, int[] iMap, int idy, LongSpecies s) ->
            s.vOp(n -> c[idx + iMap[idy+n]]));
        }

    /**
     * Loads a vector from an array using indexes obtained from an index
     * map and using a mask.
     * FIXME: EDIT THIS
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then the array element at
     * index {@code offset + indexMap[mapOffset + N]} is placed into the resulting vector
     * at lane index {@code N}.
     *
     * @param species species of desired vector
     * @param a the array
     * @param offset the offset into the array, may be negative if relative
     * indexes in the index map compensate to produce a value within the
     * array bounds
     * @param indexMap the index map
     * @param mapOffset the offset into the index map
     * @param m the mask controlling lane selection
     * @return the vector loaded from an array
     * @throws IndexOutOfBoundsException if {@code mapOffset < 0}, or
     * {@code mapOffset > indexMap.length - species.length()},
     * or for any vector lane index {@code N} where the mask at lane
     * {@code N} is set the result of {@code offset + indexMap[mapOffset + N]} is
     * {@code < 0} or {@code >= a.length}
     */
    public static
    LongVector fromArray(VectorSpecies<Long> species,
                                   long[] a, int offset,
                                   int[] indexMap, int mapOffset,
                                   VectorMask<Long> m) {
        LongSpecies vsp = (LongSpecies) species;

        // FIXME This can result in out of bounds errors for unset mask lanes
        // FIX = Use a scatter instruction which routes the unwanted lanes
        // into a bit-bucket variable (private to implementation).
        // This requires a 2-D scatter in order to set a second base address.
        // See notes in https://bugs.openjdk.java.net/browse/JDK-8223367
        assert(m.allTrue());
        return (LongVector)
            zero(species).blend(fromArray(species, a, offset, indexMap, mapOffset), m);

    }

    /**
     * Loads a vector from a {@linkplain ByteBuffer byte buffer}
     * starting at an offset into the byte buffer.
     * <p>
     * Bytes are composed into primitive lane elements according to
     * {@link ByteOrder#LITTLE_ENDIAN little endian} byte order.
     * To avoid errors, the
     * {@linkplain ByteBuffer#order() intrinsic byte order}
     * of the buffer must be little-endian.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * var bo = ByteOrder.LITTLE_ENDIAN;
     * var m = species.maskAll(true);
     * return fromByteBuffer(species, bb, offset, m, bo);
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param offset the offset into the byte buffer
     * @return a vector loaded from a byte buffer
     * @throws IllegalArgumentException if byte order of bb
     *         is not {@link ByteOrder#LITTLE_ENDIAN}
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*8 < 0}
     *         or {@code offset+N**8 >= bb.limit()}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    LongVector fromByteBuffer(VectorSpecies<Long> species,
                                        ByteBuffer bb, int offset,
                                        ByteOrder bo) {
        LongSpecies vsp = (LongSpecies) species;
        offset = checkFromIndexSize(offset,
                                    vsp.laneCount(),
                                    bb.limit());
        return vsp.dummyVector()
            .fromByteBuffer0(bb, offset).maybeSwap(bo);
    }

    /**
     * Loads a vector from a {@linkplain ByteBuffer byte buffer}
     * starting at an offset into the byte buffer
     * and using a mask.
     * <p>
     * Bytes are composed into primitive lane elements according to
     * {@link ByteOrder#LITTLE_ENDIAN little endian} byte order.
     * To avoid errors, the
     * {@linkplain ByteBuffer#order() intrinsic byte order}
     * of the buffer must be little-endian.
     * <p>
     * This method behaves as if it returns the result of calling
     * {@link #fromByteBuffer(VectorSpecies,ByteBuffer,int,ByteOrder,VectorMask)
     * fromByteBuffer()} as follows:
     * <pre>{@code
     * var bb = ByteBuffer.wrap(a);
     * var bo = ByteOrder.LITTLE_ENDIAN;
     * var m = species.maskAll(true);
     * return fromByteBuffer(species, bb, offset, m, bo);
     * }</pre>
     *
     * @param species species of desired vector
     * @param bb the byte buffer
     * @param offset the offset into the byte buffer
     * @param m the mask controlling lane selection
     * @return a vector loaded from a byte buffer
     * @throws IllegalArgumentException if byte order of bb
     *         is not {@link ByteOrder#LITTLE_ENDIAN}
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N*8 < 0}
     *         or {@code offset+N**8 >= bb.limit()}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public static
    LongVector fromByteBuffer(VectorSpecies<Long> species,
                                        ByteBuffer bb, int offset,
                                        ByteOrder bo,
                                        VectorMask<Long> m) {
        if (m.allTrue()) {
            return fromByteBuffer(species, bb, offset, bo);
        }
        LongSpecies vsp = (LongSpecies) species;
        checkMaskFromIndexSize(offset,
                               vsp, m, 1,
                               bb.limit());
        LongVector zero = zero(vsp);
        LongVector v = zero.fromByteBuffer0(bb, offset);
        return zero.blend(v.maybeSwap(bo), m);
    }

    // Memory store operations

    /**
     * Stores this vector into an array of type {@code long[]}
     * starting at an offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * the lane element at index {@code N} is stored into the array
     * element {@code a[offset+N]}.
     *
     * @param a the array, of type {@code long[]}
     * @param offset the offset into the array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public void intoArray(long[] a, int offset) {
        LongSpecies vsp = vspecies();
        offset = checkFromIndexSize(offset,
                                    vsp.laneCount(),
                                    a.length);
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, arrayAddress(a, offset),
            this,
            a, offset,
            (arr, off, v)
            -> v.stOp(arr, off,
                      (arr_, off_, i, e) -> arr_[off_ + i] = e));
    }

    /**
     * Stores this vector into an array of long
     * starting at offset and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * the lane element at index {@code N} is stored into the array
     * element {@code a[offset+N]}.
     * If the mask lane at {@code N} is unset then the corresponding
     * array element {@code a[offset+N]} is left unchanged.
     * <p>
     * Array range checking is done for lanes where the mask is set.
     * Lanes where the mask is unset are not stored and do not need
     * to correspond to legitimate elements of {@code a}.
     * That is, unset lanes may correspond to array indexes less than
     * zero or beyond the end of the array.
     *
     * @param a the array, of type {@code long[]}
     * @param offset the offset into the array
     * @param m the mask controlling lane storage
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public final void intoArray(long[] a, int offset,
                                VectorMask<Long> m) {
        if (m.allTrue()) {
            intoArray(a, offset);
        } else {
            // FIXME: Cannot vectorize yet, if there's a mask.
            stOp(a, offset, m, (arr, off, i, v) -> arr[off+i] = v);
        }
    }

    /**
     * Stores this vector into an array of type {@code long[]}
     * using indexes obtained from an index map
     * and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then
     * the lane element at index {@code N} is stored into the array
     * element {@code a[f(N)]}, where {@code f(N)} is the
     * index mapping expression
     * {@code offset + indexMap[mapOffset + N]]}.
     *
     * @param a the array
     * @param offset an offset to combine with the index map offsets
     * @param indexMap the index map
     * @param mapOffset the offset into the index map
     * @param m the mask
     * @returns a vector of the values {@code m ? a[f(N)] : 0},
     *          {@code f(N) = offset + indexMap[mapOffset + N]]}.
     * @throws IndexOutOfBoundsException
     *         if {@code mapOffset+N < 0}
     *         or if {@code mapOffset+N >= indexMap.length},
     *         or if {@code f(N)=offset+indexMap[mapOffset+N]}
     *         is an invalid index into {@code a},
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public void intoArray(long[] a, int offset,
                          int[] indexMap, int mapOffset) {
        LongSpecies vsp = vspecies();
        if (length() == 1) {
            intoArray(a, offset + indexMap[mapOffset]);
            return;
        }
        IntVector.IntSpecies isp = (IntVector.IntSpecies) vsp.indexSpecies();
        if (isp.laneCount() != vsp.laneCount()) {
            stOp(a, offset,
                 (arr, off, i, e) -> {
                     int j = indexMap[mapOffset + i];
                     arr[off + j] = e;
                 });
            return;
        }

        // Index vector: vix[0:n] = i -> offset + indexMap[mo + i]
        IntVector vix = IntVector
            .fromArray(isp, indexMap, mapOffset)
            .add(offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        VectorIntrinsics.storeWithMap(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            isp.vectorType(),
            a, arrayAddress(a, 0), vix,
            this,
            a, offset, indexMap, mapOffset,
            (arr, off, v, map, mo)
            -> v.stOp(arr, off,
                      (arr_, off_, i, e) -> {
                          int j = map[mo + i];
                          arr[off + j] = e;
                      }));
    }

    /**
     * Stores this vector into an array of type {@code long[]}
     * using indexes obtained from an index map
     * and using a mask.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * if the mask lane at index {@code N} is set then
     * the lane element at index {@code N} is stored into the array
     * element {@code a[f(N)]}, where {@code f(N)} is the
     * index mapping expression
     * {@code offset + indexMap[mapOffset + N]]}.
     *
     * @param a the array
     * @param offset an offset to combine with the index map offsets
     * @param indexMap the index map
     * @param mapOffset the offset into the index map
     * @param m the mask
     * @returns a vector of the values {@code m ? a[f(N)] : 0},
     *          {@code f(N) = offset + indexMap[mapOffset + N]]}.
     * @throws IndexOutOfBoundsException
     *         if {@code mapOffset+N < 0}
     *         or if {@code mapOffset+N >= indexMap.length},
     *         or if {@code f(N)=offset+indexMap[mapOffset+N]}
     *         is an invalid index into {@code a},
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public final void intoArray(long[] a, int offset,
                                int[] indexMap, int mapOffset,
                                VectorMask<Long> m) {
        LongSpecies vsp = vspecies();
        if (m.allTrue()) {
            intoArray(a, offset, indexMap, mapOffset);
            return;
        }
        throw new AssertionError("fixme");
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int offset) {
        offset = checkFromIndexSize(offset,
                                    bitSize() / Byte.SIZE,
                                    a.length);
        this.maybeSwap(ByteOrder.LITTLE_ENDIAN)
            .intoByteArray0(a, offset);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int offset,
                                    VectorMask<Long> m) {
        if (m.allTrue()) {
            intoByteArray(a, offset);
            return;
        }
        LongSpecies vsp = vspecies();
        checkMaskFromIndexSize(offset, vsp, m, 8, a.length);
        conditionalStoreNYI(offset, vsp, m, 8, a.length);
        var oldVal = fromByteArray0(a, offset);
        var newVal = oldVal.blend(this, m);
        newVal.intoByteArray0(a, offset);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int offset,
                                    ByteOrder bo,
                                    VectorMask<Long> m) {
        maybeSwap(bo).intoByteArray(a, offset, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int offset,
                               ByteOrder bo) {
        maybeSwap(bo).intoByteBuffer0(bb, offset);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int offset,
                               ByteOrder bo,
                               VectorMask<Long> m) {
        if (m.allTrue()) {
            intoByteBuffer(bb, offset, bo);
            return;
        }
        LongSpecies vsp = vspecies();
        checkMaskFromIndexSize(offset, vsp, m, 8, bb.limit());
        conditionalStoreNYI(offset, vsp, m, 8, bb.limit());
        var oldVal = fromByteBuffer0(bb, offset);
        var newVal = oldVal.blend(this.maybeSwap(bo), m);
        newVal.intoByteBuffer0(bb, offset);
    }

    // ================================================

    // Low-level memory operations.
    //
    // Note that all of these operations *must* inline into a context
    // where the exact species of the involved vector is a
    // compile-time constant.  Otherwise, the intrinsic generation
    // will fail and performance will suffer.
    //
    // In many cases this is achieved by re-deriving a version of the
    // method in each concrete subclass (per species).  The re-derived
    // method simply calls one of these generic methods, with exact
    // parameters for the controlling metadata, which is either a
    // typed vector or constant species instance.

    // Unchecked loading operations in native byte order.
    // Caller is reponsible for applying index checks, masking, and
    // byte swapping.

    /*package-private*/
    @ForceInline
    LongVector fromArray0(long[] a, int offset) {
        LongSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, arrayAddress(a, offset),
            a, offset, vsp,
            (arr, off, s) -> s.ldOp(arr, off,
                                    (arr_, off_, i) -> arr_[off_ + i]));
    }

    @ForceInline
    @Override
    LongVector fromByteArray0(byte[] a, int offset) {
        LongSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, byteArrayAddress(a, offset),
            a, offset, vsp,
            (arr, off, s) -> {
                LongBuffer tb = wrapper(arr, off, NATIVE_ENDIAN);
                return s.ldOp(tb, 0, (tb_, __, i) -> tb_.get(i));
            });
    }

    @ForceInline
    LongVector fromByteBuffer0(ByteBuffer bb, int offset) {
        LongSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            bufferBase(bb), bufferAddress(bb, offset),
            bb, offset, vsp,
            (buf, off, s) -> {
                LongBuffer tb = wrapper(buf, off, NATIVE_ENDIAN);
                return s.ldOp(tb, 0, (tb_, __, i) -> tb_.get(i));
           });
    }

    // Unchecked storing operations in native byte order.
    // Caller is reponsible for applying index checks, masking, and
    // byte swapping.

    @ForceInline
    void intoArray0(long[] a, int offset) {
        LongSpecies vsp = vspecies();
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, arrayAddress(a, offset),
            this, a, offset,
            (arr, off, v)
            -> v.stOp(arr, off,
                      (arr_, off_, i, e) -> arr_[off_+i] = e));
    }

    @ForceInline
    void intoByteArray0(byte[] a, int offset) {
        LongSpecies vsp = vspecies();
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, byteArrayAddress(a, offset),
            this, a, offset,
            (arr, off, v) -> {
                LongBuffer tb = wrapper(arr, off, NATIVE_ENDIAN);
                v.stOp(tb, 0, (tb_, __, i, e) -> tb_.put(i, e));
            });
    }

    @ForceInline
    void intoByteBuffer0(ByteBuffer bb, int offset) {
        LongSpecies vsp = vspecies();
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            bufferBase(bb), bufferAddress(bb, offset),
            this, bb, offset,
            (buf, off, v) -> {
                LongBuffer tb = wrapper(buf, off, NATIVE_ENDIAN);
                v.stOp(tb, 0, (tb_, __, i, e) -> tb_.put(i, e));
            });
    }

    // End of low-level memory operations.

    private static
    void checkMaskFromIndexSize(int offset,
                                LongSpecies vsp,
                                VectorMask<Long> m,
                                int scale,
                                int limit) {
        ((AbstractMask<Long>)m)
            .checkIndexByLane(offset, limit, vsp.iota(), scale);
    }

    private void conditionalStoreNYI(int offset,
                                     LongSpecies vsp,
                                     VectorMask<Long> m,
                                     int scale,
                                     int limit) {
        if (offset < 0 || offset + vsp.laneCount() * scale > limit) {
            String msg =
                String.format("unimplemented: store @%d in [0..%d), %s in %s",
                              offset, limit, m, vsp);
            throw new AssertionError(msg);
        }
    }

    /*package-private*/
    @Override
    @ForceInline
    LongVector maybeSwap(ByteOrder bo) {
        if (bo != NATIVE_ENDIAN) {
            return this.reinterpretAsBytes()
                .rearrange(swapBytesShuffle())
                .reinterpretAsLongs();
        }
        return this;
    }

    static final int ARRAY_SHIFT =
        31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_LONG_INDEX_SCALE);
    static final long ARRAY_BASE =
        Unsafe.ARRAY_LONG_BASE_OFFSET;

    @ForceInline
    static long arrayAddress(long[] a, int index) {
        return ARRAY_BASE + (((long)index) << ARRAY_SHIFT);
    }

    @ForceInline
    static long byteArrayAddress(byte[] a, int index) {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + index;
    }

    // Byte buffer wrappers.
    private static LongBuffer wrapper(ByteBuffer bb, int offset,
                                        ByteOrder bo) {
        return bb.duplicate().position(offset).slice()
            .order(bo).asLongBuffer();
    }
    private static LongBuffer wrapper(byte[] a, int offset,
                                        ByteOrder bo) {
        return ByteBuffer.wrap(a, offset, a.length - offset)
            .order(bo).asLongBuffer();
    }

    // ================================================

    /// Reinterpreting view methods:
    //   lanewise reinterpret: viewAsXVector()
    //   keep shape, redraw lanes: reinterpretAsEs()

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    @Override
    public final ByteVector reinterpretAsBytes() {
         // Going to ByteVector, pay close attention to byte order.
         assert(REGISTER_ENDIAN == ByteOrder.LITTLE_ENDIAN);
         return asByteVectorRaw();
         //return asByteVectorRaw().rearrange(swapBytesShuffle());
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    @Override
    public final LongVector viewAsIntegralLanes() {
        return this;
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    @Override
    public final
    DoubleVector
    viewAsFloatingLanes() {
        LaneType flt = LaneType.LONG.asFloating();
        return (DoubleVector) asVectorRaw(flt);
    }

    // ================================================

    /// Object methods: toString, equals, hashCode
    //
    // Object methods are defined as if via Arrays.toString, etc.,
    // is applied to the array of elements.  Two equal vectors
    // are required to have equal species and equal lane values.

    /**
     * Returns a string representation of this vector, of the form
     * {@code "[0,1,2...]"}, reporting the lane values of this vector,
     * in lane order.
     *
     * The string is produced as if by a call to {@linkplain
     * java.util.Arrays#toString(long[]) the {@code Arrays.toString}
     * method} appropriate to the long array returned by
     * {@linkplain #toArray this vector's {@code toArray} method}.
     *
     * @return a string of the form {@code "[0,1,2...]"}
     * reporting the lane values of this vector
     */
    @Override
    public final String toString() {
        // now that toArray is strongly typed, we can define this
        return Arrays.toString(toArray());
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    @ForceInline
    public boolean equals(Object obj) {
        if (obj instanceof Vector) {
            Vector<?> that = (Vector<?>) obj;
            if (this.species().equals(that.species())) {
                return this.eq(that.check(this.species())).allTrue();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public int hashCode() {
        // now that toArray is strongly typed, we can define this
        return Objects.hash(species(), Arrays.hashCode(toArray()));
    }

    // ================================================

    // Species

    /**
     * Class representing {@link LongVector}'s of the same {@link VectorShape VectorShape}.
     */
    /*package-private*/
    static final class LongSpecies extends AbstractSpecies<Long> {
        private LongSpecies(VectorShape shape,
                Class<? extends LongVector> vectorType,
                Class<? extends AbstractMask<Long>> maskType,
                Function<Object, LongVector> vectorFactory) {
            super(shape, LaneType.of(long.class),
                  vectorType, maskType,
                  vectorFactory);
            assert(this.elementSize() == Long.SIZE);
        }

        // Specializing overrides:

        @Override
        @ForceInline
        public final Class<Long> elementType() {
            return long.class;
        }

        @Override
        @ForceInline
        public final Class<Long> genericElementType() {
            return Long.class;
        }

        @Override
        @ForceInline
        public final Class<long[]> arrayType() {
            return long[].class;
        }

        @SuppressWarnings("unchecked")
        @Override
        @ForceInline
        public final Class<? extends LongVector> vectorType() {
            return (Class<? extends LongVector>) vectorType;
        }

        @Override
        @ForceInline
        public final long checkValue(long e) {
            longToElementBits(e);  // only for exception
            return e;
        }

        /*package-private*/
        @Override
        @ForceInline
        final LongVector broadcastBits(long bits) {
            return (LongVector)
                VectorIntrinsics.broadcastCoerced(
                    vectorType, long.class, laneCount,
                    bits, this,
                    (bits_, s_) -> s_.rvOp(i -> bits_));
        }

        /*package-private*/
        @ForceInline
        public
        final LongVector broadcast(long e) {
            return broadcastBits(toBits(e));
        }


        /*package-private*/
        final @Override
        @ForceInline
        long longToElementBits(long value) {
            // In this case, the conversion can never fail.
            return value;
        }

        @Override
        @ForceInline
        public final LongVector fromValues(long... values) {
            VectorIntrinsics.requireLength(values.length, laneCount);
            long[] va = new long[laneCount()];
            for (int i = 0; i < va.length; i++) {
                long lv = values[i];
                long v = (long) lv;
                va[i] = v;
                if ((long)v != lv) {
                    throw badElementBits(lv, v);
                }
            }
            return dummyVector().fromArray0(va, 0);
        }

        /* this non-public one is for internal conversions */
        @Override
        @ForceInline
        final LongVector fromIntValues(int[] values) {
            VectorIntrinsics.requireLength(values.length, laneCount);
            long[] va = new long[laneCount()];
            for (int i = 0; i < va.length; i++) {
                int lv = values[i];
                long v = (long) lv;
                va[i] = v;
                if ((int)v != lv) {
                    throw badElementBits(lv, v);
                }
            }
            return dummyVector().fromArray0(va, 0);
        }

        // Virtual constructors

        @ForceInline
        @Override final
        public LongVector fromArray(Object a, int offset) {
            // User entry point:  Be careful with inputs.
            return LongVector
                .fromArray(this, (long[]) a, offset);
        }

        @Override final
        LongVector dummyVector() {
            return (LongVector) super.dummyVector();
        }

        final
        LongVector vectorFactory(long[] vec) {
            // Species delegates all factory requests to its dummy
            // vector.  The dummy knows all about it.
            return dummyVector().vectorFactory(vec);
        }

        /*package-private*/
        final @Override
        @ForceInline
        LongVector rvOp(RVOp f) {
            long[] res = new long[laneCount()];
            for (int i = 0; i < res.length; i++) {
                long bits =  f.apply(i);
                res[i] = fromBits(bits);
            }
            return dummyVector().vectorFactory(res);
        }

        LongVector vOp(FVOp f) {
            long[] res = new long[laneCount()];
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i);
            }
            return dummyVector().vectorFactory(res);
        }

        LongVector vOp(VectorMask<Long> m, FVOp f) {
            long[] res = new long[laneCount()];
            boolean[] mbits = ((AbstractMask<Long>)m).getBits();
            for (int i = 0; i < res.length; i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return dummyVector().vectorFactory(res);
        }

        /*package-private*/
        @ForceInline
        <M> LongVector ldOp(M memory, int offset,
                                      FLdOp<M> f) {
            return dummyVector().ldOp(memory, offset, f);
        }

        /*package-private*/
        @ForceInline
        <M> LongVector ldOp(M memory, int offset,
                                      AbstractMask<Long> m,
                                      FLdOp<M> f) {
            return dummyVector().ldOp(memory, offset, m, f);
        }

        /*package-private*/
        @ForceInline
        <M> void stOp(M memory, int offset, FStOp<M> f) {
            dummyVector().stOp(memory, offset, f);
        }

        /*package-private*/
        @ForceInline
        <M> void stOp(M memory, int offset,
                      AbstractMask<Long> m,
                      FStOp<M> f) {
            dummyVector().stOp(memory, offset, m, f);
        }

        // N.B. Make sure these constant vectors and
        // masks load up correctly into registers.
        //
        // Also, see if we can avoid all that switching.
        // Could we cache both vectors and both masks in
        // this species object?

        // Zero and iota vector access
        @Override
        @ForceInline
        public final LongVector zero() {
            if ((Class<?>) vectorType() == LongMaxVector.class)
                return LongMaxVector.ZERO;
            switch (vectorBitSize()) {
                case 64: return Long64Vector.ZERO;
                case 128: return Long128Vector.ZERO;
                case 256: return Long256Vector.ZERO;
                case 512: return Long512Vector.ZERO;
            }
            throw new AssertionError();
        }        

        @Override
        @ForceInline
        public final LongVector iota() {
            if ((Class<?>) vectorType() == LongMaxVector.class)
                return LongMaxVector.IOTA;
            switch (vectorBitSize()) {
                case 64: return Long64Vector.IOTA;
                case 128: return Long128Vector.IOTA;
                case 256: return Long256Vector.IOTA;
                case 512: return Long512Vector.IOTA;
            }
            throw new AssertionError();
        }

        // Mask access
        @Override
        @ForceInline
        public final VectorMask<Long> maskAll(boolean bit) {
            if ((Class<?>) vectorType() == LongMaxVector.class)
                return LongMaxVector.LongMaxMask.maskAll(bit);
            switch (vectorBitSize()) {
                case 64: return Long64Vector.Long64Mask.maskAll(bit);
                case 128: return Long128Vector.Long128Mask.maskAll(bit);
                case 256: return Long256Vector.Long256Mask.maskAll(bit);
                case 512: return Long512Vector.Long512Mask.maskAll(bit);
            }
            throw new AssertionError();
        }
    }

    /**
     * Finds a species for an element type of {@code long} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code long} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static LongSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (LongSpecies) SPECIES_64;
            case S_128_BIT: return (LongSpecies) SPECIES_128;
            case S_256_BIT: return (LongSpecies) SPECIES_256;
            case S_512_BIT: return (LongSpecies) SPECIES_512;
            case S_Max_BIT: return (LongSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link LongVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Long> SPECIES_64
        = new LongSpecies(VectorShape.S_64_BIT,
                            Long64Vector.class,
                            Long64Vector.Long64Mask.class,
                            Long64Vector::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Long> SPECIES_128
        = new LongSpecies(VectorShape.S_128_BIT,
                            Long128Vector.class,
                            Long128Vector.Long128Mask.class,
                            Long128Vector::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Long> SPECIES_256
        = new LongSpecies(VectorShape.S_256_BIT,
                            Long256Vector.class,
                            Long256Vector.Long256Mask.class,
                            Long256Vector::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Long> SPECIES_512
        = new LongSpecies(VectorShape.S_512_BIT,
                            Long512Vector.class,
                            Long512Vector.Long512Mask.class,
                            Long512Vector::new);

    /** Species representing {@link LongVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Long> SPECIES_MAX
        = new LongSpecies(VectorShape.S_Max_BIT,
                            LongMaxVector.class,
                            LongMaxVector.LongMaxMask.class,
                            LongMaxVector::new);

    /**
     * Preferred species for {@link LongVector}s.
     * A preferred species is a species of maximal bit-size for the platform.
     */
    public static final VectorSpecies<Long> SPECIES_PREFERRED
        = (LongSpecies) VectorSpecies.ofPreferred(long.class);


    // ==== JROSE NAME CHANGES ====

    /** Use lanewise(NEG, m). */
    @Deprecated
    public final LongVector neg(VectorMask<Long> m) {
        return lanewise(NEG, m);
    }

    /** Use lanewise(ABS, m). */
    @Deprecated
    public final LongVector abs(VectorMask<Long> m) {
        return lanewise(ABS, m);
    }

    /** Use explicit argument of ByteOrder.LITTLE_ENDIAN */
    @Deprecated
    public static
    LongVector fromByteBuffer(VectorSpecies<Long> species,
                                        ByteBuffer bb, int offset) {
        ByteOrder bo = ByteOrder.LITTLE_ENDIAN;
        if (bb.order() != bo)  throw new IllegalArgumentException();
        return fromByteBuffer(species, bb, offset, bo);
    }

    /** Use explicit argument of ByteOrder.LITTLE_ENDIAN */
    @Deprecated
    public static
    LongVector fromByteBuffer(VectorSpecies<Long> species,
                                        ByteBuffer bb, int offset,
                                        VectorMask<Long> m) {
        ByteOrder bo = ByteOrder.LITTLE_ENDIAN;
        if (bb.order() != bo)  throw new IllegalArgumentException();
        return fromByteBuffer(species, bb, offset, bo, m);
    }

    /** Use fromValues(s, value...) */
    @Deprecated
    public static
    LongVector scalars(VectorSpecies<Long> species,
                                 long... values) {
        return fromValues(species, values);
    }

    @Deprecated public final long addLanes() { return reduceLanes(ADD); }
    @Deprecated public final long addLanes(VectorMask<Long> m) { return reduceLanes(ADD, m); }
    @Deprecated public final long mulLanes() { return reduceLanes(MUL); }
    @Deprecated public final long mulLanes(VectorMask<Long> m) { return reduceLanes(MUL, m); }
    @Deprecated public final long minLanes() { return reduceLanes(MIN); }
    @Deprecated public final long minLanes(VectorMask<Long> m) { return reduceLanes(MIN, m); }
    @Deprecated public final long maxLanes() { return reduceLanes(MAX); }
    @Deprecated public final long maxLanes(VectorMask<Long> m) { return reduceLanes(MAX, m); }
    @Deprecated public final long orLanes() { return reduceLanes(OR); }
    @Deprecated public final long orLanes(VectorMask<Long> m) { return reduceLanes(OR, m); }
    @Deprecated public final long andLanes() { return reduceLanes(AND); }
    @Deprecated public final long andLanes(VectorMask<Long> m) { return reduceLanes(AND, m); }
    @Deprecated public final long xorLanes() { return reduceLanes(XOR); }
    @Deprecated public final long xorLanes(VectorMask<Long> m) { return reduceLanes(XOR, m); }
    @Deprecated public final LongVector sqrt() { return lanewise(SQRT); }
    @Deprecated public final LongVector sqrt(VectorMask<Long> m) { return lanewise(SQRT, m); }
    @Deprecated public final LongVector tan() { return lanewise(TAN); }
    @Deprecated public final LongVector tan(VectorMask<Long> m) { return lanewise(TAN, m); }
    @Deprecated public final LongVector tanh() { return lanewise(TANH); }
    @Deprecated public final LongVector tanh(VectorMask<Long> m) { return lanewise(TANH, m); }
    @Deprecated public final LongVector sin() { return lanewise(SIN); }
    @Deprecated public final LongVector sin(VectorMask<Long> m) { return lanewise(SIN, m); }
    @Deprecated public final LongVector sinh() { return lanewise(SINH); }
    @Deprecated public final LongVector sinh(VectorMask<Long> m) { return lanewise(SINH, m); }
    @Deprecated public final LongVector cos() { return lanewise(COS); }
    @Deprecated public final LongVector cos(VectorMask<Long> m) { return lanewise(COS, m); }
    @Deprecated public final LongVector cosh() { return lanewise(COSH); }
    @Deprecated public final LongVector cosh(VectorMask<Long> m) { return lanewise(COSH, m); }
    @Deprecated public final LongVector asin() { return lanewise(ASIN); }
    @Deprecated public final LongVector asin(VectorMask<Long> m) { return lanewise(ASIN, m); }
    @Deprecated public final LongVector acos() { return lanewise(ACOS); }
    @Deprecated public final LongVector acos(VectorMask<Long> m) { return lanewise(ACOS, m); }
    @Deprecated public final LongVector atan() { return lanewise(ATAN); }
    @Deprecated public final LongVector atan(VectorMask<Long> m) { return lanewise(ATAN, m); }
    @Deprecated public final LongVector atan2(Vector<Long> v) { return lanewise(ATAN2, v); }
    @Deprecated public final LongVector atan2(long s) { return lanewise(ATAN2, s); }
    @Deprecated public final LongVector atan2(Vector<Long> v, VectorMask<Long> m) { return lanewise(ATAN2, v, m); }
    @Deprecated public final LongVector atan2(long s, VectorMask<Long> m) { return lanewise(ATAN2, s, m); }
    @Deprecated public final LongVector cbrt() { return lanewise(CBRT); }
    @Deprecated public final LongVector cbrt(VectorMask<Long> m) { return lanewise(CBRT, m); }
    @Deprecated public final LongVector log() { return lanewise(LOG); }
    @Deprecated public final LongVector log(VectorMask<Long> m) { return lanewise(LOG, m); }
    @Deprecated public final LongVector log10() { return lanewise(LOG10); }
    @Deprecated public final LongVector log10(VectorMask<Long> m) { return lanewise(LOG10, m); }
    @Deprecated public final LongVector log1p() { return lanewise(LOG1P); }
    @Deprecated public final LongVector log1p(VectorMask<Long> m) { return lanewise(LOG1P, m); }
    @Deprecated public final LongVector pow(Vector<Long> v) { return lanewise(POW, v); }
    @Deprecated public final LongVector pow(long s) { return lanewise(POW, s); }
    @Deprecated public final LongVector pow(Vector<Long> v, VectorMask<Long> m) { return lanewise(POW, v, m); }
    @Deprecated public final LongVector pow(long s, VectorMask<Long> m) { return lanewise(POW, s, m); }
    @Deprecated public final LongVector exp() { return lanewise(EXP); }
    @Deprecated public final LongVector exp(VectorMask<Long> m) { return lanewise(EXP, m); }
    @Deprecated public final LongVector expm1() { return lanewise(EXPM1); }
    @Deprecated public final LongVector expm1(VectorMask<Long> m) { return lanewise(EXPM1, m); }
    @Deprecated public final LongVector hypot(Vector<Long> v) { return lanewise(HYPOT, v); }
    @Deprecated public final LongVector hypot(long s) { return lanewise(HYPOT, s); }
    @Deprecated public final LongVector hypot(Vector<Long> v, VectorMask<Long> m) { return lanewise(HYPOT, v, m); }
    @Deprecated public final LongVector hypot(long s, VectorMask<Long> m) { return lanewise(HYPOT, s, m); }
    @Deprecated 
    public final LongVector and(Vector<Long> v) { return lanewise(AND, v); }
    @Deprecated public final LongVector and(long s) { return lanewise(AND, s); }
    @Deprecated public final LongVector and(Vector<Long> v, VectorMask<Long> m) { return lanewise(AND, v, m); }
    @Deprecated public final LongVector and(long s, VectorMask<Long> m) { return lanewise(AND, s, m); }
    @Deprecated public final LongVector or(Vector<Long> v) { return lanewise(OR, v); }
    @Deprecated public final LongVector or(long s) { return lanewise(OR, s); }
    @Deprecated public final LongVector or(Vector<Long> v, VectorMask<Long> m) { return lanewise(OR, v, m); }
    @Deprecated public final LongVector or(long s, VectorMask<Long> m) { return lanewise(OR, s, m); }
    @Deprecated public final LongVector xor(Vector<Long> v) { return lanewise(XOR, v); }
    @Deprecated public final LongVector xor(long s) { return lanewise(XOR, s); }
    @Deprecated public final LongVector xor(Vector<Long> v, VectorMask<Long> m) { return lanewise(XOR, v, m); }
    @Deprecated public final LongVector xor(long s, VectorMask<Long> m) { return lanewise(XOR, s, m); }
    @Deprecated public final LongVector not() { return lanewise(NOT); }
    @Deprecated public final LongVector not(VectorMask<Long> m) { return lanewise(NOT, m); }
    @Deprecated public final LongVector shiftLeft(int s) { return lanewise(LSHL, (long) s); }
    @Deprecated public final LongVector shiftLeft(int s, VectorMask<Long> m) { return lanewise(LSHL, (long) s, m); }
    @Deprecated public final LongVector shiftLeft(Vector<Long> v) { return lanewise(LSHL, v); }
    @Deprecated public final LongVector shiftLeft(Vector<Long> v, VectorMask<Long> m) { return lanewise(LSHL, v, m); }
    @Deprecated public final LongVector shiftRight(int s) { return lanewise(LSHR, (long) s); }
    @Deprecated public final LongVector shiftRight(int s, VectorMask<Long> m) { return lanewise(LSHR, (long) s, m); }
    @Deprecated public final LongVector shiftRight(Vector<Long> v) { return lanewise(LSHR, v); }
    @Deprecated public final LongVector shiftRight(Vector<Long> v, VectorMask<Long> m) { return lanewise(LSHR, v, m); }
    @Deprecated public final LongVector shiftArithmeticRight(int s) { return lanewise(ASHR, (long) s); }
    @Deprecated public final LongVector shiftArithmeticRight(int s, VectorMask<Long> m) { return lanewise(ASHR, (long) s, m); }
    @Deprecated public final LongVector shiftArithmeticRight(Vector<Long> v) { return lanewise(ASHR, v); }
    @Deprecated public final LongVector shiftArithmeticRight(Vector<Long> v, VectorMask<Long> m) { return lanewise(ASHR, v, m); }
    @Deprecated public final LongVector rotateLeft(int s) { return lanewise(ROL, (long) s); }
    @Deprecated public final LongVector rotateLeft(int s, VectorMask<Long> m) { return lanewise(ROL, (long) s, m); }
    @Deprecated public final LongVector rotateRight(int s) { return lanewise(ROR, (long) s); }
    @Deprecated public final LongVector rotateRight(int s, VectorMask<Long> m) { return lanewise(ROR, (long) s, m); }
    @Deprecated @Override public LongVector rotateLanesLeft(int i) { return (LongVector) super.rotateLanesLeft(i); }
    @Deprecated @Override public LongVector rotateLanesRight(int i) { return (LongVector) super.rotateLanesRight(i); }
    @Deprecated @Override public LongVector shiftLanesLeft(int i) { return (LongVector) super.shiftLanesLeft(i); }
    @Deprecated @Override public LongVector shiftLanesRight(int i) { return (LongVector) super.shiftLanesRight(i); }
    @Deprecated public LongVector with(int i, long e) { return withLane(i, e); }
}
