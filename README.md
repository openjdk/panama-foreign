
Welcome to the JDK!
===================

For information about building the JDK, including how to retrieve all
of the source code, please see either of these files:

  * doc/building.html   (html version)
  * doc/building.md     (markdown version)

See http://openjdk.java.net/ for more information about the OpenJDK
Community and the JDK.

---
About Project Panama
===================
This repository contains changes which aim at improving the interoperability between the Java programming language and native libraries, which is one of the main goals of Project Panama. More information can be found at: https://openjdk.java.net/projects/panama/

The Project Panama JBS dashboard can be found at: https://bugs.openjdk.java.net/Dashboard.jspa?selectPageId=18412

Early acccess (EA) binary snapshots can be found at: http://jdk.java.net/panama/

About the panama-foreign repository
===================
This repository houses three main branches:
- **foreign-memaccess**: Contains the developement of the foreign memory access API for [JEP 370](https://openjdk.java.net/jeps/370), which can be used to interact with different kinds of memory resources, including so-called off-heap or native memory.
- **foreign-abi**: Contains the developement of the foreign function interface (FFI), which can be used to call native code in a .dll/.so/.dylib, or to create a native function pointer to a Java method which can be passed to code in a native library.
- **foreign-jextract**: Contains the development of an API to parse native headers, which can be used to create an abstract representation (declarations) from a C header file. This branch also provides an accompanying extraction tool (jextract), which is built on top of the API, and can be used to generate Java bindings to access functions and/or structs in a native library described by a given header file.

The foreign-jextract branch depends on the foreign-abi branch, which in turn depends on the foreign-memaccess branch. As such, the foreign-jextract branch is the most complete of the three and therefore the default branch of this repository.

Building notes
===================
Jextract requires [LLVM 9.x](https://releases.llvm.org/download.html) as a dependency. So, to build the foreign-jextract branch, the flag `--with-libclang=/path/to/llvm/root` must be supplied when generating a build configuration. Please see [doc/building.md](doc/building.md) for general building instructions.

Samples
=======

Please see [doc/panama_jextract.md](doc/panama_jextract.md) for examples on how to use `jextract` to work with common native libraries.
