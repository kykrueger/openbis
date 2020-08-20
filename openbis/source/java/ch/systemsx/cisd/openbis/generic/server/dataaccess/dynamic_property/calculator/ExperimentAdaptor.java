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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * {@link IEntityAdaptor} implementation for {@link ExperimentPE}.
 * 
 * @author Piotr Buczek
 */
public class ExperimentAdaptor extends AbstractEntityAdaptor implements IExperimentAdaptor,
        INonAbstractEntityAdapter
{
    private final ExperimentPE experimentPE;

    private final Session session;

    public ExperimentAdaptor(ExperimentPE experimentPE, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        super(experimentPE, evaluator);
        this.session = session;
        this.experimentPE = experimentPE;
    }

    public ExperimentPE experimentPE()
    {
        return experimentPE;
    }

    @Override
    public ExperimentPE entityPE()
    {
        return experimentPE();
    }

    @Override
    public Iterable<ISampleAdaptor> samples()
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<ISampleAdaptor> samplesOfType(String typeRegexp)
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<IDataAdaptor> dataSets()
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<IDataAdaptor> dataSetsOfType(String typeRegexp)
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }
}
