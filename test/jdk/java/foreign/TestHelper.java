import java.foreign.Libraries;
import java.foreign.annotations.NativeAddressof;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeGetter;
import java.foreign.annotations.NativeHeader;
import java.foreign.annotations.NativeSetter;
import java.foreign.annotations.NativeStruct;
import java.foreign.memory.Pointer;
import java.foreign.memory.Struct;
import java.lang.invoke.MethodHandles;

@NativeHeader(libraries={ "TestHelpers" })
public interface TestHelper {
    @NativeStruct("${dot}")
    interface OpaquePoint extends Struct<OpaquePoint> {}

    @NativeStruct("[i32(x)i32(y)u64(data):u8](point)")
    interface Point extends Struct<Point> {
        @NativeGetter("x")
        int x$get();
        @NativeSetter("x")
        void x$set(int x);
        @NativeAddressof("x")
        Pointer<Integer> x$ptr();

        @NativeGetter("y")
        int y$get();
        @NativeSetter("y")
        void y$set(int y);
        @NativeAddressof("y")
        Pointer<java.lang.Integer> y$ptr();

        @NativeGetter("data")
        Pointer<java.lang.Byte> data$get();
        @NativeSetter("data")
        void data$set(Pointer<Byte> data);
        @NativeAddressof("data")
        Pointer<Pointer<Byte>> data$ptr();
    }

    @NativeFunction("(u64:${dot}i32i32)v")
    void setCoordination(Pointer<OpaquePoint> dot, int x, int y);

    @NativeFunction("(u64:${dot})i32")
    int getDotX(Pointer<OpaquePoint> dot);

    @NativeFunction("(u64:${dot})i32")
    int getDotY(Pointer<OpaquePoint> dot);

    @NativeFunction("()u64:${dot}")
    Pointer<OpaquePoint> allocateDot();

    @NativeFunction("(u64:${dot})v")
    void freeDot(Pointer<OpaquePoint> dot);

    @NativeFunction("(i32)u64:${point}")
    Pointer<Point> allocateDotArray(int number);

    @NativeFunction("(u64:${point})v")
    void freeDotArray(Pointer<Point> array);

    @NativeFunction("(i32u64:u64:${dot})v")
    void allocateDots(int number, Pointer<? extends Pointer<OpaquePoint>> dots);

    @NativeFunction("(i32u64:u64:${dot})v")
    void freeDots(int number, Pointer<? extends Pointer<OpaquePoint>> dots);

    @NativeFunction("(i32i32u64:u8)${point}")
    Point getDot(int x, int y, Pointer<?> dot);

    final static TestHelper lib = Libraries.bind(MethodHandles.lookup(), TestHelper.class);
}
