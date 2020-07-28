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

jextract \
  -l python2.7 \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/python2.7/ \
  -t org.python \
   /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/python2.7/Python.h

```

### Java program that uses extracted Python interface

```java

import static jdk.incubator.foreign.CSupport.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
// import jextracted python 'header' class
import static org.python.RuntimeHelper.*;
import static org.python.Python_h.*;

public class PythonMain {
    public static void main(String[] args) {
        String script = "print(sum([33, 55, 66])); print('Hello from Python!')\n";

        Py_Initialize();
        try (var str = toCString(script)) {
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

jextract \
  -l readline -t org.unix \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
   /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/readline/readline.h

```

### Java code that uses readline

```java

import static org.unix.RuntimeHelper.*;
import static org.unix.readline_h.*;
import static jdk.incubator.foreign.CSupport.*;

public class Readline {
    public static void main(String[] args) {
        try (var str = toCString("name? ")) {
            // call "readline" API
            var p = readline(str);

            // print char* as is
            System.out.println(p);
            // convert char* ptr from readline as Java String & print it
            System.out.println("Hello, " + toJavaStringRestricted(p));
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
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/ \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/curl/ \
  /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/curl/curl.h

```

### Java code that uses libcurl

```java

import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.jextract.RuntimeHelper.*;
import static org.jextract.curl_h.*;
import static jdk.incubator.foreign.CSupport.*;

public class CurlMain {
   public static void main(String[] args) {
       var urlStr = args[0];
       curl_global_init(CURL_GLOBAL_DEFAULT());
       var curl = curl_easy_init();
       if(!curl.equals(NULL)) {
           try (var url = toCString(urlStr)) {
               curl_easy_setopt(curl, CURLOPT_URL(), url.address());
               int res = curl_easy_perform(curl);
               if (res != CURLE_OK()) {
                   String error = toJavaStringRestricted(curl_easy_strerror(res));
                   System.out.println("Curl error: " + error);
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
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  -l openblas -t blas /usr/local/opt/openblas/include/cblas.h

```

### Java sample code that uses cblas library

```java

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.NativeScope;
import blas.*;
import static blas.RuntimeHelper.*;
import static blas.cblas_h.*;
import static jdk.incubator.foreign.CSupport.*;

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

        try (var scope = NativeScope.unboundedScope()) {
            var a = scope.allocateArray(C_DOUBLE, new double[] {
                1.0, 2.0, 3.0, 4.0,
                1.0, 1.0, 1.0, 1.0,
                3.0, 4.0, 5.0, 6.0,
                5.0, 6.0, 7.0, 8.0
            });
            var x = scope.allocateArray(C_DOUBLE, new double[] {
                1.0, 2.0, 1.0, 1.0
            });
            var y = scope.allocateArray(C_DOUBLE, n);

            cblas_dgemv(Layout, transa, m, n, alpha, a, lda, x, incx, beta, y, incy);
            /* Print y */
            for (i = 0; i < n; i++) {
                System.out.print(String.format(" y%d = %f\n", i, MemoryAccess.getDoubleAtIndex(y, i)));
            }
        }
    }
}
```

### Compiling and running the above BLAS sample

```sh

jextract \
  -C "-D FORCE_OPENBLAS_COMPLEX_STRUCT" \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  -l openblas -t blas /usr/local/opt/openblas/include/cblas.h

```

## Using LAPACK library (Mac OS)

On Mac OS, lapack is installed under /usr/local/opt/lapack directory.

### jextracting lapacke.h

```sh

jextract \
   -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
   -l lapacke -t lapack \
   --filter lapacke.h \
   /usr/local/opt/lapack/include/lapacke.h

```

### Java sample code that uses LAPACK library

```java

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import lapack.*;
import static lapack.lapacke_h.*;
import static jdk.incubator.foreign.CSupport.*;

public class TestLapack {
    public static void main(String[] args) {

        /* Locals */
        try (var scope = NativeScope.unboundedScope()) {
            var A = scope.allocateArray(C_DOUBLE, new double[]{
                    1, 2, 3, 4, 5, 1, 3, 5, 2, 4, 1, 4, 2, 5, 3
            });
            var b = scope.allocateArray(C_DOUBLE, new double[]{
                    -10, 12, 14, 16, 18, -3, 14, 12, 16, 16
            });
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
    
    static void print_matrix_colmajor(String msg, int m, int n, MemorySegment mat, int ldm) {
        int i, j;
        System.out.printf("\n %s\n", msg);

        for( i = 0; i < m; i++ ) {
            for( j = 0; j < n; j++ ) System.out.printf(" %6.2f", MemoryAccess.getDoubleAtIndex(mat, i+j*ldm));
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

jextract \
  -t org.unix \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  --filter libproc.h \
  /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/libproc.h

```

### Java program that uses libproc to list processes

```java

import jdk.incubator.foreign.CSupport;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;
import org.unix.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.unix.libproc_h.*;

public class LibprocMain {
    private static final int NAME_BUF_MAX = 256;

    public static void main(String[] args) {
        try (var scope = NativeScope.unboundedScope()) {
            // get the number of processes
            int numPids = proc_listallpids(NULL, 0);
            // allocate an array
            var pids = scope.allocateArray(CSupport.C_INT, numPids);
            // list all the pids into the native array
            proc_listallpids(pids, numPids);
            // convert native array to java array
            int[] jpids = pids.toIntArray();
            // buffer for process name
            var nameBuf = scope.allocateArray(CSupport.C_CHAR, NAME_BUF_MAX);
            for (int i = 0; i < jpids.length; i++) {
                int pid = jpids[i];
                // get the process name
                proc_name(pid, nameBuf, NAME_BUF_MAX);
                String procName = CSupport.toJavaString(nameBuf);
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

jextract \
  -t com.github -lgit2 \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/ \
  -I ${LIBGIT2_HOME}/include/ \
  -I ${LIBGIT2_HOME}/include/git2 \
  ${LIBGIT2_HOME}/include/git2.h

```

### Java program that uses libgit2 to clone github repo

```java

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.NativeScope;
import static com.github.git2_h.*;
import static jdk.incubator.foreign.CSupport.*;
import static jdk.incubator.foreign.MemoryAddress.NULL;

public class GitClone {
    public static void main(String[] args) {
          if (args.length != 2) {
              System.err.println("java GitClone <url> <path>");
              System.exit(1);
          }
          git_libgit2_init();
          try (var scope = NativeScope.unboundedScope()) {
              var repo = scope.allocate(C_POINTER);
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

## Using sqlite3 library from Java (Mac OS)


### jextract sqlite3.h

```sh

jextract \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include \
  /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/sqlite3.h \
  -t org.sqlite -lsqlite3

```
### Java program that uses sqlite3

```java

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.NativeScope;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.sqlite.sqlite3_h.*;
import static org.sqlite.RuntimeHelper.*;
import static jdk.incubator.foreign.CSupport.*;

public class SqliteMain {
   public static void main(String[] args) throws Exception {
        try (var scope = NativeScope.unboundedScope()) {
            // char** errMsgPtrPtr;
            var errMsgPtrPtr = scope.allocate(C_POINTER);

            // sqlite3** dbPtrPtr;
            var dbPtrPtr = scope.allocate(C_POINTER);

            int rc = sqlite3_open(toCString("employee.db",scope), dbPtrPtr);
            if (rc != 0) {
                System.err.println("sqlite3_open failed: " + rc);
                return;
            } else {
                System.out.println("employee db opened");
            }

            // sqlite3* dbPtr;
            var dbPtr = MemoryAccess.getAddress(dbPtrPtr);

            // create a new table
            var sql = toCString(
                "CREATE TABLE EMPLOYEE ("  +
                "  ID INT PRIMARY KEY NOT NULL," +
                "  NAME TEXT NOT NULL,"    +
                "  SALARY REAL NOT NULL )", scope);

            rc = sqlite3_exec(dbPtr, sql, NULL, NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + toJavaStringRestricted(MemoryAccess.getAddress(errMsgPtrPtr)));
                sqlite3_free(MemoryAccess.getAddress(errMsgPtrPtr));
            } else {
                System.out.println("employee table created");
            }

            // insert two rows
            sql = toCString(
                "INSERT INTO EMPLOYEE (ID,NAME,SALARY) " +
                    "VALUES (134, 'Xyz', 200000.0); " +
                "INSERT INTO EMPLOYEE (ID,NAME,SALARY) " +
                    "VALUES (333, 'Abc', 100000.0);", scope
            );
            rc = sqlite3_exec(dbPtr, sql, NULL, NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + toJavaStringRestricted(MemoryAccess.getAddress(errMsgPtrPtr)));
                sqlite3_free(MemoryAccess.getAddress(errMsgPtrPtr));
            } else {
                System.out.println("rows inserted");
            }

            int[] rowNum = new int[1];
            // callback to print rows from SELECT query
            var callback = sqlite3_exec$callback.allocate((a, argc, argv, columnNames) -> {
                System.out.println("Row num: " + rowNum[0]++);
                System.out.println("numColumns = " + argc);
                var argv_seg = asArrayRestricted(argv, C_POINTER, argc);
                var columnNames_seg = asArrayRestricted(columnNames, C_POINTER, argc);
                for (int i = 0; i < argc; i++) {
                     String name = toJavaStringRestricted(MemoryAccess.getAddressAtIndex(columnNames_seg, i));
                     String value = toJavaStringRestricted(MemoryAccess.getAddressAtIndex(argv_seg, i));
                     System.out.printf("%s = %s\n", name, value);
                }
                return 0;
            }, scope);

            // select query
            sql = toCString("SELECT * FROM EMPLOYEE", scope);
            rc = sqlite3_exec(dbPtr, sql, callback, NULL, errMsgPtrPtr);

            if (rc != 0) {
                System.err.println("sqlite3_exec failed: " + rc);
                System.err.println("SQL error: " + toJavaStringRestricted(MemoryAccess.getAddress(errMsgPtrPtr)));
                sqlite3_free(MemoryAccess.getAddress(errMsgPtrPtr));
            } else {
                System.out.println("done");
            }

            sqlite3_close(dbPtr);
        }
    }
}

```

### Compiling and running the sqlite3 sample

```sh

java -Dforeign.restricted=permit \
   --add-modules jdk.incubator.foreign \
   -Djava.library.path=/usr/lib SqliteMain.java

```

## Using OpenGL library from Java (Mac OS)

### jextract glut.h

```sh

jextract -t opengl -lGL -l/System/Library/Frameworks/GLUT.framework/Versions/Current/GLUT \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/ \
  -C-F/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks \
  /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/GLUT.framework/Headers/glut.h

```
### Java program that uses OpenGL

```java

import jdk.incubator.foreign.CSupport;
import static jdk.incubator.foreign.CSupport.*;
import jdk.incubator.foreign.NativeScope;
import static opengl.glut_h.*;

public class Teapot {
    private float rot = 0;

    Teapot(NativeScope scope) {
        // Reset Background
        glClearColor(0f, 0f, 0f, 0f);
        // Setup Lighting
        glShadeModel(GL_SMOOTH());
        var pos = scope.allocateArray(C_FLOAT, new float[] {0.0f, 15.0f, -15.0f, 0});
        glLightfv(GL_LIGHT0(), GL_POSITION(), pos);
        var spec = scope.allocateArray(C_FLOAT, new float[] {1, 1, 1, 0});
        glLightfv(GL_LIGHT0(), GL_AMBIENT(), spec);
        glLightfv(GL_LIGHT0(), GL_DIFFUSE(), spec);
        glLightfv(GL_LIGHT0(), GL_SPECULAR(), spec);
        var shini = scope.allocate(C_FLOAT, 113);
        glMaterialfv(GL_FRONT(), GL_SHININESS(), shini);
        glEnable(GL_LIGHTING());
        glEnable(GL_LIGHT0());
        glEnable(GL_DEPTH_TEST());
    }

    void display() {
        glClear(GL_COLOR_BUFFER_BIT() | GL_DEPTH_BUFFER_BIT());
        glPushMatrix();
        glRotatef(-20f, 1f, 1f, 0f);
        glRotatef(rot, 0f, 1f, 0f);
        glutSolidTeapot(0.5d);
        glPopMatrix();
        glutSwapBuffers();
    }

    void onIdle() {
        rot += 0.1;
        glutPostRedisplay();
    }

    public static void main(String[] args) {
        try (var scope = NativeScope.unboundedScope()) {
            var argc = scope.allocate(C_INT, 0);
            glutInit(argc, argc);
            glutInitDisplayMode(GLUT_DOUBLE() | GLUT_RGB() | GLUT_DEPTH());
            glutInitWindowSize(500, 500);
            glutCreateWindow(CSupport.toCString("Hello Panama!", scope));
            var teapot = new Teapot(scope);
            var displayStub = glutDisplayFunc$func.allocate(teapot::display, scope);
            var idleStub = glutIdleFunc$func.allocate(teapot::onIdle, scope);
            glutDisplayFunc(displayStub);
            glutIdleFunc(idleStub);
            glutMainLoop();
        }
    }
}

```

### Compiling and running the OpenGL sample

```sh

java -XstartOnFirstThread -Dforeign.restricted=permit --add-modules jdk.incubator.foreign \
    -Djava.library.path=.:/System/Library/Frameworks/OpenGL.framework/Versions/Current/Libraries/ Teapot.java $*

```
