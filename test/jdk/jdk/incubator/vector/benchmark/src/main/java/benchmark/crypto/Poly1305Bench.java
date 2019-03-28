/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */

package benchmark.crypto;

import org.openjdk.jmh.annotations.*;
import jdk.incubator.vector.*;
import java.util.Arrays;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 8, time = 2)
public class Poly1305Bench {

    @Param({"16384", "65536"})
    private int dataSize;

    private Poly1305Vector poly1305_S128 = makePoly1305(Vector.Shape.S_128_BIT);
    private Poly1305Vector poly1305_S256 = makePoly1305(Vector.Shape.S_256_BIT);
    private Poly1305Vector poly1305_S512 = makePoly1305(Vector.Shape.S_512_BIT);

    private byte[] in;
    private byte[] out = new byte[16];
    private byte[] key = new byte[32];

    private static Poly1305Vector makePoly1305(Vector.Shape shape) {
        Poly1305Vector poly = new Poly1305Vector(shape);
        runKAT(poly);
        return poly;
    }

    @Setup
    public void setup() {
        in = new byte[dataSize];
    }

    @Benchmark
    public void auth128() {
        poly1305_S128.computeTag(key, in, out);
    }

    @Benchmark
    public void auth256() {
        poly1305_S256.computeTag(key, in, out);
    }

    @Benchmark
    public void auth512() {
        poly1305_S512.computeTag(key, in, out);
    }

    private static class Poly1305Vector {

        private static final int BITS_PER_LIMB = 26;
        private static final int LIMB_MASK = (1 << BITS_PER_LIMB) - 1;
        private static final int KEY_LENGTH = 32;
        private static final int RS_LENGTH = KEY_LENGTH / 2;

        private final LongVector.LongSpecies longSpecies;
        private final IntVector.IntSpecies intSpecies;
        private final int vectorWidth;
        private final int parBlockCount;

        private final LongVector.Shuffle<Long> inShuffle0;
        private final LongVector.Shuffle<Long> inShuffle1;
        private final IntVector.Mask<Long> inMask;

        public Poly1305Vector(Vector.Shape shape) {

            this.longSpecies = LongVector.species(shape);
            int intSize = shape.bitSize() / 2;
            Vector.Shape intShape = Vector.Shape.forBitSize(intSize);
            this.intSpecies = IntVector.species(intShape);
            this.vectorWidth = longSpecies.length();
            this.parBlockCount = vectorWidth * 16;

            this.inShuffle0 = makeInShuffle0();
            this.inShuffle1 = makeInShuffle1();
            this.inMask = makeInMask();
        }

        private LongVector.Shuffle<Long> makeInShuffle0() {
            int[] indexArr = new int[vectorWidth];
            for (int i = 0; i < indexArr.length; i++) {
                indexArr[i] = (2 * i) % vectorWidth;
            }
            return LongVector.shuffleFromArray(longSpecies, indexArr, 0);
        }
        private LongVector.Shuffle<Long> makeInShuffle1() {
            int[] indexArr = new int[vectorWidth];
            for (int i = 0; i < indexArr.length; i++) {
                indexArr[i] = ((2 * i) % vectorWidth) + 1;
            }
            return LongVector.shuffleFromArray(longSpecies, indexArr, 0);
        }
        private LongVector.Mask<Long> makeInMask() {
            boolean[] maskArr = new boolean[vectorWidth];
            for (int i = vectorWidth / 2; i < vectorWidth; i++) {
                maskArr[i] = true;
            }
            return LongVector.maskFromArray(longSpecies, maskArr, 0);
        }

        private static int[] fromByteArray(byte[] buf) {
            int[] result = new int[5];

            result[0]
                    = (buf[0] & 0xFF)
                    + ((buf[1] & 0xFF) << 8)
                    + ((buf[2] & 0xFF) << 16)
                    + ((buf[3] & 0x03) << 24);
            result[1]
                    = ((buf[3] & 0xFF) >> 2)
                    + ((buf[4] & 0xFF) << 6)
                    + ((buf[5] & 0xFF) << 14)
                    + ((buf[6] & 0x0F) << 22);
            result[2]
                    = ((buf[6] & 0xFF) >> 4)
                    + ((buf[7] & 0xFF) << 4)
                    + ((buf[8] & 0xFF) << 12)
                    + ((buf[9] & 0x3F) << 20);
            result[3]
                    = ((buf[9] & 0xFF) >> 6)
                    + ((buf[10] & 0xFF) << 2)
                    + ((buf[11] & 0xFF) << 10)
                    + ((buf[12] & 0xFF) << 18);
            result[4]
                    = (buf[13] & 0xFF)
                    + ((buf[14] & 0xFF) << 8)
                    + ((buf[15] & 0xFF) << 16);

            return result;
        }

        private static void toByteArray(long v0, long v1, long v2, long v3,
            long v4, byte[] dst) {

            dst[0] = (byte) v0;
            v0 >>= 8;
            dst[1] = (byte) v0;
            v0 >>= 8;
            dst[2] = (byte) v0;
            v0 >>= 8;
            dst[3] = (byte) v0;

            dst[3] += (v1 & 0x3F) << 2;
            v1 >>= 6;
            dst[4] = (byte) v1;
            v1 >>= 8;
            dst[5] = (byte) v1;
            v1 >>= 8;
            dst[6] = (byte) v1;

            dst[6] += (v2 & 0xF) << 4;
            v2 >>= 4;
            dst[7] = (byte) v2;
            v2 >>= 8;
            dst[8] = (byte) v2;
            v2 >>= 8;
            dst[9] = (byte) v2;

            dst[9] += (v3 & 0x3) << 6;
            v3 >>= 2;
            dst[10] = (byte) v3;
            v3 >>= 8;
            dst[11] = (byte) v3;
            v3 >>= 8;
            dst[12] = (byte) v3;

            dst[13] = (byte) v4;
            v4 >>= 8;
            dst[14] = (byte) v4;
            v4 >>= 8;
            dst[15] = (byte) v4;
        }

        protected static long carryValue(long x) {
            return x >> BITS_PER_LIMB;
        }

        public static void carryReduce(int[] r, long c0, long c1, long c2,
            long c3, long c4) {

            long c;

            c = carryValue(c3); c3 &= LIMB_MASK; c4 += c;
            c = carryValue(c4); c4 &= LIMB_MASK; c0 += c * 5;
            c = carryValue(c0); c0 &= LIMB_MASK; c1 += c;
            c = carryValue(c1); c1 &= LIMB_MASK; c2 += c;
            c = carryValue(c2); c2 &= LIMB_MASK; c3 += c;
            c = carryValue(c3); c3 &= LIMB_MASK; c4 += c;

            r[0] = (int) c0;
            r[1] = (int) c1;
            r[2] = (int) c2;
            r[3] = (int) c3;
            r[4] = (int) c4;
        }

        private int[] multiply(int[] a, int[] b) {
            int[] result = new int[5];

            long a0 = a[0];
            long a1 = a[1];
            long a2 = a[2];
            long a3 = a[3];
            long a4 = a[4];

            long c0 = (a0 * b[0]) + 5 * (a1 * b[4]) + 5 * (a2 * b[3]) +
                5 * (a3 * b[2]) + 5 * (a4 * b[1]);
            long c1 = (a0 * b[1]) + (a1 * b[0]) + 5 * (a2 * b[4]) +
                5 * (a3 * b[3]) + 5 * (a4 * b[2]);
            long c2 = (a0 * b[2]) + (a1 * b[1]) + (a2 * b[0]) +
                5 * (a3 * b[4]) + 5 * (a4 * b[3]);
            long c3 = (a0 * b[3]) + (a1 * b[2]) + (a2 * b[1]) + (a3 * b[0]) +
                5 * (a4 * b[4]);
            long c4 = (a0 * b[4]) + (a1 * b[3]) + (a2 * b[2]) + (a3 * b[1]) +
                (a4 * b[0]);

            carryReduce(result, c0, c1, c2, c3, c4);

            return result;
        }

        private LongVector rPowerVec(int[][] r, long[] temp, int maxIndex,
            int secondIndex) {

            for (int i = 0; i < temp.length; i++) {
                temp[i] = r[maxIndex - i][secondIndex];
            }
            return LongVector.fromArray(longSpecies, temp, 0);
        }

        public void computeTag(byte[] key, byte[] msg, byte[] out) {

            byte[] keyBytes = key.clone();

            // setup key values
            // Clamp the bytes in the "r" half of the key.
            keyBytes[3] &= 15;
            keyBytes[7] &= 15;
            keyBytes[11] &= 15;
            keyBytes[15] &= 15;
            keyBytes[4] &= 252;
            keyBytes[8] &= 252;
            keyBytes[12] &= 252;

            // Create IntegerModuloP elements from the r and s values
            int[][] r = new int[vectorWidth][];
            r[0] = fromByteArray(keyBytes);
            for (int i = 1; i < vectorWidth; i++) {
                r[i] = multiply(r[i - 1], r[0]);
            }

            int rUpIndex = vectorWidth - 1;
            IntVector rUp0_int = intSpecies.broadcast(r[rUpIndex][0]);
            IntVector rUp1_int = intSpecies.broadcast(r[rUpIndex][1]);
            IntVector rUp2_int = intSpecies.broadcast(r[rUpIndex][2]);
            IntVector rUp3_int = intSpecies.broadcast(r[rUpIndex][3]);
            IntVector rUp4_int = intSpecies.broadcast(r[rUpIndex][4]);

            IntVector r5Up1_int = rUp1_int.mul(5);
            IntVector r5Up2_int = rUp2_int.mul(5);
            IntVector r5Up3_int = rUp3_int.mul(5);
            IntVector r5Up4_int = rUp4_int.mul(5);

            LongVector longMsg0 = LongVector.fromByteArray(longSpecies, msg, 0);
            LongVector longMsg1 =
                LongVector.fromByteArray(longSpecies, msg, vectorWidth * 8);

            LongVector inAlign0 =
                longMsg0.rearrange(longMsg1, inShuffle0, inMask);
            LongVector inAlign1 =
                longMsg0.rearrange(longMsg1, inShuffle1, inMask);

            IntVector a0 = (IntVector)
                inAlign0.and(LIMB_MASK).cast(intSpecies);
            IntVector a1 = (IntVector)
                inAlign0.shiftR(26).and(LIMB_MASK).cast(intSpecies);
            IntVector a2 = (IntVector)
                inAlign0.shiftR(52).and(0xFFF).cast(intSpecies);
            a2 = a2.or(inAlign1.and(0x3FFF).shiftL(12).cast(intSpecies));
            IntVector a3 = (IntVector)
                inAlign1.shiftR(14).and(LIMB_MASK).cast(intSpecies);
            IntVector a4 = (IntVector)
                inAlign1.shiftR(40).and(0xFFFFFF).cast(intSpecies);
            a4 = a4.or(1 << 24);

            int numParBlocks = msg.length / parBlockCount - 1;
            for (int i = 0; i < numParBlocks; i++) {

                // multiply and reduce
                LongVector c0 = (LongVector)
                    a0.cast(longSpecies).mul(rUp0_int.cast(longSpecies))
                    .add(a1.cast(longSpecies).mul(r5Up4_int.cast(longSpecies)))
                    .add(a2.cast(longSpecies).mul(r5Up3_int.cast(longSpecies)))
                    .add(a3.cast(longSpecies).mul(r5Up2_int.cast(longSpecies)))
                    .add(a4.cast(longSpecies).mul(r5Up1_int.cast(longSpecies)));

                LongVector c1 = (LongVector)
                    a0.cast(longSpecies).mul(rUp1_int.cast(longSpecies))
                    .add(a1.cast(longSpecies).mul(rUp0_int.cast(longSpecies)))
                    .add(a2.cast(longSpecies).mul(r5Up4_int.cast(longSpecies)))
                    .add(a3.cast(longSpecies).mul(r5Up3_int.cast(longSpecies)))
                    .add(a4.cast(longSpecies).mul(r5Up2_int.cast(longSpecies)));

                LongVector c2 = (LongVector)
                    a0.cast(longSpecies).mul(rUp2_int.cast(longSpecies))
                    .add(a1.cast(longSpecies).mul(rUp1_int.cast(longSpecies)))
                    .add(a2.cast(longSpecies).mul(rUp0_int.cast(longSpecies)))
                    .add(a3.cast(longSpecies).mul(r5Up4_int.cast(longSpecies)))
                    .add(a4.cast(longSpecies).mul(r5Up3_int.cast(longSpecies)));

                LongVector c3 = (LongVector)
                    a0.cast(longSpecies).mul(rUp3_int.cast(longSpecies))
                    .add(a1.cast(longSpecies).mul(rUp2_int.cast(longSpecies)))
                    .add(a2.cast(longSpecies).mul(rUp1_int.cast(longSpecies)))
                    .add(a3.cast(longSpecies).mul(rUp0_int.cast(longSpecies)))
                    .add(a4.cast(longSpecies).mul(r5Up4_int.cast(longSpecies)));

                LongVector c4 = (LongVector)
                    a0.cast(longSpecies).mul(rUp4_int.cast(longSpecies))
                    .add(a1.cast(longSpecies).mul(rUp3_int.cast(longSpecies)))
                    .add(a2.cast(longSpecies).mul(rUp2_int.cast(longSpecies)))
                    .add(a3.cast(longSpecies).mul(rUp1_int.cast(longSpecies)))
                    .add(a4.cast(longSpecies).mul(rUp0_int.cast(longSpecies)));

                // carry/reduce
                // Note: this carry/reduce sequence might not be correct
                c4 = c4.add(c3.shiftR(BITS_PER_LIMB));
                c3 = c3.and(LIMB_MASK);
                c0 = c0.add(c4.shiftR(BITS_PER_LIMB).mul(5));
                c4 = c4.and(LIMB_MASK);
                c1 = c1.add(c0.shiftR(BITS_PER_LIMB));
                c0 = c0.and(LIMB_MASK);
                c2 = c2.add(c1.shiftR(BITS_PER_LIMB));
                c1 = c1.and(LIMB_MASK);
                c3 = c3.add(c2.shiftR(BITS_PER_LIMB));
                c2 = c2.and(LIMB_MASK);
                c4 = c4.add(c3.shiftR(BITS_PER_LIMB));
                c3 = c3.and(LIMB_MASK);

                a0 = (IntVector) c0.cast(intSpecies);
                a1 = (IntVector) c1.cast(intSpecies);
                a2 = (IntVector) c2.cast(intSpecies);
                a3 = (IntVector) c3.cast(intSpecies);
                a4 = (IntVector) c4.cast(intSpecies);

                // fromByteArray and add next part of message
                int start = parBlockCount * (i + 1);

                longMsg0 = LongVector.fromByteArray(longSpecies, msg, start);
                longMsg1 = LongVector.fromByteArray(longSpecies, msg,
                    start + vectorWidth * 8);

                inAlign0 = longMsg0.rearrange(longMsg1, inShuffle0, inMask);
                inAlign1 = longMsg0.rearrange(longMsg1, inShuffle1, inMask);

                IntVector in0 = (IntVector)
                    inAlign0.and(LIMB_MASK).cast(intSpecies);
                IntVector in1 = (IntVector)
                    inAlign0.shiftR(26).and(LIMB_MASK).cast(intSpecies);
                IntVector in2 = (IntVector)
                    inAlign0.shiftR(52).and(0xFFF).cast(intSpecies);
                in2 = in2.or(inAlign1.and(0x3FFF).shiftL(12).cast(intSpecies));
                IntVector in3 = (IntVector)
                    inAlign1.shiftR(14).and(LIMB_MASK).cast(intSpecies);
                IntVector in4 = (IntVector)
                    inAlign1.shiftR(40).and(0xFFFFFF).cast(intSpecies);
                in4 = in4.or(1 << 24);

                a0 = a0.add(in0);
                a1 = a1.add(in1);
                a2 = a2.add(in2);
                a3 = a3.add(in3);
                a4 = a4.add(in4);
            }

            // multiply by powers of r
            long[] rTemp = new long[vectorWidth];
            LongVector rFin0 = rPowerVec(r, rTemp, rUpIndex, 0);
            LongVector rFin1 = rPowerVec(r, rTemp, rUpIndex, 1);
            LongVector rFin2 = rPowerVec(r, rTemp, rUpIndex, 2);
            LongVector rFin3 = rPowerVec(r, rTemp, rUpIndex, 3);
            LongVector rFin4 = rPowerVec(r, rTemp, rUpIndex, 4);

            LongVector r5Fin_1 = rFin1.mul(5);
            LongVector r5Fin_2 = rFin2.mul(5);
            LongVector r5Fin_3 = rFin3.mul(5);
            LongVector r5Fin_4 = rFin4.mul(5);

            LongVector c0 = (LongVector) a0.cast(longSpecies).mul(rFin0)
                .add(a1.cast(longSpecies).mul(r5Fin_4))
                .add(a2.cast(longSpecies).mul(r5Fin_3))
                .add(a3.cast(longSpecies).mul(r5Fin_2))
                .add(a4.cast(longSpecies).mul(r5Fin_1));
            LongVector c1 = (LongVector) a0.cast(longSpecies).mul(rFin1)
                .add(a1.cast(longSpecies).mul(rFin0))
                .add(a2.cast(longSpecies).mul(r5Fin_4))
                .add(a3.cast(longSpecies).mul(r5Fin_3))
                .add(a4.cast(longSpecies).mul(r5Fin_2));
            LongVector c2 = (LongVector) a0.cast(longSpecies).mul(rFin2)
                .add(a1.cast(longSpecies).mul(rFin1))
                .add(a2.cast(longSpecies).mul(rFin0))
                .add(a3.cast(longSpecies).mul(r5Fin_4))
                .add(a4.cast(longSpecies).mul(r5Fin_3));
            LongVector c3 = (LongVector) a0.cast(longSpecies).mul(rFin3)
                .add(a1.cast(longSpecies).mul(rFin2))
                .add(a2.cast(longSpecies).mul(rFin1))
                .add(a3.cast(longSpecies).mul(rFin0))
                .add(a4.cast(longSpecies).mul(r5Fin_4));
            LongVector c4 = (LongVector) a0.cast(longSpecies).mul(rFin4)
                .add(a1.cast(longSpecies).mul(rFin3))
                .add(a2.cast(longSpecies).mul(rFin2))
                .add(a3.cast(longSpecies).mul(rFin1))
                .add(a4.cast(longSpecies).mul(rFin0));

            c4 = c4.add(c3.shiftR(BITS_PER_LIMB));
            c3 = c3.and(LIMB_MASK);
            c0 = c0.add(c4.shiftR(BITS_PER_LIMB).mul(5));
            c4 = c4.and(LIMB_MASK);
            c1 = c1.add(c0.shiftR(BITS_PER_LIMB));
            c0 = c0.and(LIMB_MASK);
            c2 = c2.add(c1.shiftR(BITS_PER_LIMB));
            c1 = c1.and(LIMB_MASK);
            c3 = c3.add(c2.shiftR(BITS_PER_LIMB));
            c2 = c2.and(LIMB_MASK);
            c4 = c4.add(c3.shiftR(BITS_PER_LIMB));
            c3 = c3.and(LIMB_MASK);

            a0 = (IntVector) c0.cast(intSpecies);
            a1 = (IntVector) c1.cast(intSpecies);
            a2 = (IntVector) c2.cast(intSpecies);
            a3 = (IntVector) c3.cast(intSpecies);
            a4 = (IntVector) c4.cast(intSpecies);

            // collect lanes and calculate tag
            long a0Fin = a0.addAll();
            long a1Fin = a1.addAll();
            long a2Fin = a2.addAll();
            long a3Fin = a3.addAll();
            long a4Fin = a4.addAll();

            // carry/reduce the result
            a4Fin = a4Fin + (a3Fin >>> BITS_PER_LIMB);
            a3Fin = a3Fin & LIMB_MASK;
            a0Fin = a0Fin + ((a4Fin >>> BITS_PER_LIMB) * 5);
            a4Fin = a4Fin & LIMB_MASK;
            a1Fin = a1Fin + (a0Fin >>> BITS_PER_LIMB);
            a0Fin = a0Fin & LIMB_MASK;
            a2Fin = a2Fin + (a1Fin >>> BITS_PER_LIMB);
            a1Fin = a1Fin & LIMB_MASK;
            a3Fin = a3Fin + (a2Fin >>> BITS_PER_LIMB);
            a2Fin = a2Fin & LIMB_MASK;
            a4Fin = a4Fin + (a3Fin >>> BITS_PER_LIMB);
            a3Fin = a3Fin & LIMB_MASK;

            byte[] s_arr =
                Arrays.copyOfRange(keyBytes, RS_LENGTH, 2 * RS_LENGTH);
            int[] s = fromByteArray(s_arr);

            // Add in the s-half of the key to the accumulator
            a0Fin += s[0];
            a1Fin += s[1];
            a2Fin += s[2];
            a3Fin += s[3];
            a4Fin += s[4];

            // final carry mod 2^130
            a1Fin = a1Fin + (a0Fin >> BITS_PER_LIMB);
            a0Fin = a0Fin & LIMB_MASK;
            a2Fin = a2Fin + (a1Fin >> BITS_PER_LIMB);
            a1Fin = a1Fin & LIMB_MASK;
            a3Fin = a3Fin + (a2Fin >> BITS_PER_LIMB);
            a2Fin = a2Fin & LIMB_MASK;
            a4Fin = a4Fin + (a3Fin >> BITS_PER_LIMB);
            a3Fin = a3Fin & LIMB_MASK;
            a4Fin = a4Fin & LIMB_MASK;

            // put result in buffer
            toByteArray(a0Fin, a1Fin, a2Fin, a3Fin, a4Fin, out);
        }
    }


    private static byte[] hexStringToByteArray(String str) {
        byte[] result = new byte[str.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Character.digit(str.charAt(2 * i), 16);
            result[i] <<= 4;
            result[i] += Character.digit(str.charAt(2 * i + 1), 16);
        }
        return result;
    }

    public static String byteArrayToHexString(byte[] arr) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arr.length; ++i) {
            byte curVal = arr[i];
            result.append(Character.forDigit(curVal >> 4 & 0xF, 16));
            result.append(Character.forDigit(curVal & 0xF, 16));
        }
        return result.toString();
    }

    private static void kat(Poly1305Vector poly1305, String key, String msg,
        String expectedTag) {

        kat(poly1305, hexStringToByteArray(key), hexStringToByteArray(msg),
            hexStringToByteArray(expectedTag));
    }

    private static void kat(Poly1305Vector poly1305, byte[] key, byte[] msg,
        byte[] expectedTag) {

        byte[] tag = new byte[expectedTag.length];
        poly1305.computeTag(key, msg, tag);
        if (!Arrays.equals(tag, expectedTag)) {
            throw new RuntimeException(
                    "bad tag: " + byteArrayToHexString(tag) +
                    " expected: " + byteArrayToHexString(expectedTag));
        }
    }

    /*
     * Poly1305 Known Answer Tests to ensure that the implementation is correct.
     */
    private static void runKAT(Poly1305Vector poly1305) {
        kat(poly1305,
            "d212b886dd4682a41f1759e6c5aef84760e5a63d4423ca7d1fb5c7ecfc5dac27",
            "5d2ad39e2a7b0bc5f375488643acf391188d01ad936971457427bc053c4262a1" +
            "598532850def8573213c5f79fa736703c57c03ec49b55617210998c8af408698" +
            "866632a7ecf7e9a688605cbca919e17e2badd090a7a6d83ad90be0617fa44642" +
            "cc9a1ca38514a026cbea51c287ec0b56719fc61183c88e9450ba85aa8ab7d390",
            "7ccdfa8e82df540276e8172f705adce2");

        kat(poly1305,
            "2b0b684c86910104aee1d261ac4d5a0f5443b4b7746cf7f8ba03921d273f6a9b",
            "027b359f44a5d60f81073ceb74749207742529dcefa4a26a1817db2c8d50ba2b" +
            "d9e170cd1930946872d95e4eae41389f362087871a749897e0fbe42494e6f0b3" +
            "8db01e2059510b6fda4f422ce7d226433ba00940e1761baaff80d9b8f3a61d11" +
            "a109e6082d231cf85aa718199e6eaaaf07bad562469ef1b8e639c727967bf6da" +
            "bcd16fcb0fc102095325e2fac92e599e81c26900df1deb7b0a0b5c321a658024" +
            "26506740509ece646fecf33a517b66e57577372156aae85765c6b473521d1019" +
            "4f5fbe0e932cfee716e1d41c9154fb8e15b82ab7e807fb54f3d7d3e4c589cc9a" +
            "492d17ea4fd27894fa9d22a9db6d5df674cd1e97e7e8758a360291f22dfe1cc3",
            "84ca3a778faf0ab9f840fe5fb38ace27");

        kat(poly1305,
            "870c6fa7da2eadb845ac8b0eeaed4cf856eca67bf96b64a29a2e6a881821fa8a",
            "ff1a3b67a4f575be5f05c4054e4c7365838c2cbe786ba78900c8b43f197c3c4d" +
            "120432a287e434669af579bcd56f3320e54d2f97a306f917f2f41b1c97cc69db" +
            "4ac2051adccd687fa89f92504d1ab5c3006681d846c8051aabccca0024ef5ec4" +
            "c43b8701ffc9d14fef8d55e229ed210a2b9bde996f5d7b545d15e1fc32764604" +
            "b2a0384dd173aa800b7526c8ff397c05130bb6a1f2194968adaef6979b023cd8" +
            "d9195d2739351c7e4ac6c43508634f813641f669e78cbcf732ccb1321a2cd2c4" +
            "14c7df5b9ea3408f2e12fbf3a3cbdb98699dd5402725ec25f9fff9bcd0f93cb3" +
            "cf0dac016fec41a5ef3ae8b8d258a09f530ad14ad2e52186041592eac0ea22ff" +
            "8c3751009b516c60f7764cccbb816394ef035bd8cb2a38d5c6b9229e528e56cd" +
            "62600b5219b64212642384e628f01d790eeef4963a7d1a63a9faff79d4acfa09" +
            "78b58b0b623ae89389661aa408b16814d3baaca20978dce6888c3365f4ffd2fa" +
            "8f031a44f2e870a06da21d7becf450d335e1386268bc189435e7955a477bc368",
            "ff4e0ee6feb1c6a57e638a79fafc7c60");

        kat(poly1305,
            "c27987ae88a833ae2ea90371b2e257c15773da3bc34516b6b075446e1f844a81",
            "64e5a2e2940b173c7103ae931ced302a8f8c778f4e5c0b3677c51552655005d8" +
            "504b724107e7262448c94db83fc9c6a2a26fc973360dce15c0553b73bb733d3e" +
            "f61fcba8977e76c32523b80c3b45b1226b23ee17522f9b677880c69b356917ae" +
            "3c792a0c5b0c77b90dfa51483626323b7a73fffb1b128c595d553bf62a8f5bb9" +
            "fa48b4a850a932481bf607e8da84730c9052bba9316ec7eb84007a4eb5cbed5c" +
            "7c67ef32d4c5cb6cfbccd738d239857c240de6d3d4e5af14d480feb63541e5d8" +
            "036e088b2e32431e6fe0c4d3505aebe2e14bd02b6b15325f89aef048cd1236db" +
            "4461a59304b7c61ece2c52ef8ac4cf2326e6aaff013494b1b191be4ae4381f57" +
            "e72b947ee23d0a528087db9338bc28c68484929fc3436995b2083b06a765ceb7" +
            "09e9dd41ba896d99832d6851189766e844137d9a83d2890bc2be7afc82f9ebb8" +
            "bafe08ef5f7ff0cce9a1d08e6b797a17df04731f384a34b16e72e9f2ab070114" +
            "8008945509fe378658dc51eb752248f48364be327cd1b6bd148c518a976ae95d" +
            "d391f3b0d447251988c7e77400c9d44395b8f9f10cbc442a6804d0ad83e8c3e3" +
            "9fa09c2140fac143c90f09a7d907c57e29b528d54c8bd927f39aee2cec671213" +
            "c50fe657b29682d57a419e3e52dafb348cbe44b6c17e4be18f5c5e411734fcfc" +
            "99b9ca26f29a21cc93374ef1bfa86ca2bb3be76b94b4ef69ec790c968a51e4d0",
            "d4c09727f68fa3beb57ce9e74205b652");

        kat(poly1305,
            "2593adf2efc0e49c7fde0d45de4f7a21ceb76df45c0e5a917ef1f6b7fbf4fb7e",
            "23665b9a6d4f04c9d58347d32d64d4cbf8d4ca993a8bb73758e6eb2db9f97096" +
            "d0f00aca8cb16460d2bc15c0136fa92482602f47b3ec78244c4dc619d9b28afa" +
            "19b063c196bcff848eff179102cce29dfcc58bf90a2f6311e6d021e2573ccbb4" +
            "4e06947167c9865127c0b7362196523f97c8157058f7aebff475f77e23393dc1" +
            "a3031bbaf31270db3eadc00cca6ae073aa53160d095afdce0a202de8a23d9a38" +
            "b0bed20cbe64e1ec77fc13ba0cfc9be52edb70475bf1aeaaff25e11f2e0ae47f" +
            "f23cbd4a6219d276fcc6c8f12a739f11434c86d4e24397f96ef6e36d9195fa8a" +
            "48eb55990d69feacfb754b5019a3ebafa98d5544077b46c136cc11de8ee7f8a6" +
            "76a6696600088696233f4e8f060ba8a64890fb638469639bfb727ed758c36250" +
            "a553b7ce1115509f2bb19c13cea87003a8eff45ce9e1cff0a21ba5ae19226d50" +
            "e108db212a588e5f4c502468859b9b607922c3311b5d912bd9400e696d7debbb" +
            "9ac5454cc7d0f95fc242c491f095a02f0d3bd7ead0f0b7358c9b1d85e4e9ab75" +
            "24bb43867c94a21a4e0db6470a210c9dd937e4801396bd687127fa7c83014c85" +
            "372553c56dfd6cd9b75fa10483aea825f8e3fa53c6bf17467e37c2e7439ed0ea" +
            "6fb24d13d428965c44f1ac943c7bc77fa84711c91b41f5ee6d9a7d9091648a96" +
            "cc7c261d7fc5d964446d1e3dcc41d32ecaa8d7791b8462563fcf7f96cd1d11d4" +
            "34923e0150321356866f5bdafebc96f2661bfd3c1f104e96b6492cafcbe25fc6" +
            "ec0c92a3bbec7328e1905d5951fae04625a2452f596027a5d9c64eed55165c8a" +
            "23bc3f944b4fa9c7ad83ebc1777c7153d5de13d04c0a12e774b17906a62f5134" +
            "685c2de31da08bd04840299fd62d56ffe95248365034e7ba95961cebf0542b24",
            "b9f68b0996caf5135136b10b37fe5f81");

        kat(poly1305,
            "e9c8c78bc0ad5751f094fd4657fe5ef2a3c232f6930eef3431cde76659f04210",
            "914e57a2745fd475d7b8f982483fe11a05d7b55853239112d5ae99616c718b3c" +
            "4a0c2d05e3ca1df509614c0fe051b414d404149ec422e0998e192e51518518c4" +
            "b8acd9e3e3ff9f3b4ef931d3052755785d38e75821ceefa7da0bfe3f1fb2dc6d" +
            "738e2a2332e53ce77d44547621bb7aa724dd8805c7c795088db865d6b13d9b3e" +
            "8acec846efb072d105ab6e599f8292a7601087e0ba13af9f503dcfd426e26e4d" +
            "fb22bf5a1ff1a82d67d9bd8871e6adc17aa39d221f2865f81da9ed566192c269" +
            "3c85f0442924e603b9ae54b88dd0f21e92eedc40c08dd484c552e297894eeee8" +
            "b5acc91d5ae16f56257bb0836b48e1a8fa72e83a8b10b7026a7f466c8b08eac5" +
            "4359b70e639117cf688e263b891f004db94d77941380f3ab0559538c9398c859" +
            "b76d2bbcd6b635e753160583e7adc263097a80520d003514e134a21597c1ec57" +
            "55da3a70acc6951b4d4d81e98b9eb962d9e3bc37d5e8ebd61e2a3f61cc452a65" +
            "56571e12c190d4e3d0f8cc61ffcb60324b4a6987e7375a832ff807682e0b4595" +
            "66ef1f765638f3d2e837ed43ce2c1c7837f271c866908d865c3d9174fd4f8056" +
            "265abfb88fbc207db7a12c0a0ad035e5a728725e98cb682d41fd0bcf3aef2fd7" +
            "ab261727f310fc7cf3b34286c9e9ee235995315167191f3b4d77e5642fb57dbd" +
            "fdb5ccadefc5d03866918ab1a3eff54b405d8946e2b0c2fa444d1b2be4c3d41d" +
            "990515e7534190d66d10e38c36c5d3df0315db85ba10c924bef97d1faa07a5f8" +
            "f04998a7d38689237a1912bea3f821357d8383d7c5cfa66ba5965b5a94bb702c" +
            "e6583e59879021139355c5b90e0f9cd13b34f3357ffde404bbf34c97f9fd55b5" +
            "53e42d8a6b370eded02c8a5221e15db701da56918412520e12fd1ef9f4748647" +
            "858488d5e0abd5b9e01457768907e1d24581f9591771304192711292e4025fce" +
            "bd92adb2297e9496852f80bd78578bbdb292ca209f7584ff76e9eb66ec8a111e" +
            "add30dc7ef364c4f1339312f226fe0cfa7a5b1602417e469cf2c8e3874c51232" +
            "00f2d90dbe7f3c3ff5c6c6484052a80eb6229a6ed6176ad600da185da624bea6",
            "c1de44dd8ea245ca43e5587460feb514");
    }
}
