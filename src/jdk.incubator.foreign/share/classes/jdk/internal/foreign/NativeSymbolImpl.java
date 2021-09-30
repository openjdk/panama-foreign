package jdk.internal.foreign;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.NativeSymbol;
import jdk.incubator.foreign.ResourceScope;

public record NativeSymbolImpl(String name, MemoryAddress address, ResourceScope scope) implements NativeSymbol, Scoped { }
