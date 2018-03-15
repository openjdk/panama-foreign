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
# or visit www.oracle.com if you need additional information or have
# questions.
#

. config.sh

# First, generate the template file.
bash ./gen-template.sh

Log false "Generating Vector API tests, $(date)\n"

# Compile SPP
Log true "Compiling SPP... "
compilation=$(${JAVAC} -d . "${JDK_SRC_HOME}/make/jdk/src/classes/build/tools/spp/Spp.java")
Log false "$compilation\n"
Log true "done\n"

# For each type
for type in byte short int long float double
do
  Type="$(tr '[:lower:]' '[:upper:]' <<< ${type:0:1})${type:1}"
  TYPE="$(tr '[:lower:]' '[:upper:]' <<< ${type})"
  args="-K$type -Dtype=$type -DType=$Type -DTYPE=$TYPE"

  Boxtype=$Type
  Wideboxtype=$Boxtype

  kind=BITWISE

  bitstype=$type
  Bitstype=$Type
  Boxbitstype=$Boxtype

  fptype=$type
  Fptype=$Type
  Boxfptype=$Boxtype

  case $type in
    byte)
      Wideboxtype=Byte
      ;;
    short)
      Wideboxtype=Short
      ;;
    int)
      Boxtype=Integer
      Wideboxtype=Integer
      fptype=float
      Fptype=Float
      Boxfptype=Float
      args="$args -KintOrlong"
      ;;
    long)
      Wideboxtype=Long
      fptype=double
      Fptype=Double
      Boxfptype=Double
      args="$args -KintOrlong"
      ;;
    float)
      kind=FP
      bitstype=int
      Bitstype=Int
      Boxbitstype=Integer
      ;;
    double)
      kind=FP
      bitstype=long
      Bitstype=Long
      Boxbitstype=Long
      ;;
  esac

  args="$args -K$kind -K$Type -DBoxtype=$Boxtype -DWideboxtype=$Wideboxtype"
  args="$args -Dbitstype=$bitstype -DBitstype=$Bitstype -DBoxbitstype=$Boxbitstype"
  args="$args -Dfptype=$fptype -DFptype=$Fptype -DBoxfptype=$Boxfptype"

  abstractvectortype=${typeprefix}${Type}Vector
  abstractvectorteststype=${typeprefix}${Type}VectorTests
  abstractbitsvectortype=${typeprefix}${Bitstype}Vector
  abstractfpvectortype=${typeprefix}${Fptype}Vector
  args="$args -Dabstractvectortype=$abstractvectortype -Dabstractvectorteststype=$abstractvectorteststype -Dabstractbitsvectortype=$abstractbitsvectortype -Dabstractfpvectortype=$abstractfpvectortype"

  # For each size
  for bits in 64 128 256 512
  do
    vectortype=${typeprefix}${Type}${bits}Vector
    vectorteststype=${typeprefix}${Type}${bits}VectorTests
    masktype=${typeprefix}${Type}${bits}Mask
    bitsvectortype=${typeprefix}${Bitstype}${bits}Vector
    fpvectortype=${typeprefix}${Fptype}${bits}Vector
    shape=S${bits}Bit
    Shape=S_${bits}_BIT
    bitargs="$args -Dbits=$bits -Dvectortype=$vectortype -Dvectorteststype=$vectorteststype -Dmasktype=$masktype -Dbitsvectortype=$bitsvectortype -Dfpvectortype=$fpvectortype -Dshape=$shape -DShape=$Shape"

    # Generate
    Log true "Generating $vectorteststype... "
    Log false "${JAVA} -cp . ${SPP_CLASSNAME} -nel $bitargs < ${TEMPLATE_FILE} > $vectorteststype.java "
    ${JAVA} -cp . ${SPP_CLASSNAME} -nel $bitargs \
      < ${TEMPLATE_FILE} \
      > $vectorteststype.java
    Log true "done\n"
  done
done

rm -fr build

