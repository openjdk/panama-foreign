package com.acme;

import java.nicl.metadata.Offset;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeType;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.C;
import java.math.BigDecimal;
import java.nicl.types.Pointer;
import java.nicl.types.Reference;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(headerPath="recursive.h")
public interface recursive {

    @C(file="recursive.h", line=1, column=8, USR="")
    @NativeType(isRecordType=true, layout="[p:[p]]", ctype = "struct Foo", size = 8)
    public interface Foo extends Reference<Foo> {
        @C(file="recursive.h", line=2, column=17, USR="")
        @NativeType(name="p", layout="p:[p:[p]]", ctype="struct Bar *", size=8)
        @Offset(offset=0)
        Pointer<Bar> p$get();
        void p$set(Pointer<Bar> value);
        Reference<Pointer<Bar>> p$ref();
    }

    @C(file = "recursive.h", line=5, column=8, USR="")
    @NativeType(isRecordType=true, layout="[p:[p]]", ctype = "struct Bar", size = 8)
    public interface Bar extends Reference<Bar> {
        @C(file="recursive.h", line=6, column=17, USR="")
        @NativeType(name="q", layout="p:[p:[p]]", ctype="struct Foo *", size=8)
        @Offset(offset=0)
        Pointer<Foo> q$get();
        void q$set(Pointer<Foo> value);
        Reference<Pointer<Foo>> q$ref();
    }
}
