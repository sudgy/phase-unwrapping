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

import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

import edu.pdx.imagej.dynamic_parameters.*;

@Plugin(type = Command.class, menuPath = "Plugins>DHM>Phase Unwrapping>Single Wavelength")
public class PhaseUnwrapping implements Command, Initializable {
    @Parameter private OpService        P_ops;

    @Parameter private ImageParameter   P_phase_image;
    @Parameter private QualityParameter P_quality;
    @Parameter private BoolParameter    P_single_frame;
    @Parameter private DoubleParameter  P_phase_value;
    @Parameter private ChoiceParameter  P_output_type;
    @Parameter private BoolParameter    P_show_progress;

    @Override
    public void initialize()
    {
        P_phase_image = new ImageParameter("Phase_Image");
        P_quality = new QualityParameter();
        P_single_frame = new BoolParameter("Single_Frame", false);
        P_phase_value = new DoubleParameter(256.0, "Pixel_Phase_Value");
        String[] choices = {"8-bit", "32-bit", "32-bit (radians)"};
        P_output_type = new ChoiceParameter("Output_Type", choices);
        P_show_progress = new BoolParameter("Show Progress", true);
    }

    public void run() {
        ImagePlus imp;
        Quality quality = P_quality.get_value();
        boolean show_progress = P_show_progress.get_value();
        float phase_value = (float)P_phase_value.get_value().doubleValue();
        quality.set_phase_value((float)P_phase_value.get_value().doubleValue());
        ImagePlus phase_image = P_phase_image.get_value();
        if (P_single_frame.get_value()) {
            float[][] image = phase_image.getProcessor().getFloatArray();
            quality.calculate(image, 1, 1);
            float[][] result = (float[][])P_ops.run(
                "Single Wavelength Phase Unwrapping",
                image, quality, show_progress, phase_value);
            imp = new ImagePlus("Result", convert_result(result));
        }
        else {
            int ts = phase_image.getNFrames();
            int zs = phase_image.getNSlices();
            int width = phase_image.getProcessor().getWidth();
            int height = phase_image.getProcessor().getHeight();
            ImageStack result = new ImageStack(width, height);
            ImagePlus quality_img = null;
            int q_ts = quality.get_ts();
            int q_zs = quality.get_zs();
            if (q_ts == 0) q_ts = ts;
            if (q_zs == 0) q_zs = zs;
            if (q_ts != ts && q_zs != zs) {
                float[][] image = phase_image.getProcessor().getFloatArray();
                quality.calculate(image, 1, 1);
            }
            for (int t = 1; t <= ts; ++t) {
                if (q_ts == ts && q_zs != zs) {
                    int current_slice = phase_image.getStackIndex(1, 1, t);
                    float[][] image = phase_image.getStack()
                                                 .getProcessor(current_slice)
                                                 .getFloatArray();
                    quality.calculate(image, t, 1);
                }
                for (int z = 1; z <= zs; ++z) {
                    if (q_zs == zs) {
                        int t_ = q_ts == ts ? t : 1;
                        int current_slice = phase_image.getStackIndex(1, z, t_);
                        float[][] image = phase_image.getStack()
                                                    .getProcessor(current_slice)
                                                    .getFloatArray();
                        quality.calculate(image, t_, z);
                    }

                    int current_slice = phase_image.getStackIndex(1, z, t);
                    float[][] image = phase_image.getStack()
                                                 .getProcessor(current_slice)
                                                 .getFloatArray();
                    float[][] this_result = (float[][])P_ops.run(
                        "Single Wavelength Phase Unwrapping",
                        image, quality, show_progress, phase_value);
                    String label = phase_image.getStack()
                                              .getSliceLabel(current_slice)
                                              + ", unwrapped";
                    result.addSlice(label, convert_result(this_result));
                }
            }
            String label = phase_image.getTitle() + ", unwrapped";
            imp = IJ.createHyperStack(label, width, height, 1, zs, ts, 32);
            imp.setStack(result);
        }
        imp.copyScale(phase_image);
        imp.show();
    }

    private ImageProcessor convert_result(float[][] image)
    {
        if (P_output_type.get_value().equals("8-bit")) {
            return new FloatProcessor(image).convertToByteProcessor();
        }
        else if (P_output_type.get_value().equals("32-bit")) {
            return new FloatProcessor(image);
        }
        else { // 32-bit radians
            for (int x = 0; x < image.length; ++x) {
                for (int y = 0; y < image[0].length; ++y) {
                    image[x][y] /= P_phase_value.get_value();
                    image[x][y] *= Math.PI * 2;
                }
            }
            return new FloatProcessor(image);
        }
    }
}
