/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.nicl.types.DescriptorParser.DescriptorScanner.Token;

import java.nicl.layout.Address;
import java.nicl.layout.Sequence;
import java.nicl.layout.Function;
import java.nicl.layout.Group;
import java.nicl.layout.Layout;
import java.nicl.layout.Value;
import java.nicl.layout.Value.Endianness;

import java.nicl.layout.Value.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Parse a layout string into a descriptor type {@see Type}.
 */
public class DescriptorParser {

    private final DescriptorScanner scanner;
    private Token token;
    private Optional<Endianness> endianness = Optional.empty();

    public DescriptorParser(String desc) {
        scanner = new DescriptorScanner(desc);
        nextToken();
    }

    void nextToken() {
        token = scanner.next();
    }

    /**
     * layout = 1*elementType / functionType
     */
    public Stream<? extends Object> parseDescriptorOrLayouts() {
        if (token == Token.LPAREN) {
            return Stream.of(parseFunction());
        } else {
            return parseLayout();
        }
    }

    public Stream<Layout> parseLayout() {
        List<Layout> types = new ArrayList<>();
        loop: while (true) {
            switch (token) {
                case END: break loop;
                default:
                    types.add(parseElementType());
            }
        }
        if (types.size() == 0) {
            throw scanner.error("At least one element type should be present in a layout string");
        }
        return types.stream();
    }

    /**
     * functionType = '(' *elementType ['*'] ')' elementType
     */
    private Function parseFunction() {
        nextToken(); // LPAREN
        boolean varargs = false;
        List<Layout> params = new ArrayList<>();
        while (token != Token.RPAREN &&
                (token != Token.ANY_SIZE || scanner.peek() != Token.RPAREN)) {
            params.add(parseElementType());
        }
        if (token == Token.ANY_SIZE) {
            varargs = true;
            nextToken(); // ANY_SIZE
        }
        nextToken(); //RPAREN
        if (token == Token.VOID) {
            nextToken();
            return Function.ofVoid(varargs, params.toArray(new Layout[0]));
        } else {
            return Function.of(parseElementType(), varargs, params.toArray(new Layout[0]));
        }
    }

    /**
     * elementType = *separator (scalarType / arrayType / pointerType / containerType) *separator
     */
    private Layout parseElementType() {
        skipSpacesOrComments();
        Optional<Endianness> prevEndianness = endianness;
        try {
            endianness = endiannessOpt();
            switch (token) {
                case LBRACE:
                    return parseContainer();
                case NUMERIC:
                case ANY_SIZE:
                    if (endianness.isPresent()) {
                        scanner.error("Unexpected endianness annotation");
                    }
                    return parseArray();
                case STANDARD_SIZED_SCALAR: case EXPLICIT_SIZED_SCALAR:
                case OTHER_SCALAR: case SIZE_SELECTOR:
                    return parseScalar();
                case POINTER:
                    if (endianness.isPresent()) {
                        scanner.error("Unexpected endianness annotation");
                    }
                    return parsePointer();
                default:
                    throw scanner.error("Unexpected token: " + token);
            }
        } finally {
            endianness = prevEndianness;
            skipSpacesOrComments();
        }
    }

    /**
     * comment = "#" *octet 1*(CR / LF)
     * whitespace = 1*(SP / HTAB / CR / LF)
     * separator = whitespace / comment
     */
    void skipSpacesOrComments() {
        while (token == Token.SEPARATOR) {
            nextToken();
        }
    }

    /**
     * arrayType = arraySize elementType
     */
    Layout parseArray() {
        int occurrences = token == Token.ANY_SIZE ?
                0 : parseNumber(2);
        nextToken(); // size
        Layout elemType = parseElementType();
        return Sequence.of(occurrences, elemType);
    }

    /**
     * pointerType = p [':' descriptor ]
     */
    Layout parsePointer() {
        nextToken();
        if (token == Token.BREAKDOWN) {
            nextToken();
            if (token == Token.LPAREN) {
                return Address.ofFunction(64, parseFunction());
            } else if (token != Token.VOID) {
                return Address.ofLayout(64, parseElementType());
            } else {
                //skip VOID
                nextToken();
            }
        }
        return Address.ofVoid(64);
    }

    /**
     * scalarType = integerType / realType / miscType / bitFields / sizedType
     * integerType = [endianness] (standardSizeType / 'i')
     * realType = [endianness] ('f' / 'd' / 'e')
     * miscType = 'B' / 'V' / 'c' / 'x'
     * sizedType = [endianness] "=" (standardSizeType | number explicitSizeType | vectorType)
     * standardSizeType = 'o' / 's' / 'l' / 'q'
     * explicitSizeType = 'i' / 'f'
     * vectorType = "=" number %x76 ; 'v' for vector
     * bitFields = integerType ':' 1*bitField
     * bitField = [number] 'b'
     */
    Layout parseScalar() {
        Value result;
        Endianness e = endianness.orElse(Endianness.LITTLE_ENDIAN);
        int size;
        char tag;
        if (token == Token.SIZE_SELECTOR) {
            nextToken(); // SIZE_SELECTOR
            switch (token) {
                case NUMERIC:
                    size = parseNumber(1);
                    nextToken();
                    tag = scanner.lastScalarTag();
                    break;
                case STANDARD_SIZED_SCALAR:
                    tag = scanner.lastScalarTag();
                    size = scalarStandardSize(tag, -1);
                    break;
                default:
                    throw scanner.error("Unexpected token: " + token);
            }
        } else {
            tag = scanner.lastScalarTag;
            size = definedSize(tag) * 8;
        }
        switch (scalarKind(tag)) {
            case FLOATING_POINT:
                result = Value.ofFloatingPoint(e, size);
                break;
            case INTEGRAL_SIGNED:
                result = Value.ofSignedInt(e, size);
                break;
            case INTEGRAL_UNSIGNED:
                result = Value.ofUnsignedInt(e, size);
                break;
            default:
                throw new IllegalStateException();
        }
        nextToken(); //scalar tag
        if (token == Token.BREAKDOWN) {
            //bitfield
            result = result.withContents(parseBitFields(result));
        }
        return result;
    }

    private Group parseBitFields(Value scalar) {
        if (!isValidBitfieldKind(scalar.kind())) {
            throw scanner.error("BitFields not supported on type tag '" + scalar.kind() + "'");
        }
        nextToken(); // BREAKDOWN
        List<Integer> fields = new ArrayList<>();
        while ((token == Token.NUMERIC && scanner.peek() == Token.BITFIELD) ||
                token == Token.BITFIELD) {
            if (token == Token.NUMERIC) {
                fields.add(parseNumber(1));
                nextToken(); //NUMERIC
            } else {
                fields.add(1);
            }
            if (token != Token.BITFIELD) {
                throw scanner.error("bit-field separator missing");
            }
            nextToken(); //BITFIELD
            skipSpacesOrComments();
        }
        if (fields.isEmpty()) {
            throw scanner.error("Empty bitfield");
        }
        return Group.struct(fields.stream().map(i -> Value.ofUnsignedInt(i)).toArray(Layout[]::new));
    }

    private int scalarStandardSize(char type, int size) {
        switch (type) {
            case 'o': case 'O':
                return 8;
            case 's': case 'S':
                return 16;
            case 'l': case 'L':
                return 32;
            case 'q': case 'Q':
                return 64;
            default:
                return size;
        }
    }

    private int definedSize(char type) {
        switch (type) {
            case 'c':
            case 'o':
            case 'O':
            case 'x':
            case 'B':
                return 1;
            case 's':
            case 'S':
                return 2;
            case 'i':
            case 'I':
                return 4;
            case 'l':
            case 'L':
            case 'q':
            case 'Q':
                return 8;
            case 'f':
            case 'F':
                return 4;
            case 'd':
            case 'D':
                return 8;
            case 'e':
            case 'E':
                return 16;
            case 'p':
                return 8;
            case 'V':
                return 0;
            default:
                throw new IllegalStateException();
        }
    }

    private Value.Kind scalarKind(char type) {
        switch (type) {
            case 'f': case 'F': case 'd': case 'D': case 'e': case 'E':
                return Kind.FLOATING_POINT;
            case 'c': case 'o': case 's': case 'i': case 'l': case 'q':
                return Kind.INTEGRAL_SIGNED;
            case 'x': case 'B': case 'O': case 'S': case 'I': case 'L': case 'Q':
                return Kind.INTEGRAL_UNSIGNED;
            default:
                throw new IllegalArgumentException("Invalid type descriptor " + type);
        }
    }

    private int parseNumber(int lowerBoundInclusive) {
        int num = scanner.lastNumber(); //this throws if last token is not a number
        if (num < lowerBoundInclusive) {
            throw scanner.error("number outside range");
        }
        return num;
    }

    private boolean isValidBitfieldKind(Value.Kind kind) {
        switch (kind) {
            case INTEGRAL_SIGNED: case INTEGRAL_UNSIGNED:
                return true;
            default:
                return false;
        }
    }

    /**
     * endianness = '>' / '<' / '@'
     */
    @SuppressWarnings("fallthrough")
    Optional<Endianness> endiannessOpt() {
        switch (token) {
            case BIG_ENDIAN:
                nextToken();
                return Optional.of(Endianness.BIG_ENDIAN);
            case LITTLE_ENDIAN:
                nextToken();
                return Optional.of(Endianness.LITTLE_ENDIAN);
            case NATIVE_ENDIAN:
                nextToken();
                return Optional.of(Endianness.LITTLE_ENDIAN);
            default:
                return Optional.empty();
        }
    }

    /**
     * containerType = [endianness] '[' elementType *(elementType / unionMember) ']'
     */
    Layout parseContainer() {
        nextToken(); // LBRACE
        List<Layout> components = new ArrayList<>();
        boolean isUnion = false;
        while (token != Token.RBRACE) {
            components.add(parseElementType());
            if (token == Token.UNION) {
                isUnion = true;
                nextToken();
            }
        }
        if (components.isEmpty()) {
            throw scanner.error("Empty container");
        }
        nextToken(); //RBRACE
        return isUnion ?
                Group.union(components.toArray(new Layout[0])) :
                Group.struct(components.toArray(new Layout[0]));
    }

    /**
     * The scanner is responsible for converting the descriptor string into a sequence of tokens which are then
     * processed accordingly by the parser. In addition to special type tags (e.g. 'b', 'B', 'V', etc,), the scanner
     * also handles numbers as well as comments (the latter are simply stripped from the resulting token sequence).
     */
    static class DescriptorScanner {

        enum Token {
            NATIVE_ENDIAN,
            BIG_ENDIAN,
            LITTLE_ENDIAN,
            SIZE_SELECTOR,
            FACE_SELECTOR,
            BREAKDOWN,
            STANDARD_SIZED_SCALAR,
            EXPLICIT_SIZED_SCALAR,
            OTHER_SCALAR,
            POINTER,
            BITFIELD,
            LPAREN,
            RPAREN,
            LBRACE,
            RBRACE,
            UNION,
            ANY_SIZE,
            NUMERIC,
            VOID,
            SEPARATOR,
            END;
        }

        private int cp;
        private final char[] buf;
        private StringBuilder numBuf;
        private char ch = 0;
        private char lastScalarTag = 0;

        DescriptorScanner(String desc) {
            buf = desc.toCharArray();
            cp = 0;
            nextChar();
        }

        Token next() {
            if (ch == 0) {
                return Token.END;
            }
            if (Character.isDigit(ch)) {
                //number = 1*DIGIT
                numBuf = new StringBuilder();
                numBuf.append(ch);
                nextChar();
                while (ch != 0 && Character.isDigit(ch)) {
                    numBuf.append(ch);
                    nextChar();
                }
                return Token.NUMERIC;
            } else {
                Token res;
                switch (ch) {
                    case 'o': case 's': case 'l': case 'q':
                    case 'O': case 'S': case 'L': case 'Q':
                        lastScalarTag = ch;
                        res = Token.STANDARD_SIZED_SCALAR;
                        break;
                    case 'i': case 'f': case 'I': case 'F':
                        lastScalarTag = ch;
                        res = Token.EXPLICIT_SIZED_SCALAR;
                        break;
                    case 'd': case 'e': case 'D': case 'E':
                    case 'c': case 'B': case 'v': case 'x':
                        lastScalarTag = ch;
                        res = Token.OTHER_SCALAR;
                        break;
                    case 'V':
                        res = Token.VOID;
                        break;
                    case ' ': case '\t': case '\n': case '\r':
                        res = Token.SEPARATOR;
                        break;
                    case 'p':
                        res = Token.POINTER;
                        break;
                    case 'b':
                        res = Token.BITFIELD;
                        break;
                    case '[':
                        res = Token.LBRACE;
                        break;
                    case ']':
                        res = Token.RBRACE;
                        break;
                    case '(':
                        res = Token.LPAREN;
                        break;
                    case ')':
                        res = Token.RPAREN;
                        break;
                    case '@':
                        res = Token.NATIVE_ENDIAN;
                        break;
                    case '>':
                        res = Token.BIG_ENDIAN;
                        break;
                    case '<':
                        res = Token.LITTLE_ENDIAN;
                        break;
                    case '=':
                        res = Token.SIZE_SELECTOR;
                        break;
                    case '.':
                        res = Token.FACE_SELECTOR;
                        break;
                    case ':':
                        res = Token.BREAKDOWN;
                        break;
                    case '*':
                        res = Token.ANY_SIZE;
                        break;
                    case '|':
                        res = Token.UNION;
                        break;
                    case '#': {
                        nextChar();
                        while (ch != 0 && ch != '\n' && ch != '\r') {
                            nextChar();
                        }
                        return Token.SEPARATOR;
                    }
                    default:
                        throw error("unknown char: " + ch);
                }
                nextChar();
                return res;
            }
        }

        Token peek() {
            char prevCh = ch;
            int prevCp = cp;
            char prevLastScalarTag = lastScalarTag;
            StringBuilder prevNumBuf = numBuf;
            try {
                numBuf = new StringBuilder();
                return next();
            } finally {
                ch = prevCh;
                cp = prevCp;
                lastScalarTag = prevLastScalarTag;
                numBuf = prevNumBuf;
            }
        }

        void nextChar() {
            if (cp < buf.length) {
                ch = buf[cp++];
            } else {
                ch = 0;
            }
        }

        int lastNumber() {
            if (numBuf == null) {
                throw error("last token was not a number!");
            }
            return Integer.valueOf(numBuf.toString());
        }

        char lastScalarTag() {
            if (lastScalarTag == 0) {
                throw error("Last token was not a scalar!");
            }
            return lastScalarTag;
        }
        
        InvalidDescriptorException error(String msg) {
            return new InvalidDescriptorException(this, msg);
        }
    }

    /**
     * Exception that represents a failure when parsing the descriptor string contents. The exception
     * message attempts to pinpoint the location at which the error occurred.
     */
    public static class InvalidDescriptorException extends RuntimeException {

        private final static long serialVersionUID = -5877088182715249087L;
        private final DescriptorScanner scanner;


        InvalidDescriptorException(DescriptorScanner scanner, String msg) {
            super(msg);
            this.scanner = scanner;
        }

        @Override
        public String getMessage() {
            return super.getMessage() + ": " + diagnosis();
        }

        public String diagnosis() {
            StringBuilder sb = new StringBuilder();
            int index = scanner.cp - 1;
            String descriptor = new String(scanner.buf);
            sb.append(descriptor, 0, index);
            sb.append("_");
            if (index < descriptor.length() - 1) {
               sb.append(descriptor, index, descriptor.length() - 1);
            }
            return sb.toString();
        }
    }
}
