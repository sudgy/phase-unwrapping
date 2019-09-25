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

/** NoneQuality is a {@link Quality} that does nothing. */
@Plugin(type = Quality.class, name = "None", priority = Priority.FIRST)
public class NoneQuality extends AbstractQuality {
    /** {@inheritDoc}
     * <p>
     * For NoneQuality, this just returns an array of zeros.
     */
    @Override
    public float[][] calculate(float[][] phaseImage, int t, int z)
    {
        M_result = new float[phaseImage.length][phaseImage[0].length];
        return M_result;
    }
    /** {@inheritDoc}
     * <p>
     * For NoneQuality, this just returns an array of zeros.
     */
    @Override
    public float[][] getResult()
    {
        return M_result;
    }
    private float[][] M_result;
}
