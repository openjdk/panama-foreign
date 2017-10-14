package java.lang;

import jdk.internal.HotSpotIntrinsicCandidate;

// 512-bit vector.
public final class Long8 {
    private static native void registerNatives();
    static {
        registerNatives();
    }

    public static final int SIZE = Long.SIZE << 3;

    public static final int BYTES = SIZE / Byte.SIZE;

    private final long l1, l2, l3, l4, l5, l6, l7, l8; // FIXME

    private  Long8() {
        throw new UnsupportedOperationException();
    }

    @HotSpotIntrinsicCandidate
    public static native Long8 make(long lo, long l2, long l3, long l4,
                                    long l5, long l6, long l7, long hi);

    @HotSpotIntrinsicCandidate
    public native long extract(int i);

    @HotSpotIntrinsicCandidate
    public boolean equals(Long8 v) {
        for (int i = 0; i < 8; i++) {
            if (extract(i) != v.extract(i)) {
                return false;
            }
        }
        return true;
    }

    @HotSpotIntrinsicCandidate
    public static Long8 make() {
        return make(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Long8) {
            return equals((Long8)o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < 8; i++) {
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
        long v5 = extract(4);
        long v6 = extract(5);
        long v7 = extract(6);
        long v8 = extract(7);
        return String.format("Long4 {512: 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x |" +
                                        " 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x | 0x%08x :0}",
                v8 >>> 32, v8 & 0xFFFFFFFFL,
                v7 >>> 32, v7 & 0xFFFFFFFFL,
                v6 >>> 32, v6 & 0xFFFFFFFFL,
                v5 >>> 32, v5 & 0xFFFFFFFFL,
                v4 >>> 32, v4 & 0xFFFFFFFFL,
                v3 >>> 32, v3 & 0xFFFFFFFFL,
                v2 >>> 32, v2 & 0xFFFFFFFFL,
                v1 >>> 32, v1 & 0xFFFFFFFFL);
    }
}
