/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.io.Serializable;
import java.util.Map;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.IServiceId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.search.ServiceSearchCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IServiceMethodsExecutor
{
    public SearchResult<Service> listServices(String sessionToken, ServiceSearchCriteria searchCriteria, ServiceFetchOptions fetchOptions);
    
    public Serializable executeService(String sessionToken, IServiceId serviceId, Map<String, Serializable> parameters);

}
