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
     * The mapping between the record type and this memory layout is defined as follows;
     * <p>
     * Let <em>R</em> be the provided record {@code type} with the constituent components
     * <em>C<sub>0</sub></em>, ..., <em>C<sub>N-1</sub></em>, where <em>N</em> is non-negative.
     * <p>
     * Let <em>L</em> be this group layout with the elements <em>E<sub>0</sub></em>, ..., <em>E<sub>M-1</sub></em>
     * , where <em>M</em> {@code >=} <em>N</em>.
     * <p>
     * Then, for each <em>C<sub>a</sub></em> (<em>a</em> {@code <} <em>N</em>), there must be a corresponding distinct
     * <em>E<sub>b</sub></em> such that the {@link MemoryLayout#name() name} of <em>C<sub>a</sub></em>
     * {@link Object#equals(Object) equals} the {@link RecordComponent#getName() name} of the <em>E<sub>b</sub></em> and:
     * <ul>
     *    <li>
     *        <h4>If <em>E<sub>b</sub></em> is a {@link ValueLayout };</h4>
     *        then <em>C<sub>a</sub></em> must be of the exact type of <em>E<sub>b</sub>'s</em> {@link ValueLayout#carrier() carrier()}<br>
     *        whereby C<sub>a</sub> = f<sub>a</sub>(MemorySegment ms) = ms.get(E<sub>b</sub>, offset) at the appropriate offset.<br>
     *    </li>
     *    <li>
     *        <h4>If <em>E<sub>b</sub></em> is a {@link GroupLayout };</h4>
     *        then <em>C<sub>a</sub></em> must be of another {@link Record} type <em>R2</em>
     *        (such that <em>R2</em> {@code !=} <em>R</em>) that can be mapped to <em>E<sub>b</sub></em> via
     *        a resulting mapper <em>M2</em> =
     *        {@link #recordMapper(Class) E<sub>b</sub>.recordMapper(C<sub>a</sub>.type())}<br>
     *        whereby <em>C<sub>a</sub></em> = f<sub>a</sub>(MemorySegment ms) = <em>M2</em>.apply(ms) recursively
     *        at the appropriate offset.<br>
     *    </li>
     *    <li>
     *        <h4>If <em>E<sub>b</sub></em> is a {@link SequenceLayout };</h4>
     *        then <em>C<sub>a</sub></em> must be an array <em>C[]<sup>D</sup></em> (of depth <em>D</em>
     *        and with an array component type <em>C</em>) that can be mapped to <em>E<sub>b</sub></em> via a resulting
     *        "array mapper" <em>A2</em> obtained via recursively pealing off nested sequence layouts in <em>E<sub>b</sub></em>
     *        and then (after <em>D</em> pealing operations)
     *        finally determining the leaf element layout <em>LL</em> = {@link SequenceLayout#elementLayout() elementLayout()}
     *        and subsequently obtaining a leaf record mapper:
     *        <ul>
     *            <li>
     *            if <em>LL</em> is a {@link ValueLayout}:
     *            <em>LM</em> = {@link MemorySegment#get(ValueLayout.OfInt, long) ms -> ms.get(LL, offset)}
     *            </li>
     *
     *            <li>
     *            if <em>LL</em> is a {@link GroupLayout}:
     *            <em>LM</em> = {@link #recordMapper(Class) LL.recordMapper(C.type())}
     *            </li>
     *        </ul>
     *        whereby <em>C<sub>a</sub></em> = f<sub>a</sub>(MemorySegment ms) will be extracted by
     *        applying {@code A2} which, in turn, will apply {@code LM} recursively at the appropriate offset(s).<br>
     *        Note: boolean arrays are not supported despite the above and if an attempt is made to map
     *        a boolean array, an {@link IllegalArgumentException} will be thrown.
     *    </li>
     *    <li>
     *        <h4>If <em>E<sub>b</sub></em> is a {@link PaddingLayout };</h4>
     *        then the method will throw an {@link IllegalArgumentException} as a padding layout cannot
     *        be projected to any record component.<br>
     *    </li>
     *    <li>
     *        <h4>Otherwise;</h4>
     *        the method will throw an {@link IllegalArgumentException} as <em>E<sub>b</sub></em> cannot
     *        be projected onto <em>C<sub>a</sub></em>. An example of this is trying to match a record component
     *        of type {@link String}.<br>
     *    </li>
     * </ul>
     * <p>
     * If the above is true, the returned mapper will, when invoked, subsequently invoke the record type's
     * canonical constructor using a composition of the above mapping functions:
     * <p>
     * <em>ms -> R(f<sub>0</sub>(ms), ..., f<sub>N-1</sub>(ms))</em>
     * <p>
     * Unnamed elements in this group will be ignored.
     * Unmatched elements (with respect to the name) in this group layout will be ignored.
     * <p>
     * The returned function will respect the byte orderings and alignment constraints of this
     * group layout.
     * <p>
     * The returned Function may throw an {@link IllegalArgumentException} if it, for any reason, fails
     * to extract a {@link Record}. An example of such a failure is if the applied memory segment is too
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
     * Boxing, widening and narrowing must be explicitly handled by user code. In the following example, the above
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
