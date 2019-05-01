
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
import org.scijava.app.StatusService;
import net.imagej.ops.OpService;

public class QualityUnwrappingStackOpTest {
    @Test public void test_single_frame()
    {
        OpService ops = M_context.getService(OpService.class);

        ImageStack stack = new ImageStack(1, 1);
        stack.addSlice(new FloatProcessor(new float[][]{{0}}));
        stack.addSlice(new FloatProcessor(new float[][]{{1}}));
        ImagePlus imp = new ImagePlus("", stack);
        imp.setPosition(2);

        Quality quality = new NoneQuality();

        ImagePlus single_result = (ImagePlus)ops.run(
            "Quality Guided Phase Unwrapping",
            imp, quality, false, 1, true, M_type);
        ImagePlus all_result = (ImagePlus)ops.run(
            "Quality Guided Phase Unwrapping",
            imp, quality, false, 1, false, M_type);
        assertEquals(single_result.getImageStackSize(), 1, "Using single frame "
            + "should make the image stack size one.");
        assertEquals(all_result.getImageStackSize(), 2, "Not using single frame"
            + " should make the image stack size more than one, when the image "
            + "has more than one frame.");

        FloatProcessor single_processor =
            (FloatProcessor)single_result.getProcessor();
        assertEquals(single_processor.getPixelValue(0, 0), 1, "Using single "
            + "frame should use the current frame.");
    }
    @Test public void test_stack()
    {
        OpService ops = M_context.getService(OpService.class);

        ImageStack stack = new ImageStack(1, 1);
        for (int i = 0; i < 4; ++i) {
            stack.addSlice(new FloatProcessor(new float[][]{{i}}));
        }
        ImagePlus imp = new ImagePlus("", stack);
        imp.setDimensions(1, 2, 2);

        Quality quality = new NoneQuality();

        ImagePlus result = (ImagePlus)ops.run(
            "Quality Guided Phase Unwrapping",
            imp, quality, false, 9, false, M_type);
        stack = result.getImageStack();
        for (int i = 0; i < 4; ++i) {
            FloatProcessor processor = (FloatProcessor)stack.getProcessor(i+1);
            assertEquals(processor.getPixelValue(0, 0), i, "Unwrapping a stack "
                + "should keep each slice the same from the original to the "
                + "result.");
        }
    }
    @Test public void test_quality_same_size()
    {
        OpService ops = M_context.getService(OpService.class);

        ImageStack stack = new ImageStack(1, 1);
        for (int i = 0; i < 4; ++i) {
            stack.addSlice(new FloatProcessor(new float[][]{{i}}));
        }
        ImagePlus imp = new ImagePlus("", stack);
        imp.setDimensions(1, 2, 2);

        TestDimensionQuality quality = new TestDimensionQuality(2, 2);
        ops.run("Quality Guided Phase Unwrapping",
                imp, quality, false, 1, false, M_type);
        assertEquals(quality.get_min_t(), 1, "A quality with the same size as "
            + "the image should have seen all slices (min t).");
        assertEquals(quality.get_max_t(), 2, "A quality with the same size as "
            + "the image should have seen all slices (max t).");
        assertEquals(quality.get_min_z(), 1, "A quality with the same size as "
            + "the image should have seen all slices (min z).");
        assertEquals(quality.get_max_z(), 2, "A quality with the same size as "
            + "the image should have seen all slices (max z).");
    }
    @Test public void test_quality_not_same_size()
    {
        OpService ops = M_context.getService(OpService.class);

        ImageStack stack = new ImageStack(1, 1);
        for (int i = 0; i < 4; ++i) {
            stack.addSlice(new FloatProcessor(new float[][]{{i}}));
        }
        ImagePlus imp = new ImagePlus("", stack);
        imp.setDimensions(1, 2, 2);

        TestDimensionQuality quality = new TestDimensionQuality(3, 3);
        ops.run("Quality Guided Phase Unwrapping",
                imp, quality, false, 1, false, M_type);
        assertEquals(quality.get_min_t(), 1, "A quality with a different size "
            + "as the image should have only seen one slice (min t).");
        assertEquals(quality.get_max_t(), 1, "A quality with a different size "
            + "as the image should have only seen one slice (max t).");
        assertEquals(quality.get_min_z(), 1, "A quality with a different size "
            + "as the image should have only seen one slice (min z).");
        assertEquals(quality.get_max_z(), 1, "A quality with a different size "
            + "as the image should have only seen one slice (max z).");
    }
    @Test public void test_quality_some_same_size()
    {
        OpService ops = M_context.getService(OpService.class);

        ImageStack stack = new ImageStack(1, 1);
        for (int i = 0; i < 4; ++i) {
            stack.addSlice(new FloatProcessor(new float[][]{{i}}));
        }
        ImagePlus imp = new ImagePlus("", stack);
        imp.setDimensions(1, 2, 2);

        TestDimensionQuality quality = new TestDimensionQuality(2, 3);
        ops.run("Quality Guided Phase Unwrapping",
                imp, quality, false, 1, false, M_type);
        assertEquals(quality.get_min_t(), 1, "A quality with a different z size"
            + " but the same t size as the image should see all t slices "
            + "(min).");
        assertEquals(quality.get_max_t(), 2, "A quality with a different z size"
            + " but the same t size as the image should see all t slices "
            + "(max).");
        assertEquals(quality.get_min_z(), 1, "A quality with a different z size"
            + " but the same t size as the image should see only one z slice "
            + "(min).");
        assertEquals(quality.get_max_z(), 1, "A quality with a different z size"
            + " but the same t size as the image should see only one z slice "
            + "(max).");
    }
    private Context M_context = new Context(OpService.class,
                                            StatusService.class);
    static private final QualityUnwrappingStackOp.OutputType M_type =
        QualityUnwrappingStackOp.OutputType.Type32Bit;
}

class TestDimensionQuality extends AbstractQuality {
    public TestDimensionQuality(int ts, int zs)
    {
        M_ts = ts;
        M_zs = zs;
    }
    @Override
    public float[][] calculate(float[][] phase_image, int t, int z)
    {
        M_result = phase_image;
        if (t < M_min_t) M_min_t = t;
        if (t > M_max_t) M_max_t = t;
        if (z < M_min_z) M_min_z = z;
        if (z > M_max_z) M_max_z = z;
        return M_result;
    }
    @Override public float[][] get_result() {return M_result;}
    @Override public int get_ts() {return M_ts;}
    @Override public int get_zs() {return M_zs;}

    public int get_min_t() {return M_min_t;}
    public int get_max_t() {return M_max_t;}
    public int get_min_z() {return M_min_z;}
    public int get_max_z() {return M_max_z;}

    private float[][] M_result;
    private int M_ts;
    private int M_zs;
    private int M_min_t = Integer.MAX_VALUE;
    private int M_max_t = Integer.MIN_VALUE;
    private int M_min_z = Integer.MAX_VALUE;
    private int M_max_z = Integer.MIN_VALUE;
}
