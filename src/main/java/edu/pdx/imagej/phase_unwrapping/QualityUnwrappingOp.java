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
    @Parameter private float[][] P_phaseImage;
    @Parameter private Quality   P_quality;
    @Parameter private boolean   P_showProgress;
    @Parameter private float     P_phaseValue;
    // Outputs
    @Parameter(type = ItemIO.OUTPUT) float[][] P_result;

    // QPoint is a simple data structure that represents each point of the
    // quality.  It implements Comparable so that they can be easily sorted to
    // find the pixel with the maximum quality.
    private static class QPoint implements Comparable<QPoint> {
        public Point p = new Point(); // The actual point value of this QPoint
        public float value; // The quality value for this pixel
        public Point pFrom; // The already-unwrapped pixel orthogonally
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
    private TreeSet<QPoint> M_outsidePoints = new TreeSet<QPoint>();
    // Points that have already been unwrapped
    private HashSet<Point> M_donePoints = new HashSet<Point>();
    private QPoint[][] M_quality;

    @Override
    public void run()
    {
        M_width = P_phaseImage.length;
        M_height = P_phaseImage[0].length;
        P_result = new float[M_width][M_height];
        processQuality();

        Point currentPoint = new Point(M_width / 2, M_height / 2);
        setValue(P_phaseImage[currentPoint.x][currentPoint.y],
                  currentPoint);

        ImagePlus steps = null;
        if (P_showProgress) steps = new ImagePlus("Partial Result");

        mainLoop(steps, currentPoint);

        if (steps != null) {
            steps.changes = false;
            steps.close();
        }
    }
    private void setValue(float value, Point point)
    {
        P_result[point.x][point.y] = value;
        M_donePoints.add(point);
        addOutsidePoints(point);
    }
    // The main loop does these things, in this order:
    //  - Show progress if needed
    //  - Get point of highest quality
    //  - Unwrap that pixel
    //  - Set the pixel value and update state
    private void mainLoop(ImagePlus steps, Point currentPoint)
    {
        while (M_donePoints.size() < M_width * M_height) {
            // Show progress if needed
            if (M_donePoints.size() % 50000 == 0) {
                P_status.showProgress(M_donePoints.size(), M_width*M_height);
                if (P_showProgress) {
                    steps.setProcessor(new FloatProcessor(P_result));
                    steps.show();
                }
            }
            // Get point of highest quality
            QPoint newPoint = M_outsidePoints.pollLast();
            assert newPoint != null;
            // Unwrap that pixel
            currentPoint = newPoint.p;
            Point fromPoint = newPoint.pFrom;
            assert fromPoint != null;
            float currentVal = P_phaseImage[currentPoint.x][currentPoint.y];
            float fromVal = P_result[fromPoint.x][fromPoint.y];
            if (currentVal != fromVal) {
                fromVal -= currentVal;
                fromVal /= P_phaseValue;
                fromVal = Math.round(fromVal);
                fromVal *= P_phaseValue;
                currentVal += fromVal;
            }
            // Set the pixel value and update state
            setValue(currentVal, currentPoint);
        }
    }
    // Set the values of M_quality based on P_quality
    private void processQuality()
    {
        M_quality = new QPoint[P_phaseImage.length][P_phaseImage[0].length];
        float[][] quality = P_quality.getResult();
        for (int x = 0; x < M_quality.length; ++x) {
            for (int y = 0; y < M_quality[0].length; ++y) {
                M_quality[x][y] = new QPoint();
                M_quality[x][y].p.x = x;
                M_quality[x][y].p.y = y;
                M_quality[x][y].value = quality[x][y];
            }
        }
    }
    // Add all of the points orthogonally adjacent to p to M_outsidePoints
    // This function just determines which points need to be added, and then
    // passes them to maybeAddPoint
    private void addOutsidePoints(Point p)
    {
        Point newPoint = new Point();
        if (p.x > 0) {
            newPoint.x = p.x - 1;
            newPoint.y = p.y;
            maybeAddPoint(newPoint, p);
        }
        if (p.y > 0) {
            newPoint.x = p.x;
            newPoint.y = p.y - 1;
            maybeAddPoint(newPoint, p);
        }
        if (p.x < M_width - 1) {
            newPoint.x = p.x + 1;
            newPoint.y = p.y;
            maybeAddPoint(newPoint, p);
        }
        if (p.y < M_height - 1) {
            newPoint.x = p.x;
            newPoint.y = p.y + 1;
            maybeAddPoint(newPoint, p);
        }
    }
    // Add this point, unless it has already been added.
    private void maybeAddPoint(Point p, Point pFrom)
    {
        // If it hasn't already been unwrapped
        if (!M_donePoints.contains(p)) {
            QPoint qp = M_quality[p.x][p.y];
            // If it hasn't been added already
            if (qp.pFrom == null) {
                qp.pFrom = pFrom;
                M_outsidePoints.add(qp);
            }
            // If it has been added already, figure out which from point has the
            // best quality.
            else {
                QPoint oldQpFrom = M_quality[qp.pFrom.x][qp.pFrom.y];
                QPoint newQpFrom = M_quality[pFrom.x][pFrom.y];
                if (newQpFrom.value > oldQpFrom.value) {
                    qp.pFrom = pFrom;
                }
            }
        }
    }
}
