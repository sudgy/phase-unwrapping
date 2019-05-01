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

import ij.ImagePlus;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;

/** GradientQuality is a {@link Quality} that uses the gradient of an image.  It
 * assigns a higher value to spots with a lower gradient.
 */
@Plugin(type = Quality.class,
        name = "Gradient",
        priority = Priority.VERY_HIGH * 0.9999) // Right before Phase Gradient
public class GradientQuality extends AbstractQuality {
    /** {@inheritDoc}
     * <p>
     * For GradientQuality, it is just an ImageParameter.
     */
    @Override
    public DParameter param()
    {
        if (M_image == null) {
            M_image = new ImageParameter("Gradient Image");
        }
        return M_image;
    }
    /** {@inheritDoc} */
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        ImagePlus image = M_image.get_value();
        int current_slice = image.getStackIndex(1, z, t);
        float[][] data = M_image.get_value()
                                .getStack()
                                .getProcessor(current_slice)
                                .getFloatArray();
        return calculate_with(data);
    }
    // For testing purposes, we want to be able to bypass the ImageParameter.
    // This is package-private so that the tests can see this too.
    float[][] calculate_with(float[][] data)
    {
        M_result = new float[data.length][data[0].length];
        for (int x = 0; x < M_result.length; ++x) {
            for (int y = 0; y < M_result[0].length; ++y) {
                for (int x_plus = -1; x_plus <= 1; ++x_plus) {
                    for (int y_plus = -1; y_plus <= 1; ++y_plus) {
                        if (x_plus == 0 && y_plus == 0) continue;
                        int new_x = x + x_plus;
                        int new_y = y + y_plus;
                        if (new_x == -1 || new_y == -1) continue;
                        if (new_x == M_result.length ||
                            new_y == M_result[0].length) continue;
                        float diff = data[x][y] - data[new_x][new_y];
                        M_result[x][y] -= Math.abs(diff);
                    }
                }
            }
        }
        return M_result;
    }
    /** {@inheritDoc} */
    @Override public float[][] get_result() {return M_result;}
    /** {@inheritDoc} */
    @Override public int get_ts() {return M_image.get_value().getNFrames();}
    /** {@inheritDoc} */
    @Override public int get_zs() {return M_image.get_value().getNSlices();}

    private ImageParameter M_image;
    private float[][] M_result;
}
