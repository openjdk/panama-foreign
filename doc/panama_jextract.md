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
import static jdk.incubator.foreign.MemoryAddress.NULL;
// import jextracted python 'header' class
import static org.python.Python_h.*;

public class PythonMain {
    public static void main(String[] args) {
        var f = Foreign.getInstance();
        String script = "print(sum([33, 55, 66])); print('Hello from Python!')\n";

        Py_Initialize();
        try (var s = f.toCString(script)) {
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
import static org.unix.readline_h.*;

public class Readline {
    public static void main(String[] args) {
        var f = Foreign.getInstance();
        try (var s = f.toCString("name? ")) {
            var pstr = s.baseAddress();
            // call "readline" API
            var p = readline(pstr);

            // print char* as is
            System.out.println(p);
            // convert char* ptr from readline as Java String & print it
            System.out.println("Hello, " + f.toJavaString(p));
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
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.unix.curl_h.*;

public class CurlMain {
   public static void main(String[] args) {
       var f = Foreign.getInstance();
       var urlStr = args[0];
       curl_global_init(CURL_GLOBAL_DEFAULT);
       var curl = curl_easy_init();
       if(!curl.equals(NULL)) {
           try (var s = f.toCString(urlStr)) {
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
