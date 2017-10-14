package java.lang;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;

// 256-bit vector.
public final class Long4 {
    private static native void registerNatives();
    static {
        registerNatives();
    }

    public static final int SIZE = Long.SIZE << 2;

    public static final int BYTES = SIZE / Byte.SIZE;

    public static final Long4 ZERO = make(0, 0, 0, 0);

    private final long l1, l2, l3, l4; // FIXME

    private Long4() {
        throw new UnsupportedOperationException();
    }

    @HotSpotIntrinsicCandidate
    public static native Long4 make(long lo, long l2, long l3, long hi);

    @HotSpotIntrinsicCandidate
    public native long extract(int i);

    @HotSpotIntrinsicCandidate
    public boolean equals(Long4 v) {
        for (int i = 0; i < 4; i++) {
            if (extract(i) != v.extract(i)) {
                return false;
            }
        }
        return true;
    }

    @HotSpotIntrinsicCandidate
    public static Long4 make() {
        return make(0L, 0L, 0L, 0L);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Long4) {
            return equals((Long4)o);
        }
        return false;
    }

    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < 4; i++) {
            hash = 31*hash + Long.hashCode(extract(i));
        }
        return hash;
    }

    @Override
    public String toString() {
        long v1 = extract(0);
        long v2 = extract(1);
        long v3 = extract(2);
        long v4 = extract(3);
        return String.format("Long4 {256: 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x :0}",
                v4 >>> 32, v4 & 0xFFFFFFFFL,
                v3 >>> 32, v3 & 0xFFFFFFFFL,
                v2 >>> 32, v2 & 0xFFFFFFFFL,
                v1 >>> 32, v1 & 0xFFFFFFFFL);
    }
}
