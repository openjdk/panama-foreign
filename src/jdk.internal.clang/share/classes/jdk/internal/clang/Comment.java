/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

package jdk.internal.clang;

import java.io.PrintStream;
import java.nio.ByteBuffer;

public final class Comment extends StructType {
    Comment(ByteBuffer buf) {
        super(buf);
    }

    public CommentKind kind() {
        int v = kind0();
        return CommentKind.valueOf(v);
    }

    @Override
    public String toString(){
        return String.format("Comment{ kind=%s }", kind());
    }

    public native int getNumChildren();
    public native Comment getChild(int childIdx);

    public native boolean isWhitespace();

    public native boolean inlineContentHasTrailingNewline();

    public String getText() {
        checkCommentKind(CommentKind.Text);
        return getText0();
    }

    public String inlineCommandGetCommandName() {
        checkCommentKind(CommentKind.InlineCommand);
        return inlineCommandGetCommandName0();
    }

    public CommentInlineCommandRenderKind inlineCommandGetRenderKind() {
        checkCommentKind(CommentKind.InlineCommand);
        int v = inlineCommandGetRenderKind0();
        return CommentInlineCommandRenderKind.valueOf(v);
    }

    public int inlineCommandGetNumArgs() {
        checkCommentKind(CommentKind.InlineCommand);
        return inlineCommandGetNumArgs0();
    }

    public String inlineCommandGetArgText(int argIdx) {
        checkCommentKind(CommentKind.InlineCommand);
        return inlineCommandGetArgText0(argIdx);
    }

    public String htmlTagGetTagName() {
        checkCommentKind(CommentKind.HTMLStartTag, CommentKind.HTMLEndTag);
        return htmlTagGetTagName0();
    }

    public String htmlTagGetAsString() {
        checkCommentKind(CommentKind.HTMLStartTag, CommentKind.HTMLEndTag);
        return htmlTagGetAsString0();
    }

    public int htmlStartGetNumAttrs() {
        checkCommentKind(CommentKind.HTMLStartTag);
        return htmlStartGetNumAttrs0();
    }

    public String htmlStartGetAttrName(int attrIdx) {
        checkCommentKind(CommentKind.HTMLStartTag);
        return htmlStartGetAttrName0(attrIdx);
    }

    public String htmlStartGetAttrValue(int attrIdx) {
        checkCommentKind(CommentKind.HTMLStartTag);
        return htmlStartGetAttrValue0(attrIdx);
    }

    public String blockCommandGetCommandName() {
        checkCommentKind(CommentKind.BlockCommand);
        return blockCommandGetCommandName0();
    }

    public int blockCommandGetNumArgs() {
        checkCommentKind(CommentKind.BlockCommand);
        return blockCommandGetNumArgs0();
    }

    public String blockCommandGetArgText(int argIdx) {
        checkCommentKind(CommentKind.BlockCommand);
        return blockCommandGetArgText0(argIdx);
    }

    public Comment blockCommandGetParagraph() {
        checkCommentKind(CommentKind.BlockCommand, CommentKind.VerbatimBlockCommand);
        return blockCommandGetParagraph0();
    }

    public String paramCommandGetParamName() {
        checkCommentKind(CommentKind.ParamCommand);
        return paramCommandGetParamName0();
    }

    public boolean paramCommandIsParamIndexValid() {
        checkCommentKind(CommentKind.ParamCommand);
        return paramCommandIsParamIndexValid0();
    }

    public int paramCommandGetParamIndex() {
        checkCommentKind(CommentKind.ParamCommand);
        return paramCommandGetParamIndex0();
    }

    public boolean paramCommandIsDirectionExplicit() {
        checkCommentKind(CommentKind.ParamCommand);
        return paramCommandIsDirectionExplicit0();
    }

    public CommentParamPassDirection paramCommandGetDirection() {
        checkCommentKind(CommentKind.ParamCommand);
        int dir = paramCommandGetDirection0();
        return CommentParamPassDirection.valueOf(dir);
    }

    public String tparamCommandGetParamName() {
        checkCommentKind(CommentKind.TParamCommand);
        return tparamCommandGetParamName0();
    }

    public boolean tparamCommandIsParamPositionValid() {
        checkCommentKind(CommentKind.TParamCommand);
        return tparamCommandIsParamPositionValid0();
    }

    public int tparamCommandPositionGetDepth() {
        checkCommentKind(CommentKind.TParamCommand);
        return tparamCommandPositionGetDepth0();
    }

    public int tparamCommandGetIndex(int depth) {
        checkCommentKind(CommentKind.TParamCommand);
        return tparamCommandGetIndex0(depth);
    }

    public String verbatimBlockLineGetText() {
        checkCommentKind(CommentKind.VerbatimBlockLine);
        return verbatimBlockLineGetText0();
    }

    public String verbatimLineGetText() {
        checkCommentKind(CommentKind.VerbatimLine);
        return verbatimLineGetText0();
    }

    public String fullCommentGetAsHTML() {
        checkCommentKind(CommentKind.FullComment);
        return fullCommentGetAsHTML0();
    }

    public String fullCommentGetAsXML() {
        checkCommentKind(CommentKind.FullComment);
        return fullCommentGetAsXML0();
    }

    private void checkCommentKind(CommentKind expectedKind) {
        CommentKind k = kind();
        if (k != expectedKind) {
            throw new IllegalArgumentException("Comment is of kind: " + k);
        }
    }

    private void checkCommentKind(CommentKind expected1, CommentKind expected2) {
        CommentKind k = kind();
        if (k != expected1 && k != expected2) {
            throw new IllegalArgumentException("Comment is of kind: " + k);
        }
    }

    private native int kind0();
    private native String inlineCommandGetCommandName0();
    private native String getText0();
    private native int inlineCommandGetRenderKind0();
    private native int inlineCommandGetNumArgs0();
    private native String inlineCommandGetArgText0(int argIdx);
    private native String htmlTagGetTagName0();
    private native String htmlTagGetAsString0();
    private native int htmlStartGetNumAttrs0();
    private native String htmlStartGetAttrName0(int attrIdx);
    private native String htmlStartGetAttrValue0(int attrIdx);
    private native String blockCommandGetCommandName0();
    private native int blockCommandGetNumArgs0();
    private native String blockCommandGetArgText0(int argIdx);
    private native Comment blockCommandGetParagraph0();
    private native String paramCommandGetParamName0();
    private native boolean paramCommandIsParamIndexValid0();
    private native int paramCommandGetParamIndex0();
    private native boolean paramCommandIsDirectionExplicit0();
    private native int paramCommandGetDirection0();
    private native String tparamCommandGetParamName0();
    private native boolean tparamCommandIsParamPositionValid0();
    private native int tparamCommandPositionGetDepth0();
    private native int tparamCommandGetIndex0(int depth);
    private native String verbatimBlockLineGetText0();
    private native String verbatimLineGetText0();
    private native String fullCommentGetAsHTML0();
    private native String fullCommentGetAsXML0();

    private void indent(int level, PrintStream out) {
        for (int i = 0; i < level; i++) {
            out.print("    ");
        }
    }

    private void print(int level, PrintStream out) {
        indent(level, out);
        out.println(toString());
        switch (kind()) {
            case Null:
            case Paragraph:
                break;
            case VerbatimBlockCommand:
                blockCommandGetParagraph().print(level, out);
                break;
            case Text:
                if (! isWhitespace()) {
                    indent(level, out);
                    out.println(getText());
                }
                break;
            case InlineCommand: {
                indent(level, out);
                final int numArgs = inlineCommandGetNumArgs();
                out.printf("name=%s, renderkind=%s, num_args=%d",
                    inlineCommandGetCommandName(),
                    inlineCommandGetRenderKind(), numArgs);
                for (int i = 0; i < numArgs; i++) {
                    out.print(' ');
                    out.print(inlineCommandGetArgText(i));
                }
                out.println();
            }
            break;
            case BlockCommand: {
                indent(level, out);
                final int numArgs = blockCommandGetNumArgs();
                out.printf("name=%s, num_args=%d",
                    blockCommandGetCommandName(), numArgs);
                for (int i = 0; i < numArgs; i++) {
                    out.print(' ');
                    out.print(blockCommandGetArgText(i));
                }
                out.println();
                blockCommandGetParagraph().print(level + 1, out);
            }
            break;
            case ParamCommand: {
                indent(level, out);
                out.printf("name=%s, index_valid=%b, index=%d, direction_explicit=%b, dir=%s\n",
                    paramCommandGetParamName(), paramCommandIsParamIndexValid(),
                    paramCommandGetParamIndex(), paramCommandIsDirectionExplicit(),
                    paramCommandGetDirection());
            }
            break;
            case TParamCommand: {
                indent(level, out);
                int depth = tparamCommandPositionGetDepth();
                out.printf("name=%s, position_valid=%b, depth=%d",
                    tparamCommandGetParamName(), tparamCommandIsParamPositionValid(), depth);
                for (int i = 0; i < depth; i++) {
                    out.print(' ');
                    out.print(tparamCommandGetIndex(i));
                }
                out.println();
            }
            break;
            case HTMLStartTag: {
                indent(level, out);
                out.printf("<%s ", htmlTagGetTagName());
                final int numAttrs = htmlStartGetNumAttrs();
                for (int i = 0; i < numAttrs; i++) {
                    out.printf(" %s=%s ", htmlStartGetAttrName(i), htmlStartGetAttrValue(i));
                }
                out.println('>');
            }
            break;
            case HTMLEndTag:
                indent(level, out);
                out.printf("</%s>\n", htmlTagGetTagName());
                break;
            case VerbatimBlockLine:
                indent(level, out);
                out.println(verbatimBlockLineGetText());
                break;
            case VerbatimLine:
                indent(level, out);
                out.println(verbatimLineGetText());
                break;
            case FullComment:
                indent(level, out);
                out.println(fullCommentGetAsHTML());
                break;
        }

        level++;
        for (int j = 0; j < getNumChildren(); j++) {
            getChild(j).print(level, out);
        }
        level--;
    }

    // test main
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("header file expected");
            System.exit(1);
        }

        final String[] clangArgs = new String[args.length - 1];
        System.arraycopy(args, 0, clangArgs, 0, clangArgs.length);
        final String file = args[args.length - 1];
        final Index index = LibClang.createIndex(false);
        final Cursor tuCursor = index.parse(file,
            d -> {
                System.err.println(d);
                if (d.severity() >  Diagnostic.CXDiagnostic_Warning) {
                    System.exit(2);
                }
            },
            true, clangArgs);
        tuCursor.children().forEach(c -> {
            Comment comment = c.getParsedComment();
            if (comment.kind() != CommentKind.Null) {
                System.out.println(c);
                comment.print(0, System.out);
            }
        });
    }
}
