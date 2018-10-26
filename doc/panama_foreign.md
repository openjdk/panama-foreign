# Using Panama "foreign" JDK

There are two ways to get a panama foreign branch JDK.

1. Locally build "foreign" branch of panama repo [http://hg.openjdk.java.net/panama/dev/](http://hg.openjdk.java.net/panama/dev/)
2. Download pre-built panama "foreign" early access binaries from [http://jdk.java.net/panama/](http://jdk.java.net/panama/)

Using foreign function call in Java involves the following three steps:

1. Use **jextract** tool to generate java interface for your C header file(s)
2. Use **java.foreign** API to create ("bind") implementation for C header interfaces
3. Invoke C functions via the jextracted Java interface

## Embedding Python interpreter in your Java program (Mac OS)

### jextract a Jar file for Python.h 

```sh

jextract -l python2.7 \  
  -rpath /System/Library/Frameworks/Python.framework/Versions/2.7/lib \  
  --exclude-symbols .*_FromFormatV\|_.*\|PyOS_vsnprintf\|.*_VaParse.*\|.*_VaBuild.*\|PyBuffer_SizeFromFormat\|vasprintf\|vfprintf\|vprintf\|vsprintf \  
  -t org.python \  
  /usr/include/stdio.h /usr/include/stdlib.h /usr/include/python2.7/Python.h \  
  -o python.jar

```

### Java program that uses extracted Python interface

```java

// import java.foreign packages
import java.foreign.Libraries;
import java.foreign.Scope;
import java.foreign.memory.Pointer;

// import jextracted python 'header' classes
import static org.python.Python_h.*;
import static org.python.pythonrun_h.*;

public class PythonMain {
    public static void main(String[] args) {
        Py_Initialize();
        try (Scope s = Scope.newNativeScope()) {
            PyRun_SimpleStringFlags(s.allocateCString(
                "print(sum([33, 55, 66])); print('Hello from Python!')\n"),
                Pointer.nullPointer());
        }
        Py_Finalize();
    }
}

```

### Running the Java code that calls Python interpreter

```sh

javac -cp pythor.jar PythonMain.java

java -cp python.jar:. PythonMain

```

## Using OpenBLAS library (Mac OS)

[https://github.com/xianyi/OpenBLAS/wiki](https://github.com/xianyi/OpenBLAS/wiki)

OpenBLAS is an optimized BLAS library based on GotoBLAS2 1.13 BSD version.

### Installing OpenBLAS

On Mac, you can install openblas using HomeBrew

```sh

brew install openblas

```

It installs include and lib directories under /usr/local/opt/openblas

### jextracting cblas.h

The following command can be used to extract cblas.h

```sh

jextract -C "-D FORCE_OPENBLAS_COMPLEX_STRUCT" \  
  -L /usr/local/opt/openblas/lib -I /usr/local/opt/openblas \  
  -l openblas -t blas -infer-rpath /usr/local/opt/openblas/include/cblas.h \  
  -o cblas.jar

```

The FORCE_OPENBLAS_COMPLEX_STRUCT define is needed because jextract does not
yet handle C99 _Complex types. The rest of the options are standard ones.

### Java sample code that uses cblas library


```java

import blas.cblas;

import static blas.cblas_h.*;

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;

public class TestBlas {
   public static void main(String[] args) {
       @cblas.CBLAS_ORDER int Layout;
       @cblas.CBLAS_TRANSPOSE int transa;

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

       try (Scope sc = Scope.newNativeScope()){
           Array<Double> a = sc.allocateArray(NativeTypes.DOUBLE, m * n);
           Array<Double> x = sc.allocateArray(NativeTypes.DOUBLE, n);
           Array<Double> y = sc.allocateArray(NativeTypes.DOUBLE, n);
           /* The elements of the first column */
           a.set(0, 1.0);
           a.set(1, 2.0);
           a.set(2, 3.0);
           a.set(3, 4.0);
           /* The elements of the second column */
           a.set(m, 1.0);
           a.set(m + 1, 1.0);
           a.set(m + 2, 1.0);
           a.set(m + 3, 1.0);
           /* The elements of the third column */
           a.set(m * 2, 3.0);
           a.set(m * 2 + 1, 4.0);
           a.set(m * 2 + 2, 5.0);
           a.set(m * 2 + 3, 6.0);
           /* The elements of the fourth column */
           a.set(m * 3, 5.0);
           a.set(m * 3 + 1, 6.0);
           a.set(m * 3 + 2, 7.0);
           a.set(m * 3 + 3, 8.0);
           /* The elemetns of x and y */
           x.set(0, 1.0);
           x.set(1, 2.0);
           x.set(2, 1.0);
           x.set(3, 1.0);
           y.set(0, 0.0);
           y.set(1, 0.0);
           y.set(2, 0.0);
           y.set(3, 0.0);

           cblas_dgemv(Layout, transa, m, n, alpha, a.elementPointer(), lda, x.elementPointer(), incx, beta,
                   y.elementPointer(), incy);
           /* Print y */
           for (i = 0; i < n; i++)
               System.out.print(String.format(" y%d = %f\n", i, y.get(i)));
       }
   }
}

```

### Compiling and running the above cblas samples

```sh

javac -cp cblas.jar TestBlas.java

java -cp cblas.jar:. TestBlas

```

## Using libproc library to list processes from Java (Mac OS)

### jextract a jar file for libproc.h

jextract -t org.unix -lproc -rpath /usr/lib -o libproc.jar /usr/include/libproc.h

### Java program that uses libproc to list processes

```java

import java.foreign.*;
import java.foreign.memory.*;
import static org.unix.libproc_h.*;

public class LibprocMain {
    private static final int NAME_BUF_MAX = 256;

    public static void main(String[] args) {
        // Scope for native allocations
        try (Scope s = Scope.newNativeScope()) {
            // get the number of processes
            int numPids = proc_listallpids(Pointer.nullPointer(), 0);
            // allocate an array
            Array<Integer> pids = s.allocateArray(NativeTypes.INT32, numPids);
            // list all the pids into the native array
            proc_listallpids(pids.elementPointer(), numPids);
            // convert native array to java array
            int[] jpids = pids.toArray(num -> new int[num]);
            // buffer for process name
            Pointer<Byte> nameBuf = s.allocate(NativeTypes.INT8, NAME_BUF_MAX);
            for (int i = 0; i < jpids.length; i++) {
                int pid = jpids[i];
                // get the process name
                proc_name(pid, nameBuf, NAME_BUF_MAX);
                String procName = Pointer.toString(nameBuf);
                // print pid and process name
                System.out.printf("%d %s\n", pid, procName);
            }
        }
    }
}

```

### Running the Java code that uses libproc

```sh

javac -cp libproc.jar LibprocMain.java

java -cp libproc.jar:. LibprocMain

```

## Using readline library from Java code (Mac OS)

### jextract a jar file for readline.h

```sh

jextract -l readline -rpath /usr/local/opt/readline/lib/ \  
    -t org.unix \  
    /usr/include/readline/readline.h /usr/include/_stdio.h \  
    --exclude-symbol readline_echoing_p -o readline.jar 

```

### Java code that uses readline

```java

import java.foreign.*;
import java.foreign.memory.*;
import static org.unix.readline_h.*;

public class Readline {
    public static void main(String[] args) {
        // Scope for native allocations
        try (Scope s = Scope.newNativeScope()) {
            // allocate C memory initialized with Java string content
            var pstr = s.allocateCString("name? ");

            // call "readline" API
            var p = readline(pstr);

            // print char* as is
            System.out.println(p);
            // convert char* ptr from readline as Java String & print it
            System.out.println(Pointer.toString(p));
        }
    }
}

```

### Running the java code that uses readline

```

javac -cp readline.jar Readline.java

java -cp readline.jar:. Readline

```

## Using unistd.h from Java code (Linux)

### jextract a jar file for unistd.h

```sh

jextract /usr/include/unistd.h -t org.unix -o unistd.jar

```

### Java code that calls getpid

```java

import java.foreign.*;
import java.lang.invoke.*;
import org.unix.unistd;


public class Getpid {
    public static void main(String[] args) {
        // bind unistd interface
        var u = Libraries.bind(MethodHandles.lookup(), unistd.class);
        // call getpid from the unistd.h
        System.out.println(u.getpid());
        // check process id from Java API!
        System.out.println(ProcessHandle.current().pid());
    }
}

```

### Running the Java code that uses getpid

```sh

javac -cp unistd.jar Getpid.java

java -cp unistd.jar:. Getpid

```
