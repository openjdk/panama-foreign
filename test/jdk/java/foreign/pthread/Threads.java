/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *   Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 *
 */

import java.foreign.*;
import java.foreign.annotations.NativeCallback;
import java.foreign.annotations.NativeFunction;
import java.foreign.annotations.NativeHeader;
import java.foreign.memory.*;
import java.lang.invoke.MethodHandles;

/*
 * @test
 * @requires os.family != "windows"
 * @run main Threads
 */

public class Threads {

    @NativeHeader
    interface pthread_lib {
        @NativeFunction("(u64:u64 u64:u64 u64:(u64:v)u64:v u64:v)i32")
        int pthread_create(Pointer<Long> thread, Pointer<Long> attr, Callback<pthread_cb> start_routine, Pointer<?> arg);

        @NativeCallback("(u64:v)u64:v")
        interface pthread_cb {
            Pointer<?> run(Pointer<?> arg);
        }
    }

    public static void main(String[] args) {
        pthread_lib lib = Libraries.bind(MethodHandles.lookup(), pthread_lib.class);
        try (Scope s = Scope.globalScope().fork()) {
            Pointer<Long> buf = s.allocate(NativeTypes.LONG);
            lib.pthread_create(buf, Pointer.ofNull(),
                    s.allocateCallback(pthread_lib.pthread_cb.class, x -> {
                            System.out.println("thread!");
                            return Pointer.ofNull();
                    }),
                    Pointer.ofNull());
        }
    }
}
