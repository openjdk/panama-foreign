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

/* @test
 * @summary unit tests for native call support
 * @modules java.base/jdk.internal.misc:+open
 * @run main/othervm NativeCallTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import jdk.internal.misc.Unsafe;

public class NativeCallTest {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final Object DLL = null;
    protected static final Unsafe U;

    static boolean TRACE = true;
    static void trace(String fmt, Object... args) {
        if (TRACE) {
            System.out.printf(fmt, args);
            System.out.println();
        }
    }

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    static final MethodHandle getpidMH;
    static final MethodHandle gettimeofdayMH;
    static final MethodHandle getloginMH;
    static final MethodHandle setloginMH;
    static final MethodHandle strlenMH;
    static final MethodHandle strerrorMH;

    static {
        try {
            getpidMH       = LOOKUP.findNative(DLL, "getpid", MethodType.methodType(/*pid_t*/int.class));
            gettimeofdayMH = LOOKUP.findNative(DLL, "gettimeofday", MethodType.methodType(int.class, /*ptr*/long.class, /*ptr*/long.class));
            getloginMH     = LOOKUP.findNative(DLL, "getlogin", MethodType.methodType(/*ptr*/long.class));
            setloginMH     = LOOKUP.findNative(DLL, "setlogin", MethodType.methodType(int.class, /*ptr*/long.class));
            strlenMH       = LOOKUP.findNative(DLL, "strlen", MethodType.methodType(/*size_t*/long.class, /*ptr*/long.class));
            strerrorMH     = LOOKUP.findNative(DLL, "strerror", MethodType.methodType(/*ptr*/long.class, int.class));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    /* pid_t getpid() */
    static int getpid() throws Throwable {
        return (int) getpidMH.invokeExact();
    }

    /* int gettimeofday(struct timeval *tp, struct timezone *tzp) */
    static int gettimeofday(long tv, long tz) throws Throwable {
        return (int)gettimeofdayMH.invokeExact(tv, tz);
    }

    /* char* getlogin() */
    static long getlogin() throws Throwable {
        return (long) getloginMH.invokeExact();
    }

    /* int setlogin(const char *name) */
    static int setlogin(long addr) throws Throwable {
        return (int) setloginMH.invokeExact(addr);
    }

    /* size_t strlen(const char *s) */
    static long strlen(long s) throws Throwable {
        return (long) strlenMH.invokeExact(s);
    }

    /* char* strerror(int errnum) */
    static long strerror(int errnum) throws Throwable {
        return (long) strerrorMH.invokeExact(errnum);
    }

    /* int* errno */
    static final long errnoAddr = U.findNativeAddress("errno"); // TODO: use Lookup.findNativeLocation() to produce a VarHandle?

    static int errno() {
        return U.getInt(errnoAddr); // TODO: use VarHandles?;
    }

    static void testGetPid() throws Throwable {
        int pid = getpid();
        trace("getpid: %d", pid);
    }

    /* ============================================================================================================== */
    /* int gettimeofday(struct timeval *restrict tp, void *restrict tzp); */

    /* FIXME: wire up with the groveller/LDL */

    /*
    struct timeval {
        time_t       tv_sec;   // seconds since Jan. 1, 1970
        suseconds_t  tv_usec;  // and microseconds
    };
    */
    interface Struct extends AutoCloseable {
        long getPayloadAddress();
    }

    static class StructImpl {
        protected long payload;

        protected StructImpl(int size) {
            payload = U.allocateMemory(size);
        }

        public long getPayloadAddress() {
            return payload;
        }

        public void close() throws Exception {
            U.freeMemory(payload);
        }
    }

    interface TimeVal extends Struct {
        long tv_sec();

        long tv_usec();
    }

    static class TimeValImpl extends StructImpl implements TimeVal {
        protected TimeValImpl() {
            super(16);
        }

        @Override
        public long tv_sec() {
            return U.getLong(payload + 0);
        }

        @Override
        public long tv_usec() {
            return U.getInt(payload + 8);
        }
    }

    /*
    struct timezone {
        int     tz_minuteswest; // of Greenwich
        int     tz_dsttime;     // type of dst correction to apply
    };
    */
    interface TimeZone extends Struct {
        int tz_minuteswest();

        int tz_dsttime();
    }

    static class TimeZoneImpl extends StructImpl implements TimeZone {
        protected TimeZoneImpl() {
            super(8);
        }

        @Override
        public int tz_minuteswest() {
            return U.getInt(payload + 0);
        }

        @Override
        public int tz_dsttime() {
            return U.getInt(payload + 4);
        }
    }

    static void testGetTimeOfDay() throws Throwable {
        try (TimeVal tv = new TimeValImpl()) {
            int res = gettimeofday(tv.getPayloadAddress(), /*NULL*/(long) 0);
            trace("gettimeofday: ret=%d TimeVal {tv_sec=%d tv_usec=%d}", res, tv.tv_sec(), tv.tv_usec());
        }
        try (TimeZone tz = new TimeZoneImpl()) {
            int res = gettimeofday(/*NULL*/(long) 0, tz.getPayloadAddress());
            trace("gettimeofday: ret=%d TimeZone {tz_minuteswest=%d tz_dsttime=%d}", res, tz.tz_minuteswest(), tz.tz_dsttime());
        }
        // NB: native memory allocated for tv & tz is freed.
    }

    /* ============================================================================================================== */

    static String errMsg(int errno) throws Throwable {
        long addr = strerror(errno);
        return native2string(addr);
    }

    static void testGetLogin() throws Throwable {
        long addr = getlogin();
        if (addr != 0) {
            String name = native2string(addr);
            trace("getlogin: %s", name);
        } else {
            int errno = errno();
            trace("getlogin: errno=%d(%s)", errno, errMsg(errno));
        }
    }

    static final long nameAddr = string2native("new_user_name");

    static void testSetLogin() throws Throwable {
        int res = setlogin(nameAddr);
        switch (res) {
            case 0: {
                trace("setlogin: success");
                break;
            }
            case -1: {
                int errno = errno();
                trace("setlogin: errno=%d/%s", errno, errMsg(errno));
                break;
            }
            default: {
                trace("setlogin: ret=%d", res);
            }
        }
    }

    static long string2native(String s) {
        byte[] bytes = s.getBytes();
        long address = U.allocateMemory(bytes.length + 1);
        for (int i = 0; i < bytes.length; i++) {
            U.putByte(address + i, bytes[i]);
        }
        U.putByte(address + bytes.length + 1, (byte) 0);
        return address;
    }

    static String native2string(long address) throws Throwable {
        long len = strlen(address);
        byte[] contents = new byte[(int) len];
        for (int i = 0; i < len; i++) {
            contents[i] = U.getByte(address + i);
        }
        return new String(contents, 0, contents.length);
    }

    /* ============================================================================================================== */

    //TODO: consider native invoker support.
    // It requires new VM entry point (callNative?) akin to MH.invokeBasic, but
    // the question is how to pass calling convention.
    public static void testNativeInvoker() throws Throwable {
//        MethodHandle mh = MethodHandles.nativeInvoker(TYPE);
//        long mAddr = LOOKUP.findNativeAddress(DLL, NAME);
//        int pid = (int) mh.invokeExact(mAddr);
//        System.out.printf("PID: %d\n", pid);
    }

    static void runTests() throws Throwable {
        testNativeInvoker();
        testGetPid();
        testGetTimeOfDay();
        testGetLogin();
        testSetLogin();
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("Interpreter:");
        runTests();

        System.out.println("Compiled:");
        TRACE=false;
        for (int i = 0; i < 1_000_000; i++) {
            runTests();
        }
        TRACE=true;
        runTests();
    }
}
