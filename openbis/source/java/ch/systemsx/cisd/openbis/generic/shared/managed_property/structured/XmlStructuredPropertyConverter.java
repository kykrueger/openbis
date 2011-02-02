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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.EntityLinkElementKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.util.XmlUtils;

/**
 * An implementation of {@link IStructuredPropertyConverter}, that translates elements to XML.
 * 
 * @author Kaloyan Enimanev
 */
public class XmlStructuredPropertyConverter implements IStructuredPropertyConverter
{

    private static final String ROOT_NAME = "root";

    private static final String OPENBIS_NS = "openbis";
    private static final String OPENBIS_NAMESPACE_URL = "http://openbis.org/schemas/elements/v1";

    private IElementFactory factory;

    public XmlStructuredPropertyConverter(IElementFactory factory)
    {
        this.factory = factory;
    }

    public IElement[] convertToElements(IManagedProperty property)
    {
        return property.isSpecialValue() ? new IElement[0]
                : convertStringToElements(property.getValue());
    }

    public IElement[] convertStringToElements(String propertyValue)
    {
        if (StringUtils.isBlank(propertyValue))
        {
            return new IElement[0];
        }

        Document document = XmlUtils.parseXmlDocument(propertyValue);
        IElement root = transformFromDOM(document.getDocumentElement());
        return root.getChildren();
    }

    public String convertToString(List<IElement> elements)
    {
        IElement root = createRootElement(elements);
        Document doc = createEmptyDocument();
        Node rootNode = transformToDOM(root, doc);
        doc.appendChild(rootNode);

        return XmlUtils.serializeDocument(doc);
    }

    private IElement createRootElement(List<IElement> elements)
    {
        IElement root = new Element(ROOT_NAME);
        root.addAttribute("xmlns:" + OPENBIS_NS, OPENBIS_NAMESPACE_URL);
        root.setChildren(elements);
        return root;
    }
    
    private Node transformToDOM(IElement element, Document document)
    {
        String name = getNodeNameWithNamespacePrefix(element);
        Node result = document.createElement(name);

        for (IElement child : element.getChildren())
        {
            Node childNode = transformToDOM(child, document);
            result.appendChild(childNode);
        }

        transformAttributesToDOM(element, document, result);
        transformDataToDOM(element, document, result);

        return result;
    }

    private String getNodeNameWithNamespacePrefix(IElement element)
    {
        String name = element.getName();
        if (factory.isEntityLink(element))
        {
            name = OPENBIS_NS + ":" + name;
        }
        return name;
    }

    private IElement transformFromDOM(Node node)
    {
        IElement result = createElementForNode(node);

        transformAttributesFromDOM(node, result);

        // children & text data
        NodeList domChildren = node.getChildNodes();
        List<IElement> children = new ArrayList<IElement>();
        if (domChildren != null)
        {
            for (int i = 0; i < domChildren.getLength(); i++)
            {
                Node domChild = domChildren.item(i);
                if (domChild.getNodeType() == Node.ELEMENT_NODE)
                {
                    IElement child = transformFromDOM(domChild);
                    children.add(child);
                } else if (domChild.getNodeType() == Node.CDATA_SECTION_NODE)
                {
                    String textContext = domChild.getNodeValue();
                    result.setData(textContext);
                }
            }

        }

        result.setChildren(children);
        return result;
    }

    private void transformAttributesFromDOM(Node node, IElement result)
    {
        NamedNodeMap domAttributes = node.getAttributes();
        Map<String, String> attributes = result.getAttributes();
        if (domAttributes != null)
        {
            for (int i = 0; i < domAttributes.getLength(); i++)
            {
                Attr domAttr = (Attr) domAttributes.item(i);
                String unescapedValue = StringEscapeUtils.unescapeXml(domAttr.getValue());
                attributes.put(domAttr.getName(), unescapedValue);
            }
        }
    }

    private IElement createElementForNode(Node node)
    {
        String nodeName = node.getLocalName();

        EntityLinkElementKind linkKind = EntityLinkElementKind.tryGetForElementName(nodeName);
        if (linkKind != null)
        {
            String permId = getAttrValueOrFail(node, EntityLinkElement.PERMID_ATTR_NAME);
            return new EntityLinkElement(linkKind, permId);
        } 

        // plain element, no special treatment needed
        return factory.createElement(nodeName);
    }

    private String getAttrValueOrFail(Node node, String attrName)
    {
        Attr attr = (Attr) node.getAttributes().getNamedItem(attrName);
        if (attr == null)
        {
            String error =
                    String.format("Attribute [%s] expected in nodes with name %s", attrName,
                            node.getNodeName());
            throw new IllegalArgumentException(error);
        }
        return attr.getValue();
    }

    private void transformAttributesToDOM(IElement element, Document document, Node node)
    {
        Map<String, String> attributes = element.getAttributes();
        for (String key : element.getAttributes().keySet())
        {
            Attr domAttr = document.createAttribute(key);
            String escapedValue = StringEscapeUtils.escapeXml(attributes.get(key));
            domAttr.setValue(escapedValue);
            node.getAttributes().setNamedItem(domAttr);
        }
    }

    private void transformDataToDOM(IElement element, Document document, Node node)
    {
        // text data
        if (StringUtils.isBlank(element.getData()) == false)
        {
            Node cdataNode = document.createCDATASection(element.getData());
            node.appendChild(cdataNode);
        }
    }

    private Document createEmptyDocument()
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document doc = parser.newDocument();
            return doc;
        } catch (ParserConfigurationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
