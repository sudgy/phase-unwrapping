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
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

/** This is a command that performs double-wavelength unwrapping of images and
 * stacks using {@link DoubleWavelengthOp}.  In addition to what
 * DoubleWavelengthOp does, this command deals with stacks of images.
 */
@Plugin(type = Command.class,
        menuPath = "Plugins>DHM>Phase Unwrapping>Double Wavelength")
public class DoubleWavelengthCommand implements Command {
    @Parameter(label = "Phase Image 1")
    private ImagePlus P_phase_image1;
    @Parameter(label = "Wavelength 1")
    private int P_wavelength1;
    @Parameter(label = "Phase Image 2")
    private ImagePlus P_phase_image2;
    @Parameter(label = "Wavelength 2")
    private int P_wavelength2;
    // The main reason this is not τ is because having it τ when it should be
    // 256 makes it look good when it shouldn't, but having it 256 when it
    // should be τ makes it look bad when it should.
    @Parameter(label = "Pixel phase value")
    private float P_phase_value = 256.0f;
    @Parameter(label = "Show Intermediate Steps")
    private boolean P_show_steps = false;

    @Parameter private OpService P_ops;

    /** Run the command, computing and showing all unwrapping. */
    @Override
    public void run() {
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
                image1.phase_value = P_phase_value;
                PhaseImage image2 = new PhaseImage();
                image2.phase_image = img2;
                image2.wavelength = P_wavelength2;
                image2.phase_value = P_phase_value;
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
            show_stack(debug_stack[0], "Phase Image 1 (a)", z_size, t_size);
            show_stack(debug_stack[1], "Phase Image 2 (b)", z_size, t_size);
            show_stack(debug_stack[2], "Phase Difference (c)", z_size, t_size);
            show_stack(debug_stack[3], "Coarse Map (d)", z_size, t_size);
            show_stack(debug_stack[4], "Round to Phase 1 (e)", z_size, t_size);
            show_stack(debug_stack[5], "Round + Phase 1 (f)", z_size, t_size);
            show_stack(debug_stack[6], "Fine Map (g)", z_size, t_size);
        }
        else {
            show_stack(coarse_stack, "Coarse Map", z_size, t_size);
            show_stack(fine_stack, "Fine Map", z_size, t_size);
        }
    }
    private void show_stack(ImageStack stack, String label,
                            int z_size, int t_size)
    {
        ImagePlus imp = IJ.createHyperStack(
            label, stack.getWidth(), stack.getHeight(), 1, z_size, t_size, 32);
        imp.setStack(stack);
        imp.show();
    }
}
