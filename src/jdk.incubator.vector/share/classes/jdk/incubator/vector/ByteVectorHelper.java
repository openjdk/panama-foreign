/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package jdk.incubator.vector;

/**
 * Operations on vectors that are not intrinsic candidates
 */
@SuppressWarnings("cast")
public final class ByteVectorHelper {

    private ByteVectorHelper() {}

    public interface BinaryOp {
        byte apply(int i, byte a, byte b);
    }

    public interface UnaryOp {
        byte apply(int i, byte a);
    }

    public static <S extends Vector.Shape>
    ByteVector<S> map(ByteVector<S> va, ByteVector<S> vb, BinaryOp op) {
        return va.bOp(vb, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> map(ByteVector<S> va, ByteVector<S> vb, Vector.Mask<Byte,S> m, BinaryOp op) {
        return va.bOp(vb, m, (i, a, b) -> op.apply(i, a, b));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> map(ByteVector<S> va, UnaryOp op) {
        return va.uOp((i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> map(ByteVector<S> va, Vector.Mask<Byte,S> m, UnaryOp op) {
        return va.uOp(m, (i, a) -> op.apply(i, a));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> div(ByteVector<S> va, ByteVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (byte) (a / b));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> div(ByteVector<S> va, ByteVector<S> vb, Vector.Mask<Byte, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (byte) (a / b));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> mod(ByteVector<S> va, ByteVector<S> vb) {
        return va.bOp(vb, (i, a, b) -> (byte) (a % b));
    }

    public static <S extends Vector.Shape>
    ByteVector<S> mod(ByteVector<S> va, ByteVector<S> vb, Vector.Mask<Byte, S> m) {
        return va.bOp(vb, m, (i, a, b) -> (byte) (a % b));
    }



}
