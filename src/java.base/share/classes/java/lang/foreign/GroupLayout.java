/*
 *  Copyright (c) 2019, 2023, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.foreign;

import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.function.Function;

import jdk.internal.javac.PreviewFeature;

/**
 * A compound layout that aggregates multiple <em>member layouts</em>. There are two ways in which member layouts
 * can be combined: if member layouts are laid out one after the other, the resulting group layout is said to be a <em>struct layout</em>
 * (see {@link MemoryLayout#structLayout(MemoryLayout...)}); conversely, if all member layouts are laid out at the same starting offset,
 * the resulting group layout is said to be a <em>union layout</em> (see {@link MemoryLayout#unionLayout(MemoryLayout...)}).
 *
 * @implSpec
 * This class is immutable, thread-safe and <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 *
 * @since 19
 */
@PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
public sealed interface GroupLayout extends MemoryLayout permits StructLayout, UnionLayout {

    /**
     * Returns the member layouts associated with this group.
     *
     * @apiNote the order in which member layouts are returned is the same order in which member layouts have
     * been passed to one of the group layout factory methods (see {@link MemoryLayout#structLayout(MemoryLayout...)},
     * {@link MemoryLayout#unionLayout(MemoryLayout...)}).
     *
     * @return the member layouts associated with this group.
     */
    List<MemoryLayout> memberLayouts();

    /**
     * {@inheritDoc}
     */
    @Override
    GroupLayout withName(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    GroupLayout withoutName();

    /**
     * {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IllegalArgumentException if {@code byteAlignment} is less than {@code M}, where {@code M} is the maximum alignment
     * constraint in any of the member layouts associated with this group layout.
     */
    @Override
    GroupLayout withByteAlignment(long byteAlignment);

    /**
     * {@return a {@link Function} that can project {@linkplain MemorySegment MemorySegments} onto new instances
     * of the provided {@link Record} {@code type} by means of matching the names of the
     * record components with the names of the elements in this group layout}
     * <p>
     * A mapper {@code M} between a record type {@code R}, a memory layout {@code L} and an {@code offset}
     * (which is zero for the initial invocation of this method) is defined as follows {@code M(type, layout, offset)};
     * <p>
     * Let {@code R} comprise its the constituent components
     * {@code c_1, c_2}, ..., c_N}, where {@code N} is the (non-negative) number of components of {@code R}.
     * <p>
     * Let {@code f_a(MemorySegment ms)} be a function that takes
     * a {@link MemorySegment} {@code ms} and produces a value for {@code c_a}.
     * <p>
     * Let {@code L} be a group layout with the elements {@code e_1, e_2, ..., e_M}, where {@code M >= N}.
     * <p>
     * Let {@code offsetBetween(GroupLayout layout, MemoryLayout target)} be a function that, via
     * the method {@link MemoryLayout#byteOffset(PathElement...)}, can compute the offset from the
     * {@code layout L} to a sub-layout of {@code L}.
     * <p>
     * Then, for each {@code c_a, a <= N}, there must be a corresponding distinct
     * {@code e_b} such that the {@link MemoryLayout#name() name} of {@code c_a}
     * {@link Object#equals(Object) equals} the {@link RecordComponent#getName() name} of {@code e_b} and:
     * <ul>
     *    <li>
     *        <h4>If {@code e_b} is a {@link ValueLayout };</h4>
     *        then {@code c_a} must be of the exact type of {@code e_b}'s {@link ValueLayout#carrier() carrier()}<br>
     *        whereby {@code c_a = f_a(MemorySegment ms) = ms.get(e_b, offset + offsetBetween(layout, e_b))}.<br>
     *    </li>
     *    <li>
     *        <h4>If {@code e_b} is a {@link GroupLayout };</h4>
     *        then {@code e_a} must be of another {@link Record} type {@code R2}
     *        (such that {@code R2 != R}) that can be mapped to {@code e_b} via
     *        another resulting mapper {@code M2 = M(R2, e_b, offset + offsetBetween(layout, e_b))} recursively
     *        whereby {@code c_a} = {@code f_a(MemorySegment ms) = M2.apply(ms)}<br>
     *    </li>
     *    <li>
     *        <h4>If {@code e_b} is a {@link SequenceLayout };</h4>
     *        then {@code c_a} must be an array {@code C[]^D} (an array of depth {@code D}
     *        and with an array component type {@code C}) that can be mapped to {@code e_b} via a resulting
     *        "array mapper" {@code A2} obtained via recursively pealing off nested sequence layouts in {@code e_b}
     *        and then (after {@code D} pealing operations)
     *        finally determining the leaf element layout {@code LL = } {@link SequenceLayout#elementLayout() elementLayout()}
     *        and subsequently obtaining a leaf mapper {@code (MemorySegment ms, long componentOffset)}:
     *        <ul>
     *            <li>
     *            if {@code LL} is a {@link ValueLayout}:
     *            {@code LM} = {@link MemorySegment#get(ValueLayout.OfInt, long) ms -> ms.get(LL, offset + offsetBetween(layout, e_b))}
     *            </li>
     *
     *            <li>
     *            if {@code LL} is a {@link GroupLayout}:
     *            {@code LM} = recursively M(C.type, LL, offset + offsetBetween(layout, e_b)}
     *            </li>
     *        </ul>
     *        whereby {@code c_a} = {@code f_a(MemorySegment ms)} will be extracted by
     *        applying {@code A2} which, in turn, will apply {@code LM} recursively at
     *        a memory segment slice at the applicable multidimensional array offsets.<br>
     *        Note: boolean arrays are not supported despite the above and if an attempt is made to map
     *        a boolean array, an {@link IllegalArgumentException} will be thrown.
     *    </li>
     *    <li>
     *        <h4>If {@code e_b} is a {@link PaddingLayout };</h4>
     *        then the method will throw an {@link IllegalArgumentException} as a padding layout cannot
     *        be projected to any record component.<br>
     *    </li>
     *    <li>
     *        <h4>Otherwise;</h4>
     *        the method will throw an {@link IllegalArgumentException} as {@code e_b} cannot
     *        be projected onto {@code c_a}.  An example of this is trying to match a record component
     *        of type {@link String}.<br>
     *    </li>
     * </ul>
     * <p>
     * If the above is true, the returned mapper {@code M(type, this, 0L)} will, when invoked,
     * subsequently invoke the record type's canonical constructor {@code R::new} using a composition
     * of the above mapping functions:
     * <p>
     * {@code ms -> new R(f_1(ms), f_2(ms), ..., f_N(ms))}
     * <p>
     * Unnamed elements in this group will be ignored.
     * Unmatched elements (with respect to the name) in this group layout will be ignored.
     * <p>
     * The returned function will respect the byte orderings and alignment constraints of this
     * group layout.
     * <p>
     * The returned Function may throw an {@link IllegalArgumentException} if it, for any reason, fails
     * to extract a {@link Record}.  An example of such a failure is if the applied memory segment is too
     * small for the layout at hand.
     * <p>
     * The example below shows how to extract an instance of a public {@code Point} record class
     * from a {@link MemorySegment}:
     * {@snippet lang = java:
     *     MemorySegment segment = MemorySegment.ofArray(new int[]{3, 4});
     *
     *     public record Point(int x, int y){}
     *
     *     var pointLayout = MemoryLayout.structLayout(
     *         JAVA_INT.withName("x"),
     *         JAVA_INT.withName("y")
     *     );
     *
     *     Function<MemorySegment, Point> pointExtractor = pointLayout.recordMapper(Point.class);
     *
     *     // Extracts a new Point from the provided MemorySegment
     *     Point point = pointExtractor.apply(segment); // Point[x=3, y=4]
     * }
     * <p>
     * Boxing, widening and narrowing must be explicitly handled by user code.  In the following example, the above
     * {@code Point} (using primitive {@code int x} and {@code int y} coordinates) are explicitly mapped to
     * a narrowed point type (instead using primitive {@code byte x} and {@code byte y} coordinates):
     * <p>
     * {@snippet lang = java:
     *     public record NarrowedPoint(byte x, byte y) {
     *
     *         static NarrowedPoint fromPoint(Point p) {
     *             return new NarrowedPoint((byte) p.x, (byte) p.y);
     *         }
     *     }
     *
     *     Function<MemorySegment, NarrowedPoint> narrowedPointExtractor =
     *             pointLayout.recordMapper(Point.class)
     *                     .andThen(NarrowedPoint::fromPoint);
     *
     *     // Extracts a new NarrowedPoint from the provided MemorySegment
     *     NarrowedPoint narrowedPoint = narrowedPointExtractor.apply(segment); // NarrowedPoint[x=3, y=4]
     * }
     *
     * @param <R> record type
     * @param type the type (Class) of the record
     * @throws IllegalArgumentException if the provided record {@code type} is the class {@link Record} or contains
     *                                  components for which there are no exact mapping (of names and types) in
     *                                  this group layout or if the provided {@code type} is not public or
     *                                  if the method is otherwise unable to create a record mapper as specified above.
     * @since 21
     */
    <R extends Record> Function<MemorySegment, R> recordMapper(Class<R> type);

}
