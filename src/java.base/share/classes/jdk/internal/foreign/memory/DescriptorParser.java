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

package jdk.internal.foreign.memory;

import jdk.internal.foreign.memory.DescriptorParser.DescriptorScanner.Token;

import java.foreign.layout.*;
import java.foreign.layout.Value.Endianness;

import java.util.*;
import java.util.function.Predicate;

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
        Optional<Map<String, String>> annos = annotationsOpt();
        final Function result;
        if (token == Token.VOID) {
            nextToken(Token.VOID);
            result = Function.ofVoid(varargs, args.toArray(new Layout[0]));
        } else {
            result = Function.of(parseLayout(), varargs, args.toArray(new Layout[0]));
        }
        return annotationsOpt()
                .map(a -> withAnnotations(result, a))
                .orElse(result);
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
        char tag = scanner.lastChar();
        return Character.isUpperCase(tag) ?
                Endianness.BIG_ENDIAN: Endianness.LITTLE_ENDIAN;
    }

    Value.Kind lastKind() {
        char tag = scanner.lastChar();
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
     * annotation = '{' ident [ '=' string ] '}'
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
        nextToken(Token.LPAREN);
        String name = parseIdent(t -> t == Token.EQ || t == Token.RPAREN, "'=' or ')'");
        if (token == Token.EQ) {
            annos.put(name, parseAnnotationValue());
        } else {
            annos.put(Layout.NAME, name);
        }
        nextToken(Token.RPAREN);
    }

    private String parseAnnotationValue() {
        int depth = 0;
        StringBuilder value = new StringBuilder();
        char lastChar;
        while (true) {
            lastChar = scanner.ch;
            nextToken();
            switch (token) {
                case LPAREN:
                    value.append(lastChar);
                    depth++;
                    break;
                case RPAREN:
                    if (depth-- == 0) {
                        return value.toString();
                    } else {
                        value.append(lastChar);
                    }
                    break;
                case NUMERIC:
                    value.append(scanner.lastString());
                    break;
                case END:
                    throw scanner.error("Expected ')'");
                default:
                    value.append(lastChar);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D extends Descriptor> D withAnnotations(D d, Map<String, String> annos) {
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
        Layout[] componentArr = components.toArray(new Layout[0]);
        return isUnion ? Group.union(componentArr) : Group.struct(componentArr);
    }

    /**
     * unresolvedLayout = '$' '{' layoutExpession '}' [annotations]
     */
    private Unresolved parseUnresolved() {
        nextToken(); // $
        nextToken(); // LBRACE
        String layoutExpr = parseIdent(t -> t == Token.RBRACE, "{");
        nextToken(); // RBRACE
        return annotatedOpt(Unresolved.of(layoutExpr));
    }

    String parseIdent(Predicate<Token> terminator, String expected) {
        StringBuilder buf = new StringBuilder();
        loop: while (true) {
            switch (token) {
                case NUMERIC:
                    if (buf.length() == 0) {
                        throw scanner.error("Invalid numeric start in ident");
                    }
                    buf.append(scanner.lastNumber());
                    break;
                case END:
                    throw scanner.error("Expected " + expected);
                default: {
                    if (terminator.test(token)) {
                        if (buf.length() == 0) {
                            throw scanner.error("Expected ident before " + expected);
                        }
                        break loop;
                    } else {
                        char lastChar = scanner.lastChar();
                        if (buf.length() == 0 ?
                                !Character.isJavaIdentifierStart(lastChar) :
                                !Character.isJavaIdentifierPart(lastChar)) {
                            throw scanner.error("Illegal char in ident");
                        }
                        buf.append(lastChar);
                        break;
                    }
                }
            }
            boolean pendingSeparator = scanner.foundSeparator();
            nextToken();
            if (pendingSeparator && !terminator.test(token)) {
                throw scanner.error("Unexpected separator in declaration name");
            }
        }
        return buf.toString();
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
            LBRACE,
            RBRACE,
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
        private boolean sep;
        private final char[] buf;
        private StringBuilder tempBuf;
        private char ch = 0;
        private char lastChar = 0;

        DescriptorScanner(String desc) {
            buf = desc.toCharArray();
            cp = 0;
            nextChar();
        }

        Token next() {
            lastChar = ch;
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
                        case '{':
                            res = Token.LBRACE;
                            break outer;
                        case '}':
                            res = Token.RBRACE;
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
                        sep = true;
                        break;
                    default:
                        sep = false;
                }
            } else {
                ch = 0;
            }
        }

        boolean foundSeparator() {
            return sep;
        }

        char lastChar() {
            return lastChar;
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

    public static Function parseFunction(String desc) {
        return new DescriptorParser(desc).parseFunction();
    }

    public static Layout parseLayout(String def) {
        return new DescriptorParser(def).parseLayout();
    }
}
