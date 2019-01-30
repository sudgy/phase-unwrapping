package edu.pdx.array_ops;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public final class ArrayOps {
    public static float[][] binary(final float[][] a1, final float[][] a2, BinaryOperator<Float> op)
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
