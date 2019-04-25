package edu.pdx.imagej.phase_unwrapping;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Quality.class, name = "Phase Gradient", priority = Priority.VERY_HIGH * 0.999) // Be right after normal gradient
public class PhaseGradientQuality extends AbstractQuality {
    @Override
    public void set_phase_value(float phase_value) {M_phase = phase_value;}
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        M_data = new float[phase_image.length][phase_image[0].length];
        for (int x = 0; x < M_data.length; ++x) {
            for (int y = 0; y < M_data[0].length; ++y) {
                for (int x_plus = -1; x_plus <= 1; ++x_plus) {
                    for (int y_plus = -1; y_plus <= 1; ++y_plus) {
                        if (x_plus == 0 && y_plus == 0) continue;
                        int new_x = x + x_plus;
                        int new_y = y + y_plus;
                        if (new_x == -1 || new_y == -1) continue;
                        if (new_x == M_data.length || new_y == M_data[0].length) continue;
                        float difference = phase_image[x][y] - phase_image[new_x][new_y];
                        if (difference < -M_phase / 2) difference += M_phase;
                        if (difference > M_phase / 2) difference -= M_phase;
                        M_data[x][y] -= Math.abs(difference);
                    }
                }
            }
        }
        return M_data;
    }
    @Override public float[][] get_result() {return M_data;}
    private float[][] M_data;
    private float M_phase;
}
