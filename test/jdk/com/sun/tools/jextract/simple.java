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
@NativeHeader(path="simple.h")
public interface simple {
    @NativeLocation(file="simple.h", line=1, column=5, USR="")
    @NativeType(name="global", layout="i", ctype="int")
    public int global$get();
    public void global$set(int arg);
    public Pointer<Integer> global$ptr();

    @NativeLocation(file="simple.h", line=7, column=8, USR="")
    @NativeStruct("[cosilqFDE]")
    @NativeType(layout="[cosilqFDE]", ctype = "struct anonymous")
    public static interface anonymous extends Struct<anonymous> {
        @NativeLocation(file="simple.h", line=8, column=10, USR="")
        @NativeType(name="ch", layout="c", ctype="char")
        @Offset(offset=0)
        public byte ch$get();
        public void ch$set(byte arg);
        public Pointer<Byte> ch$ptr();

        @NativeLocation(file="simple.h", line=9, column=17, USR="")
        @NativeType(name="sch", layout="o", ctype="signed char")
        @Offset(offset=8)
        public byte sch$get();
        public void sch$set(byte arg);
        public Pointer<Byte> sch$ptr();

        @NativeLocation(file="simple.h", line=10, column=11, USR="")
        @NativeType(name="s", layout="s", ctype="short")
        @Offset(offset=16)
        public short s$get();
        public void s$set(short arg);
        public Pointer<Short> s$ptr();

        @NativeLocation(file="simple.h", line=11, column=9, USR="")
        @NativeType(name="n", layout="i", ctype="int")
        @Offset(offset=32)
        public int n$get();
        public void n$set(int arg);
        public Pointer<Integer> n$ptr();

        @NativeLocation(file="simple.h", line=12, column=10, USR="")
        @NativeType(name="l", layout="l", ctype="long")
        @Offset(offset=64)
        public long l$get();
        public void l$set(long arg);
        public Pointer<Long> l$ptr();

        @NativeLocation(file="simple.h", line=13, column=15, USR="")
        @NativeType(name="ll", layout="q", ctype="long long")
        @Offset(offset=128)
        public long ll$get();
        public void ll$set(long arg);
        public Pointer<Long> ll$ptr();

        @NativeLocation(file="simple.h", line=14, column=11, USR="")
        @NativeType(name="f", layout="F", ctype="float")
        @Offset(offset=192)
        public float f$get();
        public void f$set(float arg);
        public Pointer<Float> f$ptr();

        @NativeLocation(file="simple.h", line=15, column=12, USR="")
        @NativeType(name="d", layout="D", ctype="double")
        @Offset(offset=256)
        public double d$get();
        public void d$set(double arg);
        public Pointer<Double> d$ptr();

        @NativeLocation(file="simple.h", line=16, column=17, USR="")
        @NativeType(name="ld", layout="E", ctype="long double")
        @Offset(offset=384)
        public BigDecimal ld$get();
        public void ld$set(BigDecimal arg);
        public Pointer<BigDecimal> ld$ptr();
    }

    @NativeLocation(file="simple.h", line=17, column=3, USR="")
    @NativeType(name="basics", layout="[cosilqFDE]", ctype="struct anonymous")
    public anonymous basics$get();
    public void basics$set(anonymous arg);
    public Pointer<anonymous> basics$ptr();

    @NativeLocation(file = "simple.h", line = 20, column = 8, USR = "c:@S@_unsigned")
    @NativeStruct("[BOSILQ]")
    @NativeType(layout="[BOSILQ]", ctype = "struct _unsigned")
    public static interface _unsigned extends Struct<_unsigned> {
        @NativeLocation(file="simple.h", line=21, column=11, USR="")
        @NativeType(name="b", layout="B", ctype = "_Bool")
        @Offset(offset = 0)
        public boolean b$get();
        public void b$set(boolean arg);
        public Pointer<Boolean> b$ptr();

        @NativeLocation(file="simple.h", line=22, column=19, USR="")
        @NativeType(name="ch", layout="O", ctype = "unsigned char")
        @Offset(offset = 8)
        public byte ch$get();
        public void ch$set(byte c);
        public Pointer<Byte> ch$ptr();

        @NativeLocation(file="simple.h", line=23, column=20, USR="")
        @NativeType(name="s", layout="S", ctype = "unsigned short")
        @Offset(offset = 16)
        public short s$get();
        public void s$set(short s);
        public Pointer<Short> s$ptr();

        @NativeLocation(file="simple.h", line=24, column=18, USR="")
        @NativeType(name="n", layout="I", ctype = "unsigned int")
        @Offset(offset = 32)
        public int n$get();
        public void n$set(int i);
        public Pointer<Integer> n$ptr();

        @NativeLocation(file="simple.h", line=25, column=19, USR="")
        @NativeType(name="l", layout="L", ctype = "unsigned long")
        @Offset(offset = 64)
        public long l$get();
        public void l$set(long l);
        public Pointer<Long> l$ptr();

        @NativeLocation(file="simple.h", line=26, column=24, USR="")
        @NativeType(name="ll", layout="Q", ctype = "unsigned long long")
        @Offset(offset = 128)
        public long ll$get();
        public void ll$set(long l);
        public Pointer<Long> ll$ptr();
    }

    @NativeLocation(file="simple.h", line=27, column=4, USR="")
    @NativeType(name="unsigned_int", layout="p:[BOSILQ]", ctype="struct _unsigned *")
    public Pointer<_unsigned> unsigned_int$get();
    public void unsigned_int$set(Pointer<_unsigned> arg);
    public Pointer<Pointer<_unsigned>> unsigned_int$ptr();

    @NativeLocation(file = "simple.h", line = 29, column = 6, USR = "c:@F@func")
    @NativeType(name="func", layout="([cosilqFDE]p:c)V", ctype = "void (struct anonymous, char *)")
    public void func(anonymous s, Pointer<Byte> str);
}
