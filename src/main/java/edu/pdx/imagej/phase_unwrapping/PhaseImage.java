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

/** PhaseImage is a data structure that holds information for a phase image. */
public class PhaseImage {
    /** The phase image itself. */
    public float[][] phaseImage;
    /** The wavelength of the light used to get this phase image.  It is only
     * used in {@link DoubleWavelengthOp}, where only the difference between the
     * wavelengths matter, and not their actual value.  Thus, the units on this
     * value don't matter.
     */
    public float wavelength;
    /** The pixel phase value of the image.  The pixel phase value is the
     * difference between the highest value the phase can have to the lowest
     * value it can have.  The most common values that this can take are 256 for
     * an 8-bit image, and 2π for a 32-bit float that goes from -π to π.
     */
    public float phaseValue;
}
