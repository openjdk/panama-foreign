% Using Panama "foreign" JDK

<?xml version="1.0" encoding="utf-8"?>

# Using Panama "foreign-jextract" JDK

You can build "foreign-jextract" branch of panama repo [https://github.com/openjdk/panama-foreign](https://github.com/openjdk/panama-foreign)

Using foreign function call in Java involves the following two steps:

1. Use **jextract** tool to generate java interface for your C header file(s)
2. Invoke C functions via the jextracted Java interface

## Hello World

### Hello World C Header (helloworld.h)

```C

#ifndef helloworld_h
#define helloworld_h

extern void helloworld(void);

#endif /* helloworld_h */


```

### Hello World C Source (helloworld.c)

```C

#include <stdio.h>

#include "helloworld.h"

void helloworld(void) {
    printf("Hello World!\n");
}

```

### Building Hello World

```sh

cc -shared -o libhelloworld.dylib helloworld.c

```


### jextract a Jar file for helloworld.h

```sh

jextract -t org.hello -lhelloworld helloworld.h

```

### Java program that uses extracted helloworld interface

```java

import static org.hello.helloworld_h.*;

public class HelloWorld {
    public static void main(String[] args) {
        helloworld();
    }
}

```

### Running the Java code that invokes helloworld

```sh

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign HelloWorld.java

```

## Embedding Python interpreter in your Java program (Mac OS)

### jextract Python.h

```sh

jextract -l python2.7 \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/python2.7/ \
  -t org.python \
   /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/python2.7/Python.h

```

### Java program that uses extracted Python interface

```java

import org.python.Cstring;
import static jdk.incubator.foreign.MemoryAddress.NULL;
// import jextracted python 'header' class
import static org.python.RuntimeHelper.*;
import static org.python.Python_h.*;

public class PythonMain {
    public static void main(String[] args) {
        String script = "print(sum([33, 55, 66])); print('Hello from Python!')\n";

        Py_Initialize();
        try (var s = Cstring.toCString(script)) {
            var str = s.baseAddress();
            PyRun_SimpleStringFlags(str, NULL);
            Py_Finalize();
        }
    }
}

```

### Running the Java code that calls Python interpreter

```sh

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/System/Library/Frameworks/Python.framework/Versions/2.7/lib \
    PythonMain.java

```

## Using readline library from Java code (Mac OS)

### jextract readline.h

```sh

jextract -l readline -t org.unix \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include \
   /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/readline/readline.h

```

### Java code that uses readline

```java

import org.unix.Cstring;
import static org.unix.RuntimeHelper.*;
import static org.unix.readline_h.*;

public class Readline {
    public static void main(String[] args) {
        try (var s = Cstring.toCString("name? ")) {
            var pstr = s.baseAddress();
            // call "readline" API
            var p = readline(pstr);

            // print char* as is
            System.out.println(p);
            // convert char* ptr from readline as Java String & print it
            System.out.println("Hello, " + Cstring.toJavaString(p));
        }
    }
}

```

### Running the java code that uses readline

```
java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/readline/lib/ Readline.java

```

## Using libcurl from Java (Mac OS)

### jextract curl.h

```sh

jextract -t org.unix -lcurl \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/ \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/curl/ \
  /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/curl/curl.h

```

### Java code that uses libcurl

```java

import org.unix.Cstring;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.unix.RuntimeHelper.*;
import static org.unix.curl_h.*;

public class CurlMain {
   public static void main(String[] args) {
       var urlStr = args[0];
       curl_global_init(CURL_GLOBAL_DEFAULT());
       var curl = curl_easy_init();
       if(!curl.equals(NULL)) {
           try (var s = Cstring.toCString(urlStr)) {
               var url = s.baseAddress();
               curl_easy_setopt(curl, CURLOPT_URL(), url);
               int res = curl_easy_perform(curl);
               if (res != CURLE_OK()) {
                   curl_easy_cleanup(curl);
               }
           }
       }
       curl_global_cleanup();
   }
}

```

### Running the java code that uses libcurl

```sh

# run this shell script by passing a URL as first argument
java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/lib CurlMain.java $*

```

## Using BLAS library

BLAS is a popular library that allows fast matrix and vector computation: [http://www.netlib.org/blas/](http://www.netlib.org/blas/).

### Installing OpenBLAS (Mac OS)

On Mac, blas is available as part of the OpenBLAS library: [https://github.com/xianyi/OpenBLAS/wiki](https://github.com/xianyi/OpenBLAS/wiki)

OpenBLAS is an optimized BLAS library based on GotoBLAS2 1.13 BSD version.

You can install openblas using HomeBrew

```sh

brew install openblas

```

It installs include and lib directories under /usr/local/opt/openblas

### jextracting cblas.h (MacOS)

The following command can be used to extract cblas.h on MacOs

```sh

jextract -C "-D FORCE_OPENBLAS_COMPLEX_STRUCT" \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
  -l openblas -t blas /usr/local/opt/openblas/include/cblas.h

```

### Java sample code that uses cblas library

```java

import jdk.incubator.foreign.NativeAllocationScope;
import blas.*;
import static blas.RuntimeHelper.*;
import static blas.cblas_h.*;

public class TestBlas {
    public static void main(String[] args) {
        int Layout;
        int transa;

        double alpha, beta;
        int m, n, lda, incx, incy, i;

        Layout = CblasColMajor();
        transa = CblasNoTrans();

        m = 4; /* Size of Column ( the number of rows ) */
        n = 4; /* Size of Row ( the number of columns ) */
        lda = 4; /* Leading dimension of 5 * 4 matrix is 5 */
        incx = 1;
        incy = 1;
        alpha = 1;
        beta = 0;
        try (NativeAllocationScope scope = NativeAllocationScope.unboundedScope()) {
            var a = Cdouble.allocateArray(m*n, scope);
            var x = Cdouble.allocateArray(n, scope);
            var y = Cdouble.allocateArray(n, scope);

            /* The elements of the first column */
            Cdouble.set(a, 0, 1.0);
            Cdouble.set(a, 1, 2.0);
            Cdouble.set(a, 2, 3.0);
            Cdouble.set(a, 3, 4.0);
            /* The elements of the second column */
            Cdouble.set(a, m, 1.0);
            Cdouble.set(a, m + 1, 1.0);
            Cdouble.set(a, m + 2, 1.0);
            Cdouble.set(a, m + 3, 1.0);
            /* The elements of the third column */
            Cdouble.set(a, m*2, 3.0);
            Cdouble.set(a, m*2 + 1, 4.0);
            Cdouble.set(a, m*2 + 2, 5.0);
            Cdouble.set(a, m*2 + 3, 6.0);
            /* The elements of the fourth column */
            Cdouble.set(a, m*3, 5.0);
            Cdouble.set(a, m*3 + 1, 6.0);
            Cdouble.set(a, m*3 + 2, 7.0);
            Cdouble.set(a, m*3 + 3, 8.0);
            /* The elemetns of x and y */
            Cdouble.set(x, 0, 1.0);
            Cdouble.set(x, 1, 2.0);
            Cdouble.set(x, 2, 1.0);
            Cdouble.set(x, 3, 1.0);
            Cdouble.set(y, 0, 0.0);
            Cdouble.set(y, 1, 0.0);
            Cdouble.set(y, 2, 0.0);
            Cdouble.set(y, 3, 0.0);
            cblas_dgemv(Layout, transa, m, n, alpha, a, lda, x, incx, beta, y, incy);
            /* Print y */
            for (i = 0; i < n; i++) {
                System.out.print(String.format(" y%d = %f\n", i, Cdouble.get(y, (long)i)));
            }
        }
    }
}

```

### Compiling and running the above BLAS sample

```sh

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/openblas/lib \
    TestBlas.java

```

## Using LAPACK library (Mac OS)

On Mac OS, lapack is installed under /usr/local/opt/lapack directory.

### jextracting lapacke.h

```sh

jextract \
   -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
   -l lapacke -t lapack \
   --filter lapacke.h \
   /usr/local/opt/lapack/include/lapacke.h

```

### Java sample code that uses LAPACK library

```java

import jdk.incubator.foreign.NativeAllocationScope;
import lapack.*;
import static lapack.lapacke_h.*;

public class TestLapack {
    public static void main(String[] args) {

        /* Locals */
        try (var scope = NativeAllocationScope.unboundedScope()) {
            var A = Cdouble.allocateArray(new double[]{
                    1, 2, 3, 4, 5, 1, 3, 5, 2, 4, 1, 4, 2, 5, 3
            }, scope);
            var b = Cdouble.allocateArray(new double[]{
                    -10, 12, 14, 16, 18, -3, 14, 12, 16, 16
            }, scope);
            int info, m, n, lda, ldb, nrhs;

            /* Initialization */
            m = 5;
            n = 3;
            nrhs = 2;
            lda = 5;
            ldb = 5;

            /* Print Entry Matrix */
            print_matrix_colmajor("Entry Matrix A", m, n, A, lda );
            /* Print Right Rand Side */
            print_matrix_colmajor("Right Hand Side b", n, nrhs, b, ldb );
            System.out.println();

            /* Executable statements */
            //            printf( "LAPACKE_dgels (col-major, high-level) Example Program Results\n" );
            /* Solve least squares problem*/
            info = LAPACKE_dgels(LAPACK_COL_MAJOR(), (byte)'N', m, n, nrhs, A, lda, b, ldb);

            /* Print Solution */
            print_matrix_colmajor("Solution", n, nrhs, b, ldb );
            System.out.println();
            System.exit(info);
        }
    }

    static void print_matrix_colmajor(String msg, int m, int n, MemoryAddress mat, int ldm) {
        int i, j;
        System.out.printf("\n %s\n", msg);

        for( i = 0; i < m; i++ ) {
            for( j = 0; j < n; j++ ) System.out.printf(" %6.2f", Cdouble.get(mat, i+j*ldm));
            System.out.printf( "\n" );
        }
    }
}

```

### Compiling and running the above LAPACK sample

```sh

java -Dforeign.restricted=permit \
    --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/lapack/lib \
    TestLapack.java

```
## Using libproc library to list processes from Java (Mac OS)

### jextract libproc.h

```sh

jextract -t org.unix \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
  --filter libproc.h \
  /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/libproc.h

```

### Java program that uses libproc to list processes

```java

import jdk.incubator.foreign.NativeAllocationScope;
import org.unix.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.unix.libproc_h.*;

public class LibprocMain {
    private static final int NAME_BUF_MAX = 256;

    public static void main(String[] args) {
        try (var scope = NativeAllocationScope.unboundedScope()) {
            // get the number of processes
            int numPids = proc_listallpids(NULL, 0);
            // allocate an array
            var pids = Cint.allocateArray(numPids, scope);
            // list all the pids into the native array
            proc_listallpids(pids, numPids);
            // convert native array to java array
            int[] jpids = Cint.toJavaArray(pids.segment());
            // buffer for process name
            var nameBuf = Cchar.allocateArray(NAME_BUF_MAX,scope);
            for (int i = 0; i < jpids.length; i++) {
                int pid = jpids[i];
                // get the process name
                proc_name(pid, nameBuf, NAME_BUF_MAX);
                String procName = Cstring.toJavaString(nameBuf);
                // print pid and process name
                System.out.printf("%d %s\n", pid, procName);
            }
        }
    }
}

```

### Compiling and running the libproc sample

```sh

java -Dforeign.restricted=permit \
    --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/lib LibprocMain.java

```

## Using libgit2 from Java (Mac OS)

### Getting and building libgit2

* Download libgit2 v1.0.0 source from https://github.com/libgit2/libgit2/releases
* Use cmake to build from libgit2
* Let ${LIBGIT2_HOME} be the directory where you expanded libgit2 sources.
* Let ${LIBGIT2_HOME}/build be the build directory where libgit2.dylib is built.

### jextract git2.h

```sh

jextract -t com.github -lgit2 \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/ \
  -I ${LIBGIT2_HOME}/include/ \
  -I ${LIBGIT2_HOME}/include/git2 \
  ${LIBGIT2_HOME}/include/git2.h

```

### Java program that uses libgit2 to clone github repo

```java

import static com.github.git2_h.*;
import static jdk.incubator.foreign.CSupport.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static jdk.incubator.foreign.NativeAllocationScope.*;
import static com.github.Cstring.*;

public class GitClone {
    public static void main(String[] args) {
          if (args.length != 2) {
              System.err.println("java GitClone <url> <path>");
              System.exit(1);
          }
          git_libgit2_init();
          try (var scope = unboundedScope()) {
              var repo = scope.allocate(C_POINTER, NULL);
              var url = toCString(args[0], scope);
              var path = toCString(args[1], scope);
              System.out.println(git_clone(repo, url, path, NULL));
          }
          git_libgit2_shutdown();
    }
}

```

### Compiling and running the libgit2 sample

```sh

# file run.sh

java -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=${LIBGIT2_HOME}/build/ \
    GitClone.java $*
```

### Cloning a github repo using the above run.sh command

```sh

sh run.sh https://github.com/libgit2/libgit2.git libgit2

```
