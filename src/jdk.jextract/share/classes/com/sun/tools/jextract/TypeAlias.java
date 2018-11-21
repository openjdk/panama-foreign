 /*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 */
package com.sun.tools.jextract;

/**
 * A type alias is an type annotation on a basic type, used for typedef or enum.
 */
public final class TypeAlias implements JType {
    String annotationClassDescriptor;
    JType baseType;

    private TypeAlias(String alias, JType essence) {
        baseType = essence;
        annotationClassDescriptor = alias;
    }

    public static TypeAlias of(String alias, JType essence) {
        if (essence instanceof JType2) {
            // We don't need to know extra info for intermediate definitions
            essence = ((JType2) essence).getDelegate();
            return of(alias, essence);
        }

        if (essence instanceof TypeAlias) {
            TypeAlias other = (TypeAlias) essence;
            if (alias.equals(other.annotationClassDescriptor)) {
                return other;
            }
            return of(alias, ((TypeAlias) essence).baseType);
        }

        return new TypeAlias(alias, essence);
    }

    @Override
    public String getDescriptor() {
        if (baseType instanceof JType.FnIf) {
            return "L" + annotationClassDescriptor +";";
        }
        return baseType.getDescriptor();
    }

    @Override
    public String getSignature() {
        if (baseType instanceof JType.FnIf) {
            return "L" + annotationClassDescriptor +";";
        }
        return baseType.getSignature();
    }

    /**
     * The annotation class represents the alias
     * @return The descriptor of the annotation class
     */
    public String getAnnotationDescriptor() {
        if (baseType instanceof JType.FnIf) {
            return null;
        } else {
            String rv = "L" + annotationClassDescriptor + ";";
            if (rv.equals(baseType.getDescriptor())) {
                return null;
            } else {
                return rv;
            }
        }
    }

    public JType canonicalType() {
        return baseType;
    }

    @Override
    public String toString() {
        return "TypeAlias:" + getAnnotationDescriptor() + " for " + getDescriptor();
    }
}
