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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;

/**
 * Exposes the screening openBIS API through HTTP
 * 
 * @author Tomasz Pylak
 */
@Controller
public class ScreeningApiServiceServer extends AbstractApiServiceExporter
{
    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVER)
    private IScreeningApiServer server;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IScreeningApiServer.class, server, IScreeningApiServer.SERVICE_NAME,
                IScreeningApiServer.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
    { IScreeningApiServer.SERVICE_URL, "/openbis" + IScreeningApiServer.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
