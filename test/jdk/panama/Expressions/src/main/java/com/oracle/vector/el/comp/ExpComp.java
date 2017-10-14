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
package com.oracle.vector.el.comp;

import com.oracle.vector.el.Ops;
import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Val;
import com.oracle.vector.el.expression.*;
import com.oracle.vector.el.expression.bexp.VBinBExp;
import com.oracle.vector.el.visitor.ExpressionEvaluator;
import com.oracle.vector.ops.OpProvider;
import static com.oracle.vector.el.Ops.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

public class ExpComp implements ExpressionEvaluator<Optional<MethodHandle>> {

    private final OpProvider op;

    public ExpComp(OpProvider op) {
        this.op = op;
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(VAdd<E,T> v) {
        return procMethods(op.getOp(v.elementType(),ADD,v.length()),v.getLeft(),v.getRight());
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(VDiv<E,T> v) {
        return procMethods(op.getOp(v.elementType(),DIV,v.length()),v.getLeft(),v.getRight());
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(VMul<E,T> v) {
        return procMethods(op.getOp(v.elementType(),MUL,v.length()),v.getLeft(),v.getRight());
    }

    @Override
    public <E> Optional<MethodHandle> visit(VProd<E> v) {
        throw new UnsupportedOperationException("Prod not implemented yet.");
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(VSub<E,T> v) {
        return procMethods(op.getOp(v.elementType(),SUB,v.length()),v.getLeft(),v.getRight());
    }

    @Override
    public <E> Optional<MethodHandle> visit(VSum<E> v) {
        return procMethods(op.getOp(v.elementType(),SUM,v.length()),v.getAddends());
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(Val<E, T> v) {
        return Optional.empty();
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(ITE<E, T> v) {
       MethodHandle test, then_, else_;
       test  = v.getTest().accept(this).orElse(MethodHandles.identity(boolean.class));
       then_ = v.getThen().accept(this).orElse(MethodHandles.identity(v.elementType()));
       else_ = v.getElse().accept(this).orElse(MethodHandles.identity(v.elementType()));

       List<Class<?>> t,thn,els;
       t = test.type().parameterList();
       thn = test.type().parameterList();
       els = test.type().parameterList();

       //Rebuild each MethodHandle so it has the same parameter typing, but each MethodHandle drops arguments it doesn't
       //use (ie the other MethodHandle parameters).
       MethodHandle then_p, else_p;

       then_p = MethodHandles.dropArguments(then_,0,t);
       then_p = MethodHandles.dropArguments(then_p,then_p.type().parameterCount(),els);

       else_p = MethodHandles.dropArguments(else_,0,t);
       else_p = MethodHandles.dropArguments(else_p,t.size(),thn);

       return Optional.of(MethodHandles.guardWithTest(test,then_p,else_p));
    }

    @Override
    public <E,T extends Shape> Optional<MethodHandle> visit(VConst<E, T> v) {
        return Optional.of(v.packedValue());
    }

    @Override
    public Optional<MethodHandle> visit(FloatScalarOp v) {
        throw new UnsupportedOperationException("FloatScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(DoubleScalarOp v) {
        throw new UnsupportedOperationException("DoubleScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(IntScalarOp v) {
        throw new UnsupportedOperationException("IntScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(LongScalarOp v) {
        throw new UnsupportedOperationException("LongScalar Not Implemented");
    }

    @Override
    public <E, T extends Shape> Optional<MethodHandle> visit(VBroadcast<E, T> v) {
        MethodHandle id = MethodHandles.identity(Expressions.packedType(v.getChild().elementType(),1));
        MethodHandle c = v.getChild().accept(this).orElse(id);
        MethodHandle bc = op.getOp(v.elementType(),BCAST,v.length());

        return Optional.of(MethodHandles.filterReturnValue(c,bc));
    }

    @Override
    public <E, T extends Shape> Optional<MethodHandle> visit(VMask<E, T> v) {
        Class<?> eTy = v.getThn().elementType();
        Class<?> pTy = Expressions.packedType(eTy,v.length());
        MethodHandle mask, thn, els;
        MethodHandle id = MethodHandles.identity(pTy);

        //TODO: MASKING NEEDS CONTEXT WRT EXPRESSION SHAPE
        mask = v.getMask().accept(this).orElse(id); //(args..)VectorB
        thn  = v.getThn().accept(this).orElse(id);  //(args2..)VectorA
        els  = v.getEls().accept(this).orElse(id);  //(args3..)VectorA

        MethodHandle blender = op.getOp(v.elementType(),SELECT,v.length()); //(VectorA, VectorA, VectorB)VectorA

        int thnParams = thn.type().parameterCount();
        int elsParams = els.type().parameterCount();
        MethodHandle r = MethodHandles.collectArguments(blender,0,els);
        r = MethodHandles.collectArguments(r,elsParams,thn);
        r = MethodHandles.collectArguments(r,thnParams+elsParams,mask); //(args2..,args3..,args..)VectorA (blended)
        return Optional.of(r);

    }

    @Override
    public <E, S extends Shape> Optional<MethodHandle> visit(VBinBExp<E, S> v) {
        return procMethods(op.getOp(v.getRealElementType(),v.getOp(),v.length()),v.getLeft(),v.getRight());
    }

    @Override
    public <E, S extends Shape> Optional<MethodHandle> visit(IndexedVal<E, S> v) {
        MethodHandle id = MethodHandles.identity(int.class);
        MethodHandle ix = v.getIxExp().accept(this).orElse(id);

        MethodHandle loader = op.getOp(v.elementType(),Ops.ARY_LOAD,v.length());
        MethodType mt = loader.type();
        MethodType mtn = MethodType.methodType(mt.returnType(),mt.parameterArray()[1],mt.parameterArray()[0]); //flip
        loader = MethodHandles.permuteArguments(loader,mtn,1,0);
        loader = MethodHandles.collectArguments(loader,0,ix);

        return Optional.of(loader);


    }

    @Override
    public Optional<MethodHandle> visit(FloatScalarBinOp v) {
        throw new UnsupportedOperationException("FloatScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(DoubleScalarBinOp v) {
        throw new UnsupportedOperationException("DoubleScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(IntScalarBinOp v) {
        throw new UnsupportedOperationException("IntScalar Not Implemented");
    }

    @Override
    public Optional<MethodHandle> visit(LongScalarBinOp v) {
        throw new UnsupportedOperationException("LongScalar Not Implemented");
    }



    private <E,T extends Shape> Optional<MethodHandle> procMethods(MethodHandle operator, Expression<E,T> leftNode, Expression<E,T> rightNode){

        Optional<VConst<E,T>> lVal = leftNode.toVConst();
        Optional<VConst<E,T>> rVal = rightNode.toVConst();
        try {
            if(lVal.isPresent()){
                if(rVal.isPresent()) { //Both Values
                    //Both arguments to this operator are constant values, so we fold them by eagerly evaluating now.

                    //Reflect the type of the packed value
                    Class<?> lClass = lVal.get().packedClass();
                    Class<?> rClass = lVal.get().packedClass();

                    //Take the supplied operator and apply it to the downcasted packed values
                    Object res = operator.invokeExact(lClass.cast(lVal.get().packedValue()),rClass.cast(rVal.get().packedValue()));

                    //Reflect the return type from the operator to downcast the constant result
                    Class<?> retType = operator.type().returnType();

                    //Our result is final because our aruments are constants.  Bundle this into a constant MethodHandle
                    return Optional.of(MethodHandles.constant(retType,retType.cast(res)));
                } else { //Left-only value
                    Optional<MethodHandle> rightOp = rightNode.accept(this);

                    //Reflect the type of the packed value
                    Class<?> lClass = lVal.get().packedClass();

                    MethodHandle lmh = operator.bindTo(lClass.cast(lVal.get().packedValue()));

                    Optional<MethodHandle> ret = rightOp.map(right -> MethodHandles.collectArguments(lmh,0,right))
                        .or(() -> Optional.of(lmh));

                    return ret;
                }

            } else {
                if(rVal.isPresent()) { //Right-only value
                    Optional<MethodHandle> leftOp = leftNode.accept(this);
                    Class<?> rClass = rVal.get().packedClass();

                    MethodHandle val = rVal.get().packedValue();

                    Optional<MethodHandle> mh = leftOp.flatMap(left -> {
                        MethodHandle lmh = MethodHandles.collectArguments(operator,0,left);
                        int leftArgs = left.type().parameterCount();
                        return Optional.of(MethodHandles.collectArguments(lmh,leftArgs,MethodHandles.constant(rClass,rClass.cast(val)))); //Left is a methodhandle
                   // }).or(() -> Optional.of(MethodHandles.collectArguments(operator,1,MethodHandles.constant(rClass,rClass.cast(val))))); //Left is a var
                    }).or(() -> Optional.of(MethodHandles.collectArguments(operator,1,val)));//MethodHandles.constant(rClass,rClass.cast(val))))); //Left is a var

                    return mh;


                } else { //Neither Value
                    Optional<MethodHandle> leftOp  = leftNode.accept(this);
                    Optional<MethodHandle> rightOp = rightNode.accept(this);

                    Optional<MethodHandle> mh = leftOp.flatMap(left -> {
                        MethodHandle lmh = MethodHandles.collectArguments(operator,0,left);
                        int leftArgs = left.type().parameterCount();
                        Optional<MethodHandle> ret = rightOp.map(right -> MethodHandles.collectArguments(lmh,leftArgs,right))
                                .or(() -> Optional.of(lmh));
                        return ret;
                    })
                            .or(() -> rightOp.map(right -> MethodHandles.collectArguments(operator,1,right)))
                            .or(() -> Optional.of(operator));

                    return mh;
                }

            }
        } catch (Throwable e){
            throw new Error(e);
        }


    }

    private Optional<MethodHandle> procMethods(MethodHandle operator, Expression<?,?> childNode) {
        Optional<MethodHandle> childOp = childNode.accept(this);

        Optional<MethodHandle> mh = childOp.flatMap(child ->
                Optional.of(MethodHandles.collectArguments(operator,0,child))
        ).or(() -> Optional.of(operator));

        return mh;
    }
}

