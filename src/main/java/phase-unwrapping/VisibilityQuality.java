package edu.pdx.imagej.phase_unwrapping;

import ij.ImagePlus;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;

import edu.pdx.fringe_visibility.FringeVisibility;

@Plugin(type = Quality.class,
        name = "Fringe Visibility",
        priority = Priority.VERY_HIGH)
public class VisibilityQuality extends AbstractQuality {
    @Override
    public DParameter param()
    {
        if (M_holo == null) {
            M_holo = new ImageParameter("Hologram");
        }
        return M_holo;
    }
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        ImagePlus hologram = M_holo.get_value();
        int current_slice = hologram.getStackIndex(1, z, t);
        float[][] data = hologram.getStack()
                                 .getProcessor(current_slice)
                                 .getFloatArray();
        M_fv = new FringeVisibility(data);
        M_fv.calculate();
        return M_fv.result();
    }
    @Override public float[][] get_result() {return M_fv.result();}
    @Override public int get_ts() {return M_holo.get_value().getNFrames();}
    @Override public int get_zs() {return M_holo.get_value().getNSlices();}

    private FringeVisibility M_fv;
    private ImageParameter   M_holo;
}
