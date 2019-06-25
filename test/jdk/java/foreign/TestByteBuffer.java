/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @modules java.base/jdk.internal.foreign
 *          java.base/sun.nio.ch
 * @run testng TestByteBuffer
 */

import java.foreign.GroupLayout;
import java.foreign.MemoryAddress;
import java.foreign.MemorySegment;
import java.foreign.SequenceLayout;
import java.foreign.ValueLayout;
import java.io.File;
import java.lang.invoke.VarHandle;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import jdk.internal.foreign.MemoryAddressImpl;
import org.testng.annotations.*;
import sun.nio.ch.DirectBuffer;

import static org.testng.Assert.*;

public class TestByteBuffer {

    static SequenceLayout tuples = SequenceLayout.of(500,
            GroupLayout.struct(
                    ValueLayout.ofSignedInt(32).withName("index"),
                    ValueLayout.ofFloatingPoint(32).withName("value")
            ));

    static SequenceLayout bytes = SequenceLayout.of(100,
            ValueLayout.ofSignedInt(8)
    );

    static SequenceLayout chars = SequenceLayout.of(100,
            ValueLayout.ofUnsignedInt(16)
    );

    static SequenceLayout shorts = SequenceLayout.of(100,
            ValueLayout.ofSignedInt(16)
    );

    static SequenceLayout ints = SequenceLayout.of(100,
            ValueLayout.ofSignedInt(32)
    );

    static SequenceLayout floats = SequenceLayout.of(100,
            ValueLayout.ofFloatingPoint(32)
    );

    static SequenceLayout longs = SequenceLayout.of(100,
            ValueLayout.ofSignedInt(64)
    );

    static SequenceLayout doubles = SequenceLayout.of(100,
            ValueLayout.ofFloatingPoint(64)
    );

    static VarHandle indexHandle = tuples.toPath().elementPath().elementPath("index").dereferenceHandle(int.class);
    static VarHandle valueHandle = tuples.toPath().elementPath().elementPath("value").dereferenceHandle(float.class);

    static VarHandle byteHandle = bytes.toPath().elementPath().dereferenceHandle(byte.class);
    static VarHandle charHandle = chars.toPath().elementPath().dereferenceHandle(char.class);
    static VarHandle shortHandle = shorts.toPath().elementPath().dereferenceHandle(short.class);
    static VarHandle intHandle = ints.toPath().elementPath().dereferenceHandle(int.class);
    static VarHandle floatHandle = floats.toPath().elementPath().dereferenceHandle(float.class);
    static VarHandle longHandle = doubles.toPath().elementPath().dereferenceHandle(long.class);
    static VarHandle doubleHandle = longs.toPath().elementPath().dereferenceHandle(double.class);


    static void initTuples(MemoryAddress base) {
        for (long i = 0; i < tuples.elementsSize().getAsLong() ; i++) {
            indexHandle.set(base, i, (int)i);
            valueHandle.set(base, i, (float)(i / 500f));
        }
    }

    static void checkTuples(MemoryAddress base, ByteBuffer bb) {
        bb = bb.order(ByteOrder.nativeOrder());
        for (long i = 0; i < tuples.elementsSize().getAsLong() ; i++) {
            assertEquals(bb.getInt(), (int)indexHandle.get(base, i));
            assertEquals(bb.getFloat(), (float)valueHandle.get(base, i));
        }
    }

    static void initBytes(MemoryAddress base, SequenceLayout seq, BiConsumer<MemoryAddress, Long> handleSetter) {
        for (long i = 0; i < seq.elementsSize().getAsLong() ; i++) {
            handleSetter.accept(base, i);
        }
    }

    static <Z extends Buffer> void checkBytes(MemoryAddress base, SequenceLayout layout,
                                              Function<ByteBuffer, Z> bufFactory,
                                              BiFunction<MemoryAddress, Long, Object> handleExtractor,
                                              Function<Z, Object> bufferExtractor) {
        long nelems = layout.elementsSize().getAsLong();
        long elemSize = layout.elementLayout().bytesSize();
        for (long i = 0 ; i < nelems ; i++) {
            long limit = nelems - i;
            MemorySegment resizedSegment = base.segment().resize(i * elemSize, limit * elemSize);
            ByteBuffer bb = resizedSegment.baseAddress().asByteBuffer((int)limit * (int)elemSize)
                    .order(ByteOrder.nativeOrder());
            Z z = bufFactory.apply(bb);
            for (long j = i ; j < limit ; j++) {
                Object handleValue = handleExtractor.apply(resizedSegment.baseAddress(), j - i);
                Object bufferValue = bufferExtractor.apply(z);
                if (handleValue instanceof Number) {
                    assertEquals(((Number)handleValue).longValue(), j);
                    assertEquals(((Number)bufferValue).longValue(), j);
                } else {
                    assertEquals((long)(char)handleValue, j);
                    assertEquals((long)(char)bufferValue, j);
                }
            }
        }
    }

    @Test
    public void testOffheap() {
        try (MemorySegment segment = MemorySegment.ofNative(tuples)) {
            MemoryAddress base = segment.baseAddress();
            initTuples(base);

            ByteBuffer bb = base.asByteBuffer((int) tuples.bytesSize());
            checkTuples(base, bb);
        }
    }

    @Test
    public void testHeap() {
        byte[] arr = new byte[(int) tuples.bytesSize()];
        MemorySegment region = MemorySegment.ofArray(arr);
        MemoryAddress base = region.baseAddress();
        initTuples(base);

        ByteBuffer bb = base.asByteBuffer((int) tuples.bytesSize());
        checkTuples(base, bb);
    }

    @Test
    public void testChannel() throws Throwable {
        File f = new File("test.out");
        assertTrue(f.createNewFile());
        f.deleteOnExit();

        //write to channel
        try (FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            withMappedBuffer(channel, FileChannel.MapMode.READ_WRITE, 0, tuples.bytesSize(), mbb -> {
                MemorySegment segment = MemorySegment.ofByteBuffer(mbb);
                MemoryAddress base = segment.baseAddress();
                initTuples(base);
                mbb.force();
            });
        }

        //read from channel
        try (FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.READ)) {
            withMappedBuffer(channel, FileChannel.MapMode.READ_ONLY, 0, tuples.bytesSize(), mbb -> {
                MemorySegment segment = MemorySegment.ofByteBuffer(mbb);
                MemoryAddress base = segment.baseAddress();
                checkTuples(base, mbb);
            });
        }
    }

    static void withMappedBuffer(FileChannel channel, FileChannel.MapMode mode, long pos, long size, Consumer<MappedByteBuffer> action) throws Throwable {
        MappedByteBuffer mbb = channel.map(mode, pos, size);
        var ref = new WeakReference<>(mbb);
        action.accept(mbb);
        mbb = null;
        //wait for it to be GCed
        System.gc();
        while (ref.get() != null) {
            Thread.sleep(20);
        }
    }

    @Test(dataProvider = "bufferOps")
    public void testScopedBuffer(Function<ByteBuffer, Buffer> bufferFactory, Map<Method, Object[]> members) {
        Buffer bb;
        try (MemorySegment segment = MemorySegment.ofNative(bytes)) {
            MemoryAddress base = segment.baseAddress();
            bb = bufferFactory.apply(base.asByteBuffer((int) bytes.bytesSize()));
        }
        //outside of scope!!
        for (Map.Entry<Method, Object[]> e : members.entrySet()) {
            try {
                e.getKey().invoke(bb, e.getValue());
                assertTrue(false);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof IllegalStateException) {
                    //all other buffer operation should fail because of the scope check
                    assertTrue(ex.getCause().getMessage().contains("not alive"));
                } else {
                    //all other exceptions were unexpected - fail
                    assertTrue(false);
                }
            } catch (Throwable ex) {
                //unexpected exception - fail
                assertTrue(false);
            }
        }
    }

    @Test(dataProvider = "bufferOps")
    public void testDirectBuffer(Function<ByteBuffer, Buffer> bufferFactory, Map<Method, Object[]> members) {
        try (MemorySegment segment = MemorySegment.ofNative(bytes)) {
            MemoryAddress base = segment.baseAddress();
            Buffer bb = bufferFactory.apply(base.asByteBuffer((int)bytes.bytesSize()));
            assertTrue(bb.isDirect());
            DirectBuffer directBuffer = ((DirectBuffer)bb);
            assertEquals(directBuffer.address(), MemoryAddressImpl.addressof(base));
            assertTrue((directBuffer.attachment() == null) == (bb instanceof ByteBuffer));
            assertTrue(directBuffer.cleaner() == null);
        }
    }

    @Test(dataProvider="resizeOps")
    public void testResizeOffheap(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        try (MemorySegment segment = MemorySegment.ofNative(seq)) {
            MemoryAddress base = segment.baseAddress();
            initializer.accept(base);
            checker.accept(base);
        }
    }

    @Test(dataProvider="resizeOps")
    public void testResizeHeap(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        int capacity = (int)seq.bytesSize();
        MemoryAddress base = MemorySegment.ofArray(new byte[capacity]).baseAddress();
        initializer.accept(base);
        checker.accept(base);
    }

    @Test(dataProvider="resizeOps")
    public void testResizeBuffer(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        int capacity = (int)seq.bytesSize();
        MemoryAddress base = MemorySegment.ofByteBuffer(ByteBuffer.wrap(new byte[capacity])).baseAddress();
        initializer.accept(base);
        checker.accept(base);
    }

    @Test(dataProvider="resizeOps")
    public void testResizeRoundtripHeap(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        int capacity = (int)seq.bytesSize();
        byte[] arr = new byte[capacity];
        MemoryAddress first = MemorySegment.ofArray(arr).baseAddress();
        initializer.accept(first);
        MemoryAddress second = MemorySegment.ofByteBuffer(first.asByteBuffer(capacity)).baseAddress();
        checker.accept(second);
    }

    @Test(dataProvider="resizeOps")
    public void testResizeRoundtripNative(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        try (MemorySegment segment = MemorySegment.ofNative(seq)) {
            int capacity = (int) seq.bytesSize();
            MemoryAddress first = segment.baseAddress();
            initializer.accept(first);
            MemoryAddress second = MemorySegment.ofByteBuffer(first.asByteBuffer(capacity)).baseAddress();
            checker.accept(second);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBufferOnClosedScope() {
        MemoryAddress base;
        try (MemorySegment segment = MemorySegment.ofNative(bytes)) {
            base = segment.baseAddress();
        }
        base.asByteBuffer((int)bytes.elementsSize().getAsLong());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBufferTooLarge() {
        try (MemorySegment segment = MemorySegment.ofNative(bytes)) {
            MemoryAddress base = segment.baseAddress();
            base.asByteBuffer((int)bytes.elementsSize().getAsLong() * 2);
        }
    }

    @Test(dataProvider="resizeOps")
    public void testCopyHeapToNative(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        int bytes = (int)seq.bytesSize();
        try (MemorySegment nativeArray = MemorySegment.ofNative(bytes) ;
             MemorySegment heapArray = MemorySegment.ofArray(new byte[bytes])) {
            initializer.accept(heapArray.baseAddress());
            MemoryAddress.copy(heapArray.baseAddress(), nativeArray.baseAddress(), bytes);
            checker.accept(nativeArray.baseAddress());
        }
    }

    @Test(dataProvider="resizeOps")
    public void testCopyNativeToHeap(Consumer<MemoryAddress> checker, Consumer<MemoryAddress> initializer, SequenceLayout seq) {
        int bytes = (int)seq.bytesSize();
        try (MemorySegment nativeArray = MemorySegment.ofNative(seq) ;
             MemorySegment heapArray = MemorySegment.ofArray(new byte[bytes])) {
            initializer.accept(nativeArray.baseAddress());
            MemoryAddress.copy(nativeArray.baseAddress(), heapArray.baseAddress(), bytes);
            checker.accept(heapArray.baseAddress());
        }
    }

    @DataProvider(name = "bufferOps")
    public static Object[][] bufferOps() throws Throwable {
        return new Object[][]{
                { (Function<ByteBuffer, Buffer>) bb -> bb, bufferMembers(ByteBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asCharBuffer, bufferMembers(CharBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asShortBuffer, bufferMembers(ShortBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asIntBuffer, bufferMembers(IntBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asFloatBuffer, bufferMembers(FloatBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asLongBuffer, bufferMembers(LongBuffer.class)},
                { (Function<ByteBuffer, Buffer>) ByteBuffer::asDoubleBuffer, bufferMembers(DoubleBuffer.class)},
        };
    }

    static Map<Method, Object[]> bufferMembers(Class<?> bufferClass) {
        Map<Method, Object[]> members = new HashMap<>();
        for (Method m : bufferClass.getMethods()) {
            //skip statics and method declared in j.l.Object
            if (m.getDeclaringClass().equals(Object.class) ||
                    (m.getModifiers() & Modifier.STATIC) != 0) continue;
            Object[] args = Stream.of(m.getParameterTypes())
                    .map(TestByteBuffer::defaultValue)
                    .toArray();
            members.put(m, args);
        }
        return members;
    }

    @DataProvider(name = "resizeOps")
    public Object[][] resizeOps() {
        Consumer<MemoryAddress> byteInitializer =
                (base) -> initBytes(base, bytes, (addr, pos) -> byteHandle.set(addr, pos, (byte)(long)pos));
        Consumer<MemoryAddress> charInitializer =
                (base) -> initBytes(base, chars, (addr, pos) -> charHandle.set(addr, pos, (char)(long)pos));
        Consumer<MemoryAddress> shortInitializer =
                (base) -> initBytes(base, shorts, (addr, pos) -> shortHandle.set(addr, pos, (short)(long)pos));
        Consumer<MemoryAddress> intInitializer =
                (base) -> initBytes(base, ints, (addr, pos) -> intHandle.set(addr, pos, (int)(long)pos));
        Consumer<MemoryAddress> floatInitializer =
                (base) -> initBytes(base, floats, (addr, pos) -> floatHandle.set(addr, pos, (float)(long)pos));
        Consumer<MemoryAddress> longInitializer =
                (base) -> initBytes(base, longs, (addr, pos) -> longHandle.set(addr, pos, (long)pos));
        Consumer<MemoryAddress> doubleInitializer =
                (base) -> initBytes(base, doubles, (addr, pos) -> doubleHandle.set(addr, pos, (double)(long)pos));

        Consumer<MemoryAddress> byteChecker =
                (base) -> checkBytes(base, bytes, Function.identity(), byteHandle::get, ByteBuffer::get);
        Consumer<MemoryAddress> charChecker =
                (base) -> checkBytes(base, chars, ByteBuffer::asCharBuffer, charHandle::get, CharBuffer::get);
        Consumer<MemoryAddress> shortChecker =
                (base) -> checkBytes(base, shorts, ByteBuffer::asShortBuffer, shortHandle::get, ShortBuffer::get);
        Consumer<MemoryAddress> intChecker =
                (base) -> checkBytes(base, ints, ByteBuffer::asIntBuffer, intHandle::get, IntBuffer::get);
        Consumer<MemoryAddress> floatChecker =
                (base) -> checkBytes(base, floats, ByteBuffer::asFloatBuffer, floatHandle::get, FloatBuffer::get);
        Consumer<MemoryAddress> longChecker =
                (base) -> checkBytes(base, longs, ByteBuffer::asLongBuffer, longHandle::get, LongBuffer::get);
        Consumer<MemoryAddress> doubleChecker =
                (base) -> checkBytes(base, doubles, ByteBuffer::asDoubleBuffer, doubleHandle::get, DoubleBuffer::get);

        return new Object[][]{
                {byteChecker, byteInitializer, bytes},
                {charChecker, charInitializer, chars},
                {shortChecker, shortInitializer, shorts},
                {intChecker, intInitializer, ints},
                {floatChecker, floatInitializer, floats},
                {longChecker, longInitializer, longs},
                {doubleChecker, doubleInitializer, doubles}
        };
    }

    static Object defaultValue(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == char.class) {
                return (char)0;
            } else if (c == boolean.class) {
                return false;
            } else if (c == byte.class) {
                return (byte)0;
            } else if (c == short.class) {
                return (short)0;
            } else if (c == int.class) {
                return 0;
            } else if (c == long.class) {
                return 0L;
            } else if (c == float.class) {
                return 0f;
            } else if (c == double.class) {
                return 0d;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return null;
        }
    }
}
