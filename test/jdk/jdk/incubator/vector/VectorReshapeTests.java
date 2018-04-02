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
}
