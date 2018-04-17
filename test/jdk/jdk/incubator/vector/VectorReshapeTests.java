import jdk.incubator.vector.*;
import jdk.internal.vm.annotation.ForceInline;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

/**
 * @test
 * @modules jdk.incubator.vector
 * @modules java.base/jdk.internal.vm.annotation
 * @run testng VectorReshapeTests
 */
 
@Test
public class VectorReshapeTests {
    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);
    static final int NUM_ITER = 200 * INVOC_COUNT;

    static final IntVector.IntSpecies<Shapes.S64Bit> ispec64 = IntVector.speciesInstance(Shapes.S_64_BIT);
    static final FloatVector.FloatSpecies<Shapes.S64Bit> fspec64 = FloatVector.speciesInstance(Shapes.S_64_BIT);
    static final LongVector.LongSpecies<Shapes.S64Bit> lspec64 = LongVector.speciesInstance(Shapes.S_64_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S64Bit> dspec64 = DoubleVector.speciesInstance(Shapes.S_64_BIT);
    static final ByteVector.ByteSpecies<Shapes.S64Bit> bspec64 = ByteVector.speciesInstance(Shapes.S_64_BIT);
    static final ShortVector.ShortSpecies<Shapes.S64Bit> sspec64 = ShortVector.speciesInstance(Shapes.S_64_BIT);

    static final IntVector.IntSpecies<Shapes.S128Bit> ispec128 = IntVector.speciesInstance(Shapes.S_128_BIT);
    static final FloatVector.FloatSpecies<Shapes.S128Bit> fspec128 = FloatVector.speciesInstance(Shapes.S_128_BIT);
    static final LongVector.LongSpecies<Shapes.S128Bit> lspec128 = LongVector.speciesInstance(Shapes.S_128_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S128Bit> dspec128 = DoubleVector.speciesInstance(Shapes.S_128_BIT);
    static final ByteVector.ByteSpecies<Shapes.S128Bit> bspec128 = ByteVector.speciesInstance(Shapes.S_128_BIT);
    static final ShortVector.ShortSpecies<Shapes.S128Bit> sspec128 = ShortVector.speciesInstance(Shapes.S_128_BIT);

    static final IntVector.IntSpecies<Shapes.S256Bit> ispec256 = IntVector.speciesInstance(Shapes.S_256_BIT);
    static final FloatVector.FloatSpecies<Shapes.S256Bit> fspec256 = FloatVector.speciesInstance(Shapes.S_256_BIT);
    static final LongVector.LongSpecies<Shapes.S256Bit> lspec256 = LongVector.speciesInstance(Shapes.S_256_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S256Bit> dspec256 = DoubleVector.speciesInstance(Shapes.S_256_BIT);
    static final ByteVector.ByteSpecies<Shapes.S256Bit> bspec256 = ByteVector.speciesInstance(Shapes.S_256_BIT);
    static final ShortVector.ShortSpecies<Shapes.S256Bit> sspec256 = ShortVector.speciesInstance(Shapes.S_256_BIT);

    static final IntVector.IntSpecies<Shapes.S512Bit> ispec512 = IntVector.speciesInstance(Shapes.S_512_BIT);
    static final FloatVector.FloatSpecies<Shapes.S512Bit> fspec512 = FloatVector.speciesInstance(Shapes.S_512_BIT);
    static final LongVector.LongSpecies<Shapes.S512Bit> lspec512 = LongVector.speciesInstance(Shapes.S_512_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S512Bit> dspec512 = DoubleVector.speciesInstance(Shapes.S_512_BIT);
    static final ByteVector.ByteSpecies<Shapes.S512Bit> bspec512 = ByteVector.speciesInstance(Shapes.S_512_BIT);
    static final ShortVector.ShortSpecies<Shapes.S512Bit> sspec512 = ShortVector.speciesInstance(Shapes.S_512_BIT);

    static <T> IntFunction<T> withToString(String s, IntFunction<T> f) {
        return new IntFunction<T>() {
            @Override
            public T apply(int v) {
                return f.apply(v);
            }

            @Override
            public String toString() {
                return s;
            }
        };
    }

    interface ToByteF {
        byte apply(int i);
    }

    static byte[] fill_byte(int s , ToByteF f) {
        return fill_byte(new byte[s], f);
    }

    static byte[] fill_byte(byte[] a, ToByteF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToBoolF {
        boolean apply(int i);
    }

    static boolean[] fill_bool(int s , ToBoolF f) {
        return fill_bool(new boolean[s], f);
    }

    static boolean[] fill_bool(boolean[] a, ToBoolF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToShortF {
        short apply(int i);
    }

    static short[] fill_short(int s , ToShortF f) {
        return fill_short(new short[s], f);
    }

    static short[] fill_short(short[] a, ToShortF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToIntF {
        int apply(int i);
    }

    static int[] fill_int(int s , ToIntF f) {
        return fill_int(new int[s], f);
    }

    static int[] fill_int(int[] a, ToIntF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToLongF {
        long apply(int i);
    }

    static long[] fill_long(int s , ToLongF f) {
        return fill_long(new long[s], f);
    }

    static long[] fill_long(long[] a, ToLongF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToFloatF {
        float apply(int i);
    }

    static float[] fill_float(int s , ToFloatF f) {
        return fill_float(new float[s], f);
    }

    static float[] fill_float(float[] a, ToFloatF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    interface ToDoubleF {
        double apply(int i);
    }

    static double[] fill_double(int s , ToDoubleF f) {
        return fill_double(new double[s], f);
    }

    static double[] fill_double(double[] a, ToDoubleF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    static final List<IntFunction<byte[]>> BYTE_GENERATORS = List.of(
            withToString("byte(i)", (int s) -> {
                return fill_byte(s, i -> (byte)i);
            })
    );

    @DataProvider
    public Object[][] byteUnaryOpProvider() {
        return BYTE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }
    
    static final List<IntFunction<boolean[]>> BOOL_GENERATORS = List.of(
        withToString("boolean(i%3)", (int s) -> {
            return fill_bool(s, i -> i % 3 == 0);
        })
    );

    @DataProvider
    public Object[][] booleanUnaryOpProvider() {
        return BOOL_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<short[]>> SHORT_GENERATORS = List.of(
            withToString("short(i)", (int s) -> {
                return fill_short(s, i -> (short)i);
            })
    );

    @DataProvider
    public Object[][] shortUnaryOpProvider() {
        return SHORT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }
    
    static final List<IntFunction<int[]>> INT_GENERATORS = List.of(
            withToString("int(i)", (int s) -> {
                return fill_int(s, i -> (int)i);
            })
    );

    @DataProvider
    public Object[][] intUnaryOpProvider() {
        return INT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<long[]>> LONG_GENERATORS = List.of(
            withToString("long(i)", (int s) -> {
                return fill_long(s, i -> (long)i);
            })
    );

    @DataProvider
    public Object[][] longUnaryOpProvider() {
        return LONG_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<float[]>> FLOAT_GENERATORS = List.of(
            withToString("float(i)", (int s) -> {
                return fill_float(s, i -> (float)i);
            })
    );

    @DataProvider
    public Object[][] floatUnaryOpProvider() {
        return FLOAT_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<IntFunction<double[]>> DOUBLE_GENERATORS = List.of(
            withToString("double(i)", (int s) -> {
                return fill_double(s, i -> (double)i);
            })
    );

    @DataProvider
    public Object[][] doubleUnaryOpProvider() {
        return DOUBLE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @ForceInline
    static <E,S extends Vector.Shape,T extends Vector.Shape>
    void testVectorResize(Vector.Species<E,S> a, Vector.Species<E,T> b, byte[] input, byte[] output) {
        Vector<E, S> av = a.fromByteArray(input, 0);
        Vector<E, T> bv = b.resize(av);
        bv.intoByteArray(output, 0);

        byte[] expected = Arrays.copyOf(input, output.length);


        Assert.assertEquals(expected, output);
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeByte(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(bspec64, bspec64, bin64, bout64);
            testVectorResize(bspec64, bspec128, bin64, bout128);
            testVectorResize(bspec64, bspec256, bin64, bout256);
            testVectorResize(bspec64, bspec512, bin64, bout512);

            testVectorResize(bspec128, bspec64, bin128, bout64);
            testVectorResize(bspec128, bspec128, bin128, bout128);
            testVectorResize(bspec128, bspec256, bin128, bout256);
            testVectorResize(bspec128, bspec512, bin128, bout512);

            testVectorResize(bspec256, bspec64, bin256, bout64);
            testVectorResize(bspec256, bspec128, bin256, bout128);
            testVectorResize(bspec256, bspec256, bin256, bout256);
            testVectorResize(bspec256, bspec512, bin256, bout512);

            testVectorResize(bspec512, bspec64, bin512, bout64);
            testVectorResize(bspec512, bspec128, bin512, bout128);
            testVectorResize(bspec512, bspec256, bin512, bout256);
            testVectorResize(bspec512, bspec512, bin512, bout512);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeShort(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(sspec64, sspec64, bin64, bout64);
            testVectorResize(sspec64, sspec128, bin64, bout128);
            testVectorResize(sspec64, sspec256, bin64, bout256);
            testVectorResize(sspec64, sspec512, bin64, bout512);

            testVectorResize(sspec128, sspec64, bin128, bout64);
            testVectorResize(sspec128, sspec128, bin128, bout128);
            testVectorResize(sspec128, sspec256, bin128, bout256);
            testVectorResize(sspec128, sspec512, bin128, bout512);

            testVectorResize(sspec256, sspec64, bin256, bout64);
            testVectorResize(sspec256, sspec128, bin256, bout128);
            testVectorResize(sspec256, sspec256, bin256, bout256);
            testVectorResize(sspec256, sspec512, bin256, bout512);

            testVectorResize(sspec512, sspec64, bin512, bout64);
            testVectorResize(sspec512, sspec128, bin512, bout128);
            testVectorResize(sspec512, sspec256, bin512, bout256);
            testVectorResize(sspec512, sspec512, bin512, bout512);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeInt(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(ispec64, ispec64, bin64, bout64);
            testVectorResize(ispec64, ispec128, bin64, bout128);
            testVectorResize(ispec64, ispec256, bin64, bout256);
            testVectorResize(ispec64, ispec512, bin64, bout512);

            testVectorResize(ispec128, ispec64, bin128, bout64);
            testVectorResize(ispec128, ispec128, bin128, bout128);
            testVectorResize(ispec128, ispec256, bin128, bout256);
            testVectorResize(ispec128, ispec512, bin128, bout512);

            testVectorResize(ispec256, ispec64, bin256, bout64);
            testVectorResize(ispec256, ispec128, bin256, bout128);
            testVectorResize(ispec256, ispec256, bin256, bout256);
            testVectorResize(ispec256, ispec512, bin256, bout512);

            testVectorResize(ispec512, ispec64, bin512, bout64);
            testVectorResize(ispec512, ispec128, bin512, bout128);
            testVectorResize(ispec512, ispec256, bin512, bout256);
            testVectorResize(ispec512, ispec512, bin512, bout512);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeLong(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(lspec64, lspec64, bin64, bout64);
            testVectorResize(lspec64, lspec128, bin64, bout128);
            testVectorResize(lspec64, lspec256, bin64, bout256);
            testVectorResize(lspec64, lspec512, bin64, bout512);

            testVectorResize(lspec128, lspec64, bin128, bout64);
            testVectorResize(lspec128, lspec128, bin128, bout128);
            testVectorResize(lspec128, lspec256, bin128, bout256);
            testVectorResize(lspec128, lspec512, bin128, bout512);

            testVectorResize(lspec256, lspec64, bin256, bout64);
            testVectorResize(lspec256, lspec128, bin256, bout128);
            testVectorResize(lspec256, lspec256, bin256, bout256);
            testVectorResize(lspec256, lspec512, bin256, bout512);

            testVectorResize(lspec512, lspec64, bin512, bout64);
            testVectorResize(lspec512, lspec128, bin512, bout128);
            testVectorResize(lspec512, lspec256, bin512, bout256);
            testVectorResize(lspec512, lspec512, bin512, bout512);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeFloat(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(fspec64, fspec64, bin64, bout64);
            testVectorResize(fspec64, fspec128, bin64, bout128);
            testVectorResize(fspec64, fspec256, bin64, bout256);
            testVectorResize(fspec64, fspec512, bin64, bout512);

            testVectorResize(fspec128, fspec64, bin128, bout64);
            testVectorResize(fspec128, fspec128, bin128, bout128);
            testVectorResize(fspec128, fspec256, bin128, bout256);
            testVectorResize(fspec128, fspec512, bin128, bout512);

            testVectorResize(fspec256, fspec64, bin256, bout64);
            testVectorResize(fspec256, fspec128, bin256, bout128);
            testVectorResize(fspec256, fspec256, bin256, bout256);
            testVectorResize(fspec256, fspec512, bin256, bout512);

            testVectorResize(fspec512, fspec64, bin512, bout64);
            testVectorResize(fspec512, fspec128, bin512, bout128);
            testVectorResize(fspec512, fspec256, bin512, bout256);
            testVectorResize(fspec512, fspec512, bin512, bout512);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testResizeDouble(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(64/Byte.SIZE);
        byte[] bin128 = fa.apply(128/Byte.SIZE);
        byte[] bin256 = fa.apply(256/Byte.SIZE);
        byte[] bin512 = fa.apply(512/Byte.SIZE);
        byte[] bout64 = new byte[bin64.length];
        byte[] bout128 = new byte[bin128.length];
        byte[] bout256 = new byte[bin256.length];
        byte[] bout512 = new byte[bin512.length];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorResize(dspec64, dspec64, bin64, bout64);
            testVectorResize(dspec64, dspec128, bin64, bout128);
            testVectorResize(dspec64, dspec256, bin64, bout256);
            testVectorResize(dspec64, dspec512, bin64, bout512);

            testVectorResize(dspec128, dspec64, bin128, bout64);
            testVectorResize(dspec128, dspec128, bin128, bout128);
            testVectorResize(dspec128, dspec256, bin128, bout256);
            testVectorResize(dspec128, dspec512, bin128, bout512);

            testVectorResize(dspec256, dspec64, bin256, bout64);
            testVectorResize(dspec256, dspec128, bin256, bout128);
            testVectorResize(dspec256, dspec256, bin256, bout256);
            testVectorResize(dspec256, dspec512, bin256, bout512);

            testVectorResize(dspec512, dspec64, bin512, bout64);
            testVectorResize(dspec512, dspec128, bin512, bout128);
            testVectorResize(dspec512, dspec256, bin512, bout256);
            testVectorResize(dspec512, dspec512, bin512, bout512);
        }
    }

    @ForceInline
    static <E,F,S extends Vector.Shape>
    void testVectorRebracket(Vector.Species<E,S> a, Vector.Species<F,S> b, byte[] input, byte[] output) {
       assert(input.length == output.length);

        Vector<E, S> av = a.fromByteArray(input, 0);
        Vector<F, S> bv = b.rebracket(av);
        bv.intoByteArray(output, 0);

        Assert.assertEquals(input, output);
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testRebracket64(IntFunction<byte[]> fa) {
        byte[] barr = fa.apply(64/Byte.SIZE);
        byte[] bout = new byte[barr.length];
        for (int i = 0; i < NUM_ITER; i++) {
            testVectorRebracket(bspec64, bspec64, barr, bout);
            testVectorRebracket(bspec64, sspec64, barr, bout);
            testVectorRebracket(bspec64, ispec64, barr, bout);
            testVectorRebracket(bspec64, lspec64, barr, bout);
            testVectorRebracket(bspec64, fspec64, barr, bout);
            testVectorRebracket(bspec64, dspec64, barr, bout);

            testVectorRebracket(sspec64, bspec64, barr, bout);
            testVectorRebracket(sspec64, sspec64, barr, bout);
            testVectorRebracket(sspec64, ispec64, barr, bout);
            testVectorRebracket(sspec64, lspec64, barr, bout);
            testVectorRebracket(sspec64, fspec64, barr, bout);
            testVectorRebracket(sspec64, dspec64, barr, bout);

            testVectorRebracket(ispec64, bspec64, barr, bout);
            testVectorRebracket(ispec64, sspec64, barr, bout);
            testVectorRebracket(ispec64, ispec64, barr, bout);
            testVectorRebracket(ispec64, lspec64, barr, bout);
            testVectorRebracket(ispec64, fspec64, barr, bout);
            testVectorRebracket(ispec64, dspec64, barr, bout);

            testVectorRebracket(lspec64, bspec64, barr, bout);
            testVectorRebracket(lspec64, sspec64, barr, bout);
            testVectorRebracket(lspec64, ispec64, barr, bout);
            testVectorRebracket(lspec64, lspec64, barr, bout);
            testVectorRebracket(lspec64, fspec64, barr, bout);
            testVectorRebracket(lspec64, dspec64, barr, bout);

            testVectorRebracket(fspec64, bspec64, barr, bout);
            testVectorRebracket(fspec64, sspec64, barr, bout);
            testVectorRebracket(fspec64, ispec64, barr, bout);
            testVectorRebracket(fspec64, lspec64, barr, bout);
            testVectorRebracket(fspec64, fspec64, barr, bout);
            testVectorRebracket(fspec64, dspec64, barr, bout);

            testVectorRebracket(dspec64, bspec64, barr, bout);
            testVectorRebracket(dspec64, sspec64, barr, bout);
            testVectorRebracket(dspec64, ispec64, barr, bout);
            testVectorRebracket(dspec64, lspec64, barr, bout);
            testVectorRebracket(dspec64, fspec64, barr, bout);
            testVectorRebracket(dspec64, dspec64, barr, bout);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testRebracket128(IntFunction<byte[]> fa) {
        byte[] barr = fa.apply(128/Byte.SIZE);
        byte[] bout = new byte[barr.length];
        for (int i = 0; i < NUM_ITER; i++) {
            testVectorRebracket(bspec128, bspec128, barr, bout);
            testVectorRebracket(bspec128, sspec128, barr, bout);
            testVectorRebracket(bspec128, ispec128, barr, bout);
            testVectorRebracket(bspec128, lspec128, barr, bout);
            testVectorRebracket(bspec128, fspec128, barr, bout);
            testVectorRebracket(bspec128, dspec128, barr, bout);

            testVectorRebracket(sspec128, bspec128, barr, bout);
            testVectorRebracket(sspec128, sspec128, barr, bout);
            testVectorRebracket(sspec128, ispec128, barr, bout);
            testVectorRebracket(sspec128, lspec128, barr, bout);
            testVectorRebracket(sspec128, fspec128, barr, bout);
            testVectorRebracket(sspec128, dspec128, barr, bout);

            testVectorRebracket(ispec128, bspec128, barr, bout);
            testVectorRebracket(ispec128, sspec128, barr, bout);
            testVectorRebracket(ispec128, ispec128, barr, bout);
            testVectorRebracket(ispec128, lspec128, barr, bout);
            testVectorRebracket(ispec128, fspec128, barr, bout);
            testVectorRebracket(ispec128, dspec128, barr, bout);

            testVectorRebracket(lspec128, bspec128, barr, bout);
            testVectorRebracket(lspec128, sspec128, barr, bout);
            testVectorRebracket(lspec128, ispec128, barr, bout);
            testVectorRebracket(lspec128, lspec128, barr, bout);
            testVectorRebracket(lspec128, fspec128, barr, bout);
            testVectorRebracket(lspec128, dspec128, barr, bout);

            testVectorRebracket(fspec128, bspec128, barr, bout);
            testVectorRebracket(fspec128, sspec128, barr, bout);
            testVectorRebracket(fspec128, ispec128, barr, bout);
            testVectorRebracket(fspec128, lspec128, barr, bout);
            testVectorRebracket(fspec128, fspec128, barr, bout);
            testVectorRebracket(fspec128, dspec128, barr, bout);

            testVectorRebracket(dspec128, bspec128, barr, bout);
            testVectorRebracket(dspec128, sspec128, barr, bout);
            testVectorRebracket(dspec128, ispec128, barr, bout);
            testVectorRebracket(dspec128, lspec128, barr, bout);
            testVectorRebracket(dspec128, fspec128, barr, bout);
            testVectorRebracket(dspec128, dspec128, barr, bout);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testRebracket256(IntFunction<byte[]> fa) {
        byte[] barr = fa.apply(256/Byte.SIZE);
        byte[] bout = new byte[barr.length];
        for (int i = 0; i < NUM_ITER; i++) {
            testVectorRebracket(bspec256, bspec256, barr, bout);
            testVectorRebracket(bspec256, sspec256, barr, bout);
            testVectorRebracket(bspec256, ispec256, barr, bout);
            testVectorRebracket(bspec256, lspec256, barr, bout);
            testVectorRebracket(bspec256, fspec256, barr, bout);
            testVectorRebracket(bspec256, dspec256, barr, bout);

            testVectorRebracket(sspec256, bspec256, barr, bout);
            testVectorRebracket(sspec256, sspec256, barr, bout);
            testVectorRebracket(sspec256, ispec256, barr, bout);
            testVectorRebracket(sspec256, lspec256, barr, bout);
            testVectorRebracket(sspec256, fspec256, barr, bout);
            testVectorRebracket(sspec256, dspec256, barr, bout);

            testVectorRebracket(ispec256, bspec256, barr, bout);
            testVectorRebracket(ispec256, sspec256, barr, bout);
            testVectorRebracket(ispec256, ispec256, barr, bout);
            testVectorRebracket(ispec256, lspec256, barr, bout);
            testVectorRebracket(ispec256, fspec256, barr, bout);
            testVectorRebracket(ispec256, dspec256, barr, bout);

            testVectorRebracket(lspec256, bspec256, barr, bout);
            testVectorRebracket(lspec256, sspec256, barr, bout);
            testVectorRebracket(lspec256, ispec256, barr, bout);
            testVectorRebracket(lspec256, lspec256, barr, bout);
            testVectorRebracket(lspec256, fspec256, barr, bout);
            testVectorRebracket(lspec256, dspec256, barr, bout);

            testVectorRebracket(fspec256, bspec256, barr, bout);
            testVectorRebracket(fspec256, sspec256, barr, bout);
            testVectorRebracket(fspec256, ispec256, barr, bout);
            testVectorRebracket(fspec256, lspec256, barr, bout);
            testVectorRebracket(fspec256, fspec256, barr, bout);
            testVectorRebracket(fspec256, dspec256, barr, bout);

            testVectorRebracket(dspec256, bspec256, barr, bout);
            testVectorRebracket(dspec256, sspec256, barr, bout);
            testVectorRebracket(dspec256, ispec256, barr, bout);
            testVectorRebracket(dspec256, lspec256, barr, bout);
            testVectorRebracket(dspec256, fspec256, barr, bout);
            testVectorRebracket(dspec256, dspec256, barr, bout);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testRebracket512(IntFunction<byte[]> fa) {
        byte[] barr = fa.apply(512/Byte.SIZE);
        byte[] bout = new byte[barr.length];
        for (int i = 0; i < NUM_ITER; i++) {
            testVectorRebracket(bspec512, bspec512, barr, bout);
            testVectorRebracket(bspec512, sspec512, barr, bout);
            testVectorRebracket(bspec512, ispec512, barr, bout);
            testVectorRebracket(bspec512, lspec512, barr, bout);
            testVectorRebracket(bspec512, fspec512, barr, bout);
            testVectorRebracket(bspec512, dspec512, barr, bout);

            testVectorRebracket(sspec512, bspec512, barr, bout);
            testVectorRebracket(sspec512, sspec512, barr, bout);
            testVectorRebracket(sspec512, ispec512, barr, bout);
            testVectorRebracket(sspec512, lspec512, barr, bout);
            testVectorRebracket(sspec512, fspec512, barr, bout);
            testVectorRebracket(sspec512, dspec512, barr, bout);

            testVectorRebracket(ispec512, bspec512, barr, bout);
            testVectorRebracket(ispec512, sspec512, barr, bout);
            testVectorRebracket(ispec512, ispec512, barr, bout);
            testVectorRebracket(ispec512, lspec512, barr, bout);
            testVectorRebracket(ispec512, fspec512, barr, bout);
            testVectorRebracket(ispec512, dspec512, barr, bout);

            testVectorRebracket(lspec512, bspec512, barr, bout);
            testVectorRebracket(lspec512, sspec512, barr, bout);
            testVectorRebracket(lspec512, ispec512, barr, bout);
            testVectorRebracket(lspec512, lspec512, barr, bout);
            testVectorRebracket(lspec512, fspec512, barr, bout);
            testVectorRebracket(lspec512, dspec512, barr, bout);

            testVectorRebracket(fspec512, bspec512, barr, bout);
            testVectorRebracket(fspec512, sspec512, barr, bout);
            testVectorRebracket(fspec512, ispec512, barr, bout);
            testVectorRebracket(fspec512, lspec512, barr, bout);
            testVectorRebracket(fspec512, fspec512, barr, bout);
            testVectorRebracket(fspec512, dspec512, barr, bout);

            testVectorRebracket(dspec512, bspec512, barr, bout);
            testVectorRebracket(dspec512, sspec512, barr, bout);
            testVectorRebracket(dspec512, ispec512, barr, bout);
            testVectorRebracket(dspec512, lspec512, barr, bout);
            testVectorRebracket(dspec512, fspec512, barr, bout);
            testVectorRebracket(dspec512, dspec512, barr, bout);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToFloat(ByteVector.ByteSpecies<S> a, FloatVector.FloatSpecies<T> b, byte[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToFloat(ShortVector.ShortSpecies<S> a, FloatVector.FloatSpecies<T> b, short[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToFloat(IntVector.IntSpecies<S> a, FloatVector.FloatSpecies<T> b, int[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToFloat(LongVector.LongSpecies<S> a, FloatVector.FloatSpecies<T> b, long[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToFloat(FloatVector.FloatSpecies<S> a, FloatVector.FloatSpecies<T> b, float[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToFloat(DoubleVector.DoubleSpecies<S> a, FloatVector.FloatSpecies<T> b, double[] input, float[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        FloatVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((float)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((float)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToByte(ByteVector.ByteSpecies<S> a, ByteVector.ByteSpecies<T> b, byte[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToByte(ShortVector.ShortSpecies<S> a, ByteVector.ByteSpecies<T> b, short[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToByte(IntVector.IntSpecies<S> a, ByteVector.ByteSpecies<T> b, int[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToByte(LongVector.LongSpecies<S> a, ByteVector.ByteSpecies<T> b, long[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToByte(FloatVector.FloatSpecies<S> a, ByteVector.ByteSpecies<T> b, float[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToByte(DoubleVector.DoubleSpecies<S> a, ByteVector.ByteSpecies<T> b, double[] input, byte[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        ByteVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((byte)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToShort(ByteVector.ByteSpecies<S> a, ShortVector.ShortSpecies<T> b, byte[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToShort(ShortVector.ShortSpecies<S> a, ShortVector.ShortSpecies<T> b, short[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToShort(IntVector.IntSpecies<S> a, ShortVector.ShortSpecies<T> b, int[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToShort(LongVector.LongSpecies<S> a, ShortVector.ShortSpecies<T> b, long[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToShort(FloatVector.FloatSpecies<S> a, ShortVector.ShortSpecies<T> b, float[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToShort(DoubleVector.DoubleSpecies<S> a, ShortVector.ShortSpecies<T> b, double[] input, short[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        ShortVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((short)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((short)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToInt(ByteVector.ByteSpecies<S> a, IntVector.IntSpecies<T> b, byte[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToInt(ShortVector.ShortSpecies<S> a, IntVector.IntSpecies<T> b, short[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToInt(IntVector.IntSpecies<S> a, IntVector.IntSpecies<T> b, int[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToInt(LongVector.LongSpecies<S> a, IntVector.IntSpecies<T> b, long[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToInt(FloatVector.FloatSpecies<S> a, IntVector.IntSpecies<T> b, float[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToInt(DoubleVector.DoubleSpecies<S> a, IntVector.IntSpecies<T> b, double[] input, int[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        IntVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((int)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((int)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToLong(ByteVector.ByteSpecies<S> a, LongVector.LongSpecies<T> b, byte[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToLong(ShortVector.ShortSpecies<S> a, LongVector.LongSpecies<T> b, short[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToLong(IntVector.IntSpecies<S> a, LongVector.LongSpecies<T> b, int[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToLong(LongVector.LongSpecies<S> a, LongVector.LongSpecies<T> b, long[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToLong(FloatVector.FloatSpecies<S> a, LongVector.LongSpecies<T> b, float[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToLong(DoubleVector.DoubleSpecies<S> a, LongVector.LongSpecies<T> b, double[] input, long[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        LongVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((long)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((long)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToDouble(ByteVector.ByteSpecies<S> a, DoubleVector.DoubleSpecies<T> b, byte[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ByteVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToDouble(ShortVector.ShortSpecies<S> a, DoubleVector.DoubleSpecies<T> b, short[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        ShortVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToDouble(IntVector.IntSpecies<S> a, DoubleVector.DoubleSpecies<T> b, int[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        IntVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToDouble(LongVector.LongSpecies<S> a, DoubleVector.DoubleSpecies<T> b, long[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        LongVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToDouble(FloatVector.FloatSpecies<S> a, DoubleVector.DoubleSpecies<T> b, float[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        FloatVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToDouble(DoubleVector.DoubleSpecies<S> a, DoubleVector.DoubleSpecies<T> b, double[] input, double[] output) {
        assert(input.length == a.length());
        assert(output.length == b.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        DoubleVector<T> bv = b.cast(av);
        bv.intoArray(output, 0);

        for (int i = 0; i < Math.min(input.length, output.length); i++) {
            Assert.assertEquals((double)input[i], output[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals((double)0, output[i]);
        }
    }

    @Test(dataProvider = "byteUnaryOpProvider")
    static void testCastFromByte(IntFunction<byte[]> fa) {
        byte[] bin64 = fa.apply(bspec64.length());
        byte[] bin128 = fa.apply(bspec128.length());
        byte[] bin256 = fa.apply(bspec256.length());
        byte[] bin512 = fa.apply(bspec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastByteToByte(bspec64, bspec64, bin64, bout64);
            testVectorCastByteToByte(bspec64, bspec128, bin64, bout128);
            testVectorCastByteToByte(bspec64, bspec256, bin64, bout256);
            testVectorCastByteToByte(bspec64, bspec512, bin64, bout512);

            testVectorCastByteToByte(bspec128, bspec64, bin128, bout64);
            testVectorCastByteToByte(bspec128, bspec128, bin128, bout128);
            testVectorCastByteToByte(bspec128, bspec256, bin128, bout256);
            testVectorCastByteToByte(bspec128, bspec512, bin128, bout512);

            testVectorCastByteToByte(bspec256, bspec64, bin256, bout64);
            testVectorCastByteToByte(bspec256, bspec128, bin256, bout128);
            testVectorCastByteToByte(bspec256, bspec256, bin256, bout256);
            testVectorCastByteToByte(bspec256, bspec512, bin256, bout512);

            testVectorCastByteToByte(bspec512, bspec64, bin512, bout64);
            testVectorCastByteToByte(bspec512, bspec128, bin512, bout128);
            testVectorCastByteToByte(bspec512, bspec256, bin512, bout256);
            testVectorCastByteToByte(bspec512, bspec512, bin512, bout512);

            testVectorCastByteToShort(bspec64, sspec64, bin64, sout64);
            testVectorCastByteToShort(bspec64, sspec128, bin64, sout128);
            testVectorCastByteToShort(bspec64, sspec256, bin64, sout256);
            testVectorCastByteToShort(bspec64, sspec512, bin64, sout512);

            testVectorCastByteToShort(bspec128, sspec64, bin128, sout64);
            testVectorCastByteToShort(bspec128, sspec128, bin128, sout128);
            testVectorCastByteToShort(bspec128, sspec256, bin128, sout256);
            testVectorCastByteToShort(bspec128, sspec512, bin128, sout512);

            testVectorCastByteToShort(bspec256, sspec64, bin256, sout64);
            testVectorCastByteToShort(bspec256, sspec128, bin256, sout128);
            testVectorCastByteToShort(bspec256, sspec256, bin256, sout256);
            testVectorCastByteToShort(bspec256, sspec512, bin256, sout512);

            testVectorCastByteToShort(bspec512, sspec64, bin512, sout64);
            testVectorCastByteToShort(bspec512, sspec128, bin512, sout128);
            testVectorCastByteToShort(bspec512, sspec256, bin512, sout256);
            testVectorCastByteToShort(bspec512, sspec512, bin512, sout512);

            testVectorCastByteToInt(bspec64, ispec64, bin64, iout64);
            testVectorCastByteToInt(bspec64, ispec128, bin64, iout128);
            testVectorCastByteToInt(bspec64, ispec256, bin64, iout256);
            testVectorCastByteToInt(bspec64, ispec512, bin64, iout512);

            testVectorCastByteToInt(bspec128, ispec64, bin128, iout64);
            testVectorCastByteToInt(bspec128, ispec128, bin128, iout128);
            testVectorCastByteToInt(bspec128, ispec256, bin128, iout256);
            testVectorCastByteToInt(bspec128, ispec512, bin128, iout512);

            testVectorCastByteToInt(bspec256, ispec64, bin256, iout64);
            testVectorCastByteToInt(bspec256, ispec128, bin256, iout128);
            testVectorCastByteToInt(bspec256, ispec256, bin256, iout256);
            testVectorCastByteToInt(bspec256, ispec512, bin256, iout512);

            testVectorCastByteToInt(bspec512, ispec64, bin512, iout64);
            testVectorCastByteToInt(bspec512, ispec128, bin512, iout128);
            testVectorCastByteToInt(bspec512, ispec256, bin512, iout256);
            testVectorCastByteToInt(bspec512, ispec512, bin512, iout512);

            testVectorCastByteToLong(bspec64, lspec64, bin64, lout64);
            testVectorCastByteToLong(bspec64, lspec128, bin64, lout128);
            testVectorCastByteToLong(bspec64, lspec256, bin64, lout256);
            testVectorCastByteToLong(bspec64, lspec512, bin64, lout512);

            testVectorCastByteToLong(bspec128, lspec64, bin128, lout64);
            testVectorCastByteToLong(bspec128, lspec128, bin128, lout128);
            testVectorCastByteToLong(bspec128, lspec256, bin128, lout256);
            testVectorCastByteToLong(bspec128, lspec512, bin128, lout512);

            testVectorCastByteToLong(bspec256, lspec64, bin256, lout64);
            testVectorCastByteToLong(bspec256, lspec128, bin256, lout128);
            testVectorCastByteToLong(bspec256, lspec256, bin256, lout256);
            testVectorCastByteToLong(bspec256, lspec512, bin256, lout512);

            testVectorCastByteToLong(bspec512, lspec64, bin512, lout64);
            testVectorCastByteToLong(bspec512, lspec128, bin512, lout128);
            testVectorCastByteToLong(bspec512, lspec256, bin512, lout256);
            testVectorCastByteToLong(bspec512, lspec512, bin512, lout512);

            testVectorCastByteToFloat(bspec64, fspec64, bin64, fout64);
            testVectorCastByteToFloat(bspec64, fspec128, bin64, fout128);
            testVectorCastByteToFloat(bspec64, fspec256, bin64, fout256);
            testVectorCastByteToFloat(bspec64, fspec512, bin64, fout512);

            testVectorCastByteToFloat(bspec128, fspec64, bin128, fout64);
            testVectorCastByteToFloat(bspec128, fspec128, bin128, fout128);
            testVectorCastByteToFloat(bspec128, fspec256, bin128, fout256);
            testVectorCastByteToFloat(bspec128, fspec512, bin128, fout512);

            testVectorCastByteToFloat(bspec256, fspec64, bin256, fout64);
            testVectorCastByteToFloat(bspec256, fspec128, bin256, fout128);
            testVectorCastByteToFloat(bspec256, fspec256, bin256, fout256);
            testVectorCastByteToFloat(bspec256, fspec512, bin256, fout512);

            testVectorCastByteToFloat(bspec512, fspec64, bin512, fout64);
            testVectorCastByteToFloat(bspec512, fspec128, bin512, fout128);
            testVectorCastByteToFloat(bspec512, fspec256, bin512, fout256);
            testVectorCastByteToFloat(bspec512, fspec512, bin512, fout512);

            testVectorCastByteToDouble(bspec64, dspec64, bin64, dout64);
            testVectorCastByteToDouble(bspec64, dspec128, bin64, dout128);
            testVectorCastByteToDouble(bspec64, dspec256, bin64, dout256);
            testVectorCastByteToDouble(bspec64, dspec512, bin64, dout512);

            testVectorCastByteToDouble(bspec128, dspec64, bin128, dout64);
            testVectorCastByteToDouble(bspec128, dspec128, bin128, dout128);
            testVectorCastByteToDouble(bspec128, dspec256, bin128, dout256);
            testVectorCastByteToDouble(bspec128, dspec512, bin128, dout512);

            testVectorCastByteToDouble(bspec256, dspec64, bin256, dout64);
            testVectorCastByteToDouble(bspec256, dspec128, bin256, dout128);
            testVectorCastByteToDouble(bspec256, dspec256, bin256, dout256);
            testVectorCastByteToDouble(bspec256, dspec512, bin256, dout512);

            testVectorCastByteToDouble(bspec512, dspec64, bin512, dout64);
            testVectorCastByteToDouble(bspec512, dspec128, bin512, dout128);
            testVectorCastByteToDouble(bspec512, dspec256, bin512, dout256);
            testVectorCastByteToDouble(bspec512, dspec512, bin512, dout512);
        }
    }

    @Test(dataProvider = "shortUnaryOpProvider")
    static void testCastFromShort(IntFunction<short[]> fa) {
        short[] sin64 = fa.apply(sspec64.length());
        short[] sin128 = fa.apply(sspec128.length());
        short[] sin256 = fa.apply(sspec256.length());
        short[] sin512 = fa.apply(sspec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastShortToByte(sspec64, bspec64, sin64, bout64);
            testVectorCastShortToByte(sspec64, bspec128, sin64, bout128);
            testVectorCastShortToByte(sspec64, bspec256, sin64, bout256);
            testVectorCastShortToByte(sspec64, bspec512, sin64, bout512);

            testVectorCastShortToByte(sspec128, bspec64, sin128, bout64);
            testVectorCastShortToByte(sspec128, bspec128, sin128, bout128);
            testVectorCastShortToByte(sspec128, bspec256, sin128, bout256);
            testVectorCastShortToByte(sspec128, bspec512, sin128, bout512);

            testVectorCastShortToByte(sspec256, bspec64, sin256, bout64);
            testVectorCastShortToByte(sspec256, bspec128, sin256, bout128);
            testVectorCastShortToByte(sspec256, bspec256, sin256, bout256);
            testVectorCastShortToByte(sspec256, bspec512, sin256, bout512);

            testVectorCastShortToByte(sspec512, bspec64, sin512, bout64);
            testVectorCastShortToByte(sspec512, bspec128, sin512, bout128);
            testVectorCastShortToByte(sspec512, bspec256, sin512, bout256);
            testVectorCastShortToByte(sspec512, bspec512, sin512, bout512);

            testVectorCastShortToShort(sspec64, sspec64, sin64, sout64);
            testVectorCastShortToShort(sspec64, sspec128, sin64, sout128);
            testVectorCastShortToShort(sspec64, sspec256, sin64, sout256);
            testVectorCastShortToShort(sspec64, sspec512, sin64, sout512);

            testVectorCastShortToShort(sspec128, sspec64, sin128, sout64);
            testVectorCastShortToShort(sspec128, sspec128, sin128, sout128);
            testVectorCastShortToShort(sspec128, sspec256, sin128, sout256);
            testVectorCastShortToShort(sspec128, sspec512, sin128, sout512);

            testVectorCastShortToShort(sspec256, sspec64, sin256, sout64);
            testVectorCastShortToShort(sspec256, sspec128, sin256, sout128);
            testVectorCastShortToShort(sspec256, sspec256, sin256, sout256);
            testVectorCastShortToShort(sspec256, sspec512, sin256, sout512);

            testVectorCastShortToShort(sspec512, sspec64, sin512, sout64);
            testVectorCastShortToShort(sspec512, sspec128, sin512, sout128);
            testVectorCastShortToShort(sspec512, sspec256, sin512, sout256);
            testVectorCastShortToShort(sspec512, sspec512, sin512, sout512);

            testVectorCastShortToInt(sspec64, ispec64, sin64, iout64);
            testVectorCastShortToInt(sspec64, ispec128, sin64, iout128);
            testVectorCastShortToInt(sspec64, ispec256, sin64, iout256);
            testVectorCastShortToInt(sspec64, ispec512, sin64, iout512);

            testVectorCastShortToInt(sspec128, ispec64, sin128, iout64);
            testVectorCastShortToInt(sspec128, ispec128, sin128, iout128);
            testVectorCastShortToInt(sspec128, ispec256, sin128, iout256);
            testVectorCastShortToInt(sspec128, ispec512, sin128, iout512);

            testVectorCastShortToInt(sspec256, ispec64, sin256, iout64);
            testVectorCastShortToInt(sspec256, ispec128, sin256, iout128);
            testVectorCastShortToInt(sspec256, ispec256, sin256, iout256);
            testVectorCastShortToInt(sspec256, ispec512, sin256, iout512);

            testVectorCastShortToInt(sspec512, ispec64, sin512, iout64);
            testVectorCastShortToInt(sspec512, ispec128, sin512, iout128);
            testVectorCastShortToInt(sspec512, ispec256, sin512, iout256);
            testVectorCastShortToInt(sspec512, ispec512, sin512, iout512);

            testVectorCastShortToLong(sspec64, lspec64, sin64, lout64);
            testVectorCastShortToLong(sspec64, lspec128, sin64, lout128);
            testVectorCastShortToLong(sspec64, lspec256, sin64, lout256);
            testVectorCastShortToLong(sspec64, lspec512, sin64, lout512);

            testVectorCastShortToLong(sspec128, lspec64, sin128, lout64);
            testVectorCastShortToLong(sspec128, lspec128, sin128, lout128);
            testVectorCastShortToLong(sspec128, lspec256, sin128, lout256);
            testVectorCastShortToLong(sspec128, lspec512, sin128, lout512);

            testVectorCastShortToLong(sspec256, lspec64, sin256, lout64);
            testVectorCastShortToLong(sspec256, lspec128, sin256, lout128);
            testVectorCastShortToLong(sspec256, lspec256, sin256, lout256);
            testVectorCastShortToLong(sspec256, lspec512, sin256, lout512);

            testVectorCastShortToLong(sspec512, lspec64, sin512, lout64);
            testVectorCastShortToLong(sspec512, lspec128, sin512, lout128);
            testVectorCastShortToLong(sspec512, lspec256, sin512, lout256);
            testVectorCastShortToLong(sspec512, lspec512, sin512, lout512);

            testVectorCastShortToFloat(sspec64, fspec64, sin64, fout64);
            testVectorCastShortToFloat(sspec64, fspec128, sin64, fout128);
            testVectorCastShortToFloat(sspec64, fspec256, sin64, fout256);
            testVectorCastShortToFloat(sspec64, fspec512, sin64, fout512);

            testVectorCastShortToFloat(sspec128, fspec64, sin128, fout64);
            testVectorCastShortToFloat(sspec128, fspec128, sin128, fout128);
            testVectorCastShortToFloat(sspec128, fspec256, sin128, fout256);
            testVectorCastShortToFloat(sspec128, fspec512, sin128, fout512);

            testVectorCastShortToFloat(sspec256, fspec64, sin256, fout64);
            testVectorCastShortToFloat(sspec256, fspec128, sin256, fout128);
            testVectorCastShortToFloat(sspec256, fspec256, sin256, fout256);
            testVectorCastShortToFloat(sspec256, fspec512, sin256, fout512);

            testVectorCastShortToFloat(sspec512, fspec64, sin512, fout64);
            testVectorCastShortToFloat(sspec512, fspec128, sin512, fout128);
            testVectorCastShortToFloat(sspec512, fspec256, sin512, fout256);
            testVectorCastShortToFloat(sspec512, fspec512, sin512, fout512);

            testVectorCastShortToDouble(sspec64, dspec64, sin64, dout64);
            testVectorCastShortToDouble(sspec64, dspec128, sin64, dout128);
            testVectorCastShortToDouble(sspec64, dspec256, sin64, dout256);
            testVectorCastShortToDouble(sspec64, dspec512, sin64, dout512);

            testVectorCastShortToDouble(sspec128, dspec64, sin128, dout64);
            testVectorCastShortToDouble(sspec128, dspec128, sin128, dout128);
            testVectorCastShortToDouble(sspec128, dspec256, sin128, dout256);
            testVectorCastShortToDouble(sspec128, dspec512, sin128, dout512);

            testVectorCastShortToDouble(sspec256, dspec64, sin256, dout64);
            testVectorCastShortToDouble(sspec256, dspec128, sin256, dout128);
            testVectorCastShortToDouble(sspec256, dspec256, sin256, dout256);
            testVectorCastShortToDouble(sspec256, dspec512, sin256, dout512);

            testVectorCastShortToDouble(sspec512, dspec64, sin512, dout64);
            testVectorCastShortToDouble(sspec512, dspec128, sin512, dout128);
            testVectorCastShortToDouble(sspec512, dspec256, sin512, dout256);
            testVectorCastShortToDouble(sspec512, dspec512, sin512, dout512);
        }
    }

    @Test(dataProvider = "intUnaryOpProvider")
    static void testCastFromInt(IntFunction<int[]> fa) {
        int[] iin64 = fa.apply(ispec64.length());
        int[] iin128 = fa.apply(ispec128.length());
        int[] iin256 = fa.apply(ispec256.length());
        int[] iin512 = fa.apply(ispec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastIntToByte(ispec64, bspec64, iin64, bout64);
            testVectorCastIntToByte(ispec64, bspec128, iin64, bout128);
            testVectorCastIntToByte(ispec64, bspec256, iin64, bout256);
            testVectorCastIntToByte(ispec64, bspec512, iin64, bout512);

            testVectorCastIntToByte(ispec128, bspec64, iin128, bout64);
            testVectorCastIntToByte(ispec128, bspec128, iin128, bout128);
            testVectorCastIntToByte(ispec128, bspec256, iin128, bout256);
            testVectorCastIntToByte(ispec128, bspec512, iin128, bout512);

            testVectorCastIntToByte(ispec256, bspec64, iin256, bout64);
            testVectorCastIntToByte(ispec256, bspec128, iin256, bout128);
            testVectorCastIntToByte(ispec256, bspec256, iin256, bout256);
            testVectorCastIntToByte(ispec256, bspec512, iin256, bout512);

            testVectorCastIntToByte(ispec512, bspec64, iin512, bout64);
            testVectorCastIntToByte(ispec512, bspec128, iin512, bout128);
            testVectorCastIntToByte(ispec512, bspec256, iin512, bout256);
            testVectorCastIntToByte(ispec512, bspec512, iin512, bout512);

            testVectorCastIntToShort(ispec64, sspec64, iin64, sout64);
            testVectorCastIntToShort(ispec64, sspec128, iin64, sout128);
            testVectorCastIntToShort(ispec64, sspec256, iin64, sout256);
            testVectorCastIntToShort(ispec64, sspec512, iin64, sout512);

            testVectorCastIntToShort(ispec128, sspec64, iin128, sout64);
            testVectorCastIntToShort(ispec128, sspec128, iin128, sout128);
            testVectorCastIntToShort(ispec128, sspec256, iin128, sout256);
            testVectorCastIntToShort(ispec128, sspec512, iin128, sout512);

            testVectorCastIntToShort(ispec256, sspec64, iin256, sout64);
            testVectorCastIntToShort(ispec256, sspec128, iin256, sout128);
            testVectorCastIntToShort(ispec256, sspec256, iin256, sout256);
            testVectorCastIntToShort(ispec256, sspec512, iin256, sout512);

            testVectorCastIntToShort(ispec512, sspec64, iin512, sout64);
            testVectorCastIntToShort(ispec512, sspec128, iin512, sout128);
            testVectorCastIntToShort(ispec512, sspec256, iin512, sout256);
            testVectorCastIntToShort(ispec512, sspec512, iin512, sout512);

            testVectorCastIntToInt(ispec64, ispec64, iin64, iout64);
            testVectorCastIntToInt(ispec64, ispec128, iin64, iout128);
            testVectorCastIntToInt(ispec64, ispec256, iin64, iout256);
            testVectorCastIntToInt(ispec64, ispec512, iin64, iout512);

            testVectorCastIntToInt(ispec128, ispec64, iin128, iout64);
            testVectorCastIntToInt(ispec128, ispec128, iin128, iout128);
            testVectorCastIntToInt(ispec128, ispec256, iin128, iout256);
            testVectorCastIntToInt(ispec128, ispec512, iin128, iout512);

            testVectorCastIntToInt(ispec256, ispec64, iin256, iout64);
            testVectorCastIntToInt(ispec256, ispec128, iin256, iout128);
            testVectorCastIntToInt(ispec256, ispec256, iin256, iout256);
            testVectorCastIntToInt(ispec256, ispec512, iin256, iout512);

            testVectorCastIntToInt(ispec512, ispec64, iin512, iout64);
            testVectorCastIntToInt(ispec512, ispec128, iin512, iout128);
            testVectorCastIntToInt(ispec512, ispec256, iin512, iout256);
            testVectorCastIntToInt(ispec512, ispec512, iin512, iout512);

            testVectorCastIntToLong(ispec64, lspec64, iin64, lout64);
            testVectorCastIntToLong(ispec64, lspec128, iin64, lout128);
            testVectorCastIntToLong(ispec64, lspec256, iin64, lout256);
            testVectorCastIntToLong(ispec64, lspec512, iin64, lout512);

            testVectorCastIntToLong(ispec128, lspec64, iin128, lout64);
            testVectorCastIntToLong(ispec128, lspec128, iin128, lout128);
            testVectorCastIntToLong(ispec128, lspec256, iin128, lout256);
            testVectorCastIntToLong(ispec128, lspec512, iin128, lout512);

            testVectorCastIntToLong(ispec256, lspec64, iin256, lout64);
            testVectorCastIntToLong(ispec256, lspec128, iin256, lout128);
            testVectorCastIntToLong(ispec256, lspec256, iin256, lout256);
            testVectorCastIntToLong(ispec256, lspec512, iin256, lout512);

            testVectorCastIntToLong(ispec512, lspec64, iin512, lout64);
            testVectorCastIntToLong(ispec512, lspec128, iin512, lout128);
            testVectorCastIntToLong(ispec512, lspec256, iin512, lout256);
            testVectorCastIntToLong(ispec512, lspec512, iin512, lout512);

            testVectorCastIntToFloat(ispec64, fspec64, iin64, fout64);
            testVectorCastIntToFloat(ispec64, fspec128, iin64, fout128);
            testVectorCastIntToFloat(ispec64, fspec256, iin64, fout256);
            testVectorCastIntToFloat(ispec64, fspec512, iin64, fout512);

            testVectorCastIntToFloat(ispec128, fspec64, iin128, fout64);
            testVectorCastIntToFloat(ispec128, fspec128, iin128, fout128);
            testVectorCastIntToFloat(ispec128, fspec256, iin128, fout256);
            testVectorCastIntToFloat(ispec128, fspec512, iin128, fout512);

            testVectorCastIntToFloat(ispec256, fspec64, iin256, fout64);
            testVectorCastIntToFloat(ispec256, fspec128, iin256, fout128);
            testVectorCastIntToFloat(ispec256, fspec256, iin256, fout256);
            testVectorCastIntToFloat(ispec256, fspec512, iin256, fout512);

            testVectorCastIntToFloat(ispec512, fspec64, iin512, fout64);
            testVectorCastIntToFloat(ispec512, fspec128, iin512, fout128);
            testVectorCastIntToFloat(ispec512, fspec256, iin512, fout256);
            testVectorCastIntToFloat(ispec512, fspec512, iin512, fout512);

            testVectorCastIntToDouble(ispec64, dspec64, iin64, dout64);
            testVectorCastIntToDouble(ispec64, dspec128, iin64, dout128);
            testVectorCastIntToDouble(ispec64, dspec256, iin64, dout256);
            testVectorCastIntToDouble(ispec64, dspec512, iin64, dout512);

            testVectorCastIntToDouble(ispec128, dspec64, iin128, dout64);
            testVectorCastIntToDouble(ispec128, dspec128, iin128, dout128);
            testVectorCastIntToDouble(ispec128, dspec256, iin128, dout256);
            testVectorCastIntToDouble(ispec128, dspec512, iin128, dout512);

            testVectorCastIntToDouble(ispec256, dspec64, iin256, dout64);
            testVectorCastIntToDouble(ispec256, dspec128, iin256, dout128);
            testVectorCastIntToDouble(ispec256, dspec256, iin256, dout256);
            testVectorCastIntToDouble(ispec256, dspec512, iin256, dout512);

            testVectorCastIntToDouble(ispec512, dspec64, iin512, dout64);
            testVectorCastIntToDouble(ispec512, dspec128, iin512, dout128);
            testVectorCastIntToDouble(ispec512, dspec256, iin512, dout256);
            testVectorCastIntToDouble(ispec512, dspec512, iin512, dout512);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void testCastFromLong(IntFunction<long[]> fa) {
        long[] lin64 = fa.apply(lspec64.length());
        long[] lin128 = fa.apply(lspec128.length());
        long[] lin256 = fa.apply(lspec256.length());
        long[] lin512 = fa.apply(lspec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastLongToByte(lspec64, bspec64, lin64, bout64);
            testVectorCastLongToByte(lspec64, bspec128, lin64, bout128);
            testVectorCastLongToByte(lspec64, bspec256, lin64, bout256);
            testVectorCastLongToByte(lspec64, bspec512, lin64, bout512);

            testVectorCastLongToByte(lspec128, bspec64, lin128, bout64);
            testVectorCastLongToByte(lspec128, bspec128, lin128, bout128);
            testVectorCastLongToByte(lspec128, bspec256, lin128, bout256);
            testVectorCastLongToByte(lspec128, bspec512, lin128, bout512);

            testVectorCastLongToByte(lspec256, bspec64, lin256, bout64);
            testVectorCastLongToByte(lspec256, bspec128, lin256, bout128);
            testVectorCastLongToByte(lspec256, bspec256, lin256, bout256);
            testVectorCastLongToByte(lspec256, bspec512, lin256, bout512);

            testVectorCastLongToByte(lspec512, bspec64, lin512, bout64);
            testVectorCastLongToByte(lspec512, bspec128, lin512, bout128);
            testVectorCastLongToByte(lspec512, bspec256, lin512, bout256);
            testVectorCastLongToByte(lspec512, bspec512, lin512, bout512);

            testVectorCastLongToShort(lspec64, sspec64, lin64, sout64);
            testVectorCastLongToShort(lspec64, sspec128, lin64, sout128);
            testVectorCastLongToShort(lspec64, sspec256, lin64, sout256);
            testVectorCastLongToShort(lspec64, sspec512, lin64, sout512);

            testVectorCastLongToShort(lspec128, sspec64, lin128, sout64);
            testVectorCastLongToShort(lspec128, sspec128, lin128, sout128);
            testVectorCastLongToShort(lspec128, sspec256, lin128, sout256);
            testVectorCastLongToShort(lspec128, sspec512, lin128, sout512);

            testVectorCastLongToShort(lspec256, sspec64, lin256, sout64);
            testVectorCastLongToShort(lspec256, sspec128, lin256, sout128);
            testVectorCastLongToShort(lspec256, sspec256, lin256, sout256);
            testVectorCastLongToShort(lspec256, sspec512, lin256, sout512);

            testVectorCastLongToShort(lspec512, sspec64, lin512, sout64);
            testVectorCastLongToShort(lspec512, sspec128, lin512, sout128);
            testVectorCastLongToShort(lspec512, sspec256, lin512, sout256);
            testVectorCastLongToShort(lspec512, sspec512, lin512, sout512);

            testVectorCastLongToInt(lspec64, ispec64, lin64, iout64);
            testVectorCastLongToInt(lspec64, ispec128, lin64, iout128);
            testVectorCastLongToInt(lspec64, ispec256, lin64, iout256);
            testVectorCastLongToInt(lspec64, ispec512, lin64, iout512);

            testVectorCastLongToInt(lspec128, ispec64, lin128, iout64);
            testVectorCastLongToInt(lspec128, ispec128, lin128, iout128);
            testVectorCastLongToInt(lspec128, ispec256, lin128, iout256);
            testVectorCastLongToInt(lspec128, ispec512, lin128, iout512);

            testVectorCastLongToInt(lspec256, ispec64, lin256, iout64);
            testVectorCastLongToInt(lspec256, ispec128, lin256, iout128);
            testVectorCastLongToInt(lspec256, ispec256, lin256, iout256);
            testVectorCastLongToInt(lspec256, ispec512, lin256, iout512);

            testVectorCastLongToInt(lspec512, ispec64, lin512, iout64);
            testVectorCastLongToInt(lspec512, ispec128, lin512, iout128);
            testVectorCastLongToInt(lspec512, ispec256, lin512, iout256);
            testVectorCastLongToInt(lspec512, ispec512, lin512, iout512);

            testVectorCastLongToLong(lspec64, lspec64, lin64, lout64);
            testVectorCastLongToLong(lspec64, lspec128, lin64, lout128);
            testVectorCastLongToLong(lspec64, lspec256, lin64, lout256);
            testVectorCastLongToLong(lspec64, lspec512, lin64, lout512);

            testVectorCastLongToLong(lspec128, lspec64, lin128, lout64);
            testVectorCastLongToLong(lspec128, lspec128, lin128, lout128);
            testVectorCastLongToLong(lspec128, lspec256, lin128, lout256);
            testVectorCastLongToLong(lspec128, lspec512, lin128, lout512);

            testVectorCastLongToLong(lspec256, lspec64, lin256, lout64);
            testVectorCastLongToLong(lspec256, lspec128, lin256, lout128);
            testVectorCastLongToLong(lspec256, lspec256, lin256, lout256);
            testVectorCastLongToLong(lspec256, lspec512, lin256, lout512);

            testVectorCastLongToLong(lspec512, lspec64, lin512, lout64);
            testVectorCastLongToLong(lspec512, lspec128, lin512, lout128);
            testVectorCastLongToLong(lspec512, lspec256, lin512, lout256);
            testVectorCastLongToLong(lspec512, lspec512, lin512, lout512);

            testVectorCastLongToFloat(lspec64, fspec64, lin64, fout64);
            testVectorCastLongToFloat(lspec64, fspec128, lin64, fout128);
            testVectorCastLongToFloat(lspec64, fspec256, lin64, fout256);
            testVectorCastLongToFloat(lspec64, fspec512, lin64, fout512);

            testVectorCastLongToFloat(lspec128, fspec64, lin128, fout64);
            testVectorCastLongToFloat(lspec128, fspec128, lin128, fout128);
            testVectorCastLongToFloat(lspec128, fspec256, lin128, fout256);
            testVectorCastLongToFloat(lspec128, fspec512, lin128, fout512);

            testVectorCastLongToFloat(lspec256, fspec64, lin256, fout64);
            testVectorCastLongToFloat(lspec256, fspec128, lin256, fout128);
            testVectorCastLongToFloat(lspec256, fspec256, lin256, fout256);
            testVectorCastLongToFloat(lspec256, fspec512, lin256, fout512);

            testVectorCastLongToFloat(lspec512, fspec64, lin512, fout64);
            testVectorCastLongToFloat(lspec512, fspec128, lin512, fout128);
            testVectorCastLongToFloat(lspec512, fspec256, lin512, fout256);
            testVectorCastLongToFloat(lspec512, fspec512, lin512, fout512);

            testVectorCastLongToDouble(lspec64, dspec64, lin64, dout64);
            testVectorCastLongToDouble(lspec64, dspec128, lin64, dout128);
            testVectorCastLongToDouble(lspec64, dspec256, lin64, dout256);
            testVectorCastLongToDouble(lspec64, dspec512, lin64, dout512);

            testVectorCastLongToDouble(lspec128, dspec64, lin128, dout64);
            testVectorCastLongToDouble(lspec128, dspec128, lin128, dout128);
            testVectorCastLongToDouble(lspec128, dspec256, lin128, dout256);
            testVectorCastLongToDouble(lspec128, dspec512, lin128, dout512);

            testVectorCastLongToDouble(lspec256, dspec64, lin256, dout64);
            testVectorCastLongToDouble(lspec256, dspec128, lin256, dout128);
            testVectorCastLongToDouble(lspec256, dspec256, lin256, dout256);
            testVectorCastLongToDouble(lspec256, dspec512, lin256, dout512);

            testVectorCastLongToDouble(lspec512, dspec64, lin512, dout64);
            testVectorCastLongToDouble(lspec512, dspec128, lin512, dout128);
            testVectorCastLongToDouble(lspec512, dspec256, lin512, dout256);
            testVectorCastLongToDouble(lspec512, dspec512, lin512, dout512);
        }
    }

    @Test(dataProvider = "floatUnaryOpProvider")
    static void testCastFromFloat(IntFunction<float[]> fa) {
        float[] fin64 = fa.apply(fspec64.length());
        float[] fin128 = fa.apply(fspec128.length());
        float[] fin256 = fa.apply(fspec256.length());
        float[] fin512 = fa.apply(fspec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastFloatToByte(fspec64, bspec64, fin64, bout64);
            testVectorCastFloatToByte(fspec64, bspec128, fin64, bout128);
            testVectorCastFloatToByte(fspec64, bspec256, fin64, bout256);
            testVectorCastFloatToByte(fspec64, bspec512, fin64, bout512);

            testVectorCastFloatToByte(fspec128, bspec64, fin128, bout64);
            testVectorCastFloatToByte(fspec128, bspec128, fin128, bout128);
            testVectorCastFloatToByte(fspec128, bspec256, fin128, bout256);
            testVectorCastFloatToByte(fspec128, bspec512, fin128, bout512);

            testVectorCastFloatToByte(fspec256, bspec64, fin256, bout64);
            testVectorCastFloatToByte(fspec256, bspec128, fin256, bout128);
            testVectorCastFloatToByte(fspec256, bspec256, fin256, bout256);
            testVectorCastFloatToByte(fspec256, bspec512, fin256, bout512);

            testVectorCastFloatToByte(fspec512, bspec64, fin512, bout64);
            testVectorCastFloatToByte(fspec512, bspec128, fin512, bout128);
            testVectorCastFloatToByte(fspec512, bspec256, fin512, bout256);
            testVectorCastFloatToByte(fspec512, bspec512, fin512, bout512);

            testVectorCastFloatToShort(fspec64, sspec64, fin64, sout64);
            testVectorCastFloatToShort(fspec64, sspec128, fin64, sout128);
            testVectorCastFloatToShort(fspec64, sspec256, fin64, sout256);
            testVectorCastFloatToShort(fspec64, sspec512, fin64, sout512);

            testVectorCastFloatToShort(fspec128, sspec64, fin128, sout64);
            testVectorCastFloatToShort(fspec128, sspec128, fin128, sout128);
            testVectorCastFloatToShort(fspec128, sspec256, fin128, sout256);
            testVectorCastFloatToShort(fspec128, sspec512, fin128, sout512);

            testVectorCastFloatToShort(fspec256, sspec64, fin256, sout64);
            testVectorCastFloatToShort(fspec256, sspec128, fin256, sout128);
            testVectorCastFloatToShort(fspec256, sspec256, fin256, sout256);
            testVectorCastFloatToShort(fspec256, sspec512, fin256, sout512);

            testVectorCastFloatToShort(fspec512, sspec64, fin512, sout64);
            testVectorCastFloatToShort(fspec512, sspec128, fin512, sout128);
            testVectorCastFloatToShort(fspec512, sspec256, fin512, sout256);
            testVectorCastFloatToShort(fspec512, sspec512, fin512, sout512);

            testVectorCastFloatToInt(fspec64, ispec64, fin64, iout64);
            testVectorCastFloatToInt(fspec64, ispec128, fin64, iout128);
            testVectorCastFloatToInt(fspec64, ispec256, fin64, iout256);
            testVectorCastFloatToInt(fspec64, ispec512, fin64, iout512);

            testVectorCastFloatToInt(fspec128, ispec64, fin128, iout64);
            testVectorCastFloatToInt(fspec128, ispec128, fin128, iout128);
            testVectorCastFloatToInt(fspec128, ispec256, fin128, iout256);
            testVectorCastFloatToInt(fspec128, ispec512, fin128, iout512);

            testVectorCastFloatToInt(fspec256, ispec64, fin256, iout64);
            testVectorCastFloatToInt(fspec256, ispec128, fin256, iout128);
            testVectorCastFloatToInt(fspec256, ispec256, fin256, iout256);
            testVectorCastFloatToInt(fspec256, ispec512, fin256, iout512);

            testVectorCastFloatToInt(fspec512, ispec64, fin512, iout64);
            testVectorCastFloatToInt(fspec512, ispec128, fin512, iout128);
            testVectorCastFloatToInt(fspec512, ispec256, fin512, iout256);
            testVectorCastFloatToInt(fspec512, ispec512, fin512, iout512);

            testVectorCastFloatToLong(fspec64, lspec64, fin64, lout64);
            testVectorCastFloatToLong(fspec64, lspec128, fin64, lout128);
            testVectorCastFloatToLong(fspec64, lspec256, fin64, lout256);
            testVectorCastFloatToLong(fspec64, lspec512, fin64, lout512);

            testVectorCastFloatToLong(fspec128, lspec64, fin128, lout64);
            testVectorCastFloatToLong(fspec128, lspec128, fin128, lout128);
            testVectorCastFloatToLong(fspec128, lspec256, fin128, lout256);
            testVectorCastFloatToLong(fspec128, lspec512, fin128, lout512);

            testVectorCastFloatToLong(fspec256, lspec64, fin256, lout64);
            testVectorCastFloatToLong(fspec256, lspec128, fin256, lout128);
            testVectorCastFloatToLong(fspec256, lspec256, fin256, lout256);
            testVectorCastFloatToLong(fspec256, lspec512, fin256, lout512);

            testVectorCastFloatToLong(fspec512, lspec64, fin512, lout64);
            testVectorCastFloatToLong(fspec512, lspec128, fin512, lout128);
            testVectorCastFloatToLong(fspec512, lspec256, fin512, lout256);
            testVectorCastFloatToLong(fspec512, lspec512, fin512, lout512);

            testVectorCastFloatToFloat(fspec64, fspec64, fin64, fout64);
            testVectorCastFloatToFloat(fspec64, fspec128, fin64, fout128);
            testVectorCastFloatToFloat(fspec64, fspec256, fin64, fout256);
            testVectorCastFloatToFloat(fspec64, fspec512, fin64, fout512);

            testVectorCastFloatToFloat(fspec128, fspec64, fin128, fout64);
            testVectorCastFloatToFloat(fspec128, fspec128, fin128, fout128);
            testVectorCastFloatToFloat(fspec128, fspec256, fin128, fout256);
            testVectorCastFloatToFloat(fspec128, fspec512, fin128, fout512);

            testVectorCastFloatToFloat(fspec256, fspec64, fin256, fout64);
            testVectorCastFloatToFloat(fspec256, fspec128, fin256, fout128);
            testVectorCastFloatToFloat(fspec256, fspec256, fin256, fout256);
            testVectorCastFloatToFloat(fspec256, fspec512, fin256, fout512);

            testVectorCastFloatToFloat(fspec512, fspec64, fin512, fout64);
            testVectorCastFloatToFloat(fspec512, fspec128, fin512, fout128);
            testVectorCastFloatToFloat(fspec512, fspec256, fin512, fout256);
            testVectorCastFloatToFloat(fspec512, fspec512, fin512, fout512);

            testVectorCastFloatToDouble(fspec64, dspec64, fin64, dout64);
            testVectorCastFloatToDouble(fspec64, dspec128, fin64, dout128);
            testVectorCastFloatToDouble(fspec64, dspec256, fin64, dout256);
            testVectorCastFloatToDouble(fspec64, dspec512, fin64, dout512);

            testVectorCastFloatToDouble(fspec128, dspec64, fin128, dout64);
            testVectorCastFloatToDouble(fspec128, dspec128, fin128, dout128);
            testVectorCastFloatToDouble(fspec128, dspec256, fin128, dout256);
            testVectorCastFloatToDouble(fspec128, dspec512, fin128, dout512);

            testVectorCastFloatToDouble(fspec256, dspec64, fin256, dout64);
            testVectorCastFloatToDouble(fspec256, dspec128, fin256, dout128);
            testVectorCastFloatToDouble(fspec256, dspec256, fin256, dout256);
            testVectorCastFloatToDouble(fspec256, dspec512, fin256, dout512);

            testVectorCastFloatToDouble(fspec512, dspec64, fin512, dout64);
            testVectorCastFloatToDouble(fspec512, dspec128, fin512, dout128);
            testVectorCastFloatToDouble(fspec512, dspec256, fin512, dout256);
            testVectorCastFloatToDouble(fspec512, dspec512, fin512, dout512);
        }
    }

    @Test(dataProvider = "doubleUnaryOpProvider")
    static void testCastFromDouble(IntFunction<double[]> fa) {
        double[] din64 = fa.apply(dspec64.length());
        double[] din128 = fa.apply(dspec128.length());
        double[] din256 = fa.apply(dspec256.length());
        double[] din512 = fa.apply(dspec512.length());

        byte[] bout64 = new byte[bspec64.length()];
        byte[] bout128 = new byte[bspec128.length()];
        byte[] bout256 = new byte[bspec256.length()];
        byte[] bout512 = new byte[bspec512.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastDoubleToByte(dspec64, bspec64, din64, bout64);
            testVectorCastDoubleToByte(dspec64, bspec128, din64, bout128);
            testVectorCastDoubleToByte(dspec64, bspec256, din64, bout256);
            testVectorCastDoubleToByte(dspec64, bspec512, din64, bout512);

            testVectorCastDoubleToByte(dspec128, bspec64, din128, bout64);
            testVectorCastDoubleToByte(dspec128, bspec128, din128, bout128);
            testVectorCastDoubleToByte(dspec128, bspec256, din128, bout256);
            testVectorCastDoubleToByte(dspec128, bspec512, din128, bout512);

            testVectorCastDoubleToByte(dspec256, bspec64, din256, bout64);
            testVectorCastDoubleToByte(dspec256, bspec128, din256, bout128);
            testVectorCastDoubleToByte(dspec256, bspec256, din256, bout256);
            testVectorCastDoubleToByte(dspec256, bspec512, din256, bout512);

            testVectorCastDoubleToByte(dspec512, bspec64, din512, bout64);
            testVectorCastDoubleToByte(dspec512, bspec128, din512, bout128);
            testVectorCastDoubleToByte(dspec512, bspec256, din512, bout256);
            testVectorCastDoubleToByte(dspec512, bspec512, din512, bout512);

            testVectorCastDoubleToShort(dspec64, sspec64, din64, sout64);
            testVectorCastDoubleToShort(dspec64, sspec128, din64, sout128);
            testVectorCastDoubleToShort(dspec64, sspec256, din64, sout256);
            testVectorCastDoubleToShort(dspec64, sspec512, din64, sout512);

            testVectorCastDoubleToShort(dspec128, sspec64, din128, sout64);
            testVectorCastDoubleToShort(dspec128, sspec128, din128, sout128);
            testVectorCastDoubleToShort(dspec128, sspec256, din128, sout256);
            testVectorCastDoubleToShort(dspec128, sspec512, din128, sout512);

            testVectorCastDoubleToShort(dspec256, sspec64, din256, sout64);
            testVectorCastDoubleToShort(dspec256, sspec128, din256, sout128);
            testVectorCastDoubleToShort(dspec256, sspec256, din256, sout256);
            testVectorCastDoubleToShort(dspec256, sspec512, din256, sout512);

            testVectorCastDoubleToShort(dspec512, sspec64, din512, sout64);
            testVectorCastDoubleToShort(dspec512, sspec128, din512, sout128);
            testVectorCastDoubleToShort(dspec512, sspec256, din512, sout256);
            testVectorCastDoubleToShort(dspec512, sspec512, din512, sout512);

            testVectorCastDoubleToInt(dspec64, ispec64, din64, iout64);
            testVectorCastDoubleToInt(dspec64, ispec128, din64, iout128);
            testVectorCastDoubleToInt(dspec64, ispec256, din64, iout256);
            testVectorCastDoubleToInt(dspec64, ispec512, din64, iout512);

            testVectorCastDoubleToInt(dspec128, ispec64, din128, iout64);
            testVectorCastDoubleToInt(dspec128, ispec128, din128, iout128);
            testVectorCastDoubleToInt(dspec128, ispec256, din128, iout256);
            testVectorCastDoubleToInt(dspec128, ispec512, din128, iout512);

            testVectorCastDoubleToInt(dspec256, ispec64, din256, iout64);
            testVectorCastDoubleToInt(dspec256, ispec128, din256, iout128);
            testVectorCastDoubleToInt(dspec256, ispec256, din256, iout256);
            testVectorCastDoubleToInt(dspec256, ispec512, din256, iout512);

            testVectorCastDoubleToInt(dspec512, ispec64, din512, iout64);
            testVectorCastDoubleToInt(dspec512, ispec128, din512, iout128);
            testVectorCastDoubleToInt(dspec512, ispec256, din512, iout256);
            testVectorCastDoubleToInt(dspec512, ispec512, din512, iout512);

            testVectorCastDoubleToLong(dspec64, lspec64, din64, lout64);
            testVectorCastDoubleToLong(dspec64, lspec128, din64, lout128);
            testVectorCastDoubleToLong(dspec64, lspec256, din64, lout256);
            testVectorCastDoubleToLong(dspec64, lspec512, din64, lout512);

            testVectorCastDoubleToLong(dspec128, lspec64, din128, lout64);
            testVectorCastDoubleToLong(dspec128, lspec128, din128, lout128);
            testVectorCastDoubleToLong(dspec128, lspec256, din128, lout256);
            testVectorCastDoubleToLong(dspec128, lspec512, din128, lout512);

            testVectorCastDoubleToLong(dspec256, lspec64, din256, lout64);
            testVectorCastDoubleToLong(dspec256, lspec128, din256, lout128);
            testVectorCastDoubleToLong(dspec256, lspec256, din256, lout256);
            testVectorCastDoubleToLong(dspec256, lspec512, din256, lout512);

            testVectorCastDoubleToLong(dspec512, lspec64, din512, lout64);
            testVectorCastDoubleToLong(dspec512, lspec128, din512, lout128);
            testVectorCastDoubleToLong(dspec512, lspec256, din512, lout256);
            testVectorCastDoubleToLong(dspec512, lspec512, din512, lout512);

            testVectorCastDoubleToFloat(dspec64, fspec64, din64, fout64);
            testVectorCastDoubleToFloat(dspec64, fspec128, din64, fout128);
            testVectorCastDoubleToFloat(dspec64, fspec256, din64, fout256);
            testVectorCastDoubleToFloat(dspec64, fspec512, din64, fout512);

            testVectorCastDoubleToFloat(dspec128, fspec64, din128, fout64);
            testVectorCastDoubleToFloat(dspec128, fspec128, din128, fout128);
            testVectorCastDoubleToFloat(dspec128, fspec256, din128, fout256);
            testVectorCastDoubleToFloat(dspec128, fspec512, din128, fout512);

            testVectorCastDoubleToFloat(dspec256, fspec64, din256, fout64);
            testVectorCastDoubleToFloat(dspec256, fspec128, din256, fout128);
            testVectorCastDoubleToFloat(dspec256, fspec256, din256, fout256);
            testVectorCastDoubleToFloat(dspec256, fspec512, din256, fout512);

            testVectorCastDoubleToFloat(dspec512, fspec64, din512, fout64);
            testVectorCastDoubleToFloat(dspec512, fspec128, din512, fout128);
            testVectorCastDoubleToFloat(dspec512, fspec256, din512, fout256);
            testVectorCastDoubleToFloat(dspec512, fspec512, din512, fout512);

            testVectorCastDoubleToDouble(dspec64, dspec64, din64, dout64);
            testVectorCastDoubleToDouble(dspec64, dspec128, din64, dout128);
            testVectorCastDoubleToDouble(dspec64, dspec256, din64, dout256);
            testVectorCastDoubleToDouble(dspec64, dspec512, din64, dout512);

            testVectorCastDoubleToDouble(dspec128, dspec64, din128, dout64);
            testVectorCastDoubleToDouble(dspec128, dspec128, din128, dout128);
            testVectorCastDoubleToDouble(dspec128, dspec256, din128, dout256);
            testVectorCastDoubleToDouble(dspec128, dspec512, din128, dout512);

            testVectorCastDoubleToDouble(dspec256, dspec64, din256, dout64);
            testVectorCastDoubleToDouble(dspec256, dspec128, din256, dout128);
            testVectorCastDoubleToDouble(dspec256, dspec256, din256, dout256);
            testVectorCastDoubleToDouble(dspec256, dspec512, din256, dout512);

            testVectorCastDoubleToDouble(dspec512, dspec64, din512, dout64);
            testVectorCastDoubleToDouble(dspec512, dspec128, din512, dout128);
            testVectorCastDoubleToDouble(dspec512, dspec256, din512, dout256);
            testVectorCastDoubleToDouble(dspec512, dspec512, din512, dout512);
        }
    }
}
