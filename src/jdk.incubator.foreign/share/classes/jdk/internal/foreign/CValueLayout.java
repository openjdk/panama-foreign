/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */
package jdk.internal.foreign;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.ValueLayout;

import java.lang.constant.Constable;
import java.lang.constant.DynamicConstantDesc;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_GET_STATIC_FINAL;

public class CValueLayout extends ValueLayout {

    public enum Kind {
        CHAR(findBSM("C_CHAR"), true),
        SHORT(findBSM("C_SHORT"), true),
        INT(findBSM("C_INT"), true),
        LONG(findBSM("C_LONG"), true),
        LONGLONG(findBSM("C_LONGLONG"), true),
        FLOAT(findBSM("C_FLOAT"), false),
        DOUBLE(findBSM("C_DOUBLE"), false),
        LONGDOUBLE(findBSM("C_LONGDOUBLE"), false),
        POINTER(findBSM("C_POINTER"), false);

        private final DynamicConstantDesc<ValueLayout> bsm;
        private final boolean isIntegral;

        Kind(DynamicConstantDesc<ValueLayout> bsm, boolean isIntegral) {
            this.bsm = bsm;
            this.isIntegral = isIntegral;
        }

        public DynamicConstantDesc<ValueLayout> bsm() {
            return bsm;
        }

        public boolean isIntergral() {
            return isIntegral;
        }

        public boolean isPointer() {
            return this == POINTER;
        }

        private static DynamicConstantDesc<ValueLayout> findBSM(String fieldName) {
            return DynamicConstantDesc.ofNamed(
                BSM_GET_STATIC_FINAL,
                fieldName,
                ValueLayout.class.describeConstable().orElseThrow(),
                CLinker.class.describeConstable().orElseThrow()
            );
        }
    }

    private final Kind kind;

    private CValueLayout(Kind kind, ByteOrder order, long size, long alignment, Map<String, Constable> attributes) {
        super(order, size, alignment, attributes);
        this.kind = kind;
    }

    static CValueLayout ofChar(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.CHAR, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofShort(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.SHORT, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofInt(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.INT, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofLong(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.LONG, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofLongLong(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.LONGLONG, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofFloat(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.FLOAT, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofDouble(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.DOUBLE, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofLongDouble(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.LONGDOUBLE, order, bitSize, bitSize, Map.of());
    }

    static CValueLayout ofPointer(ByteOrder order, long bitSize) {
        return new CValueLayout(Kind.POINTER, order, bitSize, bitSize, Map.of());
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public CValueLayout withOrder(ByteOrder order) {
        return (CValueLayout) super.withOrder(order);
    }

    @Override
    public CValueLayout withName(String name) {
        return (CValueLayout) super.withName(name);
    }

    @Override
    public CValueLayout withBitAlignment(long alignmentBits) {
        return (CValueLayout) super.withBitAlignment(alignmentBits);
    }

    @Override
    public CValueLayout withAttribute(String name, Constable value) {
        return (CValueLayout) super.withAttribute(name, value);
    }

    @Override
    protected CValueLayout dup(long alignment, Map<String, Constable> attributes) {
        return new CValueLayout(kind, order(), bitSize(), alignment, attributes);
    }

    @Override
    public Optional<DynamicConstantDesc<ValueLayout>> describeConstable() {
        return Optional.of(decorateLayoutConstant(kind.bsm()));
    }
}
