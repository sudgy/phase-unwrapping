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

public class GradientQualityTest {
    @Test public void test_nothing()
    {
        float[][] nothing = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        float[][] nothing_result = M_quality.calculate_with(nothing);
        assertEquals(nothing_result[0][0], 0,
            "The gradient of a bunch of zeros in the corner should be zero.");
        assertEquals(nothing_result[1][0], 0,
            "The gradient of a bunch of zeros in the side should be zero.");
        assertEquals(nothing_result[1][1], 0,
            "The gradient of a bunch of zeros in the middle should be zero.");
    }
    @Test public void test_all()
    {
        float[][] all = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        float[][] all_result = M_quality.calculate_with(all);
        assertEquals(all_result[0][0], 0,
            "The gradient of a bunch of ones in the corner should be zero.");
        assertEquals(all_result[1][0], 0,
            "The gradient of a bunch of ones in the side should be zero.");
        assertEquals(all_result[1][1], 0,
            "The gradient of a bunch of ones in the middle should be zero.");
    }
    @Test public void test_ramp()
    {
        float[][] ramp = {
            {0, 1, 2},
            {0, 1, 2},
            {0, 1, 2}
        };
        float[][] big_ramp = {
            {0, 2, 4},
            {0, 2, 4},
            {0, 2, 4}
        };
        float[][] ramp_result     = M_quality.calculate_with(ramp);
        float[][] big_ramp_result = M_quality.calculate_with(big_ramp);
        assertTrue(ramp_result[0][0] < 0, "The gradient of a ramp in the corner"
            + " should be negative.  It was actually " + ramp_result[0][0]
            + ".");
        assertTrue(ramp_result[0][1] < 0, "The gradient of a ramp on the side "
            + "up the ramp should be negative.  It was actually "
            + ramp_result[0][1] + ".");
        assertTrue(ramp_result[1][0] < 0, "The gradient of a ramp on the side "
            + "at the bottom of the ramp should be negative.  It was actually "
            + ramp_result[1][0] + ".");
        assertTrue(ramp_result[1][1] < 0, "The gradient of a ramp in the middle"
            + " of the ramp should be negative.  It was actually "
            + ramp_result[1][1] + ".");
        assertTrue(ramp_result[0][1] < ramp_result[0][0], "The gradient of a "
            + "ramp on the side partway up should be less than at the bottom.  "
            + "It was actually " + ramp_result[0][1] + " at the side and "
            + ramp_result[0][0] + " at the bottom.");
        assertTrue(ramp_result[0][1] < ramp_result[0][2], "The gradient of a "
            + "ramp on the side partway up should be less than at the top.  It"
            + " was actually " + ramp_result[0][1] + " at the side and "
            + ramp_result[0][2] + " at the top.");
        assertTrue(ramp_result[1][1] < ramp_result[1][0], "The gradient of a "
            + "ramp in the middle partway up should be less than at the bottom."
            + "  It was actually " + ramp_result[1][1] + " in the middle and "
            + ramp_result[1][0] + " at the bottom.");
        assertTrue(ramp_result[1][1] < ramp_result[1][2], "The gradient of a "
            + "ramp in the middle partway up should be less than at the top.  "
            + "It was actually " + ramp_result[1][1] + " in the middle and "
            + ramp_result[1][2] + " at the top.");
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                assertTrue(big_ramp_result[x][y] < ramp_result[x][y],
                    "A steeper ramp should have a lower gradient, at coords ["
                    + x + "][" + y + "].  The steeper slope was actually "
                    + big_ramp_result[x][y] + " and the gentler slope was "
                    + "actually" + ramp_result[x][y] + ".");
            }
        }
    }
    @Test public void test_crazy()
    {
        float[][] crazy = {
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        float[][] crazy_result = M_quality.calculate_with(crazy);
        assertTrue(crazy_result[1][1] < crazy_result[0][0], "The crazy gradient"
            + " should be less in the middle than on the corner.  The actual "
            + "value for the middle was " + crazy_result[1][1] + " and the "
            + "actual value for the corner was " + crazy_result[0][0] + ".");
        assertTrue(crazy_result[1][1] < crazy_result[1][0], "The crazy gradient"
            + " should be less in the middle than on the edge.  The actual "
            + "value for the middle was " + crazy_result[1][1] + " and the "
            + "actual value for the edge was " + crazy_result[1][0] + ".");
    }

    private GradientQuality M_quality = new GradientQuality();
}
