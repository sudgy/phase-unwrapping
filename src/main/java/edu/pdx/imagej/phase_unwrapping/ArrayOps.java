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

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

final class ArrayOps {
    public static float[][] binary(final float[][] a1,
                                   final float[][] a2,
                                   BinaryOperator<Float> op)
    {
        assert a1.length == a2.length;
        assert a1[0].length == a2[0].length;
        // Hopefully the others are all equal as well
        float[][] result = new float[a1.length][a1[0].length];
        for (int x = 0; x < a1.length; ++x) {
            for (int y = 0; y < a1[0].length; ++y) {
                result[x][y] = op.apply(a1[x][y], a2[x][y]);
            }
        }
        return result;
    }
    public static float[][] unary(final float[][] a, UnaryOperator<Float> op)
    {
        float[][] result = new float[a.length][a[0].length];
        for (int x = 0; x < a.length; ++x) {
            for (int y = 0; y < a[0].length; ++y) {
                result[x][y] = op.apply(a[x][y]);
            }
        }
        return result;
    }
    public static BinaryOperator<Float> Add = (a, b) -> a + b;
    public static BinaryOperator<Float> Subtract = (a, b) -> a - b;
    public static BinaryOperator<Float> Multiply = (a, b) -> a * b;
    public static BinaryOperator<Float> Divide = (a, b) -> a / b;
    public static UnaryOperator<Float> AddBy(float val)
        {return a -> a + val;}
    public static UnaryOperator<Float> SubtractBy(float val)
        {return a -> a - val;}
    public static UnaryOperator<Float> MultiplyBy(float val)
        {return a -> a * val;}
    public static UnaryOperator<Float> DivideBy(float val)
        {return a -> a / val;}
}
