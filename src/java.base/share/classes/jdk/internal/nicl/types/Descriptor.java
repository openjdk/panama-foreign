/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.nicl.types;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static jdk.internal.nicl.types.Type.*;

/**
 * Native Type Descriptor
 */
public final class Descriptor {
    final char[] descriptor;

    public static final char NATIVE_ENDIAN = '@';
    public static final char BIG_ENDIAN = '>';
    public static final char LITTLE_ENDIAN = '<';
    public static final char SIZE_SELECTOR = '=';
    public static final char FACE_SELECTOR = '$';
    public static final char BREAKDOWN = ':';

    public Descriptor(String v) {
        descriptor = v.toCharArray();
    }

    public static class InvalidDescriptorException extends RuntimeException {
        final char[] descriptor;
        final int index;

        private final static long serialVersionUID = -5877088182715249087L;

        InvalidDescriptorException(char[] desc, int idx, String msg) {
            super(msg);
            descriptor = desc;
            index = idx;
        }

        @Override
        public String getMessage() {
            return super.getMessage() + ": " + diagnosis();
        }

        public String diagnosis() {
            if (index < 0 || index > descriptor.length) {
                throw new IndexOutOfBoundsException();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(descriptor, 0, index);
            sb.append("_");
            if (index < descriptor.length) {
                sb.append(descriptor, index, descriptor.length - index);
            }
            return sb.toString();
        }
    }

    private interface TypeBuilder<T extends Type> extends Type {
        void add(Type t);

        T build();

        @Override
        default long getSize() {
            throw new UnsupportedOperationException("Type builder has no size");
        }
    }

    public static class FunctionBuilder implements TypeBuilder<Function> {
        final Deque<Type> arguments = new LinkedList<>();
        boolean isVarArg = false;

        public FunctionBuilder() {
        }

        FunctionBuilder setVarArg() {
            isVarArg = true;
            return this;
        }

        @Override
        public void add(Type t) {
            arguments.add(t);
        }

        @Override
        public Function build() {
            Type rt = arguments.removeLast();
            return new Function(arguments.toArray(new Type[0]), rt, isVarArg);
        }

    }

    private class ContainerBuilder implements TypeBuilder<Type> {
        boolean isUnion;
        final int array_size;
        final Endianness endianness;
        final Deque<Type> members;

        ContainerBuilder(int occurrence, Endianness endianness) {
            isUnion = false;
            array_size = occurrence;
            this.endianness = endianness;
            members = new LinkedList<>();
        }

        ContainerBuilder asUnion() {
            isUnion = true;
            return this;
        }

        Endianness getEndianness() {
            return endianness;
        }

        @Override
        public void add(Type t) {
            members.add(t);
        }

        @Override
        public Type build() {
            if (members.isEmpty()) {
                throw new IllegalStateException();
            }
            Container c = new Container(isUnion, members.toArray(new Type[0]));
            return (array_size > 1) ? new Array(c, array_size) : c;
        }
    }

    private class BitFieldsBuilder implements TypeBuilder<BitFields> {
        Scalar storage;
        Deque<Integer> fieldBits;
        int bitsTotal;

        BitFieldsBuilder(Scalar storage) {
            this.storage = storage;
            fieldBits = new LinkedList<>();
        }

        BitFields.BitField addField(int bits) {
            fieldBits.add(bits);
            BitFields.BitField rv = new BitFields.BitField(storage, bitsTotal, bits);
            bitsTotal += bits;
            return rv;
        }

        @Override
        public void add(Type t) {
            // do nothing
            // addField should have been called instead to create BitField
        }

        @Override
        public BitFields build() {
            if (fieldBits.isEmpty()) {
                throw new IllegalStateException();
            }
            int[] fields = new int[fieldBits.size()];
            int i = 0;
            for (int bits: fieldBits) {
                fields[i++] = bits;
            }
            return new BitFields(storage, fields);
        }
    }

    /**
     * An iterator takes a descriptor string and iterate through the top level of members
     */
    private class TypeIterator implements Iterator<Type> {
        int current;
        final Deque<TypeBuilder<? extends Type>> levels;
        final boolean flatten;
        BitFieldsBuilder bfb;
        Type latch;

        TypeIterator(boolean flatten) {
            current = 0;
            levels = new LinkedList<>();
            this.flatten = flatten;
            // get rid of leading whitespaces
            skipWhitespaces();
        }

        @SuppressWarnings("unchecked")
        private <T extends TypeBuilder<?>> T getTopBuilder(Class<T> clz) {
            TypeBuilder<?> tb = levels.peek();
            return (tb != null && clz.isInstance(tb)) ? (T) tb : null;
        }

        /**
         * Close current BitFields if any. The BitFields will be stored in the latch.
         * @return true if a BitFields is active and closed, false otherwise
         */
        private boolean concludeBitFields() {
            if (bfb == null) {
                return false;
            }

            TypeBuilder<?> tb = levels.pop();
            assert(bfb == tb);
            assert(latch == null);
            try {
                latch = bfb.build();
                bfb = null;

                // Add this BitFields to owner
                if (levels.peek() != null) {
                    levels.peek().add(latch);
                }

                return true;
            } catch (IllegalStateException ex) {
                throw new InvalidDescriptorException(descriptor, current,
                        "Bitfields must have at least one field definition");
            }
        }

        private boolean skipWhitespaces() {
            while (current < descriptor.length) {
                char ch = descriptor[current];
                if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
                    // move past comment so that we can have more precise hasNext()
                    // for tail comment
                    return (ch == '#') ? ignoreComment() : true;
                }
                current++;
            }
            return false;
        }

        private boolean ignoreComment() {
            assert(descriptor[current] == '#');
            current++;
            while (current < descriptor.length) {
                char ch = descriptor[current];
                if (ch == '\n' || ch == '\r') {
                    break;
                }
                current++;
            }
            // Remove leading WS.
            // The loop between skipWhitespaces and ignoreComment is upsetting
            // Would be nice to use goto or have tail call elimination
            return skipWhitespaces();
        }

        private char nextChar(boolean skipWS) {
            current++;

            if (skipWS) {
                skipWhitespaces();
            }
            if (current >= descriptor.length) {
                throw new InvalidDescriptorException(descriptor, current, "More characters expected");
            }
            char ch = descriptor[current];
            if (ch < ' ' || ch > '\u007E') {
                throw new InvalidDescriptorException(descriptor, current, "Invalid character");
            }
            return ch;
        }

        /**
         * Get the repetition count of a type descriptor
         * @return The count
         */
        private int getCount() {
            int count = 0;
            boolean specified = false;
            for (char ch = descriptor[current]; ch >= '0' && ch <= '9'; ch = nextChar(false)) {
                count = count * 10 + (ch - '0');
                specified = true;
            }
            if (specified && count == 0) {
                // 1 is acceptable but ineffective, 1 should only be used with bitfield for clarity.
                throw new InvalidDescriptorException(descriptor, current, "Count should be greater than 1");
            }
            return count;
        }

        /**
         * Return the type after size selector
         * @param endianness The current endianness setting
         * @return The type
         */
        private Type sizedType(Endianness endianness) {
            assert(descriptor[current] == '=');
            // skip '='
            nextChar(false);
            int size = getCount();
            // multiple of 8
            assert((size & 7) == 0);
            char ch = descriptor[current++];
            if (size == 0) {
                switch (ch) {
                    case 'o':
                        return new Scalar('i', endianness, 8);
                    case 's':
                        return new Scalar('i', endianness, 16);
                    case 'l':
                        return new Scalar('i', endianness, 32);
                    case 'q':
                        return new Scalar('i', endianness, 64);
                    case 'O':
                        return new Scalar('I', endianness, 8);
                    case 'S':
                        return new Scalar('I', endianness, 16);
                    case 'L':
                        return new Scalar('I', endianness, 32);
                    case 'Q':
                        return new Scalar('I', endianness, 64);
                    default:
                        throw new InvalidDescriptorException(descriptor, current - 1, "Invalid standard size type");
                }
            }
            switch (ch) {
                case 'v':
                case 'i':
                case 'f':
                case 'I':
                case 'F':
                    return new Scalar(ch, endianness, size);
                default:
                    throw new InvalidDescriptorException(descriptor, current - 1, "Type not support size specifier");
            }
        }

        private Pointer pointerType() {
            assert(descriptor[current] == 'p');
            current++;
            if (current < descriptor.length && descriptor[current] == BREAKDOWN) {
                final int index = current;
                current++;
                try {
                    Type pointee = nextCompleteType();
                    return new Pointer(pointee);
                } catch (NoSuchElementException ex) {
                    throw new InvalidDescriptorException(descriptor, index, "Missing pointee type");
                }
            }
            return new Pointer(null);
        }

        private Type scalarType(Endianness endianness) {
            final char ch = descriptor[current];
            current++;
            String intTypes = "OSILQosilq";
            String validTypes = "cBVxfdeFDE";

            Scalar rv;
            if (intTypes.indexOf(ch) != -1) {
                rv = new Scalar(ch, endianness);
                if (current < descriptor.length && descriptor[current] == BREAKDOWN) {
                    current++;
                    bfb = new BitFieldsBuilder(rv);
                    levels.push(bfb);
                }
            } else if (validTypes.indexOf(ch) != -1) {
                rv = new Scalar(ch, endianness);
                if (current < descriptor.length && descriptor[current] == BREAKDOWN) {
                    throw new InvalidDescriptorException(descriptor, current,
                            "Only integer type can be bit-field storage");
                }
            } else {
                throw new InvalidDescriptorException(descriptor, current - 1, "Invalid type code encountered:" + ch);
            }
            return rv;
        }

        private Type functionType(boolean isVarArg) {
            assert(descriptor[current] == ')');
            BitFields bf = null;
            // Temporarily clean latch so we can retrieve return type of function
            if (concludeBitFields()) {
                bf = (BitFields) latch;
                latch = null;
            }
            FunctionBuilder builder = getTopBuilder(FunctionBuilder.class);
            if (builder == null) {
                throw new InvalidDescriptorException(descriptor, current, "Unmatched ')'");
            }
            levels.pop();

            final int index = current;
            current++;
            skipWhitespaces();

            try {
                // important that BitFields not in the latch when we get next type,
                // meaning latch should be null
                builder.add(nextCompleteType());
            } catch (NoSuchElementException ex) {
                throw new InvalidDescriptorException(descriptor, index, "Function must have a return type, 'V' for void");
            }

            if (bf != null) {
                // put back BitFields to the latch
                latch = bf;
            }

            if (isVarArg) {
                builder.setVarArg();
            }
            return builder.build();
        }

        private Type containerType() {
            assert(descriptor[current] == ']');
            concludeBitFields();
            ContainerBuilder cb = getTopBuilder(ContainerBuilder.class);
            if (cb == null) {
                throw new InvalidDescriptorException(descriptor, current, "Unmatched ']'");
            }
            levels.pop();

            current++;
            skipWhitespaces();
            try {
                return cb.build();
            } catch (IllegalStateException ex) {
                throw new InvalidDescriptorException(descriptor, current,
                        "Container type should have at least one member");
            }
        }

        private Type wrapup(Type t, int occurrence) {
            Type rv = (occurrence == 1) ? t : new Array(t, occurrence);
            TypeBuilder<?> tb = levels.peek();
            if (null != tb) {
                tb.add(rv);
            }

            // BitFields concluded
            if (latch != null) {
                assert(latch instanceof BitFields);
                Type tmp = rv;
                rv = latch;
                latch = tmp;
            }

            skipWhitespaces();
            return rv;
        }

        private Type nextType() {
            if (latch != null) {
                if (latch instanceof TypeBuilder) {
                    // For '[', we pushed ContainerBuilder to the latch if BitFields
                    // is the type before it. This is to ensure next() will have the
                    // BitFields reported at the right level.
                    @SuppressWarnings("unchecked")
                    TypeBuilder<?> tb = (TypeBuilder<?>) latch;
                    levels.push(tb);
                    latch = null;
                } else {
                    Type rv = latch;
                    latch = null;
                    return rv;
                }
            }

            // All elements before current should have been iterated
            assert(latch == null);
            if (current >= descriptor.length) {
                // end of layout, check if last element is BitField
                if (bfb != null) {
                    levels.pop();
                    try {
                        BitFields tmp = bfb.build();
                        bfb = null;
                        return tmp;
                    } catch (IllegalStateException ex) {
                        throw new InvalidDescriptorException(descriptor, descriptor.length,
                                "Bitfields must have at least one field definition");
                    }
                }
                // all nested element should have been closed
                if (! levels.isEmpty()) {
                    throw new InvalidDescriptorException(descriptor, current, "More characters expected");
                }
                // no more elements
                throw new NoSuchElementException();
            }

            char ch = descriptor[current];
            // make sure only use printable ASCII code
            if (ch < ' ' || ch > '\u007E') {
                throw new InvalidDescriptorException(descriptor, current, "Invalid character " + ch);
            }

            if (ch == '#') {
                // really should never reached here
                assert false : "Comment should have been skipped with whitespaces";
                ignoreComment();
                // star over, this maybe end of it.
                return nextType();
            }

            // if this is a function descriptor
            if (ch == '(') {
                if (concludeBitFields()) {
                    // Report the BitFields in the latch
                    // Keep current on '(' so we pick up where we left
                    return nextType();
                }
                levels.push(new FunctionBuilder());
                ch = nextChar(true);
            }

            if (ch == ')') {
                // Function won't be an array or a member in struct/union
                // But we still need to make sure BitFields is concluded
                return wrapup(functionType(false), 1);
            } else if (ch == ']') {
                return wrapup(containerType(), 1);
            } else if (ch == '|') {
                if (concludeBitFields()) {
                    return nextType();
                }
                ContainerBuilder cb = getTopBuilder(ContainerBuilder.class);
                if (cb == null) {
                    throw new InvalidDescriptorException(descriptor, current, "Union must be enclosed in '['");
                } else {
                    cb.asUnion();
                }
                ch = nextChar(true);
            }

            int occurrence = 1;
            // get occurrence
            if (ch == '*') {
                occurrence = -1;
                ch = nextChar(false);
                if (ch == ')') {
                    return wrapup(functionType(true), 1);
                }
            } else if (ch >= '0' && ch <= '9') {
                occurrence = getCount();
                if (occurrence < 1) {
                    throw new InvalidDescriptorException(descriptor, current, "Occurrence must be greater than 0");
                }
                ch = descriptor[current];
            }

            if (ch == 'b') {
                if (bfb == null) {
                    throw new InvalidDescriptorException(descriptor, current,
                            "Bit-field must be within storage integer");
                }
                if (occurrence <= 0) {
                    throw new InvalidDescriptorException(descriptor, current, "Bit-field must have at least 1 bit");
                }
                current++;
                skipWhitespaces();
                return bfb.addField(occurrence);
            } else {
                concludeBitFields();
            }

            ContainerBuilder cb = getTopBuilder(ContainerBuilder.class);
            Type.Endianness endianness = (cb == null) ? Endianness.NATIVE : cb.getEndianness();
            // get Endianness of type other than a pointer
            switch (ch) {
                case 'p':
                    return wrapup(pointerType(), occurrence);
                case 'B':
                case 'V':
                case 'c':
                case 'x':
                    return wrapup(scalarType(Endianness.NATIVE), occurrence);
                case BIG_ENDIAN:
                    endianness = Type.Endianness.BIG;
                    ch = nextChar(false);
                    break;
                case LITTLE_ENDIAN:
                    endianness = Type.Endianness.LITTLE;
                    ch = nextChar(false);
                    break;
                case NATIVE_ENDIAN:
                    endianness = Type.Endianness.NATIVE;
                    ch = nextChar(false);
                    break;
            }

            switch (ch) {
                case '[':
                    current++;
                    if (latch != null) {
                        Type bf = wrapup(latch, 1);
                        latch = new ContainerBuilder(occurrence, endianness);
                        assert(bf instanceof BitFields);
                        return bf;
                    } else {
                        levels.push(new ContainerBuilder(occurrence, endianness));
                        return nextType();
                    }
                case SIZE_SELECTOR:
                    return wrapup(sizedType(endianness), occurrence);
                case FACE_SELECTOR:
                    throw new InvalidDescriptorException(descriptor, current, "Reserved face selector");
                default:
                    return wrapup(scalarType(endianness), occurrence);
            }
        }

        private Type nextCompleteType() {
            TypeBuilder<Type> nop = new TypeBuilder<>() {
                @Override
                public void add(Type t) {}
                @Override
                public Type build() { return null; }
            };
            // capture the type to avoid leak to parent
            // also prevents unexpected close of parent
            levels.push(nop);
            int level_now = levels.size();
            // Make sure we get through nested elements
            Type type = nextType();
            while (levels.size() > level_now) {
                type = nextType();
            }
            levels.pop();
            return type;
        }

        @Override
        public boolean hasNext() {
            if (latch != null) {
                return true;
            }
            if (current < descriptor.length) {
                return true;
            }
            // Running out, make sure descriptor is complete
            return !levels.isEmpty();
        }

        @Override
        public Type next() {
            Type t = nextType();
            if (!flatten) {
                while (!levels.isEmpty()) {
                    t = nextType();
                }
                // bitfields as last argument or member
                if (latch != null) {
                    if (latch instanceof Function || latch instanceof Container) {
                        t = nextType();
                    }
                }
            }
            return t;
        }
    }

    public Stream<Type> types() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TypeIterator(false),
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    public Stream<Type> elements() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TypeIterator(true),
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }
}
