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
import net.imagej.ops.OpService;

public class DoubleWavelengthOpTest {
    @Test public void test_show_steps()
    {
        // I am trying to get the craziest parameters to ensure that everything
        // will be different
        float[][] crazy1 = {
            {3, 1},
            {0, 3}
        };
        float[][] crazy2 = {
            {2, 4},
            {0, 0}
        };
        PhaseImage image1 = new PhaseImage();
        image1.phase_image = crazy1;
        image1.wavelength = 3.1415f + 1.618f + 2.71828f;
        image1.phase_value = 2;
        PhaseImage image2 = new PhaseImage();
        image2.phase_image = crazy2;
        image2.wavelength = 573;
        image2.phase_value = 42;

        float[][][] steps = run(image1, image2, true);
        float[][][] no_steps = run(image1, image2, false);
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 2; ++y) {
                float cs = steps[3][x][y];
                float cns = no_steps[0][x][y];
                assertEquals(cs, cns, "The coarse map should be the same "
                    + "whether or not you showed steps or not.  This is at ("
                    + x + ", " + y + ").");
                float fs = steps[6][x][y];
                float fns = no_steps[1][x][y];
                assertEquals(fs, fns, "The fine map should be the same whether "
                    + "or not you showed steps or not.  This is at (" + x + ", "
                    + y + ").");
            }
        }
    }
    @Test public void test_same()
    {
        float[][] same = {
            {0, 4},
            {1, 2}
        };
        PhaseImage image1 = new PhaseImage();
        image1.phase_image = same;
        image1.wavelength = 100;
        image1.phase_value = 5;
        PhaseImage image2 = new PhaseImage();
        image2.phase_image = same;
        image2.wavelength = 120;
        image2.phase_value = 5;

        float[][][] steps = run(image1, image2, true);
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 2; ++y) {
                String after = "  This is at (" + x + ", " + y + ").";
                assertEquals(steps[2][x][y], 0, "When the images are the same, "
                    + "step (c) (the difference) should be zero." + after);
                assertEquals(steps[3][x][y], 0, "When the images are the same, "
                    + "step (d) (the coarse map) should be zero." + after);
                assertEquals(steps[4][x][y], 0, "When the images are the same, "
                    + "step (d) (the rounding) should be zero." + after);
            }
        }
    }
    @Test public void test_different_phase_value()
    {
        PhaseImage image1 = new PhaseImage();
        image1.phase_image = new float[][] {{1}};
        image1.wavelength = 5;
        image1.phase_value = 5;
        PhaseImage image2 = new PhaseImage();
        image2.phase_image = new float[][] {{2}};
        image2.wavelength = 5;
        image2.phase_value = 5;
        PhaseImage image3 = new PhaseImage();
        image3.phase_image = new float[][] {{4}};
        image3.wavelength = 5;
        image3.phase_value = 10;

        float[][][] reference = run(image1, image2, true);
        float[][][] different = run(image1, image3, true);
        for (int i = 0; i < 7; ++i) {
            assertEquals(reference[i][0][0], different[i][0][0],
                "At step (" + (i + 'a') + "), the values should be the same.");
        }
    }
    @Test public void test_steps()
    {
        float[][] phase1 = {{1}};
        PhaseImage image1 = new PhaseImage();
        image1.phase_image = phase1;
        image1.wavelength = 2;
        image1.phase_value = 5;
        float[][] phase2 = {{2}};
        PhaseImage image2 = new PhaseImage();
        image2.phase_image = phase2;
        image2.wavelength = 3;
        image2.phase_value = 5;

        // Combined wavelength is 6 (2 * 3 / (2 - 1))

        float[][][] steps_ = run(image1, image2, true);
        float[] steps = new float[steps_.length];
        for (int i = 0; i < steps.length; ++i) steps[i] = steps_[i][0][0];

        assertEquals(steps[0], 1, "The first phase image should be "
            + "at the correct step.");
        assertEquals(steps[1], 2, "The first phase image should be "
            + "at the correct step.");
        assertEquals(steps[2], -1, "The third step should be the difference.");
        // This is the previous step, plus the phase value, scaled to use the
        // combined wavelength
        assertEquals(steps[3], 12, "The coarse map should be correct.");
        // Rounded to phase value
        assertEquals(steps[4], 10, "The coarse map should have rounded to the "
            + "phase value.");
        // This might be where the algorithm messes up?
        assertEquals(steps[5], 11, "I don't know how to describe this step, but"
            + " it should work.");
        assertEquals(steps[6], 11, "The fine map, in this particular case, "
            + "should be close enough to the coarse map that there is no "
            + "change.");
    }
    private float[][][] run(PhaseImage image1, PhaseImage image2, boolean steps)
    {
        return (float[][][])M_ops.run(DoubleWavelengthOp.class,
                                      image1, image2, steps);
    }
    private Context M_context = new Context(OpService.class);
    private OpService M_ops = M_context.getService(OpService.class);
}
