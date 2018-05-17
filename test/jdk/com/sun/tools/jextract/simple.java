package com.acme;

import java.nicl.metadata.Offset;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeType;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.C;
import java.math.BigDecimal;
import java.nicl.types.Pointer;
import java.nicl.types.Struct;

/**
 * This test is platform dependent, as the C type size may vary on platform.
 * Current value is based on x64 with __LP64__.
 */
@NativeHeader(headerPath="simple.h")
public interface simple {
    @C(file="simple.h", line=1, column=5, USR="")
    @NativeType(name="global", layout="i", ctype="int", size=4)
    public int global$get();
    public void global$set(int arg);
    public Pointer<Integer> global$ptr();

    @C(file="simple.h", line=7, column=8, USR="")
    @NativeType(isRecordType=true, layout="[cosilqFDE]", ctype = "struct anonymous", size = 64)
    public static interface anonymous extends Struct<anonymous> {
        @C(file="simple.h", line=8, column=10, USR="")
        @NativeType(name="ch", layout="c", ctype="char", size=1)
        @Offset(offset=0)
        public byte ch$get();
        public void ch$set(byte arg);
        public Pointer<Byte> ch$ptr();

        @C(file="simple.h", line=9, column=17, USR="")
        @NativeType(name="sch", layout="o", ctype="signed char", size=1)
        @Offset(offset=8)
        public byte sch$get();
        public void sch$set(byte arg);
        public Pointer<Byte> sch$ptr();

        @C(file="simple.h", line=10, column=11, USR="")
        @NativeType(name="s", layout="s", ctype="short", size=2)
        @Offset(offset=16)
        public short s$get();
        public void s$set(short arg);
        public Pointer<Short> s$ptr();

        @C(file="simple.h", line=11, column=9, USR="")
        @NativeType(name="n", layout="i", ctype="int", size=4)
        @Offset(offset=32)
        public int n$get();
        public void n$set(int arg);
        public Pointer<Integer> n$ptr();

        @C(file="simple.h", line=12, column=10, USR="")
        @NativeType(name="l", layout="l", ctype="long", size=8)
        @Offset(offset=64)
        public long l$get();
        public void l$set(long arg);
        public Pointer<Long> l$ptr();

        @C(file="simple.h", line=13, column=15, USR="")
        @NativeType(name="ll", layout="q", ctype="long long", size=8)
        @Offset(offset=128)
        public long ll$get();
        public void ll$set(long arg);
        public Pointer<Long> ll$ptr();

        @C(file="simple.h", line=14, column=11, USR="")
        @NativeType(name="f", layout="F", ctype="float", size=4)
        @Offset(offset=192)
        public float f$get();
        public void f$set(float arg);
        public Pointer<Float> f$ptr();

        @C(file="simple.h", line=15, column=12, USR="")
        @NativeType(name="d", layout="D", ctype="double", size=8)
        @Offset(offset=256)
        public double d$get();
        public void d$set(double arg);
        public Pointer<Double> d$ptr();

        @C(file="simple.h", line=16, column=17, USR="")
        @NativeType(name="ld", layout="E", ctype="long double", size=16)
        @Offset(offset=384)
        public BigDecimal ld$get();
        public void ld$set(BigDecimal arg);
        public Pointer<BigDecimal> ld$ptr();
    }

    @C(file="simple.h", line=17, column=3, USR="")
    @NativeType(name="basics", layout="[cosilqFDE]", ctype="struct anonymous", size=64)
    public anonymous basics$get();
    public void basics$set(anonymous arg);
    public Pointer<anonymous> basics$ptr();

    @C(file = "simple.h", line = 20, column = 8, USR = "c:@S@_unsigned")
    @NativeType(isRecordType=true, layout="[BOSILQ]", ctype = "struct _unsigned", size = 24)
    public static interface _unsigned extends Struct<_unsigned> {
        @C(file="simple.h", line=21, column=11, USR="")
        @NativeType(name="b", layout="B", ctype = "_Bool", size = 1)
        @Offset(offset = 0)
        public boolean b$get();
        public void b$set(boolean arg);
        public Pointer<Boolean> b$ptr();

        @C(file="simple.h", line=22, column=19, USR="")
        @NativeType(name="ch", layout="O", ctype = "unsigned char", size = 1)
        @Offset(offset = 8)
        public byte ch$get();
        public void ch$set(byte c);
        public Pointer<Byte> ch$ptr();

        @C(file="simple.h", line=23, column=20, USR="")
        @NativeType(name="s", layout="S", ctype = "unsigned short", size = 2)
        @Offset(offset = 16)
        public short s$get();
        public void s$set(short s);
        public Pointer<Short> s$ptr();

        @C(file="simple.h", line=24, column=18, USR="")
        @NativeType(name="n", layout="I", ctype = "unsigned int", size = 4)
        @Offset(offset = 32)
        public int n$get();
        public void n$set(int i);
        public Pointer<Integer> n$ptr();

        @C(file="simple.h", line=25, column=19, USR="")
        @NativeType(name="l", layout="L", ctype = "unsigned long", size = 8)
        @Offset(offset = 64)
        public long l$get();
        public void l$set(long l);
        public Pointer<Long> l$ptr();

        @C(file="simple.h", line=26, column=24, USR="")
        @NativeType(name="ll", layout="Q", ctype = "unsigned long long", size = 8)
        @Offset(offset = 128)
        public long ll$get();
        public void ll$set(long l);
        public Pointer<Long> ll$ptr();
    }

    @C(file="simple.h", line=27, column=4, USR="")
    @NativeType(name="unsigned_int", layout="p:[BOSILQ]", ctype="struct _unsigned *", size=8)
    public Pointer<_unsigned> unsigned_int$get();
    public void unsigned_int$set(Pointer<_unsigned> arg);
    public Pointer<Pointer<_unsigned>> unsigned_int$ptr();

    @C(file = "simple.h", line = 29, column = 6, USR = "c:@F@func")
    @NativeType(name="func", layout="([cosilqFDE]p:c)V", ctype = "void (struct anonymous, char *)", size = 1)
    @CallingConvention(1)
    public void func(anonymous s, Pointer<Byte> str);
}
