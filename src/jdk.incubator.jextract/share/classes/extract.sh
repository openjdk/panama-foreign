jextract --source -t jdk.internal.clang.libclang -lclang \
  -I /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/usr/include/ \
  -I ${LIBCLANG_HOME}/include/ \
  -I ${LIBCLANG_HOME}/include/clang-c \
  --filter ${LIBCLANG_HOME}/include/clang-c/CXString.h \
  --filter ${LIBCLANG_HOME}/include/clang-c/CXErrorCode.h \
  --filter ${LIBCLANG_HOME}/include/clang-c/Index.h \
  ${LIBCLANG_HOME}/include/clang-c/Index.h
