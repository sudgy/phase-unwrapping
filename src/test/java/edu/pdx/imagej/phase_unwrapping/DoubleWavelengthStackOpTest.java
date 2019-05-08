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
import ij.ImageStack;
import ij.process.FloatProcessor;

import org.scijava.Context;
import net.imagej.ops.OpService;

public class DoubleWavelengthStackOpTest {
    @Test public void test_correct_result_no_steps()
    {
        test_correct_result_impl(false);
    }
    @Test public void test_correct_result_steps()
    {
        test_correct_result_impl(true);
    }
    private void test_correct_result_impl(boolean show_steps)
    {
        // These are taken from DoubleWavelengthOpTest
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

        float[][][] array = run_single(image1, image2, show_steps);
        ImagePlus[] stack = run_stack(
            new ImagePlus("", new FloatProcessor(crazy1)),
            image1.wavelength, image1.phase_value,
            new ImagePlus("", new FloatProcessor(crazy2)),
            image2.wavelength, image2.phase_value,
            show_steps);
        assertEquals(array.length, stack.length, "Running "
            + "DoubleWavelengthStackOp should produce the same amount of "
            + "results as running DoubleWavelengthOp.");
        for (int i = 0; i < stack.length; ++i) {
            FloatProcessor processor = (FloatProcessor)stack[i].getProcessor();
            for (int x = 0; x < array[i].length; ++x) {
                for (int y = 0; y < array[i][x].length; ++y) {
                    assertEquals(array[i][x][y], processor.getPixelValue(x, y),
                        "Running DoubleWavelengthStackOp should produce the "
                        + "same values as running DoubleWavlengthOp at step "
                        + i + " at coordinates (" + x + ", " + y + ").");
                }
            }
        }
    }
    @Test public void test_inequal_sizes()
    {
        ImageStack stack1 = new ImageStack(1, 1);
        for (int i = 0; i < 4; ++i) {
            stack1.addSlice(new FloatProcessor(new float[][]{{i}}));
        }
        ImagePlus imp1 = new ImagePlus("", stack1);
        imp1.setDimensions(1, 2, 2);

        ImageStack stack2 = new ImageStack(1, 1);
        for (int i = 0; i < 9; ++i) {
            stack2.addSlice(new FloatProcessor(new float[][]{{i * 10}}));
        }
        ImagePlus imp2 = new ImagePlus("", stack2);
        imp2.setDimensions(1, 3, 3);

        ImagePlus[] stacks = run_stack(imp1, 5, 256, imp2, 6, 256, false);

        for (int t = 0; t < 2; ++t) {
            for (int z = 0; z < 2; ++z) {
                PhaseImage image1 = new PhaseImage();
                int current_slice1 = imp1.getStackIndex(1, z, t);
                image1.phase_image = imp1.getStack()
                                         .getProcessor(current_slice1)
                                         .getFloatArray();
                image1.wavelength = 5;
                image1.phase_value = 256;
                PhaseImage image2 = new PhaseImage();
                int current_slice2 = imp2.getStackIndex(1, z, t);
                image2.phase_image = imp2.getStack()
                                         .getProcessor(current_slice2)
                                         .getFloatArray();
                image2.wavelength = 6;
                image2.phase_value = 256;

                float[][][] arrays = run_single(image1, image2, false);

                test_inequal_sizes_internal(stacks, arrays, t, z);
            }
        }
    }
    private void test_inequal_sizes_internal(ImagePlus[] stacks,
                                             float[][][] arrays,
                                             int t, int z)
    {
        for (int i = 0; i < stacks.length; ++i) {
            for (int x = 0; x < arrays[i].length; ++x) {
                for (int y = 0; y < arrays[i][x].length; ++y) {
                    int current_slice = stacks[i].getStackIndex(1, z, t);
                    float[][] stack = stacks[i].getStack()
                                               .getProcessor(current_slice)
                                               .getFloatArray();
                    float[][] array = arrays[i];
                    assertEquals(stack[x][y], array[x][y],
                        "DoubleWavelengthStackOp should deal with inequal stack"
                        + " sizes correctly.  This is at t = " + t + ", z = "
                        + z + ", step " + i + ", at coordinates (" + x + ", "
                        + y + ").");
                }
            }
        }
    }
    private float[][][] run_single(PhaseImage image1,
                                   PhaseImage image2,
                                   boolean show_steps)
    {
        return (float[][][])M_ops.run(DoubleWavelengthOp.class,
                                      image1, image2, show_steps);
    }
    private ImagePlus[] run_stack(
        ImagePlus image1, float wavelength1, float value1,
        ImagePlus image2, float wavelength2, float value2,
        boolean show_steps)
    {
        return (ImagePlus[])M_ops.run(DoubleWavelengthStackOp.class,
                                      image1, wavelength1, value1,
                                      image2, wavelength2, value2,
                                      show_steps);
    }
    private Context M_context = new Context(OpService.class);
    private OpService M_ops = M_context.getService(OpService.class);
}
