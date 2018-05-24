/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @modules java.base/jdk.internal.misc
 */

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.nicl.*;
import java.nicl.types.*;
import java.nicl.metadata.*;

public class UnixSystem {
    @NativeHeader
    static interface system {
        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@getpid")
        @NativeType(layout="()i32", ctype="dummy")
        public abstract int getpid();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@snprintf")
        @NativeType(layout="(u64:u8i64u64:u8*)i32", ctype="dummy")
        public abstract int snprintf(Pointer<Byte> buf, long size, Pointer<Byte> fmt, Object... args);

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@strerror")
        @NativeType(layout="(i32)u64:u8", ctype="dummy")
        public abstract Pointer<Byte> strerror(int errno);

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@errno")
        @NativeType(layout="i32", ctype="dummy")
        public abstract int errno$get();

        @NativeLocation(file="dummy", line=1, column=1, USR="c:@environ")
        @NativeType(layout="u64:u64:v", ctype="dummy", name="environ")
        public abstract Pointer<Pointer<Byte>> environ$get();

        public abstract Pointer<Pointer<Pointer<Byte>>> environ$ptr();
    }

    @NativeHeader
    static interface LinuxSystem {
        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@__xstat")
        @NativeType(layout="(i32u64:u8u64:$(linux_stat))i32", ctype="dummy")
        public abstract int __xstat(int ver, Pointer<Byte> path, Pointer<stat> buf);

        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        @NativeStruct("[i32i32i32i32i32i32i32i32i32i32i32i32i32](linux_stat)")
        static interface stat extends Struct<stat> {
            @Offset(offset=384l)
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@stat@st_size")
            @NativeType(layout="i32", ctype="off_t")
            int st_size$get();
            void st_size$set(int i);
        }
    }

    @NativeHeader
    static interface MacOSXSystem {
        @NativeLocation(file="dummy", line=1, column=1, USR="c:@F@stat")
        @NativeType(layout="(u64:u8u64:$(osx_stat))i32", ctype="dummy")
        public abstract int stat$INODE64(Pointer<Byte> path, Pointer<stat> buf);


        @NativeStruct("[i32u16u16u64u32u32i64[i64i64][i64i64][i64i64][i64i64]i64i64i32u32u32i32[2i64]](osx_stat)")
        @NativeLocation(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface stat extends Struct<stat> {
            @Offset(offset=768l)
            @NativeLocation(file="dummy", line=47, column=11, USR="c:@SA@stat@st_size")
            @NativeType(layout="i64", ctype="off_t")
            long st_size$get();
            void st_size$set(long i);
        }
    }

    private static final String OS = System.getProperty("os.name");

    public void testGetpid() {
        system i = Libraries.bind(MethodHandles.lookup(), system.class);

        long actual = i.getpid();
        long expected = ProcessHandle.current().pid();

        if (actual != expected) {
            throw new RuntimeException("Actual pid: " + actual + " does not match expected pid: " + expected);
        }
    }

    private static String lowerAndSprintf(system i, String fmt, Object... args) {
        System.err.println("lowerAndSprintf fmt=" + fmt);
        try (Scope scope = Scope.newNativeScope()) {
            long bufSize = 128;

            LayoutType<Byte> t = NativeTypes.UINT8;
            Pointer<Byte> buf = scope.allocate(t, bufSize);
            Pointer<Byte> cfmt = scope.toCString(fmt);

            int n = i.snprintf(buf, bufSize, cfmt, args);
            if (n >= bufSize) {
                throw new IndexOutOfBoundsException(n);
            }

            return Pointer.toString(buf);
        }
    }

    public void testPrintf() {
        system i = Libraries.bind(MethodHandles.lookup(), system.class);

        int n;

        assertEquals("foo", lowerAndSprintf(i, "foo"));
        assertEquals("foo: 4711", lowerAndSprintf(i, "foo: %d", 4711));
        assertEquals("foo: 47 11", lowerAndSprintf(i, "foo: %d %d", 47, 11));
        try (Scope scope = Scope.newNativeScope()) {
            assertEquals("foo: bar", lowerAndSprintf(i, "foo: %s", scope.toCString("bar")));
            assertEquals("foo: bar baz", lowerAndSprintf(i, "foo: %s %s", scope.toCString("bar"), scope.toCString("baz")));
        }
    }

    private int getSizeUsingStat(String path) throws Exception {
        switch (OS) {
        case "Linux":
            return getSizeUsingStat_Linux(path);
        case "Mac OS X":
            return getSizeUsingStat_MacOSX(path);
        default:
            // FIXME: Add other operating systems here...
            throw new UnsupportedOperationException(OS + " not supported (yet)");
        }
    }

    private int getSizeUsingStat_Linux(String path) throws Exception {
        LinuxSystem i = Libraries.bind(MethodHandles.lookup(), LinuxSystem.class);

        try (Scope scope = Scope.newNativeScope()) {
            LinuxSystem.stat s = scope.allocateStruct(LinuxSystem.stat.class);
            Pointer<LinuxSystem.stat> p = s.ptr();

            s = p.get();

            int res = i.__xstat(1, scope.toCString(path), p);
            if (res != 0) {
                throwErrnoException("Call to __xstat failed");
            }

            return s.st_size$get();
        }
    }

    private int getSizeUsingStat_MacOSX(String path) throws Exception {
        MacOSXSystem i = Libraries.bind(MethodHandles.lookup(), MacOSXSystem.class);

        try (Scope scope = Scope.newNativeScope()) {
            MacOSXSystem.stat s = scope.allocateStruct(MacOSXSystem.stat.class);
            Pointer<MacOSXSystem.stat> p = s.ptr();

            s = p.get();

            int res = i.stat$INODE64(scope.toCString(path), p);
            if (res != 0) {
                throwErrnoException("Call to stat failed");
            }

            return (int)s.st_size$get();
        }
    }

    private static void throwErrnoException(String msg) {
        try {
            system sys = Libraries.bind(MethodHandles.lookup(), system.class);
            Pointer<Byte> p = sys.strerror(sys.errno$get());
            throw new Exception(msg + ": " + Pointer.toString(p));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void testStat() {
        system i = Libraries.bind(MethodHandles.lookup(), system.class);

        int nBytes = 4711;

        try {
            File f = File.createTempFile("stat_test", null);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(new byte[nBytes]);
            fos.close();

            try {
                assertEquals(nBytes, getSizeUsingStat(f.getPath()));
            } finally {
                f.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            int size = getSizeUsingStat("__surely_this___file_does_NOT_exist.txt");
            throw new RuntimeException("stat unexpectedly succeeded");
        } catch (Exception e) {
            // expected
        }
    }


    public void testEnviron() {
        system i = Libraries.bind(MethodHandles.lookup(), system.class);

        {
            // Pointer version
            Pointer<Pointer<Byte>> pp = i.environ$get().cast(NativeTypes.UINT8.pointer());
            Pointer<Byte> sp = pp.get();
            System.out.println("testEnviron.str: " + Pointer.toString(sp));
        }

        {
            // Reference version
            Pointer<Pointer<Pointer<Byte>>> r = i.environ$ptr();
            Pointer<Pointer<Byte>> spp = r.get().cast(NativeTypes.UINT8.pointer());
            Pointer<Byte> sp = spp.get().cast(NativeTypes.UINT8);
        }
    }

    private static void assertEquals(long expected, long actual) {
        if (expected != actual) {
            throw new RuntimeException("actual: " + actual + " does not match expected: " + expected);
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new RuntimeException("actual: " + actual + " does not match expected: " + expected);
        }
    }

    public static void main(String[] args) {
        UnixSystem us = new UnixSystem();

        us.testGetpid();
        us.testPrintf();
        us.testStat();
        us.testEnviron();
    }
}
