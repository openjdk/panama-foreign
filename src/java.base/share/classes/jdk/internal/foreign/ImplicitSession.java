package jdk.internal.foreign;

import sun.nio.ch.DirectBuffer;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;

/**
 * This is an implicit, GC-backed memory session. Implicit sessions cannot be closed explicitly.
 * While it would be possible to model an implicit session as a non-closeable view of a shared
 * session, it is better to capture the fact that an implicit session is not just a non-closeable
 * view of some session which might be closeable. This is useful e.g. in the implementations of
 * {@link DirectBuffer#address()}, where obtaining an address of a buffer instance associated
 * with a potentially closeable session is forbidden.
 */
final class ImplicitSession extends SharedSession {

    public ImplicitSession(Cleaner cleaner) {
        super();
        cleaner.register(this, resourceList);
    }

    @Override
    public void release0() {
        Reference.reachabilityFence(this);
    }

    @Override
    public void acquire0() {
        // do nothing
    }

    @Override
    public boolean isCloseable() {
        return false;
    }

    @Override
    public void justClose() {
        throw nonCloseable();
    }
}
