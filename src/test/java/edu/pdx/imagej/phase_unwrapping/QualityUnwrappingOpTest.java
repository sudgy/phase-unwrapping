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

import org.scijava.Context;
import org.scijava.app.StatusService;
import net.imagej.ops.OpService;

public class QualityUnwrappingOpTest {
    @Test public void testNormalUnwrapping()
    {
        OpService ops = M_context.getService(OpService.class);
        float[][] phaseImage = {
            {0, 1, 4, 7, 9, 2, 5, 3, 9, 5}
        };
        Quality quality = new NoneQuality();
        quality.calculate(phaseImage, 0, 0);
        float[][] result = (float[][])ops.run(QualityUnwrappingOp.class,
                                              phaseImage, quality, false, 10);
        float[] differences = {1, 3, 3, 2, 3, 3, -2, -4, -4};
        for (int i = 0; i < 9; ++i) {
            float diff = result[0][i + 1] - result[0][i];
            assertEquals(diff, differences[i], "The unwrapping should produce "
                + "the correct values for the differences between values.");
        }
    }
    @Test public void testQuality()
    {
        OpService ops = M_context.getService(OpService.class);
        float[][] phaseImage = {
            {0, 3},
            {8, 5}
        };
        // The algorithm will start on the bottom right, which is its best guess
        // at the middle
        float[][] quality1_values = {
            {0, 1},
            {0, 0}
        };
        float[][] quality2_values = {
            {0, 0},
            {1, 0}
        };
        Quality quality1 = new TestQuality();
        Quality quality2 = new TestQuality();
        quality1.calculate(quality1_values, 0, 0);
        quality2.calculate(quality2_values, 0, 0);
        float[][] result1 =(float[][])ops.run(QualityUnwrappingOp.class,
                                              phaseImage, quality1, false, 10);
        float[][] result2 =(float[][])ops.run(QualityUnwrappingOp.class,
                                              phaseImage, quality2, false, 10);
        assertEquals(result1[0][0], 0, "The quality should affect the path "
            + "taken for residues (1).");
        assertEquals(result2[0][0], 10, "The quality should affect the path "
            + "taken for residues (2).");
    }
    private Context M_context = new Context(OpService.class, StatusService.class);
}

class TestQuality extends AbstractQuality {
    @Override
    public float[][] calculate(float[][] phaseImage, int t, int z)
    {
        M_result = phaseImage;
        return M_result;
    }
    public float[][] getResult()
    {
        return M_result;
    }
    float[][] M_result;
}
