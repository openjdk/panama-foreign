package org.openjdk.bench.java.lang.foreign.pointers;

public abstract class Struct<X extends Struct<X>> {
    protected final Pointer<X> ptr;

    public Struct(Pointer<X> ptr) {
        this.ptr = ptr;
    }
}
