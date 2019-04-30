/* Copyright (C) 2019 Portland State University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For any questions regarding the license, please contact the Free Software
 * Foundation.  For any other questions regarding this program, please contact
 * David Cohoe at dcohoe@pdx.edu.
 */

package edu.pdx.imagej.phase_unwrapping;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;

@Plugin(type = Op.class, name = "Double Wavelength Phase Unwrapping")
public class DoubleWavelengthOp extends AbstractOp {
    // Inputs
    @Parameter private PhaseImage P_image1;
    @Parameter private PhaseImage P_image2;
    @Parameter private boolean P_show_steps;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) float[][][] P_result;

    @Override
    public void run()
    {
        float combined_wavelength =
                    (P_image1.wavelength * P_image2.wavelength) /
            Math.abs(P_image1.wavelength - P_image2.wavelength);
        int width = P_image1.phase_image.length;
        int height = P_image1.phase_image[0].length;
        float phase_value = P_image1.phase_value;
        if (P_image1.phase_value != P_image2.phase_value) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    P_image2.phase_image[x][y] *=
                        (phase_value / P_image2.phase_value);
                }
            }
        }
        float[][][] result = new float[7][width][height];
        result[0] = P_image1.phase_image;
        result[1] = P_image2.phase_image;
        result[2] = subtract_images(P_image1.phase_image, P_image2.phase_image);
        result[3] = create_coarse_from_difference(result[2], phase_value,
                                      combined_wavelength, P_image1.wavelength);
        result[4] = round_to(result[3], phase_value);
        result[5] = add_images(result[4], P_image1.phase_image);
        result[6] = bring_close_to(result[5], result[3], phase_value);
        if (P_show_steps) P_result = result;
        else P_result = new float[][][] {result[3], result[6]};
    }
    static private float[][] subtract_images(float[][] image1, float[][] image2)
    {
        return ArrayOps.binary(image1, image2, ArrayOps.Subtract);
    }
    static private float[][] create_coarse_from_difference(
        float[][] difference, float phase_value,
        float combined_wavelength, float wavelength1)
    {
        return ArrayOps.unary(
            ArrayOps.unary(difference, a -> a < 0 ? a + phase_value : a),
            ArrayOps.MultiplyBy(combined_wavelength / wavelength1));
    }
    static private float[][] round_to(float[][] image, float round)
    {
        return ArrayOps.unary(image, a -> (int)(a / round) * round);
    }
    static private float[][] add_images(float[][] image1, float[][] image2)
    {
        return ArrayOps.binary(image1, image2, ArrayOps.Add);
    }
    static private float[][] bring_close_to(float[][] from,
                                            float[][] to,
                                            float threshold)
    {
        return ArrayOps.binary(from, to,
            (a, b) -> Math.abs(a - b) > (threshold / 2)
                ? a - threshold * Math.signum(a - b)
                : a);
    }
}
