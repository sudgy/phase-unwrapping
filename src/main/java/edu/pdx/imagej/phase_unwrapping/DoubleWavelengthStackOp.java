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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;

/** DoubleWavelengthStackOp is an Op that calculates double wavelength phase
 * unwrapping on an ImagePlus, being able to unwrap the whole stack.  It uses
 * {@link DoubleWavelengthOp} to unwrap each slice.
 * <p>
 * To run this op yourself, it has the name "Double Wavelength Phase Unwrapping"
 * and has these parameters:
 * <ol>
 *     <li>Phase image 1: An <code>ImagePlus</code> representing all of the
 *                        phase images to unwrap of the first wavelength.
 *     <li>Wavelength 1: The first wavelength.
 *     <li>Phase value 1: The pixel phase value of the first wavelength images.
 *     <li>Phase image 2: An <code>ImagePlus</code> representing all of the
 *                        phase images to unwrap of the second wavelength.
 *     <li>Wavelength 2: The second wavelength.
 *     <li>Phase value 2: The pixel phase value of the second wavelength images.
 *     <li>Show steps: Whether or not to show the steps taken.  See
 *                     {@link DoubleWavelengthOp}.
 * </ol>
 */
@Plugin(type = Op.class, name = "Double Wavelength Phase Unwrapping")
public class DoubleWavelengthStackOp extends AbstractOp {
    @Parameter private OpService P_ops;
    // Inputs
    @Parameter private ImagePlus P_phase_image1;
    @Parameter private float     P_wavelength1;
    @Parameter private float     P_phase_value1;
    @Parameter private ImagePlus P_phase_image2;
    @Parameter private float     P_wavelength2;
    @Parameter private float     P_phase_value2;
    @Parameter private boolean   P_show_steps;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) private ImagePlus[] P_result;

    @Override
    public void run()
    {
        int width = P_phase_image1.getWidth();
        int height = P_phase_image1.getHeight();
        ImageStack coarse_stack = new ImageStack(width, height);
        ImageStack fine_stack = new ImageStack(width, height);
        ImageStack[] debug_stack = null;
        if (P_show_steps) {
            debug_stack = new ImageStack[7];
            for (int i = 0; i < 7; ++i) {
                debug_stack[i] = new ImageStack(width, height);
            }
        }

        int t_size = Math.min(P_phase_image1.getNFrames(),
                              P_phase_image2.getNFrames());
        int z_size = Math.min(P_phase_image1.getNSlices(),
                              P_phase_image2.getNSlices());
        int final_size = t_size * z_size;
        int i = 0;
        for (int t = 1; t <= t_size; ++t) {
            for (int z = 1; z <= z_size; ++z) {
                if (final_size > 1) {
                    IJ.showProgress(i, final_size);
                    ++i;
                }
                int current_slice1 = P_phase_image1.getStackIndex(1, z, t);
                int current_slice2 = P_phase_image2.getStackIndex(1, z, t);
                float[][] img1 = P_phase_image1.getStack()
                                                .getProcessor(current_slice1)
                                                .getFloatArray();
                float[][] img2 = P_phase_image2.getStack()
                                               .getProcessor(current_slice2)
                                               .getFloatArray();
                PhaseImage image1 = new PhaseImage();
                image1.phase_image = img1;
                image1.wavelength = P_wavelength1;
                image1.phase_value = P_phase_value1;
                PhaseImage image2 = new PhaseImage();
                image2.phase_image = img2;
                image2.wavelength = P_wavelength2;
                image2.phase_value = P_phase_value2;
                float[][][] result = (float[][][])P_ops.run(
                    "Double Wavelength Phase Unwrapping",
                    image1, image2, P_show_steps);
                if (P_show_steps) {
                    coarse_stack.addSlice(new FloatProcessor(result[3]));
                    fine_stack.addSlice(new FloatProcessor(result[6]));
                    for (int j = 0; j <= 6; ++j) {
                        debug_stack[j].addSlice(new FloatProcessor(result[j]));
                    }
                }
                else {
                    coarse_stack.addSlice(new FloatProcessor(result[0]));
                    fine_stack.addSlice(new FloatProcessor(result[1]));
                }
            }
        }
        if (P_show_steps) {
            P_result = new ImagePlus[] {
            get_stack(debug_stack[0], "Phase Image 1 (a)", z_size, t_size),
            get_stack(debug_stack[1], "Phase Image 2 (b)", z_size, t_size),
            get_stack(debug_stack[2], "Phase Difference (c)", z_size, t_size),
            get_stack(debug_stack[3], "Coarse Map (d)", z_size, t_size),
            get_stack(debug_stack[4], "Round to Phase 1 (e)", z_size, t_size),
            get_stack(debug_stack[5], "Round + Phase 1 (f)", z_size, t_size),
            get_stack(debug_stack[6], "Fine Map (g)", z_size, t_size)
            };
        }
        else {
            P_result = new ImagePlus[] {
                get_stack(coarse_stack, "Coarse Map", z_size, t_size),
                get_stack(fine_stack, "Fine Map", z_size, t_size)
            };
        }
    }
    private ImagePlus get_stack(ImageStack stack, String label,
                                int z_size, int t_size)
    {
        ImagePlus imp = IJ.createHyperStack(
            label, stack.getWidth(), stack.getHeight(), 1, z_size, t_size, 32);
        imp.setStack(stack);
        return imp;
    }
}
