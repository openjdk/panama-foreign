/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

final public class Shapes {

    // @@@ Move into Vector.Shape

    public static final S64Bit S_64_BIT = new S64Bit();

    public static final class S64Bit extends Vector.Shape {
        @Override
        public int bitSize() {
            return 64;
        }
    }

    public static final S128Bit S_128_BIT = new S128Bit();

    public static final class S128Bit extends Vector.Shape {
        @Override
        public int bitSize() {
            return 128;
        }
    }

    public static final S256Bit S_256_BIT = new S256Bit();

    public static final class S256Bit extends Vector.Shape {
        @Override
        public int bitSize() {
            return 256;
        }
    }

    public static final S512Bit S_512_BIT = new S512Bit();

    public static final class S512Bit extends Vector.Shape {
        @Override
        public int bitSize() {
            return 512;
        }
    }

}
