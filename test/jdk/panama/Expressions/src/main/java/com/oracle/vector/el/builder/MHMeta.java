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
package com.oracle.vector.el.builder;

import com.oracle.vector.el.Val;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;

/*
    Transformations on MethodHandles to form expressions from Expression tree compositions.
 */
public class MHMeta {
    private final Val binder;
    private final List<Val> inputs;
    private final MethodHandle body;

    public MHMeta(Val binder, List<Val> inputs, MethodHandle body){
        this.binder  = binder;
        this.inputs  = inputs;
        this.body    = body;
    }

    public Val getBinder() {
        return binder;
    }

    public List<Val> getInputs() {
        return inputs;
    }

    public MethodHandle getBody() {
        return body;
    }

    public MHMeta substitute(MHMeta incoming){
        return substitute(this,incoming);
    }

    public MHMeta normalize(){
        return normalize(this);
    }


    //Here we normalize our MethodHandles so that all bound variables are presented as arguments only once.
    //All duplicate variables appearing after the first instance are dropped, types are changed to reflect this,
    //and MethodHandles permuted to map the data flow correctly.
    public static MHMeta normalize(MHMeta mhm){
        Val binder = mhm.getBinder();
        List<Val> vals = new ArrayList<>(mhm.getInputs());
        Set<Val> varset = new HashSet<>();
        varset.addAll(vals);
        MethodHandle mh = mhm.getBody();

        Map<Integer,Val> im = new HashMap<>();
        Map<Val,Integer> m = new HashMap<>();

        int ij = 0;
        for (Val v : varset) {
            m.put(v, ij);
            im.put(ij, v);
            ij++;
        }

        List<Val> valsp = varset.stream().collect(Collectors.toList());
        MethodType newTy = MethodType.methodType(mh.type().returnType(),valsp.stream()
                                                                             .map(v -> v.getPackedType())
                                                                             .toArray(Class<?>[]::new));
        int[] remap = vals.stream().mapToInt(m::get).toArray();

        mh = MethodHandles.permuteArguments(mh,newTy,remap);
        return new MHMeta(binder,valsp,mh);
    }

    public static boolean isNormal(MHMeta mhm){
        return new HashSet<>(mhm.getInputs()).size() == mhm.getInputs().size();
    }



    //mhm1[var] <= mhm2
    public static MHMeta substitute(MHMeta mhm1, MHMeta mhm2){
        List<Val> vars1, vars2;
        vars1 = mhm1.getInputs();
        vars2 = mhm2.getInputs();
        Val target = mhm2.getBinder();

        MethodHandle mh = mhm1.getBody();

        List<Integer> ixs = new ArrayList<>();
        for (int i = 0; i < vars1.size(); i++) {
            if(vars1.get(i).equals(target)){
               ixs.add(i);
            }
        }
        if(vars1.contains(target)){
            //Substitute vars
            List<Val> b = new ArrayList<Val>();
            for (Val val : vars1) {
               if(val.equals(target)){
                  b.addAll(vars2);
               } else {
                  b.add(val);
               }
            }

            //Connect MH's
            int offset = 0;
            for(int i = 0; i < ixs.size(); i++){
               mh = MethodHandles.collectArguments(mh,ixs.get(i)+offset,mhm2.getBody());
                offset+=vars2.size();
            }
            return new MHMeta(mhm1.getBinder(),b,mh);
        } else { //No substitutions available
           return mhm1;
        }
    }


    //This Takes an MHMeta object and rebinds its parameters given a list of in-order parameters
    //The return type is not effected.
    //Under the hood, this is simply a permute arguments on method handles.
    public static MHMeta rebind(MHMeta mhm, List<Val> vals){
        List<Val> inputs = mhm.getInputs();
        MethodHandle body = mhm.getBody();

        Set<Val> varsh = new HashSet<>(vals);
        Set<Val> inputsh = new HashSet<>(inputs);
        //Sanity checks:
        //1. The new rebound vals don't have any repeat vals.
        if(vals.size() != varsh.size()){
            throw new UnsupportedOperationException("Rebound var list can't have any repeated binders.");
        }
        //2. The target must be a subset of the rebound var list.
        for(Val s : varsh){
            inputsh.remove(s);
        }
        if(inputsh.size() > 0){
            throw new UnsupportedOperationException("Target binders must be a subset of the rebound binders.");
        }

        //Notes:
        //1. Rebound vals can be a superset of the target, but non-matching args will be dropped on invocation.
        int[] ixs = inputs.stream().mapToInt(vals::indexOf).toArray();
        Class<?>[] ixtypes = vals.stream().map(Val::getPackedType).toArray(Class<?>[]::new);
        MethodType newtype = MethodType.methodType(body.type().returnType(),ixtypes);
        MethodHandle newbody = MethodHandles.permuteArguments(body,newtype,ixs);

        return new MHMeta(mhm.binder, vals,newbody);


    }

    private static Class<?> deriveType(Val v){
        int len       = v.length();
        Class<?> elem = v.getValueLevelElementType();
        int bitLen;
        if(elem.equals(int.class) || elem.equals(Integer.class)
                || elem.equals(float.class) || elem.equals(Float.class)){
           bitLen = 32;
        } else if(elem.equals(short.class) || elem.equals(Short.class)
                || elem.equals(char.class) || elem.equals(Character.class)) {
           bitLen = 16;
        } else if(elem.equals(Byte.class) || elem.equals(byte.class)){
           bitLen = 8;
        } else {
           //Double, Long
           bitLen = 64;
        }

        if(len == 1) {
            return elem;
        } else {
            return bitLenToType(bitLen * len);
        }

    }

    private static Class<?> bitLenToType(int len){
        switch(len){
            case 64:
                return Long.class;
            case 128:
                return Long2.class;
            case 256:
                return Long4.class;
            case 512:
                return Long8.class;
            default:
                throw new UnsupportedOperationException("Invalid bitLen Received.");
        }
    }
}
