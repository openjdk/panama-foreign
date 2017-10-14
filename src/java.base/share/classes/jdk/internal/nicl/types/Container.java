package jdk.internal.nicl.types;

import java.util.Arrays;
import java.util.stream.Stream;
import jdk.internal.nicl.Platform;

public class Container implements Type {
    final Type[] members;
    final boolean isUnion;

    public Container(boolean isUnion, Type... members) {
        this.members = members;
        this.isUnion = isUnion;
    }

    @Override
    public long getSize() {
        return Platform.getInstance().getABI().sizeof(this);
    }

    public boolean isUnion() {
        return isUnion;
    }

    public Stream<Type> getMembers() {
        return Stream.of(members);
    }

    public int memberCount() {
        return members.length;
    }

    public Type getMember(int index) {
        return members[index];
    }

    @Override
    public int hashCode() {
        return (isUnion ? 0x40000000 : 0x60000000) | Arrays.hashCode(members);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof jdk.internal.nicl.types.Container)) {
            return false;
        }

        jdk.internal.nicl.types.Container other = (jdk.internal.nicl.types.Container) o;
        if (other.isUnion != isUnion) {
            return false;
        }
        if (other.members.length != members.length) {
            return false;
        }
        for (int i = 0; i < members.length; i++) {
            if (!members[i].equals(other.members[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (Type t : members) {
            sb.append(t);
            if (isUnion) {
                sb.append('|');
            }
        }
        if (isUnion) {
            sb.setCharAt(sb.length() - 1, ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}
