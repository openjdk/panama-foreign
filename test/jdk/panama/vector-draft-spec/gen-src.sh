#!/bin/bash

javac -d target src/pre/java/Spp.java

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
      ;;
    short)
      Wideboxtype=Integer
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

  args="$args -K$kind -DBoxtype=$Boxtype -DWideboxtype=$Wideboxtype"
  args="$args -Dbitstype=$bitstype -DBitstype=$Bitstype -DBoxbitstype=$Boxbitstype"
  args="$args -Dfptype=$fptype -DFptype=$Fptype -DBoxfptype=$Boxfptype"

  abstractvectortype=${typeprefix}${Type}Vector
  abstractbitsvectortype=${typeprefix}${Bitstype}Vector
  abstractfpvectortype=${typeprefix}${Fptype}Vector
  args="$args -Dabstractvectortype=$abstractvectortype -Dabstractbitsvectortype=$abstractbitsvectortype -Dabstractfpvectortype=$abstractfpvectortype"
  echo $args
  java -cp target Spp -nel $args \
    < src/main/java/com/oracle/vector/X-Vector.java.template \
    > src/main/java/com/oracle/vector/$abstractvectortype.java


  for bits in 128 256 512
  do
    vectortype=${typeprefix}${Type}${bits}Vector
    bitsvectortype=${typeprefix}${Bitstype}${bits}Vector
    fpvectortype=${typeprefix}${Fptype}${bits}Vector
    shape=S${bits}Bit
    Shape=S_${bits}_BIT
    args="$args -Dbits=$bits -Dvectortype=$vectortype -Dbitsvectortype=$bitsvectortype -Dfpvectortype=$fpvectortype -Dshape=$shape -DShape=$Shape"

    echo $args
    java -cp target Spp -nel $args \
      < src/main/java/com/oracle/vector/X-VectorBits.java.template \
      > src/main/java/com/oracle/vector/$vectortype.java
  done

done

