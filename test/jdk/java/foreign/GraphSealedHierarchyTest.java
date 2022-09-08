/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @enablePreview
 * @run testng/othervm GraphSealedHierarchyTest
 */

import org.testng.annotations.*;

import java.lang.foreign.MemoryLayout;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static org.testng.Assert.*;

public class GraphSealedHierarchyTest {

    @Test
    public void graph() {
        Stream.of(
                        MemoryLayout.class
                )
                .map(GraphSealedHierarchyTest::graph)
                .forEach(System.out::println);
    }

    // Generates a graph in DOT format
    static String graph(Class<?> rootClass) {
        final State state = new State(rootClass);
        traverse(state, rootClass);
        return state.render();
    }

    static void traverse(State state, Class<?> node) {
        for (Class<?> subNode : permittedSubclasses(node)) {
            state.addEdge(node, subNode);
            traverse(state, subNode);
        }
    }

    private static final class State {

        private final StringBuilder builder;
        private final Map<Class<?>, List<String>> labels;
        private final Map<Class<?>, Map<String, String>> stylings;

        public State(Class<?> rootNode) {
            builder = new StringBuilder()
                    .append("digraph G {")
                    .append(lineSeparator())
                    .append("  labelloc=\"b\";")
                    .append(lineSeparator())
                    .append("  label=\"The Sealed Hierarchy of ")
                    .append(rootNode.getName())
                    .append("\";")
                    .append(lineSeparator())
                    .append("  rankdir=\"BT\";")
                    .append(lineSeparator());
            labels = new HashMap<>();
            stylings = new HashMap<>();
        }

        public void addEdge(Class<?> node, Class<?> subNode) {
            builder.append("  ")
                    .append(subNode.getSimpleName())
                    .append(" -> ")
                    .append(node.getSimpleName())
                    .append(";")
                    .append(lineSeparator());
            var subNodeStyles = stylings.computeIfAbsent(subNode, k -> new HashMap<>());
            var subNodeLabels = labels.computeIfAbsent(subNode, k -> new ArrayList<>());
            if (subNode.getName().contains(".internal")) {
                subNodeStyles.put("style", "filled");
                subNodeStyles.put("fillcolor", "gray");
                subNodeLabels.add("internal");
            }
            if (!subNode.isInterface()) {
                if (Modifier.isPublic(subNode.getModifiers()) && !subNode.getName().contains(".internal")) {
                    subNodeLabels.add("public impl");
                    subNodeStyles.put("style", "filled");
                    subNodeStyles.put("fillcolor", "#FFD0D0");
                }
                if (Modifier.isAbstract(subNode.getModifiers())) {
                    subNodeLabels.add("abstract");
                    subNodeStyles.put("style", "filled");
                    subNodeStyles.put("fillcolor", "red");
                }
            }
            if (!Modifier.isPublic(subNode.getModifiers())) {
                subNodeLabels.add("non-public");
                subNodeStyles.put("style", "filled");
                subNodeStyles.put("fillcolor", "red");
            }

        }

        public String render() {
            final StringBuilder result = new StringBuilder(builder);

            labels.forEach((node, l) -> {
                final var label = Stream.concat(
                        Stream.of(node.getSimpleName()),
                        l.stream()).collect(Collectors.joining("\\n"));
                stylings.computeIfAbsent(node, k -> new HashMap<>())
                        .put("label", label);
            });

            stylings.forEach((node, styles) -> {
                result.append("  ")
                        .append(node.getSimpleName());

                result.append("[");
                result.append(styles.entrySet().stream()
                        .map(e -> String.format("%s=\"%s\"", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(", ")));
                result.append("];")
                        .append(lineSeparator());
            });

            result.append("}");
            return result.toString();
        }

    }

    private static Class<?>[] permittedSubclasses(Class<?> node) {
        return java.util.Optional.ofNullable(node.getPermittedSubclasses())
                .orElse(new Class<?>[0]);
    }

}