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

@Plugin(type = Op.class, name = "Fringe Visibility")
public class FringeVisibilityOp extends AbstractOp {
    // Inputs
    @Parameter float[][] P_data;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) float[][] P_result;

    @Override
    public void run()
    {
        M_width = P_data.length;
        M_height = P_data[0].length;
        P_result = new float[M_width][M_height];

        float tmax = 0;
        float tmean = 0;
        for (int x = 0; x < M_width; ++x) {
            for (int y = 0; y < M_height; ++y) {
                float val = P_data[x][y];
                if (val > tmax) tmax = val;
                tmean += val;
            }
        }
        tmean /= M_width * M_height;
        for (int x = 0; x < M_width; ++x) {
            for (int y = 0; y < M_height; ++y) {
                float max = 0;
                float min = 1;
                float average = 0;
                int i = 0;
                for (int x_plus = -1; x_plus <= 1; ++x_plus) {
                    for (int y_plus = -1; y_plus <= 1; ++y_plus) {
                        int new_x = x + x_plus;
                        int new_y = y + y_plus;
                        if (   (new_x < 0)
                            || (new_y < 0)
                            || (new_x == M_width)
                            || (new_y == M_height)) continue;
                        float val = P_data[new_x][new_y];
                        average += val;
                        if (val > max) max = val;
                        if (val < min) min = val;
                        ++i;
                    }
                }
                average /= i;
                P_result[x][y] = ((max - average) / 2) / average;
            }
        }
    }

    private int M_width;
    private int M_height;
}
