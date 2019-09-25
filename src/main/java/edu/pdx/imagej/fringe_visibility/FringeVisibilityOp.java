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

package edu.pdx.imagej.fringe_visibility;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;

/** This is a simple Op that determines the "fringe visibility" of a hologram.
 * <p>
 * Note that because its only input is a float[][], when calling this Op, you
 * must cast your float[][] to an Object, or else Java thinks you are passing
 * a bunch of float[]'s instead.
 */
@Plugin(type = Op.class, name = "Fringe Visibility")
public class FringeVisibilityOp extends AbstractOp {
    // Inputs
    @Parameter private float[][] P_data;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) private float[][] P_result;

    /** Compute the fringe visibility.
     * <p>
     * Note that because its only input is a float[][], when calling this Op, you
     * must cast your float[][] to an Object, or else Java thinks you are passing
     * a bunch of float[]'s instead.
     */
    @Override
    public void run()
    {
        int width = P_data.length;
        int height = P_data[0].length;
        P_result = new float[width][height];

        // Every value must be greater than zero, so we need to subtract the
        // minimum to all of the values.
        float originalMin = Float.MAX_VALUE;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float val = P_data[x][y];
                if (val < originalMin) originalMin = val;
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                float max = 0;
                float min = Float.MAX_VALUE;
                for (int xPlus = -1; xPlus <= 1; ++xPlus) {
                    for (int yPlus = -1; yPlus <= 1; ++yPlus) {
                        int newX = x + xPlus;
                        int newY = y + yPlus;
                        if (   (newX < 0)
                            || (newY < 0)
                            || (newX == width)
                            || (newY == height)) continue;
                        float val = P_data[newX][newY];
                        // Make value positive
                        val -= originalMin;
                        if (val > max) max = val;
                        if (val < min) min = val;
                    }
                }
                if (max == 0 && min == 0) P_result[x][y] = 0;
                else P_result[x][y] = (max - min) / (max + min);
            }
        }
    }
}
