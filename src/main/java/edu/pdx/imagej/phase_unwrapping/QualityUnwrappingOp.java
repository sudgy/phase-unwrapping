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

/** QualityUnwrappingOp is an Op that calculates single wavelength phase
 * unwrapping using a quality-guided unwrapping algorithm.
 * <p>
 * To run this op yourself, it has the name "Quality Guided Phase Unwrapping"
 * and has these parameters:
 * <ol>
 *     <li>Phase image: a <code>float[][]</code> representing the current phase
 *                      image.
 *     <li>Quality: A {@link Quality} <em>that has already been calculated</em>.
 *     <li>Show progress: Whether or not you want to watch the progress of the
 *                        algorithm.
 *     <li>Phase value: The pixel phase value for the phase image.
 * </ol>
 * The result is a <code>float[][]</code> representing the unwrapped phase
 * image.
 * <p>
 * The gist of this algorithm is that it starts with a single pixel (arbitrarily
 * picked to be the center) and finds the adjacent pixel that has the highest
 * quality value, and unwraps that one.  It then finds the pixel adjacent to all
 * currently unwrapped pixels with the highest quality value, and unwraps that
 * one.  This process continues until all pixels are unwrapped.
 */
@Plugin(type = Op.class, name = "Quality Guided Phase Unwrapping")
public class QualityUnwrappingOp extends AbstractOp {
    @Parameter private StatusService P_status;
    // Inputs
    @Parameter private float[][] P_phase_image;
    @Parameter private Quality   P_quality;
    @Parameter private boolean   P_show_progress;
    @Parameter private float     P_phase_value;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) float[][] P_result;

    // QPoint is a simple data structure that represents each point of the
    // quality.  It implements Comparable so that they can be easily sorted to
    // find the pixel with the maximum quality.
    private static class QPoint implements Comparable<QPoint> {
        public Point p = new Point(); // The actual point value of this QPoint
        public float value; // The quality value for this pixel
        public Point p_from; // The already-unwrapped pixel orthogonally
                             // adjacent to this one that has the highest
                             // quality.  It will be null until a pixel next to
                             // it has been unwrapped.
        // Compares the quality value, mainly.
        @Override
        public int compareTo(QPoint other) {
            if (value < other.value) return -1;
            else if (value > other.value) return 1;
            // This is needed to make it consistent with equals, which is
            // required for TreeSet to work correctly
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

    private int M_width;
    private int M_height;
    // Points on the border of what has been unwrapped, that will be unwrapped
    // next
    private TreeSet<QPoint> M_outside_points = new TreeSet<QPoint>();
    // Points that have already been unwrapped
    private HashSet<Point> M_done_points = new HashSet<Point>();
    private QPoint[][] M_quality;

    @Override
    public void run()
    {
        M_width = P_phase_image.length;
        M_height = P_phase_image[0].length;
        P_result = new float[M_width][M_height];
        process_quality();

        Point current_point = new Point(M_width / 2, M_height / 2);
        set_value(P_phase_image[current_point.x][current_point.y],
                  current_point);

        ImagePlus steps = null;
        if (P_show_progress) steps = new ImagePlus("Partial Result");

        main_loop(steps, current_point);

        if (steps != null) {
            steps.changes = false;
            steps.close();
        }
    }
    private void set_value(float value, Point point)
    {
        P_result[point.x][point.y] = value;
        M_done_points.add(point);
        add_outside_points(point);
    }
    // The main loop does these things, in this order:
    //  - Show progress if needed
    //  - Get point of highest quality
    //  - Unwrap that pixel
    //  - Set the pixel value and update state
    private void main_loop(ImagePlus steps, Point current_point)
    {
        while (M_done_points.size() < M_width * M_height) {
            // Show progress if needed
            if (M_done_points.size() % 50000 == 0) {
                P_status.showProgress(M_done_points.size(), M_width*M_height);
                if (P_show_progress) {
                    steps.setProcessor(new FloatProcessor(P_result));
                    steps.show();
                }
            }
            // Get point of highest quality
            QPoint new_point = M_outside_points.pollLast();
            assert new_point != null;
            // Unwrap that pixel
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
            // Set the pixel value and update state
            set_value(current_val, current_point);
        }
    }
    // Set the values of M_quality based on P_quality
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
    // Add all of the points orthogonally adjacent to p to M_outside_points
    // This function just determines which points need to be added, and then
    // passes them to maybe_add_point
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
        if (p.x < M_width - 1) {
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
    // Add this point, unless it has already been added.
    private void maybe_add_point(Point p, Point p_from)
    {
        // If it hasn't already been unwrapped
        if (!M_done_points.contains(p)) {
            QPoint qp = M_quality[p.x][p.y];
            // If it hasn't been added already
            if (qp.p_from == null) {
                qp.p_from = p_from;
                M_outside_points.add(qp);
            }
            // If it has been added already, figure out which from point has the
            // best quality.
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
