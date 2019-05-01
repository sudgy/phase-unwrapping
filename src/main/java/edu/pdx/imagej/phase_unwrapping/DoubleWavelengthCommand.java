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
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

/** This is a light wrapper around {@link DoubleWavelengthStackOp}.  The only
 * difference is that this command is meant to be used by the user while ImageJ
 * is open, while DoubleWavelengthStackOp is meant to be used by the programmer.
 */
@Plugin(type = Command.class,
        menuPath = "Plugins>DHM>Phase Unwrapping>Double Wavelength")
public class DoubleWavelengthCommand implements Command {
    @Parameter(label = "Phase Image 1")
    private ImagePlus P_phase_image1;
    @Parameter(label = "Wavelength 1")
    private int P_wavelength1;
    @Parameter(label = "Phase Image 2")
    private ImagePlus P_phase_image2;
    @Parameter(label = "Wavelength 2")
    private int P_wavelength2;
    // The main reason this is not τ is because having it τ when it should be
    // 256 makes it look good when it shouldn't, but having it 256 when it
    // should be τ makes it look bad when it should.
    @Parameter(label = "Pixel phase value")
    private float P_phase_value = 256.0f;
    @Parameter(label = "Show Intermediate Steps")
    private boolean P_show_steps = false;

    @Parameter private OpService P_ops;

    /** Run the command, computing and showing all unwrapping. */
    @Override
    public void run() {
        ImagePlus[] result = (ImagePlus[])P_ops.run(
            "Double Wavelength Phase Unwrapping",
            P_phase_image1, P_wavelength1, P_phase_value,
            P_phase_image2, P_wavelength2, P_phase_value,
            P_show_steps);
        for (ImagePlus stack : result) stack.show();
    }
}
