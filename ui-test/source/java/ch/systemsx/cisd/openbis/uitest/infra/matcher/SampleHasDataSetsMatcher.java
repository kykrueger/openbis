/*
 * Copyright 2012 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.uitest.infra.matcher;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class SampleHasDataSetsMatcher extends TypeSafeMatcher<Sample>
{

    @SuppressWarnings("unused")
    private GuiApplicationRunner openbis;

    private List<DataSet> datasets;

    public SampleHasDataSetsMatcher(GuiApplicationRunner openbis, DataSet... data)
    {
        this.openbis = openbis;
        this.datasets = new ArrayList<DataSet>();
        for (DataSet d : data)
        {
            datasets.add(d);
        }

    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("A Sample containing data sets " + datasets);
    }

    @Override
    public boolean matchesSafely(Sample item)
    {
        return false;
    }
}
