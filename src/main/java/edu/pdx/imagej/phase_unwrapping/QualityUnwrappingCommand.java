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
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.OpService;

import edu.pdx.imagej.dynamic_parameters.*;

/** This is a light wrapper around {@link QualityUnwrappingStackOp}.  The only
 * difference is that this command is meant to be used by the user while ImageJ
 * is open, while QualityUnwrappingStackOp is meant to be used by the
 * programmer.
 */
@Plugin(type = Command.class,
        menuPath = "Plugins>DHM>Phase Unwrapping>Quality Guided")
public class QualityUnwrappingCommand implements Command, Initializable {
    @Parameter private OpService P_ops;

    @Parameter private ImageParameter      P_phaseImage;
    @Parameter private QualityParameter    P_quality;
    @Parameter private BoolParameter       P_singleFrame;
    @Parameter private PhaseValueParameter P_phaseValue;
    @Parameter private ChoiceParameter     P_outputType;
    @Parameter private BoolParameter       P_showProgress;

    /** Initializes the dynamic parameters. */
    @Override
    public void initialize()
    {
        P_phaseImage = new ImageParameter("Phase_image");
        P_quality = new QualityParameter();
        P_singleFrame = new BoolParameter("Single_frame", false);
        P_phaseValue = new PhaseValueParameter("Pixel_phase_value",
                                                P_phaseImage);
        String[] choices = {"8-bit", "32-bit", "32-bit (radians)"};
        P_outputType = new ChoiceParameter("Output_type", choices);
        P_showProgress = new BoolParameter("Show Progress", true);
    }

    /** Run the command, computing and showing all unwrapping. */
    public void run() {
        QualityUnwrappingStackOp.OutputType type = null;
        switch (P_outputType.getValue()) {
            case "8-bit":
                type = QualityUnwrappingStackOp.OutputType.Type8Bit;
                break;
            case "32-bit":
                type = QualityUnwrappingStackOp.OutputType.Type32Bit;
                break;
            case "32-bit (radians)":
                type = QualityUnwrappingStackOp.OutputType.Type32BitRadians;
                break;
        }
        ImagePlus result = (ImagePlus)P_ops.run(
            "Quality Guided Phase Unwrapping",
            P_phaseImage.getValue(),
            P_quality.getValue(),
            P_showProgress.getValue(),
            (float)P_phaseValue.getValue().doubleValue(),
            P_singleFrame.getValue(),
            type
        );
        result.show();
    }
}
