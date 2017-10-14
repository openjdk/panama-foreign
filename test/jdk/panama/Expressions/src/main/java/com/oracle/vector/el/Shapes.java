/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package com.oracle.vector.el;

public final class Shapes {

   public static final LENGTH1 L1 = new LENGTH1();
   public static final LENGTH2 L2 = new LENGTH2();
   public static final LENGTH4 L4 = new LENGTH4();
   public static final LENGTH8 L8 = new LENGTH8();
   public static final LENGTH16 L16 = new LENGTH16();

   public static final class LENGTH1 implements Shape {
     public int length(){
        return 1;
     }
   }
   public static final class LENGTH2 implements Shape {
      public int length(){
         return 2;
      }
   }
   public static final class LENGTH4 implements Shape {
      public int length(){
         return 4;
      }
   }
   public static final class LENGTH8 implements Shape {
      public int length(){
         return 8;
      }
   }
   public static final class LENGTH16 implements Shape {
      public int length(){
         return 16;
      }
   }
   public static final class LENGTH32 implements Shape {
      public int length(){
         return 32;
      }
   }
   public static final class LENGTH64 implements Shape {
      public int length(){
         return 64;
      }
   }
}
