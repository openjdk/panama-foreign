/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
  * @modules jdk.incubator.foreign/jdk.incubator.foreign.unsafe
 *          jdk.incubator.foreign/jdk.internal.foreign
 *          jdk.incubator.foreign/jdk.internal.foreign.abi
 *          java.base/sun.security.action
 * @build NativeTestHelper StdLibTest
 * @run testng StdLibTest
 * @run testng/othervm -Djdk.internal.foreign.NativeInvoker.FASTPATH=none -Djdk.internal.foreign.UpcallHandler.FASTPATH=none StdLibTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import jdk.incubator.foreign.SystemABI;
import jdk.incubator.foreign.unsafe.ForeignUnsafe;
import org.testng.annotations.*;

import static jdk.incubator.foreign.MemoryLayouts.*;
import static org.testng.Assert.*;

@Test
public class StdLibTest extends NativeTestHelper {

    final static SystemABI abi = SystemABI.getInstance();

    final static VarHandle byteHandle = MemoryHandles.varHandle(byte.class, ByteOrder.nativeOrder());
    final static VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
    final static VarHandle longHandle = MemoryHandles.varHandle(long.class, ByteOrder.nativeOrder());
    final static VarHandle byteArrHandle = arrayHandle(C_CHAR, byte.class);
    final static VarHandle intArrHandle = arrayHandle(C_INT, int.class);

    static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(1, elemLayout)
                .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }

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
        try (StdLibHelper.Tm tm = stdLibHelper.gmtime(instant.getEpochSecond())) {
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

    @Test(dataProvider = "printfArgs")
    void test_printf(List<PrintfArg> args) throws Throwable {
        String formatArgs = args.stream()
                .map(a -> a.format)
                .collect(Collectors.joining(","));

        String formatString = "hello(" + formatArgs + ")\n";

        String expected = String.format(formatString, args.stream()
                .map(a -> a.javaValue).toArray());

        int found = stdLibHelper.printf(formatString, args);
        assertEquals(found, expected.length());
    }

    static class StdLibHelper {

        final static MethodHandle strcat;
        final static MethodHandle strcmp;
        final static MethodHandle puts;
        final static MethodHandle strlen;
        final static MethodHandle gmtime;
        final static MethodHandle qsort;
        final static MethodHandle qsortCompar;
        final static FunctionDescriptor qsortComparFunction;
        final static MethodHandle rand;
        final static MemoryAddress printfAddr;
        final static FunctionDescriptor printfBase;

        static {
            try {
                LibraryLookup lookup = LibraryLookup.ofDefault();

                strcat = abi.downcallHandle(lookup.lookup("strcat"),
                        MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_POINTER, C_POINTER, C_POINTER));

                strcmp = abi.downcallHandle(lookup.lookup("strcmp"),
                        MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER));

                puts = abi.downcallHandle(lookup.lookup("puts"),
                        MethodType.methodType(int.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_INT, C_POINTER));

                strlen = abi.downcallHandle(lookup.lookup("strlen"),
                        MethodType.methodType(int.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_INT, C_POINTER));

                gmtime = abi.downcallHandle(lookup.lookup("gmtime"),
                        MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_POINTER, C_POINTER));

                qsortComparFunction = FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER);

                qsort = abi.downcallHandle(lookup.lookup("qsort"),
                        MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class, MemoryAddress.class),
                        FunctionDescriptor.ofVoid(C_POINTER, C_ULONG, C_ULONG, C_POINTER));

                //qsort upcall handle
                qsortCompar = MethodHandles.lookup().findStatic(StdLibTest.StdLibHelper.class, "qsortCompare",
                        MethodType.methodType(int.class, MemorySegment.class, MemoryAddress.class, MemoryAddress.class));

                rand = abi.downcallHandle(lookup.lookup("rand"),
                        MethodType.methodType(int.class),
                        FunctionDescriptor.of(C_INT));

                printfAddr = lookup.lookup("printf");

                printfBase = FunctionDescriptor.of(C_INT, C_POINTER);
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        }

        String strcat(String s1, String s2) throws Throwable {
            try (MemorySegment buf = MemorySegment.allocateNative(s1.length() + s2.length() + 1) ;
                 MemorySegment other = makeNativeString(s2)) {
                char[] chars = s1.toCharArray();
                for (long i = 0 ; i < chars.length ; i++) {
                    byteArrHandle.set(buf.baseAddress(), i, (byte)chars[(int)i]);
                }
                byteArrHandle.set(buf.baseAddress(), (long)chars.length, (byte)'\0');
                return toJavaString(((MemoryAddress)strcat.invokeExact(buf.baseAddress(), other.baseAddress())).rebase(buf));
            }
        }

        int strcmp(String s1, String s2) throws Throwable {
            try (MemorySegment ns1 = makeNativeString(s1) ;
                 MemorySegment ns2 = makeNativeString(s2)) {
                return (int)strcmp.invokeExact(ns1.baseAddress(), ns2.baseAddress());
            }
        }

        int puts(String msg) throws Throwable {
            try (MemorySegment s = makeNativeString(msg)) {
                return (int)puts.invokeExact(s.baseAddress());
            }
        }

        int strlen(String msg) throws Throwable {
            try (MemorySegment s = makeNativeString(msg)) {
                return (int)strlen.invokeExact(s.baseAddress());
            }
        }

        Tm gmtime(long arg) throws Throwable {
            try (MemorySegment time = MemorySegment.allocateNative(8)) {
                longHandle.set(time.baseAddress(), arg);
                return new Tm((MemoryAddress)gmtime.invokeExact(time.baseAddress()));
            }
        }

        static class Tm implements AutoCloseable {

            //Tm pointer should never be freed directly, as it points to shared memory
            private MemoryAddress base;

            static final long SIZE = 56;

            Tm(MemoryAddress base) {
                this.base = base.rebase(ForeignUnsafe.ofNativeUnchecked(base, SIZE));
            }

            int sec() {
                return (int)intHandle.get(base);
            }
            int min() {
                return (int)intHandle.get(base.addOffset(4));
            }
            int hour() {
                return (int)intHandle.get(base.addOffset(8));
            }
            int mday() {
                return (int)intHandle.get(base.addOffset(12));
            }
            int mon() {
                return (int)intHandle.get(base.addOffset(16));
            }
            int year() {
                return (int)intHandle.get(base.addOffset(20));
            }
            int wday() {
                return (int)intHandle.get(base.addOffset(24));
            }
            int yday() {
                return (int)intHandle.get(base.addOffset(28));
            }
            boolean isdst() {
                byte b = (byte)byteHandle.get(base.addOffset(32));
                return b == 0 ? false : true;
            }

            @Override
            public void close() throws Exception {
                base.segment().close();
            }
        }

        int[] qsort(int[] arr) throws Throwable {
            //init native array
            SequenceLayout seq = MemoryLayout.ofSequence(arr.length, C_INT);

            try (MemorySegment nativeArr = MemorySegment.allocateNative(seq)) {

                IntStream.range(0, arr.length)
                        .forEach(i -> intArrHandle.set(nativeArr.baseAddress(), i, arr[i]));

                //call qsort
                MemoryAddress qsortUpcallAddr = abi.upcallStub(qsortCompar.bindTo(nativeArr), qsortComparFunction);
                qsort.invokeExact(nativeArr.baseAddress(), seq.elementCount().getAsLong(), C_INT.byteSize(), qsortUpcallAddr);
                abi.freeUpcallStub(qsortUpcallAddr);

                //convert back to Java array
                return LongStream.range(0, arr.length)
                        .mapToInt(i -> (int)intArrHandle.get(nativeArr.baseAddress(), i))
                        .toArray();
            }
        }

        static int qsortCompare(MemorySegment base, MemoryAddress addr1, MemoryAddress addr2) {
            return (int)intHandle.get(addr1.rebase(base)) - (int)intHandle.get(addr2.rebase(base));
        }

        int rand() throws Throwable {
            return (int)rand.invokeExact();
        }

        int printf(String format, List<PrintfArg> args) throws Throwable {
            try (MemorySegment formatStr = makeNativeString(format)) {
                return (int)specializedPrintf(args).invokeExact(formatStr.baseAddress(),
                        args.stream().map(a -> a.nativeValue).toArray());
            }
        }

        private MethodHandle specializedPrintf(List<PrintfArg> args) {
            //method type
            MethodType mt = MethodType.methodType(int.class, MemoryAddress.class);
            FunctionDescriptor fd = printfBase;
            for (PrintfArg arg : args) {
                mt = mt.appendParameterTypes(arg.carrier);
                fd = fd.appendArgumentLayouts(arg.layout);
            }
            MethodHandle mh = abi.downcallHandle(printfAddr, mt, fd);
            return mh.asSpreader(1, Object[].class, args.size());
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

    @DataProvider
    public static Object[][] printfArgs() {
        ArrayList<List<PrintfArg>> res = new ArrayList<>();
        List<List<PrintfArg>> perms = new ArrayList<>(perms(0, PrintfArg.values()));
        for (int i = 0 ; i < 100 ; i++) {
            Collections.shuffle(perms);
            res.addAll(perms);
        }
        return res.stream()
                .map(l -> new Object[] { l })
                .toArray(Object[][]::new);
    }

    enum PrintfArg {
        INTEGRAL(int.class, asVarArg(C_INT), "%d", 42, 42),
        STRING(MemoryAddress.class, asVarArg(C_POINTER), "%s", makeNativeString("str").baseAddress(), "str"),
        CHAR(char.class, asVarArg(C_CHAR), "%c", 'h', 'h'),
        DOUBLE(double.class, asVarArg(C_DOUBLE), "%.4f", 1.2345d, 1.2345d);

        final Class<?> carrier;
        final MemoryLayout layout;
        final String format;
        final Object nativeValue;
        final Object javaValue;

        PrintfArg(Class<?> carrier, MemoryLayout layout, String format, Object nativeValue, Object javaValue) {
            this.carrier = carrier;
            this.layout = layout;
            this.format = format;
            this.nativeValue = nativeValue;
            this.javaValue = javaValue;
        }
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

    static MemorySegment makeNativeString(String value) {
        return makeNativeString(value, value.length() + 1);
    }

    static MemorySegment makeNativeString(String value, int length) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(length, C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        for (int i = 0 ; i < value.length() ; i++) {
            byteArrHandle.set(addr, i, (byte)value.charAt(i));
        }
        byteArrHandle.set(addr, (long)value.length(), (byte)0);
        return segment;
    }

    static String toJavaString(MemoryAddress address) {
        StringBuilder buf = new StringBuilder();
        byte curr = (byte)byteArrHandle.get(address, 0);
        long offset = 0;
        while (curr != 0) {
            buf.append((char)curr);
            curr = (byte)byteArrHandle.get(address, ++offset);
        }
        return buf.toString();
    }
}
