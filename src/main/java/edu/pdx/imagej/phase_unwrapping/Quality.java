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

import org.scijava.Prioritized;
import org.scijava.plugin.Plugin;
import net.imagej.ImageJPlugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;

/** Quality is the interface for an algorithm that gives a quality value for
 * every pixel in an image, to be used in {@link SingleWavelengthOp}.  While it
 * is possible to implement a Quality based purely on this interface, it is
 * suggested that you instead extend the {@link AbstractQuality} class, for
 * convenience.
 * <p>
 * The pixel that should be unwrapped first should have a higher value for the
 * quality than a pixel that should be unwrapped later.  Make sure that any
 * algorithms you make follow this convention.
 * <p>
 * This interface declares methods for getting both inputs and outputs.  Some of
 * them may not be necessary, so several of these methods are defaulted.
 */
public interface Quality extends ImageJPlugin, Prioritized {
    /** The dynamic parameter to use to get the inputs for this quality.  It
     * defaults to returning null, which means that it doesn't need any
     * parameters.
     * <p>
     * For details on how to use a DParameter, please consult the documentation
     * for dynamic_parameters.
     *
     * @return The DParameter used to get the inputs for this quality.
     */
    default DParameter param() {return null;}
    /** Set the phase value for the phase image.
     * <p>
     * This phase value will be the phase value used for the phase image passed
     * to {@link calculate}.  Because not all qualities need to know the phase
     * value, this functions defaults to doing nothing.
     *
     * @param phase_value The pixel phase value that is for the images passed in
     *                    to {@link calculate}.
     */
    default void set_phase_value(float phase_value) {}
    /** Calculate the quality values.
     * <p>
     * The other parameters are not used by all qualities.  While the phase
     * image parameter is simple, <code>t</code> and <code>z</code> are not.
     * When unwrapping a stack of images, you sometimes might want to use
     * different images for the quality for each slice, but sometimes not.  The
     * functions {@link get_ts} and {@link get_zs} say how many time values and
     * z values are in this quality's image(s), and if their values match the
     * number of slices in what you are unwrapping, the parameters
     * <code>t</code> and <code>z</code> will have the time and z slice of the
     * current frame that we are unwrapping.  If one of them is not equal, then
     * that parameter will always be one here.
     *
     * @param phase_image The current phase image being unwrapped, as a float
     *                    array.
     * @param t The time slice that should be used to calculate the quality.
     * @param z The z slice that should be used to calculate the quality.
     * @return A float array of all values of the quality, with the highest
     *         values corresponding to pixels that should be unwrapped earliest.
     */
    float[][]    calculate(float[][] phase_image, int t, int z);
    /** Get the last result from calculate.
     *
     * @return The result from the last time {@link calculate} was called.
     */
    float[][]    get_result();
    /** Get the number of time slices this quality can use.  See the description
     * of {@link calculate} for more info.  This defaults to returning zero,
     * which means that this quality doesn't care about time.
     *
     * @return The number of time slices this quality can use.
     */
    default int get_ts() {return 0;}
    /** Get the number of z slices this quality can use.  See the description of
     * {@link calculate} for more info.  This defaults to returning zero, which
     * means that this quality doesn't care about z.
     *
     * @return The number of z slices this quality can use.
     */
    default int get_zs() {return 0;}
}
