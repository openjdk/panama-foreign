#!/bin/bash

javac -d . ../../../../../../../make/jdk/src/classes/build/tools/spp/Spp.java

SPP=build.tools.spp.Spp

typeprefix=

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
      Wideboxtype=Integer
      args="$args -KbyteOrShort"
      ;;
    short)
      Wideboxtype=Integer
      args="$args -KbyteOrShort"
      ;;
    int)
      Boxtype=Integer
      Wideboxtype=Integer
      fptype=float
      Fptype=Float
      Boxfptype=Float
      args="$args -KintOrLong -KintOrFP -KintOrFloat -KintOrLongOrFP"
      ;;
    long)
      fptype=double
      Fptype=Double
      Boxfptype=Double
      args="$args -KintOrLong -KintOrLongOrFP"
      ;;
    float)
      kind=FP
      bitstype=int
      Bitstype=Int
      Boxbitstype=Integer
      args="$args -KintOrFP -KintOrFloat  -KintOrLongOrFP"
      ;;
    double)
      kind=FP
      bitstype=long
      Bitstype=Long
      Boxbitstype=Long
      args="$args -KintOrFP -KintOrLongOrFP"
      ;;
  esac

  args="$args -K$kind -DBoxtype=$Boxtype -DWideboxtype=$Wideboxtype"
  args="$args -Dbitstype=$bitstype -DBitstype=$Bitstype -DBoxbitstype=$Boxbitstype"
  args="$args -Dfptype=$fptype -DFptype=$Fptype -DBoxfptype=$Boxfptype"

  abstractvectortype=${typeprefix}${Type}Vector
  abstractbitsvectortype=${typeprefix}${Bitstype}Vector
  abstractfpvectortype=${typeprefix}${Fptype}Vector
  args="$args -Dabstractvectortype=$abstractvectortype -Dabstractbitsvectortype=$abstractbitsvectortype -Dabstractfpvectortype=$abstractfpvectortype"
  echo $args
  java $SPP -nel $args \
    < X-Vector.java.template \
    > $abstractvectortype.java

  if [ VAR_OS_ENV==windows.cygwin ]; then
    tr -d '\r' < $abstractvectortype.java > temp
    mv temp $abstractvectortype.java
  fi

  java $SPP -nel $args \
    < X-VectorHelper.java.template \
    > ${abstractvectortype}Helper.java

  if [ VAR_OS_ENV==windows.cygwin ]; then
    tr -d '\r' < ${abstractvectortype}Helper.java > temp
    mv temp ${abstractvectortype}Helper.java
  fi

  for bits in 64 128 256 512 Max
  do
    vectortype=${typeprefix}${Type}${bits}Vector
    masktype=${typeprefix}${Type}${bits}Mask
    shuffletype=${typeprefix}${Type}${bits}Shuffle
    bitsvectortype=${typeprefix}${Bitstype}${bits}Vector
    fpvectortype=${typeprefix}${Fptype}${bits}Vector
    shape=S${bits}Bit
    Shape=S_${bits}_BIT
    bitargs="$args -Dbits=$bits -Dvectortype=$vectortype -Dmasktype=$masktype -Dshuffletype=$shuffletype -Dbitsvectortype=$bitsvectortype -Dfpvectortype=$fpvectortype -Dshape=$shape -DShape=$Shape"

    echo $bitargs
    java $SPP -nel $bitargs \
      < X-VectorBits.java.template \
      > $vectortype.java

    if [ VAR_OS_ENV==windows.cygwin ]; then
      tr -d  '\r' < $vectortype.java > temp
      mv temp $vectortype.java
    fi
  done

done

rm -fr build

