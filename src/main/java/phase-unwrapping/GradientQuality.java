package edu.pdx.phase_unwrapping;

class GradientQuality implements Quality {
    private float[][] M_data;
    public void set_input(float[][] input)
    {
        M_data = input;
    }
    public void calculate()
    {
        float[][] result = new float[M_data.length][M_data[0].length];
        for (int x = 0; x < result.length; ++x) {
            for (int y = 0; y < result[0].length; ++y) {
                for (int x_plus = -1; x_plus <= 1; ++x_plus) {
                    for (int y_plus = -1; y_plus <= 1; ++y_plus) {
                        if (x_plus == 0 && y_plus == 0) continue;
                        int new_x = x + x_plus;
                        int new_y = y + y_plus;
                        if (new_x == -1 || new_y == -1) continue;
                        if (new_x == result.length || new_y == result[0].length) continue;
                        result[x][y] -= Math.abs(M_data[x][y] - M_data[new_x][new_y]);
                    }
                }
            }
        }
        M_data = result;
    }
    public float[][] get_result()
    {
        return M_data;
    }
}
