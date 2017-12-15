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
package com.oracle.vector.el.stmt;

import com.oracle.vector.el.Ops;
import com.oracle.vector.el.Shape;
import com.oracle.vector.el.Shapes;
import com.oracle.vector.el.Val;
import com.oracle.vector.el.builder.MHMeta;
//import com.oracle.vector.el.builder.Term;
import com.oracle.vector.el.comp.ExpComp;
import com.oracle.vector.el.comp.ExpVarOrder;
import com.oracle.vector.el.expression.Expression;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.oracle.vector.el.expression.IndexableVal;
import com.oracle.vector.ops.OpProvider;
import com.oracle.vector.ops.OpProviders;
import com.oracle.vector.el.expression.types.VectorClass;


public class Builder<E,S extends Shape> {

    Class<E> retT;
    S retS;

    private List<Val>  params = new ArrayList<>();
    private List<Val> vals = new ArrayList<>();
    private List<Statement>  stmts = new ArrayList<>();


    private Builder(Class<E> e, S shape){
        retT = e;
        retS = shape;
    }
    //private Term body;
    private Expression<E,S> retexp;

    public static Builder builder(){
        return new Builder();
    }

    public static <E, S extends Shape> Builder<E,S> builder(Class<E> e, S s) { return new Builder<>(e,s); }

    public Builder(){
        params = new ArrayList<>();
    }

    private Builder(List<Val> le){
        this.params = le;
    }

    private Builder(Builder b){
        this(b.params);
    }

    @SuppressWarnings("unchecked")
    public <E, S extends Shape> Val<E,S> assign(Expression<E,S> e){
        Class<E> etype = e.elementType();
        S s;
        switch(e.length()){
            case 1:
                s = (S) Shapes.L1;
                break;
            case 2:
                s = (S) Shapes.L2;
                break;
            case 4:
                s = (S) Shapes.L4;
                break;
            case 8:
                s = (S) Shapes.L8;
                break;
            case 16:
                s = (S) Shapes.L16;
                break;
            default:
                throw new UnsupportedOperationException("Shape length not supported: " + e.length());

        }
        Val<E,S> v = new Val<>(s,etype);
        Statement stmt = new Assignment<>(v,e);
        List<Statement> ss = new ArrayList<>();
        ss.addAll(stmts);
        ss.add(stmt);
        stmts = ss;
        return v;
    }


    public <T extends Shape> Builder<E,S> bindFloatIndexable(T s, Param1DBinder<E,Float,S,T> binder) {
        IndexableVal<Float,T> v = new IndexableVal<>(s,Float.class,float[].class);
        addParams(v);
        return binder.apply(v,this);
    }

    public <T extends Shape> Builder<E,S> bindDoubleIndexable(T s, Param1DBinder<E,Double,S,T> binder) {
        IndexableVal<Double,T> v = new IndexableVal<>(s,Double.class,double[].class);
        addParams(v);
        return binder.apply(v,this);
    }



    public <F,T extends Shape> Builder<E,S> bind(Class<F> v2e, T v2s, Param1Binder<E,F,S,T> binder){
        Val<F,T> v = new Val<>(v2s,v2e);
        addParams(v);
        return binder.apply(v,this);
    }

    /*
    public <E, S extends Shape> Builder<E,S> bind(Val<E,S> v, Param1Binder<E,S> binder){
        addParams(v);
        return binder.apply(v,this);
    }
    */

    public <F,T extends Shape> Builder<E,S> bind(VectorClass<F,T> v2, Param1Binder<E,F,S,T> binder){
        return this.bind(v2.getElementType(),v2.getShape(),binder);
    }

    public <F, G, T extends Shape, U extends Shape> Builder<E,S> bind(Class<F> v2e, T v2s, Class<G> v3e, U v3s, Param2Binder<E,F,G,S,T,U> binder){
        Val<G,U> v3; Val<F,T> v2;
        v2 = new Val<>(v2s,v2e);
        v3  = new Val<>(v3s,v3e);
        addParams(v2,v3);
        return binder.apply(v2,v3,this);
    }

    public <F, G, T extends Shape, U extends Shape> Builder<E,S> bind(Val<F,T> v2, Val<G,U> v3, Param2Binder<E,F,G,S,T,U> binder){
        addParams(v2,v3);
        return binder.apply(v2,v3,this);
    }

    public <F, G, T extends Shape, U extends Shape> Builder<E,S> bind(VectorClass<F,T> v2, VectorClass<G,U> v3, Param2Binder<E,F,G,S,T,U> binder){
        return this.bind(v2.getElementType(),v2.getShape(),v3.getElementType(),v3.getShape(),binder);

    }

    public <F, G, H, T extends Shape, U extends Shape, V extends Shape> Builder<E,S> bind(Class<F> v2e,T v2s, Class<G> v3e, U v3s, Class<H> v4e, V v4s, Param3Binder<E,F,G,H,S,T,U,V> binder){
        Val<F,T> v2; Val<G,U> v3; Val<H,V> v4;
        v2 = new Val<>(v2s,v2e);
        v3 = new Val<>(v3s,v3e);
        v4 = new Val<>(v4s,v4e);

        addParams(v2,v3,v4);
        return binder.apply(v2,v3,v4,this);
    }

    public <F, G, H, T extends Shape, U extends Shape, V extends Shape> Builder<E,S> bind(Val<E,S> v, Val<F,T> v2, Val<G,U> v3, Val<H,V> v4, Param3Binder<E,F,G,H,S,T,U,V> binder){
        addParams(v2,v3,v4);
        return binder.apply(v2,v3,v4,this);
    }

    public <F, G, H, T extends Shape, U extends Shape, V extends Shape> Builder<E,S> bind(VectorClass<F,T> v2, VectorClass<G,U> v3, VectorClass<H,V> v4, Param3Binder<E,F,G,H,S,T,U,V> binder){
        return this.bind(v2.getElementType(),v2.getShape(),v3.getElementType(),v3.getShape(),v4.getElementType(),v4.getShape(),binder);
    }

    public <F, G, H, I, T extends Shape, U extends Shape, V extends Shape, W extends Shape> Builder<E,S> bind(Class<F> v2e, T v2s, Class<G> v3e,U v3s, Class<H> v4e,V v4s, Class<I> v5e, W v5s, Param4Binder<E,F,G,H,I,S,T,U,V,W> binder){
        Val<F,T> v2; Val<G,U> v3; Val<H,V> v4; Val<I,W> v5;
        v2 = new Val<>(v2s,v2e);
        v3 = new Val<>(v3s,v3e);
        v4 = new Val<>(v4s,v4e);
        v5 = new Val<>(v5s,v5e);
        addParams(v2,v3,v4,v5);
        return binder.apply(v2,v3,v4,v5,this);
    }

    public <F, G, H, I, T extends Shape, U extends Shape, V extends Shape, W extends Shape> Builder<E,S> bind(Val<F,T> v2, Val<G,U> v3, Val<H,V> v4, Val<I,W> v5, Param4Binder<E,F,G,H,I,S,T,U,V,W> binder){
        addParams(v2,v3,v4,v5);
        return binder.apply(v2,v3,v4,v5,this);
    }

    public <F, G, H, I, T extends Shape, U extends Shape, V extends Shape, W extends Shape> Builder<E,S> bind(VectorClass<F,T> v2, VectorClass<G,U> v3, VectorClass<H,V> v4, VectorClass<I,W> v5, Param4Binder<E,F,G,H,I,S,T,U,V,W> binder){
        return this.bind(v2.getElementType(),v2.getShape(),v3.getElementType(),v3.getShape(),v4.getElementType(),v4.getShape(),v5.getElementType(),v5.getShape(),binder);
    }


    public Builder<E,S> return_(Expression<E,S> e){
        retexp = e;
        return this;
    }


    public MethodHandle build(OpProvider op){

        MHMeta[] mhs = new MHMeta[stmts.size()];
        for(int i = 0 ; i < stmts.size(); i++){
            Statement stmt = stmts.get(i);
            Assignment as = (Assignment) stmt;
            Val v = as.val;
            Expression<?,?> e = as.exp;
            ExpComp ec = new ExpComp(op);
            MethodHandle body = e.accept(ec).get();
            List<Val> vals = e.accept(ExpVarOrder.instance);
            mhs[i] = new MHMeta(v, vals,body);
        }

        ExpComp ec = new ExpComp(op);
        MethodHandle body = retexp.accept(ec).get();

        MHMeta result = new MHMeta(null,retexp.accept(ExpVarOrder.instance),body);

        result = result.normalize();
        for(int i = mhs.length-1; i >= 0; i--){
            result = result.substitute(mhs[i]);
            result = result.normalize();
        }

        result = MHMeta.rebind(result,this.params);

        return result.getBody();

    }

    @FunctionalInterface
    public interface Param1DBinder<E,F,S extends Shape, T extends Shape> {
        Builder<E,S> apply(IndexableVal<F,T> ival1, Builder<E,S> builder);
    }

    @FunctionalInterface
    public interface Param1Binder<E,F,S extends Shape,T extends Shape> {
        Builder<E,S> apply(Val<F,T> val1, Builder<E,S> builder);
    }

    @FunctionalInterface
    public interface Param2Binder<E,F,G,S extends Shape, T extends Shape, U extends Shape> {
        Builder<E,S> apply(Val<F,T> val1, Val<G,U> val2, Builder<E,S> builder);
    }

    @FunctionalInterface
    public interface Param3Binder<E,F,G,H,S extends Shape, T extends Shape, U extends Shape, V extends Shape> {
        Builder<E,S> apply(Val<F,T> val1, Val<G,U> val2, Val<H,V> val3, Builder<E,S> builder);
    }

    @FunctionalInterface
    public interface Param4Binder<E,F,G,H,I, S extends Shape, T extends Shape, U extends Shape, V extends Shape, W extends Shape> {
        Builder<E,S> apply(Val<F,T> val1, Val<G,U> val2, Val<H,V> val3, Val<I,W> val4, Builder<E,S> builder);
    }


    //Utilities
    void addVars(Val...vs){
        List<Val> vv = new ArrayList<>();
        vv.addAll(vals);
        for(Val v : vs){
            vv.add(v);
        }
        /*
        vals = new ImmutableList.Builder<Val>()
                .addAll(vals)
                .add(vs)
                .build();
        */
        vals = vv;
    }

    void addParams(Val...ps){
        List<Val> vv = new ArrayList<>();
        vv.addAll(params);
        for(Val p : ps){
           vv.add(p);
        }
        params = vv;
    }


    //Test Combinators
    public static <Z extends Shape> MethodHandle zip(Z shape, Param2Binder<Float,Float,Float,Z,Z,Z> binder) {

        OpProvider ops = OpProviders.provider(Float.class,shape.length());


        MethodHandle load = ops.getOp(Float.class,Ops.ARY_LOAD,shape.length());
        MethodHandle store = ops.getOp(Float.class,Ops.ARY_STORE,shape.length());

        Val<Float,Z> left  = new Val<>(shape,Float.class);
        Val<Float,Z> right = new Val<>(shape,Float.class);
        Builder b = new Builder();
        b.addParams(left,right);
        b = binder.apply(left,right,b); //(LongZ,LongZ)LongZ

        MethodHandle kernel = b.build(ops);

        //(float[],int,LongZ)LongZ
        kernel = MethodHandles.collectArguments(kernel,0,load);

        //(float[],int,float[],int)LongZ
        kernel = MethodHandles.collectArguments(kernel,2,load);

        //(float[],int,float[],int,float[],int)void
        kernel = MethodHandles.collectArguments(store,2,kernel);

        MethodType loopBodyTy = MethodType.methodType(void.class,int.class,float[].class,float[].class,float[].class);
        MethodType iterTy = MethodType.methodType(int.class,float[].class,float[].class,float[].class);

        //(int,float[],float[],float[])void
        kernel = MethodHandles.permuteArguments(kernel, loopBodyTy,1,0,2,0,3,0);




        //Build loop
        MethodHandle iterations = MethodHandles.arrayLength(float[].class);
        iterations = MethodHandles.permuteArguments(iterations,iterTy,0);


        //Divide
        try {
            MethodHandle dscaler = MethodHandles.lookup().findStatic(Builder.class,"divide",MethodType.methodType(int.class,int.class,int.class));
            dscaler = MethodHandles.collectArguments(dscaler,1,MethodHandles.constant(int.class,shape.length()));
            // Scale iterations down by shape length
            iterations = MethodHandles.filterReturnValue(iterations,dscaler);

            MethodHandle mscaler = MethodHandles.lookup().findStatic(Builder.class,"multiply",MethodType.methodType(int.class,int.class,int.class));
            mscaler = MethodHandles.collectArguments(mscaler,1,MethodHandles.constant(int.class,shape.length()));


            kernel = MethodHandles.collectArguments(kernel,0,mscaler);

            return MethodHandles.countedLoop(iterations,null,kernel);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static <Z extends Shape> MethodHandle reduce(Z shape, Param2Binder<Float,Float,Float,Z,Z,Z> binder){

        OpProvider ops = OpProviders.provider(Float.class,shape.length());

        MethodHandle load = ops.getOp(Float.class,Ops.ARY_LOAD,shape.length());

        Val<Float,Z> left  = new Val<>(shape,Float.class);
        Val<Float,Z> right = new Val<>(shape,Float.class);
        Builder b = new Builder();
        b.addParams(left,right);
        b = binder.apply(left,right,b); //(LongZ,LongZ)LongZ


        MethodHandle kernel = b.build(ops);

        //(LongZ,float[],int)LongZ
        kernel = MethodHandles.collectArguments(kernel,1,load);

        //(LongZ,int,float[])LongZ
        kernel = MethodHandles.permuteArguments(kernel,
                                                MethodType.methodType(kernel.type().returnType(),
                                                                      int.class,
                                                                      kernel.type().parameterArray()[0],
                                                                      float[].class),
                                                1,2,0);

        if(shape.length() == 8){
            Long4 zero = Long4.ZERO;
            MethodHandle init = MethodHandles.constant(Long4.class,zero);
            init = MethodHandles.permuteArguments(init,MethodType.methodType(Long4.class,float[].class)); //Dummy args
            MethodHandle iterations = MethodHandles.arrayLength(float[].class);

            MethodHandle incr, pred;

            try {
                incr = MethodHandles.lookup().findStatic(Builder.class,"increment",MethodType.methodType(int.class,int.class,int.class));
                incr = MethodHandles.collectArguments(incr,1,MethodHandles.constant(int.class,shape.length()));

                pred = MethodHandles.lookup().findStatic(Builder.class,"intLT",MethodType.methodType(boolean.class,int.class,int.class));
                pred = MethodHandles.collectArguments(pred,1,MethodHandles.arrayLength(float[].class));

                /*
                dscaler = MethodHandles.lookup().findStatic(Builder.class,"divide",MethodType.methodType(int.class,int.class,int.class));
                dscaler = MethodHandles.collectArguments(dscaler,1,MethodHandles.constant(int.class,shape.length()));

                mscaler = MethodHandles.lookup().findStatic(Builder.class,"multiply",MethodType.methodType(int.class,int.class,int.class));
                mscaler = MethodHandles.collectArguments(mscaler,1,MethodHandles.constant(int.class,shape.length()));
                */
            } catch (Throwable e){
                throw new Error(e);
            }
            incr = MethodHandles.permuteArguments(incr,MethodType.methodType(int.class,int.class,Long4.class,float[].class),0);
            pred = MethodHandles.permuteArguments(pred,MethodType.methodType(boolean.class,int.class,Long4.class,float[].class),0,2);
            MethodHandle fin = MethodHandles.permuteArguments(MethodHandles.identity(Long4.class),MethodType.methodType(Long4.class,int.class,Long4.class,float[].class),1);
            MethodHandle truepred = MethodHandles.permuteArguments(MethodHandles.constant(boolean.class,true),MethodType.methodType(boolean.class,int.class,Long4.class,float[].class));

            MethodHandle intZero = MethodHandles.dropArguments(MethodHandles.constant(int.class,0),0,int.class,Long4.class,float[].class);
            MethodHandle long4Zero = MethodHandles.dropArguments(MethodHandles.constant(Long4.class,Long4.ZERO),0,float[].class);
            //Scale upperbound
            //iterations = MethodHandles.filterReturnValue(iterations,dscaler);

            //Scale step
            //kernel     = MethodHandles.collectArguments(kernel,1,mscaler);

            //Loop State: (int i ,Long4 acc) (counter,accumulator)
            //Loop predicate (i < ary.length)
            //Loop Steps i = i + 8; acc = acc + load_acc(float[i]);
            //(V...,A...) = (int,Long4,float[])
            //(V...) = (int,Long4)
            //(A...) = (float[])
            MethodHandle[][] clauses = {
                    new MethodHandle[]{null,incr,pred,fin}, //int i = 0; i+=len; i <
                    new MethodHandle[]{long4Zero,kernel,truepred,fin} //Long4 = Long4.ZERO; kernel body
            };


            return MethodHandles.loop(clauses);

        } else if(shape.length() == 4) {
           throw new UnsupportedOperationException("4 not implemented yet.");
        } else {
            throw new UnsupportedOperationException("2 not implemented yet.");
        }

    }

    private static int divide(int num, int den){
        return num / den;
    }

    private static int multiply(int left, int right){
        return left * right;
    }

    private static int increment(int left, int right){
        return left + right;
    }

    private static boolean intLT(int left, int right){
        return left < right;
    }


}
