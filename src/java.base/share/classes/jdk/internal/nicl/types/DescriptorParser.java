/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.nicl.layout.*;
import java.nicl.layout.Value.Endianness;

import java.util.*;

/**
 * Parse a layout string into a descriptor type {@see Type}.
 */
public class DescriptorParser {

    private final DescriptorScanner scanner;
    private Token token;
    private boolean allowSubByteSizes = false;

    public DescriptorParser(String desc) {
        scanner = new DescriptorScanner(desc);
        nextToken();
    }

    void nextToken() {
        token = scanner.next();
    }

    void nextToken(Token expected) {
        if (token != expected) {
            throw scanner.error("expected: " + expected + "; found: " + token);
        }
        nextToken();
    }

    /**
     * function = ( nonVoidFunction / voidFunction)
     * nonVoidFunction = '(' *( layout ) ['*'] ')' layout
     * voidFunction = '(' *( layout ) ['*'] ')' 'v'
     */
    public Function parseFunction() {
        nextToken(Token.LPAREN);
        List<Layout> args = new ArrayList<>();
        while (token != Token.RPAREN && token != Token.VARARGS) {
            args.add(parseLayout());
        }
        boolean varargs = false;
        if (token == Token.VARARGS) {
            varargs = true;
            nextToken();
        }
        nextToken(Token.RPAREN);
        if (token == Token.VOID) {
            nextToken(Token.VOID);
            return Function.ofVoid(varargs, args.toArray(new Layout[0]));
        } else {
            return Function.of(parseLayout(), varargs, args.toArray(new Layout[0]));
        }
    }

    /**
     * layout = (padding / value / group / unresolved)
     */
    public Layout parseLayout() {
        switch (token) {
            case UNRESOLVED:
                return parseUnresolved();
            case LBRACKET:
                return parseGroup();
            case PADDING:
                return parsePadding();
            case VALUE:
                return parseValue();
            default:
                throw scanner.error("Unexpected token: " + token);
        }
    }

    /**
     * addressRest = ':' addresseeInfo
     * addresseeInfo = ( 'v' / function / layout )
     */
    private Address parsePointerRest(Value value) {
        Address addr;
        nextToken(Token.BREAKDOWN);
        if (token == Token.LPAREN) {
            Function function = parseFunction();
            addr = Address.ofFunction(value.bitsSize(), function, value.kind(), value.endianness());
        } else if (token == Token.VOID) {
            nextToken();
            addr = Address.ofVoid(value.bitsSize(), value.kind(), value.endianness());
        } else {
            Layout layout = parseLayout();
            addr = Address.ofLayout(value.bitsSize(), layout, value.kind(), value.endianness());
        }
        addr = withAnnotations(addr, value.annotations());
        if (value.contents().isPresent()) {
            addr = addr.withContents(value.contents().get());
        }
        return addr;
    }

    /**
     * value = [ 'u' ] valueTag number [annotations] [ '=' group ] [ addressRest ]
     * valueTag = 'u' / 'U' / 'i' / 'I' / 'f' / 'F'
     */
    private Value parseValue() {
        Value.Kind kind = lastKind();
        Endianness endianness = lastEndianness();
        nextToken(Token.VALUE);
        int size = parseSize();
        nextToken(Token.NUMERIC); //NUMERIC
        Value value;
        switch (kind) {
            case INTEGRAL_UNSIGNED:
                value = Value.ofUnsignedInt(endianness, size);
                break;
            case INTEGRAL_SIGNED:
                value = Value.ofSignedInt(endianness, size);
                break;
            case FLOATING_POINT:
                value = Value.ofFloatingPoint(endianness, size);
                break;
            default:
                throw scanner.error("unexpected value tag: " + kind);
        }
        value = contentsOpt(annotatedOpt(value));
        return (token == Token.BREAKDOWN) ?
                parsePointerRest(value) :
                value;
    }

    /**
     * padding = [ 'x' ] number [annotations]
     */
    private Padding parsePadding() {
        nextToken(Token.PADDING);
        int size = parseSize();
        nextToken(Token.NUMERIC);
        return annotatedOpt(Padding.of(size));
    }

    Endianness lastEndianness() {
        char tag = scanner.lastValueTag;
        return Character.isUpperCase(tag) ?
                Endianness.BIG_ENDIAN: Endianness.LITTLE_ENDIAN;
    }

    Value.Kind lastKind() {
        char tag = scanner.lastValueTag;
        switch (tag) {
            case 'u': case 'U': return Value.Kind.INTEGRAL_UNSIGNED;
            case 'i': case 'I': return Value.Kind.INTEGRAL_SIGNED;
            case 'f': case 'F': return Value.Kind.FLOATING_POINT;
            default:
                throw new IllegalStateException("Cannot get here!");
        }
    }

    /**
     * contents = '=' group
     */
    @SuppressWarnings("unchecked")
    private <V extends Value> V contentsOpt(V value) {
        if (token != Token.EQ) {
            return value;
        }
        nextToken();
        boolean prevAllowSubByteSizes = allowSubByteSizes;
        try {
            allowSubByteSizes = true;
            return (V)value.withContents(parseGroup());
        } finally {
            allowSubByteSizes = prevAllowSubByteSizes;
        }
    }

    /**
     * annotations = +( annotation )
     * annotation = '{' string [ '=' string ] '}'
     */
    @SuppressWarnings("unchecked")
    private <D extends Layout> D annotatedOpt(D l) {
        return annotationsOpt().map(annos -> withAnnotations(l, annos)).orElse(l);
    }

    private Optional<Map<String, String>> annotationsOpt() {
        Map<String, String> annos = new HashMap<>();
        while (token == Token.LPAREN) {
            parseAnnotation(annos);
        }
        return annos.isEmpty() ?
                Optional.empty() :
                Optional.of(annos);
    }

    private void parseAnnotation(Map<String, String> annos) {
        int depth = 0;
        StringBuilder[] nameValue = new StringBuilder[] { new StringBuilder(), new StringBuilder() };
        int nameValueIdx = 0;
        char lastChar;
        while (true) {
            lastChar = scanner.ch;
            nextToken();
            switch (token) {
                case LPAREN:
                    nameValue[nameValueIdx].append(lastChar);
                    depth++;
                    break;
                case RPAREN:
                    if (depth-- == 0) {
                        if (nameValueIdx == 0) {
                            annos.put(Layout.NAME, nameValue[0].toString());
                        } else {
                            annos.put(nameValue[0].toString(), nameValue[1].toString());
                        }
                        nextToken();
                        return;
                    } else {
                        nameValue[nameValueIdx].append(lastChar);
                    }
                    break;
                case EQ:
                    if (depth == 0) {
                        nameValueIdx++;
                        if (nameValueIdx > 1) {
                            throw scanner.error("Expected ')'");
                        }
                    } else {
                        nameValue[nameValueIdx].append(lastChar);
                    }
                    break;
                case NUMERIC:
                    nameValue[nameValueIdx].append(scanner.lastString());
                    break;
                case END:
                    throw scanner.error("Expected ')'");
                default:
                    nameValue[nameValueIdx].append(lastChar);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D extends Layout> D withAnnotations(D d, Map<String, String> annos) {
        for (Map.Entry<String, String> anno : annos.entrySet()) {
            d = (D)d.withAnnotation(anno.getKey(), anno.getValue());
        }
        return d;
    }

    private int parseNumber(int lowerBoundInclusive) {
        int num = scanner.lastNumber(); //this throws if last token is not a number
        if (num < lowerBoundInclusive) {
            throw scanner.error("number outside range");
        }
        return num;
    }

    private int parseSize() {
        int size = parseNumber(1);
        if (size % 8 != 0 && !allowSubByteSizes) {
            throw scanner.error("invalid sub-byte size");
        }
        return size;
    }

    /**
     * group = '[' (structOrUnionRest / sequenceRest) ']' [annotations]
     */
    private Group parseGroup() {
        nextToken(Token.LBRACKET);
        Group group;
        if (token == Token.NUMERIC) {
            group = parseSequenceRest();
        } else {
            group = parseStructOrUnionRest();
        }
        nextToken(Token.RBRACKET);
        return annotatedOpt(group);
    }

    /**
     * sequenceRest = [number] layout
     */
    private Sequence parseSequenceRest() {
        int arraySize = parseNumber(0);
        nextToken();
        Layout elem = parseLayout();
        return Sequence.of(arraySize, elem);
    }

    /**
     * structOrUnionRest = layout *(layout / unionMember)
     * unionMember = '|' layout
     */
    Group parseStructOrUnionRest() {
        List<Layout> components = new ArrayList<>();
        boolean isUnion = false;
        while (token != Token.RBRACKET) {
            components.add(parseLayout());
            if (token == Token.UNION) {
                isUnion = true;
                nextToken();
            }
        }
        if (components.isEmpty()) {
            throw scanner.error("Empty container");
        }
        Layout[] componentArr = components.toArray(new Layout[0]);
        return isUnion ? Group.union(componentArr) : Group.struct(componentArr);
    }

    /**
     * unresolvedLayout = '$' [annotations]
     */
    private Unresolved parseUnresolved() {
        nextToken();
        return annotatedOpt(Unresolved.of());
    }

    /**
     * The scanner is responsible for converting the descriptor string into a sequence of tokens which are then
     * processed accordingly by the parser. In addition to special type tags (e.g. 'f', 'U', 'i', etc,), the scanner
     * also handles numbers, name annotations and separators (the latter are simply stripped from the resulting token sequence).
     */
    static class DescriptorScanner {

        enum Token {
            BREAKDOWN,
            EQ,
            VALUE,
            PADDING,
            UNRESOLVED,
            LPAREN,
            RPAREN,
            LBRACKET,
            RBRACKET,
            UNION,
            NUMERIC,
            UNKNOWN,
            VOID,
            VARARGS,
            END;
        }

        private int cp;
        private final char[] buf;
        private StringBuilder tempBuf;
        private char ch = 0;
        private char lastValueTag = 0;

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
                tempBuf = new StringBuilder();
                tempBuf.append(ch);
                nextChar();
                while (ch != 0 && Character.isDigit(ch)) {
                    tempBuf.append(ch);
                    nextChar();
                }
                return Token.NUMERIC;
            } else {
                Token res;
                outer: while (true) {
                    switch (ch) {
                        case '$':
                            res = Token.UNRESOLVED;
                            break outer;
                        case 'u': case 'U':
                        case 'f': case 'i':
                        case 'F': case 'I':
                            lastValueTag = ch;
                            res = Token.VALUE;
                            break outer;
                        case 'x':
                            res = Token.PADDING;
                            break outer;
                        case 'v':
                            res = Token.VOID;
                            break outer;
                        case '*':
                            res = Token.VARARGS;
                            break outer;
                        case '[':
                            res = Token.LBRACKET;
                            break outer;
                        case ']':
                            res = Token.RBRACKET;
                            break outer;
                        case '(':
                            res = Token.LPAREN;
                            break outer;
                        case ')':
                            res = Token.RPAREN;
                            break outer;
                        case '=':
                            res = Token.EQ;
                            break outer;
                        case ':':
                            res = Token.BREAKDOWN;
                            break outer;
                        case '|':
                            res = Token.UNION;
                            break outer;
                        default:
                            res = Token.UNKNOWN;
                            break outer;
                    }
                }
                nextChar();
                return res;
            }
        }

        void nextChar() {
            if (cp < buf.length) {
                ch = buf[cp++];
                switch (ch) {
                    case ' ': case '\t': case '\n': case '\r':
                        nextChar();
                }
            } else {
                ch = 0;
            }
        }

        String lastString() {
            if (tempBuf == null) {
                throw error("last token was not a string!");
            }
            return tempBuf.toString();
        }

        int lastNumber() {
            if (tempBuf == null) {
                throw error("last token was not a number!");
            }
            return Integer.valueOf(tempBuf.toString());
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

    /**
     * declarations = *( declaration )
     * declaration = name '=' (function / layout)
     */
    public static Map<String, Object> parseHeaderDeclarations(String decls) {
        Map<String, Object> declarations = new LinkedHashMap<>();
        while (true) {
            int split = decls.indexOf('=');
            if (split == -1) break;
            String name = decls.substring(0, split);
            DescriptorParser parser = new DescriptorParser(decls.substring(split + 1));
            Object decl = parser.token == Token.LPAREN ?
                    parser.parseFunction() :
                    parser.parseLayout();
            declarations.put(name, decl);
            decls = decls.substring(split + parser.scanner.cp - 1);
        }
        return declarations;
    }

    public static Layout parseLayout(String def) {
        return new DescriptorParser(def).parseLayout();
    }
}
