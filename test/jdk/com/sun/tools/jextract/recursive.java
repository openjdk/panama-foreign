package com.acme;

import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeLocation;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(path="recursive.h")
public interface recursive {

    @NativeLocation(file="recursive.h", line=1, column=8, USR="")
    @NativeStruct("[u64(get=p$get)(set=p$set)(ptr=p$ptr):$(Bar)](Foo)")
    public interface Foo extends Struct<Foo> {
        @NativeLocation(file="recursive.h", line=2, column=17, USR="")
        Pointer<Bar> p$get();
        void p$set(Pointer<Bar> value);
        Pointer<Pointer<Bar>> p$ptr();
    }

    @NativeLocation(file = "recursive.h", line=5, column=8, USR="")
    @NativeStruct("[u64(get=q$get)(set=q$set)(ptr=q$ptr):$(Foo)](Bar)")
    public interface Bar extends Struct<Bar> {
        @NativeLocation(file="recursive.h", line=6, column=17, USR="")
        Pointer<Foo> q$get();
        void q$set(Pointer<Foo> value);
        Pointer<Pointer<Foo>> q$ptr();
    }
}
