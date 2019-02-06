% Using Panama "foreign" JDK

<?xml version="1.0" encoding="utf-8"?>

# Using Panama "foreign" JDK

There are two ways to get a panama foreign branch JDK.

1. Locally build "foreign" branch of panama repo [http://hg.openjdk.java.net/panama/dev/](http://hg.openjdk.java.net/panama/dev/)
2. Download pre-built panama "foreign" early access binaries from [http://jdk.java.net/panama/](http://jdk.java.net/panama/)

Using foreign function call in Java involves the following three steps:

1. Use **jextract** tool to generate java interface for your C header file(s)
2. Use **java.foreign** API to create ("bind") implementation for C header interfaces
3. Invoke C functions via the jextracted Java interface

### Windows notes

You will (almost always) need to have Visual Studio installed, since most libraries indirectly depend on Visual Studio runtime libraries and this currently means that jextract needs their header to extract successfully. Windows examples have been tested with [Build Tools for Visual Studio 2017](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2017).

It is generally a good idea to give jextract a bunch of extra memory since a lot of big system headers are transitively included. The extra memory will make the jextract run significantly faster. Windows support was added only recently, and the memory usage of jextract has not been optimized yet, so this is a workaround. You can give extra memory by passing e.g. `-J-Xmx8G` to jextract as an additional argument, which in this example gives jextract 8 gigabytes of memory.

Commands are tested in PowerShell.

## Embedding Python interpreter in your Java program (Mac OS)

### jextract a Jar file for Python.h

```sh

jextract -l python2.7 \
  -L /System/Library/Frameworks/Python.framework/Versions/2.7/lib --record-library-path \
  --exclude-symbols .*_FromFormatV\|_.*\|PyOS_vsnprintf\|.*_VaParse.*\|.*_VaBuild.*\|PyBuffer_SizeFromFormat\|vasprintf\|vfprintf\|vprintf\|vsprintf \
  -t org.python \
  /usr/include/python2.7/Python.h \
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

javac -cp python.jar PythonMain.java

java -cp python.jar:. PythonMain

```

## jlinking Python Interpreter in your JDK (Mac OS)

### Generating jmod using jextract

```sh

jextract -l python2.7 \
  -L /System/Library/Frameworks/Python.framework/Versions/2.7/lib \
  --exclude-symbols .*_FromFormatV\|_.*\|PyOS_vsnprintf\|.*_VaParse.*\|.*_VaBuild.*\|PyBuffer_SizeFromFormat\|vasprintf\|vfprintf\|vprintf\|vsprintf \
  -t org.python \
  /usr/include/python2.7/Python.h \
  -o org.python.jmod

```

### Jlinking python module to create a JDK with Python in it

jdk.compiler and org.python modules are added to the generated (jlinked) JDK

```sh

jlink --add-modules org.python,jdk.compiler --module-path . --output pythonjdk

```

### Compile and run user code with "pythonjdk" jdk

In the following commands, it is assumed that you've put $pythonjdk/bin in your $PATH

```sh

javac PythonMain.java
java PythonMain

```

## Embedding Python interpreter in your Java program (Ubuntu 16.04)

### jextract a Jar file for Python.h

```sh

jextract -l python2.7 \
  -L /usr/lib/python2.7/config-x86_64-linux-gnu --record-library-path \
  --exclude-symbols .*_FromFormatV\|_.*\|PyOS_vsnprintf\|.*_VaParse.*\|.*_VaBuild.*\|PyBuffer_SizeFromFormat\|vasprintf\|vfprintf\|vprintf\|vsprintf \
  -t org.python \
  /usr/include/python2.7/Python.h \
  -o python.jar

```

### Compiling and Running Python Java example

Follow the instructions from the Mac OS section

## Embedding Python interpreter in your Java program (Windows)

### jextract a Jar file for Python.h

Where python 2.7 is installed in the `C:\Python27` directory:

```powershell
jextract -L "C:\Windows\System32" -l python27 -o python.jar -t "org.python" --record-library-path C:\Python27\include\Python.h
```

### Compiling and Running Python Java example

```powershell
javac -cp python.jar PythonMain.java
java -cp "python.jar;." PythonMain
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

### Installing OpenBLAS (Ubuntu 16.04)

On Ubuntu, blas is distributed as part of the atlas library: [http://math-atlas.sourceforge.net/](http://math-atlas.sourceforge.net/).

You can install atlas using apt

```sh

sudo apt-get install libatlas-base-dev

```

This command will install include files under `/usr/include/atlas` and corresponding libraries under `/usr/lib/atlas-dev`.


### jextracting cblas.h (MacOS)

The following command can be used to extract cblas.h on MacOs

```sh

jextract -C "-D FORCE_OPENBLAS_COMPLEX_STRUCT" \
  -L /usr/local/opt/openblas/lib -I /usr/local/opt/openblas \
  -l openblas -t blas --record-library-path /usr/local/opt/openblas/include/cblas.h \
  -o cblas.jar

```

The FORCE_OPENBLAS_COMPLEX_STRUCT define is needed because jextract does not
yet handle C99 `_Complex` types. The rest of the options are standard ones.

### jextracting cblas.h (Ubuntu 16.04)

The following command can be used to extract cblas.h on Ubuntu

```sh

jextract -L /usr/lib/atlas-base -I /usr/include/atlas/ \
   -l cblas -t blas --record-library-path \
   /usr/include/atlas/cblas.h -o cblas.jar

```

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

## Using LAPACK library (Ubuntu)

On Ubuntu, the same steps used to install the blas (via atlas) library also install headers and libraries for the LAPACK library, a linear algebra computation library built on top of blas.

### jextracting clapack.h (Ubuntu 16.04)

The following command can be used to extract the LAPACK header:

```sh

jextract -L /usr/lib/atlas-base/atlas -I /usr/include/atlas/ \
   -l lapack -t lapack --record-library-path /usr/include/atlas/clapack.h -o clapack.jar

```

### Java sample code that uses LAPACK library

```java
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;

import static lapack.clapack_h.*;
import static lapack.cblas_h.*;

public class TestLapack {
    public static void main(String[] args) {

        /* Locals */
        try (Scope sc = Scope.newNativeScope()) {
            Array<Double> A = sc.allocateArray(NativeTypes.DOUBLE, new double[]{
                    1, 2, 3, 4, 5, 1, 3, 5, 2, 4, 1, 4, 2, 5, 3
            });
            Array<Double> b = sc.allocateArray(NativeTypes.DOUBLE, new double[]{
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
            info = clapack_dgels(CblasColMajor, CblasNoTrans, m, n, nrhs, A.elementPointer(), lda, b.elementPointer(), ldb);

            /* Print Solution */
            print_matrix_colmajor("Solution", n, nrhs, b, ldb );
            System.out.println();
            System.exit(info);
        }
    }

    static void print_matrix_colmajor(String msg, int m, int n, Array<Double> mat, int ldm) {
        int i, j;
        System.out.printf("\n %s\n", msg);

        for( i = 0; i < m; i++ ) {
            for( j = 0; j < n; j++ ) System.out.printf(" %6.2f", mat.get(i+j*ldm));
            System.out.printf( "\n" );
        }
    }
}
```

### Compiling and running the above LAPACK sample

```sh

javac -cp clapack.jar TestLapack.java

java -cp clapack.jar:. TestLapack

```

## Using LAPACK library (Mac OS)

On Mac OS, lapack is installed under /usr/local/opt/lapack directory.

### jextracting lapacke.h

The following command can be used to extract the LAPACK header. These are too many symbols in lapacke.h
and so jextract throws too many constant pool entries (IllegalArgumentException). To workaround, we
include only the symbols used in the Java sample code below.

```sh

jextract --include-symbols LAPACKE_dgels\|LAPACK_COL_MAJOR \
  -L /usr/local/opt/lapack/lib -I /usr/local/opt/lapack/ \
  -l lapacke -t lapack --record-library-path /usr/local/opt/lapack/include/lapacke.h -o clapack.jar

```
### Java sample code that uses LAPACK library

```java

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;

import static lapack.lapacke_h.*;

public class TestLapack {
    public static void main(String[] args) {

        /* Locals */
        try (Scope sc = Scope.newNativeScope()) {
            Array<Double> A = sc.allocateArray(NativeTypes.DOUBLE, new double[]{
                    1, 2, 3, 4, 5, 1, 3, 5, 2, 4, 1, 4, 2, 5, 3
            });
            Array<Double> b = sc.allocateArray(NativeTypes.DOUBLE, new double[]{
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
            info = LAPACKE_dgels(LAPACK_COL_MAJOR, (byte)'N', m, n, nrhs, A.elementPointer(), lda, b.elementPointer(), ldb);

            /* Print Solution */
            print_matrix_colmajor("Solution", n, nrhs, b, ldb );
            System.out.println();
            System.exit(info);
        }
    }

    static void print_matrix_colmajor(String msg, int m, int n, Array<Double> mat, int ldm) {
        int i, j;
        System.out.printf("\n %s\n", msg);

        for( i = 0; i < m; i++ ) {
            for( j = 0; j < n; j++ ) System.out.printf(" %6.2f", mat.get(i+j*ldm));
            System.out.printf( "\n" );
        }
    }
}

```

### Compiling and running the above LAPACK sample

```sh

javac -cp clapack.jar TestLapack.java

java -cp clapack.jar:. TestLapack

```

## Using libproc library to list processes from Java (Mac OS)

### jextract a jar file for libproc.h

jextract -t org.unix -lproc -L /usr/lib --record-library-path -o libproc.jar /usr/include/libproc.h

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

### Note: This sample fails because of too big UTF-8 String in NativeHeader annotation

### jextract a jar file for readline.h

```sh

jextract -l readline -L /usr/local/opt/readline/lib/ --record-library-path \
    -t org.unix \
    /usr/include/readline/readline.h \
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


## Using OpenGL graphic library (Ubuntu 16.04)

OpenGL is a popular portable graphic library: [https://www.opengl.org/](https://www.opengl.org/)

### Installing OpenGL (Ubuntu 16.04)

Installing relevant OpenGL headers and libraries can be a bit tricky, as it depends on what graphic card is installed on the target platform. The following instruction assume that the standard version of OpenGL is used (e.g. mesa), rather than a proprietary one (Nvidia or AMD), although the changes to get these working are rather small.

OpenGL is always coupled with a bunch of other libraries, namely GLU and glut. You can install all those libraries using `apt`, as follows:

```sh

sudo apt-get install libgl1-mesa-dev libglu1-mesa-dev freeglut3-dev

```

If the installation was successful, OpenGL headers can be found under `/usr/include/GL`, while libraries can be found in the folder `/usr/lib/x86_64-linux-gnu/`.

### jextracting OpenGL (Ubuntu 16.04)

To extract the opengl libraries the following command suffices:

```sh

jextract -L /usr/lib/x86_64-linux-gnu  -l glut -l GLU -l GL --record-library-path -t opengl -o opengl.jar /usr/include/GL/glut.h

```

Since glut depends on the other libraries (GLU and GL), it is not necessary to give additional headers to jextract.

### Java sample code that uses the OpenGL library

```java
import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.Pointer;

import static opengl.gl_h.*;
import static opengl.freeglut_std_h.*;

public class Teapot {

    float rot = 0;

    Teapot(Scope sc) {
        // Misc Parameters
        Array<Float> pos = sc.allocateArray(NativeTypes.FLOAT, new float[] {0.0f, 15.0f, -15.0f, 0});
        Array<Float> spec = sc.allocateArray(NativeTypes.FLOAT, new float[] {1, 1, 1, 0});
        Array<Float> shini = sc.allocateArray(NativeTypes.FLOAT, new float[] {113});

        // Reset Background
        glClearColor(0, 0, 0, 0);

        // Setup Lighting
        glShadeModel(GL_SMOOTH);
        glLightfv(GL_LIGHT0, GL_POSITION, pos.elementPointer());
        glLightfv(GL_LIGHT0, GL_AMBIENT, spec.elementPointer());
        glLightfv(GL_LIGHT0, GL_DIFFUSE, spec.elementPointer());
        glLightfv(GL_LIGHT0, GL_SPECULAR, spec.elementPointer());
        glMaterialfv(GL_FRONT, GL_SHININESS, shini.elementPointer());
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_DEPTH_TEST);
    }

    void display() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glPushMatrix();
        glRotatef(-20, 1, 1, 0);
        glRotatef(rot, 0, 1, 0);
        glutSolidTeapot(0.5);
        glPopMatrix();
        glutSwapBuffers();
    }

    void onIdle() {
        rot += 0.1;
        glutPostRedisplay();
    }

    public static void main(String[] args) {
        try (Scope sc = Scope.newNativeScope()) {
            Pointer<Integer> argc = sc.allocate(NativeTypes.INT32);
            argc.set(0);
            glutInit(argc, Pointer.nullPointer());
            glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
            glutInitWindowSize(900, 900);
            glutCreateWindow(sc.allocateCString("Hello Panama!"));
            Teapot teapot = new Teapot(sc);
            glutDisplayFunc(sc.allocateCallback(teapot::display));
            glutIdleFunc(sc.allocateCallback(teapot::onIdle));
            glutMainLoop();
        }
    }
}
```
### Running the Java code that uses OpenGL (Ubuntu 16.04)

```sh

javac -cp opengl.jar Teapot.java

java -cp opengl.jar:. Teapot

```

## Using OpenGL graphic library (Windows)

### Installing OpenGL

Download the freeglut package for MSVC at [https://www.transmissionzero.co.uk/software/freeglut-devel/](https://www.transmissionzero.co.uk/software/freeglut-devel/)

Extract the freeglut zip.

### jextracting OpenGL

Navigate to the root directory of the extracted zip and run the following commands:

```powershell
$inc = "C:\Program Files (x86)\Windows Kits\10\Include\10.0.17134.0"
jextract -L C:\Windows\System32\ -L .\freeglut\bin\x64\ -l opengl32 -l freeglut -t opengl -o opengl.jar -m "$inc\um\gl=opengl" --record-library-path .\freeglut\include\GL\glut.h
```

The directory that is assigned to `$inc` is an example, and is system dependent. Make sure that the build number at the end of the path (in this case `10.0.17134.0`) is the latest one found in the parent folder (`C:\Program Files (x86)\Windows Kits\10\Include\`).

There are a bunch of warnings generated, but as long as the jar file is generated in the working directory the extraction was successful.

### Java sample code that uses the OpenGL library

This is the same as in the Ubuntu section

### Running the Java code that uses OpenGL

```powershell
javac -cp .\opengl.jar Teapot.java
java -cp "opengl.jar;." Teapot
```

## Using TensorFlow C API (Mac OS)

Quoted from [https://www.tensorflow.org/install/lang_c](https://www.tensorflow.org/install/lang_c)

"TensorFlow provides a C API that can be used to build bindings for other
languages. The API is defined in c_api.h and designed for simplicity and
uniformity rather than convenience."


### Installing libtensorflow

You can follow the setup procedure as described in the above page.

Alternatively, on Mac, you can install libtensorflow using HomeBrew

```sh

brew install libtensorflow

```

Tensorflow ship the libtensorflow with an .so extension, this doesn't work
well for java on MacOS as java expect .dylib extension. To work around this,
create a symbolic link.

```sh

sudo ln -s /usr/local/lib/libtensorflow.so /usr/local/lib/libtensorflow.dylib

```

### jextracting libtensorflow c_api.h

The following command can be used to extract c_api.h.

```sh

jextract -C -x -C c++  \
        -L /usr/local/lib -l tensorflow --record-library-path \
        -o tf.jar -t org.tensorflow.panama \
        /usr/local/include/tensorflow/c/c_api.h

```

The caveat to extract tensorflow C API is that it declare function prototype
without argument in C++ style, for example, TF_Version(), which is considered
incomplete C function prototype instead of C style as in TF_Version(void). An
incomplete function prototype will become vararg funciton. To avoid that, we
need to pass clang '-x c++' options to jextract with '-C -x -C c++'

### Java sample code that uses tensorflow library

```java

import java.foreign.NativeTypes;
import java.foreign.Scope;
import java.foreign.memory.Array;
import java.foreign.memory.LayoutType;
import java.foreign.memory.Pointer;
import org.tensorflow.panama.c_api.TF_DataType;
import org.tensorflow.panama.c_api.TF_Graph;
import org.tensorflow.panama.c_api.TF_Operation;
import org.tensorflow.panama.c_api.TF_OperationDescription;
import org.tensorflow.panama.c_api.TF_Output;
import org.tensorflow.panama.c_api.TF_Session;
import org.tensorflow.panama.c_api.TF_SessionOptions;
import org.tensorflow.panama.c_api.TF_Status;
import org.tensorflow.panama.c_api.TF_Tensor;

import static org.tensorflow.panama.c_api_h.*;

public class TensorFlowExample {
    static Pointer<TF_Operation> PlaceHolder(Pointer<TF_Graph> graph, Pointer<TF_Status> status,
                                      @TF_DataType int dtype, String name) {
        try (var s = Scope.newNativeScope()) {
            Pointer<TF_OperationDescription> desc = TF_NewOperation(graph,
                    s.allocateCString("Placeholder"), s.allocateCString(name));
            TF_SetAttrType(desc, s.allocateCString("dtype"), TF_FLOAT);
            return TF_FinishOperation(desc, status);
        }
    }

    static Pointer<TF_Operation> ConstValue(Pointer<TF_Graph> graph, Pointer<TF_Status> status,
                                Pointer<TF_Tensor> tensor, String name) {
        try (var s = Scope.newNativeScope()) {
            Pointer<TF_OperationDescription> desc = TF_NewOperation(graph,
                    s.allocateCString("Const"), s.allocateCString(name));
            TF_SetAttrTensor(desc, s.allocateCString("value"), tensor, status);
            TF_SetAttrType(desc, s.allocateCString("dtype"), TF_TensorType(tensor));
            return TF_FinishOperation(desc, status);
        }
    }

    static Pointer<TF_Operation> Add(Pointer<TF_Graph> graph, Pointer<TF_Status> status,
                              Pointer<TF_Operation> one, Pointer<TF_Operation> two,
                              String name) {
        try (var s = Scope.newNativeScope()) {
            Pointer<TF_OperationDescription> desc = TF_NewOperation(graph,
                    s.allocateCString("AddN"), s.allocateCString(name));
            Array<TF_Output> add_inputs = s.allocateArray(
                    LayoutType.ofStruct(TF_Output.class),2);
            add_inputs.get(0).oper$set(one);
            add_inputs.get(0).index$set(0);
            add_inputs.get(1).oper$set(two);
            add_inputs.get(1).index$set(0);
            TF_AddInputList(desc, add_inputs.elementPointer(), 2);
            return TF_FinishOperation(desc, status);
        }
    }

    public static void main(String... args) {
        System.out.println("TensorFlow C library version: " + Pointer.toString(TF_Version()));

        Pointer<TF_Graph> graph = TF_NewGraph();
        Pointer<TF_SessionOptions> options = TF_NewSessionOptions();
        Pointer<TF_Status> status = TF_NewStatus();
        Pointer<TF_Session> session = TF_NewSession(graph, options, status);

        float in_val_one = 4.0f;
        float const_two = 2.0f;

        Pointer<TF_Tensor> tensor_in = TF_AllocateTensor(TF_FLOAT, Pointer.nullPointer(), 0, Float.BYTES);
        TF_TensorData(tensor_in).cast(NativeTypes.FLOAT).set(in_val_one);
        Pointer<TF_Tensor> tensor_const_two = TF_AllocateTensor(TF_FLOAT, Pointer.nullPointer(), 0, Float.BYTES);
        TF_TensorData(tensor_const_two).cast(NativeTypes.FLOAT).set(const_two);

        // Operations
        Pointer<TF_Operation> feed = PlaceHolder(graph, status, TF_FLOAT, "feed");
        Pointer<TF_Operation> two = ConstValue(graph, status, tensor_const_two, "const");
        Pointer<TF_Operation> add = Add(graph, status, feed, two, "add");


        try (var s = Scope.newNativeScope()) {
            var ltPtrTensor = LayoutType.ofStruct(TF_Tensor.class).pointer();

            // Session Inputs
            TF_Output input_operations = s.allocateStruct(TF_Output.class);
            input_operations.oper$set(feed);
            input_operations.index$set(0);
            Pointer<Pointer<TF_Tensor>> input_tensors = s.allocate(ltPtrTensor);
            input_tensors.set(tensor_in);

            // Session Outputs
            TF_Output output_operations = s.allocateStruct(TF_Output.class);
            output_operations.oper$set(add);
            output_operations.index$set(0);
            Pointer<Pointer<TF_Tensor>> output_tensors = s.allocate(ltPtrTensor);
            TF_SessionRun(session, Pointer.nullPointer(),
                // Inputs
                input_operations.ptr(), input_tensors, 1,
                // Outputs
                output_operations.ptr(), output_tensors, 1,
                // Target operations
                Pointer.nullPointer(), 0, Pointer.nullPointer(),
                status);

            System.out.println(String.format("Session Run Status: %d - %s",
                    TF_GetCode(status), Pointer.toString(TF_Message(status))));
            Pointer<TF_Tensor> tensor_out = output_tensors.get();
            System.out.println("Output Tensor Type: " + TF_TensorType(tensor_out));
            float outval = TF_TensorData(tensor_out).cast(NativeTypes.FLOAT).get();
            System.out.println("Output Tensor Value: " + outval);

            TF_CloseSession(session, status);
            TF_DeleteSession(session, status);

            TF_DeleteSessionOptions(options);

            TF_DeleteGraph(graph);

            TF_DeleteTensor(tensor_in);
            TF_DeleteTensor(tensor_out);
            TF_DeleteTensor(tensor_const_two);

            TF_DeleteStatus(status);
        }
    }
}

```

### Compiling and running the above TensorFlow sample

```sh

javac -cp tf.jar TensorFlowExample.java

java -cp tf.jar:. TensorFlowExample

```

## Using TensorFlow C API (Windows)

### Installing libtensorflow

You can download a binary distribution from [https://www.tensorflow.org/install/lang_c](https://www.tensorflow.org/install/lang_c)

Extract the zip file.

### jextracting libtensorflow c_api.h

The problem outlined in the Mac OS instruction w.r.t. incorrect function prototypes still exists (though it has been solved in the Tensorflow github repository, this change has not yet made it into the binary distributions). On Windows there is however no jextract command that works around this, so the only way to extract the \include\tensorflow\c\c_api.h header successfully is to first manually fix the incorrect function prototypes:

```C
TF_Version() -> TF_Version(void)  
TF_NewGraph() -> TF_NewGraph(void)  
TF_NewSessionOptions() -> TF_NewSessionOptions(void)  
TF_NewStatus() -> TF_NewStatus(void)
TF_NewBuffer() -> TF_NewBuffer(void)
TF_NewImportGraphDefOptions() -> TF_NewImportGraphDefOptions(void)
TF_GetAllOpList() -> TF_GetAllOpList(void)
```
Once you've done this you can use the following jextract command from the libtensorflow root directory:

```powershell
jextract -L .\lib -l tensorflow -o tf.jar -t "org.tensorflow.panama" --record-library-path .\include\tensorflow\c\c_api.h
```

### Java sample code that uses tensorflow library

This is the same as for the Mac OS section.

### Compiling and running the above TensorFlow sample

```powershell
javac -cp tf.jar TensorFlowExample.java
java -cp "tf.jar;." TensorFlowExample
```
