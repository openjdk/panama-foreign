/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.foreign.memory;

import java.foreign.annotations.NativeStruct;
import java.math.BigDecimal;

@NativeStruct(
   value="[f128(get=real$get)(set=real$set)(ptr=real$ptr)f128(get=imag$get)(set=imag$set)(ptr=imag$ptr)](LongDoubleComplex)"
)
public interface LongDoubleComplex extends Struct<LongDoubleComplex> {
    BigDecimal real$get();
    void real$set(BigDecimal value);
    Pointer<BigDecimal> real$ptr();
    BigDecimal imag$get();
    void imag$set(BigDecimal value);
    Pointer<BigDecimal> imag$ptr();
}
