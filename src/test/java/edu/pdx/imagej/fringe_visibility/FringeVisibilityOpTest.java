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

package edu.pdx.imagej.fringe_visibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import org.scijava.Context;
import net.imagej.ops.OpService;

public class FringeVisibilityOpTest {
    @Test public void test_fringe_visibility()
    {
        Context context = new Context(OpService.class);
        OpService ops = context.getService(OpService.class);

        // This is just to make sure that the algorithm won't divide by zero or
        // something
        float[][] almost_nothing = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0},
            {1, 1, 1}
        };
        ops.run("Fringe Visibility", (Object)almost_nothing);

        float[][] nothing = {
            {0, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
        };
        float[][] all = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        float[][] some = {
            {1, 0, 1},
            {1, 0, 1},
            {1, 0, 1}
        };
        float[][] nothing_result = run(ops, nothing);
        float[][] all_result = run(ops, all);
        float[][] some_result = run(ops, some);
        assertEquals(nothing_result[1][1], 0,
            "The fringe visibility of nothing should be zero.");
        assertEquals(all_result[1][1], 0,
           "The fringe visibility when everything is the same should be zero.");
        assertTrue(some_result[1][1] > 0, "The fringe visibility of changing "
            + "things should be greater than zero.");

        // There should probably be more tests, but I honestly don't quite get
        // why the fringe visibility equations are the way they are, so I waon't
        // try to do anything else
    }
    private float[][] run(OpService ops, float[][] input)
    {
        return (float[][])ops.run("Fringe Visibility", (Object)input);
    }
}
