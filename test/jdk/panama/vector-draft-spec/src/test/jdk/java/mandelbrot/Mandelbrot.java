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
package mandelbrot;

import com.oracle.vector.FloatVector;
import com.oracle.vector.IntVector;
import com.oracle.vector.Shapes;
import com.oracle.vector.Vector;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


/*
This implementation was derived from a number of Mandelbrot/Graphics sources on the Web.  Some are listed below:

http://jonisalonen.com/2013/lets-draw-the-mandelbrot-set/

http://howaboutanorange.com/blog/2011/08/10/color_interpolation/
 */
public class Mandelbrot extends JFrame {
    private static final FloatVector.FloatSpecies<Shapes.S256Bit> F_SPEC = (FloatVector.FloatSpecies<Shapes.S256Bit>) Vector.speciesInstance(Float.class, Shapes.S_256_BIT);
    private static final IntVector.IntSpecies<Shapes.S256Bit> I_SPEC = (IntVector.IntSpecies<Shapes.S256Bit>) Vector.speciesInstance(Integer.class, Shapes.S_256_BIT);
    BufferedImage image;
    int[] colors;
    int width, height, iterations;

    public Mandelbrot(int width, int height, int iterations, Color[] palette, int palette_len) {
        super("Mandelbrot Set");
        this.width = width;
        this.height = height;
        this.iterations = iterations;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        colors = new int[palette_len];

        int bandwidth = palette_len / palette.length;
        for (int i = 0; i < palette.length - 1; i++) {
            for (int j = 0; j < bandwidth; j++) {
                colors[i * bandwidth + j] = linterp(palette[i], palette[i + 1], j, bandwidth).getRGB();
            }
        }
        setBounds(this.width / 2, this.width / 3, this.width, this.height);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mandelbrot();
        setVisible(true);
    }

    public void mandelbrot() {
        Vector<Float, Shapes.S256Bit> offsets = F_SPEC.fromArray(new float[]{0, 1, 2, 3, 4, 5, 6, 7}, 0);
        Vector<Float, Shapes.S256Bit> width = F_SPEC.broadcast(this.width);
        Vector<Float, Shapes.S256Bit> height = F_SPEC.broadcast(this.height);
        Vector<Float, Shapes.S256Bit> two = F_SPEC.broadcast(2);
        Vector<Float, Shapes.S256Bit> sheight = height.div(two);
        Vector<Float, Shapes.S256Bit> swidth = width.div(two);
        Vector<Float, Shapes.S256Bit> thresh = F_SPEC.broadcast(4);


        Vector<Integer, Shapes.S256Bit> iones = I_SPEC.broadcast(1);
        Vector<Integer, Shapes.S256Bit> max = I_SPEC.broadcast(this.iterations);

        int[] buff = new int[I_SPEC.length()];

        for (int row = 0; row < this.height; row++) {
            Vector<Float, Shapes.S256Bit> cim = F_SPEC.broadcast(row).sub(sheight).mul(thresh).div(width);
            for (int col = 0; col < this.width; col += F_SPEC.length()) {
                Vector<Float, Shapes.S256Bit> cre, x, y;
                cre = F_SPEC.broadcast(col).add(offsets).sub(swidth).mul(thresh).div(width);
                x = F_SPEC.zero();
                y = F_SPEC.zero();
                IntVector<Shapes.S256Bit> iter = I_SPEC.zero();
                Vector.Mask<Float, Shapes.S256Bit> mres = F_SPEC.constantMask(true, true, true, true, true, true, true, true);
                while (mres.anyTrue() && iter.lessThan(max).allTrue()) {
                    Vector<Float, Shapes.S256Bit> x_new = x.mul(x).sub(y.mul(y)).add(cre);
                    y = two.mul(x).mul(y).add(cim);
                    x = x_new;
                    iter = iter.add(iones, mres.rebracket(Integer.class));
                    mres = x.mul(x).add(y.mul(y)).lessThan(thresh);
                }
                IntVector<Shapes.S256Bit> res = iter.blend(I_SPEC.zero(), iter.lessThan(max)); //Keep color if less than

                //End Vectorized code for now.
                //The following code could be vectorized by reducing the raster to a byte[] array
                //and writing to it directly.

                //This would be after applying a color transformation to make it more appealing.  Doing so without
                //a color transformation places the whole thing into the blue or red space where RGB or BGR are concerned.
                res.intoArray(buff, 0);
                for (int i = 0; i < buff.length; i++) {
                    image.setRGB(col + i, row, colors[buff[i] % colors.length]);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    private static Color linterp(Color left, Color right, int index, int width) {
        if (index >= width) {
            throw new IndexOutOfBoundsException("Bad index");
        }
        float scale = ((float) index) / width;
        int red = (int) ((1 - scale) * left.getRed() + (scale * right.getRed()));
        int green = (int) ((1 - scale) * left.getGreen() + (scale * right.getGreen()));
        int blue = (int) ((1 - scale) * left.getBlue() + (scale * right.getBlue()));

        return new Color(red, green, blue);

    }

    public static void main(String args[]) {
        Color[] pal = new Color[5];
        pal[0] = Color.BLACK; //new Color(97,179,255);
        pal[1] = new Color(33, 10, 127);
        pal[2] = new Color(5, 136, 218);
        pal[3] = new Color(11, 204, 49);
        pal[4] = new Color(33, 253, 43);

        Mandelbrot mbrot = new Mandelbrot(1920, 1080, 1000, pal, 100);
    }
}
