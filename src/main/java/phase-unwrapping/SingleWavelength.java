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

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.Point;
import java.util.TreeSet;
import java.util.HashSet;

@Plugin(type = Op.class, name = "Single Wavelength Phase Unwrapping")
public class SingleWavelength extends AbstractOp {
    @Parameter private StatusService P_status;
    // Inputs
    @Parameter private float[][] P_phase_image;
    @Parameter private Quality   P_quality;
    @Parameter private boolean   P_show_progress;
    @Parameter private float     P_phase_value;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) float[][] P_result;
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

    private int M_length;
    private int M_height;
    private TreeSet<QPoint> M_outside_points;
    private HashSet<Point> M_done_points;
    private QPoint[][] M_quality;

    @Override
    public void run()
    {
        M_length = P_phase_image.length;
        M_height = P_phase_image[0].length;
        P_result = new float[M_length][M_height];
        M_outside_points = new TreeSet<QPoint>();
        M_done_points = new HashSet<Point>();
        process_quality();

        Point current_point = new Point(M_length / 2, M_height / 2);
        P_result[current_point.x][current_point.y] = P_phase_image[current_point.x][current_point.y];
        M_done_points.add(current_point);
        add_outside_points(current_point);

        ImagePlus timp = null;
        if (P_show_progress) timp = new ImagePlus("Temp");
        while (M_done_points.size() < M_length * M_height) {
            QPoint new_point = M_outside_points.pollLast();
            if (M_done_points.size() % 50000 == 0) {
                P_status.showProgress(M_done_points.size(), M_length * M_height);
                if (P_show_progress) {
                    timp.setProcessor(new FloatProcessor(P_result));
                    timp.show();
                }
            }
            assert new_point != null;
            current_point = new_point.p;
            Point from_point = new_point.p_from;
            assert from_point != null;
            float current_val = P_phase_image[current_point.x][current_point.y];
            float from_val = P_result[from_point.x][from_point.y];
            if (current_val != from_val) {
                from_val -= current_val;
                from_val /= P_phase_value;
                from_val = Math.round(from_val);
                from_val *= P_phase_value;
                current_val += from_val;
            }
            P_result[current_point.x][current_point.y] = current_val;
            M_done_points.add(current_point);
            add_outside_points(current_point);
        }
    }
    private void process_quality()
    {
        M_quality = new QPoint[P_phase_image.length][P_phase_image[0].length];
        float[][] quality = P_quality.get_result();
        for (int x = 0; x < M_quality.length; ++x) {
            for (int y = 0; y < M_quality[0].length; ++y) {
                M_quality[x][y] = new QPoint();
                M_quality[x][y].p.x = x;
                M_quality[x][y].p.y = y;
                M_quality[x][y].value = quality[x][y];
            }
        }
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
