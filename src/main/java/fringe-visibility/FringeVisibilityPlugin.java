package edu.pdx.fringe_visibility;

import ij.plugin.filter.PlugInFilter;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import edu.pdx.fringe_visibility.FringeVisibility;

public class FringeVisibilityPlugin implements PlugInFilter {
    public int setup(String arg, ImagePlus image) {
        return DOES_ALL;
    }
    public void run(ImageProcessor ip) {
        FringeVisibility fv = new FringeVisibility(ip.getFloatArray());
        fv.calculate();
        fv.show();
    }
}
