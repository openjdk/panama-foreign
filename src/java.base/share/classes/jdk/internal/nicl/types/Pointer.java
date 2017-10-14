package jdk.internal.nicl.types;

import jdk.internal.nicl.Platform;

public class Pointer implements Type {
    Type pointee;

    public Pointer(Type pointee) {
        this.pointee = pointee;
    }

    public Type getPointeeType() {
        return pointee;
    }

    @Override
    public long getSize() {
        return Platform.getInstance().getABI().definedSize('p');
    }

    @Override
    public int hashCode() {
        return 0x80000000 | pointee.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Pointer)) {
            return false;
        }
        jdk.internal.nicl.types.Pointer other = (jdk.internal.nicl.types.Pointer) o;

        if (pointee == null) {
            return other.pointee == null;
        }

        return pointee.equals(other.pointee);
    }

    @Override
    public String toString() {
        return (pointee == null) ? "p" : "p:" + pointee.toString();
    }
}
