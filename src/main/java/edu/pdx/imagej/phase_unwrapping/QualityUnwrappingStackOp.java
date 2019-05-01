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
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;
import net.imagej.ops.OpService;

/** QualityUnwrappingStackOp is an Op that calculates single wavelength phase
 * unwrapping on an ImagePlus, being able to unwrap the whole stack.  It also
 * determines which slices should be used in the quality type given to it.  It
 * uses {@link QualityUnwrappingOp} to unwrap each slice.
 * <p>
 * To run this op yourself, it has the name "Quality Guided Phase Unwrapping"
 * and has these parameters:
 * <ol>
 *     <li>Phase image: an <code>ImagePlus</code> representing all of the phase
 *                      images to unwrap.
 *     <li>Quality: An instance of {@link Quality}.  None of the calculations
 *                  have to have been done yet, but its parameters must have
 *                  already been determined.
 *     <li>Show progress: Whether or not you want to watch the progress of the
 *                        algorithm.
 *     <li>Phase value: The pixel phase value for the image.
 *     <li>Single frame: Whether or not you only want the current slice of the
 *                       phase image to be unwrapped.
 *     <li>Output type: An {@link OutputType} saying what type of image the
 *                      output should be.
 * </ol>
 * The result is an <code>ImagePlus</code> with all unwrapped phase images.
 */
@Plugin(type = Op.class, name = "Quality Guided Phase Unwrapping")
public class QualityUnwrappingStackOp extends AbstractOp {
    public enum OutputType {Type8Bit, Type32Bit, Type32BitRadians};

    @Parameter private OpService P_ops;
    // Inputs
    @Parameter private ImagePlus  P_phase_image;
    @Parameter private Quality    P_quality;
    @Parameter private boolean    P_show_progress;
    @Parameter private float      P_phase_value;
    @Parameter private boolean    P_single_frame;
    @Parameter private OutputType P_output_type;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) ImagePlus P_result;

    @Override
    public void run()
    {
        P_quality.set_phase_value(P_phase_value);
        if (P_single_frame) {
            float[][] image = P_phase_image.getProcessor().getFloatArray();
            P_quality.calculate(image, 1, 1);
            float[][] result = (float[][])P_ops.run(
                "Quality Guided Phase Unwrapping",
                image, P_quality, P_show_progress, P_phase_value);
            P_result = new ImagePlus("Result", convert_result(result));
        }
        else {
            int ts = P_phase_image.getNFrames();
            int zs = P_phase_image.getNSlices();
            int width = P_phase_image.getProcessor().getWidth();
            int height = P_phase_image.getProcessor().getHeight();
            ImageStack result = new ImageStack(width, height);
            ImagePlus P_quality_img = null;
            int q_ts = P_quality.get_ts();
            int q_zs = P_quality.get_zs();
            if (q_ts == 0) q_ts = ts;
            if (q_zs == 0) q_zs = zs;
            if (q_ts != ts && q_zs != zs) {
                float[][] image = P_phase_image.getProcessor().getFloatArray();
                P_quality.calculate(image, 1, 1);
            }
            for (int t = 1; t <= ts; ++t) {
                if (q_ts == ts && q_zs != zs) {
                    int current_slice = P_phase_image.getStackIndex(1, 1, t);
                    float[][] image = P_phase_image.getStack()
                                                 .getProcessor(current_slice)
                                                 .getFloatArray();
                    P_quality.calculate(image, t, 1);
                }
                for (int z = 1; z <= zs; ++z) {
                    if (q_zs == zs) {
                        int t_ = q_ts == ts ? t : 1;
                        int current_slice =
                            P_phase_image.getStackIndex(1, z, t_);
                        float[][] image =
                            P_phase_image.getStack()
                                         .getProcessor(current_slice)
                                         .getFloatArray();
                        P_quality.calculate(image, t_, z);
                    }

                    int current_slice = P_phase_image.getStackIndex(1, z, t);
                    float[][] image = P_phase_image.getStack()
                                                 .getProcessor(current_slice)
                                                 .getFloatArray();
                    float[][] this_result = (float[][])P_ops.run(
                        "Quality Guided Phase Unwrapping",
                        image, P_quality, P_show_progress, P_phase_value);
                    String label = P_phase_image.getStack()
                                              .getSliceLabel(current_slice)
                                              + ", unwrapped";
                    result.addSlice(label, convert_result(this_result));
                }
            }
            String label = P_phase_image.getTitle() + ", unwrapped";
            P_result = IJ.createHyperStack(label, width, height, 1, zs, ts, 32);
            P_result.setStack(result);
        }
        P_result.copyScale(P_phase_image);
    }

    private ImageProcessor convert_result(float[][] image)
    {
        if (P_output_type == OutputType.Type8Bit) {
            return new FloatProcessor(image).convertToByteProcessor();
        }
        else if (P_output_type == OutputType.Type32Bit) {
            return new FloatProcessor(image);
        }
        else { // 32-bit radians
            for (int x = 0; x < image.length; ++x) {
                for (int y = 0; y < image[0].length; ++y) {
                    image[x][y] /= P_phase_value;
                    image[x][y] *= Math.PI * 2;
                }
            }
            return new FloatProcessor(image);
        }
    }
}
