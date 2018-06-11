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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.nicl.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractLayout<L extends AbstractLayout<L>> implements Layout {
    private final Map<String, String> annotations;

    public AbstractLayout(Map<String, String> annotations) {
        this.annotations = Collections.unmodifiableMap(annotations);
    }

    @Override
    public Map<String, String> annotations() {
        return annotations;
    }

    @Override
    public L stripAnnotations() {
        return dup(NO_ANNOS);
    }

    @Override
    public L withAnnotation(String name, String value) {
        Map<String, String> newAnnotations = new LinkedHashMap<>(annotations);
        newAnnotations.put(name, value);
        return dup(newAnnotations);
    }

    abstract L dup(Map<String, String> annotations);

    String wrapWithAnnotations(String s) {
        if (!annotations.isEmpty()) {
            return String.format("%s%s",
                    s, annotations.entrySet().stream()
                            .map(e -> !e.getKey().equals(NAME) ?
                                    String.format("(%s=%s)", e.getKey(), e.getValue()) :
                                    String.format("(%s)", e.getValue()))
                            .collect(Collectors.joining()));
        } else {
            return s;
        }
    }

    static final Map<String, String> NO_ANNOS = Collections.unmodifiableMap(new HashMap<>());
}
