/*
 * Copyright (C) 2014 hjen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.sun.tools.jextract.jnr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import com.sun.tools.jextract.*;
import jdk.internal.clang.Cursor;
import jdk.internal.clang.Index;
import jdk.internal.clang.LibClang;
import jdk.internal.clang.Type;

public class JnrCodeFactory extends CodeFactory {
    private final StructFactory helper;
    private final LibFactory lib;

    private JnrCodeFactory(LibFactory lib, StructFactory helper) {
        this.lib = lib;
        this.helper = helper;
    }

    /**
     * Setup a factory for the target class or interface
     * @param pkg_name The package name for the generated code
     * @param name The class or interface name for globals
     * @return The CodeFactory itself
     */
    public static JnrCodeFactory setup(String pkg_name, String name) {
        try {
            StructFactory helper = new StructFactory();
            LibFactory lib = LibFactory.create(pkg_name, name, helper);
            return new JnrCodeFactory(lib, helper);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public CodeFactory addType(JType t, Cursor c) {
        try {
            switch(c.kind()) {
                case StructDecl:
                    helper.create(c);
                    break;
                case FunctionDecl:
                    lib.addFunction(c);
                    break;
                default:
                    new Printer().dumpCursor(c);
                    break;
            }
        } catch(IOException ex) {
            ex.printStackTrace(System.err);
        }

        return this;
    }

    protected String getTypeDescriptor(Type type) {
        try {
            switch (type.kind()) {
                case Typedef:
            }
            String str = helper.getTypeClassName(type);
            return str.replace('.', '/');
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    @Override
    public void produce() {
        try {
            lib.build();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        Index index = LibClang.createIndex();
        Cursor tuCursor = index.parse(args[0], diag -> { System.err.println(diag.toString()); }, false);
        final Collection<String> set = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
        JnrCodeFactory cf = JnrCodeFactory.setup("", "MiniLibc");

        Predicate<Cursor> inFile = c -> {
            Path p = c.getSourceLocation().getFileLocation().path().toAbsolutePath();
            return set.stream().anyMatch(s -> Paths.get(s).toAbsolutePath().equals(p));
        };

        tuCursor.children()
                .filter(Cursor::isDeclaration)
                .filter(inFile)
                .forEach(c -> cf.addType(JType.Void, c));

        cf.produce();
    }

    @Override
    protected Map<String, byte[]> collect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
