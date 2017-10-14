package jdk.internal.nicl.types;

import jdk.internal.nicl.Platform;

public class Scalar implements Type {
    final char type;
    final Endianness endianness;
    final long size;

    private final static String noEndianness = "BVcxv";

    public Scalar(char type) {
        this(type, Endianness.NATIVE);
    }

    public Scalar(char type, Endianness endianness) {
        this.type = type;
        this.endianness = (noEndianness.indexOf(type) == -1) ?
                Endianness.NATIVE : endianness;
        this.size = Platform.getInstance().getABI().definedSize(type);
    }

    public Scalar(char type, Endianness endianness, int bits) {
        this.type = type;
        this.size = ((bits & 7) != 0) ? (bits >> 3) + 1 : bits >> 3;
        this.endianness = (noEndianness.indexOf(type) == -1) ?
                Endianness.NATIVE : endianness;
    }

    public Endianness getEndianness() {
        return endianness;
    }

    public char typeCode() {
        return type;
    }

    public boolean isSigned() {
        return Character.isLowerCase(type);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return (type & 0xFF) | ((int) size << 8) | (endianness.ordinal() << 16);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Scalar)) {
            return false;
        }
        jdk.internal.nicl.types.Scalar other = (jdk.internal.nicl.types.Scalar) o;
        if (type != other.type) {
            return false;
        }
        if (endianness != other.endianness) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (Endianness.NATIVE != endianness) {
            sb.append(endianness.modifier);
        }
        if (type == 'i' || type == 'f' || type == 'v' || type == 'I' || type == 'F') {
            sb.append('=');
            sb.append(size << 3);
        }
        sb.append(type);
        return sb.toString();
    }
}
