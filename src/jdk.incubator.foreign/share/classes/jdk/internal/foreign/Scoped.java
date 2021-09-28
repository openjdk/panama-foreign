package jdk.internal.foreign;

import jdk.incubator.foreign.ResourceScope;

public interface Scoped {
    ResourceScope scope();
}
