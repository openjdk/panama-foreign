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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import jdk.incubator.foreign.MemorySegment;
import java.nio.file.Path;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/*
 * @test
 * @bug 8252634
 * @summary jextract should generate type annotations for C types
 * @library /test/lib
 * @modules jdk.incubator.jextract
 * @build JextractToolRunner
 * @run testng/othervm -Dforeign.restricted=permit Test8252634
 */
public class Test8252634 extends JextractToolRunner {
    private Class<? extends Annotation> cAnnoClass;
    private Method cValueMethod;

    @Test
    public void test() throws Throwable {
        Path outputPath = getOutputFilePath("output8252634");
        Path headerFile = getInputFilePath("test8252634.h");
        run("-d", outputPath.toString(), headerFile.toString()).checkSuccess();
        try(Loader loader = classLoader(outputPath)) {
            Class<?> headerClass = loader.loadClass("test8252634_h");
            this.cAnnoClass = (Class<? extends Annotation>)loader.loadClass("C");
            this.cValueMethod = findMethod(cAnnoClass, "value");
            Method make = findMethod(headerClass, "make", int.class, int.class);
            Parameter[] params = make.getParameters();
            checkAnnotation(params[0].getAnnotatedType(), "int");
            checkAnnotation(params[1].getAnnotatedType(), "int");
            checkAnnotation(make.getAnnotatedReturnType(), "struct Point*");
            Method func = findFirstMethod(headerClass, "func");
            params = func.getParameters();
            checkAnnotation(params[0].getAnnotatedType(), "int(*)(int)");

            Class<?> pointClass = loader.loadClass("test8252634_h$Point");
            Method xGetter = findMethod(pointClass, "x$get", MemorySegment.class);
            checkAnnotation(xGetter.getParameters()[0].getAnnotatedType(), "struct Point");
            checkAnnotation(xGetter.getAnnotatedReturnType(), "int");
            Method yGetter = findMethod(pointClass, "y$get", MemorySegment.class);
            checkAnnotation(yGetter.getParameters()[0].getAnnotatedType(), "struct Point");
            checkAnnotation(yGetter.getAnnotatedReturnType(), "int");
            Method xSetter = findMethod(pointClass, "x$set", MemorySegment.class, int.class);
            checkAnnotation(xSetter.getParameters()[0].getAnnotatedType(), "struct Point");
            checkAnnotation(xSetter.getParameters()[1].getAnnotatedType(), "int");
            Method ySetter = findMethod(pointClass, "y$set", MemorySegment.class, int.class);
            checkAnnotation(ySetter.getParameters()[0].getAnnotatedType(), "struct Point");
            checkAnnotation(ySetter.getParameters()[1].getAnnotatedType(), "int");
            Method allocate = findMethod(pointClass, "allocate");
            checkAnnotation(allocate.getAnnotatedReturnType(), "struct Point");
        } finally {
            deleteDir(outputPath);
        }
    }

    private void checkAnnotation(AnnotatedElement ae, String expected) throws Throwable {
        Object anno = ae.getAnnotation(cAnnoClass);
        assertEquals(cValueMethod.invoke(anno).toString(), expected);
    }
}
