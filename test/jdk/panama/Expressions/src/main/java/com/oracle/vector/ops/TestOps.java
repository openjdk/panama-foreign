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
package com.oracle.vector.ops;

import com.oracle.vector.PatchableVecUtils;
import com.oracle.vector.el.Ops;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class TestOps implements OpProvider{

    public static final TestOps instance = new TestOps();

    @Override
    public MethodHandle getOp(Class<?> lane, Ops op,int length) {
        try {

            if(lane == Float.class){
                switch(length){
                    case 1:
                        switch(op){
                            case ADD:
                                return MethodHandles.lookup().findStatic(TestOps.class,"fadd", MethodType.methodType(float.class,float.class,float.class));
                            case SUM:
                                return PatchableVecUtils.sum_float_L4;
                        }
                        break;
                    case 8:
                        switch(op){
                            case ADD:
                                return PatchableVecUtils.MHm256_vaddps;
                            case MUL:
                                return PatchableVecUtils.MHm256_vmulps;
                            case ARY_LOAD:
                                return PatchableVecUtils.MHm256_vmovdqu_load_floatarray;
                            case ARY_STORE:
                                return PatchableVecUtils.MHm256_vmovdqu_store_floatarray;
                            case LT:
                                return PatchableVecUtils.MHm256_vcmpgtps;
                            case EQ:
                                return PatchableVecUtils.MHm256_vcmpeqps;
                            case BCAST:
                                return PatchableVecUtils.MHm256_vbroadcastss;
                            case SELECT:
                                return PatchableVecUtils.MHm256_vblendvps;
                            default:
                                throw new UnsupportedOperationException(op + " not supported");
                        }
                    default:
                        throw new UnsupportedOperationException("Operator " + op + " of size " + length + " unsupported for " + lane + " element types.");
                }
            } else if(lane == Double.class){
                switch(length){
                    case 1:
                        switch(op){
                            case ADD:
                                return MethodHandles.lookup().findStatic(TestOps.class,"dadd", MethodType.methodType(double.class,double.class,double.class));
                            case SUM:
                                return PatchableVecUtils.sum_float_L4;
                        }
                        break;
                    case 4:
                        switch(op){
                            case ADD:
                                return PatchableVecUtils.MHm256_vaddpd;
                            case MUL:
                                return PatchableVecUtils.MHm256_vmulpd;
                            case ARY_LOAD:
                                return PatchableVecUtils.MHm256_vmovdqu_load_doublearray;
                            case ARY_STORE:
                                return PatchableVecUtils.MHm256_vmovdqu_store_doublearray;
                            default:
                                throw new UnsupportedOperationException(op + " not supported");
                        }
                    default:
                        throw new UnsupportedOperationException("Operator " + op + " of size " + length + " unsupported for " + lane + " element types.");
                }
            } else if(lane == Integer.class){
                switch(length){
                    case 1:
                        switch(op){
                            case ADD:
                                return MethodHandles.lookup().findStatic(TestOps.class,"iadd", MethodType.methodType(int.class,int.class,int.class));
                        }
                }
            }
            throw new UnsupportedOperationException("Operator " + op + " of size " + length + " unsupported for " + lane + " element types.");
            }
        catch (NoSuchMethodException|IllegalAccessException e){
            throw new UnsupportedOperationException(e);

        }
    }


    public static float fadd(float left, float right){
        return left + right;
    }

    public static int iadd(int left, int right){
        return left + right;
    }


        public static double dadd(double left, double right){
        return left + right;
    }

    private static float zero(){
        return 0f;
    }
}
