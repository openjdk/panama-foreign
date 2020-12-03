#!/bin/bash
#
# Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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

# This script generates a libclang bundle. On linux by building it from source
# using a devkit, which should match the devkit used to build the JDK. On Macos
# prebuilt binaries are downloaded and repackaged. On Windows, the binary LLVM
# distribution needs to be downloaded and installed manually first.
#
# Set MAKE_ARGS to add parameters to make. Ex:
#
# $ MAKE_ARGS=-j32 bash createLibclangBundle.sh
#
# The llvm/clang build is very resource intensive at the end so often needs
# to be restarted a few times before it fully succeeds.
#
# The script tries to behave well on multiple invocations, only performing steps
# not already done. To redo a step, manually delete the target files from that
# step.

LLVM_VERSION=9.0.0

BUNDLE_NAME=libclang-$LLVM_VERSION.tar.gz

SCRIPT_DIR="$(cd "$(dirname $0)" > /dev/null && pwd)"
OUTPUT_DIR="${SCRIPT_DIR}/../../build/libclang"
SRC_DIR="$OUTPUT_DIR/src"
BUILD_DIR="$OUTPUT_DIR/build"
DOWNLOAD_DIR="$OUTPUT_DIR/download"
INSTALL_DIR="$OUTPUT_DIR/install"
IMAGE_DIR="$OUTPUT_DIR/image"

OS_NAME=$(uname -s)
case $OS_NAME in
  Linux)
    USAGE="$0 <devkit dir>"

    if [ "$1" = "" ]; then
      echo $USAGE
      exit 1
    fi
    DEVKIT_DIR="$1"

    LIB_SUFFIX=.so

    # Download source distros
    mkdir -p $DOWNLOAD_DIR
    cd $DOWNLOAD_DIR
    LLVM_FILE=llvm-$LLVM_VERSION.src.tar.xz
    if [ ! -f $LLVM_FILE ]; then
      wget http://releases.llvm.org/$LLVM_VERSION/$LLVM_FILE
    fi
    CLANG_FILE=cfe-$LLVM_VERSION.src.tar.xz
    if [ ! -f $CLANG_FILE ]; then
      wget http://releases.llvm.org/$LLVM_VERSION/$CLANG_FILE
    fi


    # Unpack src
    mkdir -p $SRC_DIR
    cd $SRC_DIR
    LLVM_DIRNAME=llvm-$LLVM_VERSION.src
    LLVM_DIR=$SRC_DIR/$LLVM_DIRNAME
    if [ ! -d $LLVM_DIRNAME ]; then
      echo "Unpacking $LLVM_FILE"
      tar xf $DOWNLOAD_DIR/$LLVM_FILE
    fi
    CLANG_DIRNAME=cfe-$LLVM_VERSION.src
    CLANG_DIR=$LLVM_DIRNAME/tools/$CLANG_DIRNAME
    if [ ! -d $CLANG_DIR ]; then
      echo "Unpacking $CLANG_FILE"
      (cd $LLVM_DIR/tools && tar xf $DOWNLOAD_DIR/$CLANG_FILE)
    fi

    # Build
    mkdir -p $BUILD_DIR
    cd $BUILD_DIR

    #init cmake
    if [ ! -e cmake ]; then
      cmake -G 'Unix Makefiles' \
            -DCMAKE_INSTALL_PREFIX=../install \
            -DCMAKE_BUILD_TYPE=Release \
            -DCMAKE_C_COMPILER=$DEVKIT_DIR/bin/gcc \
            -DCMAKE_CXX_COMPILER=$DEVKIT_DIR/bin/g++ \
            -DCMAKE_C_FLAGS="-static-libgcc" \
            -DCMAKE_CXX_FLAGS="-static-libgcc -static-libstdc++" \
            -DLLVM_ENABLE_TERMINFO=no \
            $LLVM_DIR
    fi

    # Run with nice to keep system usable during build.
    nice make $MAKE_ARGS libclang
    nice make $MAKE_ARGS install
    ;;
  Darwin)
    LIB_SUFFIX=".dylib"

    # Download binaries
    mkdir -p $DOWNLOAD_DIR
    cd $DOWNLOAD_DIR
    LLVM_FILE=clang+llvm-$LLVM_VERSION-x86_64-darwin-apple.tar.xz
    if [ ! -f $LLVM_FILE ]; then
      echo http://releases.llvm.org/$LLVM_VERSION/$LLVM_FILE
      curl -O http://releases.llvm.org/$LLVM_VERSION/$LLVM_FILE
    fi

    # Extract binaries
    cd $OUTPUT_DIR
    LLVM_DIRNAME=clang+llvm-$LLVM_VERSION-x86_64-darwin-apple
    LLVM_DIR=$OUTPUT_DIR/$LLVM_DIRNAME
    INSTALL_DIR=$LLVM_DIR
    if [ ! -d $LLVM_DIRNAME ]; then
      echo "Unpacking $LLVM_FILE"
      tar xf $DOWNLOAD_DIR/$LLVM_FILE
    fi
    ;;
  CYGWIN*)
    if [ "$1" = "" ]; then
      echo "Download and install http://releases.llvm.org/$LLVM_VERSION/LLVM-$LLVM_VERSION-win64.exe"
      echo "Then run: $0 <path to install dir>"
      exit 1
    fi
    INSTALL_DIR="$(cygpath -m -s "$1")"
    echo "Copying from $INSTALL_DIR"
    LIB_SUFFIX=".lib"

    if [ ! -e $IMAGE_DIR/bin/libclang.dll ]; then
      echo "Copying libclang.dll to image"
      mkdir -p $IMAGE_DIR/bin
      cp -a $INSTALL_DIR/bin/libclang.* $IMAGE_DIR/bin/
    fi
    ;;
  *)
    echo " Unsupported OS: $OS_NAME"
    exit 1
    ;;
esac

mkdir -p $IMAGE_DIR
# Extract what we need into an image
if [ ! -e $IMAGE_DIR/lib/libclang$LIB_SUFFIX ]; then
  echo "Copying libclang$LIB_SUFFIX to image"
  mkdir -p $IMAGE_DIR/lib
  cp -a $INSTALL_DIR/lib/libclang.* $IMAGE_DIR/lib/
fi
if [ ! -e $IMAGE_DIR/include/clang-c ]; then
  echo "Copying include to image"
  mkdir -p $IMAGE_DIR/include
  cp -a $INSTALL_DIR/include/. $IMAGE_DIR/include/
fi
if [ ! -e $IMAGE_DIR/lib/clang/$LLVM_VERSION/include/stddef.h ]; then
  echo "Copying lib/clang/*/include to image"
  mkdir -p $IMAGE_DIR/lib/clang/$LLVM_VERSION/include
  cp -a $INSTALL_DIR/lib/clang/$LLVM_VERSION/include/. \
     $IMAGE_DIR/lib/clang/$LLVM_VERSION/include/
fi

# Create bundle
if [ ! -e $OUTPUT_DIR/$BUNDLE_NAME ]; then
  echo "Creating $OUTPUT_DIR/$BUNDLE_NAME"
  cd $IMAGE_DIR
  tar zcf $OUTPUT_DIR/$BUNDLE_NAME *
fi
