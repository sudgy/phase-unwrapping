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

import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.ImagePlus;

public class FringeVisibility {
    private float[][] M_data;
    private float[][] M_result;
    int M_width;
    int M_height;
    public FringeVisibility(float[][] data)
    {
        M_data = data;
        M_width = M_data.length;
        M_height = M_data[0].length;
        M_result = new float[M_width][M_height];
    }
    public void calculate()
    {
        float tmax = 0;
        float tmean = 0;
        for (int x = 0; x < M_width; ++x) {
            for (int y = 0; y < M_height; ++y) {
                float val = M_data[x][y];
                if (val > tmax) tmax = val;
                tmean += val;
            }
        }
        tmean /= M_width * M_height;
        //IJ.showMessage("" + tmax + "\n" + tmean);
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
                        float val = M_data[new_x][new_y];
                        average += val;
                        if (val > max) max = val;
                        if (val < min) min = val;
                        ++i;
                    }
                }
                average /= i;
                M_result[x][y] = ((max - average) / 2) / average;
            }
        }
    }
    public void show()
    {
        ImagePlus imp = new ImagePlus("Fringe Visibility", new FloatProcessor(M_result));
        imp.show();
    }
    public float[][] result()
    {
        return M_result;
    }
}
