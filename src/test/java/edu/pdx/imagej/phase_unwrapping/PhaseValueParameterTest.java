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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import edu.pdx.imagej.dynamic_parameters.ImageParameter;
import edu.pdx.imagej.dynamic_parameters.TestDialog;

public class PhaseValueParameterTest {
    @Test public void testDefault()
    {
        ImageParameter bigParam = new ImageParameter("", new ImagePlus[] {
            new ImagePlus("", new FloatProcessor(new float[][]{{0, 300}}))});
        PhaseValueParameter param = new PhaseValueParameter("", bigParam);
        assertEquals(param.getValue().doubleValue(), 256.0,
            "PhaseValueParameter should start with the correct default value.");
        assertTrue(param.getWarning() != null, "If PhaseValueParameter starts "
            + "with a bad value, it should instantly have its warning.");
    }
    @Test public void testBounds()
    {
        TestDialog dialog = new TestDialog();
        PhaseValueParameter param = new PhaseValueParameter("", M_imageParam);
        param.addToDialog(dialog);

        dialog.getDouble(0).value = -1.0;
        param.readFromDialog();
        assertTrue(param.getError() != null, "After inputting a negative "
            + "number, a PhaseValueParameter should have an error.");

        dialog.getDouble(0).value = 0.0;
        param.readFromDialog();
        assertTrue(param.getError() != null, "After inputting zero, a "
            + "PhaseValueParameter should have an error.");

        dialog.getDouble(0).value = 1.0;
        param.readFromDialog();
        assertTrue(param.getError() == null, "After inputting a positive "
            + "number, PhaseValueParameter should not have an error.");
    }
    @Test public void testWarning()
    {
        TestDialog dialog = new TestDialog();
        PhaseValueParameter param = new PhaseValueParameter("", M_imageParam);
        param.addToDialog(dialog);

        assertTrue(param.getWarning() == null, "If PhaseValueParameter starts "
            + "with a good value, it should have no warning.");

        dialog.getDouble(0).value = 5.0;
        param.readFromDialog();
        assertTrue(param.getWarning() != null, "Setting PhaseValueParameter to"
            + " a bad value should make a warning.");

        dialog.getDouble(0).value = 300.0;
        param.readFromDialog();
        assertTrue(param.getWarning() == null, "Setting PhaseValueParameter to"
            + " a good value should remove a warning.");
    }
    private FloatProcessor M_processor =
        new FloatProcessor(new float[][] {{0, 200}});
    private ImagePlus M_imp = new ImagePlus("", M_processor);
    private ImageParameter M_imageParam
        = new ImageParameter("", new ImagePlus[]{M_imp});
}
