package java.lang.foreign;

import jdk.internal.foreign.layout.ValueLayouts;
import jdk.internal.javac.PreviewFeature;
import jdk.internal.reflect.CallerSensitive;

import java.lang.foreign.Linker.Option;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.util.Optional;

/**
 * A value layout used to model the address of some region of memory. The carrier associated with an address layout is
 * {@code MemorySegment.class}. The size and alignment of an address layout are platform dependent
 * (e.g. on a 64-bit platform, the size and alignment of an address layout are set to 64 bits).
 * <p>
 * An address layout may optionally feature a {@linkplain #targetLayout() target layout}. The target layout of an address
 * layout is used to model the layout of the region of memory whose address is described by that address layout.
 * For instance, if an address layout has target layout {@link ValueLayout#JAVA_INT}, the region of memory pointed to by the address
 * described by the address layout is 4 bytes long. Specifying a target layout can be useful in the following situations:
 * <ul>
 *     <li>When accessing a memory segment that has been obtained by reading an address from another
 *     memory segment, e.g. using {@link MemorySegment#getAtIndex(AddressLayout, long)};</li>
 *     <li>When creating a downcall method handle, using {@link Linker#downcallHandle(FunctionDescriptor, Option...)};
 *     <li>When creating an upcall stub, using {@link Linker#upcallStub(MethodHandle, FunctionDescriptor, Arena, Option...)}.
 * </ul>
 *
 * @see #ADDRESS
 * @see #ADDRESS_UNALIGNED
 * @since 19
 */
@PreviewFeature(feature = PreviewFeature.Feature.FOREIGN)
sealed public interface AddressLayout extends ValueLayout permits ValueLayouts.OfAddressImpl {

    /**
     * {@inheritDoc}
     */
    @Override
    AddressLayout withName(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    AddressLayout withBitAlignment(long bitAlignment);

    /**
     * {@inheritDoc}
     */
    @Override
    AddressLayout withOrder(ByteOrder order);

    /**
     * Returns an address layout with the same carrier, alignment constraint, name and order as this address layout,
     * but associated with the specified target layout. The returned address layout allows raw addresses to be accessed
     * as {@linkplain MemorySegment memory segments} whose size is set to the size of the specified layout. Moreover,
     * if the accessed raw address is not compatible with the alignment constraint in the provided layout,
     * {@linkplain IllegalArgumentException} will be thrown.
     * @apiNote
     * This method can also be used to create an address layout which, when used, creates native memory
     * segments with maximal size (e.g. {@linkplain Long#MAX_VALUE}. This can be done by using a target sequence
     * layout with unspecified size, as follows:
     * {@snippet lang = java:
     * AddressLayout addressLayout   = ...
     * AddressLayout unboundedLayout = addressLayout.withTargetLayout(
     *         MemoryLayout.sequenceLayout(ValueLayout.JAVA_BYTE));
     *}
     * <p>
     * This method is <a href="package-summary.html#restricted"><em>restricted</em></a>.
     * Restricted methods are unsafe, and, if used incorrectly, their use might crash
     * the JVM or, worse, silently result in memory corruption. Thus, clients should refrain from depending on
     * restricted methods, and use safe and supported functionalities, where possible.
     *
     * @param layout the target layout.
     * @return an address layout with same characteristics as this layout, but with the provided target layout.
     * @throws IllegalCallerException If the caller is in a module that does not have native access enabled.
     * @see #targetLayout()
     */
    @CallerSensitive
    AddressLayout withTargetLayout(MemoryLayout layout);

    /**
     * {@return the target layout associated with this address layout (if any)}.
     */
    Optional<MemoryLayout> targetLayout();

}
