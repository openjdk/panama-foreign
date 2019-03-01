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
public class ChaChaBench {

    @Param({"16384", "65536"})
    private int dataSize;
    
    private ChaChaVector cc20_S128 = makeCC20(Vector.Shape.S_128_BIT);
    private ChaChaVector cc20_S256 = makeCC20(Vector.Shape.S_256_BIT);
    private ChaChaVector cc20_S512 = makeCC20(Vector.Shape.S_512_BIT);
 
    private byte[] in;
    private byte[] out;
    
    private byte[] key = new byte[32];
    private byte[] nonce = new byte[12];
    private long counter = 0;

    private static ChaChaVector makeCC20(Vector.Shape shape) {
        ChaChaVector cc20 = new ChaChaVector(shape);
        runKAT(cc20);
        return cc20;
    }

    @Setup
    public void setup() {
        
        in = new byte[dataSize];
        out = new byte[dataSize];
    }

    @Benchmark
    public void encrypt128() {
        cc20_S128.chacha20(key, nonce, counter, in, out);
    }

    @Benchmark
    public void encrypt256() {
        cc20_S256.chacha20(key, nonce, counter, in, out);
    }

    @Benchmark
    public void encrypt512() {
        cc20_S512.chacha20(key, nonce, counter, in, out);
    }

    private static class ChaChaVector {

        private static final int[] STATE_CONSTANTS =
            new int[]{0x61707865, 0x3320646e, 0x79622d32, 0x6b206574};

        private final IntVector.IntSpecies intSpecies;
        private final int numBlocks;

        private final Vector.Shuffle<Integer> rot1;
        private final Vector.Shuffle<Integer> rot2;
        private final Vector.Shuffle<Integer> rot3;

        private final IntVector counterAdd;

        private final Vector.Shuffle<Integer> shuf0;
        private final Vector.Shuffle<Integer> shuf1;
        private final Vector.Shuffle<Integer> shuf2;
        private final Vector.Shuffle<Integer> shuf3;

        private final Vector.Mask<Integer> mask0;
        private final Vector.Mask<Integer> mask1;
        private final Vector.Mask<Integer> mask2;
        private final Vector.Mask<Integer> mask3;

        private final int[] state;

        public ChaChaVector(Vector.Shape shape) {
            this.intSpecies = IntVector.species(shape);
            this.numBlocks = intSpecies.length() / 4;

            this.rot1 = makeRotate(1);
            this.rot2 = makeRotate(2);
            this.rot3 = makeRotate(3);

            this.counterAdd = makeCounterAdd();

            this.shuf0 = makeRearrangeShuffle(0);
            this.shuf1 = makeRearrangeShuffle(1);
            this.shuf2 = makeRearrangeShuffle(2);
            this.shuf3 = makeRearrangeShuffle(3);

            this.mask0 = makeRearrangeMask(0);
            this.mask1 = makeRearrangeMask(1);
            this.mask2 = makeRearrangeMask(2);
            this.mask3 = makeRearrangeMask(3);

            this.state = new int[numBlocks * 16];
        }

        private Vector.Shuffle<Integer>  makeRotate(int amount) {
            int[] shuffleArr = new int[intSpecies.length()];

            for (int i = 0; i < intSpecies.length(); i ++) {
                int offset = (i / 4) * 4;
                shuffleArr[i] = offset + ((i + amount) % 4);
            }

            return IntVector.shuffleFromValues(intSpecies, shuffleArr);
        }

        private IntVector makeCounterAdd() {
            int[] addArr = new int[intSpecies.length()];
            for(int i = 0; i < numBlocks; i++) {
                addArr[4 * i] = numBlocks;
            }
            return IntVector.fromArray(intSpecies, addArr, 0);
        }

        private Vector.Shuffle<Integer>  makeRearrangeShuffle(int order) {
            int[] shuffleArr = new int[intSpecies.length()];
            int start = order * 4;
            for (int i = 0; i < shuffleArr.length; i++) {
                shuffleArr[i] = (i % 4) + start;
            }
            return IntVector.shuffleFromArray(intSpecies, shuffleArr, 0);
        }

        private Vector.Mask<Integer> makeRearrangeMask(int order) {
            boolean[] maskArr = new boolean[intSpecies.length()];
            int start = order * 4;
            if (start < maskArr.length) {
                for (int i = 0; i < 4; i++) {
                    maskArr[i + start] = true;
                }
            }

            return IntVector.maskFromValues(intSpecies, maskArr);
        }

        public void makeState(byte[] key, byte[] nonce, long counter,
            int[] out) {

            // first field is constants
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < numBlocks; j++) {
                    out[4*j + i] = STATE_CONSTANTS[i];
                }
            }

            // second field is first part of key
            int fieldStart = 4 * numBlocks;
            for (int i = 0; i < 4; i++) {
                int keyInt = 0;
                for (int j = 0; j < 4; j++) {
                    keyInt += (0xFF & key[4 * i + j]) << 8 * j;
                }
                for (int j = 0; j < numBlocks; j++) {
                    out[fieldStart + j*4 + i] = keyInt;
                }
            }

            // third field is second part of key
            fieldStart = 8 * numBlocks;
            for (int i = 0; i < 4; i++) {
                int keyInt = 0;
                for (int j = 0; j < 4; j++) {
                    keyInt += (0xFF & key[4 * (i + 4) + j]) << 8 * j;
                }

                for (int j = 0; j < numBlocks; j++) {
                    out[fieldStart + j*4 + i] = keyInt;
                }
            }

            // fourth field is counter and nonce
            fieldStart = 12 * numBlocks;
            for (int j = 0; j < numBlocks; j++) {
                out[fieldStart + j*4] = (int) (counter + j);
            }

            for (int i = 0; i < 3; i++) {
                int nonceInt = 0;
                for (int j = 0; j < 4; j++) {
                    nonceInt += (0xFF & nonce[4 * i + j]) << 8 * j;
                }

                for (int j = 0; j < numBlocks; j++) {
                    out[fieldStart + j*4 + 1 + i] = nonceInt;
                }
            }
        }

        public void chacha20(byte[] key, byte[] nonce, long counter,
            byte[] in, byte[] out) {

            makeState(key, nonce, counter, state);

            int len = intSpecies.length();

            IntVector sa = IntVector.fromArray(intSpecies, state, 0);
            IntVector sb = IntVector.fromArray(intSpecies, state, len);
            IntVector sc = IntVector.fromArray(intSpecies, state, 2 * len);
            IntVector sd = IntVector.fromArray(intSpecies, state, 3 * len);

            int stateLenBytes = state.length * 4;
            int numStates = (in.length + stateLenBytes - 1) / stateLenBytes;
            for (int j = 0; j < numStates; j++){

                IntVector a = sa;
                IntVector b = sb;
                IntVector c = sc;
                IntVector d = sd;

                for (int i = 0; i < 10; i++) {
                    // first round
                    a = a.add(b);
                    d = d.xor(a);
                    d = d.rotateL(16);

                    c = c.add(d);
                    b = b.xor(c);
                    b = b.rotateL(12);

                    a = a.add(b);
                    d = d.xor(a);
                    d = d.rotateL(8);

                    c = c.add(d);
                    b = b.xor(c);
                    b = b.rotateL(7);

                    // makeRotate
                    b = b.rearrange(rot1);
                    c = c.rearrange(rot2);
                    d = d.rearrange(rot3);

                    // second round
                    a = a.add(b);
                    d = d.xor(a);
                    d = d.rotateL(16);

                    c = c.add(d);
                    b = b.xor(c);
                    b = b.rotateL(12);

                    a = a.add(b);
                    d = d.xor(a);
                    d = d.rotateL(8);

                    c = c.add(d);
                    b = b.xor(c);
                    b = b.rotateL(7);

                    // makeRotate
                    b = b.rearrange(rot3);
                    c = c.rearrange(rot2);
                    d = d.rearrange(rot1);
                }

                a = a.add(sa);
                b = b.add(sb);
                c = c.add(sc);
                d = d.add(sd);

                // rearrange the vectors
                if (intSpecies.length() == 4) {
                    // no rearrange needed
                } else if (intSpecies.length() == 8) {
                    IntVector a_r = a.rearrange(b, shuf0, mask1);
                    IntVector b_r = c.rearrange(d, shuf0, mask1);
                    IntVector c_r = a.rearrange(b, shuf1, mask1);
                    IntVector d_r = c.rearrange(d, shuf1, mask1);

                    a = a_r;
                    b = b_r;
                    c = c_r;
                    d = d_r;
                } else if (intSpecies.length() == 16) {
                    IntVector a_r = a;
                    a_r = a_r.blend(b.rearrange(shuf0), mask1);
                    a_r = a_r.blend(c.rearrange(shuf0), mask2);
                    a_r = a_r.blend(d.rearrange(shuf0), mask3);

                    IntVector b_r = b;
                    b_r = b_r.blend(a.rearrange(shuf1), mask0);
                    b_r = b_r.blend(c.rearrange(shuf1), mask2);
                    b_r = b_r.blend(d.rearrange(shuf1), mask3);

                    IntVector c_r = c;
                    c_r = c_r.blend(a.rearrange(shuf2), mask0);
                    c_r = c_r.blend(b.rearrange(shuf2), mask1);
                    c_r = c_r.blend(d.rearrange(shuf2), mask3);

                    IntVector d_r = d;
                    d_r = d_r.blend(a.rearrange(shuf3), mask0);
                    d_r = d_r.blend(b.rearrange(shuf3), mask1);
                    d_r = d_r.blend(c.rearrange(shuf3), mask2);

                    a = a_r;
                    b = b_r;
                    c = c_r;
                    d = d_r;
                } else {
                    throw new RuntimeException("not supported");
                }

                // xor keystream with input
                int inOff = stateLenBytes * j;
                IntVector ina = intSpecies.fromByteArray(in, inOff);
                IntVector inb = intSpecies.fromByteArray(in, inOff + 4 * len);
                IntVector inc = intSpecies.fromByteArray(in, inOff + 8 * len);
                IntVector ind = intSpecies.fromByteArray(in, inOff + 12 * len);

                ina.xor(a).intoByteArray(out, inOff);
                inb.xor(b).intoByteArray(out, inOff + 4 * len);
                inc.xor(c).intoByteArray(out, inOff + 8 * len);
                ind.xor(d).intoByteArray(out, inOff + 12 * len);

                // increment counter
                sd = sd.add(counterAdd);
            }
        }

        public int implBlockSize() {
            return numBlocks * 64;
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

    private static void runKAT(ChaChaVector cc20, String keyStr,
        String nonceStr, long counter, String inStr, String outStr) {

        byte[] key = hexStringToByteArray(keyStr);
        byte[] nonce = hexStringToByteArray(nonceStr);
        byte[] in = hexStringToByteArray(inStr);
        byte[] expOut = hexStringToByteArray(outStr);

        // implementation only works at multiples of some size
        int blockSize = cc20.implBlockSize();

        int length = blockSize * ((in.length + blockSize - 1) / blockSize);
        in = Arrays.copyOf(in, length);
        byte[] out = new byte[length];

        cc20.chacha20(key, nonce, counter, in, out);

        byte[] actOut = new byte[expOut.length];
        System.arraycopy(out, 0, actOut, 0, expOut.length);

        if (!Arrays.equals(out, 0, expOut.length, expOut, 0, expOut.length)) {
            throw new RuntimeException("Incorrect result");
        }
    }

    /*
     * ChaCha20 Known Answer Tests to ensure that the implementation is correct.
     */
    private static void runKAT(ChaChaVector cc20) {
        runKAT(cc20,
        "0000000000000000000000000000000000000000000000000000000000000001",
        "000000000000000000000002",
        1,
        "416e79207375626d697373696f6e20746f20746865204945544620696e74656e" +
        "6465642062792074686520436f6e7472696275746f7220666f72207075626c69" +
        "636174696f6e20617320616c6c206f722070617274206f6620616e2049455446" +
        "20496e7465726e65742d4472616674206f722052464320616e6420616e792073" +
        "746174656d656e74206d6164652077697468696e2074686520636f6e74657874" +
        "206f6620616e204945544620616374697669747920697320636f6e7369646572" +
        "656420616e20224945544620436f6e747269627574696f6e222e205375636820" +
        "73746174656d656e747320696e636c756465206f72616c2073746174656d656e" +
        "747320696e20494554462073657373696f6e732c2061732077656c6c20617320" +
        "7772697474656e20616e6420656c656374726f6e696320636f6d6d756e696361" +
        "74696f6e73206d61646520617420616e792074696d65206f7220706c6163652c" +
        "207768696368206172652061646472657373656420746f",
        "a3fbf07df3fa2fde4f376ca23e82737041605d9f4f4f57bd8cff2c1d4b7955ec" +
        "2a97948bd3722915c8f3d337f7d370050e9e96d647b7c39f56e031ca5eb6250d" +
        "4042e02785ececfa4b4bb5e8ead0440e20b6e8db09d881a7c6132f420e527950" +
        "42bdfa7773d8a9051447b3291ce1411c680465552aa6c405b7764d5e87bea85a" +
        "d00f8449ed8f72d0d662ab052691ca66424bc86d2df80ea41f43abf937d3259d" +
        "c4b2d0dfb48a6c9139ddd7f76966e928e635553ba76c5c879d7b35d49eb2e62b" +
        "0871cdac638939e25e8a1e0ef9d5280fa8ca328b351c3c765989cbcf3daa8b6c" +
        "cc3aaf9f3979c92b3720fc88dc95ed84a1be059c6499b9fda236e7e818b04b0b" +
        "c39c1e876b193bfe5569753f88128cc08aaa9b63d1a16f80ef2554d7189c411f" +
        "5869ca52c5b83fa36ff216b9c1d30062bebcfd2dc5bce0911934fda79a86f6e6" +
        "98ced759c3ff9b6477338f3da4f9cd8514ea9982ccafb341b2384dd902f3d1ab" +
        "7ac61dd29c6f21ba5b862f3730e37cfdc4fd806c22f221"
        );
    }
}
