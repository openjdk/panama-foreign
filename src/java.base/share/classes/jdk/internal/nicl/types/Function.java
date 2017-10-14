package jdk.internal.nicl.types;

import java.util.Arrays;
import java.util.stream.Stream;

public class Function implements Type {
    final Type[] arguments;
    final Type returnType;
    final boolean isVarArg;

    public Function(Type[] args, Type returnType, boolean isVarArg) {
        this.arguments = args;
        this.returnType = returnType;
        this.isVarArg = isVarArg;
    }

    public boolean isVarArg() {
        return isVarArg;
    }

    public Stream<Type> arguments() {
        return Stream.of(arguments);
    }

    public int argumentCount() {
        return arguments.length;
    }

    public Type getArgumentType(int index) {
        return arguments[index];
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException("Function type has no size");
    }

    @Override
    public int hashCode() {
        return (returnType.hashCode() * 31 + Arrays.hashCode(arguments)) << 1 + (isVarArg ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Function)) {
            return false;
        }
        jdk.internal.nicl.types.Function other = (jdk.internal.nicl.types.Function) o;
        if (!other.returnType.equals(returnType)) {
            return false;
        }
        if (other.isVarArg != isVarArg) {
            return false;
        }
        if (other.arguments.length != arguments.length) {
            return false;
        }
        for (int i = 0; i < arguments.length; i++) {
            if (!arguments[i].equals(other.arguments[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        for (Type arg : arguments) {
            sb.append(arg.toString());
        }
        if (isVarArg) {
            sb.append('*');
        }
        sb.append(')');
        sb.append(returnType.toString());
        return sb.toString();
    }
}
