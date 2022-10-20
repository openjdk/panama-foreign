package jdk.internal.foreign;

import jdk.internal.vm.annotation.ForceInline;

/**
 * The global, non-closeable, shared session. Similar to a shared session, but its {@link #close()} method throws unconditionally.
 * Adding new resources to the global session, does nothing: as the session can never become not-alive, there is nothing to track.
 * Acquiring and or releasing a memory session similarly does nothing.
 */
final class GlobalSession extends MemorySessionImpl {

    final Object ref;

    public GlobalSession(Object ref) {
        super(null, null);
        this.ref = ref;
    }

    @Override
    @ForceInline
    public void release0() {
        // do nothing
    }

    @Override
    public boolean isCloseable() {
        return false;
    }

    @Override
    @ForceInline
    public void acquire0() {
        // do nothing
    }

    @Override
    void addInternal(ResourceList.ResourceCleanup resource) {
        // do nothing
    }

    @Override
    public void justClose() {
        throw nonCloseable();
    }
}
