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

java -Djdk.incubator.foreign.Foreign=permit --add-modules jdk.incubator.foreign HelloWorld.java

```

## Embedding Python interpreter in your Java program (Mac OS)

### jextract a Jar file for Python.h

```sh

jextract -l python2.7 \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/python2.7/ \
  -t org.python \
  --filter pythonrun.h \
  --filter python.h \
   /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/python2.7/Python.h

```

### Java program that uses extracted Python interface

```java

import jdk.incubator.foreign.Foreign;
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

java -Djdk.incubator.foreign.Foreign=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/System/Library/Frameworks/Python.framework/Versions/2.7/lib PythonMain.java

```

## Using readline library from Java code (Mac OS)

### jextract a jar file for readline.h

```sh

jextract -l readline -t org.unix \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include \
  --filter readline.h \
   /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/readline/readline.h

```

### Java code that uses readline

```java

import jdk.incubator.foreign.Foreign;
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
java -Djdk.incubator.foreign.Foreign=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/readline/lib/ Readline.java

```

## Using libcurl from Java (Mac OS)

### jextract a jar for curl.h

```sh

jextract -t org.unix -lcurl \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/ \
  -I /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/curl/ \
  --filter easy.h \
  --filter curl.h \
  /Library/Developer/CommandLineTools/SDKs/MacOSX10.14.sdk/usr/include/curl/curl.h

```

### Java code that uses libcurl

```java

import jdk.incubator.foreign.Foreign;
import org.unix.Cstring;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.unix.RuntimeHelper.*;
import static org.unix.curl_h.*;

public class CurlMain {
   public static void main(String[] args) {
       var urlStr = args[0];
       curl_global_init(CURL_GLOBAL_DEFAULT);
       var curl = curl_easy_init();
       if(!curl.equals(NULL)) {
           try (var s = Cstring.toCString(urlStr)) {
               var url = s.baseAddress();
               curl_easy_setopt(curl, CURLOPT_URL, url);
               int res = curl_easy_perform(curl);
               if (res != CURLE_OK) {
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
java -Djdk.incubator.foreign.Foreign=permit --add-modules \
    jdk.incubator.foreign -Djava.library.path=/usr/lib CurlMain.java $*

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
  --filter cblas.h \
  -l openblas -t blas /usr/local/opt/openblas/include/cblas.h

```

### Java sample code that uses cblas library

```java

import jdk.incubator.foreign.AllocationScope;
import blas.*;
import static blas.RuntimeHelper.*;
import static blas.cblas_h.*;

public class TestBlas {
    public static void main(String[] args) {
        int Layout;
        int transa;

        double alpha, beta;
        int m, n, lda, incx, incy, i;

        Layout = CblasColMajor;
        transa = CblasNoTrans;

        m = 4; /* Size of Column ( the number of rows ) */
        n = 4; /* Size of Row ( the number of columns ) */
        lda = 4; /* Leading dimension of 5 * 4 matrix is 5 */
        incx = 1;
        incy = 1;
        alpha = 1;
        beta = 0;
        try (AllocationScope scope = AllocationScope.unboundedNativeScope()) {
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

### Compiling and running the above LAPACK sample

```sh

java -Djdk.incubator.foreign.Foreign=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=/usr/local/opt/openblas/lib \
    TestBlas.java

```
