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

import org.scijava.Prioritized;
import org.scijava.plugin.Plugin;
import net.imagej.ImageJPlugin;

import edu.pdx.imagej.dynamic_parameters.DParameter;

public interface Quality extends ImageJPlugin, Prioritized {
    default DParameter param() {return null;}

    default void set_phase_value(float phase_value) {}
    float[][]    calculate(float[][] phase_image, int t, int z);
    float[][]    get_result();
    default int  get_ts() {return 1;}
    default int  get_zs() {return 1;}
}
