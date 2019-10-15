/*
 *  Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

/**
 * <p> Classes to support low-level, safe and efficient memory access. For example:
 *
 * <pre>{@code
static final VarHandle intHandle = MemoryAccessVarHandles.dereferenceVarHandle(int.class);

try (MemorySegment segment = MemorySegment.ofNative(10 * 4)) {
   MemoryAddress base = segment.baseAddress();
   for (long i = 0 ; i < 10 ; i++) {
     intHandle.set(base.offset(i * 4), (int)i);
   }
 }
 * }</pre>
 *
 * Here we create a var handle, namely {@code intHandle}, to manipulate values of the primitive type {@code int}, at
 * a given memory location. We then create a <em>native</em> memory segment, that is, a memory segment backed by
 * off-heap memory; the size of the segment is 40 bytes, enough to store 10 values of the primitive type {@code int}.
 * The segment is created inside a <em>try-with-resources</em> construct: this idiom ensures that all the memory resources
 * associated with the segment will be released at the end of the block. Inside the try-with-resources block, we initialize
 * the contents of the memory segment; more specifically, if we view the memory segment as a set of 10 adjacent slots,
 * <em>s[i]</em>, where 0 &le; <em>i</em> &lt; 10, where the size of each slot is exactly 4 bytes, the initialization logic above will set each slot
 * so that <em>s[i] = i</em>, again where 0 &le; <em>i</em> &lt; 10.
 *
 * <p>
 * The key abstractions introduced by this package are {@link jdk.incubator.foreign.MemorySegment} and {@link jdk.incubator.foreign.MemoryAddress}.
 * The first models a contiguous memory region, which can reside either inside or outside the Java heap; the latter models an address - that is,
 * an offset inside a given segment. Memory addresses represents the main access coordinate of memory access var handles, which can be obtained
 * using the combinator methods defined in the {@link jdk.incubator.foreign.MemoryHandles} class. Finally, the {@link jdk.incubator.foreign.MemoryLayout} class
 * hierarchy allows to describe <em>memory layouts</em> and simplify operations such as computing the size in bytes of a given
 * layout, obtain its alignment requirements, and so on. Memory layouts also provide an alternate, more abstract way, to produce
 * memory access var handles, e.g. using <a href="Layout.html#layout-paths"><em>layout paths</em></a>.
 *
 * <h2><a id = "concurrency">Concurrent access</a></h2>
 *
 * In their default configuration, memory segments support strong thread-confinement guarantees. Upon creation,
 * a memory segment is assigned an <em>owner thread</em>, typically the thread which initiated the creation operation.
 * A segment in this configuration is said to be a <em>confined</em> segment. Confined segments can be operated upon
 * only by their owner threads; that is, any operation, such as accessing the memory associated with a confined segment,
 * in a thread other than the owner thread will result in a runtime failure. As such, clients of confined segments
 * do not have to worry about other threads concurrently attempting to access and/or release the resources associated
 * with a memory segment they have created.
 *
 * Optionally, a client that owns a confined segment might transfer the segment ownership onto a different owner thread
 * (see {@link jdk.incubator.foreign.MemorySegment#asConfined(java.lang.Thread)}). This operation can be useful when two
 * (or more) threads might want to cooperate to achieve a certain task; this pattern of access is also known as
 * <em>serial thread confinement</em>.
 *
 * There are cases, however, where confinement rules might be too strict, and where multiple threads might require concurrent
 * access on the same memory segment. For this purpose, a confined segment can be turned into a <em>shared segment</em>
 * (see {@link jdk.incubator.foreign.MemorySegment#asShared()}}.
 * Since a shared segment has no thread owner, multiple threads can access the <em>same</em> shared segment concurrently; it is therefore
 * the responsibility of the clients to ensure that concurrent access is correct, by using some form of synchronization.
 *
 * <h2><a id="deallocation"></a>Deallocation</h2>
 * When writing code that manipulates memory segments, especially if backed by memory which resides outside the Java heap, it is
 * crucial that the resources associated with a memory segment are released when the segments are no longer in use. Confined segments support a so called <em>deterministic</em>
 * deallocation model, where clients have to explicitly free resources associated with a segment by calling the {@link jdk.incubator.foreign.MemorySegment#close()}
 * method either explicitly, or implicitly, by relying on try-with-resources construct (as demonstrated in the example above).
 * Closing a given memory segment is an <em>atomic</em> operation which can either succeed - and result in the underlying
 * memory associated with the segment to be released, or <em>fail</em> with an exception.
 * <p>
 * Conversely, shared memory segments adopt a so called <em>implicit deallocation model</em> where the underlying memory is not released until
 * the shared segment becomes <em>unreachable</em> (this model is similar to the one provided by the {@link java.nio.ByteBuffer} API,
 * see {@link java.nio.ByteBuffer#allocateDirect(int)}). While implicit deallocation can be handy - clients do not have to remember
 * to <em>close</em> a shared segment - such models can also make it harder for clients to ensure that the memory associated
 * with a native segment has indeed been released.
 *
 * <h2><a id="safety"></a>Safety</h2>
 * This API provides strong safety guarantees when it comes to memory access. First, when dereferencing a memory segment using
 * a memory address, such an address is validated (upon access), to make sure that it does not point to a memory location
 * which resides <em>outside</em> the boundaries of the memory segment it refers to. We call this guarantee <em>spatial safety</em>.
 * <p>
 * Moreover, since the resources associated with a given segment can be released, either explicitly or implicitly (see above),
 * a memory address is also validated (upon access) to make sure that the segment it points to is still valid.
 * We call this guarantee <em>temporal safety</em>. Temporal safety in confined segments is made possible by the fact that
 * these segments are serially thread-confined, that is, only one thread at a time can gain access to a given confined segment;
 * this rules out race conditions where a thread tries to access a segment while another is trying to close it.
 *
 * In the case of shared segments, temporal safety is guaranteed by the fact that the implicit deallocation model will
 * not free any resource associated with a segment while the segment is still <em>reachable</em> by some thread. This means
 * that it is not possible, by design, to access a shared memory segment <em>after</em> its resources have been released.
 * <p>
 * Together, spatial and temporal safety ensure that each memory access operation either succeeds - and accesses a valid
 * memory location - or fails gracefully (e.g. with a runtime failure as opposed to a JVM crash).
 */
package jdk.incubator.foreign;
