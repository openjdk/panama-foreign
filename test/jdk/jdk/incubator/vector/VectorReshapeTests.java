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

    static final IntVector.IntSpecies<Shapes.S64Bit> ispec64 = IntVector.species(Shapes.S_64_BIT);
    static final FloatVector.FloatSpecies<Shapes.S64Bit> fspec64 = FloatVector.species(Shapes.S_64_BIT);
    static final LongVector.LongSpecies<Shapes.S64Bit> lspec64 = LongVector.species(Shapes.S_64_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S64Bit> dspec64 = DoubleVector.species(Shapes.S_64_BIT);
    static final ByteVector.ByteSpecies<Shapes.S64Bit> bspec64 = ByteVector.species(Shapes.S_64_BIT);
    static final ShortVector.ShortSpecies<Shapes.S64Bit> sspec64 = ShortVector.species(Shapes.S_64_BIT);

    static final IntVector.IntSpecies<Shapes.S128Bit> ispec128 = IntVector.species(Shapes.S_128_BIT);
    static final FloatVector.FloatSpecies<Shapes.S128Bit> fspec128 = FloatVector.species(Shapes.S_128_BIT);
    static final LongVector.LongSpecies<Shapes.S128Bit> lspec128 = LongVector.species(Shapes.S_128_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S128Bit> dspec128 = DoubleVector.species(Shapes.S_128_BIT);
    static final ByteVector.ByteSpecies<Shapes.S128Bit> bspec128 = ByteVector.species(Shapes.S_128_BIT);
    static final ShortVector.ShortSpecies<Shapes.S128Bit> sspec128 = ShortVector.species(Shapes.S_128_BIT);

    static final IntVector.IntSpecies<Shapes.S256Bit> ispec256 = IntVector.species(Shapes.S_256_BIT);
    static final FloatVector.FloatSpecies<Shapes.S256Bit> fspec256 = FloatVector.species(Shapes.S_256_BIT);
    static final LongVector.LongSpecies<Shapes.S256Bit> lspec256 = LongVector.species(Shapes.S_256_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S256Bit> dspec256 = DoubleVector.species(Shapes.S_256_BIT);
    static final ByteVector.ByteSpecies<Shapes.S256Bit> bspec256 = ByteVector.species(Shapes.S_256_BIT);
    static final ShortVector.ShortSpecies<Shapes.S256Bit> sspec256 = ShortVector.species(Shapes.S_256_BIT);

    static final IntVector.IntSpecies<Shapes.S512Bit> ispec512 = IntVector.species(Shapes.S_512_BIT);
    static final FloatVector.FloatSpecies<Shapes.S512Bit> fspec512 = FloatVector.species(Shapes.S_512_BIT);
    static final LongVector.LongSpecies<Shapes.S512Bit> lspec512 = LongVector.species(Shapes.S_512_BIT);
    static final DoubleVector.DoubleSpecies<Shapes.S512Bit> dspec512 = DoubleVector.species(Shapes.S_512_BIT);
    static final ByteVector.ByteSpecies<Shapes.S512Bit> bspec512 = ByteVector.species(Shapes.S_512_BIT);
    static final ShortVector.ShortSpecies<Shapes.S512Bit> sspec512 = ShortVector.species(Shapes.S_512_BIT);

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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToFloatFail(ByteVector.ByteSpecies<S> a, FloatVector.FloatSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToFloatFail(ShortVector.ShortSpecies<S> a, FloatVector.FloatSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToFloatFail(IntVector.IntSpecies<S> a, FloatVector.FloatSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToFloatFail(LongVector.LongSpecies<S> a, FloatVector.FloatSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToFloatFail(FloatVector.FloatSpecies<S> a, FloatVector.FloatSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (float)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (float)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToFloatFail(DoubleVector.DoubleSpecies<S> a, FloatVector.FloatSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToByteFail(ByteVector.ByteSpecies<S> a, ByteVector.ByteSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToByteFail(ShortVector.ShortSpecies<S> a, ByteVector.ByteSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToByteFail(IntVector.IntSpecies<S> a, ByteVector.ByteSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToByteFail(LongVector.LongSpecies<S> a, ByteVector.ByteSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToByteFail(FloatVector.FloatSpecies<S> a, ByteVector.ByteSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (byte)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (byte)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToByteFail(DoubleVector.DoubleSpecies<S> a, ByteVector.ByteSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToShortFail(ByteVector.ByteSpecies<S> a, ShortVector.ShortSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToShortFail(ShortVector.ShortSpecies<S> a, ShortVector.ShortSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToShortFail(IntVector.IntSpecies<S> a, ShortVector.ShortSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToShortFail(LongVector.LongSpecies<S> a, ShortVector.ShortSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToShortFail(FloatVector.FloatSpecies<S> a, ShortVector.ShortSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (short)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (short)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToShortFail(DoubleVector.DoubleSpecies<S> a, ShortVector.ShortSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToIntFail(ByteVector.ByteSpecies<S> a, IntVector.IntSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToIntFail(ShortVector.ShortSpecies<S> a, IntVector.IntSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToIntFail(IntVector.IntSpecies<S> a, IntVector.IntSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToIntFail(LongVector.LongSpecies<S> a, IntVector.IntSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToIntFail(FloatVector.FloatSpecies<S> a, IntVector.IntSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (int)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (int)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToIntFail(DoubleVector.DoubleSpecies<S> a, IntVector.IntSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToLongFail(ByteVector.ByteSpecies<S> a, LongVector.LongSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToLongFail(ShortVector.ShortSpecies<S> a, LongVector.LongSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToLongFail(IntVector.IntSpecies<S> a, LongVector.LongSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToLongFail(LongVector.LongSpecies<S> a, LongVector.LongSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToLongFail(FloatVector.FloatSpecies<S> a, LongVector.LongSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (long)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (long)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToLongFail(DoubleVector.DoubleSpecies<S> a, LongVector.LongSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastByteToDoubleFail(ByteVector.ByteSpecies<S> a, DoubleVector.DoubleSpecies<T> b, byte[] input) {
        assert(input.length == a.length());

        ByteVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastShortToDoubleFail(ShortVector.ShortSpecies<S> a, DoubleVector.DoubleSpecies<T> b, short[] input) {
        assert(input.length == a.length());

        ShortVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastIntToDoubleFail(IntVector.IntSpecies<S> a, DoubleVector.DoubleSpecies<T> b, int[] input) {
        assert(input.length == a.length());

        IntVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastLongToDoubleFail(LongVector.LongSpecies<S> a, DoubleVector.DoubleSpecies<T> b, long[] input) {
        assert(input.length == a.length());

        LongVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastFloatToDoubleFail(FloatVector.FloatSpecies<S> a, DoubleVector.DoubleSpecies<T> b, float[] input) {
        assert(input.length == a.length());

        FloatVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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
            Assert.assertEquals(output[i], (double)input[i]);
        }
        for(int i = input.length; i < output.length; i++) {
            Assert.assertEquals(output[i], (double)0);
        }
    }

    @ForceInline
    static <S extends Vector.Shape, T extends Vector.Shape>
    void testVectorCastDoubleToDoubleFail(DoubleVector.DoubleSpecies<S> a, DoubleVector.DoubleSpecies<T> b, double[] input) {
        assert(input.length == a.length());

        DoubleVector<S> av = a.fromArray(input, 0);
        try {
            b.cast(av);
            Assert.fail(String.format(
                    "Cast failed to throw IllegalArgumentException for differing species lengths for %s and %s",
                    a, b));
        } catch (IllegalArgumentException e) {
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

        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout512 = new long[lspec512.length()];

        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastByteToByte(bspec64, bspec64, bin64, bout64);
            testVectorCastByteToByte(bspec128, bspec128, bin128, bout128);
            testVectorCastByteToByte(bspec256, bspec256, bin256, bout256);
            testVectorCastByteToByte(bspec512, bspec512, bin512, bout512);

            testVectorCastByteToShort(bspec64, sspec128, bin64, sout128);
            testVectorCastByteToShort(bspec128, sspec256, bin128, sout256);
            testVectorCastByteToShort(bspec256, sspec512, bin256, sout512);

            testVectorCastByteToInt(bspec64, ispec256, bin64, iout256);
            testVectorCastByteToInt(bspec128, ispec512, bin128, iout512);

            testVectorCastByteToLong(bspec64, lspec512, bin64, lout512);

            testVectorCastByteToFloat(bspec64, fspec256, bin64, fout256);
            testVectorCastByteToFloat(bspec128, fspec512, bin128, fout512);

            testVectorCastByteToDouble(bspec64, dspec512, bin64, dout512);
        }
    }

    @Test
    static void testCastFromByteFail() {
        byte[] bin64 = new byte[bspec64.length()];
        byte[] bin128 = new byte[bspec128.length()];
        byte[] bin256 = new byte[bspec256.length()];
        byte[] bin512 = new byte[bspec512.length()];

        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastByteToByteFail(bspec64, bspec128, bin64);
            testVectorCastByteToByteFail(bspec64, bspec256, bin64);
            testVectorCastByteToByteFail(bspec64, bspec512, bin64);

            testVectorCastByteToByteFail(bspec128, bspec64, bin128);
            testVectorCastByteToByteFail(bspec128, bspec256, bin128);
            testVectorCastByteToByteFail(bspec128, bspec512, bin128);

            testVectorCastByteToByteFail(bspec256, bspec64, bin256);
            testVectorCastByteToByteFail(bspec256, bspec128, bin256);
            testVectorCastByteToByteFail(bspec256, bspec512, bin256);

            testVectorCastByteToByteFail(bspec512, bspec64, bin512);
            testVectorCastByteToByteFail(bspec512, bspec128, bin512);
            testVectorCastByteToByteFail(bspec512, bspec256, bin512);

            testVectorCastByteToShortFail(bspec64, sspec64, bin64);
            testVectorCastByteToShortFail(bspec64, sspec256, bin64);
            testVectorCastByteToShortFail(bspec64, sspec512, bin64);

            testVectorCastByteToShortFail(bspec128, sspec64, bin128);
            testVectorCastByteToShortFail(bspec128, sspec128, bin128);
            testVectorCastByteToShortFail(bspec128, sspec512, bin128);

            testVectorCastByteToShortFail(bspec256, sspec64, bin256);
            testVectorCastByteToShortFail(bspec256, sspec128, bin256);
            testVectorCastByteToShortFail(bspec256, sspec256, bin256);

            testVectorCastByteToShortFail(bspec512, sspec64, bin512);
            testVectorCastByteToShortFail(bspec512, sspec128, bin512);
            testVectorCastByteToShortFail(bspec512, sspec256, bin512);
            testVectorCastByteToShortFail(bspec512, sspec512, bin512);

            testVectorCastByteToIntFail(bspec64, ispec64, bin64);
            testVectorCastByteToIntFail(bspec64, ispec128, bin64);
            testVectorCastByteToIntFail(bspec64, ispec512, bin64);

            testVectorCastByteToIntFail(bspec128, ispec64, bin128);
            testVectorCastByteToIntFail(bspec128, ispec128, bin128);
            testVectorCastByteToIntFail(bspec128, ispec256, bin128);

            testVectorCastByteToIntFail(bspec256, ispec64, bin256);
            testVectorCastByteToIntFail(bspec256, ispec128, bin256);
            testVectorCastByteToIntFail(bspec256, ispec256, bin256);
            testVectorCastByteToIntFail(bspec256, ispec512, bin256);

            testVectorCastByteToIntFail(bspec512, ispec64, bin512);
            testVectorCastByteToIntFail(bspec512, ispec128, bin512);
            testVectorCastByteToIntFail(bspec512, ispec256, bin512);
            testVectorCastByteToIntFail(bspec512, ispec512, bin512);

            testVectorCastByteToLongFail(bspec64, lspec64, bin64);
            testVectorCastByteToLongFail(bspec64, lspec128, bin64);
            testVectorCastByteToLongFail(bspec64, lspec256, bin64);

            testVectorCastByteToLongFail(bspec128, lspec64, bin128);
            testVectorCastByteToLongFail(bspec128, lspec128, bin128);
            testVectorCastByteToLongFail(bspec128, lspec256, bin128);
            testVectorCastByteToLongFail(bspec128, lspec512, bin128);

            testVectorCastByteToLongFail(bspec256, lspec64, bin256);
            testVectorCastByteToLongFail(bspec256, lspec128, bin256);
            testVectorCastByteToLongFail(bspec256, lspec256, bin256);
            testVectorCastByteToLongFail(bspec256, lspec512, bin256);

            testVectorCastByteToLongFail(bspec512, lspec64, bin512);
            testVectorCastByteToLongFail(bspec512, lspec128, bin512);
            testVectorCastByteToLongFail(bspec512, lspec256, bin512);
            testVectorCastByteToLongFail(bspec512, lspec512, bin512);

            testVectorCastByteToFloatFail(bspec64, fspec64, bin64);
            testVectorCastByteToFloatFail(bspec64, fspec128, bin64);
            testVectorCastByteToFloatFail(bspec64, fspec512, bin64);

            testVectorCastByteToFloatFail(bspec128, fspec64, bin128);
            testVectorCastByteToFloatFail(bspec128, fspec128, bin128);
            testVectorCastByteToFloatFail(bspec128, fspec256, bin128);

            testVectorCastByteToFloatFail(bspec256, fspec64, bin256);
            testVectorCastByteToFloatFail(bspec256, fspec128, bin256);
            testVectorCastByteToFloatFail(bspec256, fspec256, bin256);
            testVectorCastByteToFloatFail(bspec256, fspec512, bin256);

            testVectorCastByteToFloatFail(bspec512, fspec64, bin512);
            testVectorCastByteToFloatFail(bspec512, fspec128, bin512);
            testVectorCastByteToFloatFail(bspec512, fspec256, bin512);
            testVectorCastByteToFloatFail(bspec512, fspec512, bin512);

            testVectorCastByteToDoubleFail(bspec64, dspec64, bin64);
            testVectorCastByteToDoubleFail(bspec64, dspec128, bin64);
            testVectorCastByteToDoubleFail(bspec64, dspec256, bin64);

            testVectorCastByteToDoubleFail(bspec128, dspec64, bin128);
            testVectorCastByteToDoubleFail(bspec128, dspec128, bin128);
            testVectorCastByteToDoubleFail(bspec128, dspec256, bin128);
            testVectorCastByteToDoubleFail(bspec128, dspec512, bin128);

            testVectorCastByteToDoubleFail(bspec256, dspec64, bin256);
            testVectorCastByteToDoubleFail(bspec256, dspec128, bin256);
            testVectorCastByteToDoubleFail(bspec256, dspec256, bin256);
            testVectorCastByteToDoubleFail(bspec256, dspec512, bin256);

            testVectorCastByteToDoubleFail(bspec512, dspec64, bin512);
            testVectorCastByteToDoubleFail(bspec512, dspec128, bin512);
            testVectorCastByteToDoubleFail(bspec512, dspec256, bin512);
            testVectorCastByteToDoubleFail(bspec512, dspec512, bin512);
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

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];
        short[] sout512 = new short[sspec512.length()];

        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastShortToByte(sspec128, bspec64, sin128, bout64);
            testVectorCastShortToByte(sspec256, bspec128, sin256, bout128);
            testVectorCastShortToByte(sspec512, bspec256, sin512, bout256);

            testVectorCastShortToShort(sspec64, sspec64, sin64, sout64);
            testVectorCastShortToShort(sspec128, sspec128, sin128, sout128);
            testVectorCastShortToShort(sspec256, sspec256, sin256, sout256);
            testVectorCastShortToShort(sspec512, sspec512, sin512, sout512);

            testVectorCastShortToInt(sspec64, ispec128, sin64, iout128);
            testVectorCastShortToInt(sspec128, ispec256, sin128, iout256);
            testVectorCastShortToInt(sspec256, ispec512, sin256, iout512);

            testVectorCastShortToLong(sspec64, lspec256, sin64, lout256);
            testVectorCastShortToLong(sspec128, lspec512, sin128, lout512);

            testVectorCastShortToFloat(sspec64, fspec128, sin64, fout128);
            testVectorCastShortToFloat(sspec128, fspec256, sin128, fout256);
            testVectorCastShortToFloat(sspec256, fspec512, sin256, fout512);

            testVectorCastShortToDouble(sspec64, dspec256, sin64, dout256);
            testVectorCastShortToDouble(sspec128, dspec512, sin128, dout512);
        }
    }

    @Test()
    static void testCastFromShortFail() {
        short[] sin64 = new short[sspec64.length()];
        short[] sin128 = new short[sspec128.length()];
        short[] sin256 = new short[sspec256.length()];
        short[] sin512 = new short[sspec512.length()];
        
        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastShortToByteFail(sspec64, bspec64, sin64);
            testVectorCastShortToByteFail(sspec64, bspec128, sin64);
            testVectorCastShortToByteFail(sspec64, bspec256, sin64);
            testVectorCastShortToByteFail(sspec64, bspec512, sin64);

            testVectorCastShortToByteFail(sspec128, bspec128, sin128);
            testVectorCastShortToByteFail(sspec128, bspec256, sin128);
            testVectorCastShortToByteFail(sspec128, bspec512, sin128);

            testVectorCastShortToByteFail(sspec256, bspec64, sin256);
            testVectorCastShortToByteFail(sspec256, bspec256, sin256);
            testVectorCastShortToByteFail(sspec256, bspec512, sin256);

            testVectorCastShortToByteFail(sspec512, bspec64, sin512);
            testVectorCastShortToByteFail(sspec512, bspec128, sin512);
            testVectorCastShortToByteFail(sspec512, bspec512, sin512);

            testVectorCastShortToShortFail(sspec64, sspec128, sin64);
            testVectorCastShortToShortFail(sspec64, sspec256, sin64);
            testVectorCastShortToShortFail(sspec64, sspec512, sin64);

            testVectorCastShortToShortFail(sspec128, sspec64, sin128);
            testVectorCastShortToShortFail(sspec128, sspec256, sin128);
            testVectorCastShortToShortFail(sspec128, sspec512, sin128);

            testVectorCastShortToShortFail(sspec256, sspec64, sin256);
            testVectorCastShortToShortFail(sspec256, sspec128, sin256);
            testVectorCastShortToShortFail(sspec256, sspec512, sin256);

            testVectorCastShortToShortFail(sspec512, sspec64, sin512);
            testVectorCastShortToShortFail(sspec512, sspec128, sin512);
            testVectorCastShortToShortFail(sspec512, sspec256, sin512);

            testVectorCastShortToIntFail(sspec64, ispec64, sin64);
            testVectorCastShortToIntFail(sspec64, ispec256, sin64);
            testVectorCastShortToIntFail(sspec64, ispec512, sin64);

            testVectorCastShortToIntFail(sspec128, ispec64, sin128);
            testVectorCastShortToIntFail(sspec128, ispec128, sin128);
            testVectorCastShortToIntFail(sspec128, ispec512, sin128);

            testVectorCastShortToIntFail(sspec256, ispec64, sin256);
            testVectorCastShortToIntFail(sspec256, ispec128, sin256);
            testVectorCastShortToIntFail(sspec256, ispec256, sin256);

            testVectorCastShortToIntFail(sspec512, ispec64, sin512);
            testVectorCastShortToIntFail(sspec512, ispec128, sin512);
            testVectorCastShortToIntFail(sspec512, ispec256, sin512);
            testVectorCastShortToIntFail(sspec512, ispec512, sin512);

            testVectorCastShortToLongFail(sspec64, lspec64, sin64);
            testVectorCastShortToLongFail(sspec64, lspec128, sin64);
            testVectorCastShortToLongFail(sspec64, lspec512, sin64);

            testVectorCastShortToLongFail(sspec128, lspec64, sin128);
            testVectorCastShortToLongFail(sspec128, lspec128, sin128);
            testVectorCastShortToLongFail(sspec128, lspec256, sin128);

            testVectorCastShortToLongFail(sspec256, lspec64, sin256);
            testVectorCastShortToLongFail(sspec256, lspec128, sin256);
            testVectorCastShortToLongFail(sspec256, lspec256, sin256);
            testVectorCastShortToLongFail(sspec256, lspec512, sin256);

            testVectorCastShortToLongFail(sspec512, lspec64, sin512);
            testVectorCastShortToLongFail(sspec512, lspec128, sin512);
            testVectorCastShortToLongFail(sspec512, lspec256, sin512);
            testVectorCastShortToLongFail(sspec512, lspec512, sin512);

            testVectorCastShortToFloatFail(sspec64, fspec64, sin64);
            testVectorCastShortToFloatFail(sspec64, fspec256, sin64);
            testVectorCastShortToFloatFail(sspec64, fspec512, sin64);

            testVectorCastShortToFloatFail(sspec128, fspec64, sin128);
            testVectorCastShortToFloatFail(sspec128, fspec128, sin128);
            testVectorCastShortToFloatFail(sspec128, fspec512, sin128);

            testVectorCastShortToFloatFail(sspec256, fspec64, sin256);
            testVectorCastShortToFloatFail(sspec256, fspec128, sin256);
            testVectorCastShortToFloatFail(sspec256, fspec256, sin256);

            testVectorCastShortToFloatFail(sspec512, fspec64, sin512);
            testVectorCastShortToFloatFail(sspec512, fspec128, sin512);
            testVectorCastShortToFloatFail(sspec512, fspec256, sin512);
            testVectorCastShortToFloatFail(sspec512, fspec512, sin512);

            testVectorCastShortToDoubleFail(sspec64, dspec64, sin64);
            testVectorCastShortToDoubleFail(sspec64, dspec128, sin64);
            testVectorCastShortToDoubleFail(sspec64, dspec512, sin64);

            testVectorCastShortToDoubleFail(sspec128, dspec64, sin128);
            testVectorCastShortToDoubleFail(sspec128, dspec128, sin128);
            testVectorCastShortToDoubleFail(sspec128, dspec256, sin128);

            testVectorCastShortToDoubleFail(sspec256, dspec64, sin256);
            testVectorCastShortToDoubleFail(sspec256, dspec128, sin256);
            testVectorCastShortToDoubleFail(sspec256, dspec256, sin256);
            testVectorCastShortToDoubleFail(sspec256, dspec512, sin256);

            testVectorCastShortToDoubleFail(sspec512, dspec64, sin512);
            testVectorCastShortToDoubleFail(sspec512, dspec128, sin512);
            testVectorCastShortToDoubleFail(sspec512, dspec256, sin512);
            testVectorCastShortToDoubleFail(sspec512, dspec512, sin512);
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

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastIntToByte(ispec256, bspec64, iin256, bout64);
            testVectorCastIntToByte(ispec512, bspec128, iin512, bout128);

            testVectorCastIntToShort(ispec128, sspec64, iin128, sout64);
            testVectorCastIntToShort(ispec256, sspec128, iin256, sout128);
            testVectorCastIntToShort(ispec512, sspec256, iin512, sout256);

            testVectorCastIntToInt(ispec64, ispec64, iin64, iout64);
            testVectorCastIntToInt(ispec128, ispec128, iin128, iout128);
            testVectorCastIntToInt(ispec256, ispec256, iin256, iout256);
            testVectorCastIntToInt(ispec512, ispec512, iin512, iout512);

            testVectorCastIntToLong(ispec64, lspec128, iin64, lout128);
            testVectorCastIntToLong(ispec128, lspec256, iin128, lout256);
            testVectorCastIntToLong(ispec256, lspec512, iin256, lout512);

            testVectorCastIntToFloat(ispec64, fspec64, iin64, fout64);
            testVectorCastIntToFloat(ispec128, fspec128, iin128, fout128);
            testVectorCastIntToFloat(ispec256, fspec256, iin256, fout256);
            testVectorCastIntToFloat(ispec512, fspec512, iin512, fout512);

            testVectorCastIntToDouble(ispec64, dspec128, iin64, dout128);
            testVectorCastIntToDouble(ispec128, dspec256, iin128, dout256);
            testVectorCastIntToDouble(ispec256, dspec512, iin256, dout512);
        }
    }

    @Test
    static void testCastFromIntFail() {
        int[] iin64 = new int[ispec64.length()];
        int[] iin128 = new int[ispec128.length()];
        int[] iin256 = new int[ispec256.length()];
        int[] iin512 = new int[ispec512.length()];

        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastIntToByteFail(ispec64, bspec64, iin64);
            testVectorCastIntToByteFail(ispec64, bspec128, iin64);
            testVectorCastIntToByteFail(ispec64, bspec256, iin64);
            testVectorCastIntToByteFail(ispec64, bspec512, iin64);

            testVectorCastIntToByteFail(ispec128, bspec64, iin128);
            testVectorCastIntToByteFail(ispec128, bspec128, iin128);
            testVectorCastIntToByteFail(ispec128, bspec256, iin128);
            testVectorCastIntToByteFail(ispec128, bspec512, iin128);

            testVectorCastIntToByteFail(ispec256, bspec128, iin256);
            testVectorCastIntToByteFail(ispec256, bspec256, iin256);
            testVectorCastIntToByteFail(ispec256, bspec512, iin256);

            testVectorCastIntToByteFail(ispec512, bspec64, iin512);
            testVectorCastIntToByteFail(ispec512, bspec256, iin512);
            testVectorCastIntToByteFail(ispec512, bspec512, iin512);

            testVectorCastIntToShortFail(ispec64, sspec64, iin64);
            testVectorCastIntToShortFail(ispec64, sspec128, iin64);
            testVectorCastIntToShortFail(ispec64, sspec256, iin64);
            testVectorCastIntToShortFail(ispec64, sspec512, iin64);

            testVectorCastIntToShortFail(ispec128, sspec128, iin128);
            testVectorCastIntToShortFail(ispec128, sspec256, iin128);
            testVectorCastIntToShortFail(ispec128, sspec512, iin128);

            testVectorCastIntToShortFail(ispec256, sspec64, iin256);
            testVectorCastIntToShortFail(ispec256, sspec256, iin256);
            testVectorCastIntToShortFail(ispec256, sspec512, iin256);

            testVectorCastIntToShortFail(ispec512, sspec64, iin512);
            testVectorCastIntToShortFail(ispec512, sspec128, iin512);
            testVectorCastIntToShortFail(ispec512, sspec512, iin512);

            testVectorCastIntToIntFail(ispec64, ispec128, iin64);
            testVectorCastIntToIntFail(ispec64, ispec256, iin64);
            testVectorCastIntToIntFail(ispec64, ispec512, iin64);

            testVectorCastIntToIntFail(ispec128, ispec64, iin128);
            testVectorCastIntToIntFail(ispec128, ispec256, iin128);
            testVectorCastIntToIntFail(ispec128, ispec512, iin128);

            testVectorCastIntToIntFail(ispec256, ispec64, iin256);
            testVectorCastIntToIntFail(ispec256, ispec128, iin256);
            testVectorCastIntToIntFail(ispec256, ispec512, iin256);

            testVectorCastIntToIntFail(ispec512, ispec64, iin512);
            testVectorCastIntToIntFail(ispec512, ispec128, iin512);
            testVectorCastIntToIntFail(ispec512, ispec256, iin512);

            testVectorCastIntToLongFail(ispec64, lspec64, iin64);
            testVectorCastIntToLongFail(ispec64, lspec256, iin64);
            testVectorCastIntToLongFail(ispec64, lspec512, iin64);

            testVectorCastIntToLongFail(ispec128, lspec64, iin128);
            testVectorCastIntToLongFail(ispec128, lspec128, iin128);
            testVectorCastIntToLongFail(ispec128, lspec512, iin128);

            testVectorCastIntToLongFail(ispec256, lspec64, iin256);
            testVectorCastIntToLongFail(ispec256, lspec128, iin256);
            testVectorCastIntToLongFail(ispec256, lspec256, iin256);

            testVectorCastIntToLongFail(ispec512, lspec64, iin512);
            testVectorCastIntToLongFail(ispec512, lspec128, iin512);
            testVectorCastIntToLongFail(ispec512, lspec256, iin512);
            testVectorCastIntToLongFail(ispec512, lspec512, iin512);

            testVectorCastIntToFloatFail(ispec64, fspec128, iin64);
            testVectorCastIntToFloatFail(ispec64, fspec256, iin64);
            testVectorCastIntToFloatFail(ispec64, fspec512, iin64);

            testVectorCastIntToFloatFail(ispec128, fspec64, iin128);
            testVectorCastIntToFloatFail(ispec128, fspec256, iin128);
            testVectorCastIntToFloatFail(ispec128, fspec512, iin128);

            testVectorCastIntToFloatFail(ispec256, fspec64, iin256);
            testVectorCastIntToFloatFail(ispec256, fspec128, iin256);
            testVectorCastIntToFloatFail(ispec256, fspec512, iin256);

            testVectorCastIntToFloatFail(ispec512, fspec64, iin512);
            testVectorCastIntToFloatFail(ispec512, fspec128, iin512);
            testVectorCastIntToFloatFail(ispec512, fspec256, iin512);

            testVectorCastIntToDoubleFail(ispec64, dspec64, iin64);
            testVectorCastIntToDoubleFail(ispec64, dspec256, iin64);
            testVectorCastIntToDoubleFail(ispec64, dspec512, iin64);

            testVectorCastIntToDoubleFail(ispec128, dspec64, iin128);
            testVectorCastIntToDoubleFail(ispec128, dspec128, iin128);
            testVectorCastIntToDoubleFail(ispec128, dspec512, iin128);

            testVectorCastIntToDoubleFail(ispec256, dspec64, iin256);
            testVectorCastIntToDoubleFail(ispec256, dspec128, iin256);
            testVectorCastIntToDoubleFail(ispec256, dspec256, iin256);

            testVectorCastIntToDoubleFail(ispec512, dspec64, iin512);
            testVectorCastIntToDoubleFail(ispec512, dspec128, iin512);
            testVectorCastIntToDoubleFail(ispec512, dspec256, iin512);
            testVectorCastIntToDoubleFail(ispec512, dspec512, iin512);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void testCastFromLong(IntFunction<long[]> fa) {
        long[] lin64 = fa.apply(lspec64.length());
        long[] lin128 = fa.apply(lspec128.length());
        long[] lin256 = fa.apply(lspec256.length());
        long[] lin512 = fa.apply(lspec512.length());

        byte[] bout64 = new byte[bspec64.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastLongToByte(lspec512, bspec64, lin512, bout64);

            testVectorCastLongToShort(lspec256, sspec64, lin256, sout64);
            testVectorCastLongToShort(lspec512, sspec128, lin512, sout128);

            testVectorCastLongToInt(lspec128, ispec64, lin128, iout64);
            testVectorCastLongToInt(lspec256, ispec128, lin256, iout128);
            testVectorCastLongToInt(lspec512, ispec256, lin512, iout256);

            testVectorCastLongToLong(lspec64, lspec64, lin64, lout64);
            testVectorCastLongToLong(lspec128, lspec128, lin128, lout128);
            testVectorCastLongToLong(lspec256, lspec256, lin256, lout256);
            testVectorCastLongToLong(lspec512, lspec512, lin512, lout512);

            testVectorCastLongToFloat(lspec128, fspec64, lin128, fout64);
            testVectorCastLongToFloat(lspec256, fspec128, lin256, fout128);
            testVectorCastLongToFloat(lspec512, fspec256, lin512, fout256);

            testVectorCastLongToDouble(lspec64, dspec64, lin64, dout64);
            testVectorCastLongToDouble(lspec128, dspec128, lin128, dout128);
            testVectorCastLongToDouble(lspec256, dspec256, lin256, dout256);
            testVectorCastLongToDouble(lspec512, dspec512, lin512, dout512);
        }
    }

    @Test
    static void testCastFromLongFail() {
        long[] lin64 = new long[lspec64.length()];
        long[] lin128 = new long[lspec128.length()];
        long[] lin256 = new long[lspec256.length()];
        long[] lin512 = new long[lspec512.length()];

        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastLongToByteFail(lspec64, bspec64, lin64);
            testVectorCastLongToByteFail(lspec64, bspec128, lin64);
            testVectorCastLongToByteFail(lspec64, bspec256, lin64);
            testVectorCastLongToByteFail(lspec64, bspec512, lin64);

            testVectorCastLongToByteFail(lspec128, bspec64, lin128);
            testVectorCastLongToByteFail(lspec128, bspec128, lin128);
            testVectorCastLongToByteFail(lspec128, bspec256, lin128);
            testVectorCastLongToByteFail(lspec128, bspec512, lin128);

            testVectorCastLongToByteFail(lspec256, bspec64, lin256);
            testVectorCastLongToByteFail(lspec256, bspec128, lin256);
            testVectorCastLongToByteFail(lspec256, bspec256, lin256);
            testVectorCastLongToByteFail(lspec256, bspec512, lin256);

            testVectorCastLongToByteFail(lspec512, bspec128, lin512);
            testVectorCastLongToByteFail(lspec512, bspec256, lin512);
            testVectorCastLongToByteFail(lspec512, bspec512, lin512);

            testVectorCastLongToShortFail(lspec64, sspec64, lin64);
            testVectorCastLongToShortFail(lspec64, sspec128, lin64);
            testVectorCastLongToShortFail(lspec64, sspec256, lin64);
            testVectorCastLongToShortFail(lspec64, sspec512, lin64);

            testVectorCastLongToShortFail(lspec128, sspec64, lin128);
            testVectorCastLongToShortFail(lspec128, sspec128, lin128);
            testVectorCastLongToShortFail(lspec128, sspec256, lin128);
            testVectorCastLongToShortFail(lspec128, sspec512, lin128);

            testVectorCastLongToShortFail(lspec256, sspec128, lin256);
            testVectorCastLongToShortFail(lspec256, sspec256, lin256);
            testVectorCastLongToShortFail(lspec256, sspec512, lin256);

            testVectorCastLongToShortFail(lspec512, sspec64, lin512);
            testVectorCastLongToShortFail(lspec512, sspec256, lin512);
            testVectorCastLongToShortFail(lspec512, sspec512, lin512);

            testVectorCastLongToIntFail(lspec64, ispec64, lin64);
            testVectorCastLongToIntFail(lspec64, ispec128, lin64);
            testVectorCastLongToIntFail(lspec64, ispec256, lin64);
            testVectorCastLongToIntFail(lspec64, ispec512, lin64);

            testVectorCastLongToIntFail(lspec128, ispec128, lin128);
            testVectorCastLongToIntFail(lspec128, ispec256, lin128);
            testVectorCastLongToIntFail(lspec128, ispec512, lin128);

            testVectorCastLongToIntFail(lspec256, ispec64, lin256);
            testVectorCastLongToIntFail(lspec256, ispec256, lin256);
            testVectorCastLongToIntFail(lspec256, ispec512, lin256);

            testVectorCastLongToIntFail(lspec512, ispec64, lin512);
            testVectorCastLongToIntFail(lspec512, ispec128, lin512);
            testVectorCastLongToIntFail(lspec512, ispec512, lin512);

            testVectorCastLongToLongFail(lspec64, lspec128, lin64);
            testVectorCastLongToLongFail(lspec64, lspec256, lin64);
            testVectorCastLongToLongFail(lspec64, lspec512, lin64);

            testVectorCastLongToLongFail(lspec128, lspec64, lin128);
            testVectorCastLongToLongFail(lspec128, lspec256, lin128);
            testVectorCastLongToLongFail(lspec128, lspec512, lin128);

            testVectorCastLongToLongFail(lspec256, lspec64, lin256);
            testVectorCastLongToLongFail(lspec256, lspec128, lin256);
            testVectorCastLongToLongFail(lspec256, lspec512, lin256);

            testVectorCastLongToLongFail(lspec512, lspec64, lin512);
            testVectorCastLongToLongFail(lspec512, lspec128, lin512);
            testVectorCastLongToLongFail(lspec512, lspec256, lin512);

            testVectorCastLongToFloatFail(lspec64, fspec64, lin64);
            testVectorCastLongToFloatFail(lspec64, fspec128, lin64);
            testVectorCastLongToFloatFail(lspec64, fspec256, lin64);
            testVectorCastLongToFloatFail(lspec64, fspec512, lin64);

            testVectorCastLongToFloatFail(lspec128, fspec128, lin128);
            testVectorCastLongToFloatFail(lspec128, fspec256, lin128);
            testVectorCastLongToFloatFail(lspec128, fspec512, lin128);

            testVectorCastLongToFloatFail(lspec256, fspec64, lin256);
            testVectorCastLongToFloatFail(lspec256, fspec256, lin256);
            testVectorCastLongToFloatFail(lspec256, fspec512, lin256);

            testVectorCastLongToFloatFail(lspec512, fspec64, lin512);
            testVectorCastLongToFloatFail(lspec512, fspec128, lin512);
            testVectorCastLongToFloatFail(lspec512, fspec512, lin512);

            testVectorCastLongToDoubleFail(lspec64, dspec128, lin64);
            testVectorCastLongToDoubleFail(lspec64, dspec256, lin64);
            testVectorCastLongToDoubleFail(lspec64, dspec512, lin64);

            testVectorCastLongToDoubleFail(lspec128, dspec64, lin128);
            testVectorCastLongToDoubleFail(lspec128, dspec256, lin128);
            testVectorCastLongToDoubleFail(lspec128, dspec512, lin128);

            testVectorCastLongToDoubleFail(lspec256, dspec64, lin256);
            testVectorCastLongToDoubleFail(lspec256, dspec128, lin256);
            testVectorCastLongToDoubleFail(lspec256, dspec512, lin256);

            testVectorCastLongToDoubleFail(lspec512, dspec64, lin512);
            testVectorCastLongToDoubleFail(lspec512, dspec128, lin512);
            testVectorCastLongToDoubleFail(lspec512, dspec256, lin512);
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

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];
        short[] sout256 = new short[sspec256.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];
        int[] iout512 = new int[ispec512.length()];

        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];
        float[] fout512 = new float[fspec512.length()];

        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastFloatToByte(fspec256, bspec64, fin256, bout64);
            testVectorCastFloatToByte(fspec512, bspec128, fin512, bout128);

            testVectorCastFloatToShort(fspec128, sspec64, fin128, sout64);
            testVectorCastFloatToShort(fspec256, sspec128, fin256, sout128);
            testVectorCastFloatToShort(fspec512, sspec256, fin512, sout256);

            testVectorCastFloatToInt(fspec64, ispec64, fin64, iout64);
            testVectorCastFloatToInt(fspec128, ispec128, fin128, iout128);
            testVectorCastFloatToInt(fspec256, ispec256, fin256, iout256);
            testVectorCastFloatToInt(fspec512, ispec512, fin512, iout512);

            testVectorCastFloatToLong(fspec64, lspec128, fin64, lout128);
            testVectorCastFloatToLong(fspec128, lspec256, fin128, lout256);
            testVectorCastFloatToLong(fspec256, lspec512, fin256, lout512);

            testVectorCastFloatToFloat(fspec64, fspec64, fin64, fout64);
            testVectorCastFloatToFloat(fspec128, fspec128, fin128, fout128);
            testVectorCastFloatToFloat(fspec256, fspec256, fin256, fout256);
            testVectorCastFloatToFloat(fspec512, fspec512, fin512, fout512);

            testVectorCastFloatToDouble(fspec64, dspec128, fin64, dout128);
            testVectorCastFloatToDouble(fspec128, dspec256, fin128, dout256);
            testVectorCastFloatToDouble(fspec256, dspec512, fin256, dout512);
        }
    }

    @Test
    static void testCastFromFloatFail() {
        float[] fin64 = new float[fspec64.length()];
        float[] fin128 = new float[fspec128.length()];
        float[] fin256 = new float[fspec256.length()];
        float[] fin512 = new float[fspec512.length()];

        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastFloatToByteFail(fspec64, bspec64, fin64);
            testVectorCastFloatToByteFail(fspec64, bspec128, fin64);
            testVectorCastFloatToByteFail(fspec64, bspec256, fin64);
            testVectorCastFloatToByteFail(fspec64, bspec512, fin64);

            testVectorCastFloatToByteFail(fspec128, bspec64, fin128);
            testVectorCastFloatToByteFail(fspec128, bspec128, fin128);
            testVectorCastFloatToByteFail(fspec128, bspec256, fin128);
            testVectorCastFloatToByteFail(fspec128, bspec512, fin128);

            testVectorCastFloatToByteFail(fspec256, bspec128, fin256);
            testVectorCastFloatToByteFail(fspec256, bspec256, fin256);
            testVectorCastFloatToByteFail(fspec256, bspec512, fin256);

            testVectorCastFloatToByteFail(fspec512, bspec64, fin512);
            testVectorCastFloatToByteFail(fspec512, bspec256, fin512);
            testVectorCastFloatToByteFail(fspec512, bspec512, fin512);

            testVectorCastFloatToShortFail(fspec64, sspec64, fin64);
            testVectorCastFloatToShortFail(fspec64, sspec128, fin64);
            testVectorCastFloatToShortFail(fspec64, sspec256, fin64);
            testVectorCastFloatToShortFail(fspec64, sspec512, fin64);

            testVectorCastFloatToShortFail(fspec128, sspec128, fin128);
            testVectorCastFloatToShortFail(fspec128, sspec256, fin128);
            testVectorCastFloatToShortFail(fspec128, sspec512, fin128);

            testVectorCastFloatToShortFail(fspec256, sspec64, fin256);
            testVectorCastFloatToShortFail(fspec256, sspec256, fin256);
            testVectorCastFloatToShortFail(fspec256, sspec512, fin256);

            testVectorCastFloatToShortFail(fspec512, sspec64, fin512);
            testVectorCastFloatToShortFail(fspec512, sspec128, fin512);
            testVectorCastFloatToShortFail(fspec512, sspec512, fin512);

            testVectorCastFloatToIntFail(fspec64, ispec128, fin64);
            testVectorCastFloatToIntFail(fspec64, ispec256, fin64);
            testVectorCastFloatToIntFail(fspec64, ispec512, fin64);

            testVectorCastFloatToIntFail(fspec128, ispec64, fin128);
            testVectorCastFloatToIntFail(fspec128, ispec256, fin128);
            testVectorCastFloatToIntFail(fspec128, ispec512, fin128);

            testVectorCastFloatToIntFail(fspec256, ispec64, fin256);
            testVectorCastFloatToIntFail(fspec256, ispec128, fin256);
            testVectorCastFloatToIntFail(fspec256, ispec512, fin256);

            testVectorCastFloatToIntFail(fspec512, ispec64, fin512);
            testVectorCastFloatToIntFail(fspec512, ispec128, fin512);
            testVectorCastFloatToIntFail(fspec512, ispec256, fin512);

            testVectorCastFloatToLongFail(fspec64, lspec64, fin64);
            testVectorCastFloatToLongFail(fspec64, lspec256, fin64);
            testVectorCastFloatToLongFail(fspec64, lspec512, fin64);

            testVectorCastFloatToLongFail(fspec128, lspec64, fin128);
            testVectorCastFloatToLongFail(fspec128, lspec128, fin128);
            testVectorCastFloatToLongFail(fspec128, lspec512, fin128);

            testVectorCastFloatToLongFail(fspec256, lspec64, fin256);
            testVectorCastFloatToLongFail(fspec256, lspec128, fin256);
            testVectorCastFloatToLongFail(fspec256, lspec256, fin256);

            testVectorCastFloatToLongFail(fspec512, lspec64, fin512);
            testVectorCastFloatToLongFail(fspec512, lspec128, fin512);
            testVectorCastFloatToLongFail(fspec512, lspec256, fin512);
            testVectorCastFloatToLongFail(fspec512, lspec512, fin512);

            testVectorCastFloatToFloatFail(fspec64, fspec128, fin64);
            testVectorCastFloatToFloatFail(fspec64, fspec256, fin64);
            testVectorCastFloatToFloatFail(fspec64, fspec512, fin64);

            testVectorCastFloatToFloatFail(fspec128, fspec64, fin128);
            testVectorCastFloatToFloatFail(fspec128, fspec256, fin128);
            testVectorCastFloatToFloatFail(fspec128, fspec512, fin128);

            testVectorCastFloatToFloatFail(fspec256, fspec64, fin256);
            testVectorCastFloatToFloatFail(fspec256, fspec128, fin256);
            testVectorCastFloatToFloatFail(fspec256, fspec512, fin256);

            testVectorCastFloatToFloatFail(fspec512, fspec64, fin512);
            testVectorCastFloatToFloatFail(fspec512, fspec128, fin512);
            testVectorCastFloatToFloatFail(fspec512, fspec256, fin512);

            testVectorCastFloatToDoubleFail(fspec64, dspec64, fin64);
            testVectorCastFloatToDoubleFail(fspec64, dspec256, fin64);
            testVectorCastFloatToDoubleFail(fspec64, dspec512, fin64);

            testVectorCastFloatToDoubleFail(fspec128, dspec64, fin128);
            testVectorCastFloatToDoubleFail(fspec128, dspec128, fin128);
            testVectorCastFloatToDoubleFail(fspec128, dspec512, fin128);

            testVectorCastFloatToDoubleFail(fspec256, dspec64, fin256);
            testVectorCastFloatToDoubleFail(fspec256, dspec128, fin256);
            testVectorCastFloatToDoubleFail(fspec256, dspec256, fin256);

            testVectorCastFloatToDoubleFail(fspec512, dspec64, fin512);
            testVectorCastFloatToDoubleFail(fspec512, dspec128, fin512);
            testVectorCastFloatToDoubleFail(fspec512, dspec256, fin512);
            testVectorCastFloatToDoubleFail(fspec512, dspec512, fin512);
        }
    }

    @Test(dataProvider = "doubleUnaryOpProvider")
    static void testCastFromDouble(IntFunction<double[]> fa) {
        double[] din64 = fa.apply(dspec64.length());
        double[] din128 = fa.apply(dspec128.length());
        double[] din256 = fa.apply(dspec256.length());
        double[] din512 = fa.apply(dspec512.length());

        byte[] bout64 = new byte[bspec64.length()];

        short[] sout64 = new short[sspec64.length()];
        short[] sout128 = new short[sspec128.length()];

        int[] iout64 = new int[ispec64.length()];
        int[] iout128 = new int[ispec128.length()];
        int[] iout256 = new int[ispec256.length()];

        long[] lout64 = new long[lspec64.length()];
        long[] lout128 = new long[lspec128.length()];
        long[] lout256 = new long[lspec256.length()];
        long[] lout512 = new long[lspec512.length()];

        float[] fout64 = new float[fspec64.length()];
        float[] fout128 = new float[fspec128.length()];
        float[] fout256 = new float[fspec256.length()];

        double[] dout64 = new double[dspec64.length()];
        double[] dout128 = new double[dspec128.length()];
        double[] dout256 = new double[dspec256.length()];
        double[] dout512 = new double[dspec512.length()];

        for (int i = 0; i < NUM_ITER; i++) {
            testVectorCastDoubleToByte(dspec512, bspec64, din512, bout64);

            testVectorCastDoubleToShort(dspec256, sspec64, din256, sout64);
            testVectorCastDoubleToShort(dspec512, sspec128, din512, sout128);

            testVectorCastDoubleToInt(dspec128, ispec64, din128, iout64);
            testVectorCastDoubleToInt(dspec256, ispec128, din256, iout128);
            testVectorCastDoubleToInt(dspec512, ispec256, din512, iout256);

            testVectorCastDoubleToLong(dspec64, lspec64, din64, lout64);
            testVectorCastDoubleToLong(dspec128, lspec128, din128, lout128);
            testVectorCastDoubleToLong(dspec256, lspec256, din256, lout256);
            testVectorCastDoubleToLong(dspec512, lspec512, din512, lout512);

            testVectorCastDoubleToFloat(dspec128, fspec64, din128, fout64);
            testVectorCastDoubleToFloat(dspec256, fspec128, din256, fout128);
            testVectorCastDoubleToFloat(dspec512, fspec256, din512, fout256);

            testVectorCastDoubleToDouble(dspec64, dspec64, din64, dout64);
            testVectorCastDoubleToDouble(dspec128, dspec128, din128, dout128);
            testVectorCastDoubleToDouble(dspec256, dspec256, din256, dout256);
            testVectorCastDoubleToDouble(dspec512, dspec512, din512, dout512);
        }
    }

    @Test
    static void testCastFromDoubleFail() {
        double[] din64 = new double[dspec64.length()];
        double[] din128 = new double[dspec128.length()];
        double[] din256 = new double[dspec256.length()];
        double[] din512 = new double[dspec512.length()];

        for (int i = 0; i < INVOC_COUNT; i++) {
            testVectorCastDoubleToByteFail(dspec64, bspec64, din64);
            testVectorCastDoubleToByteFail(dspec64, bspec128, din64);
            testVectorCastDoubleToByteFail(dspec64, bspec256, din64);
            testVectorCastDoubleToByteFail(dspec64, bspec512, din64);

            testVectorCastDoubleToByteFail(dspec128, bspec64, din128);
            testVectorCastDoubleToByteFail(dspec128, bspec128, din128);
            testVectorCastDoubleToByteFail(dspec128, bspec256, din128);
            testVectorCastDoubleToByteFail(dspec128, bspec512, din128);

            testVectorCastDoubleToByteFail(dspec256, bspec64, din256);
            testVectorCastDoubleToByteFail(dspec256, bspec128, din256);
            testVectorCastDoubleToByteFail(dspec256, bspec256, din256);
            testVectorCastDoubleToByteFail(dspec256, bspec512, din256);

            testVectorCastDoubleToByteFail(dspec512, bspec128, din512);
            testVectorCastDoubleToByteFail(dspec512, bspec256, din512);
            testVectorCastDoubleToByteFail(dspec512, bspec512, din512);

            testVectorCastDoubleToShortFail(dspec64, sspec64, din64);
            testVectorCastDoubleToShortFail(dspec64, sspec128, din64);
            testVectorCastDoubleToShortFail(dspec64, sspec256, din64);
            testVectorCastDoubleToShortFail(dspec64, sspec512, din64);

            testVectorCastDoubleToShortFail(dspec128, sspec64, din128);
            testVectorCastDoubleToShortFail(dspec128, sspec128, din128);
            testVectorCastDoubleToShortFail(dspec128, sspec256, din128);
            testVectorCastDoubleToShortFail(dspec128, sspec512, din128);

            testVectorCastDoubleToShortFail(dspec256, sspec128, din256);
            testVectorCastDoubleToShortFail(dspec256, sspec256, din256);
            testVectorCastDoubleToShortFail(dspec256, sspec512, din256);

            testVectorCastDoubleToShortFail(dspec512, sspec64, din512);
            testVectorCastDoubleToShortFail(dspec512, sspec256, din512);
            testVectorCastDoubleToShortFail(dspec512, sspec512, din512);

            testVectorCastDoubleToIntFail(dspec64, ispec64, din64);
            testVectorCastDoubleToIntFail(dspec64, ispec128, din64);
            testVectorCastDoubleToIntFail(dspec64, ispec256, din64);
            testVectorCastDoubleToIntFail(dspec64, ispec512, din64);

            testVectorCastDoubleToIntFail(dspec128, ispec128, din128);
            testVectorCastDoubleToIntFail(dspec128, ispec256, din128);
            testVectorCastDoubleToIntFail(dspec128, ispec512, din128);

            testVectorCastDoubleToIntFail(dspec256, ispec64, din256);
            testVectorCastDoubleToIntFail(dspec256, ispec256, din256);
            testVectorCastDoubleToIntFail(dspec256, ispec512, din256);

            testVectorCastDoubleToIntFail(dspec512, ispec64, din512);
            testVectorCastDoubleToIntFail(dspec512, ispec128, din512);
            testVectorCastDoubleToIntFail(dspec512, ispec512, din512);

            testVectorCastDoubleToLongFail(dspec64, lspec128, din64);
            testVectorCastDoubleToLongFail(dspec64, lspec256, din64);
            testVectorCastDoubleToLongFail(dspec64, lspec512, din64);

            testVectorCastDoubleToLongFail(dspec128, lspec64, din128);
            testVectorCastDoubleToLongFail(dspec128, lspec256, din128);
            testVectorCastDoubleToLongFail(dspec128, lspec512, din128);

            testVectorCastDoubleToLongFail(dspec256, lspec64, din256);
            testVectorCastDoubleToLongFail(dspec256, lspec128, din256);
            testVectorCastDoubleToLongFail(dspec256, lspec512, din256);

            testVectorCastDoubleToLongFail(dspec512, lspec64, din512);
            testVectorCastDoubleToLongFail(dspec512, lspec128, din512);
            testVectorCastDoubleToLongFail(dspec512, lspec256, din512);

            testVectorCastDoubleToFloatFail(dspec64, fspec64, din64);
            testVectorCastDoubleToFloatFail(dspec64, fspec128, din64);
            testVectorCastDoubleToFloatFail(dspec64, fspec256, din64);
            testVectorCastDoubleToFloatFail(dspec64, fspec512, din64);

            testVectorCastDoubleToFloatFail(dspec128, fspec128, din128);
            testVectorCastDoubleToFloatFail(dspec128, fspec256, din128);
            testVectorCastDoubleToFloatFail(dspec128, fspec512, din128);

            testVectorCastDoubleToFloatFail(dspec256, fspec64, din256);
            testVectorCastDoubleToFloatFail(dspec256, fspec256, din256);
            testVectorCastDoubleToFloatFail(dspec256, fspec512, din256);

            testVectorCastDoubleToFloatFail(dspec512, fspec64, din512);
            testVectorCastDoubleToFloatFail(dspec512, fspec128, din512);
            testVectorCastDoubleToFloatFail(dspec512, fspec512, din512);

            testVectorCastDoubleToDoubleFail(dspec64, dspec128, din64);
            testVectorCastDoubleToDoubleFail(dspec64, dspec256, din64);
            testVectorCastDoubleToDoubleFail(dspec64, dspec512, din64);

            testVectorCastDoubleToDoubleFail(dspec128, dspec64, din128);
            testVectorCastDoubleToDoubleFail(dspec128, dspec256, din128);
            testVectorCastDoubleToDoubleFail(dspec128, dspec512, din128);

            testVectorCastDoubleToDoubleFail(dspec256, dspec64, din256);
            testVectorCastDoubleToDoubleFail(dspec256, dspec128, din256);
            testVectorCastDoubleToDoubleFail(dspec256, dspec512, din256);

            testVectorCastDoubleToDoubleFail(dspec512, dspec64, din512);
            testVectorCastDoubleToDoubleFail(dspec512, dspec128, din512);
            testVectorCastDoubleToDoubleFail(dspec512, dspec256, din512);
        }
    }
}
