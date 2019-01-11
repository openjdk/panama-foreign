/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This class is used to generate the files "libTestDowncall.c" and corresponding header "libTestDowncall.h".
 * To generate the .c file, run w/o arguments; to generate .h file, use the 'header' argument.
 */
public class TestDowncallGenerator {

    enum Ret {
        VOID,
        NON_VOID;
    }

    enum StructFieldType {
        INT("int"),
        FLOAT("float"),
        DOUBLE("double"),
        POINTER("void*");

        String typeStr;

        StructFieldType(String typeStr) {
            this.typeStr = typeStr;
        }

        @SuppressWarnings("unchecked")
        static List<List<StructFieldType>>[] perms = new List[10];

        static List<List<StructFieldType>> perms(int i) {
            if (perms[i] == null) {
                perms[i] = generateTest(i, values());
            }
            return perms[i];
        }
    }

    enum ParamType {
        INT("int"),
        FLOAT("float"),
        DOUBLE("double"),
        POINTER("void*"),
        STRUCT("struct S");

        private String typeStr;

        ParamType(String typeStr) {
            this.typeStr = typeStr;
        }

        String type(List<StructFieldType> fields) {
            return this == STRUCT ?
                    typeStr + "_" + sigCode(fields) :
                    typeStr;
        }

        @SuppressWarnings("unchecked")
        static List<List<ParamType>>[] perms = new List[10];

        static List<List<ParamType>> perms(int i) {
            if (perms[i] == null) {
                perms[i] = generateTest(i, values());
            }
            return perms[i];
        }
    }

    static final int MAX_FIELDS = 3;
	static final int MAX_PARAMS = 3;
	static final int CHUNK_SIZE = 600;

	static int functions = 0;

    public static void main(String[] args) {
        boolean header = args.length > 0 && args[0].equals("header");

        if (header) {
            System.out.println(
                "#ifdef _WIN64\n" + 
                "#define EXPORT __declspec(dllexport)\n" +
                "#else\n" +
                "#define EXPORT\n" +
                "#endif\n"
            );

            for (int j = 1; j <= MAX_FIELDS; j++) {
                for (List<StructFieldType> fields : StructFieldType.perms(j)) {
                    generateStructDecl(fields);
                }
            }
        } else {
            System.out.println(
                "#include \"libTestDowncall.h\"\n" +
                "#ifdef __clang__\n" +
                "#pragma clang optimize off\n" +
                "#elif defined __GNUC__\n" +
                "#pragma GCC optimize (\"O0\")\n" +
                "#elif defined _MSC_BUILD\n" + 
                "#pragma optimize( \"\", off )\n" +
                "#endif\n"
            );
        }

        for (Ret r : Ret.values()) {
            for (int i = 0; i <= MAX_PARAMS; i++) {
                if (r != Ret.VOID && i == 0) continue;
                for (List<ParamType> ptypes : ParamType.perms(i)) {
                    if (ptypes.contains(ParamType.STRUCT)) {
                        for (int j = 1; j <= MAX_FIELDS; j++) {
                            for (List<StructFieldType> fields : StructFieldType.perms(j)) {
                                generateFunction(r, ptypes, fields, header);
                            }
                        }
                    } else {
                        generateFunction(r, ptypes, List.of(), header);
                    }
                }
            }
        }
    }

    static <Z> List<List<Z>> generateTest(int i, Z[] elems) {
        List<List<Z>> res = new ArrayList<>();
        generateTest(i, new Stack<>(), elems, res);
        return res;
    }

    static <Z> void generateTest(int i, Stack<Z> combo, Z[] elems, List<List<Z>> results) {
        if (i == 0) {
            results.add(new ArrayList<>(combo));
        } else {
            for (Z z : elems) {
                combo.push(z);
                generateTest(i - 1, combo, elems, results);
                combo.pop();
            }
        }
    }

    static <Z extends Enum<Z>> String sigCode(List<Z> elems) {
        return elems.stream().map(p -> p.name().charAt(0) + "").collect(Collectors.joining());
    }

    static void generateStructDecl(List<StructFieldType> fields) {
        String structCode = sigCode(fields);
        List<String> fieldDecls = new ArrayList<>();
        for (int i = 0 ; i < fields.size() ; i++) {
            fieldDecls.add(String.format("%s p%d;", fields.get(i).typeStr, i));
        }
        String res = String.format("struct S_%s { %s };", structCode,
                fieldDecls.stream().collect(Collectors.joining(" ")));
        System.out.println(res);
    }

    static void generateFunction(Ret ret, List<ParamType> params, List<StructFieldType> fields, boolean declOnly) {
        String retType = ret == Ret.VOID ? "void" : params.get(0).type(fields);
        String retCode = ret == Ret.VOID ? "V" : params.get(0).name().charAt(0) + "";
        String sigCode = sigCode(params);
		String structCode = sigCode(fields);
        List<String> paramDecls = new ArrayList<>();
        for (int i = 0 ; i < params.size() ; i++) {
            paramDecls.add(String.format("%s p%d", params.get(i).type(fields), i));
        }
        String sig = paramDecls.isEmpty() ?
                "void" :
                paramDecls.stream().collect(Collectors.joining(", "));
        String body = ret == Ret.VOID ? "{ }" : "{ return p0; }";
        int fCode = functions++ / CHUNK_SIZE;
        String res = String.format("EXPORT %s f%d_%s_%s_%s(%s) %s", retType, fCode, retCode, sigCode, structCode,
                sig, declOnly ? ";" : body);
        System.out.println(res);
    }
}
