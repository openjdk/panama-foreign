package jdk.internal.nicl.types;

public class Array implements Type {
    final Type elementType;
    final int occurrence;

    /**
     * Array type.
     *
     * @param elementType The element type
     * @param occurrence  Number of elements. Negative number indicates incomplete array.
     */
    public Array(Type elementType, int occurrence) {
        this.elementType = elementType;
        this.occurrence = occurrence;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public long getSize() {
        if (occurrence < 0) {
            return occurrence;
        }

        return occurrence * elementType.getSize();
    }

    public int hashCode() {
        return occurrence * 31 + elementType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Array)) {
            return false;
        }
        jdk.internal.nicl.types.Array other = (jdk.internal.nicl.types.Array) o;
        return (occurrence == other.occurrence &&
                elementType.equals(other.elementType));
    }

    @Override
    public String toString() {
        return "" + occurrence + elementType.toString();
    }
}
