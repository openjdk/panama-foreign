package com.sun.tools.jextract;

import java.foreign.memory.Callback;

public class CallbackType implements JType {

    final JType referenced;

    CallbackType(JType type) {
        referenced = type;
    }

    @Override
    public String getDescriptor() {
        return JType.of(Callback.class).getDescriptor();
    }

    public String getSignature(boolean useWildcard) {
        StringBuilder sb = new StringBuilder();
        sb.append("L");
        sb.append(Callback.class.getName().replace('.', '/'));
        sb.append("<");
        JType pt = referenced;
        if (pt instanceof JType2) {
            pt = ((JType2) pt).getDelegate();
        }
        if (pt instanceof TypeAlias) {
            pt = ((TypeAlias) pt).canonicalType();
        }
        if (pt == JType.Void && useWildcard) {
            sb.append("*");
        } else {
            sb.append(JType.boxing(pt));
        }
        sb.append(">;");
        return sb.toString();
    }

    @Override
    public String getSignature() {
        return getSignature(false);
    }

    public JType getFuncInterface() {
        return referenced;
    }
}
