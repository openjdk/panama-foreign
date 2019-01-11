package jdk.internal.foreign.abi.x64;

public class SharedConstants {
    public static final int INTEGER_REGISTER_SIZE = 8;
    public static final int VECTOR_REGISTER_SIZE = 64; // (maximum) vector size is 512 bits
    public static final int X87_REGISTER_SIZE = 16; // x87 register is 128 bits

    public static final int STACK_SLOT_SIZE = 8;
}
