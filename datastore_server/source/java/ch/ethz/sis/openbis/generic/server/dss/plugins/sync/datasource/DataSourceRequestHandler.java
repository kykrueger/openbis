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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.IRequestHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSourceQueryService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
public class DataSourceRequestHandler implements IRequestHandler
{
    private enum Capability
    {
        ABOUT("about", "description", null, false),
        CAPABILITY_LIST("capabilitylist", ABOUT, false),
        RESOURCE_LIST("resourcelist", CAPABILITY_LIST, true)
        {
            @Override
            void writeUrls(XMLStreamWriter writer, DeliveryContext context, IDataSourceQueryService queryService, IDeliverer deliverer,
                    Map<String, List<String>> parameterMap, String sessionToken, Date requestTimestamp) throws XMLStreamException
            {
                SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
                SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
                List<Space> spaces = context.getV3api().searchSpaces(sessionToken, searchCriteria, fetchOptions).getObjects();
                List<String> spaceCodes = spaces.stream().map(Space::getCode).collect(Collectors.toList());
                Set<String> requestedSpaces = DataSourceUtils.getRequestedAndAllowedSubSet(spaceCodes,
                        parameterMap.get("white_list"), parameterMap.get("black_list"));
                deliverer.deliverEntities(writer, queryService, sessionToken, requestedSpaces, requestTimestamp);
            }
        };

        private String name;

        private Capability up;

        private Capability down;

        private String capabilityAttribute;

        private boolean withAt;

        static
        {
            ABOUT.down = CAPABILITY_LIST;
            CAPABILITY_LIST.down = RESOURCE_LIST;
        }

        private Capability(String name, Capability up, boolean withAt)
        {
            this(name, name, up, withAt);
        }

        private Capability(String name, String capabilityAttribute, Capability up, boolean withAt)
        {
            this.name = name;
            this.capabilityAttribute = capabilityAttribute;
            this.up = up;
            this.withAt = withAt;
        }

        boolean matchVerb(Set<String> verbs)
        {
            return verbs.contains(asVerb());
        }

        String asVerb()
        {
            return name + ".xml";
        }

        void write(XMLStreamWriter writer, DeliveryContext context, IDataSourceQueryService queryService, IDeliverer deliverer,
                Map<String, List<String>> parameterMap, String sessionToken, Date requestTimestamp) throws XMLStreamException
        {
            writer.writeStartElement("rs:ln");
            String verb = up == null ? asVerb() : up.asVerb();
            writer.writeAttribute("href", createDownloadUrl(context, verb));
            writer.writeAttribute("rel", up == null ? "describedby" : "up");
            writer.writeEndElement();
            writer.writeStartElement("rs:md");
            if (withAt)
            {
                writer.writeAttribute("at", DataSourceUtils.convertToW3CDate(requestTimestamp));
            }
            writer.writeAttribute("capability", capabilityAttribute);
            writer.writeEndElement();
            writeUrls(writer, context, queryService, deliverer, parameterMap, sessionToken, requestTimestamp);
        }

        void writeUrls(XMLStreamWriter writer, DeliveryContext context, IDataSourceQueryService queryService, IDeliverer deliverer,
                Map<String, List<String>> parameterMap, String sessionToken, Date requestTimestamp) throws XMLStreamException
        {
            writer.writeStartElement("url");
            writer.writeStartElement("loc");
            writer.writeCharacters(createDownloadUrl(context, down.asVerb()));
            writer.writeEndElement();
            writer.writeStartElement("rs:md");
            writer.writeAttribute("capability", down.name);
            writer.writeEndElement();
        }

        private String createDownloadUrl(DeliveryContext context, String verb)
        {
            return context.getDownloadUrl() + context.getServletPath() + "/?verb=" + verb;
        }

    }

    private IDeliverer deliverer;

    private DeliveryContext deliveryContext;

    @Override
    public void init(Properties properties)
    {
        deliveryContext = new DeliveryContext();
        deliveryContext.setServletPath(new File(PropertyUtils.getMandatoryProperty(properties, "path")).getParent());
        deliveryContext.setServerUrl(PropertyUtils.getMandatoryProperty(properties, "server-url"));
        deliveryContext.setDownloadUrl(PropertyUtils.getMandatoryProperty(properties, "download-url"));
        deliveryContext.setV3api(ServiceProvider.getV3ApplicationService());
        deliveryContext.setContentProvider(ServiceProvider.getHierarchicalContentProvider());
        deliveryContext.setOpenBisDataSourceName(properties.getProperty("openbis-data-source-name", "openbis-db"));
        Deliverers deliverers = new Deliverers();
        deliverers.addDeliverer(new MasterDataDeliverer(deliveryContext));
        deliverers.addDeliverer(new MaterialDeliverer(deliveryContext));
        deliverers.addDeliverer(new SpaceDeliverer(deliveryContext));
        deliverers.addDeliverer(new ProjectDeliverer(deliveryContext));
        deliverers.addDeliverer(new ExperimentDeliverer(deliveryContext));
        deliverers.addDeliverer(new SampleDeliverer(deliveryContext));
        deliverers.addDeliverer(new DataSetDeliverer(deliveryContext));
        deliverer = deliverers;
    }

    @Override
    public void handle(SessionContextDTO session, HttpServletRequest request, HttpServletResponse response)
    {
        DataSourceQueryService queryService = new DataSourceQueryService();
        try
        {
            Map<String, List<String>> parameterMap = getParameterMap(request);
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
            Date requestTimestamp = getRequestTimestamp(queryService);
            Set<String> verbs = new HashSet<>(parameterMap.get("verb"));
            Capability capability = findMatchingCapability(verbs);
            capability.write(writer, deliveryContext, queryService, deliverer, parameterMap, sessionToken, requestTimestamp);
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            queryService.release();
        }
    }

    private Capability findMatchingCapability(Set<String> verbs)
    {
        if (verbs != null)
        {
            for (Capability capability : Capability.values())
            {
                if (capability.matchVerb(verbs))
                {
                    return capability;
                }
            }
        }
        return Capability.ABOUT;
    }

    private Date getRequestTimestamp(IDataSourceQueryService queryService)
    {
        Date requestTimestamp = new Date();
        String query = "select xact_start FROM pg_stat_activity WHERE xact_start IS NOT NULL ORDER BY xact_start ASC LIMIT 1";
        for (Map<String, Object> map : queryService.select(deliveryContext.getOpenBisDataSourceName(), query))
        {
            requestTimestamp = (Date) map.get("xact_start");
        }
        return requestTimestamp;
    }

    private Map<String, List<String>> getParameterMap(HttpServletRequest request)
    {
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, List<String>> parameterMap = new HashMap<>();
        while (enumeration.hasMoreElements())
        {
            String parameter = enumeration.nextElement();
            parameterMap.put(parameter, Arrays.asList(request.getParameterValues(parameter)));
        }
        return parameterMap;
    }

}
