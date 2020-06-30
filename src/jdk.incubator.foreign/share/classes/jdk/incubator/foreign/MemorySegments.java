package jdk.incubator.foreign;

import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.HeapMemorySegmentImpl;
import jdk.internal.foreign.MappedMemorySegmentImpl;
import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.misc.Unsafe;
import jdk.internal.util.ArraysSupport;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * This class contains various methods for creating memory segments from a variety of sources (byte buffers, arrrays),
 * for manipulating memory segments (such as filling contents of segments, or obtaining a segment
 * {@link Spliterator}), for mapping existing memory segments into different views (byte buffers, arrays) and
 * for accessing the memory segment contents in various ways.
 */
public final class MemorySegments {
    private MemorySegments() {
        // just the one
    }

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    /**
     * Fills a value into the given memory segment.
     * <p>
     * More specifically, the given value is filled into each address of the given
     * segment. Equivalent to (but likely more efficient than) the following code:
     *
     * <pre>{@code
    byteHandle = MemoryLayout.ofSequence(MemoryLayouts.JAVA_BYTE)
    .varHandle(byte.class, MemoryLayout.PathElement.sequenceElement());
    for (long l = 0; l < segment.byteSize(); l++) {
    byteHandle.set(segment.baseAddress(), l, value);
    }
     * }</pre>
     *
     * without any regard or guarantees on the ordering of particular memory
     * elements being set.
     * <p>
     * Fill can be useful to initialize or reset the memory of a segment.
     *
     * @param segment the segment whose bytes are to be filled
     * @param value the value to fill into this segment
     * @throws IllegalStateException if the given segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning the given segment
     * @throws UnsupportedOperationException if this segment does not support the {@link MemorySegment#WRITE} access mode
     */
    public static void fill(MemorySegment segment, byte value) {
        AbstractMemorySegmentImpl segmentImpl = (AbstractMemorySegmentImpl)segment;
        segmentImpl.checkRange(0, segment.byteSize(), true);
        UNSAFE.setMemory(segmentImpl.base(), segmentImpl.min(), segmentImpl.byteSize(), value);
    }

    /**
     * Performs a bulk copy from given source segment to a given target segment. More specifically, the bytes at
     * offset {@code 0} through {@code from.byteSize() - 1} in the source segment are copied into the target segment
     * at offset {@code 0} through {@code to.byteSize() - 1}.
     * If the source segment overlaps with the target segment, then the copying is performed as if the bytes at
     * offset {@code 0} through {@code from.byteSize() - 1} in the source segment were first copied into a
     * temporary segment with size {@code bytes}, and then the contents of the temporary segment were copied into
     * the target segment at offset {@code 0} through {@code to.byteSize() - 1}.
     * <p>
     * The result of a bulk copy is unspecified if, in the uncommon case, the source segment and the target segment
     * do not overlap, but refer to overlapping regions of the same backing storage using different addresses.
     * For example, this may occur if the same file is {@link MemorySegments#mapFromPath mapped} to two segments.
     *
     * @param from the source segment.
     * @param to the target segment.
     * @throws IndexOutOfBoundsException if {@code from.byteSize() > to.byteSize()}.
     * @throws IllegalStateException if either the source segment or this segment have been already closed,
     * or if access occurs from a thread other than the thread owning either segment.
     * @throws UnsupportedOperationException if either the source segment or this segment do not feature required access modes;
     * more specifically, {@code from} should feature at least the {@link MemorySegment#READ} access mode,
     * while {@code to} should feature at least the {@link MemorySegment#WRITE} access mode.
     */
    public static void copy(MemorySegment from, MemorySegment to) {
        AbstractMemorySegmentImpl fromImpl = (AbstractMemorySegmentImpl)from;
        AbstractMemorySegmentImpl toImpl = (AbstractMemorySegmentImpl)to;
        long size = from.byteSize();
        toImpl.checkRange(0, size, true);
        fromImpl.checkRange(0, size, false);
        UNSAFE.copyMemory(
                fromImpl.base(), fromImpl.min(),
                toImpl.base(), toImpl.min(), size);
    }

    /**
     * Finds and returns the offset, in bytes, of the first mismatch between
     * two given segments. The offset is relative to the
     * {@link MemorySegment#baseAddress() base address} of each segment and will be in the
     * range of 0 (inclusive) up to the {@link MemorySegment#byteSize() size} (in bytes) of
     * the smaller memory segment (exclusive).
     * <p>
     * If the two segments share a common prefix then the returned offset is
     * the length of the common prefix and it follows that there is a mismatch
     * between the two segments at that offset within the respective segments.
     * If one segment is a proper prefix of the other then the returned offset is
     * the smaller of the segment sizes, and it follows that the offset is only
     * valid for the larger segment. Otherwise, there is no mismatch and {@code
     * -1} is returned.
     *
     * @param src the first segment to be tested for a mismatch
     * @param dst the second segment to be tested for a mismatch
     * @return the relative offset, in bytes, of the first mismatch between this
     * and the given other segment, otherwise -1 if no mismatch
     * @throws IllegalStateException if either this segment of the other segment
     * have been already closed, or if access occurs from a thread other than the
     * thread owning either segment
     * @throws UnsupportedOperationException if either this segment or the other
     * segment does not feature at least the {@link MemorySegment#READ} access mode
     */
    public static long mismatch(MemorySegment src, MemorySegment dst) {
        AbstractMemorySegmentImpl srcImpl = (AbstractMemorySegmentImpl)src;
        AbstractMemorySegmentImpl dstImpl = (AbstractMemorySegmentImpl)dst;
        final long thisSize = src.byteSize();
        final long thatSize = dst.byteSize();
        final long length = Math.min(thisSize, thatSize);
        srcImpl.checkRange(0, length, false);
        dstImpl.checkRange(0, length, false);
        if (src == dst) {
            return -1;
        }

        long i = 0;
        if (length > 7) {
            if (getByte(src.baseAddress(), 0) != getByte(dst.baseAddress(), 0)) {
                return 0;
            }
            i = ArraysSupport.vectorizedMismatchLargeForBytes(
                    srcImpl.base(), srcImpl.min(),
                    dstImpl.base(), dstImpl.min(),
                    length);
            if (i >= 0) {
                return i;
            }
            long remaining = ~i;
            assert remaining < 8 : "remaining greater than 7: " + remaining;
            i = length - remaining;
        }
        MemoryAddress thisAddress = src.baseAddress();
        MemoryAddress thatAddress = dst.baseAddress();
        for (; i < length; i++) {
            if (getByte(thisAddress, i) != getByte(thatAddress, i)) {
                return i;
            }
        }
        return thisSize != thatSize ? length : -1;
    }

    /**
     * Returns a spliterator for the given memory segment. The returned spliterator reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, {@link Spliterator#IMMUTABLE}, {@link Spliterator#NONNULL} and {@link Spliterator#ORDERED}
     * characteristics.
     * <p>
     * The returned spliterator splits the segment according to the specified sequence layout; that is,
     * if the supplied layout is a sequence layout whose element count is {@code N}, then calling {@link Spliterator#trySplit()}
     * will result in a spliterator serving approximatively {@code N/2} elements (depending on whether N is even or not).
     * As such, splitting is possible as long as {@code N >= 2}. The spliterator returns segments that feature the same
     * <a href="#access-modes">access modes</a> as the given segment less the {@link MemorySegment#CLOSE} access mode.
     * <p>
     * The returned spliterator effectively allows to slice a segment into disjoint sub-segments, which can then
     * be processed in parallel by multiple threads (if the access mode {@link MemorySegment#ACQUIRE} is set).
     * While closing the segment (see {@link MemorySegment#close()}) during pending concurrent execution will generally
     * fail with an exception, it is possible to close a segment when a spliterator has been obtained but no thread
     * is actively working on it using {@link Spliterator#tryAdvance(Consumer)}; in such cases, any subsequent call
     * to {@link Spliterator#tryAdvance(Consumer)} will fail with an exception.
     * @param segment the segment to be used for splitting.
     * @param layout the layout to be used for splitting.
     * @param <S> the memory segment type
     * @return the element spliterator for this segment
     * @throws IllegalStateException if the segment is not <em>alive</em>, or if access occurs from a thread other than the
     * thread owning the given segment
     */
    public static <S extends MemorySegment> Spliterator<S> spliterator(S segment, SequenceLayout layout) {
        return AbstractMemorySegmentImpl.spliterator(segment, layout);
    }

    /**
     * Wraps the given segment in a {@link ByteBuffer}. Some of the properties of the returned buffer are linked to
     * the properties of the given segment. For instance, if the given segment is <em>immutable</em>
     * (e.g. the segment has access mode {@link MemorySegment#READ} but not {@link MemorySegment#WRITE}), then the resulting buffer is <em>read-only</em>
     * (see {@link ByteBuffer#isReadOnly()}. Additionally, if the given segment is a native memory segment, the resulting buffer is
     * <em>direct</em> (see {@link ByteBuffer#isDirect()}).
     * <p>
     * The life-cycle of the returned buffer will be tied to that of the given segment. That means that if the given segment
     * is closed (see {@link MemorySegment#close()}, accessing the returned
     * buffer will throw an {@link IllegalStateException}.
     * <p>
     * The resulting buffer's byte order is {@link java.nio.ByteOrder#BIG_ENDIAN}; this can be changed using
     * {@link ByteBuffer#order(java.nio.ByteOrder)}.
     *
     * @param segment the segment from which a byte buffer view has to be created.
     * @return a {@link ByteBuffer} view of the given memory segment.
     * @throws UnsupportedOperationException if the given segment cannot be mapped onto a {@link ByteBuffer} instance,
     * e.g. because it models an heap-based segment that is not based on a {@code byte[]}), or if its size is greater
     * than {@link Integer#MAX_VALUE}, or if the segment does not support the {@link MemorySegment#READ} access mode.
     */
    public static ByteBuffer asByteBuffer(MemorySegment segment) {
        AbstractMemorySegmentImpl segmentImpl = (AbstractMemorySegmentImpl)segment;
        segmentImpl.checkAccessModes(MemorySegment.READ);
        checkArraySize("ByteBuffer", 1, segment.byteSize());
        ByteBuffer _bb = segmentImpl.makeByteBuffer();
        if (!segment.hasAccessModes(MemorySegment.WRITE)) {
            //scope is IMMUTABLE - obtain a RO byte buffer
            _bb = _bb.asReadOnlyBuffer();
        }
        return _bb;
    }

    private static int checkArraySize(String typeName, int elemSize, long length) {
        if (length % elemSize != 0) {
            throw new UnsupportedOperationException(String.format("Segment size is not a multiple of %d. Size: %d", elemSize, length));
        }
        long arraySize = length / elemSize;
        if (arraySize > (Integer.MAX_VALUE - 8)) { //conservative check
            throw new UnsupportedOperationException(String.format("Segment is too large to wrap as %s. Size: %d", typeName, length));
        }
        return (int)arraySize;
    }

    private static <Z> Z toArray(MemorySegment segment, Class<Z> arrayClass, int elemSize, IntFunction<Z> arrayFactory, Function<Z, MemorySegment> segmentFactory) {
        int size = checkArraySize(arrayClass.getSimpleName(), elemSize, segment.byteSize());
        Z arr = arrayFactory.apply(size);
        MemorySegment arrSegment = segmentFactory.apply(arr);
        copy(segment, arrSegment);
        return arr;
    }

    /**
     * Copy the contents of the given memory segment into a fresh byte array.
     * @param segment the segment whose contents are to be copied into a byte array.
     * @return a fresh byte array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link byte[]} instance, e.g. its size is greater than {@link Integer#MAX_VALUE},
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static byte[] toByteArray(MemorySegment segment) {
        return toArray(segment, byte[].class, 1, byte[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh short array.
     * @param segment the segment whose contents are to be copied into a short array.
     * @return a fresh short array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link short[]} instance, e.g. because {@code byteSize() % 4 != 0},
     * or {@code byteSize() / 2 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static short[] toShortArray(MemorySegment segment) {
        return toArray(segment, short[].class, 2, short[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh char array.
     * @param segment the segment whose contents are to be copied into a char array.
     * @return a fresh char array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link char[]} instance, e.g. because {@code byteSize() % 2 != 0},
     * or {@code byteSize() / 2 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static char[] toCharArray(MemorySegment segment) {
        return toArray(segment, char[].class, 2, char[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh int array.
     * @param segment the segment whose contents are to be copied into an int array.
     * @return a fresh int array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link int[]} instance, e.g. because {@code byteSize() % 4 != 0},
     * or {@code byteSize() / 4 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static int[] toIntArray(MemorySegment segment) {
        return toArray(segment, int[].class, 4, int[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh float array.
     * @param segment the segment whose contents are to be copied into a float array.
     * @return a fresh float array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link float[]} instance, e.g. because {@code byteSize() % 4 != 0},
     * or {@code byteSize() / 4 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static float[] toFloatArray(MemorySegment segment) {
        return toArray(segment, float[].class, 4, float[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh long array.
     * @param segment the segment whose contents are to be copied into a long array.
     * @return a fresh long array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link long[]} instance, e.g. because {@code byteSize() % 8 != 0},
     * or {@code byteSize() / 8 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static long[] toLongArray(MemorySegment segment) {
        return toArray(segment, long[].class, 8, long[]::new, MemorySegments::ofArray);
    }

    /**
     * Copy the contents of the given memory segment into a fresh double array.
     * @param segment the segment whose contents are to be copied into a double array.
     * @return a fresh double array copy of the given memory segment.
     * @throws UnsupportedOperationException if the given segment does not feature the {@link MemorySegment#READ} access mode, or if the given
     * segment's contents cannot be copied into a {@link double[]} instance, e.g. because {@code byteSize() % 8 != 0},
     * or {@code byteSize() / 8 > Integer#MAX_VALUE}.
     * @throws IllegalStateException if the given segment has been closed, or if access occurs from a thread other than the
     * thread owning the given segment.
     */
    public static double[] toDoubleArray(MemorySegment segment) {
        return toArray(segment, double[].class, 8, double[]::new, MemorySegments::ofArray);
    }

    /**
     * Creates a new buffer memory segment that models the memory associated with the given byte
     * buffer. The segment starts relative to the buffer's position (inclusive)
     * and ends relative to the buffer's limit (exclusive).
     * <p>
     * The segment will feature all <a href="#access-modes">access modes</a> (see {@link MemorySegment#ALL_ACCESS}),
     * unless the given buffer is {@linkplain ByteBuffer#isReadOnly() read-only} in which case the segment will
     * not feature the {@link MemorySegment#WRITE} access mode.
     * <p>
     * The resulting memory segment keeps a reference to the backing buffer, to ensure it remains <em>reachable</em>
     * for the life-time of the segment.
     *
     * @param bb the byte buffer backing the buffer memory segment.
     * @return a new buffer memory segment.
     */
    public static MemorySegment ofByteBuffer(ByteBuffer bb) {
        return AbstractMemorySegmentImpl.ofBuffer(bb);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated byte array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(byte[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated char array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(char[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated short array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(short[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated int array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>.
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(int[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated float array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(float[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated long array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(long[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new array memory segment that models the memory associated with a given heap-allocated double array.
     * <p>
     * The resulting memory segment keeps a reference to the backing array, to ensure it remains <em>reachable</em>
     * for the life-time of the segment. The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @param arr the primitive array backing the array memory segment.
     * @return a new array memory segment.
     */
    public static MemorySegment ofArray(double[] arr) {
        return HeapMemorySegmentImpl.makeArraySegment(arr);
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given layout.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    allocateNative(layout.bytesSize(), layout.bytesAlignment());
     * }</pre></blockquote>
     *
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param layout the layout of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if the specified layout has illegal size or alignment constraint.
     */
    public static MemorySegment allocateNative(MemoryLayout layout) {
        return allocateNative(layout.byteSize(), layout.byteAlignment());
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size (in bytes).
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    allocateNative(bytesSize, 1);
     * }</pre></blockquote>
     *
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if {@code bytesSize < 0}.
     */
    public static MemorySegment allocateNative(long bytesSize) {
        return allocateNative(bytesSize, 1);
    }

    /**
     * Returns a new native memory segment with given base address and size; the returned segment has its own temporal
     * bounds, and can therefore be closed; closing such a segment can optionally result in calling an user-provided cleanup
     * action. This method can be very useful when interacting with custom native memory sources (e.g. custom allocators,
     * GPU memory, etc.), where an address to some underlying memory region is typically obtained from native code
     * (often as a plain {@code long} value). The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     * <p>
     * This method is <em>restricted</em>. Restricted methods are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param addr the desired base address
     * @param bytesSize the desired size.
     * @param owner the desired owner thread. If {@code owner == null}, the returned segment is <em>not</em> confined.
     * @param cleanup a cleanup action to be executed when the {@link MemorySegment#close()} method is called on the
     *                returned segment. If {@code cleanup == null}, no cleanup action is executed.
     * @param attachment an object that must be kept alive by the returned segment; this can be useful when
     *                   the returned segment depends on memory which could be released if a certain object
     *                   is determined to be unreacheable. In most cases this will be set to {@code null}.
     * @return a new native memory segment with given base address, size, owner, cleanup action and object attachment.
     * @throws IllegalArgumentException if {@code bytesSize <= 0}.
     * @throws UnsupportedOperationException if {@code addr} is associated with an heap segment.
     * @throws IllegalAccessError if the runtime property {@code foreign.restricted} is not set to either
     * {@code permit}, {@code warn} or {@code debug} (the default value is set to {@code deny}).
     * @throws NullPointerException if {@code addr == null}.
     */
    public static MemorySegment ofNativeRestricted(MemoryAddress addr, long bytesSize, Thread owner, Runnable cleanup, Object attachment) {
        Objects.requireNonNull(addr);
        if (bytesSize <= 0) {
            throw new IllegalArgumentException("Invalid size : " + bytesSize);
        }
        Utils.checkRestrictedAccess("MemorySegment.ofNativeRestricted");
        return NativeMemorySegmentImpl.makeNativeSegmentUnchecked(addr, bytesSize, owner, cleanup, attachment);
    }

    /**
     * Creates a new mapped memory segment that models a memory-mapped region of a file from a given path.
     * <p>
     * The segment will feature all <a href="#access-modes">access modes</a> (see {@link MemorySegment#ALL_ACCESS}),
     * unless the given mapping mode is {@linkplain FileChannel.MapMode#READ_ONLY}, in which case
     * the segment will not feature the {@link MemorySegment#WRITE} access mode.
     *
     * @implNote When obtaining a mapped segment from a newly created file, the initialization state of the contents of the block
     * of mapped memory associated with the returned mapped memory segment is unspecified and should not be relied upon.
     *
     * @param path the path to the file to memory map.
     * @param bytesOffset the offset (expressed in bytes) within the file at which the mapped segment is to start.
     * @param bytesSize the size (in bytes) of the mapped memory backing the memory segment.
     * @param mapMode a file mapping mode, see {@link FileChannel#map(FileChannel.MapMode, long, long)}; the chosen mapping mode
     *                might affect the behavior of the returned memory mapped segment (see {@link MappedMemorySegment#force()}).
     * @return a new mapped memory segment.
     * @throws IllegalArgumentException if {@code bytesOffset < 0}.
     * @throws IllegalArgumentException if {@code bytesSize < 0}.
     * @throws UnsupportedOperationException if an unsupported map mode is specified.
     * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
     */
    public static MappedMemorySegment mapFromPath(Path path, long bytesOffset, long bytesSize, FileChannel.MapMode mapMode) throws IOException {
        return MappedMemorySegmentImpl.makeMappedSegment(path, bytesOffset, bytesSize, mapMode);
    }

    /**
     * Creates a new native memory segment that models a newly allocated block of off-heap memory with given size and
     * alignment constraint (in bytes). The segment will feature all <a href="#access-modes">access modes</a>
     * (see {@link MemorySegment#ALL_ACCESS}).
     *
     * @implNote The block of off-heap memory associated with the returned native memory segment is initialized to zero.
     * Moreover, a client is responsible to call the {@link MemorySegment#close()} on a native memory segment,
     * to make sure the backing off-heap memory block is deallocated accordingly. Failure to do so will result in off-heap memory leaks.
     *
     * @param bytesSize the size (in bytes) of the off-heap memory block backing the native memory segment.
     * @param alignmentBytes the alignment constraint (in bytes) of the off-heap memory block backing the native memory segment.
     * @return a new native memory segment.
     * @throws IllegalArgumentException if {@code bytesSize < 0}, {@code alignmentBytes < 0}, or if {@code alignmentBytes}
     * is not a power of 2.
     */
    public static MemorySegment allocateNative(long bytesSize, long alignmentBytes) {
        if (bytesSize <= 0) {
            throw new IllegalArgumentException("Invalid allocation size : " + bytesSize);
        }

        if (alignmentBytes < 0 ||
                ((alignmentBytes & (alignmentBytes - 1)) != 0L)) {
            throw new IllegalArgumentException("Invalid alignment constraint : " + alignmentBytes);
        }

        return NativeMemorySegmentImpl.makeNativeSegment(bytesSize, alignmentBytes);
    }

    private static final VarHandle byte_LE_handle = indexedHandle(MemoryLayouts.BITS_8_LE, byte.class);
    private static final VarHandle char_LE_handle = indexedHandle(MemoryLayouts.BITS_16_LE, char.class);
    private static final VarHandle short_LE_handle = indexedHandle(MemoryLayouts.BITS_16_LE, short.class);
    private static final VarHandle int_LE_handle = indexedHandle(MemoryLayouts.BITS_32_LE, int.class);
    private static final VarHandle float_LE_handle = indexedHandle(MemoryLayouts.BITS_32_LE, float.class);
    private static final VarHandle long_LE_handle = indexedHandle(MemoryLayouts.BITS_64_LE, long.class);
    private static final VarHandle double_LE_handle = indexedHandle(MemoryLayouts.BITS_64_LE, double.class);

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_LE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte_LE(MemoryAddress addr, long offset) {
        return (byte)byte_LE_handle.get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_LE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte_LE(MemoryAddress addr, long offset, byte value) {
        byte_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar_LE(MemoryAddress addr, long offset) {
        return (char)char_LE_handle.get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar_LE(MemoryAddress addr, long offset, char value) {
        char_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort_LE(MemoryAddress addr, long offset) {
        return (short)short_LE_handle.get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_LE.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort_LE(MemoryAddress addr, long offset, short value) {
        short_LE_handle.set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt_LE(MemoryAddress addr, long offset) {
        return (int)int_LE_handle.get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt_LE(MemoryAddress addr, long offset, int value) {
        int_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat_LE(MemoryAddress addr, long offset) {
        return (float)float_LE_handle.get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat_LE(MemoryAddress addr, long offset, float value) {
        float_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong_LE(MemoryAddress addr, long offset) {
        return (long)long_LE_handle.get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong_LE(MemoryAddress addr, long offset, long value) {
        long_LE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_LE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble_LE(MemoryAddress addr, long offset) {
        return (double)double_LE_handle.get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#LITTLE_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_LE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble_LE(MemoryAddress addr, long offset, double value) {
        double_LE_handle.set(addr, offset, value);
    }

    private static final VarHandle byte_BE_handle = indexedHandle(MemoryLayouts.BITS_8_BE, byte.class);
    private static final VarHandle char_BE_handle = indexedHandle(MemoryLayouts.BITS_16_BE, char.class);
    private static final VarHandle short_BE_handle = indexedHandle(MemoryLayouts.BITS_16_BE, short.class);
    private static final VarHandle int_BE_handle = indexedHandle(MemoryLayouts.BITS_32_BE, int.class);
    private static final VarHandle float_BE_handle = indexedHandle(MemoryLayouts.BITS_32_BE, float.class);
    private static final VarHandle long_BE_handle = indexedHandle(MemoryLayouts.BITS_64_BE, long.class);
    private static final VarHandle double_BE_handle = indexedHandle(MemoryLayouts.BITS_64_BE, double.class);

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_BE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte_BE(MemoryAddress addr, long offset) {
        return (byte)byte_BE_handle.get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_8_BE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte_BE(MemoryAddress addr, long offset, byte value) {
        byte_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar_BE(MemoryAddress addr, long offset) {
        return (char)char_BE_handle.get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar_BE(MemoryAddress addr, long offset, char value) {
        char_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort_BE(MemoryAddress addr, long offset) {
        return (short)short_BE_handle.get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_16_BE.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort_BE(MemoryAddress addr, long offset, short value) {
        short_BE_handle.set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt_BE(MemoryAddress addr, long offset) {
        return (int)int_BE_handle.get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt_BE(MemoryAddress addr, long offset, int value) {
        int_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat_BE(MemoryAddress addr, long offset) {
        return (float)float_BE_handle.get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat_BE(MemoryAddress addr, long offset, float value) {
        float_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong_BE(MemoryAddress addr, long offset) {
        return (long)long_BE_handle.get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong_BE(MemoryAddress addr, long offset, long value) {
        long_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_32_BE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble_BE(MemoryAddress addr, long offset) {
        return (double)double_BE_handle.get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#BIG_ENDIAN}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(BITS_64_BE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble_BE(MemoryAddress addr, long offset, double value) {
        double_BE_handle.set(addr, offset, value);
    }

    /**
     * Read a byte from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_BYTE.withBitAlignment(8).varHandle(byte.class), 1L);
    byte value = (byte)handle.get(addr, offset);
     * }</pre></blockquote>
     *
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a byte value read from {@code addr} at the offset specified by {@code index}.
     */
    public static byte getByte(MemoryAddress addr, long offset) {
        return (byte)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? byte_BE_handle : byte_LE_handle).get(addr, offset);
    }

    /**
     * Writes a byte at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_BYTE.withBitAlignment(8).varHandle(byte.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the byte value to be written.
     */
    public static void setByte(MemoryAddress addr, long offset, byte value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? byte_BE_handle : byte_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a char from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_CHAR.withBitAlignment(8).varHandle(char.class), 1L);
    char value = (char)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a char value read from {@code addr} at the offset specified by {@code index}.
     */
    public static char getChar(MemoryAddress addr, long offset) {
        return (char)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).get(addr, offset);
    }

    /**
     * Writes a char at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_CHAR.withBitAlignment(8).varHandle(char.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the char value to be written.
     */
    public static void setChar(MemoryAddress addr, long offset, char value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? char_BE_handle : char_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a short from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_SHORT.withBitAlignment(8).varHandle(short.class), 1L);
    short value = (short)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a short value read from {@code addr} at the offset specified by {@code index}.
     */
    public static short getShort(MemoryAddress addr, long offset) {
        return (short)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).get(addr, offset);
    }

    /**
     * Writes a short at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_SHORT.withBitAlignment(8).varHandle(short.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the short value to be written.
     */
    public static void setShort(MemoryAddress addr, long offset, short value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? short_BE_handle : short_LE_handle).set(addr, offset, value);
    }

    /**
     * Read an int from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_INT.withBitAlignment(8).varHandle(int.class), 1L);
    int value = (int)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return an int value read from {@code addr} at the offset specified by {@code index}.
     */
    public static int getInt(MemoryAddress addr, long offset) {
        return (int)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).get(addr, offset);
    }

    /**
     * Writes an int at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_INT.withBitAlignment(8).varHandle(int.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the int value to be written.
     */
    public static void setInt(MemoryAddress addr, long offset, int value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? int_BE_handle : int_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a float from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_FLOAT.withBitAlignment(8).varHandle(float.class), 1L);
    float value = (float)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a float value read from {@code addr} at the offset specified by {@code index}.
     */
    public static float getFloat(MemoryAddress addr, long offset) {
        return (float)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).get(addr, offset);
    }

    /**
     * Writes a float at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_FLOAT.withBitAlignment(8).varHandle(float.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the float value to be written.
     */
    public static void setFloat(MemoryAddress addr, long offset, float value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? float_BE_handle : float_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a long from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L);
    long value = (long)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a long value read from {@code addr} at the offset specified by {@code index}.
     */
    public static long getLong(MemoryAddress addr, long offset) {
        return (long)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).get(addr, offset);
    }

    /**
     * Writes a long at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_LONG.withBitAlignment(8).varHandle(long.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the long value to be written.
     */
    public static void setLong(MemoryAddress addr, long offset, long value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? long_BE_handle : long_LE_handle).set(addr, offset, value);
    }

    /**
     * Read a double from given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_DOUBLE.withBitAlignment(8).varHandle(double.class), 1L);
    double value = (double)handle.get(addr, offset);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @return a double value read from {@code addr} at the offset specified by {@code index}.
     */
    public static double getDouble(MemoryAddress addr, long offset) {
        return (double)((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).get(addr, offset);
    }

    /**
     * Writes a double at given address and offset, with byte order set to {@link ByteOrder#nativeOrder()}.
     * <p>
     * This is equivalent to the following code:
     * <blockquote><pre>{@code
    VarHandle handle = MemoryHandles.withStride(JAVA_DOUBLE.withBitAlignment(8).varHandle(double.class), 1L);
    handle.set(addr, offset, value);
     * }</pre></blockquote>
     * @param addr base address.
     * @param offset offset (relative to {@code addr}). The final address of this read operation can be expressed as {@code addr.addOffset(offset)}.
     * @param value the double value to be written.
     */
    public static void setDouble(MemoryAddress addr, long offset, double value) {
        ((ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) ? double_BE_handle : double_LE_handle).set(addr, offset, value);
    }

    private static VarHandle indexedHandle(MemoryLayout elementLayout, Class<?> carrier) {
        return MemoryHandles.withStride(elementLayout.withBitAlignment(8).varHandle(carrier), 1L);
    }
}
