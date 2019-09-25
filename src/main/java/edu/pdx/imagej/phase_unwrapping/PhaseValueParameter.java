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

import ij.ImagePlus;
import ij.process.ImageStatistics;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.Parameter;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.DoubleParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;

/** PhaseValueParameter is a DParameter that is a DoubleParameter but will give
 * a warning when it is given a strange phase value.
 * <p>
 * The pixel phase value is the value, in pixels, of a full period of phase.
 * For example, if the phase image is a 32-bit image with values ranging from -π
 * to π, the phase value should be 2π.  If the image was converted to an 8-bit
 * image, the phase value should be 256.
 * <p>
 * This parameter will give an error when the phase value is less than or equal
 * to zero, and will give a warning if the phase value is less than the range of
 * values on the image.
 */
@Plugin(type = DParameter.class)
public class PhaseValueParameter extends DoubleParameter {
    /** Construct a PhaseValueParameter.  The default phase value is 256.0.
     *
     * @param label The label to be used.  It is passed directly to
     *              DoubleParameter's constructor.
     * @param phaseImage The phase image that this parameter is supposed to be
     *                    the phase value of.
     */
    public PhaseValueParameter(String label, ImageParameter phaseImage)
    {
        super(256.0, label);
        M_imageParam = phaseImage;
        setBounds(Double.MIN_VALUE, Double.MAX_VALUE);
        checkForErrors();
    }
    @Override
    public void readFromDialog()
    {
        super.readFromDialog();
        checkForErrors();
    }
    @Override
    public void readFromPrefs(Class<?> c, String name)
    {
        super.readFromPrefs(c, name);
        checkForErrors();
    }
    private void checkForErrors()
    {
        if (M_imageParam.getValue() != M_image) {
            M_image = M_imageParam.getValue();
            ImageStatistics stats = M_image.getStatistics();
            M_minValue = (float)(stats.max - stats.min);
        }
        if (getValue() < M_minValue) {
            setWarning(DParameter.displayLabel(label()) + " is less than the "
                + "range of values present in the image " + M_image.getTitle()
                + ".");
        }
        else {
            setWarning(null);
        }
    }

    private ImageParameter M_imageParam;
    private ImagePlus      M_image;
    private float          M_minValue;
}
