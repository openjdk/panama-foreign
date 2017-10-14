/*
 *  Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Oracle designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Oracle in the LICENSE file that accompanied this code.
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
 *  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 *  or visit www.oracle.com if you need additional information or have any
 *  questions.
 */

package javax.arrays.tbd;

/**
 * A boxed implementation of double complex with double real and imaginary
 * components.
 *
 * Includes methods for arithmetic, sin, cos, log, exp, and pow.

* The multiplication, division, and square root operations are not reliable
 * because they are vulnerable to catastrophic cancellation.  (This is a
 * prototype for benchmarking purposes).
 */
public final class DComplex {

    public static final DComplex ZERO = DComplex.valueOf(0,0);
    public static final DComplex ONE = DComplex.valueOf(1,0);
    public static final DComplex I = DComplex.valueOf(0,1);

    public final double re;
    public final double im;

    private DComplex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static DComplex valueOf(double re, double im) {
        return new DComplex(re, im);
    }

    public static DComplex valueOf(double re) {
        return new DComplex(re, 0.0);
    }

    public final double re() {
        return re;
    }
    public final double im() {
        return im;
    }

    public final DComplex plus(DComplex other) {
        return new DComplex(this.re + other.re, this.im + other.im);
    }

    public final DComplex minus(DComplex other) {
        return new DComplex(this.re - other.re, this.im - other.im);
    }

    public final DComplex times(DComplex other) {
        return new DComplex(
                this.re * other.re - this.im * other.im ,
                this.im * other.re + this.re * other.im );
    }

    public final DComplex times(double other) {
        return new DComplex(this.re * other, this.im * other );
    }

    public final DComplex divided(DComplex other) {
        double denom =  other.re * other.re +  other.im * other.im;
        return new DComplex(
                 (this.re * other.re + this.im * other.im)/denom ,
                 (this.im * other.re - this.re * other.im)/denom );
    }

    public final DComplex conjugate() {
        return new DComplex(re, -im);
    }

    public final double abs() {
        return  Math.sqrt(this.re * this.re +  this.im * this.im);
    }

    public final double dabs() {
        return Math.sqrt(this.re * this.re +  this.im * this.im);
    }

    public final double angle() {
        return  Math.atan2(this.im,  this.re);
    }

    public final DComplex sqrt() {
        double modulus = dabs();
        return new DComplex(
                Math.sqrt((modulus + this.re)/2.0),
                (this.im < 0 ? -1 : 1) * Math.sqrt((modulus - this.re)/2.0)
        );
    }

    public final DComplex log() {
        return new DComplex(Math.log(dabs()), angle());
    }

    public final DComplex exp() {
        double exp_r = Math.exp(this.re);
        return new DComplex(exp_r * Math.cos(this.im), exp_r * Math.sin(this.im));
    }

    public final DComplex pow(double p) {
        return log().times(p).exp();
    }

    public final DComplex pow(DComplex p) {
        return log().times(p).exp();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(re);
        if (! (im < 0) ) {
            sb.append('+');
        }
        sb.append(im);
        sb.append("i");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.re) ^ (Double.doubleToLongBits(this.re) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.im) ^ (Double.doubleToLongBits(this.im) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DComplex other = (DComplex) obj;
        if (Double.doubleToLongBits(this.re) != Double.doubleToLongBits(other.re)) {
            return false;
        }
        return Double.doubleToLongBits(this.im) == Double.doubleToLongBits(other.im);
    }
}
