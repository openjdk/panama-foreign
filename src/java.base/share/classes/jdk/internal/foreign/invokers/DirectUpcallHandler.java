/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.foreign.invokers;

import jdk.internal.foreign.Util;
import jdk.internal.foreign.abi.CallingSequence;
import jdk.internal.foreign.abi.StorageClass;
import jdk.internal.vm.annotation.Stable;

import java.foreign.NativeTypes;
import java.foreign.layout.Function;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * This class implements upcall invocation from native code through a set of specialized entry points. A specialized
 * entry point is a Java method which takes a number N of long arguments followed by a number M of double arguments;
 * possible return types for the entry point are either long, double or void.
 */
class DirectUpcallHandler extends UpcallHandler {
    @Stable
    private final MethodHandle mh;

    DirectUpcallHandler(CallingSequence callingSequence, Method fiMethod, Function function, Object receiver) {
        super(callingSequence, fiMethod, function, receiver, up -> {
            MethodType mt = ((DirectUpcallHandler)up).mh.type();
            Class<?> retClass = mt.returnType();
            return allocateSpecializedUpcallStub(up,
                    (int)mt.parameterList().stream().filter(p -> p == long.class).count(),
                    (int)mt.parameterList().stream().filter(p -> p == double.class).count(),
                    encode(retClass));
        });

        try {
            MethodHandle mh = MethodHandles.publicLookup().unreflect(fiMethod);
            Util.checkNoArrays(mh.type());
            mh = mh.bindTo(receiver);
            MethodType methodType = mh.type();
            DirectSignatureShuffler shuffler =
                    DirectSignatureShuffler.nativeToJavaShuffler(callingSequence, methodType, this::layoutFor);
            this.mh = shuffler.adapt(mh);
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    private LayoutType<?> layoutFor(int pos) {
        return pos == -1 ?
                function.returnLayout().<LayoutType<?>>map(l -> Util.makeType(fiMethod.getGenericReturnType(), l)).orElse(NativeTypes.VOID) :
                Util.makeType(fiMethod.getGenericParameterTypes()[pos], function.argumentLayouts().get(pos));
    }

    private static int encode(Class<?> ret) {
        if (ret == double.class) {
            return 2;
        } else if (ret == void.class) {
            return 0;
        } else if (ret == long.class) {
            return 1;
        } else {
            throw new IllegalStateException("Unexpected carrier: " + ret.getName());
        }
    }

    /*** direct entry points ***/

    public static void invoke_V_V(DirectUpcallHandler handler) throws Throwable {
        handler.mh.invokeExact();
    }
    public static void invoke_V_D(DirectUpcallHandler handler, double arg0) throws Throwable {
        handler.mh.invokeExact(arg0);
    }
    public static void invoke_V_J(DirectUpcallHandler handler, long arg0) throws Throwable {
        handler.mh.invokeExact(arg0);
    }
    public static void invoke_V_DD(DirectUpcallHandler handler, double arg0, double arg1) throws Throwable {
        handler.mh.invokeExact(arg0, arg1);
    }
    public static void invoke_V_JD(DirectUpcallHandler handler, long arg0, double arg1) throws Throwable {
        handler.mh.invokeExact(arg0, arg1);
    }
    public static void invoke_V_JJ(DirectUpcallHandler handler, long arg0, long arg1) throws Throwable {
        handler.mh.invokeExact(arg0, arg1);
    }
    public static void invoke_V_DDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static void invoke_V_JDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static void invoke_V_JJD(DirectUpcallHandler handler, long arg0, long arg1, double arg2) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static void invoke_V_JJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static void invoke_V_DDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static void invoke_V_JDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static void invoke_V_JJDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static void invoke_V_JJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static void invoke_V_JJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static void invoke_V_DDDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static void invoke_V_JDDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static void invoke_V_JJDDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3, double arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static void invoke_V_JJJDD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3, double arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static void invoke_V_JJJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, double arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static void invoke_V_JJJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, long arg4) throws Throwable {
        handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_V(DirectUpcallHandler handler) throws Throwable {
        return (long)handler.mh.invokeExact();
    }
    public static long invoke_J_D(DirectUpcallHandler handler, double arg0) throws Throwable {
        return (long)handler.mh.invokeExact(arg0);
    }
    public static long invoke_J_J(DirectUpcallHandler handler, long arg0) throws Throwable {
        return (long)handler.mh.invokeExact(arg0);
    }
    public static long invoke_J_DD(DirectUpcallHandler handler, double arg0, double arg1) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1);
    }
    public static long invoke_J_JD(DirectUpcallHandler handler, long arg0, double arg1) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1);
    }
    public static long invoke_J_JJ(DirectUpcallHandler handler, long arg0, long arg1) throws Throwable {
        long l = (long)handler.mh.invokeExact(arg0, arg1);
//        if (true) {
//            throw new UnsupportedOperationException();
//        }
        return l;
    }
    public static long invoke_J_DDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static long invoke_J_JDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static long invoke_J_JJD(DirectUpcallHandler handler, long arg0, long arg1, double arg2) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static long invoke_J_JJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static long invoke_J_DDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static long invoke_J_JDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static long invoke_J_JJDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static long invoke_J_JJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static long invoke_J_JJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static long invoke_J_DDDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_JDDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_JJDDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_JJJDD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3, double arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_JJJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, double arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static long invoke_J_JJJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, long arg4) throws Throwable {
        return (long)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_V(DirectUpcallHandler handler) throws Throwable {
        return (double)handler.mh.invokeExact();
    }
    public static double invoke_D_D(DirectUpcallHandler handler, double arg0) throws Throwable {
        return (double)handler.mh.invokeExact(arg0);
    }
    public static double invoke_D_J(DirectUpcallHandler handler, long arg0) throws Throwable {
        return (double)handler.mh.invokeExact(arg0);
    }
    public static double invoke_D_DD(DirectUpcallHandler handler, double arg0, double arg1) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1);
    }
    public static double invoke_D_JD(DirectUpcallHandler handler, long arg0, double arg1) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1);
    }
    public static double invoke_D_JJ(DirectUpcallHandler handler, long arg0, long arg1) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1);
    }
    public static double invoke_D_DDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static double invoke_D_JDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static double invoke_D_JJD(DirectUpcallHandler handler, long arg0, long arg1, double arg2) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static double invoke_D_JJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2);
    }
    public static double invoke_D_DDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static double invoke_D_JDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static double invoke_D_JJDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static double invoke_D_JJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static double invoke_D_JJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3);
    }
    public static double invoke_D_DDDDD(DirectUpcallHandler handler, double arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_JDDDD(DirectUpcallHandler handler, long arg0, double arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_JJDDD(DirectUpcallHandler handler, long arg0, long arg1, double arg2, double arg3, double arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_JJJDD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, double arg3, double arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_JJJJD(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, double arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }
    public static double invoke_D_JJJJJ(DirectUpcallHandler handler, long arg0, long arg1, long arg2, long arg3, long arg4) throws Throwable {
        return (double)handler.mh.invokeExact(arg0, arg1, arg2, arg3, arg4);
    }

    // natives

    static native long allocateSpecializedUpcallStub(UpcallHandler handler, int nlongs, int ndoubles, int rettag);

    static native void registerNatives();
    static {
        registerNatives();
    }
}
