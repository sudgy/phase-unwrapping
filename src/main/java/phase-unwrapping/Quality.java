package edu.pdx.imagej.phase_unwrapping;

import org.scijava.Prioritized;
import org.scijava.plugin.Plugin;
import net.imagej.ImageJPlugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;

public interface Quality extends ImageJPlugin, Prioritized {
    default DParameter param() {return null;}

    default void set_phase_value(float phase_value) {}
    float[][]    calculate(float[][] phase_image, int t, int z);
    float[][]    get_result();
    default int  get_ts() {return 1;}
    default int  get_zs() {return 1;}
}
