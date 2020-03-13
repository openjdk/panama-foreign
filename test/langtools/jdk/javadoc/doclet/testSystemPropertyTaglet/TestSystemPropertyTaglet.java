/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 5076751 8234746
 * @summary System properties documentation needed in javadoc
 * @library /tools/lib ../../lib
 * @modules jdk.javadoc/jdk.javadoc.internal.tool
 * @build javadoc.tester.* toolbox.ToolBox builder.ClassBuilder
 * @run main TestSystemPropertyTaglet
 */

import java.nio.file.Path;
import java.nio.file.Paths;

import builder.ClassBuilder;
import builder.ClassBuilder.MethodBuilder;
import toolbox.ToolBox;

import javadoc.tester.JavadocTester;

public class TestSystemPropertyTaglet extends JavadocTester {

    final ToolBox tb;

    public static void main(String... args) throws Exception {
        TestSystemPropertyTaglet tester = new TestSystemPropertyTaglet();
        tester.runTests(m -> new Object[] { Paths.get(m.getName()) });
    }

    TestSystemPropertyTaglet() {
        tb = new ToolBox();
    }

    @Test
    public void test(Path base) throws Exception {
        javadoc("-d", base.resolve("out").toString(),
                "--module-source-path", testSrc,
                "--module", "mymodule");

        checkExit(Exit.OK);

        checkOrder("mymodule/mypackage/MyAnnotation.html",
                   "<h1 title=\"Annotation Type MyAnnotation\" class=\"title\">Annotation Type MyAnnotation</h1>",
                   "(annotation) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Element Details</h2>",
                   "(annotation/method) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/mypackage/MyClass.html",
                   "<h1 title=\"Class MyClass\" class=\"title\">Class MyClass</h1>",
                   "(class) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Field Details</h2>",
                   "(class/field) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "(class/static-field) the <code><span id=\"test.property-2\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Constructor Details</h2>",
                   "(class/constructor) the <code><span id=\"test.property-3\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Method Details</h2>",
                   "(class/static-method) the <code><span id=\"test.property-4\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "(class/method) the <code><span id=\"test.property-5\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/mypackage/MyEnum.html",
                   "<h1 title=\"Enum MyEnum\" class=\"title\">Enum MyEnum</h1>",
                   "(enum) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Enum Constant Details</h2>",
                   "(enum/constant) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/mypackage/MyError.html",
                   "<h1 title=\"Class MyError\" class=\"title\">Class MyError</h1>",
                   "(error) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Constructor Details</h2>",
                   "(error/constructor) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/mypackage/MyException.html",
                   "<h1 title=\"Class MyException\" class=\"title\">Class MyException</h1>",
                   "(exception) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Constructor Details</h2>",
                   "(exception/constructor) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code>",
                   "");

        checkOrder("mymodule/mypackage/MyInterface.html",
                   "<h1 title=\"Interface MyInterface\" class=\"title\">Interface MyInterface</h1>",
                   "(interface) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Field Details</h2>",
                   "(interface/constant) the <code><span id=\"test.property-1\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "<h2>Method Details</h2>",
                   "(interface/method-1) the <code><span id=\"test.property-2\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "(interface/method-2) the <code><span id=\"test.property-3\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/module-summary.html",
                   "<h1 title=\"Module\" class=\"title\">Module&nbsp;mymodule</h1>",
                   "(module) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("mymodule/mypackage/package-summary.html",
                   "<h1 title=\"Package\" class=\"title\">Package&nbsp;mypackage</h1>",
                   "(package) the <code><span id=\"test.property\" class=\"searchTagResult\">test.property</span></code> system property.",
                   "");

        checkOrder("index-all.html",
                   "<h2 class=\"title\" id=\"I:T\">T</h2>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyAnnotation.html#test.property\">test.property</a></span>" +
                           " - Search tag in annotation type mypackage.MyAnnotation</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property\">test.property</a></span>" +
                           " - Search tag in class mypackage.MyClass</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyEnum.html#test.property\">test.property</a></span>" +
                           " - Search tag in enum mypackage.MyEnum</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyError.html#test.property\">test.property</a></span>" +
                           " - Search tag in error mypackage.MyError</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyException.html#test.property\">test.property</a></span>" +
                           " - Search tag in exception mypackage.MyException</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyInterface.html#test.property\">test.property</a></span>" +
                           " - Search tag in interface mypackage.MyInterface</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/module-summary.html#test.property\">test.property</a></span>" +
                           " - Search tag in module mymodule</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyAnnotation.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyAnnotation.value()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property-2\">test.property</a></span>" +
                           " - Search tag in mypackage.MyClass.INT_CONSTANT</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property-3\">test.property</a></span>" +
                           " - Search tag in mypackage.MyClass.MyClass()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyClass.intField</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property-5\">test.property</a></span>" +
                           " - Search tag in mypackage.MyClass.run()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyClass.html#test.property-4\">test.property</a></span>" +
                           " - Search tag in mypackage.MyClass.value()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyEnum.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyEnum.X</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyEnum.html#test.property-2\">test.property</a></span>" +
                           " - Search tag in mypackage.MyEnum.m()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyError.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyError.MyError()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyException.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyException.MyException()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyInterface.html#test.property-1\">test.property</a></span>" +
                           " - Search tag in mypackage.MyInterface.INT_CONSTANT</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyInterface.html#test.property-2\">test.property</a></span>" +
                           " - Search tag in mypackage.MyInterface.m()</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/MyInterface.html#test.property-3\">test.property</a></span>" +
                           " - Search tag in mypackage.MyInterface.m(String...)</dt>\n<dd>System Property</dd>",
                   "<dt><span class=\"searchTagLink\"><a href=\"mymodule/mypackage/package-summary.html#test.property\">test.property</a></span>" +
                           " - Search tag in package mypackage</dt>\n<dd>System Property</dd>",
                   "");

        checkOutput("tag-search-index.js", true,
                    "{\"l\":\"test.property\",\"h\":\"annotation type mypackage.MyAnnotation\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyAnnotation.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"class mypackage.MyClass\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"enum mypackage.MyEnum\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyEnum.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"error mypackage.MyError\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyError.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"exception mypackage.MyException\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyException.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"interface mypackage.MyInterface\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyInterface.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"module mymodule\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/module-summary.html#test.property\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyAnnotation.value()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyAnnotation.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyClass.INT_CONSTANT\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property-2\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyClass.MyClass()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property-3\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyClass.intField\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyClass.run()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property-5\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyClass.value()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyClass.html#test.property-4\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyEnum.X\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyEnum.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyEnum.m()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyEnum.html#test.property-2\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyError.MyError()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyError.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyException.MyException()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyException.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyInterface.INT_CONSTANT\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyInterface.html#test.property-1\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyInterface.m()\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyInterface.html#test.property-2\"}",
                    "{\"l\":\"test.property\",\"h\":\"mypackage.MyInterface.m(String...)\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/MyInterface.html#test.property-3\"}",
                    "{\"l\":\"test.property\",\"h\":\"package mypackage\"" +
                            ",\"d\":\"System Property\",\"u\":\"mymodule/mypackage/package-summary.html#test.property\"}",
                    "");
    }

    @Test
    public void testSystemPropertyWithinATag(Path base) throws Exception {
        Path srcDir = base.resolve("src");
        Path outDir = base.resolve("out");

        new ClassBuilder(tb, "pkg2.A")
                .setModifiers("public", "class")
                .addMembers(MethodBuilder.parse("public void func(){}")
                        .setComments("a within a : <a href='..'>{@systemProperty user.name}</a>"))
                .write(srcDir);

        javadoc("-d", outDir.toString(),
                "-sourcepath", srcDir.toString(),
                "pkg2");

        checkExit(Exit.OK);

        checkOutput(Output.OUT, true,
                "warning: {@systemProperty} tag, which expands to <a>, within <a>");
    }
}
