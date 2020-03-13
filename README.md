
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
This is a repo of OpenJDK Project Panama: Interconnecting JVM and native code. You can find more information at: https://openjdk.java.net/projects/panama/

You can find the Project Panama JBS dashboard at: https://bugs.openjdk.java.net/Dashboard.jspa?selectPageId=18412

You can find early access (EA) snapshots at: http://jdk.java.net/panama/

About the panama-foreign repo
===================
This repo houses three main branches:
- **foreign-memaccess**: Contains the developement of the foreign memory access API for [JEP 370](https://openjdk.java.net/jeps/370), which can be used to interact with different kinds of memory resource, inluding so-called off-heap or native memory.
- **foreign-abi**: Contains the developement of the foreign function interface (FFI), which can be used to call native code in a  .dll/.so/.dylib, or to create a native function pointer to a Java method which can be passed to code in a native library.
- **foreign-jextract**: Contains the developement of the jextract API, which can be used to create an abstract syntax tree from a C header file. There's also the accompanying jextract tool, which is a wrapper for the API and can be used to generate a Java interface to call the C library described by a given header file.

The foreign-jextract branch depends on the foreign-abi branch, which in turn depends on the foreign-memaccess branch. As such, the foreign-jextract is the most complete of the three and therefore the default branch of this repository.

Building notes
===================
Jextract requires [LLVM 9.x](https://releases.llvm.org/download.html) as a dependency. So, if you want to build the foreign-jextract branch yourself you will need to pass `--with-libclang=/path/to/llvm/root` when generating a build configuration. Please see [doc/building.md](https://github.com/openjdk/panama-foreign/blob/foreign-jextract/doc/building.md) for general building instructions.
