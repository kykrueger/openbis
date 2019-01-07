/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier.TYPE_SEPARATOR_PREFIX;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier.TYPE_SEPARATOR_SUFFIX;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.DefaultNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Ganime Betul Akin
 */
public class ResourceListParser
{
    private final ResourceListParserData data;

    private final INameTranslator nameTranslator;

    private final String dataStoreCode;

    public INameTranslator getNameTranslator()
    {
        return nameTranslator;
    }

    private ResourceListParser(INameTranslator nameTranslator, String dataStoreCode)
    {
        this.data = new ResourceListParserData();
        this.nameTranslator = nameTranslator;
        this.dataStoreCode = dataStoreCode;
    }

    public static ResourceListParser create(INameTranslator nameTranslator, String dataStoreCode)
    {
        if (nameTranslator == null)
        {
            return create(dataStoreCode);
        }
        return new ResourceListParser(nameTranslator, dataStoreCode);
    }

    private static ResourceListParser create(String dataStoreCode)
    {
        return create(new DefaultNameTranslator(), dataStoreCode);
    }

    public ResourceListParserData parseResourceListDocument(Document doc, Monitor monitor) throws XPathExpressionException
    {
        XPath xpath = createXPath();
        Date resourceListTimestamp = getResourceListTimestamp(doc, xpath);
        data.setResourceListTimestamp(resourceListTimestamp);

        monitor.log();
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            Map<String, List<Node>> childrenByType = getChildrenByType(node);
            List<Node> locNodes = childrenByType.get("loc");
            if (locNodes == null)
            {
                continue;
            }
            String uri = locNodes.get(0).getTextContent();
            if (uri.endsWith("MASTER_DATA/MASTER_DATA/M"))
            {
                parseMasterData(doc, xpath, uri);
            }
        }
        for (int i = 0, n = nodes.getLength(); i < n; i++)
        {
            Node node = nodes.item(i);
            Map<String, List<Node>> childrenByType = getChildrenByType(node);
            List<Node> locNodes = childrenByType.get("loc");
            if (locNodes == null)
            {
                continue;
            }
            String uri = locNodes.get(0).getTextContent();
            if (uri.endsWith("MASTER_DATA/MASTER_DATA/M") == false && uri.endsWith("/M"))
            {
                List<Node> lastmodNodes = childrenByType.get("lastmod");
                if (lastmodNodes == null)
                {
                    continue;
                }
                String lastModDataStr = lastmodNodes.get(0).getTextContent().trim();
                try
                {
                    Date lastModificationDate = convertFromW3CDate(lastModDataStr);
                    List<Node> xdNodes = childrenByType.get("x:xd");
                    if (xdNodes == null)
                    {
                        continue;
                    }
                    if ((i + 1) % 10000 == 0)
                    {
                        monitor.log(String.format("%7d/%d uri: %s", i + 1, n, uri));
                    }
                    parseMetaData(uri, lastModificationDate, xdNodes.get(0));
                } catch (ParseException e)
                {
                    throw new XPathExpressionException("Last modification date cannot be parsed:" + lastModDataStr);
                }
            }
        }

        return data;
    }

    private XPath createXPath()
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext()
            {
                public String getNamespaceURI(String prefix)
                {
                    if (prefix == null)
                        throw new NullPointerException("Null prefix");
                    else if ("s".equals(prefix))
                        return "http://www.sitemaps.org/schemas/sitemap/0.9";
                    else if ("rs".equals(prefix))
                        return "http://www.openarchives.org/rs/terms/";
                    else if ("x".equals(prefix))
                        return "https://sis.id.ethz.ch/software/#openbis/xdterms/";
                    else if ("xml".equals(prefix))
                        return XMLConstants.XML_NS_URI;
                    return XMLConstants.NULL_NS_URI;
                }

                // This method isn't necessary for XPath processing.
                public String getPrefix(String uri)
                {
                    throw new UnsupportedOperationException("Not implemented!!!");
                }

                // This method isn't necessary for XPath processing either.
                public Iterator<?> getPrefixes(String uri)
                {
                    throw new UnsupportedOperationException("Not implemented!!!");
                }
            });
        return xpath;
    }

    private Date getResourceListTimestamp(Document doc, XPath xpath) throws XPathExpressionException
    {
        XPathExpression expr = xpath.compile("*[name() = 'urlset']/*[name() = 'rs:md']");
        Node mdNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
        String timestamp = mdNode.getAttributes().getNamedItem("at").getTextContent();
        try
        {
            return convertFromW3CDate(timestamp);
        } catch (ParseException e)
        {
            throw new XPathExpressionException("Last modification date cannot be parsed:" + timestamp);
        }
    }

    private void parseMasterData(Document doc, XPath xpath, String uri) throws XPathExpressionException
    {
        MasterDataParser mdParser = MasterDataParser.create(nameTranslator);
        mdParser.parseMasterData(doc, xpath, uri);
        MasterData masterData = data.getMasterData();
        masterData.setFileFormatTypesToProcess(mdParser.getFileFormatTypes());
        masterData.setValidationPluginsToProcess(mdParser.getValidationPlugins());
        masterData.setVocabulariesToProcess(mdParser.getVocabularies());
        masterData.setPropertyTypesToProcess(mdParser.getPropertyTypes());
        masterData.setSampleTypesToProcess(mdParser.getSampleTypes());
        masterData.setDataSetTypesToProcess(mdParser.getDataSetTypes());
        masterData.setExperimentTypesToProcess(mdParser.getExperimentTypes());
        masterData.setMaterialTypesToProcess(mdParser.getMaterialTypes());
        masterData.setPropertyAssignmentsToProcess(mdParser.getEntityPropertyAssignments());
    }

    private void parseMetaData(String uri, Date lastModificationDate, Node xdNode) throws XPathExpressionException
    {
        String entityKind = xdNode.getAttributes().getNamedItem("kind").getTextContent();

        if (SyncEntityKind.PROJECT.toString().equals(entityKind))
        {
            parseProjectMetaData(extractPermIdFromURI(uri), xdNode, lastModificationDate);
        } else if (SyncEntityKind.EXPERIMENT.toString().equals(entityKind))
        {
            parseExperimentMetaData(extractPermIdFromURI(uri), xdNode, lastModificationDate);
        } else if (SyncEntityKind.SAMPLE.toString().equals(entityKind))
        {
            parseSampleMetaData(extractPermIdFromURI(uri), xdNode, lastModificationDate);
        } else if (SyncEntityKind.DATA_SET.toString().equals(entityKind))
        {
            parseDataSetMetaData(extractDataSetCodeFromURI(uri), xdNode, lastModificationDate);
        } else if (SyncEntityKind.MATERIAL.toString().equals(entityKind))
        {
            parseMaterialMetaData(extractMaterialCodeFromURI(uri), xdNode, lastModificationDate);
        }
    }

    private Map<String, List<Node>> getChildrenByType(Node node)
    {
        Map<String, List<Node>> result = new TreeMap<>();
        if (node.hasChildNodes())
        {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0, n = childNodes.getLength(); i < n; i++)
            {
                Node childNode = childNodes.item(i);
                String nodeName = childNode.getNodeName().toLowerCase();
                List<Node> list = result.get(nodeName);
                if (list == null)
                {
                    list = new ArrayList<>();
                    result.put(nodeName, list);
                }
                list.add(childNode);
            }
        }
        return result;
    }

    private Date convertFromW3CDate(String lastModDataStr) throws ParseException
    {
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df1.parse(lastModDataStr);
    }

    private void parseDataSetMetaData(String permId, Node xdNode, Date lastModificationDate)
    {
        String code = extractCode(xdNode);
        String sampleIdentifier = extractAttribute(xdNode, "sample", true);
        String experimentIdentifier = extractAttribute(xdNode, "experiment", true);
        String type = extractType(xdNode);
        String dsKind = extractAttribute(xdNode, "dsKind");
        NewExternalData ds = null;
        if (dsKind.equals(DataSetKind.CONTAINER.toString()))
        {
            ds = new NewContainerDataSet();
            ds.setDataSetKind(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind.CONTAINER);
        } else if (dsKind.equals(DataSetKind.PHYSICAL.toString()))
        {
            ds = new NewExternalData();
            ds.setDataSetKind(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind.PHYSICAL);
        } else
        {
            throw new IllegalArgumentException(dsKind + " data sets are currently not supported");
        }
        ds.setCode(code);
        ds.setDataSetType(new DataSetType(type));
        ds.setDataStoreCode(this.dataStoreCode);

        ds.setSampleIdentifierOrNull(getSampleIdentifier(sampleIdentifier));
        ds.setExperimentIdentifierOrNull(getExperimentIdentifier(experimentIdentifier));

        IncomingDataSet incomingDataSet = new IncomingDataSet(ds, lastModificationDate);
        data.getDataSetsToProcess().put(permId, incomingDataSet);
        incomingDataSet.setConnections(parseConnections(xdNode));
        ds.setDataSetProperties(parseDataSetProperties(xdNode));
    }

    private String extractAttribute(Node xdNode, String attrName, boolean nullAllowed)
    {
        String val = xdNode.getAttributes().getNamedItem(attrName).getTextContent();
        if (StringUtils.isBlank(val) == true)
        {
            if (nullAllowed == false)
            {
                throw new IllegalArgumentException(attrName + " cannot be empty in Resource List");
            } else
            {
                return null;
            }
        } else
        {
            return val.trim();
        }
    }

    private String extractAttribute(Node xdNode, String attrName) throws IllegalArgumentException
    {
        return extractAttribute(xdNode, attrName, false);
    }

    private String extractCode(Node xdNode)
    {
        return extractAttribute(xdNode, "code");
    }

    private SampleIdentifier getSampleIdentifier(String sampleIdentifierStr)
    {
        if (sampleIdentifierStr == null)
        {
            return null;
        }
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sampleIdentifierStr);
        SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
        String originalSpaceCode = spaceLevel.getSpaceCode();
        SpaceIdentifier translatedSpaceIdentifier = new SpaceIdentifier(nameTranslator.translate(originalSpaceCode));

        if (sampleIdentifier.isProjectLevel())
        {
            return new SampleIdentifier(new ProjectIdentifier(translatedSpaceIdentifier, sampleIdentifier.getProjectLevel().getProjectCode()),
                    sampleIdentifier.getSampleCode());
        } else
        {
            return new SampleIdentifier(translatedSpaceIdentifier, sampleIdentifier.getSampleCode());
        }
    }

    private ExperimentIdentifier getExperimentIdentifier(String experimentIdentifierStr)
    {
        if (experimentIdentifierStr == null)
        {
            return null;
        }
        ExperimentIdentifier experimentIdentifier = ExperimentIdentifierFactory.parse(experimentIdentifierStr);
        String originalSpaceCode = experimentIdentifier.getSpaceCode();
        String projectCode = experimentIdentifier.getProjectCode();
        String expCode = experimentIdentifier.getExperimentCode();
        return new ExperimentIdentifier(new ProjectIdentifier(new SpaceIdentifier(nameTranslator.translate(originalSpaceCode)), projectCode),
                expCode);
    }

    private void parseProjectMetaData(String permId, Node xdNode, Date lastModificationDate)
    {

        String code = extractCode(xdNode);
        String desc = xdNode.getAttributes().getNamedItem("desc").getTextContent();
        String space = extractSpace(xdNode, false);
        ProjectIdentifier projectIdentifier = createProjectIdentifier(code, space);
        NewProject newProject = new NewProject(projectIdentifier.toString(), desc);
        newProject.setPermID(permId);
        IncomingProject incomingProject = new IncomingProject(newProject, lastModificationDate);
        data.getProjectsToProcess().put(permId, incomingProject);
        incomingProject.setConnections(parseConnections(xdNode));
        incomingProject.setHasAttachments(hasAttachments(xdNode));
    }

    private ExperimentIdentifier createExperimentIdentifier(String spaceId, String prjCode, String expCode)
    {
        return new ExperimentIdentifier(createProjectIdentifier(prjCode, spaceId), expCode);
    }

    private ProjectIdentifier createProjectIdentifier(String code, String space)
    {
        return new ProjectIdentifier(createSpaceIdentifier(space), code);
    }

    private SampleIdentifier createSampleIdentifier(String code, String project, String space)
    {
        if (space == null)
        {
            return new SampleIdentifier(nameTranslator.translate(code));
        } else if (project == null)
        {
            SpaceIdentifier spaceIdentifier = createSpaceIdentifier(space);
            return new SampleIdentifier(spaceIdentifier, code);
        } else
        {
            ProjectIdentifier projectIdentifier = createProjectIdentifier(project, space);
            return new SampleIdentifier(projectIdentifier, code);
        }
    }

    private SpaceIdentifier createSpaceIdentifier(String space)
    {
        assert space != null : "Space id cannot be null";
        String translatedSpaceId = nameTranslator.translate(space);
        data.getHarvesterSpaceList().add(translatedSpaceId);
        return new SpaceIdentifier(translatedSpaceId);
    }

    private void parseMaterialMetaData(String permId, Node xdNode, Date lastModificationDate)
    {
        String code = nameTranslator.translate(extractCode(xdNode));
        String type = extractType(xdNode);
        NewMaterialWithType newMaterial = new NewMaterialWithType(code, type);
        MaterialWithLastModificationDate materialWithLastModDate =
                new MaterialWithLastModificationDate(newMaterial, lastModificationDate);
        data.getMaterialsToProcess().put(code, type, materialWithLastModDate);
        newMaterial.setProperties(parseProperties(xdNode));
    }

    private List<Connection> parseConnections(Node xdNode)
    {
        List<Connection> conns = new ArrayList<Connection>();
        Element docElement = (Element) xdNode;
        NodeList connsNode = docElement.getElementsByTagName("x:connections");
        if (connsNode.getLength() == 1)
        {
            Element connsElement = (Element) connsNode.item(0);
            NodeList connNodes = connsElement.getElementsByTagName("x:connection");
            for (int i = 0; i < connNodes.getLength(); i++)
            {
                String to = connNodes.item(i).getAttributes().getNamedItem("to").getTextContent();
                String type = connNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
                conns.add(new Connection(to, type));
            }
        }
        return conns;
    }

    private boolean hasAttachments(Node xdNode)
    {
        Element docElement = (Element) xdNode;
        NodeList connsNode = docElement.getElementsByTagName("x:binaryData");
        // if a sample/experiment/project node has binaryData element, it can only be because of attachments
        return connsNode.getLength() == 1;
    }

    private List<NewProperty> parseDataSetProperties(Node xdNode)
    {
        EntityProperty[] entityProperties = parseProperties(xdNode);
        List<NewProperty> dsProperties = new ArrayList<NewProperty>();
        for (EntityProperty entityProperty : entityProperties)
        {
            dsProperties.add(new NewProperty(entityProperty.getPropertyType().getCode(), entityProperty.getValue()));
        }
        return dsProperties;
    }

    private EntityProperty[] parseProperties(Node xdNode)
    {

        List<EntityProperty> entityProperties = new ArrayList<EntityProperty>();
        Element docElement = (Element) xdNode;
        NodeList propsNode = docElement.getElementsByTagName("x:properties");
        if (propsNode.getLength() == 1)
        {
            Element propsElement = (Element) propsNode.item(0);
            NodeList propertyNodes = propsElement.getElementsByTagName("x:property");
            for (int i = 0; i < propertyNodes.getLength(); i++)
            {
                Element propertyElement = (Element) propertyNodes.item(i);
                // TODO proper error handling needed below in case the XML is not correct and item 0 does not exist
                String code = propertyElement.getElementsByTagName("x:code").item(0).getTextContent();
                String val = propertyElement.getElementsByTagName("x:value").item(0).getTextContent();
                EntityProperty property = createEntityProperty(code, val);
                entityProperties.add(property);
            }
        }
        return entityProperties.toArray(new EntityProperty[entityProperties.size()]);
    }

    private EntityProperty createEntityProperty(String code, String val)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        String translatedCode = nameTranslator.translate(code);
        PropertyType pt = data.getMasterData().getPropertyTypesToProcess().get(translatedCode);
        if (pt.getDataType().getCode().equals(DataTypeCode.MATERIAL))
        {
            if (val != null)
            {
                val = nameTranslator.translate(val);
                val = translateMaterialIdentifier(val);
            }

        }
        propertyType.setCode(translatedCode);
        property.setPropertyType(propertyType);
        property.setValue(val);
        return property;
    }

    private String translateMaterialIdentifier(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        int typePrefix = value.indexOf(TYPE_SEPARATOR_PREFIX);
        if (typePrefix == -1)
        {
            return null;
        }
        String code = value.substring(0, typePrefix);
        String typeCode = nameTranslator.translate(value.substring(typePrefix + TYPE_SEPARATOR_PREFIX.length()));
        // we allow to omit the closing brace
        if (typeCode.endsWith(TYPE_SEPARATOR_SUFFIX))
        {
            typeCode = typeCode.substring(0, typeCode.length() - TYPE_SEPARATOR_SUFFIX.length());
        }
        return new MaterialIdentifier(code, typeCode).toString();
    }

    private void parseExperimentMetaData(String permId, Node xdNode, Date lastModificationDate)
    {
        String code = extractCode(xdNode);
        String type = extractType(xdNode);
        String project = extractAttribute(xdNode, "project");
        String space = extractSpace(xdNode, false);
        ExperimentIdentifier experimentIdentifier = createExperimentIdentifier(space, project, code);
        NewExperiment newExp = new NewExperiment(experimentIdentifier.toString(), type);
        newExp.setPermID(permId);
        IncomingExperiment incomingExperiment = new IncomingExperiment(newExp, lastModificationDate);
        data.getExperimentsToProcess().put(permId, incomingExperiment);
        incomingExperiment.setConnections(parseConnections(xdNode));
        incomingExperiment.setHasAttachments(hasAttachments(xdNode));
        newExp.setProperties(parseProperties(xdNode));
    }

    private void parseSampleMetaData(String permId, Node xdNode, Date lastModificationDate)
    {
        String code = extractCode(xdNode);
        String type = extractType(xdNode);
        String experiment = extractAttribute(xdNode, "experiment", true);
        String project = extractAttribute(xdNode, "project", true);
        String space = extractSpace(xdNode, true);
        SampleType sampleType = new SampleType();
        sampleType.setCode(type);

        if (false == StringUtils.isBlank(project) && StringUtils.isBlank(space))
        {
            throw new IllegalArgumentException("Sample with perm id '" + permId + "' has 'project' attribute specified but 'space' attribute empty");
        }

        SampleIdentifier identifier = createSampleIdentifier(code, project, space);
        String expIdentifier = null;
        ExperimentIdentifier experimentIdentifier = getExperimentIdentifier(experiment);
        if (experimentIdentifier != null)
        {
            expIdentifier = experimentIdentifier.toString();
        }
        NewSample newSample = new NewSample(identifier.toString(), sampleType, null, null,
                expIdentifier, null, null, null,
                null);
        newSample.setPermID(permId);
        if (space == null)
        {
            newSample.setDefaultSpaceIdentifier(null);
        }
        if (space != null && project != null)
        {
            newSample.setProjectIdentifier(createProjectIdentifier(project, space).toString());
        }
        IncomingSample incomingSample = new IncomingSample(newSample, lastModificationDate);
        data.getSamplesToProcess().put(permId, incomingSample);
        incomingSample.setHasAttachments(hasAttachments(xdNode));
        incomingSample.setConnections(parseConnections(xdNode));
        newSample.setProperties(parseProperties(xdNode));
    }

    private String extractType(Node xdNode)
    {
        return nameTranslator.translate(extractAttribute(xdNode, "type"));
    }

    private String extractSpace(Node xdNode, boolean nullAllowed)
    {
        return extractAttribute(xdNode, "space", nullAllowed);
    }

    private String extractPermIdFromURI(String uri) throws XPathExpressionException
    {
        Pattern pattern = Pattern.compile("([0-9\\-]{17,})");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        throw new XPathExpressionException("Malformed resource url");
    }

    private String extractDataSetCodeFromURI(String uri) throws XPathExpressionException
    {
        Pattern pattern = Pattern.compile("(?<=DATA_SET\\/)(.*)(?=\\/M)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        throw new XPathExpressionException("Malformed resource url");
    }

    private String extractMaterialCodeFromURI(String uri) throws XPathExpressionException
    {
        // TODO malformed uri handling
        String[] parts = uri.split("/");
        return parts[parts.length - 2];
    }
}
// expr = xpath.compile("//s:loc[normalize-space(.) ='" + uri + "']/../x:xd/x:properties/x:property"); //
// "//*/text()[normalize-space(.)='" + loc_text + "']/parent::/rs:ln");// "/s:urlset/s:url[/s:loc/text() = '" +
// for (int ji = 0; ji < url_node.getLength(); ji++)
// {
// System.out.print(url_node.item(ji).getNodeName() + ":" + url_node.item(ji).getAttributes().getNamedItem("kind"));
// System.out.println();
// }
