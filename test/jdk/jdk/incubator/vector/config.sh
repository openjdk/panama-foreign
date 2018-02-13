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

VECTORTESTS_HOME="$(pwd)"
JDK_SRC_HOME="${VECTORTESTS_HOME}/../../../../../"
JDK_BUILD="${JDK_SRC_HOME}/build/"
JAVA="java"
JAVAC="javac"
BUILDLOG_FILE="./build.log"
SPP_CLASSNAME="build.tools.spp.Spp"
# Windows: Classpath Separator is ';'
# Linux: ':'
SEPARATOR=":"
TYPEPREFIX=""
TEMPLATE_FILE="myVectorTemplate.template"
TESTNG_JAR="${TESTNG_PLUGIN}/plugins/org.testng.source_6.13.1.r201712040515.jar"
TESTNG_RUN_JAR="${TESTNG_PLUGIN}/plugins/org.testng_6.13.1.r201712040515.jar"
JCOMMANDER_JAR="${TESTNG_PLUGIN}/plugins/com.beust.jcommander_1.72.0.jar"

function Log () {
  if [ $1 == true ]; then
    echo -ne "$2"
  fi
  echo -ne "$2" >> $BUILDLOG_FILE
}

function LogRun () {
  if [ $1 == true ]; then
    echo -ne "$2"
  fi
  echo -ne "$2" >> $BUILDLOG_FILE
}
