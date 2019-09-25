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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import org.scijava.plugin.Plugin;
import org.scijava.plugin.Parameter;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.dynamic_parameters.HoldingParameter;
import edu.pdx.imagej.dynamic_parameters.ChoiceParameter;

/** QualityParameter is a DParameter that gets any of the {@link Quality}
 * plugins that are found.
 */
@Plugin(type = DParameter.class)
public class QualityParameter extends HoldingParameter<Quality> {
    @Parameter private QualityService P_qualityService;

    /** Default constructor.  Doesn't really do anything. */
    public QualityParameter() {super("QualityBase");}
    /** Initialize this parameter.  This shouldn't be called by user code.  It
     * gets all of the qualities and determines what needs to be shown.
     */
    @Override
    public void initialize()
    {
        M_qualities = P_qualityService.getQualities();
        ArrayList<Entry<String, Quality>> qualitiesArray =
            new ArrayList<>(M_qualities.entrySet());
        Collections.sort(qualitiesArray,
            new Comparator<Entry<String, Quality>>() {
                @Override
                public int compare(Entry<String, Quality> lhs,
                                   Entry<String, Quality> rhs)
                {
                    return lhs.getValue().compareTo(rhs.getValue());
                }
            }
        );
        ArrayList<String> choicesList = new ArrayList<>();
        for (Entry<String, Quality> entry : qualitiesArray) {
            choicesList.add(entry.getKey());
        }
        String[] choices = new String[choicesList.size()];
        choices = choicesList.toArray(choices);
        M_choice = addParameter(new ChoiceParameter(
                                 "Quality", choices, choices[0]));
        for (HashMap.Entry<String, Quality> entry : M_qualities.entrySet()) {
            if (entry.getValue().param() != null) {
                M_parameters.put(entry.getKey(), entry.getValue().param());
                addParameter(entry.getValue().param());
            }
        }
        setVisibilities();
    }
    /** See DParameter's documentation. */
    @Override
    public void readFromDialog()
    {
        super.readFromDialog();
        setVisibilities();
    }
    /** See DParameter's documentation.
     *
     * @param c The class that is reading, usually the command that is being
     *          run.
     * @param name The name used for this parameter.
     */
    @Override
    public void readFromPrefs(Class<?> c, String name)
    {
        super.readFromPrefs(c, name);
        setVisibilities();
    }
    /** Get the quality this parameter is holding.
     *
     * @return The Quality that this parameter is currently holding.
     */
    @Override
    public Quality getValue()
    {
        return M_qualities.get(M_choice.getValue());
    }

    private void setVisibilities()
    {
        for (DParameter param : M_parameters.values()) {
            param.setNewVisibility(false);
        }
        DParameter current = M_parameters.get(M_choice.getValue());
        if (current != null) current.setNewVisibility(true);
    }

    private ChoiceParameter             M_choice;
    private HashMap<String, DParameter> M_parameters = new HashMap<>();
    private HashMap<String, Quality>    M_qualities;
}
