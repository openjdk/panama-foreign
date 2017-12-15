/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.vector.el.expression;

import com.oracle.vector.PatchableVecUtils;
import com.oracle.vector.el.Shape;
import com.oracle.vector.el.visitor.ExpressionEvaluator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class VConst<E,S extends Shape> implements Expression<E,S>{

    final S shape;
    final Class<E> vClass;
    final E val;

    @SuppressWarnings("unchecked")
    VConst(S shape, E val){
       this.val = val;
       this.shape = shape;
       this.vClass = (Class<E>) normalizeClass(val.getClass());
    }

    //TODO: Fixme.
    private static Class<?> normalizeClass(Class<?> c) {
        if(c == Float.class){
            return float.class;
        } else if (c == Integer.class) {
            return int.class;
        }
        return c;
    }

    @Override
    public <R> R accept(ExpressionEvaluator<R> v) {
        return v.visit(this);
    }

    @Override
    public int length() {
            return shape.length();
    }

    @Override
    public S shape() {
        return shape;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<E> elementType() {
        return (Class<E>) val.getClass();
    }

    @Override
    public Optional<VConst<E, S>> toVConst() {
        return Optional.of(this);
    }

    private int laneWidth() {
        if(vClass == int.class || vClass == float.class) {
            return 32;
        } else if(vClass == long.class || vClass == double.class) {
            return  64;
        } else {
            throw new UnsupportedOperationException("Invalid lane type: " + vClass);
        }
    }

    public MethodHandle packedValue(){
        int laneWidth = laneWidth();
        int bitLength;

        bitLength = laneWidth * shape.length();

        switch(bitLength) {
            case 32:
                if(vClass == int.class) {
                   return MethodHandles.constant(vClass,((Number)val).intValue());
                } else if(vClass == float.class) {
                   return MethodHandles.constant(vClass,((Number)val).floatValue());
                }
            case 64:
                if(vClass == double.class) {
                    return MethodHandles.constant(vClass,((Number)val).doubleValue());
                } else if(vClass == long.class){
                    return MethodHandles.constant(vClass,((Number)val).longValue());
                }
            case 128:
            case 256:
                switch(vClass.getSimpleName()) {
                    case "int":
                    case "Integer":
                        int v = (int) val;
                        int[] ibuffer = new int[bitLength/laneWidth];
                        for(int i = 0; i < ibuffer.length; i++){
                            ibuffer[i] = v;
                        }
                        return MethodHandles.constant(vClass,bitLength == 256 ? PatchableVecUtils.long4FromIntArray(ibuffer,0) : PatchableVecUtils.long2FromIntArray(ibuffer,0));
                    case "float":
                    case "Float":
                        float f = (float) val;
                        float[] fbuffer = new float[bitLength/laneWidth];
                        for(int i = 0; i < fbuffer.length; i++){
                           fbuffer[i] = f;
                        }
                        return MethodHandles.constant(vClass,bitLength == 256 ? PatchableVecUtils.long4FromFloatArray(fbuffer,0) : PatchableVecUtils.long2FromFloatArray(fbuffer,0));
                    case "double":
                    case "Double":
                        double d = (double) val;
                        double[] dbuffer = new double[bitLength/laneWidth];
                        for(int i = 0; i < dbuffer.length; i++){
                            dbuffer[i] = d;
                        }
                        return MethodHandles.constant(vClass,bitLength == 256 ? PatchableVecUtils.long4FromDoubleArray(dbuffer,0) : PatchableVecUtils.long2FromDoubleArray(dbuffer,0));
                    case "long":
                    case "Long":
                        long l = (long) val;
                        long[] lbuffer = new long[bitLength/laneWidth];
                        for(int i = 0; i < lbuffer.length; i++){
                           lbuffer[i] = l;
                        }
                        return MethodHandles.constant(vClass,bitLength == 256 ? PatchableVecUtils.long4FromLongArray(lbuffer,0) : PatchableVecUtils.long2FromLongArray(lbuffer,0));
                    default:
                        throw new UnsupportedOperationException("Bad element/size configuration.");
                }
            default:
                throw new UnsupportedOperationException("Invalid lane width.");
        }
    }

    public Class<?> packedClass(int laneWidth, Shape shape){
        int bitLength = laneWidth * shape.length();

        switch(bitLength) {
            case 32:
                return val.getClass();
            case 64:
                return val.getClass();
            case 128:
                return Long2.class;
            case 256:
                return Long4.class;
            default:
                throw new UnsupportedOperationException("Cannot retrive class for invalid lanewidth/shape combo.");
        }

    }

    public Class<?> packedClass(){
        int laneWidth = laneWidth();
        int bitLength = laneWidth * shape.length();

        switch(bitLength) {
            case 32:
                //return val.getClass();
                return vClass;
            case 64:
                //return val.getClass();
                return vClass;
            case 128:
                return Long2.class;
            case 256:
                return Long4.class;
            default:
                throw new UnsupportedOperationException("Cannot retrive class for invalid lanewidth/shape combo.");
        }

    }
}
