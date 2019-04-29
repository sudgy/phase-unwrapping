/* Copyright (C) 2019 Portland State University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For any questions regarding the license, please contact the Free Software
 * Foundation.  For any other questions regarding this program, please contact
 * David Cohoe at dcohoe@pdx.edu.
 */

package edu.pdx.imagej.phase_unwrapping;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/** GradientQuality is a {@link Quality} that uses the "phase gradient" of an
 * image.  The Phase Gradient is like the gradient, but it considers 0 and 2π to
 * be next to each other.  It assigns a higher value to spots with a lower
 * gradient.
 * <p>
 * The idea behind this quality is that the background has a lower phase
 * gradient than any samples, so that the background gets unwrapped first.
 * Then, if there are any unwrapping errors in the sample, they will stay local.
 */
@Plugin(type = Quality.class,
        name = "Phase Gradient",
        priority = Priority.VERY_HIGH * 0.999) // Right after normal gradient
public class PhaseGradientQuality extends AbstractQuality {
    /** {@inheritDoc}
     * <p>
     * PhaseGradientQuality does use the phase value, and this function saves
     * it.
     */
    @Override
    public void set_phase_value(float phase_value) {M_phase = phase_value;}
    /** {@inheritDoc} */
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
                        if (new_x == M_data.length ||
                            new_y == M_data[0].length) continue;
                        float difference = phase_image[x][y]
                                           - phase_image[new_x][new_y];
                        if (difference < -M_phase / 2) difference += M_phase;
                        if (difference > M_phase / 2) difference -= M_phase;
                        M_data[x][y] -= Math.abs(difference);
                    }
                }
            }
        }
        return M_data;
    }
    /** {@inheritDoc} */
    @Override public float[][] get_result() {return M_data;}
    private float[][] M_data;
    private float M_phase;
}
