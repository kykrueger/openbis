/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface ISearchService
{
    /**
     * List all experiments for a given project.
     * 
     * @param projectIdentifier The project identifier as a string (e.g., /SPACE-CODE/PROJECT-CODE).
     * @return A list of experiments for the specified project.
     */
    public List<IExperimentImmutable> listExperiments(String projectIdentifier);

    /**
     * List all data sets with a given value for a particular property, optionally restricted to a
     * specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards.
     * @return A list of matching data sets.
     */
    public List<IDataSetImmutable> searchForDataSets(String property, String value, String typeOrNul);

    /**
     * List all samples with a given value for a particular property, optionally restricted to a
     * specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards.
     * @return A list of matching samples.
     */
    public List<ISampleImmutable> searchForSamples(String property, String value, String typeOrNull);

    /**
     * List all data sets that match the given searchCriteria.
     * 
     * @param searchCriteria The criteria to match against.
     * @return A list of matching data sets.
     */
    public List<IDataSetImmutable> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * List all samples that match the given searchCriteria.
     * 
     * @param searchCriteria The criteria to match against.
     * @return A list of matching samples.
     */
    public List<ISampleImmutable> searchForSamples(SearchCriteria searchCriteria);
}
