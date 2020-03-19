/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.nio.file.Path;
import org.testng.annotations.Test;

/*
 * @test
 * @modules jdk.incubator.jextract
 * @build JextractToolRunner
 * @bug 8240181
 * @run testng/othervm -Djdk.incubator.foreign.Foreign=permit -Duser.language=en --add-modules jdk.incubator.jextract Test8240181
 */
public class Test8240181 extends JextractToolRunner {
    @Test
    public void testAnonymousEnum() {
        Path anonenumOutput = getOutputFilePath("anonenumgen");
        Path anonenumH = getInputFilePath("anonenum.h");
        run("-d", anonenumOutput.toString(), anonenumH.toString()).checkSuccess();
        try(Loader loader = classLoader(anonenumOutput)) {
            Class<?> cls = loader.loadClass("anonenum_h");
            checkIntField(cls, "RED", 0xff0000);
            checkIntField(cls, "GREEN", 0x00ff00);
            checkIntField(cls, "BLUE", 0x0000ff);
            checkIntField(cls, "Java", 0);
            checkIntField(cls, "C", 1);
            checkIntField(cls, "CPP", 2);
            checkIntField(cls, "Python", 3);
            checkIntField(cls, "Ruby", 4);
            checkIntField(cls, "ONE", 1);
            checkIntField(cls, "TWO", 2);
        } finally {
            deleteDir(anonenumOutput);
        }
    }
}
