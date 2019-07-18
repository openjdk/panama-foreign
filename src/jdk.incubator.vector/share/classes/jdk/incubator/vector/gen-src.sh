#!/bin/bash

# For manual invocation.
# You can regenerate the source files,
# and you can clean them up.
# FIXME: Move this script under $REPO/make/gensrc/
list_mech_gen() {
    ( # List MG files physically present
      grep -il 'mechanically generated.*do not edit' *.java
      # List MG files currently deleted (via --clean)
      hg status -nd .
    ) | grep '.Vector\.java$'
}
case $* in
'')             CLASS_FILTER='*';;
--generate*)    CLASS_FILTER=${2-'*'};;
--clean)        MG=$(list_mech_gen); set -x; rm -f $MG; exit;;
--revert)       MG=$(list_mech_gen); set -x; hg revert $MG; exit;;
--list)         list_mech_gen; exit;;
--help|*)       echo "Usage: $0 [--generate [file] | --clean | --revert | --list]"; exit 1;;
esac

javac -d . ../../../../../../../make/jdk/src/classes/build/tools/spp/Spp.java

SPP=build.tools.spp.Spp

typeprefix=

globalArgs=""
#globalArgs="$globalArgs -KextraOverrides"

for type in byte short int long float double
do
  Type="$(tr '[:lower:]' '[:upper:]' <<< ${type:0:1})${type:1}"
  TYPE="$(tr '[:lower:]' '[:upper:]' <<< ${type})"
  args=$globalArgs
  args="$args -K$type -Dtype=$type -DType=$Type -DTYPE=$TYPE"

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
      sizeInBytes=1
      args="$args -KbyteOrShort"
      ;;
    short)
      Wideboxtype=Integer
      sizeInBytes=2
      args="$args -KbyteOrShort"
      ;;
    int)
      Boxtype=Integer
      Wideboxtype=Integer
      Boxbitstype=Integer
      fptype=float
      Fptype=Float
      Boxfptype=Float
      sizeInBytes=4
      args="$args -KintOrLong -KintOrFP -KintOrFloat"
      ;;
    long)
      fptype=double
      Fptype=Double
      Boxfptype=Double
      sizeInBytes=8
      args="$args -KintOrLong -KlongOrDouble"
      ;;
    float)
      kind=FP
      bitstype=int
      Bitstype=Int
      Boxbitstype=Integer
      sizeInBytes=4
      args="$args -KintOrFP -KintOrFloat"
      ;;
    double)
      kind=FP
      bitstype=long
      Bitstype=Long
      Boxbitstype=Long
      sizeInBytes=8
      args="$args -KintOrFP -KlongOrDouble"
      ;;
  esac

  args="$args -K$kind -DBoxtype=$Boxtype -DWideboxtype=$Wideboxtype"
  args="$args -Dbitstype=$bitstype -DBitstype=$Bitstype -DBoxbitstype=$Boxbitstype"
  args="$args -Dfptype=$fptype -DFptype=$Fptype -DBoxfptype=$Boxfptype"
  args="$args -DsizeInBytes=$sizeInBytes"

  abstractvectortype=${typeprefix}${Type}Vector
  abstractbitsvectortype=${typeprefix}${Bitstype}Vector
  abstractfpvectortype=${typeprefix}${Fptype}Vector
  args="$args -Dabstractvectortype=$abstractvectortype -Dabstractbitsvectortype=$abstractbitsvectortype -Dabstractfpvectortype=$abstractfpvectortype"
  case $abstractvectortype in
  $CLASS_FILTER)
    echo $abstractvectortype.java : $args
    rm -f $abstractvectortype.java
    java $SPP -nel $args \
       -iX-Vector.java.template \
       -o$abstractvectortype.java
    [ -f $abstractvectortype.java ] || exit 1

    if [ VAR_OS_ENV==windows.cygwin ]; then
      tr -d '\r' < $abstractvectortype.java > temp
      mv temp $abstractvectortype.java
    fi
  esac

  old_args="$args"
  for bits in 64 128 256 512 Max
  do
    vectortype=${typeprefix}${Type}${bits}Vector
    masktype=${typeprefix}${Type}${bits}Mask
    shuffletype=${typeprefix}${Type}${bits}Shuffle
    bitsvectortype=${typeprefix}${Bitstype}${bits}Vector
    fpvectortype=${typeprefix}${Fptype}${bits}Vector
    vectorindexbits=$((bits * 4 / sizeInBytes))
    if [[ "${bits}" == "Max" ]]; then
        vectorindextype="vix.getClass()"
    else
        vectorindextype="Int${vectorindexbits}Vector.class"
    fi;

    BITS=$bits
    case $bits in
      Max)
        BITS=MAX
        ;;
    esac

    shape=S${bits}Bit
    Shape=S_${bits}_BIT
    args="$old_args"
    if [[ "${vectortype}" == "Long64Vector" || "${vectortype}" == "Double64Vector" ]]; then
      args="$args -KlongOrDouble64"
    fi
    bitargs="$args -Dbits=$bits -DBITS=$BITS -Dvectortype=$vectortype -Dmasktype=$masktype -Dshuffletype=$shuffletype -Dbitsvectortype=$bitsvectortype -Dfpvectortype=$fpvectortype -Dvectorindextype=$vectorindextype -Dshape=$shape -DShape=$Shape"

    case $vectortype in
    $CLASS_FILTER)
      echo $vectortype.java : $bitargs
      rm -f $vectortype.java
      java $SPP -nel $bitargs \
         -iX-VectorBits.java.template \
         -o$vectortype.java
      [ -f $vectortype.java ] || exit 1

      if [ VAR_OS_ENV==windows.cygwin ]; then
        tr -d  '\r' < $vectortype.java > temp
        mv temp $vectortype.java
      fi
    esac
  done

done

rm -fr build

