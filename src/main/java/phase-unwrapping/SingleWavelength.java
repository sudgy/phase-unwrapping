package edu.pdx.phase_unwrapping;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.Point;
import java.util.TreeSet;
import java.util.HashSet;

public class SingleWavelength {
    private static class QPoint implements Comparable<QPoint> {
        public Point p = new Point();
        public float value;
        public Point p_from;
        @Override
        public int compareTo(QPoint other) {
            if (value < other.value) return -1;
            else if (value > other.value) return 1;
            // This is needed to make it consistent with equals, which is required for TreeSet to work correctly
            else if (p.equals(other.p)) return 0;
            else return -1;
        }
        @Override
        public boolean equals(Object other)
        {
            if (this == other) return true;
            if (other == null) return false;
            if (getClass() != other.getClass()) return false;
            QPoint q = (QPoint)other;
            return p == q.p && value == q.value;
        }
    }

    private float[][] M_original;
    private int M_length;
    private int M_height;
    private float[][] M_result;
    private TreeSet<QPoint> M_outside_points;
    private HashSet<Point> M_done_points;
    private QPoint[][] M_quality;
    private float M_phase;
    private boolean M_debug;

    public SingleWavelength(float[][] original, Quality quality, boolean debug, float phase)
    {
        M_phase = phase;
        M_original = original;
        M_length = M_original.length;
        M_height = M_original[0].length;
        M_result = new float[M_length][M_height];
        M_outside_points = new TreeSet<QPoint>();
        M_done_points = new HashSet<Point>();
        M_quality = process_quality(quality);
        M_debug = debug;
    }

    public void calculate()
    {
        Point current_point = new Point(M_length / 2, M_height / 2);
        M_result[current_point.x][current_point.y] = M_original[current_point.x][current_point.y];
        M_done_points.add(current_point);
        add_outside_points(current_point);

        ImagePlus timp = null;
        if (M_debug) timp = new ImagePlus("Temp");
        while (M_done_points.size() < M_length * M_height) {
            QPoint new_point = M_outside_points.pollLast();
            if (M_done_points.size() % 50000 == 0) {
                IJ.showProgress(M_done_points.size(), M_length * M_height);
                if (M_debug) {
                    timp.setProcessor(new FloatProcessor(M_result));
                    timp.show();
                }
            }
            assert new_point != null;
            current_point = new_point.p;
            Point from_point = new_point.p_from;
            assert from_point != null;
            float current_val = M_original[current_point.x][current_point.y];
            float from_val = M_result[from_point.x][from_point.y];
            if (current_val != from_val) {
                from_val -= current_val;
                from_val /= M_phase;
                from_val = Math.round(from_val);
                from_val *= M_phase;
                current_val += from_val;
            }
            M_result[current_point.x][current_point.y] = current_val;
            M_done_points.add(current_point);
            add_outside_points(current_point);
        }
    }
    private QPoint[][] process_quality(Quality q)
    {
        QPoint[][] result = new QPoint[M_original.length][M_original[0].length];
        for (int x = 0; x < result.length; ++x) {
            for (int y = 0; y < result[0].length; ++y) {
                result[x][y] = new QPoint();
                result[x][y].p.x = x;
                result[x][y].p.y = y;
                if (q == null) result[x][y].value = 0;
                else result[x][y].value = q.get_result()[x][y];
            }
        }
        return result;
    }
    public float[][] get_result()
    {
        return M_result;
    }
    private void add_outside_points(Point p)
    {
        Point new_point = new Point();
        if (p.x > 0) {
            new_point.x = p.x - 1;
            new_point.y = p.y;
            maybe_add_point(new_point, p);
        }
        if (p.y > 0) {
            new_point.x = p.x;
            new_point.y = p.y - 1;
            maybe_add_point(new_point, p);
        }
        if (p.x < M_length - 1) {
            new_point.x = p.x + 1;
            new_point.y = p.y;
            maybe_add_point(new_point, p);
        }
        if (p.y < M_height - 1) {
            new_point.x = p.x;
            new_point.y = p.y + 1;
            maybe_add_point(new_point, p);
        }
    }
    private void maybe_add_point(Point p, Point p_from)
    {
        if (!M_done_points.contains(p)) {
            QPoint qp = M_quality[p.x][p.y];
            if (qp.p_from == null) {
                qp.p_from = p_from;
                M_outside_points.add(qp);
            }
            else {
                QPoint old_qp_from = M_quality[qp.p_from.x][qp.p_from.y];
                QPoint new_qp_from = M_quality[p_from.x][p_from.y];
                if (new_qp_from.value > old_qp_from.value) {
                    qp.p_from = p_from;
                }
            }
        }
    }
}
