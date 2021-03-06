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

package ch.systemsx.cisd.cina.client.util.v1;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * Interface for the facade for various CINA utilities
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface ICinaUtilities
{
    /**
     * Returns the session token.
     * 
     * @return The session token for an authenticated user.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     */
    public String getSessionToken() throws IllegalStateException;

    /**
     * Return a list of samples matching the provided criteria.
     * 
     * @param searchCriteria The criteria to search for
     * @return A list of samples that match the criteria.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Return a generated sample code for the provided sample type.
     * 
     * @param sampleTypeCode The sample type of the sample the generated identifier will belong to.
     * @return A sample code for the provided sample type.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public String generateSampleCode(String sampleTypeCode) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Return a list of experiments of the given type for which the user has write priveledges.
     * 
     * @param experimentType The type of experiment we want listed
     * @return A list of experiments for the given experiment type
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public List<Experiment> listVisibleExperiments(String experimentType)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Return a list of data sets for the sample specified by code.
     * 
     * @param samples The samples we want to retrieve data sets for
     * @return The data sets connected to the sample
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server or if there are multiple samples with the
     *             given code.
     * @throws UserFailureException Thrown if no sample exists with the specified code.
     */
    public List<DataSet> listDataSets(List<Sample> samples) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException;

    /**
     * Return
     * 
     * @param dataSetCode The code of the data set to download
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server or if there are multiple samples with the
     *             given code.
     * @throws UserFailureException Thrown if no sample exists with the specified code.
     */
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException;

    /**
     * Return a list of data sets for the sample specified by code.
     * 
     * @param sample The sample we want to retrieve data sets for
     * @param areOnlyDirectlyConnectedIncluded If false, data sets for children of the sample are included, o.w. only data sets directly connected to
     *            the sample are included.
     * @return The data sets connected to the sample
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server or if there are multiple samples with the
     *             given code.
     * @throws UserFailureException Thrown if no sample exists with the specified code.
     */
    public List<DataSet> listDataSetsForSample(Sample sample,
            boolean areOnlyDirectlyConnectedIncluded) throws IllegalStateException,
            EnvironmentFailureException, UserFailureException;

    /**
     * Logs the current user out.
     */
    public void logout();
}
