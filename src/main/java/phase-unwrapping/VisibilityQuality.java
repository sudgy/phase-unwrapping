package edu.pdx.phase_unwrapping;

import edu.pdx.phase_unwrapping.Quality;
import edu.pdx.fringe_visibility.FringeVisibility;

class VisibilityQuality implements Quality {
    private FringeVisibility M_fv;
    public void set_input(float[][] input)
    {
        M_fv = new FringeVisibility(input);
    }
    public void calculate()
    {
        M_fv.calculate();
    }
    public float[][] get_result()
    {
        return M_fv.result();
    }
}
