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
 * A boxed implementation of Complex with float real and imaginary components.
 *
 * Includes methods for arithmetic, sin, cos, log, exp, and pow.
 */
public final class Complex {
    public static final Complex ZERO = Complex.valueOf(0,0);
    public static final Complex ONE = Complex.valueOf(1,0);
    public static final Complex I = Complex.valueOf(0,1);

    public final float re;
    public final float im;

    private Complex(float re, float im) {
        this.re = re;
        this.im = im;
    }

    private Complex(double re, double im) {
        this.re = (float) re;
        this.im = (float) im;
    }

    public static Complex valueOf(float re, float im) {
        return new Complex(re, im);
    }

   public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }

    public static Complex valueOf(float re) {
        return new Complex(re, 0.0);
    }

    public static Complex valueOf(double re) {
        return new Complex(re, 0.0);
    }

    public final Complex plus(Complex other) {
        return new Complex(this.re + other.re, this.im + other.im);
    }

    public final Complex minus(Complex other) {
        return new Complex(this.re - other.re, this.im - other.im);
    }

    public final Complex times(Complex other) {
        return new Complex(
                (double) this.re * other.re - this.im * other.im ,
                (double) this.im * other.re + this.re * other.im );
    }

    public final Complex times(double other) {
        return new Complex(this.re * other, this.im * other );
    }

    public final Complex divided(Complex other) {
        double denom = (double) other.re * other.re + (double) other.im * other.im;
        return new Complex(
                 ((double) this.re * other.re + this.im * other.im)/denom ,
                 ((double) this.im * other.re - this.re * other.im)/denom );
    }

    public final Complex conjugate() {
        return new Complex(re, -im);
    }

    public final float abs() {
        return (float) Math.sqrt((double) this.re * this.re + (double) this.im * this.im);
    }

    public final double dabs() {
        return Math.sqrt((double) this.re * this.re + (double) this.im * this.im);
    }

    public final float angle() {
        return (float) Math.atan2((double) this.im, (double) this.re);
    }

    public final Complex sqrt() {
        double modulus = dabs();
        return new Complex(
                Math.sqrt((modulus + this.re)/2.0),
                (this.im < 0 ? -1 : 1) * Math.sqrt((modulus - this.re)/2.0)
        );
    }

    public final Complex log() {
        return new Complex((float)Math.log(dabs()), angle());
    }

    public final Complex exp() {
        double exp_r = Math.exp((double)this.re);
        return new Complex(exp_r * Math.cos(this.im), exp_r * Math.sin(this.im));
    }

    public final Complex pow(double p) {
        return log().times(p).exp();
    }

    public final Complex pow(Complex p) {
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
        int hash = 5;
        hash = 67 * hash + Float.floatToIntBits(this.re);
        hash = 67 * hash + Float.floatToIntBits(this.im);
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
        final Complex other = (Complex) obj;
        if (Float.floatToIntBits(this.re) != Float.floatToIntBits(other.re)) {
            return false;
        }
        return Float.floatToIntBits(this.im) == Float.floatToIntBits(other.im);
    }
}
