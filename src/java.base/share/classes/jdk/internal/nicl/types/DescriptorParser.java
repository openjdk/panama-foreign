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
import jdk.internal.nicl.types.Type.Endianness;

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
    public Stream<Type> parseLayout() {
        if (token == Token.LPAREN) {
            return Stream.of(parseFunction());
        } else {
            List<Type> types = new ArrayList<>();
            while (token != Token.END) {
                types.add(parseElementType());
            }
            if (types.size() == 0) {
                throw scanner.error("At least one element type should be present in a layout string");
            }
            return types.stream();
        }
    }

    /**
     * descriptor = scalarType / arrayType / containerType / functionType
     */
    private Type parseDescriptor() {
        return token == Token.LPAREN ?
            parseFunction() :
            parseElementType();
    }

    /**
     * functionType = '(' *elementType ['*'] ')' elementType
     */
    private Type parseFunction() {
        nextToken(); // LPAREN
        boolean varargs = false;
        List<Type> params = new ArrayList<>();
        while (token != Token.RPAREN &&
                (token != Token.ANY_SIZE || scanner.peek() != Token.RPAREN)) {
            params.add(parseElementType());
        }
        if (token == Token.ANY_SIZE) {
            varargs = true;
            nextToken(); // ANY_SIZE
        }
        nextToken(); //RPAREN
        return new Function(params.toArray(new Type[0]), parseElementType(), varargs);
    }

    /**
     * elementType = *separator (scalarType / arrayType / pointerType / containerType) *separator
     */
    private Type parseElementType() {
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
    Type parseArray() {
        int occurrences = token == Token.ANY_SIZE ?
                -1 : parseNumber(2);
        nextToken(); // size
        Type elemType = parseElementType();
        return new Array(elemType, occurrences);
    }

    /**
     * pointerType = p [':' descriptor ]
     */
    Type parsePointer() {
        nextToken();
        Type pointee = null;
        if (token == Token.BREAKDOWN) {
            nextToken();
            pointee = parseDescriptor();
        }
        return new Pointer(pointee);
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
    Type parseScalar() {
        Type result;
        Endianness e = endianness.orElse(Endianness.NATIVE);
        if (token == Token.SIZE_SELECTOR) {
            nextToken(); // SIZE_SELECTOR
            switch (token) {
                case NUMERIC:
                    int size = parseNumber(1);
                    nextToken();
                    result = new Scalar(scanner.lastScalarTag(), e, size);
                    break;
                case STANDARD_SIZED_SCALAR:
                    char tag = scanner.lastScalarTag();
                    result = new Scalar(scalarTag(tag), e, scalarSize(tag, -1));
                    break;
                default:
                    throw scanner.error("Unexpected token: " + token);
            }
        } else {
            char tag = scanner.lastScalarTag;
            if (tag == 'v') {
                throw scanner.error("Unsized vector scalar");
            }
            result = new Scalar(scanner.lastScalarTag(), e);
        }
        nextToken(); //scalar tag
        if (token == Token.BREAKDOWN) {
            //bitfield
            result = parseBitFields((Scalar)result);
        }
        return result;
    }

    private BitFields parseBitFields(Scalar scalar) {
        if (!isValidBitfieldTag(scalar.type)) {
            throw scanner.error("BitFields not supported on type tag '" + scalar.type + "'");
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
        return new BitFields(scalar, fields.stream().mapToInt(x -> x).toArray());
    }

    private int scalarSize(char type, int size) {
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

    private char scalarTag(char type) {
        switch (type) {
            case 'o': case 's': case 'l': case 'q':
                return 'i';
            case 'O': case 'S': case 'L': case 'Q':
                return 'I';
            default:
                return type;
        }
    }

    private int parseNumber(int lowerBoundInclusive) {
        int num = scanner.lastNumber(); //this throws if last token is not a number
        if (num < lowerBoundInclusive) {
            throw scanner.error("number outside range");
        }
        return num;
    }

    private boolean isValidBitfieldTag(char ch) {
        switch (ch) {
            case 'o': case 's': case 'l': case 'q': case 'i':
            case 'O': case 'S': case 'L': case 'Q': case 'I':
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
                return Optional.of(Endianness.BIG);
            case LITTLE_ENDIAN:
                nextToken();
                return Optional.of(Endianness.LITTLE);
            case NATIVE_ENDIAN:
                nextToken();
                return Optional.of(Endianness.NATIVE);
            default:
                return Optional.empty();
        }
    }

    /**
     * containerType = [endianness] '[' elementType *(elementType / unionMember) ']'
     */
    Type parseContainer() {
        nextToken(); // LBRACE
        List<Type> components = new ArrayList<>();
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
        return new Container(isUnion, components.toArray(new Type[0]));
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
                    case 'c': case 'B': case 'V': case 'x': case 'v':
                        lastScalarTag = ch;
                        res = Token.OTHER_SCALAR;
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
