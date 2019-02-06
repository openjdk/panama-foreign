/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @run testng StdLibTest
 * @run testng/othervm -Djdk.internal.foreign.NativeInvoker.FASTPATH=none -Djdk.internal.foreign.UpcallHandler.FASTPATH=none StdLibTest
 */

import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeSetter;
import java.foreign.memory.*;
import java.lang.invoke.MethodHandles;
import java.foreign.Libraries;
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeStruct;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.*;

import static org.testng.Assert.*;

@Test
public class StdLibTest {

    private StdLibHelper stdLibHelper = new StdLibHelper();

    @Test(dataProvider = "stringPairs")
    void test_strcat(String s1, String s2) {
        assertEquals(stdLibHelper.strcat(s1, s2), s1 + s2);
    }

    @Test(dataProvider = "stringPairs")
    void test_strcmp(String s1, String s2) {
        assertEquals(Math.signum(stdLibHelper.strcmp(s1, s2)), Math.signum(s1.compareTo(s2)));
    }

    @Test(dataProvider = "strings")
    void test_puts(String s) {
        assertTrue(stdLibHelper.puts(s) >= 0);
    }

    @Test(dataProvider = "strings")
    void test_strlen(String s) {
        assertEquals(stdLibHelper.strlen(s), s.length());
    }

    @Test(dataProvider = "instants")
    void test_time(Instant instant) {
        try (Scope s = Scope.newNativeScope()) {
            StdLibHelper.Time time = s.allocateStruct(StdLibHelper.Time.class);
            time.setSeconds(instant.getEpochSecond());
            //numbers should be in the same ballpark
            assertEquals(time.seconds(), instant.getEpochSecond());
            @SuppressWarnings("unchecked")
            StdLibHelper.Tm tm = stdLibHelper.gmtime(time.ptr()).get();
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
                    .isDaylightSavings(Instant.ofEpochMilli(time.seconds() * 1000)));
        }
    }

    @Test(dataProvider = "ints")
    void test_qsort(List<Integer> ints) {
        if (ints.size() > 0) {
            int[] input = ints.stream().mapToInt(i -> i).toArray();
            int[] sorted = stdLibHelper.qsort(input);
            Arrays.sort(input);
            assertEquals(sorted, input);
        }
    }

    @Test
    void test_rand() {
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
    void test_printf(List<PrintfArg> args) {
        try (Scope s = Scope.newNativeScope()) {
            String formatArgs = args.stream()
                    .map(a -> a.format)
                    .collect(Collectors.joining(","));

            String formatString = "hello(" + formatArgs + ")\n";

            String expected = String.format(formatString, args.stream()
                    .map(a -> a.javaValue).toArray());

            int found = stdLibHelper.printf(formatString, args.stream().map(a -> a.nativeValueFunc.apply(s)).toArray());
            assertEquals(found, expected.length());
        }
    }

    static class StdLibHelper {
        StdLib stdLib = Libraries.bind(MethodHandles.lookup(), StdLib.class);

        String strcat(String s1, String s2) {
            try (Scope s = Scope.newNativeScope()) {
                Pointer<Byte> buf = s.allocate(NativeTypes.INT8, s1.length() + s2.length() + 1);
                Pointer<Byte> base = buf;
                for (char c : s1.toCharArray()) {
                    buf.set((byte)c);
                    buf = buf.offset(1);
                }
                buf.set((byte)'\0');
                return Pointer.toString(stdLib.strcat(base, s.allocateCString(s2)));
            }
        }

        int strcmp(String s1, String s2) {
            try (Scope s = Scope.newNativeScope()) {
                return stdLib.strcmp(s.allocateCString(s1), s.allocateCString(s2));
            }
        }

        int puts(String msg) {
            try (Scope s = Scope.newNativeScope()) {
                return stdLib.puts(s.allocateCString(msg));
            }
        }

        int strlen(String msg) {
            try (Scope s = Scope.newNativeScope()) {
                return stdLib.strlen(s.allocateCString(msg));
            }
        }

        Pointer<Tm> gmtime(Pointer<Time> arg) {
            return stdLib.gmtime(arg);
        }

        int[] qsort(int[] array) {
            try (Scope s = Scope.newNativeScope()) {
                //allocate the array
                Array<Integer> arr = s.allocateArray(NativeTypes.INT32, array);
                
                //call the function
                stdLib.qsort(arr.elementPointer(), array.length, 4,
                        s.allocateCallback((u1, u2) -> {
                                int i1 = u1.get();
                                int i2 = u2.get();
                                return i1 - i2;
                        }));
                //get result
                return arr.toArray(int[]::new);
            }
        }

        int rand() {
            return stdLib.rand();
        }

        int printf(String format, Object... args) {
            try (Scope sc = Scope.newNativeScope()) {
                return stdLib.printf(sc.allocateCString(format), args);
            }
        }

        Pointer<Void> fopen(String filename, String mode) {
            try (Scope s = Scope.newNativeScope()) {
                return stdLib.fopen(s.allocateCString(filename), s.allocateCString(mode));
            }
        }

        @NativeHeader
        public interface StdLib {
            @NativeFunction("(u64:u8)i32")
            int puts(Pointer<Byte> str);
            @NativeFunction("(u64:u8u64:i8)u64:u8")
            Pointer<Byte> strcat(Pointer<Byte> s1, Pointer<Byte> s2);
            @NativeFunction("(u64:u8u64:i8)i32")
            int strcmp(Pointer<Byte> s1, Pointer<Byte> s2);
            @NativeFunction("(u64:u8)i32")
            int strlen(Pointer<Byte> s2);
            @NativeFunction("(u64:${Time})${Time}")
            Time time(Pointer<Time> arg);
            @NativeFunction("(u64:${Time})u64:${Tm}")
            Pointer<Tm> gmtime(Pointer<Time> arg);
            @NativeFunction("(u64:[0i32]i32i32u64:(u64:i32u64:i32)i32)v")
            void qsort(Pointer<Integer> base, int nitems, int size, Callback<QsortComparator> comparator);
            @NativeFunction("()i32")
            int rand();
            @NativeFunction("(u64:u8*)i32")
            int printf(Pointer<Byte> format, Object... args);
            @NativeFunction("(u64:u8u64:i8)u64:v")
            Pointer<Void> fopen(Pointer<Byte> filename, Pointer<Byte> mode);

            @NativeCallback("(u64:i32u64:i32)i32")
            interface QsortComparator {
                int compare(Pointer<Integer> u1, Pointer<Integer> u2);
            }
        }

        @NativeStruct("[" +
                "   i64(seconds)" +
                "](Time)")
        public interface Time extends Struct<Time> {
            @NativeGetter("seconds")
            long seconds();
            @NativeSetter("seconds")
            void setSeconds(long secs);
        }

        @NativeStruct("[" +
                "   i32(sec)" +
                "   i32(min)" +
                "   i32(hour)" +
                "   i32(mday)" +
                "   i32(mon)" +
                "   i32(year)" +
                "   i32(wday)" +
                "   i32(yday)" +
                "   i8(isdst)" +
                "](Tm)")
        public interface Tm extends Struct<Tm> {
            @NativeGetter("sec")
            int sec();
            @NativeGetter("min")
            int min();
            @NativeGetter("hour")
            int hour();
            @NativeGetter("mday")
            int mday();
            @NativeGetter("mon")
            int mon();
            @NativeGetter("year")
            int year();
            @NativeGetter("wday")
            int wday();
            @NativeGetter("yday")
            int yday();
            @NativeGetter("isdst")
            boolean isdst();
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
        INTEGRAL("%d", s -> 42, 42),
        STRING("%s", s -> s.allocateCString("str"), "str"),
        CHAR("%c", s -> 'h', 'h'),
        FLOAT("%.4f", s -> 1.2345d, 1.2345d);

        final String format;
        final Function<Scope, Object> nativeValueFunc;
        final Object javaValue;

        PrintfArg(String format, Function<Scope, Object> nativeValueFunc, Object javaValue) {
            this.format = format;
            this.nativeValueFunc = nativeValueFunc;
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
}
