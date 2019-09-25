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

/** VisibilityQuality is a {@link Quality} that uses the fringe visibility of
 * the hologram used to create the phase image.  See
 * {@link edu.pdx.imagej.fringe_visibility.FringeVisibilityOp} for more
 * information on the fringe visibility.
 */
@Plugin(type = Quality.class,
        name = "Fringe Visibility",
        priority = Priority.VERY_HIGH)
public class VisibilityQuality extends AbstractQuality {
    @Parameter private OpService P_ops;
    /** {@inheritDoc}
     * <p>
     * For VisibilityQuality, it is just an ImageParameter.
     */
    @Override
    public DParameter param()
    {
        if (M_holo == null) {
            M_holo = new ImageParameter("Hologram");
        }
        return M_holo;
    }
    /** {@inheritDoc} */
    @Override
    public float[][] calculate(float[][] phaseImage, int t, int z)
    {
        ImagePlus hologram = M_holo.getValue();
        int currentSlice = hologram.getStackIndex(1, z, t);
        float[][] data = hologram.getStack()
                                 .getProcessor(currentSlice)
                                 .getFloatArray();
        M_result = (float[][])P_ops.run("Fringe Visibility", (Object)data);
        return M_result;
    }
    /** {@inheritDoc} */
    @Override public float[][] getResult() {return M_result;}
    /** {@inheritDoc} */
    @Override public int getTs() {return M_holo.getValue().getNFrames();}
    /** {@inheritDoc} */
    @Override public int getZs() {return M_holo.getValue().getNSlices();}

    private ImageParameter   M_holo;
    private float[][]        M_result;
}
