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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

import edu.pdx.imagej.dynamic_parameters.*;

/** This is a light wrapper around {@link DoubleWavelengthStackOp}.  The only
 * difference is that this command is meant to be used by the user while ImageJ
 * is open, while DoubleWavelengthStackOp is meant to be used by the programmer.
 */
@Plugin(type = Command.class,
        menuPath = "Plugins>DHM>Phase Unwrapping>Double Wavelength")
public class DoubleWavelengthCommand implements Command, Initializable {
    @Parameter private OpService P_ops;

    @Parameter private ImageParameter      P_phaseImage1;
    @Parameter private DoubleParameter     P_wavelength1;
    @Parameter private ImageParameter      P_phaseImage2;
    @Parameter private DoubleParameter     P_wavelength2;
    @Parameter private PhaseValueParameter P_phaseValue;
    @Parameter private BoolParameter       P_showSteps;

    /** Initializes the dynamic parameters. */
    @Override
    public void initialize()
    {
        P_phaseImage1 = new ImageParameter("Phase_image_1");
        P_wavelength1  = new DoubleParameter(0.0, "Wavelength_1");
        P_phaseImage2 = new ImageParameter("Phase_image_2");
        P_wavelength2  = new DoubleParameter(0.0, "Wavelength_2");
        P_phaseValue  = new PhaseValueParameter("Pixel_phase_value",
                                                 P_phaseImage1);
        P_showSteps   = new BoolParameter("Show_intermediate_steps", false);

        P_wavelength1.setBounds(Double.MIN_VALUE, Double.MAX_VALUE);
        P_wavelength2.setBounds(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /** Run the command, computing and showing all unwrapping. */
    @Override
    public void run() {
        ImagePlus[] result = (ImagePlus[])P_ops.run(
            "Double Wavelength Phase Unwrapping",
            P_phaseImage1.getValue(),
            P_wavelength1.getValue(),
            P_phaseValue.getValue(),
            P_phaseImage2.getValue(),
            P_wavelength2.getValue(),
            P_phaseValue.getValue(),
            P_showSteps.getValue());
        for (ImagePlus stack : result) stack.show();
    }
}
