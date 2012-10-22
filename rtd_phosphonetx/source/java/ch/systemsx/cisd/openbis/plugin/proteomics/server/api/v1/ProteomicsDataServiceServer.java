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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.api.v1;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.IProteomicsDataService;

/**
 * Server wrapping {@link IProteomicsDataService}.
 * 
 * @author Franz-Josef Elmer
 */
@Controller
@RequestMapping(
    { IProteomicsDataService.SERVER_URL, "/openbis" + IProteomicsDataService.SERVER_URL })
public class ProteomicsDataServiceServer extends AbstractApiServiceExporter
{
    @Resource(name = Constants.PROTEOMICS_DATA_SERVICE)
    private IProteomicsDataService service;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IProteomicsDataService.class, service, IProteomicsDataService.SERVICE_NAME,
                IProteomicsDataService.SERVER_URL);
        super.afterPropertiesSet();
    }
}
