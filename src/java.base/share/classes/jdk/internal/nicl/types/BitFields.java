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
