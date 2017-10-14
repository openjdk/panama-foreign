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
package LoopExample;

import java.util.Objects;

@DeriveValueType
public class Float256 {
   float f0,f1,f2,f3,f4,f5,f6,f7;

   public Float256(float f0,float f1,float f2,float f3,float f4,float f5,float f6,float f7){
       this.f0 = f0;
       this.f1 = f1;
       this.f2 = f2;
       this.f3 = f3;
       this.f4 = f4;
       this.f5 = f5;
       this.f6 = f6;
       this.f7 = f7;
   }

   //Aligning L-type equivalence with value-type equivalence
   @Override
   public boolean equals(Object o) {
     if(o instanceof Float256){
        Float256 that = (Float256) o;
        return equals(that);
     }
     return false;
   }

   public boolean equals(Float256 that){
       return this.f0 == that.f0
               && this.f1 == that.f1
               && this.f2 == that.f2
               && this.f3 == that.f3
               && this.f4 == that.f4
               && this.f5 == that.f5
               && this.f6 == that.f6
               && this.f7 == that.f7;
   }

    //Aligning L-type hashcode with value-type hashcode
   @Override
   public int hashCode(){
       return Objects.hash(f0, f1, f2, f3, f4, f5, f6, f7);
   }

   public static int elements() {
       return 8;
   }
}
