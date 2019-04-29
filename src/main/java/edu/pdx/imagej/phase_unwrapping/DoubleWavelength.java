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

@Plugin(type = Command.class, menuPath = "Plugins>DHM>Phase Unwrapping>Double Wavelength")
public class DoubleWavelength implements Command {
    @Parameter(label = "Phase Image 1")
    private ImagePlus M_phase_image1;
    @Parameter(label = "Wavelength 1 (nm)")
    private int M_wavelength1;
    @Parameter(label = "Phase Image 2")
    private ImagePlus M_phase_image2;
    @Parameter(label = "Wavelength 2 (nm)")
    private int M_wavelength2;
    @Parameter(label = "Pixel phase value")
    private float M_phase = 6.2831853072f;
    //@Parameter(label = "Debug")
    private boolean M_debug = false;

    private float M_combined_wavelength;

    @Override
    public void run() {
        M_combined_wavelength = (float)(M_wavelength1 * M_wavelength2) / Math.abs(M_wavelength1 - M_wavelength2);
        ImageStack coarse_stack = new ImageStack(M_phase_image1.getWidth(), M_phase_image1.getHeight());
        ImageStack fine_stack = new ImageStack(M_phase_image1.getWidth(), M_phase_image1.getHeight());
        ImageStack debug_stack = null;
        if (M_debug) debug_stack = new ImageStack(M_phase_image1.getWidth(), M_phase_image1.getHeight());

        int final_size = Math.min(M_phase_image1.getImageStackSize(), M_phase_image2.getImageStackSize());
        for (int i = 0; i < final_size; ++i) {
            if (final_size > 1) {
                IJ.showProgress(i + 1, final_size);
            }
            boolean debug = M_debug && i == 0;
            float[][] img1 = M_phase_image1.getStack().getProcessor(i + 1).getFloatArray();
                if (debug) debug_stack.addSlice("Phase Image 1 (a)", new FloatProcessor(img1));
            float[][] img2 = M_phase_image2.getStack().getProcessor(i + 1).getFloatArray();
                if (debug) debug_stack.addSlice("Phase Image 2 (b)", new FloatProcessor(img2));
            float[][] result = ArrayOps.binary(img1, img2, ArrayOps.Subtract);
                if (debug) debug_stack.addSlice("Phase Difference (c)", new FloatProcessor(result));
                if (debug) new ImagePlus("Phase Difference (c)", new FloatProcessor(result)).show();
            result = ArrayOps.unary(result, a -> a < 0 ? a + M_phase : a);
                if (debug) debug_stack.addSlice("Coarse Map (d)", new FloatProcessor(result));
            float[][] coarse = ArrayOps.unary(result, ArrayOps.MultiplyBy(M_combined_wavelength / M_wavelength1));
                if (debug) new ImagePlus("Coarse Map (d)", new FloatProcessor(coarse)).show();
            coarse_stack.addSlice(new FloatProcessor(coarse));
            result = ArrayOps.unary(coarse, a -> (int)(a / M_phase) * M_phase);
                if (debug) debug_stack.addSlice("Round to Phase 1 (e)", new FloatProcessor(ArrayOps.unary(result, ArrayOps.MultiplyBy(M_wavelength1 / M_combined_wavelength))));
                if (debug) new ImagePlus("Round to Phase 1 (e)", new FloatProcessor(result)).show();
            result = ArrayOps.binary(result, img1, ArrayOps.Add);
                if (debug) debug_stack.addSlice("Round + Phase 1 (f)", new FloatProcessor(ArrayOps.unary(result, ArrayOps.MultiplyBy(M_wavelength1 / M_combined_wavelength))));
                if (debug) new ImagePlus("Round + Phase 1 (f)", new FloatProcessor(result)).show();
            result = ArrayOps.binary(result, coarse, (a, b) -> Math.abs(a - b) > (M_phase / 2) ? a - M_phase * Math.signum(a - b) : a);
                if (debug) debug_stack.addSlice("Fine Map (g)", new FloatProcessor(ArrayOps.unary(result, ArrayOps.MultiplyBy(M_wavelength1 / M_combined_wavelength))));
                if (debug) new ImagePlus("Fine Map (g)", new FloatProcessor(result)).show();
            fine_stack.addSlice(new FloatProcessor(result));
            if (debug) (new ImagePlus("Test", debug_stack)).show();
        }
        (new ImagePlus("Coarse Map", coarse_stack)).show();
        (new ImagePlus("Fine Map", fine_stack)).show();
    }
}
