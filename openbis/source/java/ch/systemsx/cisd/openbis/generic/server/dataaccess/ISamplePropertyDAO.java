/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * An interface that contains all data access operations on {@link SamplePropertyPE}s.
 * 
 * @author Izabela Adamczyk
 */
public interface ISamplePropertyDAO
{
    /**
     * Returns {@link SamplePropertyPE}s for given list of {@link SampleIdentifier}s and list of
     * property types.
     */
    public Map<SampleIdentifier, List<SamplePropertyPE>> listSampleProperties(
            List<SampleIdentifier> samples, List<PropertyTypePE> propertyCodes)
            throws DataAccessException;

}