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

TEMPLATE_FOLDER="templates/"

unary="$TEMPLATE_FOLDER/Unary-op.template"
unary_masked="$TEMPLATE_FOLDER/Unary-Masked-op.template"
unary_scalar="$TEMPLATE_FOLDER/Unary-Scalar-op.template"
binary="$TEMPLATE_FOLDER/Binary-op.template"
binary_masked="$TEMPLATE_FOLDER/Binary-Masked-op.template"
binary_scalar="$TEMPLATE_FOLDER/Binary-Scalar-op.template"
blend="$TEMPLATE_FOLDER/Blend-op.template"
compare_template="$TEMPLATE_FOLDER/Compare.template"
reduction_scalar="$TEMPLATE_FOLDER/Reduction-Scalar-op.template"
reduction_template="$TEMPLATE_FOLDER/Reduction-op.template"
reduction_min_template="$TEMPLATE_FOLDER/Reduction-Scalar-Min-op.template"
reduction_max_template="$TEMPLATE_FOLDER/Reduction-Scalar-Max-op.template"
unary_math_template="$TEMPLATE_FOLDER/Unary-op-math.template"
binary_math_template="$TEMPLATE_FOLDER/Binary-op-math.template"
bool_reduction_scalar="$TEMPLATE_FOLDER/BoolReduction-Scalar-op.template"
bool_reduction_template="$TEMPLATE_FOLDER/BoolReduction-op.template"
with_op_template="$TEMPLATE_FOLDER/With-Op.template"

function gen_op_tmpl { 
  template=$1
  test=$2
  op=$3
  guard=""
  init=""
  if [ $# -gt 4 ]; then
    guard=$5
  fi
  if [ $# == 6 ]; then
    init=$6
  fi

  sed "s/\[\[TEST\]\]/${test}/g" $template > $TEMPLATE_FOLDER/current
  sed -i -e "s/\[\[TEST_OP\]\]/${op}/g" $TEMPLATE_FOLDER/current
  sed -i -e "s/\[\[TEST_INIT\]\]/${init}/g" $TEMPLATE_FOLDER/current

  if [ "$guard" != "" ]; then
    echo -e "#if[${guard}]\n" >> $4
  fi
  cat $TEMPLATE_FOLDER/current >> $4
  if [ "$guard" != "" ]; then
    echo -e "#end[${guard}]\n" >> $4
  fi
}

function finalize {
  rm -rf $TEMPLATE_FOLDER/current
}

function gen_binary_alu_op {
  echo "Generating binary op $1 ($2)..."
  gen_op_tmpl $binary_scalar "$@"
  gen_op_tmpl $binary "$@"
  gen_op_tmpl $binary_masked "$@"
}

function gen_unary_alu_op {
  echo "Generating binary op $1 ($2)..."
  gen_op_tmpl $unary_scalar "$@"
  gen_op_tmpl $unary "$@"
  gen_op_tmpl $unary_masked "$@"
}

function gen_binary_op {
  echo "Generating binary op $1 ($2)..."
  gen_op_tmpl $binary_scalar "$@"
  gen_op_tmpl $binary "$@"
}

function gen_reduction_op {
  echo "Generating reduction op $1 ($2)..."
  gen_op_tmpl $reduction_scalar "$@"
  gen_op_tmpl $reduction_template "$@"
}

function gen_reduction_op_min {
  echo "Generating reduction op $1 ($2)..."
  gen_op_tmpl $reduction_min_template "$@"
  gen_op_tmpl $reduction_template "$@"
}

function gen_reduction_op_max {
  echo "Generating reduction op $1 ($2)..."
  gen_op_tmpl $reduction_max_template "$@"
  gen_op_tmpl $reduction_template "$@"
}

function gen_bool_reduction_op {
  echo "Generating boolean reduction op $1 ($2)..."
  gen_op_tmpl $bool_reduction_scalar "$@"
  gen_op_tmpl $bool_reduction_template "$@"
}

function gen_with_op {
  echo "Generating with op $1 ($2)..."
  gen_op_tmpl $with_op_template "$@"
}

function gen_header {
  cat $TEMPLATE_FOLDER/header.template > $1
}

function gen_footer {
  cat $TEMPLATE_FOLDER/footer.template >> $1
}

template_file="myVectorTemplate.template"
gen_header $template_file

# ALU binary ops.
gen_binary_alu_op "add" "a + b" $template_file
gen_binary_alu_op "sub" "a - b" $template_file
gen_binary_alu_op "div" "a \/ b" $template_file "FP"
gen_binary_alu_op "mul" "a \* b" $template_file
gen_binary_alu_op "and" "a \& b" $template_file "BITWISE"
gen_binary_alu_op "or" "a | b" $template_file "BITWISE"
gen_binary_alu_op "xor" "a ^ b" $template_file "BITWISE"
#gen_binary_alu_op "addSaturate" "((a >= Integer.MAX_VALUE || Integer.MAX_VALUE - b > a) ? Integer.MAX_VALUE : a + b)" $template_file
#gen_binary_alu_op "subSaturate" "((a >= Integer.MIN_VALUE || Integer.MIN_VALUE + b > a) ? Integer.MAX_VALUE : a - b)" $template_file

# Masked reductions.
gen_binary_op "max" "(a > b) ? a : b" $template_file
gen_binary_op "min" "(a < b) ? a : b" $template_file

# Reductions.
gen_reduction_op "andAll" "\&" $template_file "BITWISE" "-1"
gen_reduction_op "orAll" "|" $template_file "BITWISE" "0"
gen_reduction_op "xorAll" "^" $template_file "BITWISE" "0"
gen_reduction_op "addAll" "+" $template_file "" "0"
gen_reduction_op "subAll" "-" $template_file "" "0"
gen_reduction_op "mulAll" "*" $template_file "" "1"
gen_reduction_op_min "minAll" "" $template_file "" "MAX_VALUE"
gen_reduction_op_max "maxAll" "" $template_file "" "MIN_VALUE"

# Boolean reductions.
gen_bool_reduction_op "anyTrue" "|" $template_file "BITWISE" "false"
gen_bool_reduction_op "allTrue" "\&" $template_file "BITWISE" "true"

#Insert
gen_with_op "with" "" $template_file "" ""

# Compares
gen_op_tmpl $compare_template "lessThan" "<" $template_file
gen_op_tmpl $compare_template "greaterThan" ">" $template_file
gen_op_tmpl $compare_template "equal" "==" $template_file
gen_op_tmpl $compare_template "notEqual" "!=" $template_file
gen_op_tmpl $compare_template "lessThanEq" "<=" $template_file
gen_op_tmpl $compare_template "greaterThanEq" ">=" $template_file

# Blend.
gen_op_tmpl $blend "blend" "" $template_file

# Math
gen_op_tmpl $unary_math_template "sin" "Math.sin((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "exp" "Math.exp((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "log1p" "Math.log1p((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "log" "Math.log((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "log10" "Math.log10((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "expm1" "Math.expm1((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "cos" "Math.cos((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "tan" "Math.tan((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "sinh" "Math.sinh((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "cosh" "Math.cosh((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "tanh" "Math.tanh((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "asin" "Math.asin((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "acos" "Math.acos((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "atan" "Math.atan((double)a)" $template_file "FP"
gen_op_tmpl $unary_math_template "cbrt" "Math.cbrt((double)a)" $template_file "FP"
gen_op_tmpl $binary_math_template "hypot" "Math.hypot((double)a, (double)b)" $template_file "FP"
gen_op_tmpl $binary_math_template "pow" "Math.pow((double)a, (double)b)" $template_file "FP"
gen_op_tmpl $binary_math_template "atan2" "Math.atan2((double)a, (double)b)" $template_file "FP"

# Unary operations.
gen_unary_alu_op "neg" "-((\$type\$)a)" $template_file
gen_unary_alu_op "abs" "Math.abs((\$type\$)a)" $template_file
gen_unary_alu_op "not" "~((\$type\$)a)" $template_file "BITWISE"
gen_unary_alu_op "sqrt" "Math.sqrt((double)a)" $template_file "FP"
#gen_binary_op "lessThan" "<" $template_file
#gen_binary_op "greaterThan" ">" $template_file
gen_footer $template_file

finalize
