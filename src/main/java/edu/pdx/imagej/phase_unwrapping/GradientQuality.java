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
    public float[][] calculate(float[][] phaseImage, int t, int z)
    {
        ImagePlus image = M_image.getValue();
        int currentSlice = image.getStackIndex(1, z, t);
        float[][] data = M_image.getValue()
                                .getStack()
                                .getProcessor(currentSlice)
                                .getFloatArray();
        return calculateWith(data);
    }
    // For testing purposes, we want to be able to bypass the ImageParameter.
    // This is package-private so that the tests can see this too.
    float[][] calculateWith(float[][] data)
    {
        M_result = new float[data.length][data[0].length];
        for (int x = 0; x < M_result.length; ++x) {
            for (int y = 0; y < M_result[0].length; ++y) {
                for (int xPlus = -1; xPlus <= 1; ++xPlus) {
                    for (int yPlus = -1; yPlus <= 1; ++yPlus) {
                        if (xPlus == 0 && yPlus == 0) continue;
                        int newX = x + xPlus;
                        int newY = y + yPlus;
                        if (newX == -1 || newY == -1) continue;
                        if (newX == M_result.length ||
                            newY == M_result[0].length) continue;
                        float diff = data[x][y] - data[newX][newY];
                        M_result[x][y] -= Math.abs(diff);
                    }
                }
            }
        }
        return M_result;
    }
    /** {@inheritDoc} */
    @Override public float[][] getResult() {return M_result;}
    /** {@inheritDoc} */
    @Override public int getTs() {return M_image.getValue().getNFrames();}
    /** {@inheritDoc} */
    @Override public int getZs() {return M_image.getValue().getNSlices();}

    private ImageParameter M_image;
    private float[][] M_result;
}
