/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @library /test/lib
 * @build TestEnableNativeAccess
 *        panama_module/*
 *        org.openjdk.foreigntest.PanamaMainUnnamedModule
 * @run testng/othervm/timeout=180 TestEnableNativeAccess
 * @summary Basic test for java --enable-native-access=<module name>
 */

//  from old test, needed?
//  @requires vm.compMode != "Xcomp"
//  @modules java.base/jdk.internal.misc
//           java.base/sun.security.x509
// @build
// *        jdk.test.lib.compiler.CompilerUtils
// *        jdk.test.lib.util.JarUtils

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.process.OutputAnalyzer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Basic test of --enable-native-access=<module name> with expected behaviour:
 *
 *  if flag present:        - permit access to modules that are specified
 *                          - deny access to modules that are not specified (throw IllegalCallerException)
 *  if flag not present:    - permit access to all modules and omit a warning
*/

@Test
public class TestEnableNativeAccess {

    static final String TEST_SRC = System.getProperty("test.src");
    static final String TEST_CLASSES = System.getProperty("test.classes");
    static final String MODULE_PATH = System.getProperty("jdk.module.path");

    static final String PANAMA_MAIN = "panama_module/org.openjdk.foreigntest.PanamaMain";
    static final String PANAMA_REFLECTION = "panama_module/org.openjdk.foreigntest.PanamaMainReflection";
    static final String PANAMA_INVOKE = "panama_module/org.openjdk.foreigntest.PanamaMainInvoke";
    static final String UNNAMED = "org.openjdk.foreigntest.PanamaMainUnnamedModule";

    /**
     * Represents the expected result of a test.
     */
    static final class Result {
        private final boolean success;
        private final List<String> expectedOutput = new ArrayList<>();
        private final List<String> notExpectedOutput = new ArrayList<>();

        Result(boolean success) {
            this.success = success;
        }

        Result expect(String msg) {
            expectedOutput.add(msg);
            return this;
        }

        Result doNotExpect(String msg) {
            notExpectedOutput.add(msg);
            return this;
        }

        boolean shouldSucceed() {
            return success;
        }

        Stream<String> expectedOutput() {
            return expectedOutput.stream();
        }

        Stream<String> notExpectedOutput() {
            return notExpectedOutput.stream();
        }

        @Override
        public String toString() {
            String s = (success) ? "success" : "failure";
            for (String msg : expectedOutput) {
                s += "/" + msg;
            }
            return s;
        }
    }

    static Result success() {
        return new Result(true);
    }

    static Result successNoWarning() {
        return success().doNotExpect("WARNING");
    }

    static Result successWithWarning() {
        return success().expect("WARNING");
    }

    static Result fail(String expectedOutput) {
        return new Result(false).expect(expectedOutput).doNotExpect("WARNING");
    }

    static Result failWithWarning(String expectedOutput) {
        return new Result(false).expect(expectedOutput).expect("WARNING");
    }

    @DataProvider(name = "denyCases")
    public Object[][] denyCases() {
        return new Object[][] {
                { "accessPublicClassNonExportedPackage", fail("IllegalAccessError") },
                { "accessPublicClassJdk9NonExportedPackage", fail("IllegalAccessError") },

                { "reflectPublicMemberExportedPackage", successNoWarning() },
                { "reflectNonPublicMemberExportedPackage", fail("IllegalAccessException") },
                { "reflectPublicMemberNonExportedPackage", fail("IllegalAccessException") },
                { "reflectNonPublicMemberNonExportedPackage", fail("IllegalAccessException") },
                { "reflectPublicMemberJdk9NonExportedPackage", fail("IllegalAccessException") },
                { "reflectPublicMemberApplicationModule", successNoWarning() },

                { "setAccessiblePublicMemberExportedPackage", successNoWarning() },
                { "setAccessibleNonPublicMemberExportedPackage", fail("InaccessibleObjectException") },
                { "setAccessiblePublicMemberNonExportedPackage", fail("InaccessibleObjectException") },
                { "setAccessibleNonPublicMemberNonExportedPackage", fail("InaccessibleObjectException") },
                { "setAccessiblePublicMemberJdk9NonExportedPackage", fail("InaccessibleObjectException") },
                { "setAccessiblePublicMemberApplicationModule", successNoWarning() },
                { "setAccessibleNotPublicMemberApplicationModule", fail("InaccessibleObjectException") },

                { "privateLookupPublicClassExportedPackage", fail("IllegalAccessException") },
                { "privateLookupNonPublicClassExportedPackage", fail("IllegalAccessException") },
                { "privateLookupPublicClassNonExportedPackage", fail("IllegalAccessException") },
                { "privateLookupNonPublicClassNonExportedPackage", fail("IllegalAccessException") },
                { "privateLookupPublicClassJdk9NonExportedPackage", fail("IllegalAccessException") },
        };
    }

    @DataProvider(name = "permitCases")
    public Object[][] permitCases() {
        return new Object[][] {
                { "accessPublicClassNonExportedPackage", successNoWarning() },
                { "accessPublicClassJdk9NonExportedPackage", fail("IllegalAccessError") },

                { "reflectPublicMemberExportedPackage", successNoWarning() },
                { "reflectNonPublicMemberExportedPackage", fail("IllegalAccessException") },
                { "reflectPublicMemberNonExportedPackage", successWithWarning() },
                { "reflectNonPublicMemberNonExportedPackage", fail("IllegalAccessException") },
                { "reflectPublicMemberJdk9NonExportedPackage", fail("IllegalAccessException") },

                { "setAccessiblePublicMemberExportedPackage", successNoWarning()},
                { "setAccessibleNonPublicMemberExportedPackage", successWithWarning() },
                { "setAccessiblePublicMemberNonExportedPackage", successWithWarning() },
                { "setAccessibleNonPublicMemberNonExportedPackage", successWithWarning() },
                { "setAccessiblePublicMemberJdk9NonExportedPackage", fail("InaccessibleObjectException") },
                { "setAccessiblePublicMemberApplicationModule", successNoWarning() },
                { "setAccessibleNotPublicMemberApplicationModule", fail("InaccessibleObjectException") },

                { "privateLookupPublicClassExportedPackage", successWithWarning() },
                { "privateLookupNonPublicClassExportedPackage", successWithWarning() },
                { "privateLookupPublicClassNonExportedPackage", successWithWarning() },
                { "privateLookupNonPublicClassNonExportedPackage",  successWithWarning() },
                { "privateLookupPublicClassJdk9NonExportedPackage", fail("IllegalAccessException") },
                { "privateLookupPublicClassApplicationModule", fail("IllegalAccessException") },
        };
    }

    /**
     * Checks an expected result with the output captured by the given
     * OutputAnalyzer.
     */
    void checkResult(Result expectedResult, OutputAnalyzer outputAnalyzer) {
        expectedResult.expectedOutput().forEach(outputAnalyzer::shouldContain);
        expectedResult.notExpectedOutput().forEach(outputAnalyzer::shouldNotContain);
        int exitValue = outputAnalyzer.getExitValue();
        if (expectedResult.shouldSucceed()) {
            assertTrue(exitValue == 0);
        } else {
            assertTrue(exitValue != 0);
        }
    }

    /**
     * Runs the test to execute the given test action. The VM is run with the
     * given VM options and the output checked to see that it matches the
     * expected result.
     */
    OutputAnalyzer run(String action, String cls, Result expectedResult, String... vmopts)
            throws Exception
    {
        Stream<String> s1 = Stream.of(vmopts);
        Stream<String> s2 = Stream.of("--enable-preview", "-p", MODULE_PATH, "-m", cls, action);
        String[] opts = Stream.concat(s1, s2).toArray(String[]::new);
        OutputAnalyzer outputAnalyzer = ProcessTools
                .executeTestJava(opts)
                .outputTo(System.out)
                .errorTo(System.out);
        if (expectedResult != null)
            checkResult(expectedResult, outputAnalyzer);
        return outputAnalyzer;
    }
//
//    OutputAnalyzer run(String action, String... vmopts) throws Exception {
//        return run(action, null, vmopts);
//    }

//    @Test(dataProvider = "denyCases")
//    public void testDefault(String action, Result expectedResult) throws Exception {
//        run(action, expectedResult);
//    }
//
//    @Test(dataProvider = "denyCases")
//    public void testDeny(String action, Result expectedResult) throws Exception {
//        run(action, expectedResult, "--illegal-access=deny");
//    }
//
//    @Test(dataProvider = "permitCases")
//    public void testPermit(String action, Result expectedResult) throws Exception {
//        run(action, expectedResult, "--illegal-access=permit");
//    }
//
//    @Test(dataProvider = "permitCases")
//    public void testWarn(String action, Result expectedResult) throws Exception {
//        run(action, expectedResult, "--illegal-access=warn");
//    }
//
//    @Test(dataProvider = "permitCases")
//    public void testDebug(String action, Result expectedResult) throws Exception {
//        // expect stack trace with WARNING
//        if (expectedResult.expectedOutput().anyMatch("WARNING"::equals)) {
//            expectedResult.expect("TryAccess.main");
//        }
//        run(action, expectedResult, "--illegal-access=debug");
//    }
//
//    /**
//     * Test that without --enable-native-access, a multi-line warning is printed
//     * on first access of a module.
//     */
//    public void testWarnOnFirstNativeAccess() throws Exception {
//        String action1 = "reflectPublicMemberNonExportedPackage";
//        String action2 = "setAccessibleNonPublicMemberExportedPackage";
//        int warningCount = count(run(action1, "--illegal-access=permit").asLines(), "WARNING");
//        assertTrue(warningCount > 0);  // multi line warning
//
//        // same native access
//        List<String> output1 = run(action1 + "," + action1, "--illegal-access=permit").asLines();
//        assertTrue(count(output1, "WARNING") == warningCount);
//
//        // different native access
//        List<String> output2 = run(action1 + "," + action2, "--illegal-access=permit").asLines();
//        assertTrue(count(output2, "WARNING") == warningCount);
//    }
//
    /**
     * Test that without --enable-native-access, a one-line warning is printed
     * on each native access of a module.
     */
    public void testWarnPerNativeAccess() throws Exception {
        String action1 = "panama_enable_native_access_first";
        String action2 = "panama_enable_native_access_second";

        // same native access
        String repeatedActions = action1 + "," + action1;
        List<String> output1 = run(repeatedActions, PANAMA_MAIN, successWithWarning()).asLines();
        assertTrue(count(output1, "WARNING") == 1);

        // different native access
        String differentActions = action1 + "," + action2;
        List<String> output2 = run(differentActions, PANAMA_MAIN, successWithWarning()).asLines();
        assertTrue(count(output2, "WARNING") == 2);
    }

    /**
     * Specify --enable-native-access more than once, each list of module names is appended
     */
    public void testRepeatedOption() throws Exception {
        run("panama_enable_native_access_last_one_wins", PANAMA_MAIN,
                successWithWarning(), "--enable-native-access=java.base", "--enable-native-access=panama_module");
        run("panama_enable_native_access_last_one_wins", PANAMA_MAIN,
                successWithWarning(), "--enable-native-access=panama_module", "--enable-native-access=java.base");
    }

    /**
     * Specify bad value to --enable-native-access
     */
    public void testBadValue() throws Exception {
        run("panama_enable_native_access_warn_unknown_module", PANAMA_MAIN,
                failWithWarning("WARNING: Unknown module: BAD specified to --enable-native-access"),
                "--enable-native-access=BAD");
    }

    private int count(Iterable<String> lines, CharSequence cs) {
        int count = 0;
        for (String line : lines) {
            if (line.contains(cs)) count++;
        }
        return count;
    }
}
