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

/**
 * Interface to specify an experiment to be created. 
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IExperiment extends IExperimentImmutable
{
    /**
     * Set the value for a property.
     * 
     * @throws IllegalArgumentException if no property for code <code>propertyCode</code> is found.
     */
    void setPropertyValue(String propertyCode, String propertyValue);

    /**
     * Set the experiment type for this experiment.
     */
    void setType(String experimentType);
}
