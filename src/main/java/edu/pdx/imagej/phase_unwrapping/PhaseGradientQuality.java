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
    public void setPhaseValue(float phaseValue) {M_phase = phaseValue;}
    /** {@inheritDoc} */
    @Override
    public float[][] calculate(float[][] phaseImage, int t, int z)
    {
        M_data = new float[phaseImage.length][phaseImage[0].length];
        for (int x = 0; x < M_data.length; ++x) {
            for (int y = 0; y < M_data[0].length; ++y) {
                for (int xPlus = -1; xPlus <= 1; ++xPlus) {
                    for (int yPlus = -1; yPlus <= 1; ++yPlus) {
                        if (xPlus == 0 && yPlus == 0) continue;
                        int newX = x + xPlus;
                        int newY = y + yPlus;
                        if (newX == -1 || newY == -1) continue;
                        if (newX == M_data.length ||
                            newY == M_data[0].length) continue;
                        float difference = phaseImage[x][y]
                                           - phaseImage[newX][newY];
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
    @Override public float[][] getResult() {return M_data;}
    private float[][] M_data;
    private float M_phase;
}
