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

public class PhaseGradientQualityTest {
    @Test public void testWrap()
    {
        float[][] wrap = {
            {0, 3, 0, 1, 9, 1}
        };
        float[][] wrapResult = M_quality.calculate(wrap, 0, 0);
        assertTrue(wrapResult[0][1] < wrapResult[0][4], "The phase gradient "
            + "should wrap values when calculating the gradient.  The actual "
            + "value for the larger change was " + wrapResult[0][1]
            + "while the value for the smaller, wrapped changed was "
            + wrapResult[0][4] + ".");
    }
    // The rest of the tests are just checking that the normal gradient seems
    // fine.  They are directly copied from GradientQualityTest.
    @Test public void testNothing()
    {
        float[][] nothing = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        float[][] nothingResult = M_quality.calculate(nothing, 0, 0);
        assertEquals(nothingResult[0][0], 0,
            "The gradient of a bunch of zeros in the corner should be zero.");
        assertEquals(nothingResult[1][0], 0,
            "The gradient of a bunch of zeros in the side should be zero.");
        assertEquals(nothingResult[1][1], 0,
            "The gradient of a bunch of zeros in the middle should be zero.");
    }
    @Test public void testAll()
    {
        float[][] all = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        float[][] allResult = M_quality.calculate(all, 0, 0);
        assertEquals(allResult[0][0], 0,
            "The gradient of a bunch of ones in the corner should be zero.");
        assertEquals(allResult[1][0], 0,
            "The gradient of a bunch of ones in the side should be zero.");
        assertEquals(allResult[1][1], 0,
            "The gradient of a bunch of ones in the middle should be zero.");
    }
    @Test public void testRamp()
    {
        float[][] ramp = {
            {0, 1, 2},
            {0, 1, 2},
            {0, 1, 2}
        };
        float[][] bigRamp = {
            {0, 2, 4},
            {0, 2, 4},
            {0, 2, 4}
        };
        float[][] rampResult     = M_quality.calculate(ramp, 0, 0);
        float[][] bigRampResult = M_quality.calculate(bigRamp, 0, 0);
        assertTrue(rampResult[0][0] < 0, "The gradient of a ramp in the corner"
            + " should be negative.  It was actually " + rampResult[0][0]
            + ".");
        assertTrue(rampResult[0][1] < 0, "The gradient of a ramp on the side "
            + "up the ramp should be negative.  It was actually "
            + rampResult[0][1] + ".");
        assertTrue(rampResult[1][0] < 0, "The gradient of a ramp on the side "
            + "at the bottom of the ramp should be negative.  It was actually "
            + rampResult[1][0] + ".");
        assertTrue(rampResult[1][1] < 0, "The gradient of a ramp in the middle"
            + " of the ramp should be negative.  It was actually "
            + rampResult[1][1] + ".");
        assertTrue(rampResult[0][1] < rampResult[0][0], "The gradient of a "
            + "ramp on the side partway up should be less than at the bottom.  "
            + "It was actually " + rampResult[0][1] + " at the side and "
            + rampResult[0][0] + " at the bottom.");
        assertTrue(rampResult[0][1] < rampResult[0][2], "The gradient of a "
            + "ramp on the side partway up should be less than at the top.  It"
            + " was actually " + rampResult[0][1] + " at the side and "
            + rampResult[0][2] + " at the top.");
        assertTrue(rampResult[1][1] < rampResult[1][0], "The gradient of a "
            + "ramp in the middle partway up should be less than at the bottom."
            + "  It was actually " + rampResult[1][1] + " in the middle and "
            + rampResult[1][0] + " at the bottom.");
        assertTrue(rampResult[1][1] < rampResult[1][2], "The gradient of a "
            + "ramp in the middle partway up should be less than at the top.  "
            + "It was actually " + rampResult[1][1] + " in the middle and "
            + rampResult[1][2] + " at the top.");
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                assertTrue(bigRampResult[x][y] < rampResult[x][y],
                    "A steeper ramp should have a lower gradient, at coords ["
                    + x + "][" + y + "].  The steeper slope was actually "
                    + bigRampResult[x][y] + " and the gentler slope was "
                    + "actually" + rampResult[x][y] + ".");
            }
        }
    }
    @Test public void testCrazy()
    {
        float[][] crazy = {
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        float[][] crazyResult = M_quality.calculate(crazy, 0, 0);
        assertTrue(crazyResult[1][1] < crazyResult[0][0], "The crazy gradient"
            + " should be less in the middle than on the corner.  The actual "
            + "value for the middle was " + crazyResult[1][1] + " and the "
            + "actual value for the corner was " + crazyResult[0][0] + ".");
        assertTrue(crazyResult[1][1] < crazyResult[1][0], "The crazy gradient"
            + " should be less in the middle than on the edge.  The actual "
            + "value for the middle was " + crazyResult[1][1] + " and the "
            + "actual value for the edge was " + crazyResult[1][0] + ".");
    }
    private static Quality getQuality()
    {
        Quality result = new PhaseGradientQuality();
        result.setPhaseValue(10);
        return result;
    }
    private Quality M_quality = getQuality();
}
