/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @run testng/othervm --enable-native-access=ALL-UNNAMED StdLibTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdk.incubator.foreign.*;

import static jdk.incubator.foreign.MemoryAccess.*;

import org.testng.annotations.*;

import static jdk.incubator.foreign.CLinker.*;
import static org.testng.Assert.*;

@Test
public class StdLibTest {

    final static CLinker abi = CLinker.getInstance();

    private StdLibHelper stdLibHelper = new StdLibHelper();

    @Test(dataProvider = "stringPairs")
    void test_strcat(String s1, String s2) throws Throwable {
        assertEquals(stdLibHelper.strcat(s1, s2), s1 + s2);
    }

    @Test(dataProvider = "stringPairs")
    void test_strcmp(String s1, String s2) throws Throwable {
        assertEquals(Math.signum(stdLibHelper.strcmp(s1, s2)), Math.signum(s1.compareTo(s2)));
    }

    @Test(dataProvider = "strings")
    void test_puts(String s) throws Throwable {
        assertTrue(stdLibHelper.puts(s) >= 0);
    }

    @Test(dataProvider = "strings")
    void test_strlen(String s) throws Throwable {
        assertEquals(stdLibHelper.strlen(s), s.length());
    }

    @Test(dataProvider = "instants")
    void test_time(Instant instant) throws Throwable {
        StdLibHelper.Tm tm = stdLibHelper.gmtime(instant.getEpochSecond());
        LocalDateTime localTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        assertEquals(tm.sec(), localTime.getSecond());
        assertEquals(tm.min(), localTime.getMinute());
        assertEquals(tm.hour(), localTime.getHour());
        //day pf year in Java has 1-offset
        assertEquals(tm.yday(), localTime.getDayOfYear() - 1);
        assertEquals(tm.mday(), localTime.getDayOfMonth());
        //days of week starts from Sunday in C, but on Monday in Java, also account for 1-offset
        assertEquals((tm.wday() + 6) % 7, localTime.getDayOfWeek().getValue() - 1);
        //month in Java has 1-offset
        assertEquals(tm.mon(), localTime.getMonth().getValue() - 1);
        assertEquals(tm.isdst(), ZoneOffset.UTC.getRules()
                .isDaylightSavings(Instant.ofEpochMilli(instant.getEpochSecond() * 1000)));
    }

    @Test(dataProvider = "ints")
    void test_qsort(List<Integer> ints) throws Throwable {
        if (ints.size() > 0) {
            int[] input = ints.stream().mapToInt(i -> i).toArray();
            int[] sorted = stdLibHelper.qsort(input);
            Arrays.sort(input);
            assertEquals(sorted, input);
        }
    }

    @Test
    void test_rand() throws Throwable {
        int val = stdLibHelper.rand();
        for (int i = 0 ; i < 100 ; i++) {
            int newVal = stdLibHelper.rand();
            if (newVal != val) {
                return; //ok
            }
            val = newVal;
        }
        fail("All values are the same! " + val);
    }

    static class StdLibHelper {

        final static MethodHandle strcat = abi.downcallHandle(CLinker.systemLookup().lookup("strcat").get(),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER));

        final static MethodHandle strcmp = abi.downcallHandle(CLinker.systemLookup().lookup("strcmp").get(),
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));

        final static MethodHandle puts = abi.downcallHandle(CLinker.systemLookup().lookup("puts").get(),
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(C_INT, C_POINTER));

        final static MethodHandle strlen = abi.downcallHandle(CLinker.systemLookup().lookup("strlen").get(),
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(C_INT, C_POINTER));

        final static MethodHandle gmtime = abi.downcallHandle(
                CLinker.systemLookup().lookup(NativeTestHelper.IS_WINDOWS ? "_gmtime64" : "gmtime").get(),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER));

        final static MethodHandle qsort = abi.downcallHandle(CLinker.systemLookup().lookup("qsort").get(),
                MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_LONG_LONG, C_LONG_LONG, C_POINTER));

        final static FunctionDescriptor qsortComparFunction = FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER);

        final static MethodHandle qsortCompar;

        final static MethodHandle rand = abi.downcallHandle(CLinker.systemLookup().lookup("rand").get(),
                MethodType.methodType(int.class),
                FunctionDescriptor.of(C_INT));

        static {
            try {
                //qsort upcall handle
                qsortCompar = MethodHandles.lookup().findStatic(StdLibTest.StdLibHelper.class, "qsortCompare",
                        MethodType.methodType(int.class, MemorySegment.class, MemoryAddress.class, MemoryAddress.class));
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        String strcat(String s1, String s2) throws Throwable {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment buf = MemorySegment.allocateNative(s1.length() + s2.length() + 1, scope);
                MemorySegment other = toCString(s2, scope);
                char[] chars = s1.toCharArray();
                for (long i = 0 ; i < chars.length ; i++) {
                    setByteAtOffset(buf, i, (byte)chars[(int)i]);
                }
                setByteAtOffset(buf, chars.length, (byte)'\0');
                return toJavaString(((MemoryAddress)strcat.invokeExact(buf.address(), other.address())));
            }
        }

        int strcmp(String s1, String s2) throws Throwable {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment ns1 = toCString(s1, scope);
                MemorySegment ns2 = toCString(s2, scope);
                return (int)strcmp.invokeExact(ns1.address(), ns2.address());
            }
        }

        int puts(String msg) throws Throwable {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment s = toCString(msg, scope);
                return (int)puts.invokeExact(s.address());
            }
        }

        int strlen(String msg) throws Throwable {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment s = toCString(msg, scope);
                return (int)strlen.invokeExact(s.address());
            }
        }

        Tm gmtime(long arg) throws Throwable {
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                MemorySegment time = MemorySegment.allocateNative(8, scope);
                setLong(time, arg);
                return new Tm((MemoryAddress)gmtime.invokeExact(time.address()));
            }
        }

        static class Tm {

            //Tm pointer should never be freed directly, as it points to shared memory
            private final MemorySegment base;

            static final long SIZE = 56;

            Tm(MemoryAddress addr) {
                this.base = addr.asSegment(SIZE, ResourceScope.globalScope());
            }

            int sec() {
                return getIntAtOffset(base, 0);
            }
            int min() {
                return getIntAtOffset(base, 4);
            }
            int hour() {
                return getIntAtOffset(base, 8);
            }
            int mday() {
                return getIntAtOffset(base, 12);
            }
            int mon() {
                return getIntAtOffset(base, 16);
            }
            int year() {
                return getIntAtOffset(base, 20);
            }
            int wday() {
                return getIntAtOffset(base, 24);
            }
            int yday() {
                return getIntAtOffset(base, 28);
            }
            boolean isdst() {
                byte b = getByteAtOffset(base, 32);
                return b != 0;
            }
        }

        int[] qsort(int[] arr) throws Throwable {
            //init native array
            try (ResourceScope scope = ResourceScope.newConfinedScope()) {
                SegmentAllocator allocator = SegmentAllocator.ofScope(scope);
                MemorySegment nativeArr = allocator.allocateArray(C_INT, arr);

                //call qsort
                MemoryAddress qsortUpcallStub = abi.upcallStub(qsortCompar.bindTo(nativeArr), qsortComparFunction, scope);

                qsort.invokeExact(nativeArr.address(), (long)arr.length, C_INT.byteSize(), qsortUpcallStub);

                //convert back to Java array
                return nativeArr.toIntArray();
            }
        }

        static int qsortCompare(MemorySegment base, MemoryAddress addr1, MemoryAddress addr2) {
            return getIntAtOffset(base, addr1.segmentOffset(base)) -
                   getIntAtOffset(base, addr2.segmentOffset(base));
        }

        int rand() throws Throwable {
            return (int)rand.invokeExact();
        }
    }

    /*** data providers ***/

    @DataProvider
    public static Object[][] ints() {
        return perms(0, new Integer[] { 0, 1, 2, 3, 4 }).stream()
                .map(l -> new Object[] { l })
                .toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] strings() {
        return perms(0, new String[] { "a", "b", "c" }).stream()
                .map(l -> new Object[] { String.join("", l) })
                .toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] stringPairs() {
        Object[][] strings = strings();
        Object[][] stringPairs = new Object[strings.length * strings.length][];
        int pos = 0;
        for (Object[] s1 : strings) {
            for (Object[] s2 : strings) {
                stringPairs[pos++] = new Object[] { s1[0], s2[0] };
            }
        }
        return stringPairs;
    }

    @DataProvider
    public static Object[][] instants() {
        Instant start = ZonedDateTime.of(LocalDateTime.parse("2017-01-01T00:00:00"), ZoneOffset.UTC).toInstant();
        Instant end = ZonedDateTime.of(LocalDateTime.parse("2017-12-31T00:00:00"), ZoneOffset.UTC).toInstant();
        Object[][] instants = new Object[100][];
        for (int i = 0 ; i < instants.length ; i++) {
            Instant instant = start.plusSeconds((long)(Math.random() * (end.getEpochSecond() - start.getEpochSecond())));
            instants[i] = new Object[] { instant };
        }
        return instants;
    }

    static <Z> Set<List<Z>> perms(int count, Z[] arr) {
        if (count == arr.length) {
            return Set.of(List.of());
        } else {
            return Arrays.stream(arr)
                    .flatMap(num -> {
                        Set<List<Z>> perms = perms(count + 1, arr);
                        return Stream.concat(
                                //take n
                                perms.stream().map(l -> {
                                    List<Z> li = new ArrayList<>(l);
                                    li.add(num);
                                    return li;
                                }),
                                //drop n
                                perms.stream());
                    }).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
}
