package jdk.internal.foreign.abi;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name(UpcallLinkEvent.NAME)
@Label("Upcall Stub Linked")
@Category("Foreign Linker")
@Description("Linking of an upcall stub")
@StackTrace(false)
public class UpcallLinkEvent extends Event {
    static final String NAME = "jdk.incubator.foreign.UpcallLink";

    @Label("Function Descriptor")
    public String functionDescriptor;

    @Label("Inferred Method Type")
    public String methodType;
}
