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
import java.nio.FloatBuffer;
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
 * {@code float} values.
 */
@SuppressWarnings("cast")  // warning: redundant cast
public abstract class FloatVector extends AbstractVector<Float> {

    FloatVector() {}

    static final int FORBID_OPCODE_KIND = VO_NOFP;

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
    abstract float[] getElements();

    // Virtualized constructors

    /**
     * Build a vector directly using my own constructor.
     * It is an error if the array is aliased elsewhere.
     */
    /*package-private*/
    abstract FloatVector vectorFactory(float[] vec);

    /**
     * Build a mask directly using my species.
     * It is an error if the array is aliased elsewhere.
     */
    /*package-private*/
    @ForceInline
    AbstractMask<Float> maskFactory(boolean[] bits) {
        return vspecies().maskFactory(bits);
    }

    // Constant loader (takes dummy as vector arg)
    interface FVOp {
        float apply(int i);
    }

    /*package-private*/
    @ForceInline
    FloatVector vOp(FVOp f) {
        float[] res = new float[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i);
        }
        return vectorFactory(res);
    }

    @ForceInline
    FloatVector vOp(VectorMask<Float> m, FVOp f) {
        float[] res = new float[length()];
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
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
        float apply(int i, float a);
    }

    /*package-private*/
    @ForceInline
    FloatVector uOp(FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    FloatVector uOp(VectorMask<Float> m,
                             FUnOp f) {
        float[] vec = getElements();
        float[] res = new float[length()];
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return vectorFactory(res);
    }

    // Binary operator

    /*package-private*/
    interface FBinOp {
        float apply(int i, float a, float b);
    }

    /*package-private*/
    @ForceInline
    FloatVector bOp(Vector<Float> o,
                             FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((FloatVector)o).getElements();
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    FloatVector bOp(Vector<Float> o,
                             VectorMask<Float> m,
                             FBinOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((FloatVector)o).getElements();
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return vectorFactory(res);
    }

    // Ternary operator

    /*package-private*/
    interface FTriOp {
        float apply(int i, float a, float b, float c);
    }

    /*package-private*/
    @ForceInline
    FloatVector tOp(Vector<Float> o1,
                             Vector<Float> o2,
                             FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((FloatVector)o1).getElements();
        float[] vec3 = ((FloatVector)o2).getElements();
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    FloatVector tOp(Vector<Float> o1,
                             Vector<Float> o2,
                             VectorMask<Float> m,
                             FTriOp f) {
        float[] res = new float[length()];
        float[] vec1 = this.getElements();
        float[] vec2 = ((FloatVector)o1).getElements();
        float[] vec3 = ((FloatVector)o2).getElements();
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return vectorFactory(res);
    }

    // Reduction operator

    /*package-private*/
    @ForceInline
    float rOp(float v, FBinOp f) {
        float[] vec = getElements();
        for (int i = 0; i < vec.length; i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    // Memory reference

    /*package-private*/
    interface FLdOp<M> {
        float apply(M memory, int offset, int i);
    }

    /*package-private*/
    @ForceInline
    <M> FloatVector ldOp(M memory, int offset,
                                  FLdOp<M> f) {
        //dummy; no vec = getElements();
        float[] res = new float[length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = f.apply(memory, offset, i);
        }
        return vectorFactory(res);
    }

    /*package-private*/
    @ForceInline
    <M> FloatVector ldOp(M memory, int offset,
                                  VectorMask<Float> m,
                                  FLdOp<M> f) {
        //float[] vec = getElements();
        float[] res = new float[length()];
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
        for (int i = 0; i < res.length; i++) {
            if (mbits[i]) {
                res[i] = f.apply(memory, offset, i);
            }
        }
        return vectorFactory(res);
    }

    interface FStOp<M> {
        void apply(M memory, int offset, int i, float a);
    }

    /*package-private*/
    @ForceInline
    <M> void stOp(M memory, int offset,
                  FStOp<M> f) {
        float[] vec = getElements();
        for (int i = 0; i < vec.length; i++) {
            f.apply(memory, offset, i, vec[i]);
        }
    }

    /*package-private*/
    @ForceInline
    <M> void stOp(M memory, int offset,
                  VectorMask<Float> m,
                  FStOp<M> f) {
        float[] vec = getElements();
        boolean[] mbits = ((AbstractMask<Float>)m).getBits();
        for (int i = 0; i < vec.length; i++) {
            if (mbits[i]) {
                f.apply(memory, offset, i, vec[i]);
            }
        }
    }

    // Binary test

    /*package-private*/
    interface FBinTest {
        boolean apply(int cond, int i, float a, float b);
    }

    /*package-private*/
    @ForceInline
    AbstractMask<Float> bTest(int cond,
                                  Vector<Float> o,
                                  FBinTest f) {
        float[] vec1 = getElements();
        float[] vec2 = ((FloatVector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(cond, i, vec1[i], vec2[i]);
        }
        return maskFactory(bits);
    }

    /*package-private*/
    @ForceInline
    static boolean doBinTest(int cond, float a, float b) {
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
    abstract FloatSpecies vspecies();

    /*package-private*/
    @ForceInline
    static long toBits(float e) {
        return  Float.floatToIntBits(e);
    }

    /*package-private*/
    @ForceInline
    static float fromBits(long bits) {
        return Float.intBitsToFloat((int)bits);
    }

    // Static factories (other than memory operations)

    // Note: A surprising behavior in javadoc
    // sometimes makes a lone /** {@inheritDoc} */
    // comment drop the method altogether,
    // apparently if the method mentions an
    // parameter or return type of Vector<Float>
    // instead of Vector<E> as originally specified.
    // Adding an empty HTML fragment appears to
    // nudge javadoc into providing the desired
    // inherited documentation.  We use the HTML
    // comment <!--workaround--> for this.

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public static FloatVector zero(VectorSpecies<Float> species) {
        FloatSpecies vsp = (FloatSpecies) species;
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
    public abstract FloatVector broadcast(float e);

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
    public static FloatVector broadcast(VectorSpecies<Float> species, float e) {
        FloatSpecies vsp = (FloatSpecies) species;
        return vsp.broadcast(e);
    }

    /*package-private*/
    @ForceInline
    final FloatVector broadcastTemplate(float e) {
        FloatSpecies vsp = vspecies();
        return vsp.broadcast(e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract FloatVector broadcast(long e);

    /**
     * Returns a vector of the given species
     * where all lane elements are set to
     * the primitive value {@code e}.
     *
     * The {@code long} value must be accurately representable
     * by the {@code ETYPE} of the vector species, so that
     * {@code e==(long)(ETYPE)e}.
     *
     * @param species species of the desired vector
     * @param e the value to broadcast
     * @return a vector where all lane elements are set to
     *         the primitive value {@code e}
     * @throws IllegalArgumentException
     *         if the given {@code long} value cannot
     *         be represented by the vector's {@code ETYPE}
     * @see #broadcast(VectorSpecies,float)
     * @see VectorSpecies#checkValue(VectorSpecies,float)
     */
    public static FloatVector broadcast(VectorSpecies<Float> species, long e) {
        FloatSpecies vsp = (FloatSpecies) species;
        return vsp.broadcast(e);
    }

    /*package-private*/
    @ForceInline
    final FloatVector broadcastTemplate(long e) {
        return vspecies().broadcast(e);
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
    public static FloatVector fromValues(VectorSpecies<Float> species, float... es) {
        FloatSpecies vsp = (FloatSpecies) species;
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
    public static final FloatVector single(VectorSpecies<Float> species, float e) {
        return zero(species).withLane(0, e);
    }

    /**
     * Returns a vector where each lane element is set to a randomly
     * generated primitive value.
     *
     * The semantics are equivalent to calling
     * {@link ThreadLocalRandom#nextFloat()}
     * for each lane, from first to last.
     *
     * @param species species of the desired vector
     * @return a vector where each lane elements is set to a randomly
     * generated primitive value
     */
    public static FloatVector random(VectorSpecies<Float> species) {
        FloatSpecies vsp = (FloatSpecies) species;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return vsp.vOp(i -> nextRandom(r));
    }
    private static float nextRandom(ThreadLocalRandom r) {
        return r.nextFloat();
    }

    // Unary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public abstract FloatVector lanewise(VectorOperators.Unary op);

    @ForceInline
    final
    FloatVector lanewiseTemplate(VectorOperators.Unary op) {
        if (opKind(op, VO_SPECIAL)) {
            if (op == ZOMO) {
                return blend(broadcast(-1), compare(NE, 0));
            }
        }
        int opc = opCode(op);
        return VectorIntrinsics.unaryOp(
            opc, getClass(), float.class, length(),
            this,
            UN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_NEG: return v0 ->
                        v0.uOp((i, a) -> (float) -a);
                case VECTOR_OP_ABS: return v0 ->
                        v0.uOp((i, a) -> (float) Math.abs(a));
                case VECTOR_OP_SIN: return v0 ->
                        v0.uOp((i, a) -> (float) Math.sin(a));
                case VECTOR_OP_COS: return v0 ->
                        v0.uOp((i, a) -> (float) Math.cos(a));
                case VECTOR_OP_TAN: return v0 ->
                        v0.uOp((i, a) -> (float) Math.tan(a));
                case VECTOR_OP_ASIN: return v0 ->
                        v0.uOp((i, a) -> (float) Math.asin(a));
                case VECTOR_OP_ACOS: return v0 ->
                        v0.uOp((i, a) -> (float) Math.acos(a));
                case VECTOR_OP_ATAN: return v0 ->
                        v0.uOp((i, a) -> (float) Math.atan(a));
                case VECTOR_OP_EXP: return v0 ->
                        v0.uOp((i, a) -> (float) Math.exp(a));
                case VECTOR_OP_LOG: return v0 ->
                        v0.uOp((i, a) -> (float) Math.log(a));
                case VECTOR_OP_LOG10: return v0 ->
                        v0.uOp((i, a) -> (float) Math.log10(a));
                case VECTOR_OP_SQRT: return v0 ->
                        v0.uOp((i, a) -> (float) Math.sqrt(a));
                case VECTOR_OP_CBRT: return v0 ->
                        v0.uOp((i, a) -> (float) Math.cbrt(a));
                case VECTOR_OP_SINH: return v0 ->
                        v0.uOp((i, a) -> (float) Math.sinh(a));
                case VECTOR_OP_COSH: return v0 ->
                        v0.uOp((i, a) -> (float) Math.cosh(a));
                case VECTOR_OP_TANH: return v0 ->
                        v0.uOp((i, a) -> (float) Math.tanh(a));
                case VECTOR_OP_EXPM1: return v0 ->
                        v0.uOp((i, a) -> (float) Math.expm1(a));
                case VECTOR_OP_LOG1P: return v0 ->
                        v0.uOp((i, a) -> (float) Math.log1p(a));
                default: return null;
              }}));
    }
    private static final
    ImplCache<Unary,UnaryOperator<FloatVector>> UN_IMPL
        = new ImplCache<>(Unary.class, FloatVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public FloatVector lanewise(VectorOperators.Unary op,
                                         VectorMask<Float> m) {
        return blend(lanewise(op), m);
    }

    // Binary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Binary,float)
     */
    @Override
    public abstract FloatVector lanewise(VectorOperators.Binary op,
                                                  Vector<Float> v);
    @ForceInline
    final
    FloatVector lanewiseTemplate(VectorOperators.Binary op,
                                          Vector<Float> v) {
        FloatVector that = (FloatVector) v;
        that.check(this);
        if (opKind(op, VO_SPECIAL )) {
            if (op == FIRST_NONZERO) {
                // FIXME: Support this in the JIT.
                VectorMask<Integer> thisNZ
                    = this.viewAsIntegralLanes().compare(NE, (int) 0);
                that = that.blend((float) 0, thisNZ.cast(vspecies()));
                op = OR_UNCHECKED;
                // FIXME: Support OR_UNCHECKED on float/double also!
                return this.viewAsIntegralLanes()
                    .lanewise(op, that.viewAsIntegralLanes())
                    .viewAsFloatingLanes();
            }
        }
        int opc = opCode(op);
        return VectorIntrinsics.binaryOp(
            opc, getClass(), float.class, length(),
            this, that,
            BIN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_ADD: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)(a + b));
                case VECTOR_OP_SUB: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)(a - b));
                case VECTOR_OP_MUL: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)(a * b));
                case VECTOR_OP_DIV: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)(a / b));
                case VECTOR_OP_MAX: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)Math.max(a, b));
                case VECTOR_OP_MIN: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float)Math.min(a, b));
                case VECTOR_OP_FIRST_NONZERO: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> toBits(a) != 0 ? a : b);
                case VECTOR_OP_OR: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> fromBits(toBits(a) | toBits(b)));
                case VECTOR_OP_ATAN2: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float) Math.atan2(a, b));
                case VECTOR_OP_POW: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float) Math.pow(a, b));
                case VECTOR_OP_HYPOT: return (v0, v1) ->
                        v0.bOp(v1, (i, a, b) -> (float) Math.hypot(a, b));
                default: return null;
                }}));
    }
    private static final
    ImplCache<Binary,BinaryOperator<FloatVector>> BIN_IMPL
        = new ImplCache<>(Binary.class, FloatVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Binary,float,VectorMask)
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Binary op,
                                  Vector<Float> v,
                                  VectorMask<Float> m) {
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
     * @see #lanewise(VectorOperators.Binary,float,VectorMask)
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Binary op,
                                  float e) {
        int opc = opCode(op);
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
     * @see #lanewise(VectorOperators.Binary,float)
     * @see #lanewise(VectorOperators.Binary,Vector,VectorMask)
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Binary op,
                                  float e,
                                  VectorMask<Float> m) {
        return blend(lanewise(op, e), m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Binary op,
                                  long e) {
        float e1 = (float) e;
        if ((long)e1 != e
            ) {
            vspecies().checkValue(e);  // for exception
        }
        return lanewise(op, e1);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Binary op,
                                  long e, VectorMask<Float> m) {
        return blend(lanewise(op, e), m);
    }


    // Ternary lanewise support

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Ternary,float,float)
     */
    @Override
    public abstract FloatVector lanewise(VectorOperators.Ternary op,
                                                  Vector<Float> v1,
                                                  Vector<Float> v2);
    @ForceInline
    final
    FloatVector lanewiseTemplate(VectorOperators.Ternary op,
                                          Vector<Float> v1,
                                          Vector<Float> v2) {
        FloatVector that = (FloatVector) v1;
        FloatVector tother = (FloatVector) v2;
        // It's a word: https://www.dictionary.com/browse/tother
        // See also Chapter 11 of Dickens, Our Mutual Friend:
        // "Totherest Governor," replied Mr Riderhood...
        that.check(this);
        tother.check(this);
        int opc = opCode(op);
        return VectorIntrinsics.ternaryOp(
            opc, getClass(), float.class, length(),
            this, that, tother,
            TERN_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
                case VECTOR_OP_FMA: return (v0, v1_, v2_) ->
                        v0.tOp(v1_, v2_, (i, a, b, c) -> Math.fma(a, b, c));
                default: return null;
                }}));
    }
    private static final
    ImplCache<Ternary,TernaryOperation<FloatVector>> TERN_IMPL
        = new ImplCache<>(Ternary.class, FloatVector.class);

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #lanewise(VectorOperators.Ternary,float,float,VectorMask)
     * @apiNote Since {@code float} vectors do not support
     * any ternary operations, this method always throws an exception.
     * 
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Ternary op,
                                  Vector<Float> v1,
                                  Vector<Float> v2,
                                  VectorMask<Float> m) {
        return blend(lanewise(op, v1, v2), m);
    }

    /**
     * Combines the lane values of this vector
     * with the values of two broadcast scalars.
     * <p>
     * This is a lane-wise ternary operation which applies
     * the selected operation to each lane.
     * The return value will be equal to this expression:
     * {@code this.lanewise(op, this.broadcast(e1), this.broadcast(e2))}.
     *
     * @param e1 the first input scalar
     * @param e2 the second input scalar
     * @return the result of applying the operation lane-wise
     *         to the input vector and the scalars
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #lanewise(VectorOperators.Ternary,Vector,Vector)
     * @see #lanewise(VectorOperators.Ternary,float,float,VectorMask)
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Ternary op,
                                  float e1,
                                  float e2) {
        return lanewise(op, broadcast(e1), broadcast(e1));
    }

    /**
     * Combines the lane values of this vector
     * with the values of two broadcast scalars,
     * with selection of lane elements controlled by a mask.
     * <p>
     * This is a lane-wise ternary operation which applies
     * the selected operation to each lane.
     * The return value will be equal to this expression:
     * {@code this.lanewise(op, this.broadcast(e1), this.broadcast(e2), m)}.
     *
     * @param e1 the first input scalar
     * @param e2 the second input scalar
     * @param m the mask controlling lane selection
     * @return the result of applying the operation lane-wise
     *         to the input vector and the scalars
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #lanewise(VectorOperators.Ternary,float,float)
     * @see #lanewise(VectorOperators.Ternary,Vector,Vector,VectorMask)
     */
    @ForceInline
    public final
    FloatVector lanewise(VectorOperators.Ternary op,
                                  float e1,
                                  float e2,
                                  VectorMask<Float> m) {
        return blend(lanewise(op, e1, e2), m);
    }

    /// FULL-SERVICE BINARY METHODS: ADD, SUB, MUL, DIV
    //
    // These include masked and non-masked versions.
    // This subclass adds broadcast (masked or not).

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #add(float)
     */
    @Override
    public final FloatVector add(Vector<Float> v) {
        return lanewise(ADD, v);
    }

    /**
     * Adds this vector to the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive addition operation ({@code +}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,float)
     *    lanewise}{@code (}{@link VectorOperators#ADD
     *    ADD}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of adding each lane of this vector to the scalar
     * @see #add(Vector)
     * @see #broadcast(float)
     * @see #add(int,VectorMask)
     * @see VectorOperators#ADD
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector add(float e) {
        return lanewise(ADD, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #add(float,VectorMask)
     */
    @Override
    public final FloatVector add(Vector<Float> v,
                                          VectorMask<Float> m) {
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
     * {@link #lanewise(VectorOperators.Binary,float,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#ADD
     *    ADD}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of adding each lane of this vector to the scalar
     * @see #add(Vector,VectorMask)
     * @see #broadcast(float)
     * @see #add(int)
     * @see VectorOperators#ADD
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector add(float e,
                                          VectorMask<Float> m) {
        return lanewise(ADD, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #sub(float)
     */
    @Override
    public final FloatVector sub(Vector<Float> v) {
        return lanewise(SUB, v);
    }

    /**
     * Subtracts an input scalar from this vector.
     *
     * This is a lane-wise binary operation which applies
     * the primitive subtraction operation ({@code -}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,float)
     *    lanewise}{@code (}{@link VectorOperators#SUB
     *    SUB}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of subtracting the scalar from each lane of this vector
     * @see #sub(Vector)
     * @see #broadcast(float)
     * @see #sub(int,VectorMask)
     * @see VectorOperators#SUB
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector sub(float e) {
        return lanewise(SUB, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #sub(float,VectorMask)
     */
    @Override
    public final FloatVector sub(Vector<Float> v,
                                          VectorMask<Float> m) {
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
     * {@link #lanewise(VectorOperators.Binary,float,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#SUB
     *    SUB}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of subtracting the scalar from each lane of this vector
     * @see #sub(Vector,VectorMask)
     * @see #broadcast(float)
     * @see #sub(int)
     * @see VectorOperators#SUB
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector sub(float e,
                                          VectorMask<Float> m) {
        return lanewise(SUB, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #mul(float)
     */
    @Override
    public final FloatVector mul(Vector<Float> v) {
        return lanewise(MUL, v);
    }

    /**
     * Multiplies this vector by the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive multiplication operation ({@code *}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,float)
     *    lanewise}{@code (}{@link VectorOperators#MUL
     *    MUL}{@code , s)}.
     *
     * @param e the input scalar
     * @return the result of multiplying this vector by the given scalar
     * @see #mul(Vector)
     * @see #broadcast(float)
     * @see #mul(int,VectorMask)
     * @see VectorOperators#MUL
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector mul(float e) {
        return lanewise(MUL, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #mul(float,VectorMask)
     */
    @Override
    public final FloatVector mul(Vector<Float> v,
                                          VectorMask<Float> m) {
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
     * {@link #lanewise(VectorOperators.Binary,float,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#MUL
     *    MUL}{@code , s, m)}.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of muling each lane of this vector to the scalar
     * @see #mul(Vector,VectorMask)
     * @see #broadcast(float)
     * @see #mul(int)
     * @see VectorOperators#MUL
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector mul(float e,
                                          VectorMask<Float> m) {
        return lanewise(MUL, e, m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     * @see #div(float)
     * <p> Because the underlying scalar operator is an IEEE
     * floating point number, division by zero in fact will
     * not throw an exception, but will yield a signed
     * infinity or NaN.
     */
    @Override
    public final FloatVector div(Vector<Float> v) {
        return lanewise(DIV, v);
    }

    /**
     * Divides this vector by the broadcast of an input scalar.
     *
     * This is a lane-wise binary operation which applies
     * the primitive division operation ({@code /}) to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Binary,float)
     *    lanewise}{@code (}{@link VectorOperators#DIV
     *    DIV}{@code , s)}.
     *
     * <p>
     * If the underlying scalar operator does not support
     * division by zero, but is presented with a zero divisor,
     * an {@code ArithmeticException} will be thrown.
     * Because the underlying scalar operator is an IEEE
     * floating point number, division by zero in fact will
     * not throw an exception, but will yield a signed
     * infinity or NaN.
     *
     * @param e the input scalar
     * @return the result of dividing each lane of this vector by the scalar
     * @see #div(Vector)
     * @see #broadcast(float)
     * @see #div(int,VectorMask)
     * @see VectorOperators#DIV
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector div(float e) {
        return lanewise(DIV, e);
    }

    /**
    /**
     * {@inheritDoc} <!--workaround-->
     * @see #div(float,VectorMask)
     * <p> Because the underlying scalar operator is an IEEE
     * floating point number, division by zero in fact will
     * not throw an exception, but will yield a signed
     * infinity or NaN.
     */
    @Override
    public final FloatVector div(Vector<Float> v,
                                          VectorMask<Float> m) {
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
     * {@link #lanewise(VectorOperators.Binary,float,VectorMask)
     *    lanewise}{@code (}{@link VectorOperators#DIV
     *    DIV}{@code , s, m)}.
     *
     * <p>
     * If the underlying scalar operator does not support
     * division by zero, but is presented with a zero divisor,
     * an {@code ArithmeticException} will be thrown.
     * Because the underlying scalar operator is an IEEE
     * floating point number, division by zero in fact will
     * not throw an exception, but will yield a signed
     * infinity or NaN.
     *
     * @param e the input scalar
     * @param m the mask controlling lane selection
     * @return the result of dividing each lane of this vector by the scalar
     * @see #div(Vector,VectorMask)
     * @see #broadcast(float)
     * @see #div(int)
     * @see VectorOperators#DIV
     * @see #lanewise(VectorOperators.Binary,Vector)
     * @see #lanewise(VectorOperators.Binary,float)
     */
    public final FloatVector div(float e,
                                          VectorMask<Float> m) {
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
    public final FloatVector min(Vector<Float> v) {
        return lanewise(MIN, v);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final FloatVector max(Vector<Float> v) {
        return lanewise(MAX, v);
    }

    /// UNARY OPERATIONS

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    FloatVector neg() {
        return lanewise(NEG);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    FloatVector abs() {
        return lanewise(ABS);
    }

    /// COMPARISONS

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    VectorMask<Float> eq(Vector<Float> v) {
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
     * @see #compare(VectorOperators.Comparison, float)
     */
    public final
    VectorMask<Float> eq(float e) {
        return compare(EQ, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public final
    VectorMask<Float> lt(Vector<Float> v) {
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
     * @see #compare(VectorOperators.Comparison, float)
     */
    public final
    VectorMask<Float> lt(float e) {
        return compare(LT, e);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract
    VectorMask<Float> compare(VectorOperators.Comparison op, Vector<Float> v);

    /*package-private*/
    final
    <M extends VectorMask<Float>>
    M compareTemplate(Class<M> maskType, Comparison op, Vector<Float> v) {
        Objects.requireNonNull(v);
        FloatSpecies vsp = vspecies();
        int opc = opCode(op);
        return VectorIntrinsics.compare(
            opc, getClass(), maskType, float.class, length(),
            this, (FloatVector) v,
            (cond, v0, v1) -> {
                AbstractMask<Float> m
                    = v0.bTest(cond, v1, (cond_, i, a, b)
                               -> compareWithOp(cond, a, b));
                @SuppressWarnings("unchecked")
                M m2 = (M) m;
                return m2;
            });
    }

    private static
    boolean compareWithOp(int cond, float a, float b) {
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
     * @see #eq(float)
     * @see #lessThan(float)
     */
    public abstract
    VectorMask<Float> compare(Comparison op, float e);

    /*package-private*/
    final
    <M extends VectorMask<Float>>
    M compareTemplate(Class<M> maskType, Comparison op, float e) {
        return compareTemplate(maskType, op, broadcast(e));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    VectorMask<Float> compare(Comparison op, long e);

    /*package-private*/
    final
    <M extends VectorMask<Float>>
    M compareTemplate(Class<M> maskType, Comparison op, long e) {
        return compareTemplate(maskType, op, broadcast(e));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    FloatVector blend(Vector<Float> v, VectorMask<Float> m);

    /*package-private*/
    @ForceInline
    <M extends VectorMask<Float>>
    FloatVector
    blendTemplate(Class<M> maskType, FloatVector v, M m) {
        v.check(this);
        return VectorIntrinsics.blend(
            getClass(), maskType, float.class, length(),
            this, v, m,
            (v0, v1, m_) -> v0.bOp(v1, m_, (i, a, b) -> b));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract FloatVector addIndex(int scale);

    /*package-private*/
    @ForceInline
    final FloatVector addIndexTemplate(int scale) {
        FloatSpecies vsp = vspecies();
        // make sure VLENGTH*scale doesn't overflow:
        vsp.checkScale(scale);
        return VectorIntrinsics.indexVector(
            getClass(), float.class, length(),
            this, scale, vsp,
            (v, scale_, s)
            -> {
                // If the platform doesn't support an INDEX
                // instruction directly, load IOTA from memory
                // and multiply.
                FloatVector iota = s.iota();
                float sc = (float) scale_;
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
    public final FloatVector blend(float e,
                                            VectorMask<Float> m) {
        return blend(broadcast(e), m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    FloatVector slice(int origin, Vector<Float> v1);

    /*package-private*/
    final
    @ForceInline
    FloatVector sliceTemplate(int origin, Vector<Float> v1) {
        FloatVector that = (FloatVector) v1;
        that.check(this);
        float[] a0 = this.getElements();
        float[] a1 = that.getElements();
        float[] res = new float[a0.length];
        int vlen = res.length;
        int firstPart = vlen - origin;
        System.arraycopy(a0, origin, res, 0, firstPart);
        System.arraycopy(a1, 0, res, firstPart, origin);
        return vectorFactory(res);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public final FloatVector
    slice(int origin, Vector<Float> w, VectorMask<Float> m) {
        return broadcast(0).blend(slice(origin, w), m);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public final FloatVector slice(int origin) {
        return slice(origin, broadcast(0));
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract
    FloatVector unslice(int origin, Vector<Float> w, int part);

    /*package-private*/
    final
    @ForceInline
    FloatVector
    unsliceTemplate(int origin, Vector<Float> w, int part) {
        FloatVector that = (FloatVector) w;
        that.check(this);
        float[] slice = this.getElements();
        float[] res = that.getElements();
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
    <M extends VectorMask<Float>>
    FloatVector
    unsliceTemplate(Class<M> maskType, int origin, Vector<Float> w, int part, M m) {
        FloatVector that = (FloatVector) w;
        that.check(this);
        FloatVector slice = that.sliceTemplate(origin, that);
        slice = slice.blendTemplate(maskType, this, m);
        return slice.unsliceTemplate(origin, w, part);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override public abstract FloatVector
    unslice(int origin, Vector<Float> w, int part, VectorMask<Float> m);

    /**
     * {@inheritDoc} <!--workaround-->
     */
    public final FloatVector unslice(int origin) {
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
    public abstract FloatVector rearrange(VectorShuffle<Float> m);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Float>>
    FloatVector rearrangeTemplate(Class<S> shuffletype, S shuffle) {
        shuffle.checkIndexes();
        return VectorIntrinsics.rearrangeOp(
            getClass(), shuffletype, float.class, length(),
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
    public abstract FloatVector rearrange(VectorShuffle<Float> s,
                                                   VectorMask<Float> m);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Float>>
    FloatVector rearrangeTemplate(Class<S> shuffletype,
                                           S shuffle,
                                           VectorMask<Float> m) {
        FloatVector unmasked =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, float.class, length(),
                this, shuffle,
                (v1, s_) -> v1.uOp((i, a) -> {
                    int ei = s_.laneSource(i);
                    return ei < 0 ? 0 : v1.lane(ei);
                }));
        VectorMask<Float> valid = shuffle.laneIsValid();
        if (m.andNot(valid).anyTrue()) {
            shuffle.checkIndexes();
            throw new AssertionError();
        }
        return broadcast((float)0).blend(unmasked, valid);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract FloatVector rearrange(VectorShuffle<Float> s,
                                                   Vector<Float> v);

    /*package-private*/
    @ForceInline
    <S extends VectorShuffle<Float>>
    FloatVector rearrangeTemplate(Class<S> shuffletype,
                                           S shuffle,
                                           FloatVector v) {
        VectorMask<Float> valid = shuffle.laneIsValid();
        VectorShuffle<Float> ws = shuffle.wrapIndexes();
        FloatVector r1 =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, float.class, length(),
                this, shuffle,
                (v1, s_) -> v1.uOp((i, a) -> {
                    int ei = s_.laneSource(i);
                    return v1.lane(ei);
                }));
        FloatVector r2 =
            VectorIntrinsics.rearrangeOp(
                getClass(), shuffletype, float.class, length(),
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
    public abstract FloatVector selectFrom(Vector<Float> v);

    /*package-private*/
    @ForceInline
    final FloatVector selectFromTemplate(FloatVector v) {
        return v.rearrange(this.toShuffle());
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @Override
    public abstract FloatVector selectFrom(Vector<Float> s, VectorMask<Float> m);

    /*package-private*/
    @ForceInline
    final FloatVector selectFromTemplate(FloatVector v,
                                                  AbstractMask<Float> m) {
        return v.rearrange(this.toShuffle(), m);
    }

    /// FMA

    /**
     * Multiplies this vector by a second input vector, and sums
     * the result with a third.
     *
     * Extended precision is used for the intermediate result,
     * avoiding possible loss of precision from rounding once
     * for each of the two operations.
     * The result is numerically close to {@code this.mul(v1).add(v2)},
     * and is typically closer to the true mathematical result.
     * <p>
     * This is a lane-wise ternary operation which applies the
     * {@link Math#fma(float,float,float) Math#fma(a,b,c)}
     * operation to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Ternary,Vector,Vector)
     *    lanewise}{@code (}{@link VectorOperators#FMA
     *    FMA}{@code , v1, v2)}.
     *
     * @param v1 the second input vector, supplying multiplier values
     * @param v2 the third input vector, supplying addend values
     * @return the product of this vector and the second input vector
     *         summed with the third input vector, using extended precision
     *         for the intermediate result
     */
    public final
    FloatVector fma(Vector<Float> v1, Vector<Float> v2) {
        return lanewise(FMA, v1, v2);
    }

    /**
     * Multiplies this vector by a scalar multiplier, and sums
     * the result with a scalar addend.
     *
     * Extended precision is used for the intermediate result,
     * avoiding possible loss of precision from rounding once
     * for each of the two operations.
     * The result is numerically close to {@code this.mul(s1).add(s2)},
     * and is typically closer to the true mathematical result.
     * <p>
     * This is a lane-wise ternary operation which applies the
     * {@link Math#fma(float,float,float) Math#fma(a,b,c)}
     * operation to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Ternary,Vector,Vector)
     *    lanewise}{@code (}{@link VectorOperators#FMA
     *    FMA}{@code , {@link #broadcast(float)
     *    broadcast}(s1), {@link #broadcast(float)
     *    broadcast}(s2))}.
     *
     * @param s1 the scalar multiplier
     * @param s2 the scalar addend
     * @return the product of this vector and the scalar multiplier
     *         summed with scalar addend, using extended precision
     *         for the intermediate result
     */
    public final
    FloatVector fma(float s1, float s2) {
        return lanewise(FMA, s1, s2);
    }

    /**
     * Multiplies this vector by a second input vector, and sums
     * the result with a third,
     * under the control of a mask.
     *
     * Extended precision is used for the intermediate result,
     * avoiding possible loss of precision from rounding once
     * for each of the two operations.
     * The result is numerically close to {@code this.mul(v1,m).add(v2,m)},
     * and is typically closer to the true mathematical result.
     * <p>
     * This is a lane-wise ternary operation which applies the
     * {@link Math#fma(float,float,float) Math#fma(a,b,c)}
     * operation to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Ternary,Vector,Vector)
     *    lanewise}{@code (}{@link VectorOperators#FMA
     *    FMA}{@code , v1, v2, m)}.
     *
     * @param v1 the second input vector, supplying multiplier values
     * @param v2 the third input vector, supplying addend values
     * @param m the mask controlling lane selection
     * @return the product of this vector and the second input vector
     *         summed with the third input vector, using extended precision
     *         for the intermediate result
     */
    public final FloatVector
    fma(Vector<Float> v1, Vector<Float> v2, VectorMask<Float> m) {
        return lanewise(FMA, v1, v2, m);
    }

    /**
     * Multiplies this vector by a scalar multiplier, and sums
     * the result with a scalar addend,
     * under the control of a mask.
     *
     * Extended precision is used for the intermediate result,
     * avoiding possible loss of precision from rounding once
     * for each of the two operations.
     * The result is numerically close to {@code this.mul(s1,m).add(s2,m)},
     * and is typically closer to the true mathematical result.
     * <p>
     * This is a lane-wise ternary operation which applies the
     * {@link Math#fma(float,float,float) Math#fma(a,b,c)}
     * operation to each lane.
     *
     * This method is also equivalent to the expression
     * {@link #lanewise(VectorOperators.Ternary,Vector,Vector)
     *    lanewise}{@code (}{@link VectorOperators#FMA
     *    FMA}{@code , {@link #broadcast(float)
     *    broadcast}(s1), {@link #broadcast(float)
     *    broadcast}(s2), m)}.
     *
     * @param s1 the scalar multiplier
     * @param s2 the scalar addend
     * @param m the mask controlling lane selection
     * @return the product of this vector and the scalar multiplier
     *         summed with scalar addend, using extended precision
     *         for the intermediate result
     */
    public final
    FloatVector fma(float s1, float s2, VectorMask<Float> m) {
        return lanewise(FMA, s1, s2, m);
    }


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
     * @implNote
     * The value of a floating-point reduction may be a function
     * both of the input values as well as the order of scalar
     * operations which combine those values, specifically in the
     * case of {@code ADD} and {@code MUL} operations, where
     * details of rounding depend on operand order.
     * In those cases, the order of operations of this method is
     * intentionally not defined.  This allows the JVM to generate
     * optimal machine code for the underlying platform at runtime. If
     * the platform supports a vector instruction to add or multiply
     * all values in the vector, or if there is some other efficient
     * machine code sequence, then the JVM has the option of
     * generating this machine code. Otherwise, the default
     * implementation is applied, which adds vector elements
     * sequentially from beginning to end.  For this reason, the
     * output of this method may vary for the same input values,
     * if the selected operator is {@code ADD} or {@code MUL}.
     *
     *
     * @param op the operation used to combine lane values
     * @return the accumulated result
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #reduceLanes(VectorOperators.Associative,VectorMask)
     */
    public abstract float reduceLanes(VectorOperators.Associative op);

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
     * then the identity value is zero, the default {@code float} value.
     * <li>
     * If the operation is {@code MUL},
     * then the identity value is one.
     * <li>
     * If the operation is {@code MAX},
     * then the identity value is {@code Float.NEGATIVE_INFINITY}.
     * <li>
     * If the operation is {@code MIN},
     * then the identity value is {@code Float.POSITIVE_INFINITY}.
     * </ul>
     *
     * @implNote
     * The value of a floating-point reduction may be a function
     * both of the input values as well as the order of scalar
     * operations which combine those values, specifically in the
     * case of {@code ADD} and {@code MUL} operations, where
     * details of rounding depend on operand order.
     * See {@linkplain #reduceLanes(VectorOperators.Associative)
     * the unmasked version of this method}
     * for a discussion.
     *
     *
     * @param op the operation used to combine lane values
     * @param m the mask controlling lane selection
     * @return the reduced result accumulated from the selected lane values
     * @throws UnsupportedOperationException if this vector does
     *         not support the requested operation
     * @see #reduceLanes(VectorOperators.Associative)
     */
    public abstract float reduceLanes(VectorOperators.Associative op,
                                       VectorMask<Float> m);

    /*package-private*/
    @ForceInline
    float reduceLanesTemplate(VectorOperators.Associative op,
                               VectorMask<Float> m) {
        FloatVector v = reduceIdentityVector(op).blend(this, m);
        return v.reduceLanesTemplate(op);
    }

    /*package-private*/
    @ForceInline
    float reduceLanesTemplate(VectorOperators.Associative op) {
        if (op == FIRST_NONZERO) {
            // FIXME:  The JIT should handle this, and other scan ops alos.
            VectorMask<Integer> thisNZ
                = this.viewAsIntegralLanes().compare(NE, (int) 0);
            return this.lane(thisNZ.firstTrue());
        }
        int opc = opCode(op);
        return fromBits(VectorIntrinsics.reductionCoerced(
            opc, getClass(), float.class, length(),
            this,
            REDUCE_IMPL.find(op, opc, (opc_) -> {
              switch (opc_) {
              case VECTOR_OP_ADD: return v ->
                      toBits(v.rOp((float)0, (i, a, b) -> (float)(a + b)));
              case VECTOR_OP_MUL: return v ->
                      toBits(v.rOp((float)1, (i, a, b) -> (float)(a * b)));
              case VECTOR_OP_MIN: return v ->
                      toBits(v.rOp(MAX_OR_INF, (i, a, b) -> (float) Math.min(a, b)));
              case VECTOR_OP_MAX: return v ->
                      toBits(v.rOp(MIN_OR_INF, (i, a, b) -> (float) Math.max(a, b)));
              case VECTOR_OP_FIRST_NONZERO: return v ->
                      toBits(v.rOp((float)0, (i, a, b) -> toBits(a) != 0 ? a : b));
              case VECTOR_OP_OR: return v ->
                      toBits(v.rOp((float)0, (i, a, b) -> fromBits(toBits(a) | toBits(b))));
              default: return null;
              }})));
    }
    private static final
    ImplCache<Associative,Function<FloatVector,Long>> REDUCE_IMPL
        = new ImplCache<>(Associative.class, FloatVector.class);

    private
    @ForceInline
    FloatVector reduceIdentityVector(VectorOperators.Associative op) {
        int opc = opCode(op);
        UnaryOperator<FloatVector> fn
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
    ImplCache<Associative,UnaryOperator<FloatVector>> REDUCE_ID_IMPL
        = new ImplCache<>(Associative.class, FloatVector.class);

    private static final float MIN_OR_INF = Float.NEGATIVE_INFINITY;
    private static final float MAX_OR_INF = Float.POSITIVE_INFINITY;

    public @Override abstract long reduceLanesToLong(VectorOperators.Associative op);
    public @Override abstract long reduceLanesToLong(VectorOperators.Associative op,
                                                     VectorMask<Float> m);

    // Type specific accessors

    /**
     * Gets the lane element at lane index {@code i}
     *
     * @param i the lane index
     * @return the lane element at lane index {@code i}
     * @throws IllegalArgumentException if the index is is out of range
     * ({@code < 0 || >= length()})
     */
    public abstract float lane(int i);

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
    public abstract FloatVector withLane(int i, float e);

    // Memory load operations

    /**
     * Returns an array of type {@code float[]}
     * containing all the lane values.
     * The array length is the same as the vector length.
     * The array elements are stored in lane order.
     * <p>
     * This method behaves as if it stores
     * this vector into an allocated array
     * (using {@link #intoArray(float[], int) intoArray})
     * and returns the array as follows:
     * <pre>{@code
     *   float[] a = new float[this.length()];
     *   this.intoArray(a, 0);
     *   return a;
     * }</pre>
     *
     * @return an array containing the lane values of this vector
     */
    @ForceInline
    @Override
    public final float[] toArray() {
        float[] a = new float[vspecies().laneCount()];
        intoArray(a, 0);
        return a;
    }

    /** {@inheritDoc} <!--workaround-->
     * @implNote
     * When this method is used on used on vectors
     * of type FloatVector,
     * fractional bits in lane values will be lost,
     * and lane values of large magnitude will be
     * clipped to {@code Long.MAX_VALUE} or
     * {@code Long.MIN_VALUE}.
     */
    @ForceInline
    @Override
    public final long[] toLongArray() {
        float[] a = toArray();
        long[] res = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = (long) a[i];
        }
        return res;
    }

    /** {@inheritDoc} <!--workaround-->
     * @implNote
     * When this method is used on used on vectors
     * of type FloatVector,
     * there will be no loss of precision.
     */
    @ForceInline
    @Override
    public final double[] toDoubleArray() {
        float[] a = toArray();
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
    FloatVector fromByteArray(VectorSpecies<Float> species,
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
    FloatVector fromByteArray(VectorSpecies<Float> species,
                                       byte[] a, int offset,
                                       ByteOrder bo) {
        FloatSpecies vsp = (FloatSpecies) species;
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
     * value of float (zero).
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
    FloatVector fromByteArray(VectorSpecies<Float> species,
                                       byte[] a, int offset,
                                       VectorMask<Float> m) {
        return fromByteArray(species, a, offset, ByteOrder.LITTLE_ENDIAN, m);
    }

    /**
     * Loads a vector from a byte array starting at an offset
     * and using a mask.
     * Lanes where the mask is unset are filled with the default
     * value of float (zero).
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
    FloatVector fromByteArray(VectorSpecies<Float> species,
                                       byte[] a, int offset,
                                       ByteOrder bo,
                                       VectorMask<Float> m) {
        FloatSpecies vsp = (FloatSpecies) species;
        FloatVector zero = vsp.zero();
        FloatVector iota = zero.addIndex(1);
        ((AbstractMask<Float>)m)
            .checkIndexByLane(offset, a.length, iota, 4);
        FloatVector v = zero.fromByteArray0(a, offset);
        return zero.blend(v.maybeSwap(bo), m);
    }

    /**
     * Loads a vector from an array of type {@code float[]}
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
    FloatVector fromArray(VectorSpecies<Float> species,
                                   float[] a, int offset) {
        FloatSpecies vsp = (FloatSpecies) species;
        offset = checkFromIndexSize(offset,
                                    vsp.laneCount(),
                                    a.length);
        return vsp.dummyVector().fromArray0(a, offset);
    }

    /**
     * Loads a vector from an array of type {@code float[]}
     * starting at an offset and using a mask.
     * Lanes where the mask is unset are filled with the default
     * value of float (zero).
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
    FloatVector fromArray(VectorSpecies<Float> species,
                                   float[] a, int offset,
                                   VectorMask<Float> m) {
        FloatSpecies vsp = (FloatSpecies) species;
        FloatVector zero = vsp.zero();
        FloatVector iota = vsp.iota();
        ((AbstractMask<Float>)m)
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
    FloatVector fromArray(VectorSpecies<Float> species,
                                   float[] a, int offset,
                                   int[] indexMap, int mapOffset) {
        FloatSpecies vsp = (FloatSpecies) species;
        Objects.requireNonNull(a);
        Objects.requireNonNull(indexMap);
        Class<? extends FloatVector> vectorType = vsp.vectorType();


        // Index vector: vix[0:n] = k -> offset + indexMap[mapOffset + k]
        IntVector vix = IntVector.fromArray(IntVector.species(vsp.indexShape()), indexMap, mapOffset).add(offset);

        vix = VectorIntrinsics.checkIndex(vix, a.length);

        return VectorIntrinsics.loadWithMap(
            vectorType, float.class, vsp.laneCount(),
            IntVector.species(vsp.indexShape()).vectorType(),
            a, ARRAY_BASE, vix,
            a, offset, indexMap, mapOffset, vsp,
            (float[] c, int idx, int[] iMap, int idy, FloatSpecies s) ->
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
    FloatVector fromArray(VectorSpecies<Float> species,
                                   float[] a, int offset,
                                   int[] indexMap, int mapOffset,
                                   VectorMask<Float> m) {
        FloatSpecies vsp = (FloatSpecies) species;

        // FIXME This can result in out of bounds errors for unset mask lanes
        // FIX = Use a scatter instruction which routes the unwanted lanes
        // into a bit-bucket variable (private to implementation).
        // This requires a 2-D scatter in order to set a second base address.
        // See notes in https://bugs.openjdk.java.net/browse/JDK-8223367
        assert(m.allTrue());
        return (FloatVector)
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
     *         if {@code offset+N*4 < 0}
     *         or {@code offset+N**4 >= bb.limit()}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public static
    FloatVector fromByteBuffer(VectorSpecies<Float> species,
                                        ByteBuffer bb, int offset,
                                        ByteOrder bo) {
        FloatSpecies vsp = (FloatSpecies) species;
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
     *         if {@code offset+N*4 < 0}
     *         or {@code offset+N**4 >= bb.limit()}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public static
    FloatVector fromByteBuffer(VectorSpecies<Float> species,
                                        ByteBuffer bb, int offset,
                                        ByteOrder bo,
                                        VectorMask<Float> m) {
        if (m.allTrue()) {
            return fromByteBuffer(species, bb, offset, bo);
        }
        FloatSpecies vsp = (FloatSpecies) species;
        checkMaskFromIndexSize(offset,
                               vsp, m, 1,
                               bb.limit());
        FloatVector zero = zero(vsp);
        FloatVector v = zero.fromByteBuffer0(bb, offset);
        return zero.blend(v.maybeSwap(bo), m);
    }

    // Memory store operations

    /**
     * Stores this vector into an array of type {@code float[]}
     * starting at an offset.
     * <p>
     * For each vector lane, where {@code N} is the vector lane index,
     * the lane element at index {@code N} is stored into the array
     * element {@code a[offset+N]}.
     *
     * @param a the array, of type {@code float[]}
     * @param offset the offset into the array
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     */
    @ForceInline
    public void intoArray(float[] a, int offset) {
        FloatSpecies vsp = vspecies();
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
     * Stores this vector into an array of float
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
     * @param a the array, of type {@code float[]}
     * @param offset the offset into the array
     * @param m the mask controlling lane storage
     * @throws IndexOutOfBoundsException
     *         if {@code offset+N < 0} or {@code offset+N >= a.length}
     *         for any lane {@code N} in the vector
     *         where the mask is set
     */
    @ForceInline
    public final void intoArray(float[] a, int offset,
                                VectorMask<Float> m) {
        if (m.allTrue()) {
            intoArray(a, offset);
        } else {
            // FIXME: Cannot vectorize yet, if there's a mask.
            stOp(a, offset, m, (arr, off, i, v) -> arr[off+i] = v);
        }
    }

    /**
     * Stores this vector into an array of type {@code float[]}
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
    public void intoArray(float[] a, int offset,
                          int[] indexMap, int mapOffset) {
        FloatSpecies vsp = vspecies();
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
     * Stores this vector into an array of type {@code float[]}
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
    public final void intoArray(float[] a, int offset,
                                int[] indexMap, int mapOffset,
                                VectorMask<Float> m) {
        FloatSpecies vsp = vspecies();
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
                                    VectorMask<Float> m) {
        if (m.allTrue()) {
            intoByteArray(a, offset);
            return;
        }
        FloatSpecies vsp = vspecies();
        checkMaskFromIndexSize(offset, vsp, m, 4, a.length);
        conditionalStoreNYI(offset, vsp, m, 4, a.length);
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
                                    VectorMask<Float> m) {
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
                               VectorMask<Float> m) {
        if (m.allTrue()) {
            intoByteBuffer(bb, offset, bo);
            return;
        }
        FloatSpecies vsp = vspecies();
        checkMaskFromIndexSize(offset, vsp, m, 4, bb.limit());
        conditionalStoreNYI(offset, vsp, m, 4, bb.limit());
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
    FloatVector fromArray0(float[] a, int offset) {
        FloatSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, arrayAddress(a, offset),
            a, offset, vsp,
            (arr, off, s) -> s.ldOp(arr, off,
                                    (arr_, off_, i) -> arr_[off_ + i]));
    }

    @ForceInline
    @Override
    FloatVector fromByteArray0(byte[] a, int offset) {
        FloatSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, byteArrayAddress(a, offset),
            a, offset, vsp,
            (arr, off, s) -> {
                FloatBuffer tb = wrapper(arr, off, NATIVE_ENDIAN);
                return s.ldOp(tb, 0, (tb_, __, i) -> tb_.get(i));
            });
    }

    @ForceInline
    FloatVector fromByteBuffer0(ByteBuffer bb, int offset) {
        FloatSpecies vsp = vspecies();
        return VectorIntrinsics.load(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            bufferBase(bb), bufferAddress(bb, offset),
            bb, offset, vsp,
            (buf, off, s) -> {
                FloatBuffer tb = wrapper(buf, off, NATIVE_ENDIAN);
                return s.ldOp(tb, 0, (tb_, __, i) -> tb_.get(i));
           });
    }

    // Unchecked storing operations in native byte order.
    // Caller is reponsible for applying index checks, masking, and
    // byte swapping.

    @ForceInline
    void intoArray0(float[] a, int offset) {
        FloatSpecies vsp = vspecies();
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
        FloatSpecies vsp = vspecies();
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            a, byteArrayAddress(a, offset),
            this, a, offset,
            (arr, off, v) -> {
                FloatBuffer tb = wrapper(arr, off, NATIVE_ENDIAN);
                v.stOp(tb, 0, (tb_, __, i, e) -> tb_.put(i, e));
            });
    }

    @ForceInline
    void intoByteBuffer0(ByteBuffer bb, int offset) {
        FloatSpecies vsp = vspecies();
        VectorIntrinsics.store(
            vsp.vectorType(), vsp.elementType(), vsp.laneCount(),
            bufferBase(bb), bufferAddress(bb, offset),
            this, bb, offset,
            (buf, off, v) -> {
                FloatBuffer tb = wrapper(buf, off, NATIVE_ENDIAN);
                v.stOp(tb, 0, (tb_, __, i, e) -> tb_.put(i, e));
            });
    }

    // End of low-level memory operations.

    private static
    void checkMaskFromIndexSize(int offset,
                                FloatSpecies vsp,
                                VectorMask<Float> m,
                                int scale,
                                int limit) {
        ((AbstractMask<Float>)m)
            .checkIndexByLane(offset, limit, vsp.iota(), scale);
    }

    private void conditionalStoreNYI(int offset,
                                     FloatSpecies vsp,
                                     VectorMask<Float> m,
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
    FloatVector maybeSwap(ByteOrder bo) {
        if (bo != NATIVE_ENDIAN) {
            return this.reinterpretAsBytes()
                .rearrange(swapBytesShuffle())
                .reinterpretAsFloats();
        }
        return this;
    }

    static final int ARRAY_SHIFT =
        31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_FLOAT_INDEX_SCALE);
    static final long ARRAY_BASE =
        Unsafe.ARRAY_FLOAT_BASE_OFFSET;

    @ForceInline
    static long arrayAddress(float[] a, int index) {
        return ARRAY_BASE + (((long)index) << ARRAY_SHIFT);
    }

    @ForceInline
    static long byteArrayAddress(byte[] a, int index) {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + index;
    }

    // Byte buffer wrappers.
    private static FloatBuffer wrapper(ByteBuffer bb, int offset,
                                        ByteOrder bo) {
        return bb.duplicate().position(offset).slice()
            .order(bo).asFloatBuffer();
    }
    private static FloatBuffer wrapper(byte[] a, int offset,
                                        ByteOrder bo) {
        return ByteBuffer.wrap(a, offset, a.length - offset)
            .order(bo).asFloatBuffer();
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
    public final IntVector viewAsIntegralLanes() {
        LaneType ilt = LaneType.FLOAT.asIntegral();
        return (IntVector) asVectorRaw(ilt);
    }

    /**
     * {@inheritDoc} <!--workaround-->
     */
    @ForceInline
    @Override
    public final
    FloatVector
    viewAsFloatingLanes() {
        return this;
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
     * java.util.Arrays#toString(float[]) the {@code Arrays.toString}
     * method} appropriate to the float array returned by
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
     * Class representing {@link FloatVector}'s of the same {@link VectorShape VectorShape}.
     */
    /*package-private*/
    static final class FloatSpecies extends AbstractSpecies<Float> {
        private FloatSpecies(VectorShape shape,
                Class<? extends FloatVector> vectorType,
                Class<? extends AbstractMask<Float>> maskType,
                Function<Object, FloatVector> vectorFactory) {
            super(shape, LaneType.of(float.class),
                  vectorType, maskType,
                  vectorFactory);
            assert(this.elementSize() == Float.SIZE);
        }

        // Specializing overrides:

        @Override
        @ForceInline
        public final Class<Float> elementType() {
            return float.class;
        }

        @Override
        @ForceInline
        public final Class<Float> genericElementType() {
            return Float.class;
        }

        @Override
        @ForceInline
        public final Class<float[]> arrayType() {
            return float[].class;
        }

        @SuppressWarnings("unchecked")
        @Override
        @ForceInline
        public final Class<? extends FloatVector> vectorType() {
            return (Class<? extends FloatVector>) vectorType;
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
        final FloatVector broadcastBits(long bits) {
            return (FloatVector)
                VectorIntrinsics.broadcastCoerced(
                    vectorType, float.class, laneCount,
                    bits, this,
                    (bits_, s_) -> s_.rvOp(i -> bits_));
        }

        /*package-private*/
        @ForceInline
        
        final FloatVector broadcast(float e) {
            return broadcastBits(toBits(e));
        }

        @Override
        @ForceInline
        public final FloatVector broadcast(long e) {
            return broadcastBits(longToElementBits(e));
        }

        /*package-private*/
        final @Override
        @ForceInline
        long longToElementBits(long value) {
            // Do the conversion, and then test it for failure.
            float e = (float) value;
            if ((long) e != value) {
                throw badElementBits(value, e);
            }
            return toBits(e);
        }

        @Override
        @ForceInline
        public final FloatVector fromValues(long... values) {
            VectorIntrinsics.requireLength(values.length, laneCount);
            float[] va = new float[laneCount()];
            for (int i = 0; i < va.length; i++) {
                long lv = values[i];
                float v = (float) lv;
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
        final FloatVector fromIntValues(int[] values) {
            VectorIntrinsics.requireLength(values.length, laneCount);
            float[] va = new float[laneCount()];
            for (int i = 0; i < va.length; i++) {
                int lv = values[i];
                float v = (float) lv;
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
        public FloatVector fromArray(Object a, int offset) {
            // User entry point:  Be careful with inputs.
            return FloatVector
                .fromArray(this, (float[]) a, offset);
        }

        @Override final
        FloatVector dummyVector() {
            return (FloatVector) super.dummyVector();
        }

        final
        FloatVector vectorFactory(float[] vec) {
            // Species delegates all factory requests to its dummy
            // vector.  The dummy knows all about it.
            return dummyVector().vectorFactory(vec);
        }

        /*package-private*/
        final @Override
        @ForceInline
        FloatVector rvOp(RVOp f) {
            float[] res = new float[laneCount()];
            for (int i = 0; i < res.length; i++) {
                int bits = (int) f.apply(i);
                res[i] = fromBits(bits);
            }
            return dummyVector().vectorFactory(res);
        }

        FloatVector vOp(FVOp f) {
            float[] res = new float[laneCount()];
            for (int i = 0; i < res.length; i++) {
                res[i] = f.apply(i);
            }
            return dummyVector().vectorFactory(res);
        }

        FloatVector vOp(VectorMask<Float> m, FVOp f) {
            float[] res = new float[laneCount()];
            boolean[] mbits = ((AbstractMask<Float>)m).getBits();
            for (int i = 0; i < res.length; i++) {
                if (mbits[i]) {
                    res[i] = f.apply(i);
                }
            }
            return dummyVector().vectorFactory(res);
        }

        /*package-private*/
        @ForceInline
        <M> FloatVector ldOp(M memory, int offset,
                                      FLdOp<M> f) {
            return dummyVector().ldOp(memory, offset, f);
        }

        /*package-private*/
        @ForceInline
        <M> FloatVector ldOp(M memory, int offset,
                                      AbstractMask<Float> m,
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
                      AbstractMask<Float> m,
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
        public final FloatVector zero() {
            if ((Class<?>) vectorType() == FloatMaxVector.class)
                return FloatMaxVector.ZERO;
            switch (vectorBitSize()) {
                case 64: return Float64Vector.ZERO;
                case 128: return Float128Vector.ZERO;
                case 256: return Float256Vector.ZERO;
                case 512: return Float512Vector.ZERO;
            }
            throw new AssertionError();
        }        

        @Override
        @ForceInline
        public final FloatVector iota() {
            if ((Class<?>) vectorType() == FloatMaxVector.class)
                return FloatMaxVector.IOTA;
            switch (vectorBitSize()) {
                case 64: return Float64Vector.IOTA;
                case 128: return Float128Vector.IOTA;
                case 256: return Float256Vector.IOTA;
                case 512: return Float512Vector.IOTA;
            }
            throw new AssertionError();
        }

        // Mask access
        @Override
        @ForceInline
        public final VectorMask<Float> maskAll(boolean bit) {
            if ((Class<?>) vectorType() == FloatMaxVector.class)
                return FloatMaxVector.FloatMaxMask.maskAll(bit);
            switch (vectorBitSize()) {
                case 64: return Float64Vector.Float64Mask.maskAll(bit);
                case 128: return Float128Vector.Float128Mask.maskAll(bit);
                case 256: return Float256Vector.Float256Mask.maskAll(bit);
                case 512: return Float512Vector.Float512Mask.maskAll(bit);
            }
            throw new AssertionError();
        }
    }

    /**
     * Finds a species for an element type of {@code float} and shape.
     *
     * @param s the shape
     * @return a species for an element type of {@code float} and shape
     * @throws IllegalArgumentException if no such species exists for the shape
     */
    static FloatSpecies species(VectorShape s) {
        Objects.requireNonNull(s);
        switch (s) {
            case S_64_BIT: return (FloatSpecies) SPECIES_64;
            case S_128_BIT: return (FloatSpecies) SPECIES_128;
            case S_256_BIT: return (FloatSpecies) SPECIES_256;
            case S_512_BIT: return (FloatSpecies) SPECIES_512;
            case S_Max_BIT: return (FloatSpecies) SPECIES_MAX;
            default: throw new IllegalArgumentException("Bad shape: " + s);
        }
    }

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_64_BIT VectorShape.S_64_BIT}. */
    public static final VectorSpecies<Float> SPECIES_64
        = new FloatSpecies(VectorShape.S_64_BIT,
                            Float64Vector.class,
                            Float64Vector.Float64Mask.class,
                            Float64Vector::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_128_BIT VectorShape.S_128_BIT}. */
    public static final VectorSpecies<Float> SPECIES_128
        = new FloatSpecies(VectorShape.S_128_BIT,
                            Float128Vector.class,
                            Float128Vector.Float128Mask.class,
                            Float128Vector::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_256_BIT VectorShape.S_256_BIT}. */
    public static final VectorSpecies<Float> SPECIES_256
        = new FloatSpecies(VectorShape.S_256_BIT,
                            Float256Vector.class,
                            Float256Vector.Float256Mask.class,
                            Float256Vector::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_512_BIT VectorShape.S_512_BIT}. */
    public static final VectorSpecies<Float> SPECIES_512
        = new FloatSpecies(VectorShape.S_512_BIT,
                            Float512Vector.class,
                            Float512Vector.Float512Mask.class,
                            Float512Vector::new);

    /** Species representing {@link FloatVector}s of {@link VectorShape#S_Max_BIT VectorShape.S_Max_BIT}. */
    public static final VectorSpecies<Float> SPECIES_MAX
        = new FloatSpecies(VectorShape.S_Max_BIT,
                            FloatMaxVector.class,
                            FloatMaxVector.FloatMaxMask.class,
                            FloatMaxVector::new);

    /**
     * Preferred species for {@link FloatVector}s.
     * A preferred species is a species of maximal bit-size for the platform.
     */
    public static final VectorSpecies<Float> SPECIES_PREFERRED
        = (FloatSpecies) VectorSpecies.ofPreferred(float.class);


    // ==== JROSE NAME CHANGES ====

    /** Use lanewise(NEG, m). */
    @Deprecated
    public final FloatVector neg(VectorMask<Float> m) {
        return lanewise(NEG, m);
    }

    /** Use lanewise(ABS, m). */
    @Deprecated
    public final FloatVector abs(VectorMask<Float> m) {
        return lanewise(ABS, m);
    }

    /** Use explicit argument of ByteOrder.LITTLE_ENDIAN */
    @Deprecated
    public static
    FloatVector fromByteBuffer(VectorSpecies<Float> species,
                                        ByteBuffer bb, int offset) {
        ByteOrder bo = ByteOrder.LITTLE_ENDIAN;
        if (bb.order() != bo)  throw new IllegalArgumentException();
        return fromByteBuffer(species, bb, offset, bo);
    }

    /** Use explicit argument of ByteOrder.LITTLE_ENDIAN */
    @Deprecated
    public static
    FloatVector fromByteBuffer(VectorSpecies<Float> species,
                                        ByteBuffer bb, int offset,
                                        VectorMask<Float> m) {
        ByteOrder bo = ByteOrder.LITTLE_ENDIAN;
        if (bb.order() != bo)  throw new IllegalArgumentException();
        return fromByteBuffer(species, bb, offset, bo, m);
    }

    /** Use fromValues(s, value...) */
    @Deprecated
    public static
    FloatVector scalars(VectorSpecies<Float> species,
                                 float... values) {
        return fromValues(species, values);
    }

    @Deprecated public final float addLanes() { return reduceLanes(ADD); }
    @Deprecated public final float addLanes(VectorMask<Float> m) { return reduceLanes(ADD, m); }
    @Deprecated public final float mulLanes() { return reduceLanes(MUL); }
    @Deprecated public final float mulLanes(VectorMask<Float> m) { return reduceLanes(MUL, m); }
    @Deprecated public final float minLanes() { return reduceLanes(MIN); }
    @Deprecated public final float minLanes(VectorMask<Float> m) { return reduceLanes(MIN, m); }
    @Deprecated public final float maxLanes() { return reduceLanes(MAX); }
    @Deprecated public final float maxLanes(VectorMask<Float> m) { return reduceLanes(MAX, m); }
    @Deprecated public final float orLanes() { return reduceLanes(OR); }
    @Deprecated public final float orLanes(VectorMask<Float> m) { return reduceLanes(OR, m); }
    @Deprecated public final float andLanes() { return reduceLanes(AND); }
    @Deprecated public final float andLanes(VectorMask<Float> m) { return reduceLanes(AND, m); }
    @Deprecated public final float xorLanes() { return reduceLanes(XOR); }
    @Deprecated public final float xorLanes(VectorMask<Float> m) { return reduceLanes(XOR, m); }
    @Deprecated public final FloatVector sqrt() { return lanewise(SQRT); }
    @Deprecated public final FloatVector sqrt(VectorMask<Float> m) { return lanewise(SQRT, m); }
    @Deprecated public final FloatVector tan() { return lanewise(TAN); }
    @Deprecated public final FloatVector tan(VectorMask<Float> m) { return lanewise(TAN, m); }
    @Deprecated public final FloatVector tanh() { return lanewise(TANH); }
    @Deprecated public final FloatVector tanh(VectorMask<Float> m) { return lanewise(TANH, m); }
    @Deprecated public final FloatVector sin() { return lanewise(SIN); }
    @Deprecated public final FloatVector sin(VectorMask<Float> m) { return lanewise(SIN, m); }
    @Deprecated public final FloatVector sinh() { return lanewise(SINH); }
    @Deprecated public final FloatVector sinh(VectorMask<Float> m) { return lanewise(SINH, m); }
    @Deprecated public final FloatVector cos() { return lanewise(COS); }
    @Deprecated public final FloatVector cos(VectorMask<Float> m) { return lanewise(COS, m); }
    @Deprecated public final FloatVector cosh() { return lanewise(COSH); }
    @Deprecated public final FloatVector cosh(VectorMask<Float> m) { return lanewise(COSH, m); }
    @Deprecated public final FloatVector asin() { return lanewise(ASIN); }
    @Deprecated public final FloatVector asin(VectorMask<Float> m) { return lanewise(ASIN, m); }
    @Deprecated public final FloatVector acos() { return lanewise(ACOS); }
    @Deprecated public final FloatVector acos(VectorMask<Float> m) { return lanewise(ACOS, m); }
    @Deprecated public final FloatVector atan() { return lanewise(ATAN); }
    @Deprecated public final FloatVector atan(VectorMask<Float> m) { return lanewise(ATAN, m); }
    @Deprecated public final FloatVector atan2(Vector<Float> v) { return lanewise(ATAN2, v); }
    @Deprecated public final FloatVector atan2(float s) { return lanewise(ATAN2, s); }
    @Deprecated public final FloatVector atan2(Vector<Float> v, VectorMask<Float> m) { return lanewise(ATAN2, v, m); }
    @Deprecated public final FloatVector atan2(float s, VectorMask<Float> m) { return lanewise(ATAN2, s, m); }
    @Deprecated public final FloatVector cbrt() { return lanewise(CBRT); }
    @Deprecated public final FloatVector cbrt(VectorMask<Float> m) { return lanewise(CBRT, m); }
    @Deprecated public final FloatVector log() { return lanewise(LOG); }
    @Deprecated public final FloatVector log(VectorMask<Float> m) { return lanewise(LOG, m); }
    @Deprecated public final FloatVector log10() { return lanewise(LOG10); }
    @Deprecated public final FloatVector log10(VectorMask<Float> m) { return lanewise(LOG10, m); }
    @Deprecated public final FloatVector log1p() { return lanewise(LOG1P); }
    @Deprecated public final FloatVector log1p(VectorMask<Float> m) { return lanewise(LOG1P, m); }
    @Deprecated public final FloatVector pow(Vector<Float> v) { return lanewise(POW, v); }
    @Deprecated public final FloatVector pow(float s) { return lanewise(POW, s); }
    @Deprecated public final FloatVector pow(Vector<Float> v, VectorMask<Float> m) { return lanewise(POW, v, m); }
    @Deprecated public final FloatVector pow(float s, VectorMask<Float> m) { return lanewise(POW, s, m); }
    @Deprecated public final FloatVector exp() { return lanewise(EXP); }
    @Deprecated public final FloatVector exp(VectorMask<Float> m) { return lanewise(EXP, m); }
    @Deprecated public final FloatVector expm1() { return lanewise(EXPM1); }
    @Deprecated public final FloatVector expm1(VectorMask<Float> m) { return lanewise(EXPM1, m); }
    @Deprecated public final FloatVector hypot(Vector<Float> v) { return lanewise(HYPOT, v); }
    @Deprecated public final FloatVector hypot(float s) { return lanewise(HYPOT, s); }
    @Deprecated public final FloatVector hypot(Vector<Float> v, VectorMask<Float> m) { return lanewise(HYPOT, v, m); }
    @Deprecated public final FloatVector hypot(float s, VectorMask<Float> m) { return lanewise(HYPOT, s, m); }
    @Deprecated 
    public final FloatVector and(Vector<Float> v) { return lanewise(AND, v); }
    @Deprecated public final FloatVector and(float s) { return lanewise(AND, s); }
    @Deprecated public final FloatVector and(Vector<Float> v, VectorMask<Float> m) { return lanewise(AND, v, m); }
    @Deprecated public final FloatVector and(float s, VectorMask<Float> m) { return lanewise(AND, s, m); }
    @Deprecated public final FloatVector or(Vector<Float> v) { return lanewise(OR, v); }
    @Deprecated public final FloatVector or(float s) { return lanewise(OR, s); }
    @Deprecated public final FloatVector or(Vector<Float> v, VectorMask<Float> m) { return lanewise(OR, v, m); }
    @Deprecated public final FloatVector or(float s, VectorMask<Float> m) { return lanewise(OR, s, m); }
    @Deprecated public final FloatVector xor(Vector<Float> v) { return lanewise(XOR, v); }
    @Deprecated public final FloatVector xor(float s) { return lanewise(XOR, s); }
    @Deprecated public final FloatVector xor(Vector<Float> v, VectorMask<Float> m) { return lanewise(XOR, v, m); }
    @Deprecated public final FloatVector xor(float s, VectorMask<Float> m) { return lanewise(XOR, s, m); }
    @Deprecated public final FloatVector not() { return lanewise(NOT); }
    @Deprecated public final FloatVector not(VectorMask<Float> m) { return lanewise(NOT, m); }
    @Deprecated public final FloatVector shiftLeft(int s) { return lanewise(LSHL, (float) s); }
    @Deprecated public final FloatVector shiftLeft(int s, VectorMask<Float> m) { return lanewise(LSHL, (float) s, m); }
    @Deprecated public final FloatVector shiftLeft(Vector<Float> v) { return lanewise(LSHL, v); }
    @Deprecated public final FloatVector shiftLeft(Vector<Float> v, VectorMask<Float> m) { return lanewise(LSHL, v, m); }
    @Deprecated public final FloatVector shiftRight(int s) { return lanewise(LSHR, (float) s); }
    @Deprecated public final FloatVector shiftRight(int s, VectorMask<Float> m) { return lanewise(LSHR, (float) s, m); }
    @Deprecated public final FloatVector shiftRight(Vector<Float> v) { return lanewise(LSHR, v); }
    @Deprecated public final FloatVector shiftRight(Vector<Float> v, VectorMask<Float> m) { return lanewise(LSHR, v, m); }
    @Deprecated public final FloatVector shiftArithmeticRight(int s) { return lanewise(ASHR, (float) s); }
    @Deprecated public final FloatVector shiftArithmeticRight(int s, VectorMask<Float> m) { return lanewise(ASHR, (float) s, m); }
    @Deprecated public final FloatVector shiftArithmeticRight(Vector<Float> v) { return lanewise(ASHR, v); }
    @Deprecated public final FloatVector shiftArithmeticRight(Vector<Float> v, VectorMask<Float> m) { return lanewise(ASHR, v, m); }
    @Deprecated public final FloatVector rotateLeft(int s) { return lanewise(ROL, (float) s); }
    @Deprecated public final FloatVector rotateLeft(int s, VectorMask<Float> m) { return lanewise(ROL, (float) s, m); }
    @Deprecated public final FloatVector rotateRight(int s) { return lanewise(ROR, (float) s); }
    @Deprecated public final FloatVector rotateRight(int s, VectorMask<Float> m) { return lanewise(ROR, (float) s, m); }
    @Deprecated @Override public FloatVector rotateLanesLeft(int i) { return (FloatVector) super.rotateLanesLeft(i); }
    @Deprecated @Override public FloatVector rotateLanesRight(int i) { return (FloatVector) super.rotateLanesRight(i); }
    @Deprecated @Override public FloatVector shiftLanesLeft(int i) { return (FloatVector) super.shiftLanesLeft(i); }
    @Deprecated @Override public FloatVector shiftLanesRight(int i) { return (FloatVector) super.shiftLanesRight(i); }
    @Deprecated public FloatVector with(int i, float e) { return withLane(i, e); }
}
