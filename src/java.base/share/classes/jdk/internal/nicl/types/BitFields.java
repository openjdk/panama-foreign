/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.nicl.types;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BitFields implements Type {
    final Scalar storage;
    // fields is a list of bits used
    final int[] fields;

    public BitFields(Scalar storage, int[] fields) {
        this.storage = storage;
        this.fields = fields;
    }

    public Scalar getStorage() {
        return storage;
    }

    public Stream<BitField> fields() {
        IntFunction<BitField> toBitField = new IntFunction<BitField>() {
            int used = 0;

            @Override
            public BitField apply(int v) {
                BitField rv = new BitField(storage, used, v);
                used += v;
                return rv;
            }
        };

        return IntStream.of(fields).mapToObj(toBitField);
    }

    @Override
    public long getSize() {
        return storage.getSize();
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(fields) + storage.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.BitFields)) {
            return false;
        }
        jdk.internal.nicl.types.BitFields other = (jdk.internal.nicl.types.BitFields) o;
        if (!other.storage.equals(storage)) {
            return false;
        }
        return Arrays.equals(fields, other.fields);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(storage.toString());
        sb.append(':');
        for (int count : fields) {
            sb.append(count);
            sb.append('b');
        }
        return sb.toString();
    }

    public static class BitField implements Type {
        final Scalar storage;
        final int start_bit;
        final int bits;

        BitField(Scalar storage, int start, int bits) {
            this.storage = storage;
            this.start_bit = start;
            this.bits = bits;
        }

        @Override
        public long getSize() {
            return storage.getSize();
        }

        @Override
        public int hashCode() {
            return 31 * start_bit + 13 * bits + storage.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof BitField)) {
                return false;
            }
            BitField other = (BitField) o;
            if (! other.storage.equals(storage)) {
                return false;
            }
            return start_bit == other.start_bit && bits == other.bits;
        }

        @Override
        public String toString() {
            return storage.toString() + ":" + bits + "b@" + start_bit;
        }
    }
}
