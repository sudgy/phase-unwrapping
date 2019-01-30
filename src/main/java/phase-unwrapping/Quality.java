package edu.pdx.phase_unwrapping;

public interface Quality {
    void set_input(float[][] input);
    void calculate();
    float[][] get_result();
}
