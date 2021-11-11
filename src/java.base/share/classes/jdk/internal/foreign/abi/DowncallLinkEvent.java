package jdk.internal.foreign.abi;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name(DowncallLinkEvent.NAME)
@Label("Downcall Handle Linked")
@Category("Foreign Linker")
@Description("Linking of a downcall method handle")
@StackTrace(false)
public class DowncallLinkEvent extends Event {
    static final String NAME = "jdk.incubator.foreign.DowncallLink";

    @Label("Function Descriptor")
    public String functionDescriptor;

    @Label("Inferred Method Type")
    public String inferredMethodType;

    @Label("Result Method Type")
    public String resultMethodType;
}
