#!/bin/bash

jextract --source -t jdk.internal.clang.libclang -lclang \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/ \
  -I ${LIBCLANG_HOME}/include/ \
  -I ${LIBCLANG_HOME}/include/clang-c \
  @clang.symbols \
  ${LIBCLANG_HOME}/include/clang-c/Index.h


for x in jdk/internal/clang/libclang/*.java; do
head -$COPYRIGHTLEN $x | diff cp_header.txt - || ( ( cat cp_header.txt; echo; cat $x) > /tmp/file;
mv /tmp/file $x )
done
