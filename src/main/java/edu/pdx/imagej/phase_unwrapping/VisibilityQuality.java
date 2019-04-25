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
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;

@Plugin(type = Quality.class,
        name = "Fringe Visibility",
        priority = Priority.VERY_HIGH)
public class VisibilityQuality extends AbstractQuality {
    @Parameter private OpService P_ops;
    @Override
    public DParameter param()
    {
        if (M_holo == null) {
            M_holo = new ImageParameter("Hologram");
        }
        return M_holo;
    }
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        ImagePlus hologram = M_holo.get_value();
        int current_slice = hologram.getStackIndex(1, z, t);
        float[][] data = hologram.getStack()
                                 .getProcessor(current_slice)
                                 .getFloatArray();
        M_result = (float[][])P_ops.run("Fringe Visibility", (Object)data);
        return M_result;
    }
    @Override public float[][] get_result() {return M_result;}
    @Override public int get_ts() {return M_holo.get_value().getNFrames();}
    @Override public int get_zs() {return M_holo.get_value().getNSlices();}

    private ImageParameter   M_holo;
    private float[][]        M_result;
}
