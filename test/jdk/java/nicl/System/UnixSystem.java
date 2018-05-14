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
import java.nicl.*;
import java.nicl.types.*;
import java.nicl.metadata.*;

public class UnixSystem {
    @Header(path="dummy")
    static interface system {
        @C(file="dummy", line=1, column=1, USR="c:@F@getpid")
        @NativeType(layout="()i", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract int getpid();

        @C(file="dummy", line=1, column=1, USR="c:@F@snprintf")
        @NativeType(layout="(p:clp:c*)i", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract int snprintf(Pointer<Byte> buf, long size, Pointer<Byte> fmt, Object... args);

        @C(file="dummy", line=1, column=1, USR="c:@F@strerror")
        @NativeType(layout="(i)p:c", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract Pointer<Byte> strerror(int errno);

        @C(file="dummy", line=1, column=1, USR="c:@errno")
        @NativeType(layout="(i", ctype="dummy", size=4)
        public abstract int errno$get();

        @C(file="dummy", line=1, column=1, USR="c:@environ")
        @NativeType(layout="p:p:V", ctype="dummy", size=8, name="environ")
        public abstract Pointer<Pointer<Byte>> environ$get();

        public abstract Reference<Pointer<Pointer<Byte>>> environ$ref();
    }

    @Header(path="dummy")
    static interface LinuxSystem {
        @C(file="dummy", line=1, column=1, USR="c:@F@__xstat")
        @NativeType(layout="(ip:cp:[iiiiiiiiiiiii])i", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract int __xstat(int ver, Pointer<Byte> path, Pointer<stat> buf);

        @NativeType(layout="[iiiiiiiiiiiii]", ctype="dummy", size=144, isRecordType=true)
        @C(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface stat extends Reference<stat> {
            @Offset(offset=384l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@stat@st_size")
            @NativeType(layout="i", ctype="off_t", size=4l)
            int st_size$get();
            void st_size$set(int i);
        }
    }

    @Header(path="dummy")
    static interface MacOSXSystem {
        @C(file="dummy", line=1, column=1, USR="c:@F@stat")
        @NativeType(layout="(ip:cp:[iiiiiiiiiiiiiiiiiiiiii])i", ctype="dummy", size=1)
        @CallingConvention(value=1)
        public abstract int stat$INODE64(Pointer<Byte> path, Pointer<stat> buf);

        @NativeType(layout="[iiiiiiiiiiiiiiiiiiiiii]", ctype="dummy", size=144, isRecordType=true)
        @C(file="dummy", line=47, column=11, USR="C:@S@MyStruct")
        static interface stat extends Reference<stat> {
            @Offset(offset=768l)
            @C(file="dummy", line=47, column=11, USR="c:@SA@stat@st_size")
            @NativeType(layout="l", ctype="off_t", size=4l)
            long st_size$get();
            void st_size$set(long i);
        }
    }

    private static final String OS = System.getProperty("os.name");

    public void testGetpid() {
        system i = Libraries.bindRaw(system.class);

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

            LayoutType<Byte> t = LayoutType.create(byte.class);
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
        system i = Libraries.bindRaw(system.class);

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
        LinuxSystem i = Libraries.bindRaw(LinuxSystem.class);

        try (Scope scope = Scope.newNativeScope()) {
            LinuxSystem.stat s = scope.allocateStruct(LayoutType.create(LinuxSystem.stat.class));
            Pointer<LinuxSystem.stat> p = s.ptr();

            s = p.deref();

            int res = i.__xstat(1, scope.toCString(path), p);
            if (res != 0) {
                throwErrnoException("Call to __xstat failed");
            }

            return s.st_size$get();
        }
    }

    private int getSizeUsingStat_MacOSX(String path) throws Exception {
        MacOSXSystem i = Libraries.bindRaw(MacOSXSystem.class);

        try (Scope scope = Scope.newNativeScope()) {
            MacOSXSystem.stat s = scope.allocateStruct(LayoutType.create(MacOSXSystem.stat.class));
            Pointer<MacOSXSystem.stat> p = s.ptr();

            s = p.deref();

            int res = i.stat$INODE64(scope.toCString(path), p);
            if (res != 0) {
                throwErrnoException("Call to stat failed");
            }

            return (int)s.st_size$get();
        }
    }

    private static void throwErrnoException(String msg) {
        try {
            system sys = Libraries.bindRaw(system.class);
            Pointer<Byte> p = sys.strerror(sys.errno$get());
            throw new Exception(msg + ": " + Pointer.toString(p));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void testStat() {
        system i = Libraries.bindRaw(system.class);

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
        system i = Libraries.bindRaw(system.class);

        {
            // Pointer version
            Pointer<Pointer<Byte>> pp = (Pointer)i.environ$get().cast(LayoutType.create(byte.class).ptrType());
            Pointer<Byte> sp = (Pointer)pp.lvalue().get().cast(LayoutType.create(byte.class));
            System.out.println("testEnviron.str: " + Pointer.toString(sp));
        }

        {
            // Reference version
            Reference<Pointer<Pointer<Byte>>> r = i.environ$ref();
            Pointer<Pointer<Byte>> spp = (Pointer)r.get().cast(LayoutType.create(byte.class).ptrType());
            Pointer<Byte> sp = (Pointer)spp.lvalue().get().cast(LayoutType.create(byte.class));
            System.out.println("testEnviron.str: " + Pointer.toString(sp));
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
