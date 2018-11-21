/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Build testsig library (testsig.c) first. */
public class NativeAdapterTest {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final Object DLL = null;

    static final MethodHandle sigZZ_MH;
    static final MethodHandle sigBB_MH;
    static final MethodHandle sigCC_MH;
    static final MethodHandle sigSS_MH;

    static final MethodHandle sigII_MH;
    static final MethodHandle sigJJ_MH;
    static final MethodHandle sigFF_MH;
    static final MethodHandle sigDD_MH;
    //    static final MethodHandle sigLL_MH;
    static final MethodHandle sigI6I_MH;
    static final MethodHandle sigJ6J_MH;
    static final MethodHandle sigF6F_MH;
    static final MethodHandle sigD6D_MH;
    //    static final MethodHandle sigL6L_MH;
    static final MethodHandle sigI10I_MH;
    static final MethodHandle sigJ10J_MH;
    static final MethodHandle sigF10F_MH;
    static final MethodHandle sigD10D_MH;
//    static final MethodHandle sigL10L_MH;

    static final MethodHandle sigIJFDIJFDIJFD_I_MH;
    static final MethodHandle sigJFDIJFDIJFDI_J_MH;
    static final MethodHandle sigFDIJFDIJFDIJ_F_MH;
    static final MethodHandle sigDIJFDIJFDIJF_D_MH;

    static final MethodHandle recMH;

    static {
        System.loadLibrary("testsig");
        try {
            sigZZ_MH = LOOKUP.findNative(DLL, "testSig_Z_Z", MethodType.methodType(boolean.class, boolean.class));
            sigBB_MH = LOOKUP.findNative(DLL, "testSig_B_B", MethodType.methodType(byte.class, byte.class));
            sigCC_MH = LOOKUP.findNative(DLL, "testSig_C_C", MethodType.methodType(char.class, char.class));
            sigSS_MH = LOOKUP.findNative(DLL, "testSig_S_S", MethodType.methodType(short.class, short.class));

            sigII_MH = LOOKUP.findNative(DLL, "testSig_I_I", MethodType.methodType(int.class, int.class));
            sigJJ_MH = LOOKUP.findNative(DLL, "testSig_J_J", MethodType.methodType(long.class, long.class));
            sigFF_MH = LOOKUP.findNative(DLL, "testSig_F_F", MethodType.methodType(float.class, float.class));
            sigDD_MH = LOOKUP.findNative(DLL, "testSig_D_D", MethodType.methodType(double.class, double.class));
            //sigLL_MH = LOOKUP.findNative(null, "testSig_L_L", MethodType.methodType(Object.class, Object.class));

            sigI6I_MH = LOOKUP.findNative(DLL, "testSig_I6_I",
                    MethodType.methodType(int.class, int.class, int.class, int.class, int.class, int.class, int.class));
            sigJ6J_MH = LOOKUP.findNative(DLL, "testSig_J6_J",
                    MethodType.methodType(long.class, long.class, long.class, long.class, long.class, long.class, long.class));
            sigF6F_MH = LOOKUP.findNative(DLL, "testSig_F6_F",
                    MethodType.methodType(float.class, float.class, float.class, float.class, float.class, float.class, float.class));
            sigD6D_MH = LOOKUP.findNative(DLL, "testSig_D6_D",
                    MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class, double.class));
            //sigL6L_MH = LOOKUP.findNative(null, "testSig_L6_L",
            //      MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));

            sigI10I_MH = LOOKUP.findNative(DLL, "testSig_I10_I",
                    MethodType.methodType(int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class));
            sigJ10J_MH = LOOKUP.findNative(DLL, "testSig_J10_J",
                    MethodType.methodType(long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class, long.class));
            sigF10F_MH = LOOKUP.findNative(DLL, "testSig_F10_F",
                    MethodType.methodType(float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class));
            sigD10D_MH = LOOKUP.findNative(DLL, "testSig_D10_D",
                    MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class));
            //sigL10L_MH = LOOKUP.findNative(null, "testSig_L10_L",
            //      MethodType.methodType(Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));

            sigIJFDIJFDIJFD_I_MH = LOOKUP.findNative(DLL, "testSig_IJFDIJFDIJFD_I",
                    MethodType.methodType(int.class, int.class, long.class, float.class, double.class,
                            int.class, long.class, float.class, double.class,
                            int.class, long.class, float.class, double.class));;
            sigJFDIJFDIJFDI_J_MH = LOOKUP.findNative(DLL, "testSig_JFDIJFDIJFDI_J",
                    MethodType.methodType(long.class, long.class, float.class, double.class, int.class,
                            long.class, float.class, double.class, int.class,
                            long.class, float.class, double.class, int.class));;
            sigFDIJFDIJFDIJ_F_MH = LOOKUP.findNative(DLL, "testSig_FDIJFDIJFDIJ_F",
                    MethodType.methodType(float.class, float.class, double.class, int.class, long.class,
                            float.class, double.class, int.class, long.class,
                            float.class, double.class, int.class, long.class));;
            sigDIJFDIJFDIJF_D_MH = LOOKUP.findNative(DLL, "testSig_DIJFDIJFDIJF_D",
                    MethodType.methodType(double.class, double.class, int.class, long.class, float.class,
                            double.class, int.class, long.class, float.class,
                            double.class, int.class, long.class, float.class));

            recMH = LOOKUP.findNative(DLL, "rec", MethodType.methodType(int.class, int.class));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static boolean testSigZZ(boolean b) throws Throwable { return (boolean)sigZZ_MH.invokeExact(b); };
    static byte    testSigBB(byte b)    throws Throwable { return (byte)sigBB_MH.invokeExact(b); };
    static char    testSigCC(char c)    throws Throwable { return (char)sigCC_MH.invokeExact(c); };
    static short   testSigSS(short s)   throws Throwable { return (short)sigSS_MH.invokeExact(s); };

    static int    testSigII(int i)    throws Throwable { return (int)sigII_MH.invokeExact(i); };
    static long   testSigJJ(long l)   throws Throwable { return (long)sigJJ_MH.invokeExact(l); };
    static float  testSigFF(float f)  throws Throwable { return (float)sigFF_MH.invokeExact(f); };
    static double testSigDD(double d) throws Throwable { return (double)sigDD_MH.invokeExact(d); };

    static int    testSigI6I() throws Throwable { return (int)sigI6I_MH.invokeExact(0, 1, 2, 3, 4, 5); };
    static long   testSigJ6J() throws Throwable { return (long)sigJ6J_MH.invokeExact(0L, 1L, 2L, 3L, 4L, 5L); };
    static float  testSigF6F() throws Throwable { return (float)sigF6F_MH.invokeExact(0.0F, 1.0F, 2.0F, 3.0F, 4.0F, 5.0F); };
    static double testSigD6D() throws Throwable { return (double)sigD6D_MH.invokeExact(0.0D, 1.0D, 2.0D, 3.0D, 4.0D, 5.0D); };

    static int    testSigI10I() throws Throwable { return (int)sigI10I_MH.invokeExact(0, 1, 2, 3, 4, 5, 6, 7, 8, 9); };
    static long   testSigJ10J() throws Throwable { return (long)sigJ10J_MH.invokeExact(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L); };
    static float  testSigF10F() throws Throwable { return (float)sigF10F_MH.invokeExact(0.0F, 1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F, 9.0F); };
    static double testSigD10D() throws Throwable { return (double)sigD10D_MH.invokeExact(0.0D, 1.0D, 2.0D, 3.0D, 4.0D, 5.0D, 6.0D, 7.0D, 8.0D, 9.0D); };

    static int    testSigIJFDIJFDIJFD_I() throws Throwable { return (int)sigIJFDIJFDIJFD_I_MH.invokeExact(0, 1L, 2.0F, 3.0D, 4, 5L, 6.0F, 7.0D, 8, 9L, 10.0F, 11.0D); };
    static long   testSigJFDIJFDIJFDI_J() throws Throwable { return (long)sigJFDIJFDIJFDI_J_MH.invokeExact(0L, 1.0F, 2.0D, 3, 4L, 5.0F, 6.0D, 7, 8L, 9.0F, 10.0D, 11); };
    static float  testSigFDIJFDIJFDIJ_F() throws Throwable { return (float)sigFDIJFDIJFDIJ_F_MH.invokeExact(0.0F, 1.0D, 2, 3L, 4.0F, 5.0D, 6, 7L, 8.0F, 9.0D, 10, 11L); };
    static double testSigDIJFDIJFDIJF_D() throws Throwable { return (double)sigDIJFDIJFDIJF_D_MH.invokeExact(0.0D, 1, 2L, 3.0F, 4.0D, 5, 6L, 7.0F, 8.0D, 9, 10L, 11.0F); };

    static void enumerate() throws Throwable {
        assertTrue(!testSigZZ(false));
        assertTrue(testSigZZ(true));

        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            assertTrue(testSigBB((byte)i) == i);
        }
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            assertTrue(testSigCC((char) i) == i);
        }
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            assertTrue(testSigSS((short)i) == i);
        }
    }

    static void testSignatures() throws Throwable {
        for (int i = 0; i < 20_000; i++) {
            assertTrue(testSigII(Integer.MIN_VALUE) == Integer.MIN_VALUE);
            assertTrue(testSigII(-1) == -1);
            assertTrue(testSigII(0) == 0);
            assertTrue(testSigII(1) == 1);
            assertTrue(testSigII(Integer.MAX_VALUE) == Integer.MAX_VALUE);

            assertTrue(testSigJJ(Long.MIN_VALUE) == Long.MIN_VALUE);
            assertTrue(testSigJJ(-1L) == -1L);
            assertTrue(testSigJJ(0L) == 0L);
            assertTrue(testSigJJ(1L) == 1L);
            assertTrue(testSigJJ(Long.MAX_VALUE) == Long.MAX_VALUE);

            assertTrue(testSigFF(Float.NEGATIVE_INFINITY) == Float.NEGATIVE_INFINITY);
            assertTrue(testSigFF(Float.MIN_VALUE) == Float.MIN_VALUE);
            assertTrue(testSigFF(-Float.MIN_NORMAL) == -Float.MIN_NORMAL);
            assertTrue(testSigFF(-0.0F) == -0.0F);
            assertTrue(testSigFF(-0.0F) == 0.0F);
            assertTrue(testSigFF(0.0F) == -0.0F);
            assertTrue(testSigFF(0.0F) == 0.0F);
            assertTrue(testSigFF(Float.MIN_NORMAL) == Float.MIN_NORMAL);
            assertTrue(testSigFF(Float.MAX_VALUE) == Float.MAX_VALUE);
            assertTrue(testSigFF(Float.POSITIVE_INFINITY) == Float.POSITIVE_INFINITY);

            assertTrue(testSigDD(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY);
            assertTrue(testSigDD(Double.MIN_VALUE) == Double.MIN_VALUE);
            assertTrue(testSigDD(-Double.MIN_NORMAL) == -Double.MIN_NORMAL);
            assertTrue(testSigDD(-0.0D) == -0.0D);
            assertTrue(testSigDD(-0.0D) == 0.0D);
            assertTrue(testSigDD(0.0D) == -0.0D);
            assertTrue(testSigDD(0.0D) == 0.0D);
            assertTrue(testSigDD(Double.MIN_NORMAL) == Double.MIN_NORMAL);
            assertTrue(testSigDD(Double.MAX_VALUE) == Double.MAX_VALUE);
            assertTrue(testSigDD(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY);

            assertTrue(testSigI6I() == 5);
            assertTrue(testSigJ6J() == 5L);
            assertTrue(testSigF6F() == 5.0F);
            assertTrue(testSigD6D() == 5.0D);

            assertTrue(testSigI10I() == 9);
            assertTrue(testSigJ10J() == 9L);
            assertTrue(testSigF10F() == 9.0F);
            assertTrue(testSigD10D() == 9.0D);

            assertTrue(testSigIJFDIJFDIJFD_I() == 8);
            assertTrue(testSigJFDIJFDIJFDI_J() == 8L);
            assertTrue(testSigFDIJFDIJFDIJ_F() == 8.0F);
            assertTrue(testSigDIJFDIJFDIJF_D() == 8.0D);
        }
    }

    static void testStackOverflow() throws Throwable {
        /*
        for (int depth = 1; depth < 1_000_000; depth += 1) {
            System.out.println("depth="+depth);
            int res = (int)recMH.invokeExact(depth);
        }
        */
        // Try to unguard yellow pages
        int res = (int)recMH.invokeExact(960); // close to native stack overflow
        try { rec(); } catch (StackOverflowError e) {/*expected*/}
    }
    static void rec() { rec(); }

    public static void main(String[] args) throws Throwable {
        enumerate();
        testSignatures();
        testStackOverflow();
    }

    static void assertTrue(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }
}
