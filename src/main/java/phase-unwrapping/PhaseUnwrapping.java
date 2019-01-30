package edu.pdx.phase_unwrapping;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import edu.pdx.phase_unwrapping.VisibilityQuality;
import edu.pdx.phase_unwrapping.GradientQuality;
import edu.pdx.phase_unwrapping.SingleWavelength;

@Plugin(type = Command.class, menuPath = "Plugins>DHM>Phase Unwrapping>Single Wavelength")
public class PhaseUnwrapping implements Command {
    @Parameter(label = "Phase Image")
    private ImagePlus M_phase_image;
    @Parameter(label = "Quality Type", choices = {"Fringe Visibility", "Phase Gradient", "None"})
    private String M_quality_type;
    @Parameter(label = "Hologram")
    private ImagePlus M_hologram;
    @Parameter(label = "Single Frame")
    private boolean M_single_frame;
    @Parameter(label = "Pixel phase value")
    private float M_phase = 6.2831853072f;

    public void run() {
        ImagePlus imp;
        Quality quality = null;
        if (M_single_frame) {
            float[][] image = M_phase_image.getProcessor().getFloatArray();
            if (M_quality_type.equals("Fringe Visibility")) {
                quality = new VisibilityQuality();
                quality.set_input(M_hologram.getProcessor().getFloatArray());
            }
            else if (M_quality_type.equals("Phase Gradient")) {
                quality = new PhaseGradientQuality(M_phase);
                quality.set_input(M_phase_image.getProcessor().getFloatArray());
            }
            quality.calculate();
            new ImagePlus("Quality", new FloatProcessor(quality.get_result())).show();
            SingleWavelength proc = new SingleWavelength(image, quality, true, M_phase);
            proc.calculate();
            imp = new ImagePlus("Result", new FloatProcessor(proc.get_result()));
        }
        else {
            int ts = M_phase_image.getNFrames();
            int zs = M_phase_image.getNSlices();
            int width = M_phase_image.getProcessor().getWidth();
            int height = M_phase_image.getProcessor().getHeight();
            ImageStack result = new ImageStack(width, height);
            ImagePlus quality_img = null;
            if (M_quality_type.equals("Fringe Visibility")) {
                quality = new VisibilityQuality();
                quality_img = M_hologram;
            }
            else if (M_quality_type.equals("Phase Gradient")) {
                quality = new PhaseGradientQuality(M_phase);
                quality_img = M_phase_image;
            }
            int q_ts = quality_img.getNFrames();
            int q_zs = quality_img.getNSlices();
            if (q_ts != ts && q_zs != zs) {
                quality.set_input(quality_img.getProcessor().getFloatArray());
                quality.calculate();
            }
            for (int t = 1; t <= ts; ++t) {
                if (q_ts == ts && q_zs != zs) {
                    int current_q_slice = quality_img.getStackIndex(1, 1, t);
                    quality.set_input(quality_img.getStack().getProcessor(current_q_slice).getFloatArray());
                    quality.calculate();
                }
                for (int z = 1; z <= zs; ++z) {
                    if (q_zs == zs) {
                        int t_ = q_ts == ts ? t : 1;
                        int current_q_slice = quality_img.getStackIndex(1, z, t_);
                        quality.set_input(quality_img.getStack().getProcessor(current_q_slice).getFloatArray());
                        quality.calculate();
                    }

                    int current_slice = M_phase_image.getStackIndex(1, z, t);
                    float[][] image = M_phase_image.getStack().getProcessor(current_slice).getFloatArray();
                    SingleWavelength proc = new SingleWavelength(image, quality, true, M_phase);
                    proc.calculate();
                    ImageProcessor slice_result = new FloatProcessor(proc.get_result()).convertToByteProcessor();
                    result.addSlice(M_phase_image.getStack().getSliceLabel(current_slice) + ", unwrapped", slice_result);
                }
            }
            imp = IJ.createHyperStack(M_phase_image.getTitle() + ", unwrapped", width, height, 1, zs, ts, 8);
            imp.setStack(result);
            //imp.setCalibration(...);
        }
        imp.show();
    }
}
