package edu.pdx.imagej.phase_unwrapping;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin(type = Quality.class, name = "None", priority = Priority.FIRST)
public class NoneQuality extends AbstractQuality {
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        M_result = new float[phase_image.length][phase_image[0].length];
        return M_result;
    }
    @Override
    public float[][] get_result()
    {
        return M_result;
    }
    float[][] M_result;
}
