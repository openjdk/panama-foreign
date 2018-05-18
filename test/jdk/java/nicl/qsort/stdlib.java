/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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


import java.nicl.metadata.C;
import java.nicl.metadata.CallingConvention;
import java.nicl.metadata.NativeHeader;
import java.nicl.metadata.NativeType;
import java.nicl.types.Pointer;

@NativeHeader(headerPath="stdlib.h")
public interface stdlib {
    @FunctionalInterface
    static interface compar {
        @NativeType(layout="(p:Vp:V)i", ctype="int(const void*,const void*)", size=1)
        public int fn(Pointer<Void> e1, Pointer<Void> e2);
    }

    @C(file="stdlib.h", line=47, column=11, USR="c:@F@qsort")
    @NativeType(layout="(p:VLLp:(p:Vp:V)i)V", ctype="void (void*, size_t, size_t, int(*)(const void*,const void*))", size=1)
    @CallingConvention(value=1)
    public abstract void qsort(Pointer<?> base, long nmemb, long size, compar compar);
}
