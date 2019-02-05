/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IRequestHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
public class DataSourceRequestHandler implements IRequestHandler
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSourceRequestHandler.class);

    private File tempDir;

    private String serverUrl;

    private String downloadUrl;

    private String servletPath;

    private DataSourceQueryService queryService;

    private IApplicationServerApi v3api;

    private IHierarchicalContentProvider contentProvider;

    private AbstractEntityDeliverer<DataSet> dataSetDeliverer;

    private ExperimentDeliverer experimentDeliverer;

    private MaterialDeliverer materialDeliverer;

    private SampleDeliverer sampleDeliverer;

    private ProjectDeliverer projectDeliverer;

    private MasterDataDeliverer masterDataDeliverer;

    @Override
    public void init(Properties properties)
    {
        DeliveryContext deliveryContext = new DeliveryContext();
        servletPath = new File(PropertyUtils.getMandatoryProperty(properties, "path")).getParent();
        deliveryContext.setServletPath(servletPath);
        tempDir = new File(PropertyUtils.getMandatoryProperty(properties, "temp-dir"));
        serverUrl = PropertyUtils.getMandatoryProperty(properties, "server-url");
        deliveryContext.setServerUrl(serverUrl);
        downloadUrl = PropertyUtils.getMandatoryProperty(properties, "download-url");
        deliveryContext.setDownloadUrl(downloadUrl);
        queryService = new DataSourceQueryService();
        v3api = ServiceProvider.getV3ApplicationService();
        deliveryContext.setV3api(v3api);
        contentProvider = ServiceProvider.getHierarchicalContentProvider();
        deliveryContext.setContentProvider(contentProvider);
        dataSetDeliverer = new DataSetDeliverer(deliveryContext);
        experimentDeliverer = new ExperimentDeliverer(deliveryContext);
        materialDeliverer = new MaterialDeliverer(deliveryContext);
        sampleDeliverer = new SampleDeliverer(deliveryContext);
        projectDeliverer = new ProjectDeliverer(deliveryContext);
        String openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        masterDataDeliverer = new MasterDataDeliverer(deliveryContext);
    }

    @Override
    public void handle(SessionContextDTO session, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            Map<String, Set<String>> parameterMap = getParameterMap(request);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(response.getWriter());
            writer.writeStartDocument();
            writer.writeStartElement("urlset");
            writer.writeAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
            writer.writeAttribute("xmlns:rs", "http://www.openarchives.org/rs/terms/");
            writer.writeAttribute("xmlns:xmd", "https://sis.id.ethz.ch/software/#openbis/xmdterms/");
            writer.writeAttribute("xmlns:x", "https://sis.id.ethz.ch/software/#openbis/xdterms/");
            writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("xsi:schemaLocation",
                    "https://sis.id.ethz.ch/software/#openbis/xdterms/ ./xml/xdterms.xsd https://sis.id.ethz.ch/software/#openbis/xmdterms/");
            String sessionToken = session.getSessionToken();
            Set<String> verbs = parameterMap.get("verb");
            if (verbs.contains("resourcelist.xml"))
            {
                deliverResourceList(parameterMap, sessionToken, writer);
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    private void deliverResourceList(Map<String, Set<String>> parameterMap, String sessionToken, XMLStreamWriter writer) throws XMLStreamException
    {

        writer.writeStartElement("rs:ln");
        writer.writeAttribute("href", downloadUrl + servletPath + "/?verb=capabilitylist.xml");
        writer.writeAttribute("rel", "up");
        writer.writeEndElement();
        writer.writeStartElement("rs:md");
        Date requestTimestamp = getRequestTimestamp();
        writer.writeAttribute("at", DataSourceUtils.convertToW3CDate(requestTimestamp));
        writer.writeAttribute("capability", "resourceList");
        writer.writeEndElement();
        Set<String> ignoredSpaces = parameterMap.get("black_list");
        if (ignoredSpaces == null)
        {
            ignoredSpaces = Collections.emptySet();
        }
        Set<String> requestedSpaces = new TreeSet<>();
        List<Space> spaces = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();
        for (Space space : spaces)
        {
            if (ignoredSpaces.contains(space.getCode()) == false)
            {
                requestedSpaces.add(space.getCode());
            }
        }
        dataSetDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);
        experimentDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);
        masterDataDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);;
        materialDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);
        projectDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);
        sampleDeliverer.deliverEntities(writer, sessionToken, requestedSpaces, requestTimestamp);
    }

    protected Date getRequestTimestamp()
    {
        Date requestTimestamp = new Date();
        String query = "select xact_start FROM pg_stat_activity WHERE xact_start IS NOT NULL ORDER BY xact_start ASC LIMIT 1";
        for (Map<String, Object> map : queryService.select("openbis-db", query))
        {
            requestTimestamp = (Date) map.get("xact_start");
        }
        return requestTimestamp;
    }

    private Map<String, Set<String>> getParameterMap(HttpServletRequest request)
    {
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, Set<String>> parameterMap = new HashMap<>();
        while (enumeration.hasMoreElements())
        {
            String parameter = enumeration.nextElement();
            parameterMap.put(parameter, new HashSet<>(Arrays.asList(request.getParameterValues(parameter))));
        }
        return parameterMap;
    }

}
