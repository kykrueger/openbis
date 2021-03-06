/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class ExperimentLoaderTest extends AbstractLoaderTestCase
{
    @Test
    public void test()
    {
        ExperimentLoader loader =
                new ExperimentLoader(daoFactory, new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()));
        List<Sample> samples = loadSamples(980L, 981L, 986L);

        loader.enrichWithExperiments(session(), samples);

        StringBuilder builder = new StringBuilder();
        for (Sample sample : samples)
        {
            builder.append(sample.getId()).append(' ').append(sample.getCode());
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                builder.append(": ").append(experiment.getCode()).append(' ');
                builder.append(getSortedProperties(experiment));
            }
            builder.append('\n');
        }
        assertEquals("980 3V-126\n" + "981 DP\n"
                + "986 3VCP5: EXP10 [DESCRIPTION: A simple experiment, GENDER: MALE]\n",
                builder.toString());
    }

    private List<Sample> loadSamples(Long... ids)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        List<Sample> list = new ArrayList<Sample>();
        for (Long id : ids)
        {
            list.add(SampleTranslator.translate(sampleDAO.tryGetByTechId(new TechId(id)), "", null,
                    new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()), null));

        }
        return list;
    }
}
