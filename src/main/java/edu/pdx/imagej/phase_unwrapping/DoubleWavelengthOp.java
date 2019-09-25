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

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;

/** DoubleWavelengthOp is an Op that performs phase unwrapping through the
 * double wavelength unwrapping.  However, I've noticed that it doesn't work
 * that well, so use at your own risk.
 * <p>
 * To run this op yourself, it has the name "Double Wavelength Phase Unwrapping"
 * and has these parameters:
 * <ol>
 *     <li>P_image1: a {@link PhaseImage} representing the first image.
 *     <li>P_image2: a {@link PhaseImage} representing the second image.
 *     <li>Show steps: Whether or not to show the intermediate steps used during
 *                     the process.  These steps are the (a)-(g) images used in
 *                     every single description of the algorithm in the
 *                     literature.
 * </ol>
 * The result is a <code>float[][][]</code> that depends on whether you asked to
 * show the steps or not.  If show steps is <code>false</code>,
 * <code>result[0]</code> is the coarse map and <code>result[1]</code> is the
 * fine map.  If show steps is <code>true</code>, there are seven images in the
 * result, corresponding to the (a)-(g) images described in the literature.
 */
@Plugin(type = Op.class, name = "Double Wavelength Phase Unwrapping")
public class DoubleWavelengthOp extends AbstractOp {
    // Inputs
    @Parameter private PhaseImage P_image1;
    @Parameter private PhaseImage P_image2;
    @Parameter private boolean P_showSteps;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) private float[][][] P_result;

    @Override
    public void run()
    {
        float combinedWavelength =
                    (P_image1.wavelength * P_image2.wavelength) /
            Math.abs(P_image1.wavelength - P_image2.wavelength);
        int width = P_image1.phaseImage.length;
        int height = P_image1.phaseImage[0].length;
        float phaseValue = P_image1.phaseValue;
        if (P_image1.phaseValue != P_image2.phaseValue) scaleImage2();

        float[][][] result = new float[7][width][height];
        result[0] = P_image1.phaseImage;
        result[1] = P_image2.phaseImage;
        result[2] = subtractImages(P_image1.phaseImage, P_image2.phaseImage);
        result[3] = createCoarseFromDifference(result[2], phaseValue,
                                      combinedWavelength, P_image1.wavelength);
        result[4] = roundTo(result[3], phaseValue);
        result[5] = addImages(result[4], P_image1.phaseImage);
        result[6] = bringCloseTo(result[5], result[3], phaseValue);
        if (P_showSteps) P_result = result;
        else P_result = new float[][][] {result[3], result[6]};
    }
    private void scaleImage2()
    {
        int width = P_image1.phaseImage.length;
        int height = P_image1.phaseImage[0].length;
        float phaseValue = P_image1.phaseValue;
        // If we don't copy, we change the values passed in from the call
        // site.  We don't want that.
        PhaseImage newPhaseImage = new PhaseImage();
        newPhaseImage.wavelength = P_image2.wavelength;
        newPhaseImage.phaseValue = phaseValue;
        newPhaseImage.phaseImage = new float[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                newPhaseImage.phaseImage[x][y] =
                    P_image2.phaseImage[x][y]
                    * phaseValue / P_image2.phaseValue;
            }
        }
        P_image2 = newPhaseImage;
    }
    static private float[][] subtractImages(float[][] image1, float[][] image2)
    {
        return ArrayOps.binary(image1, image2, ArrayOps.Subtract);
    }
    static private float[][] createCoarseFromDifference(
        float[][] difference, float phaseValue,
        float combinedWavelength, float wavelength1)
    {
        return ArrayOps.unary(
            ArrayOps.unary(difference, a -> a < 0 ? a + phaseValue : a),
            ArrayOps.MultiplyBy(combinedWavelength / wavelength1));
    }
    static private float[][] roundTo(float[][] image, float round)
    {
        return ArrayOps.unary(image, a -> (int)(a / round) * round);
    }
    static private float[][] addImages(float[][] image1, float[][] image2)
    {
        return ArrayOps.binary(image1, image2, ArrayOps.Add);
    }
    static private float[][] bringCloseTo(float[][] from,
                                            float[][] to,
                                            float threshold)
    {
        return ArrayOps.binary(from, to,
            (a, b) -> Math.abs(a - b) > (threshold / 2)
                ? a - threshold * Math.signum(a - b)
                : a);
    }
}
