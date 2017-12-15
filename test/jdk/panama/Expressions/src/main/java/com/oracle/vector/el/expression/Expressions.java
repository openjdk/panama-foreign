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

import com.oracle.vector.el.Ops;
import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.expression.bexp.VBinBExp;
import com.oracle.vector.el.expression.scalars.*;

import java.util.function.Supplier;

public class Expressions {
    /*
    public static Expression<Integer,Shapes.LENGTH4> broadcast4(int val){
        return new VConst<>(Shapes.L4,val);
    }
    public static Expression<Float,Shapes.LENGTH4>   broadcast4(float val){
        return new VConst<>(Shapes.L4,val);
    }
    public static Expression<Float,Shapes.LENGTH8>   broadcast8(float val){
        return new VConst<>(Shapes.L8,val);
    }
    public static Expression<Double,Shapes.LENGTH4>  broadcast4(double val) { return new VConst<>(Shapes.L4,val);}
    */
    public static <Z extends Number> Expression<Z,Shapes.LENGTH1> constant(Z n){
        return new VConst<>(Shapes.L1,n);
    }

    public static <E extends Number> Expression<E,Shapes.LENGTH2> broadcast2(Expression<E,Shapes.LENGTH1> e) {
        return new VBroadcast<>(e,Shapes.L2);
    }

    public static <E extends Number> Expression<E,Shapes.LENGTH4> broadcast4(Expression<E,Shapes.LENGTH1> e) {
        return new VBroadcast<>(e,Shapes.L4);
    }

    public static <E extends Number> Expression<E,Shapes.LENGTH8> broadcast8(Expression<E,Shapes.LENGTH1> e) {
        return new VBroadcast<>(e,Shapes.L8);
    }

    public static Expression<Double,Shapes.LENGTH1>  scalar(Expression<Double,Shapes.LENGTH1> e, DoubleOp f){ return new DoubleScalarOp(e,f);}
    public static Expression<Float,Shapes.LENGTH1>   scalar(Expression<Float,Shapes.LENGTH1> e, FloatOp f){ return new FloatScalarOp(e,f);}
    public static Expression<Integer,Shapes.LENGTH1> scalar(Expression<Integer,Shapes.LENGTH1> e, IntOp f){ return new IntScalarOp(e,f);}
    public static Expression<Long,Shapes.LENGTH1>    scalar(Expression<Long,Shapes.LENGTH1> e, LongOp f){ return new LongScalarOp(e,f);}

    public static Expression<Double,Shapes.LENGTH1>  scalar(Expression<Double,Shapes.LENGTH1> left, Expression<Double,Shapes.LENGTH1> right, DoubleBinOp f){ return new DoubleScalarBinOp(left,right,f);}
    public static Expression<Float,Shapes.LENGTH1>   scalar(Expression<Float,Shapes.LENGTH1> left, Expression<Float,Shapes.LENGTH1> right, FloatBinOp f){ return new FloatScalarBinOp(left,right,f);}
    public static Expression<Integer,Shapes.LENGTH1> scalar(Expression<Integer,Shapes.LENGTH1> left, Expression<Integer,Shapes.LENGTH1> right, IntBinOp f){ return new IntScalarBinOp(left,right,f);}
    public static Expression<Long,Shapes.LENGTH1>    scalar(Expression<Long,Shapes.LENGTH1> left, Expression<Long,Shapes.LENGTH1> right, LongBinOp f){ return new LongScalarBinOp(left,right,f);}

    public static <E,S extends Shape> Expression<E,S> mask(Expression<Boolean,S> mask, Expression<E,S> thn, Expression<E,S> els) {
        return new VMask<>(mask,thn,els);
    }

    public static <E,S extends Shape> Expression<E,S> mask(Supplier<Expression<Boolean,S>> mask, Supplier<Expression<E,S>> thn, Supplier<Expression<E,S>> els) {
        return new VMask<>(mask.get(),thn.get(),els.get());
    }

    public static <E,S extends Shape> Expression<Boolean,S> eq(Expression<E,S> left, Expression<E,S> right) {
        return new VBinBExp<>(left,right, Ops.EQ);
    }
    public static <E,S extends Shape> Expression<Boolean,S> lt(Expression<E,S> left, Expression<E,S> right){
        return new VBinBExp<>(left,right,Ops.LT);
    }

    public static <E,S extends Shape> Expression<Boolean,S> lte(Expression<E,S> left, Expression<E,S> right){
        return new VBinBExp<>(left,right,Ops.LTE);
    }

    public static <E,S extends Shape> Expression<E,S> get(IndexableVal<E,S> iv, Expression<Integer,Shapes.LENGTH1> ix) {
        return new IndexedVal<>(iv,ix);
    }

    public static int elementWidth(Class<?> elem){
        if(elem == Byte.class || elem == byte.class){
            return 8;
        } else if(elem == Short.class || elem == short.class || elem == Character.class || elem == char.class) {
            return 16;
        } else if(elem == Integer.class || elem == int.class || elem == Float.class || elem == float.class) {
            return 32;
        } else if(elem == Double.class || elem == double.class || elem == Long.class || elem == long.class) {
            return 64;
        }

        throw new UnsupportedOperationException("elementWidth encountered unsupported type");
    }

    public static Class<?> packedType(Class<?> elem, Shape s){
        return packedType(elem,s.length());
    }

    public static Class<?> packedType(Class<?> elem, int l){
        if(l == 1){
            if(elem == Integer.class) return int.class;
            if(elem == Float.class) return float.class;
            if(elem == Double.class) return double.class;
            if(elem == Long.class) return long.class;

            return elem;
        } else {
            int len = elementWidth(elem) * l;
            //if(len == 16) {
            //   return short.class;
            //} else if(len == 32) {
            //   return int.class;
            //} else if(len == 64) {
            //   return long.class;
            //} else
            if(len == 128) {
               return Long2.class;
            } else if(len == 256) {
               return Long4.class;
            } else if(len == 512) {
               return Long8.class;
            }
            throw new UnsupportedOperationException("Unsupported Size for packed elements (Not 128-512 bits).");
        }
    }
}
