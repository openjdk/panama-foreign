/*
 * Note: to run this test manually, you need to build the tests first to get native
 * libraries compiled, and then execute it with plain jtreg, like:
 *
 *  $ bin/jtreg -jdk:<path-to-tested-jdk> \
 *              -nativepath:<path-to-build-dir>/support/test/jdk/jtreg/native/lib/ \
 *              -concurrency:auto \
 *              ./test/jdk/java/foreign/TestMatrix.java
 */

/*
 * @test id=UpcallHighArity-FFTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TFTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FTTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TTTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FFTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TFTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FTTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TTTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FFFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TFFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FTFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TTFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FFFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TFFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-FTFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=UpcallHighArity-TTFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcallHighArity
 *
 * @run testng/othervm/native/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcallHighArity
 */

/* @test id=Downcall-FF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestDowncall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   TestDowncall
 */

/* @test id=Downcall-TF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestDowncall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   TestDowncall
 */

/* @test id=Downcall-FT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestDowncall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   TestDowncall
 */

/* @test id=Downcall-TT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestDowncall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   TestDowncall
 */

/* @test id=Upcall-TFTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-FTTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-TTTT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-TFTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */

/* @test id=Upcall-FTTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */

/* @test id=Upcall-TTTF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */

/* @test id=Upcall-TFFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-FTFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-TTFT
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=true
 *   TestUpcall
 */

/* @test id=Upcall-TFFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */

/* @test id=Upcall-FTFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */

/* @test id=Upcall-TTFF
 * @enablePreview
 * @requires ((os.arch == "amd64" | os.arch == "x86_64") & sun.arch.data.model == "64") | os.arch == "aarch64"
 * @build NativeTestHelper CallGeneratorHelper TestUpcall
 *
 * @run testng/othervm/manual
 *   --enable-native-access=ALL-UNNAMED
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_SPEC=true
 *   -Djdk.internal.foreign.ProgrammableInvoker.USE_INTRINSICS=true
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_SPEC=false
 *   -Djdk.internal.foreign.ProgrammableUpcallHandler.USE_INTRINSICS=false
 *   TestUpcall
 */
