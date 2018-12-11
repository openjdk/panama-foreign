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

package jdk.internal.foreign.abi;

import java.foreign.Library;
import java.foreign.memory.LayoutType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import jdk.internal.foreign.Util;

/**
 *  This class implements native call invocation through specialized adapters. A specialized adapter is a native method
 *  which takes a number N of long arguments followed by a number M of double arguments; possible return types for the
 *  adapter are either long, double or void.
 */
public class DirectNativeInvoker {
    public static MethodHandle make(Library.Symbol symbol, CallingSequence callingSequence, LayoutType<?> ret, LayoutType<?>... args) {
        DirectSignatureShuffler shuffler =
                DirectSignatureShuffler.javaToNativeShuffler(callingSequence, Util.methodType(ret, args),
                        pos -> (pos == -1L) ? ret : args[pos]);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findStatic(DirectNativeInvoker.class,
                    "invokeNative_" + shuffler.nativeSigSuffix(),
                    shuffler.nativeMethodType().insertParameterTypes(0, long.class));
            mh = MethodHandles.insertArguments(mh, 0, symbol.getAddress().addr());
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
        return shuffler.adapt(mh);
    }

    //natives

    public static native void invokeNative_V_V(long addr);
    public static native void invokeNative_V_D(long addr, double arg0);
    public static native void invokeNative_V_J(long addr, long arg0);
    public static native void invokeNative_V_DD(long addr, double arg0, double arg1);
    public static native void invokeNative_V_JD(long addr, long arg0, double arg1);
    public static native void invokeNative_V_JJ(long addr, long arg0, long arg1);
    public static native void invokeNative_V_DDD(long addr, double arg0, double arg1, double arg2);
    public static native void invokeNative_V_JDD(long addr, long arg0, double arg1, double arg2);
    public static native void invokeNative_V_JJD(long addr, long arg0, long arg1, double arg2);
    public static native void invokeNative_V_JJJ(long addr, long arg0, long arg1, long arg2);
    public static native void invokeNative_V_DDDD(long addr, double arg0, double arg1, double arg2, double arg3);
    public static native void invokeNative_V_JDDD(long addr, long arg0, double arg1, double arg2, double arg3);
    public static native void invokeNative_V_JJDD(long addr, long arg0, long arg1, double arg2, double arg3);
    public static native void invokeNative_V_JJJD(long addr, long arg0, long arg1, long arg2, double arg3);
    public static native void invokeNative_V_JJJJ(long addr, long arg0, long arg1, long arg2, long arg3);
    public static native void invokeNative_V_DDDDD(long addr, double arg0, double arg1, double arg2, double arg3, double arg4);
    public static native void invokeNative_V_JDDDD(long addr, long arg0, double arg1, double arg2, double arg3, double arg4);
    public static native void invokeNative_V_JJDDD(long addr, long arg0, long arg1, double arg2, double arg3, double arg4);
    public static native void invokeNative_V_JJJDD(long addr, long arg0, long arg1, long arg2, double arg3, double arg4);
    public static native void invokeNative_V_JJJJD(long addr, long arg0, long arg1, long arg2, long arg3, double arg4);
    public static native void invokeNative_V_JJJJJ(long addr, long arg0, long arg1, long arg2, long arg3, long arg4);
    public static native long invokeNative_J_V(long addr);
    public static native long invokeNative_J_D(long addr, double arg0);
    public static native long invokeNative_J_J(long addr, long arg0);
    public static native long invokeNative_J_DD(long addr, double arg0, double arg1);
    public static native long invokeNative_J_JD(long addr, long arg0, double arg1);
    public static native long invokeNative_J_JJ(long addr, long arg0, long arg1);
    public static native long invokeNative_J_DDD(long addr, double arg0, double arg1, double arg2);
    public static native long invokeNative_J_JDD(long addr, long arg0, double arg1, double arg2);
    public static native long invokeNative_J_JJD(long addr, long arg0, long arg1, double arg2);
    public static native long invokeNative_J_JJJ(long addr, long arg0, long arg1, long arg2);
    public static native long invokeNative_J_DDDD(long addr, double arg0, double arg1, double arg2, double arg3);
    public static native long invokeNative_J_JDDD(long addr, long arg0, double arg1, double arg2, double arg3);
    public static native long invokeNative_J_JJDD(long addr, long arg0, long arg1, double arg2, double arg3);
    public static native long invokeNative_J_JJJD(long addr, long arg0, long arg1, long arg2, double arg3);
    public static native long invokeNative_J_JJJJ(long addr, long arg0, long arg1, long arg2, long arg3);
    public static native long invokeNative_J_DDDDD(long addr, double arg0, double arg1, double arg2, double arg3, double arg4);
    public static native long invokeNative_J_JDDDD(long addr, long arg0, double arg1, double arg2, double arg3, double arg4);
    public static native long invokeNative_J_JJDDD(long addr, long arg0, long arg1, double arg2, double arg3, double arg4);
    public static native long invokeNative_J_JJJDD(long addr, long arg0, long arg1, long arg2, double arg3, double arg4);
    public static native long invokeNative_J_JJJJD(long addr, long arg0, long arg1, long arg2, long arg3, double arg4);
    public static native long invokeNative_J_JJJJJ(long addr, long arg0, long arg1, long arg2, long arg3, long arg4);
    public static native double invokeNative_D_V(long addr);
    public static native double invokeNative_D_D(long addr, double arg0);
    public static native double invokeNative_D_J(long addr, long arg0);
    public static native double invokeNative_D_DD(long addr, double arg0, double arg1);
    public static native double invokeNative_D_JD(long addr, long arg0, double arg1);
    public static native double invokeNative_D_JJ(long addr, long arg0, long arg1);
    public static native double invokeNative_D_DDD(long addr, double arg0, double arg1, double arg2);
    public static native double invokeNative_D_JDD(long addr, long arg0, double arg1, double arg2);
    public static native double invokeNative_D_JJD(long addr, long arg0, long arg1, double arg2);
    public static native double invokeNative_D_JJJ(long addr, long arg0, long arg1, long arg2);
    public static native double invokeNative_D_DDDD(long addr, double arg0, double arg1, double arg2, double arg3);
    public static native double invokeNative_D_JDDD(long addr, long arg0, double arg1, double arg2, double arg3);
    public static native double invokeNative_D_JJDD(long addr, long arg0, long arg1, double arg2, double arg3);
    public static native double invokeNative_D_JJJD(long addr, long arg0, long arg1, long arg2, double arg3);
    public static native double invokeNative_D_JJJJ(long addr, long arg0, long arg1, long arg2, long arg3);
    public static native double invokeNative_D_DDDDD(long addr, double arg0, double arg1, double arg2, double arg3, double arg4);
    public static native double invokeNative_D_JDDDD(long addr, long arg0, double arg1, double arg2, double arg3, double arg4);
    public static native double invokeNative_D_JJDDD(long addr, long arg0, long arg1, double arg2, double arg3, double arg4);
    public static native double invokeNative_D_JJJDD(long addr, long arg0, long arg1, long arg2, double arg3, double arg4);
    public static native double invokeNative_D_JJJJD(long addr, long arg0, long arg1, long arg2, long arg3, double arg4);
    public static native double invokeNative_D_JJJJJ(long addr, long arg0, long arg1, long arg2, long arg3, long arg4);

    private static native void registerNatives();
    static {
        registerNatives();
    }
}
