/*
 *  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

package jdk.internal.jextract.clang;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public enum PrintingPolicyProperty {
    Indentation(0),
    SuppressSpecifiers(1),
    SuppressTagKeyword(2),
    IncludeTagDefinition(3),
    SuppressScope(4),
    SuppressUnwrittenScope(5),
    SuppressInitializers(6),
    ConstantArraySizeAsWritten(7),
    AnonymousTagLocations(8),
    SuppressStrongLifetime(9),
    SuppressLifetimeQualifiers(10),
    SuppressTemplateArgsInCXXConstructors(11),
    Bool(12),
    Restrict(13),
    Alignof(14),
    UnderscoreAlignof(15),
    UseVoidForZeroParams(16),
    TerseOutput(17),
    PolishForDeclaration(18),
    Half(19),
    MSWChar(20),
    IncludeNewlines(21),
    MSVCFormatting(22),
    ConstantsAsWritten(23),
    SuppressImplicitBase(24),
    FullyQualifiedName(25),
    LastProperty(25);

    private final int value;

    PrintingPolicyProperty(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    private final static Map<Integer, PrintingPolicyProperty> lookup;

    static {
        lookup = new HashMap<>();
        for (PrintingPolicyProperty e: PrintingPolicyProperty.values()) {
            lookup.put(e.value(), e);
        }
    }

    public final static PrintingPolicyProperty valueOf(int value) {
        PrintingPolicyProperty x = lookup.get(value);
        if (null == x) {
            throw new NoSuchElementException("Invalid PrintingPolicyProperty value: " + value);
        }
        return x;
    }
}
