/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.openjdk.bench.jdk.incubator.foreign.nio.support;

import java.nio.file.DirectoryStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DirectoryIterator implements DirectoryStream<String>, Iterator<String> {
    private final UnixNativeDispatcher dispatcher;
    private final long dir;

    // true when at EOF
    private boolean atEof;

    // next entry to return
    private String nextEntry;

    DirectoryIterator(UnixNativeDispatcher dispatcher, String path) {
        super();
        this.dispatcher = dispatcher;
        this.dir = dispatcher.opendir(path);
        atEof = false;
    }

    // Return true if file name is "." or ".."
    private boolean isSelfOrParent(String path) {
        byte nameAsBytes[] = path.getBytes();
        if (nameAsBytes[0] == '.') {
            if ((nameAsBytes.length == 1) ||
                (nameAsBytes.length == 2 && nameAsBytes[1] == '.')) {
                return true;
            }
        }
        return false;
    }

    // Returns next entry (or null)
    private String readNextEntry() {
        for (;;) {
            String name = null;
            name = dispatcher.readdir(dir);

            // EOF
            if (name == null) {
                atEof = true;
                return null;
            }

            // ignore "." and ".."
            if (!isSelfOrParent(name)) {
                return name;
            }
        }
    }

    @Override
    public synchronized boolean hasNext() {
        if (nextEntry == null && !atEof)
            nextEntry = readNextEntry();
        return nextEntry != null;
    }

    @Override
    public synchronized String next() {
        String result;
        if (nextEntry == null && !atEof) {
            result = readNextEntry();
        } else {
            result = nextEntry;
            nextEntry = null;
        }
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    @Override
    public Iterator<String> iterator() { return this; }

    @Override
    public void close() { dispatcher.closedir(dir); }
}