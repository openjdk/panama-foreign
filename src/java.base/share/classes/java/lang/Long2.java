package java.lang;

import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;

// 128-bit vector.
public final class Long2 {
    private static native void registerNatives();
    static {
        registerNatives();
    }

    public static final int SIZE = Long.SIZE << 1;

    public static final int BYTES = SIZE / Byte.SIZE;

    public static final Long2 ZERO = make(0L, 0L);

    private final long l1, l2; // FIXME

    private Long2() {
        throw new UnsupportedOperationException();
    }

    @HotSpotIntrinsicCandidate
    public static native Long2 make(long lo, long hi);

    @HotSpotIntrinsicCandidate
    public native long extract(int i);

    @HotSpotIntrinsicCandidate
    public boolean equals(Long2 v) {
        for (int i = 0; i < 2; i++) {
            if (extract(i) != v.extract(i)) {
                return false;
            }
        }
        return true;
    }

    @HotSpotIntrinsicCandidate
    public static Long2 make() { return make(0L, 0L); }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Long2) {
            return equals((Long2)o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < 2; i++) {
            hash = 31*hash + Long.hashCode(extract(i));
        }
        return hash;
    }

    @Override
    public String toString() {
        long v1 = extract(0);
        long v2 = extract(1);
        return String.format("Long2 {128: 0x%08x | 0x%08x | 0x%08x | 0x%08x :0}",
                v2 >>> 32, v2 & 0xFFFFFFFFL,
                v1 >>> 32, v1 & 0xFFFFFFFFL);
    }
}
