#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int rec(int depth) {
    char c[1024]; // consume more stack
    if (depth == 0) {
        return 0;
    } else {
        return rec(depth-1);
    }
}

/* ================================================================================== */

int id_int(int i) {
    return i;
}

unsigned int id_uint(unsigned int ui) {
    return ui;
}

signed char id_schar(signed char sc) {
    return sc;
}

unsigned char id_uchar(unsigned char uc) {
    return uc;
}

short id_short(short s) {
    return s;
}

unsigned short id_ushort(unsigned short us) {
    return us;
}

long long id_longlong(long long ll) {
    return ll;
}

unsigned long long id_ulonglong(unsigned long long ull) {
    return ull;
}

/* ================================================================================== */

jboolean testSig_Z_Z(jboolean b) {
    return b;
}

jbyte testSig_B_B(jbyte b) {
    return b;
}

jchar testSig_C_C(jchar c) {
    return c;
}

jshort testSig_S_S(jshort s) {
    return s;
}

jint testSig_I_I(jint i) {
    return i;
}

jlong testSig_J_J(jlong l) {
    return l;
}

jfloat testSig_F_F(jfloat f) {
    return f;
}

jdouble testSig_D_D(jdouble d) {
    return d;
}

/* ================================================================================== */

jint testSig_I6_I(jint i0, jint i1, jint i2, jint i3, jint i4, jint i5) {
    if (i0 != 0) return -i0;
    if (i1 != 1) return -i1;
    if (i2 != 2) return -i2;
    if (i3 != 3) return -i3;
    if (i4 != 4) return -i4;
    if (i5 != 5) return -i5;
    return i5;
}

jlong testSig_J6_J(jlong l0, jlong l1, jlong l2, jlong l3, jlong l4, jlong l5) {
    if (l0 != 0) return -l0;
    if (l1 != 1) return -l1;
    if (l2 != 2) return -l2;
    if (l3 != 3) return -l3;
    if (l4 != 4) return -l4;
    if (l5 != 5) return -l5;
    return l5;
}

jfloat testSig_F6_F(jfloat f0, jfloat f1, jfloat f2, jfloat f3, jfloat f4, jfloat f5) {
    if (f0 != 0.0) return -f0;
    if (f1 != 1.0) return -f1;
    if (f2 != 2.0) return -f2;
    if (f3 != 3.0) return -f3;
    if (f4 != 4.0) return -f4;
    if (f5 != 5.0) return -f5;
    return f5;
}

jdouble testSig_D6_D(jdouble d0, jdouble d1, jdouble d2, jdouble d3, jdouble d4, jdouble d5) {
    if (d0 != 0.0) return -d0;
    if (d1 != 1.0) return -d1;
    if (d2 != 2.0) return -d2;
    if (d3 != 3.0) return -d3;
    if (d4 != 4.0) return -d4;
    if (d5 != 5.0) return -d5;
    return d5;
}

/* ================================================================================== */

jint testSig_I10_I(jint i0, jint i1, jint i2, jint i3, jint i4,
                   jint i5, jint i6, jint i7, jint i8, jint i9) {
    return i9;
}

jlong testSig_J10_J(jlong l0, jlong l1, jlong l2, jlong l3, jlong l4,
                    jlong l5, jlong l6, jlong l7, jlong l8, jlong l9) {
    return l9;
}

jfloat testSig_F10_F(jfloat f0, jfloat f1, jfloat f2, jfloat f3, jfloat f4,
                     jfloat f5, jfloat f6, jfloat f7, jfloat f8, jfloat f9) {
    return f9;
}

jdouble testSig_D10_D(jdouble d0, jdouble d1, jdouble d2, jdouble d3, jdouble d4,
                      jdouble d5, jdouble d6, jdouble d7, jdouble d8, jdouble d9) {
    return d9;
}

/* ================================================================================== */

jint testSig_IJFDIJFDIJFD_I(jint i0, jlong l1, jfloat f2, jdouble d3,
                            jint i4, jlong l5, jfloat f6, jdouble d7,
                            jint i8, jlong l9, jfloat f10, jdouble d11) {
    return i8;
}

jlong testSig_JFDIJFDIJFDI_J(jlong l0, jfloat f1, jdouble d2, jint i3,
                             jlong l4, jfloat f5, jdouble d6, jint i7,
                             jlong l8, jfloat f9, jdouble d10, jint i11) {
    return l8;
}

jfloat testSig_FDIJFDIJFDIJ_F(jfloat f0, jdouble d1, jint i2, jlong l3,
                              jfloat f4, jdouble d5, jint i6, jlong l7,
                              jfloat f8, jdouble d9, jint i10, jlong l11) {
    return f8;
}

jdouble testSig_DIJFDIJFDIJF_D(jdouble d0, jint i1, jlong l2, jfloat f3,
                               jdouble d4, jint i5, jlong l6, jfloat f7,
                               jdouble d8, jint i9, jlong l10, jfloat f11) {
    return d8;
}
