#
# Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

################################################################################
# Setup libclang from llvm project
################################################################################
AC_DEFUN_ONCE([LIB_SETUP_LIBCLANG],
[
  AC_ARG_WITH([libclang], [AS_HELP_STRING([--with-libclang=<path to llvm>],
      [Specify path of llvm installation containing libclang. Pre-built llvm
      binary can be downloaded from http://llvm.org/releases/download.html])])
  AC_ARG_WITH([libclang-lib], [AS_HELP_STRING([--with-libclang-lib=<path>],
      [Specify where to find libclang binary, so/dylib/lib ])])
  AC_ARG_WITH([libclang-include], [AS_HELP_STRING([--with-libclang-include=<path>],
      [Specify where to find libclang header files, clang-c/Index.h ])])
  AC_ARG_WITH([libclang-include-aux], [AS_HELP_STRING([--with-libclang-include-aux=<path>],
      [Specify where to find libclang auxiliary header files, lib/clang/<clang-version>/include/stddef.h ])])
  AC_ARG_WITH([libclang-bin], [AS_HELP_STRING([--with-libclang-bin=<path>],
      [Specify where to find clang binary, libclang.dll ])])
  AC_ARG_WITH([libclang-version], [AS_HELP_STRING([--with-libclang-version=<version>],
      [Specify which libclang version to use ])])

  if test "x$with_libclang" = "xno"; then
    AC_MSG_CHECKING([if libclang should be enabled])
    AC_MSG_RESULT([no, forced])
    ENABLE_LIBCLANG="false"
  else
    if test "x$with_libclang" != "x"; then
      AC_MSG_CHECKING([if libclang should be enabled])
      AC_MSG_RESULT([yes, forced])
      ENABLE_LIBCLANG_FORCED="true"
    else
      ENABLE_LIBCLANG_FORCED="false"
    fi
    ENABLE_LIBCLANG="true"

    AC_MSG_CHECKING([libclang version to be used])
    if test "x$with_libclang_version" != "x"; then
      LIBCLANG_VERSION="$with_libclang_version"
      AC_MSG_RESULT([$LIBCLANG_VERSION (manually specified)])
    else
      LIBCLANG_VERSION="9"
      AC_MSG_RESULT([$LIBCLANG_VERSION (default)])
    fi

    if test "x$with_libclang" != "x" -a "x$with_libclang" != "xyes"; then
      CLANG_LIB_PATH="$with_libclang/lib"
      CLANG_BIN_PATH="$with_libclang/bin"
      CLANG_INCLUDE_PATH="$with_libclang/include"

      AC_MSG_CHECKING([libclang auxiliary include path])
      if test "x$with_libclang_include_aux" != "x"; then
        CLANG_INCLUDE_AUX_PATH="$with_libclang_include_aux"
        AC_MSG_RESULT([$CLANG_INCLUDE_AUX_PATH])
        if test "x$with_libclang_version" != "x"; then
          AC_MSG_WARN([--with-libclang-include-aux was specified. Manually specified value of --with-libclang-version was ignored])
        fi        
      else 
        # There may be more than one version of clang matching the specifed version.
        # Pick the last one if there are more than one versions.
        VER=`$LS $with_libclang/lib/clang/ | $GREP "^$LIBCLANG_VERSION" | $TAIL -n1`
        if test "x$VER" = "x"; then
          AC_MSG_ERROR([Can not find libclang version matching the specified version: '$LIBCLANG_VERSION' in
            $($FIND $with_libclang/lib/clang/ -mindepth 1 -maxdepth 1)])
        fi        
        CLANG_INCLUDE_AUX_PATH="$with_libclang/lib/clang/$VER/include"
        AC_MSG_RESULT([$CLANG_INCLUDE_AUX_PATH])
      fi      
    fi

    if test "x$with_libclang_lib" != "x"; then
      CLANG_LIB_PATH="$with_libclang_lib"
    fi
    if test "x$with_libclang_include" != "x"; then
      CLANG_INCLUDE_PATH="$with_libclang_include"
    fi
    if test "x$with_libclang_bin" != "x"; then
      CLANG_BIN_PATH="$with_libclang_bin"
    fi

    dnl Only for Windows platform now, as we don't need bin yet for other platform
    if test "x$OPENJDK_TARGET_OS" = xwindows; then
        UTIL_FIXUP_PATH(CLANG_BIN_PATH)
    else
        CLANG_BIN_PATH=""
    fi

    UTIL_FIXUP_PATH(CLANG_INCLUDE_PATH)
    UTIL_FIXUP_PATH(CLANG_LIB_PATH)
    UTIL_FIXUP_PATH(CLANG_INCLUDE_AUX_PATH)

    if test "x$CLANG_INCLUDE_PATH" != "x"; then
        LIBCLANG_CPPFLAGS="-I$CLANG_INCLUDE_PATH"
    else
        LIBCLANG_CPPFLAGS=""
    fi

    if test "x$CLANG_LIB_PATH" != "x"; then
      if test "x$TOOLCHAIN_TYPE" = "xmicrosoft"; then
        LIBCLANG_LDFLAGS="/LIBPATH:$CLANG_LIB_PATH"
        LIBCLANG_LIBS="$CLANG_LIB_PATH/libclang.lib"
      else
        LIBCLANG_LDFLAGS="-L$CLANG_LIB_PATH"
        LIBCLANG_LIBS="-lclang"
      fi
    else
        LIBCLANG_LDFLAGS=""
    fi

    OLD_CPPFLAGS=$CPPFLAGS
    OLD_LDFLAGS=$LDFLAGS
    OLD_LIBS=$LIBS

    CPPFLAGS="$LIBCLANG_CPPFLAGS"
    LDFLAGS="$LIBCLANG_LDFLAGS"
    LIBS=""

    OLD_CXX=$CXX
    OLD_CXXCPP=$CXXCPP
    CXX="$FIXPATH $CXX"
    CXXCPP="$FIXPATH $CXXCPP"

    AC_CHECK_HEADER("clang-c/Index.h", [], [ENABLE_LIBCLANG="false"])
    if test "x$ENABLE_LIBCLANG" = "xtrue"; then
      if test "x$TOOLCHAIN_TYPE" = "xmicrosoft" || test "x$COMPILE_TYPE" = "xcross"; then
        # Just trust the lib is there
        LIBS=$LIBCLANG_LIBS
      else
        AC_CHECK_LIB(clang, clang_getClangVersion, [], [ENABLE_LIBCLANG="false"])
      fi
    fi

    CXX=$OLD_CXX
    CXXCPP=$OLD_CXXCPP

    if test "x$ENABLE_LIBCLANG" = "xfalse"; then
      if test "x$ENABLE_LIBCLANG_FORCED" = "xtrue"; then
        AC_MSG_ERROR([Cannot locate libclang or headers at the specified locations:
            $CLANG_LIB_PATH
            $CLANG_INCLUDE_PATH])
      else
        AC_MSG_CHECKING([if libclang should be enabled])
        AC_MSG_RESULT([no, not found])
        AC_MSG_ERROR([Cannot locate libclang! You can download pre-built llvm
            binary from http://llvm.org/releases/download.html, then specify the
            location using --with-libclang])
      fi
    fi

    LIBCLANG_LIBS="$LIBS"

    LIBS="$OLD_LIBS"
    LDFLAGS="$OLD_LDFLAGS"
    CPPFLAGS="$OLD_CPPFLAGS"
  fi

  if test "x$ENABLE_LIBCLANG" = "xfalse"; then
    CLANG_INCLUDE_PATH=""
    CLANG_INCLUDE_AUX_PATH=""
    CLANG_LIB_PATH=""
    LIBCLANG_CPPFLAGS=""
    LIBCLANG_LDFLAGS=""
    LIBCLANG_LIBS=""
  else
    if test "x$OPENJDK_TARGET_OS" = xwindows; then
      CLANG_LIBNAME=[$CLANG_BIN_PATH]["/libclang"][$SHARED_LIBRARY_SUFFIX]
    else
      CLANG_LIBNAME=[$CLANG_LIB_PATH/$LIBRARY_PREFIX]["clang"][$SHARED_LIBRARY_SUFFIX]
    fi
    UTIL_REMOVE_SYMBOLIC_LINKS(CLANG_LIBNAME)
  fi

  AC_SUBST(ENABLE_LIBCLANG)
  AC_SUBST(CLANG_INCLUDE_PATH)
  AC_SUBST(CLANG_INCLUDE_AUX_PATH)
  AC_SUBST(CLANG_LIB_PATH)
  AC_SUBST(CLANG_LIBNAME)
  AC_SUBST(LIBCLANG_CPPFLAGS)
  AC_SUBST(LIBCLANG_LDFLAGS)
  AC_SUBST(LIBCLANG_LIBS)
])
