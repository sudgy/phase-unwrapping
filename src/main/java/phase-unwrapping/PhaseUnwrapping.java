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

import edu.pdx.imagej.dynamic_parameters.*;

@Plugin(type = Command.class, menuPath = "Plugins>DHM>Phase Unwrapping>Single Wavelength")
public class PhaseUnwrapping implements Command, Initializable {
    @Parameter private ImageParameter   P_phase_image;
    @Parameter private QualityParameter P_quality;
    @Parameter private BoolParameter    P_single_frame;
    @Parameter private DoubleParameter  P_phase_value;
    @Parameter private ChoiceParameter  P_output_type;

    @Override
    public void initialize()
    {
        P_phase_image = new ImageParameter("Phase_Image");
        P_quality = new QualityParameter();
        P_single_frame = new BoolParameter("Single_Frame", false);
        P_phase_value = new DoubleParameter(256.0, "Pixel_Phase_Value");
        String[] choices = {"8-bit", "32-bit", "32-bit (radians)"};
        P_output_type = new ChoiceParameter("Output_Type", choices);
    }

    public void run() {
        ImagePlus imp;
        Quality quality = P_quality.get_value();
        quality.set_phase_value((float)P_phase_value.get_value().doubleValue());
        ImagePlus phase_image = P_phase_image.get_value();
        if (P_single_frame.get_value()) {
            float[][] image = phase_image.getProcessor().getFloatArray();
            quality.calculate(image, 1, 1);
            SingleWavelength proc = new SingleWavelength(image, quality, true, (float)P_phase_value.get_value().doubleValue());
            proc.calculate();
            imp = new ImagePlus("Result", convert_result(proc.get_result()));
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
            if (q_ts != ts && q_zs != zs) {
                quality.calculate(phase_image.getProcessor().getFloatArray(), 1, 1);
            }
            for (int t = 1; t <= ts; ++t) {
                if (q_ts == ts && q_zs != zs) {
                    int current_slice = phase_image.getStackIndex(1, 1, t);
                    quality.calculate(phase_image.getStack().getProcessor(current_slice).getFloatArray(), t, 1);
                }
                for (int z = 1; z <= zs; ++z) {
                    if (q_zs == zs) {
                        int t_ = q_ts == ts ? t : 1;
                        int current_slice = phase_image.getStackIndex(1, z, t_);
                        quality.calculate(phase_image.getStack().getProcessor(current_slice).getFloatArray(), t_, z);
                    }

                    int current_slice = phase_image.getStackIndex(1, z, t);
                    float[][] image = phase_image.getStack().getProcessor(current_slice).getFloatArray();
                    SingleWavelength proc = new SingleWavelength(image, quality, true, (float)P_phase_value.get_value().doubleValue());
                    proc.calculate();
                    result.addSlice(phase_image.getStack().getSliceLabel(current_slice) + ", unwrapped", convert_result(proc.get_result()));
                }
            }
            imp = IJ.createHyperStack(phase_image.getTitle() + ", unwrapped", width, height, 1, zs, ts, 32);
            imp.setStack(result);
        }
        //imp.setCalibration(...);
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
