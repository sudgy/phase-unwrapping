package edu.pdx.imagej.phase_unwrapping;

import ij.ImagePlus;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;

@Plugin(type = Quality.class, name = "Gradient", priority = Priority.VERY_HIGH * 0.9999) // Be right before Phase Gradient
public class GradientQuality extends AbstractQuality {
    @Override
    public DParameter param()
    {
        if (M_image == null) {
            M_image = new ImageParameter("Gradient Image");
        }
        return M_image;
    }
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        ImagePlus image = M_image.get_value();
        int current_slice = image.getStackIndex(1, z, t);
        float[][] data = M_image.get_value().getStack().getProcessor(current_slice).getFloatArray();
        M_result = new float[data.length][data[0].length];
        for (int x = 0; x < M_result.length; ++x) {
            for (int y = 0; y < M_result[0].length; ++y) {
                for (int x_plus = -1; x_plus <= 1; ++x_plus) {
                    for (int y_plus = -1; y_plus <= 1; ++y_plus) {
                        if (x_plus == 0 && y_plus == 0) continue;
                        int new_x = x + x_plus;
                        int new_y = y + y_plus;
                        if (new_x == -1 || new_y == -1) continue;
                        if (new_x == M_result.length || new_y == M_result[0].length) continue;
                        M_result[x][y] -= Math.abs(data[x][y] - data[new_x][new_y]);
                    }
                }
            }
        }
        return M_result;
    }
    @Override public float[][] get_result() {return M_result;}
    @Override public int get_ts() {return M_image.get_value().getNFrames();}
    @Override public int get_zs() {return M_image.get_value().getNSlices();}

    private ImageParameter M_image;
    private float[][] M_result;
}
