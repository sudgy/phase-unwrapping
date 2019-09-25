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
    @Parameter private ImagePlus P_phaseImage1;
    @Parameter private float     P_wavelength1;
    @Parameter private float     P_phaseValue1;
    @Parameter private ImagePlus P_phaseImage2;
    @Parameter private float     P_wavelength2;
    @Parameter private float     P_phaseValue2;
    @Parameter private boolean   P_showSteps;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) private ImagePlus[] P_result;

    @Override
    public void run()
    {
        int width = P_phaseImage1.getWidth();
        int height = P_phaseImage1.getHeight();
        ImageStack[] stacks = P_showSteps ? new ImageStack[7] :
                                             new ImageStack[2];
        for (int i = 0; i < (P_showSteps ? 7 : 2); ++i) {
            stacks[i] = new ImageStack(width, height);
        }

        int tSize = Math.min(P_phaseImage1.getNFrames(),
                              P_phaseImage2.getNFrames());
        int zSize = Math.min(P_phaseImage1.getNSlices(),
                              P_phaseImage2.getNSlices());
        int finalSize = tSize * zSize;
        int i = 0;
        for (int t = 1; t <= tSize; ++t) {
            for (int z = 1; z <= zSize; ++z) {
                if (finalSize > 1) {
                    IJ.showProgress(i, finalSize);
                    ++i;
                }
                computeSlice(stacks, t, z);
            }
        }
        showResult(stacks, zSize, tSize);
    }
    private void computeSlice(ImageStack[] stacks, int t, int z)
    {
        int currentSlice1 = P_phaseImage1.getStackIndex(1, z, t);
        int currentSlice2 = P_phaseImage2.getStackIndex(1, z, t);
        float[][] img1 = P_phaseImage1.getStack()
                                       .getProcessor(currentSlice1)
                                       .getFloatArray();
        float[][] img2 = P_phaseImage2.getStack()
                                       .getProcessor(currentSlice2)
                                       .getFloatArray();
        PhaseImage image1 = new PhaseImage();
        image1.phaseImage = img1;
        image1.wavelength = P_wavelength1;
        image1.phaseValue = P_phaseValue1;
        PhaseImage image2 = new PhaseImage();
        image2.phaseImage = img2;
        image2.wavelength = P_wavelength2;
        image2.phaseValue = P_phaseValue2;
        float[][][] result = (float[][][])P_ops.run(
            "Double Wavelength Phase Unwrapping",
        image1, image2, P_showSteps);
        for (int j = 0; j < result.length; ++j) {
            stacks[j].addSlice(new FloatProcessor(result[j]));
        }
    }
    private void showResult(ImageStack[] stacks, int zSize, int tSize)
    {
        if (P_showSteps) {
            P_result = new ImagePlus[] {
                getStack(stacks[0], "Phase Image 1 (a)", zSize, tSize),
                getStack(stacks[1], "Phase Image 2 (b)", zSize, tSize),
                getStack(stacks[2], "Phase Difference (c)", zSize, tSize),
                getStack(stacks[3], "Coarse Map (d)", zSize, tSize),
                getStack(stacks[4], "Round to Phase 1 (e)", zSize, tSize),
                getStack(stacks[5], "Round + Phase 1 (f)", zSize, tSize),
                getStack(stacks[6], "Fine Map (g)", zSize, tSize)
            };
        }
        else {
            P_result = new ImagePlus[] {
                getStack(stacks[0], "Coarse Map", zSize, tSize),
                getStack(stacks[1], "Fine Map", zSize, tSize)
            };
        }
    }
    private ImagePlus getStack(ImageStack stack, String label,
                                int zSize, int tSize)
    {
        ImagePlus imp = IJ.createHyperStack(
            label, stack.getWidth(), stack.getHeight(), 1, zSize, tSize, 32);
        imp.setStack(stack);
        return imp;
    }
}
