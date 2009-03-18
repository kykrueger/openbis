/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A generic sample <i>Business Object</i>.
 * 
 * @author Christian Ribeaud
 */
public interface ISampleBO
{

    /**
     * Loads a sample given by its identifier.
     * 
     * @throws UserFailureException if no sample found.
     */
    void loadBySampleIdentifier(final SampleIdentifier identifier) throws UserFailureException;

    /**
     * Tries to load the sample with specified identifier.
     */
    void tryToLoadBySampleIdentifier(SampleIdentifier identifier);

    /**
     * Returns the loaded or defined sample or <code>null</code>.
     */
    SamplePE tryToGetSample();

    /** Returns the sample which has been loaded. */
    SamplePE getSample() throws IllegalStateException;

    /**
     * Defines a new sample. After invocation of this method {@link ISampleBO#save()} should be
     * invoked to store the new sample in the Data Access Layer.
     * 
     * @throws UserFailureException if specified sample type code is not a valid one.
     */
    void define(final NewSample newSample) throws UserFailureException;

    /**
     * Writes changed are added data to the Data Access Layers.
     */
    public void save() throws UserFailureException;

    /**
     * Connects sample with given procedure.
     */
    void addProcedure(ProcedurePE procedure);

    /**
     * Enriches the loaded sample with a valid {@link ProcedurePE}.
     */
    void enrichWithValidProcedure();

    /**
     * Changes given sample. Currently allowed changes: properties and experiment to which the
     * sample is connected.
     */
    void edit(SampleIdentifier identifier, List<SampleProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNul, Date version);
}
