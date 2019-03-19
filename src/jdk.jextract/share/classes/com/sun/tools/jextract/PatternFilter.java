/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PatternFilter {
    private static final PatternFilter EMPTY = builder().build();

    private final List<Predicate<String>> includes;
    private final List<Predicate<String>> excludes;

    private PatternFilter(List<String> includes, List<String> excludes) {
        this.includes = asPredicates(includes);
        this.excludes = asPredicates(excludes);
    }

    public static PatternFilter empty() {
        return EMPTY;
    }

    private List<Predicate<String>> asPredicates(List<String> patterns) {
        return patterns.stream()
            .map(Pattern::compile)
            .map(Pattern::asMatchPredicate)
            .collect(Collectors.toList());
    }

    protected boolean hasIncludes() {
        return !includes.isEmpty();
    }

    protected boolean isIncluded(String name) {
        return includes.stream().anyMatch(p -> p.test(name));
    }

    protected boolean isExcluded(String name) {
        return excludes.stream().anyMatch(p -> p.test(name));
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean filter(String name) {
        return (!hasIncludes() || isIncluded(name)) && !isExcluded(name);
    }

    public static class Builder {
        private final List<String> includes = new ArrayList<>();
        private final List<String> excludes = new ArrayList<>();
        public PatternFilter build() {
            return new PatternFilter(includes, excludes);
        }

        public void addInclude(String pattern) {
            includes.add(pattern);
        }

        public void addExclude(String pattern) {
            excludes.add(pattern);
        }
    }

}
