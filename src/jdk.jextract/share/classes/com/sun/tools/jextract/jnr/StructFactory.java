/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.jextract.jnr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.tools.jextract.Utils;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.CursorKind;
import jdk.internal.clang.Type;


public class StructFactory {
    // definition cursor spelling to Class Name
    final static Map<String, String> typeMap;
    final String pkgName;

    private final static String LAYOUT_CLSNAME = "Layout";

    static {
        typeMap = new HashMap<>();
    }

    public StructFactory() {
        this("");
    }

    public StructFactory(String pkg) {
        this.pkgName = pkg;
    }

    String getUnexposedTypeClassName(Type type) throws IOException {
        Cursor c = type.getDeclarationCursor().getDefinition();
        String nativeTypeName = c.spelling();
        if (typeMap.containsKey(nativeTypeName)) {
            return typeMap.get(nativeTypeName);
        } else {
            if (c.kind() == CursorKind.StructDecl) {
                StructFactory f = new StructFactory(pkgName);
                String clsName = f.create(c);
                typeMap.put(nativeTypeName, clsName);
                return clsName;
            }
        }
        //
        throw new IllegalStateException("unknown cursor " + c.kind() + ":" + c.spelling());
    }

    String getTypeClassName(Type type) throws IOException {
        switch(type.kind()) {
            case Typedef:
                return getTypeClassName(type.canonicalType());
            case Bool:
                return "Boolean";
            case UChar:
            case Char_U:
                return "Unsigned8";
            case SChar:
            case Char_S:
                return "Signed8";
            case UShort:
                return "Unsigned16";
            case Short:
            case Char16:
            case WChar:
                return "Signed16";
            case UInt:
                return "Unsigned32";
            case Int:
            case Char32:
                return "Signed32";
            case ULong:
                return "UnsignedLong";
            case Long:
                return "SignedLong";
            case ULongLong:
                return "Unsigned64";
            case LongLong:
                return "Signed64";
            case Float:
                return "Float";
            case Double:
                return "Double";
            case NullPtr:
            case Pointer:
                return "Pointer";
            case ConstantArray:
                return getTypeClassName(type.getElementType()) + "[]";
            case Unexposed:
                return getUnexposedTypeClassName(type) + "." + LAYOUT_CLSNAME;
            case Record:
                return getUnexposedTypeClassName(type) + "." + LAYOUT_CLSNAME;
            default:
                throw new IllegalArgumentException("Unsupport type: " + type.kind());
        }
    }

    public void addField(FileWriter f, Cursor c, Type parentType){
        String fieldName = c.spelling();
        Type t = c.type();
        try {
            String clsName = getTypeClassName(t);
            // get offset in bytes
            long offset = parentType.getOffsetOf(fieldName) >> 3;
            String newStmt;
            switch(t.kind()) {
                case ConstantArray:
                    newStmt = "array(new " + getTypeClassName(t.getElementType()) + "[" + t.getNumberOfElements() + "])";
                    break;
                case Unexposed:
                    newStmt = "inner(new " + clsName  + "(getRuntime()))";
                    break;
                default:
                    // No offset as array or inner cannot specify offset, simply expand the size
                    // newStmt = "new " + clsName + "(new Offset(" + offset + "))";
                    newStmt = "new " + clsName + "()";
                    break;
            }

            String decl = "    public final " + clsName + " " + fieldName + " = " + newStmt + ";\n";
            f.write(decl);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Create the StructLayout class describe the layout of the struct
     * defined at the Cursor
     *
     * @param f The output file
     * @param outterClsName The Java class name to have this class as Layout
     * @param c The libclang Cursor defines the struct
     * @return The layout class name
     * @throws IOException
     */
    public String createLayout(FileWriter f, String outterClsName, Cursor c) throws IOException {
        String clsName = LAYOUT_CLSNAME;
        // class
        f.append("public final static class ");
        f.append(clsName);
        f.append(" extends StructLayout {\n");
        f.append("    public ");
        f.append(clsName);
        f.append("(Runtime runtime) {\n");
        // No size as array or inner cannot specify offset, just expand from size
        // f.append("        super(runtime, " + c.type().size() + ");\n");
        f.append("        super(runtime);\n");
        f.append("    }\n\n");
        // fields
        try {
            c.stream()
             .filter(cx -> cx.kind() == CursorKind.FieldDecl)
             .forEachOrdered(cx -> addField(f, cx, c.type()));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
        // close class definition
        f.append("}\n");
        return clsName;
    }

    public String create(Cursor c) throws IOException {
        assert(c.kind() == CursorKind.StructDecl);
        String name = c.spelling();
        if (name.isEmpty()) {
            // This happens when a typedef an anonymouns struct, i.e., typedef struct {} type;
            Type t = c.type();
            name = t.spelling();
        }

        String clsName = Utils.toClassName(name);
        // package name
        try (FileWriter f = new FileWriter(clsName + ".java")) {
            // package name
            if (! pkgName.isEmpty()) {
                f.append("package ");
                f.append(pkgName);
                f.append(";\n\n");
            }
            // import statements
            f.append("import jnr.ffi.Memory;\n");
            f.append("import jnr.ffi.Pointer;\n");
            f.append("import jnr.ffi.Runtime;\n");
            f.append("import jnr.ffi.StructLayout;\n");
            f.append("import jnr.ffi.mapper.DataConverter;\n");
            f.append("import java.util.concurrent.atomic.AtomicReference;\n");
            f.append("\n");
            // Java class skeleton
            f.append("public class " + clsName + " {\n");
            // Layout class
            String layoutClsName = createLayout(f, clsName, c);
            // fields
            f.append("    private final Runtime runtime;\n");
            f.append("    private final AtomicReference<" + layoutClsName + "> layout;\n");
            f.append("    private volatile Pointer pointer;\n");
            f.append("    public final static DataConverter<" + clsName + ", Pointer> CONVERTER\n");
            f.append("         = DataConverter.pointer(" + clsName + "::of, " + clsName + "::pointer);\n");
            f.append("\n");
            // constuctor
            f.append("protected " + clsName + "(Runtime runtime) {\n");
            f.append("    this.runtime = runtime;\n");
            f.append("    this.layout = new AtomicReference<>();\n");
            f.append("}\n\n");
            // layout method
            f.append("public " + layoutClsName + " layout() {\n");
            f.append("    return layout.updateAndGet(x -> (x == null) ? new " + layoutClsName + "(runtime) : x);\n");
            f.append("}\n\n");
            // allocate function
            f.append("public static " + clsName + " allocate(Runtime runtime) {\n");
            f.append("    " + clsName + " instance = new " + clsName + "(runtime);\n");
            f.append("    instance.pointer = Memory.allocate(runtime, instance.layout().size());\n");
            f.append("    return instance;\n");
            f.append("}\n\n");
            // converter
            f.append("public static " + clsName + " of(Pointer ptr) {\n");
            f.append("    return new " + clsName + "(ptr.getRuntime());\n");
            f.append("}\n\n");
            f.append("public Pointer pointer() {\n");
            f.append("    return pointer;\n");
            f.append("}\n\n");
            // done
            f.append("}\n\n");
        }
        return clsName;
    }
}
