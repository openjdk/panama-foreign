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
