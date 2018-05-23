package com.acme;

import java.nicl.metadata.Offset;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeLocation;
import java.nicl.metadata.NativeStruct;
import java.nicl.metadata.NativeType;
import java.math.BigDecimal;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(path="recursive.h")
public interface recursive {

    @NativeLocation(file="recursive.h", line=1, column=8, USR="")
    @NativeStruct("[p:[p]]")
    @NativeType(layout="[p:[p]]", ctype = "struct Foo")
    public interface Foo extends Struct<Foo> {
        @NativeLocation(file="recursive.h", line=2, column=17, USR="")
        @NativeType(name="p", layout="p:[p:[p]]", ctype="struct Bar *")
        @Offset(offset=0)
        Pointer<Bar> p$get();
        void p$set(Pointer<Bar> value);
        Pointer<Pointer<Bar>> p$ptr();
    }

    @NativeLocation(file = "recursive.h", line=5, column=8, USR="")
    @NativeStruct("[p:[p]]")
    @NativeType(layout="[p:[p]]", ctype = "struct Bar")
    public interface Bar extends Struct<Bar> {
        @NativeLocation(file="recursive.h", line=6, column=17, USR="")
        @NativeType(name="q", layout="p:[p:[p]]", ctype="struct Foo *")
        @Offset(offset=0)
        Pointer<Foo> q$get();
        void q$set(Pointer<Foo> value);
        Pointer<Pointer<Foo>> q$ptr();
    }
}
