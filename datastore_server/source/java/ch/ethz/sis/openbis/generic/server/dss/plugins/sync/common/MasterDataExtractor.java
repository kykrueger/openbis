/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author Ganime Betul Akin
 */
public class MasterDataExtractor
{
    private static final VocabularySearchCriteria VOCABULARY_SEARCH_CRITERIA = new VocabularySearchCriteria();

    private static final PropertyTypeSearchCriteria PROPERTY_TYPE_SEARCH_CRITERIA = new PropertyTypeSearchCriteria();

    private static final ExperimentTypeSearchCriteria EXPERIMENT_TYPE_SEARCH_CRITERIA = new ExperimentTypeSearchCriteria();

    private static final MaterialTypeSearchCriteria MATERIAL_TYPE_SEARCH_CRITERIA = new MaterialTypeSearchCriteria();

    private static final SampleTypeSearchCriteria SAMPLE_TYPE_SEARCH_CRITERIA = new SampleTypeSearchCriteria();

    private static final DataSetTypeSearchCriteria DATA_SET_TYPE_SEARCH_CRITERIA = new DataSetTypeSearchCriteria();

    private final IMasterDataRegistrationTransaction masterDataRegistrationTransaction;

    private final IApplicationServerApi v3Api;

    private final String sessionToken;

    /**
     * @param v3Api
     * @param sessionToken
     * @param masterDataRegistrationTransaction
     */
    public MasterDataExtractor(IApplicationServerApi v3Api, String sessionToken,
            IMasterDataRegistrationTransaction masterDataRegistrationTransaction)
    {
        this.v3Api = v3Api;
        this.sessionToken = sessionToken;
        this.masterDataRegistrationTransaction = masterDataRegistrationTransaction;
    }

    public String fetchAsXmlString() throws ParserConfigurationException, TransformerException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        docFactory.setNamespaceAware(true);
        Element rootElement = doc.createElementNS("https://sis.id.ethz.ch/software/#openbis/xmdterms/", "xmd:masterData");
        rootElement.setAttribute("xmlns:xmd", "https://sis.id.ethz.ch/software/#openbis/xmdterms/");
        doc.appendChild(rootElement);

        appendFileFormatTypes(doc, rootElement);

        appendValidationPlugins(doc, rootElement);
        appendVocabularies(doc, rootElement);

        appendPropertyTypes(doc, rootElement);
        appendSampleTypes(doc, rootElement);
        appendExperimentTypes(doc, rootElement);
        appendDataSetTypes(doc, rootElement);
        appendMaterialTypes(doc, rootElement);

        appendExternalDataManagementSystems(doc, rootElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                "validationPlugin");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    protected void appendExternalDataManagementSystems(Document doc, Element rootElement)
    {
        ExternalDmsSearchCriteria searchCriteria = new ExternalDmsSearchCriteria();
        ExternalDmsFetchOptions fetchOptions = new ExternalDmsFetchOptions();
        List<ExternalDms> externalDataManagementSystems =
                v3Api.searchExternalDataManagementSystems(sessionToken, searchCriteria, fetchOptions).getObjects();
        if (externalDataManagementSystems.isEmpty() == false)
        {
            Element externalDataManagementSystemsElement = doc.createElement("xmd:externalDataManagementSystems");
            rootElement.appendChild(externalDataManagementSystemsElement);
            for (ExternalDms externalDms : externalDataManagementSystems)
            {
                Element externalDmsElement = doc.createElement("xmd:externalDataManagementSystem");
                externalDmsElement.setAttribute("code", externalDms.getCode());
                externalDmsElement.setAttribute("label", externalDms.getLabel());
                externalDmsElement.setAttribute("address", externalDms.getAddress());
                externalDmsElement.setAttribute("addressType", externalDms.getAddressType().toString());
                externalDataManagementSystemsElement.appendChild(externalDmsElement);
            }
        }
    }

    private void appendValidationPlugins(Document doc, Element rootElement)
    {
        PluginFetchOptions fetchOptions = new PluginFetchOptions();
        fetchOptions.withScript();
        List<Plugin> plugins = v3Api.searchPlugins(sessionToken, new PluginSearchCriteria(), fetchOptions).getObjects();
        if (plugins.isEmpty())
        {
            return;
        }
        Element pluginsElement = doc.createElement("xmd:validationPlugins");
        rootElement.appendChild(pluginsElement);
        for (Plugin plugin : plugins)
        {
            Element pluginElement = doc.createElement("xmd:validationPlugin");
            pluginElement.setAttribute("name", plugin.getName());
            pluginElement.setAttribute("description", plugin.getDescription());
            pluginElement.setAttribute("type", plugin.getPluginType().toString());
            pluginElement.setAttribute("entityKind", getEntityKind(plugin));
            pluginElement.setAttribute("isAvailable", String.valueOf(plugin.isAvailable()));
            pluginElement.appendChild(doc.createCDATASection(plugin.getScript()));
            pluginsElement.appendChild(pluginElement);
        }
    }

    protected String getEntityKind(Plugin plugin)
    {
        String entityKind = "All";
        Set<EntityKind> entityKinds = plugin.getEntityKinds();
        if (entityKinds != null)
        {
            CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
            for (EntityKind kind : entityKinds)
            {
                builder.append(kind.toString());
            }
            entityKind = builder.toString();
        }
        return entityKind;
    }

    private void appendFileFormatTypes(Document doc, Element rootElement)
    {
        List<IFileFormatTypeImmutable> fileFormatTypes = masterDataRegistrationTransaction.listFileFormatTypes();
        if (fileFormatTypes.size() > 0)
        {
            Element fileFormatTypesElement = doc.createElement("xmd:fileFormatTypes");
            rootElement.appendChild(fileFormatTypesElement);
            for (IFileFormatTypeImmutable fileFormatType : fileFormatTypes)
            {
                Element fileFormatTypeElement = doc.createElement("xmd:fileFormatType");
                fileFormatTypeElement.setAttribute("code", fileFormatType.getCode());
                fileFormatTypeElement.setAttribute("description", fileFormatType.getDescription());
                fileFormatTypesElement.appendChild(fileFormatTypeElement);
            }
        }
    }

    private void appendPropertyTypes(Document doc, Element rootElement)
    {
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withMaterialType();
        fetchOptions.withVocabulary();
        List<PropertyType> propertyTypes = v3Api.searchPropertyTypes(sessionToken, PROPERTY_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (propertyTypes.isEmpty())
        {
            return;
        }
        Element propertyTypesElement = doc.createElement("xmd:propertyTypes");
        rootElement.appendChild(propertyTypesElement);
        for (PropertyType propertyType : propertyTypes)
        {
            Boolean internalNameSpace = propertyType.isInternalNameSpace();
            String code =
                    (internalNameSpace && propertyType.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX))
                            ? CodeConverter.tryToDatabase(propertyType.getCode())
                            : propertyType.getCode();
            Element typeElement = doc.createElement("xmd:propertyType");
            typeElement.setAttribute("code", code);
            typeElement.setAttribute("label", propertyType.getLabel());
            typeElement.setAttribute("dataType", propertyType.getDataType().name());
            typeElement.setAttribute("internalNamespace", String.valueOf(internalNameSpace));
            typeElement.setAttribute("managedInternally", String.valueOf(propertyType.isManagedInternally()));
            typeElement.setAttribute("description", propertyType.getDescription());
            if (propertyType.getDataType().name().equals(DataType.CONTROLLEDVOCABULARY.name()))
            {
                typeElement.setAttribute("vocabulary", propertyType.getVocabulary().getCode());
            } else if (propertyType.getDataType().name().equals(DataType.MATERIAL.name()))
            {
                if (propertyType.getMaterialType() != null)
                {
                    typeElement.setAttribute("material", propertyType.getMaterialType().getCode());
                } else
                {
                    // for properties like "inhibitor_of" where it is of Material of Any Type
                    typeElement.setAttribute("material", "");
                }
            }
            propertyTypesElement.appendChild(typeElement);
        }
    }

    private void appendVocabularies(Document doc, Element rootElement)
    {
        VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        List<Vocabulary> vocabularies = v3Api.searchVocabularies(sessionToken, VOCABULARY_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (vocabularies.isEmpty())
        {
            return;
        }
        Element vocabsElement = doc.createElement("xmd:controlledVocabularies");
        rootElement.appendChild(vocabsElement);
        for (Vocabulary vocabulary : vocabularies)
        {
            Element vocabElement = doc.createElement("xmd:controlledVocabulary");
            String code = vocabulary.isInternalNameSpace()
                    && vocabulary.getCode().startsWith(INTERNAL_NAMESPACE_PREFIX) ? CodeConverter.tryToDatabase(vocabulary.getCode())
                            : vocabulary.getCode();
            vocabElement.setAttribute("code", code);
            vocabElement.setAttribute("description", vocabulary.getDescription());
            String urlTemplate = vocabulary.getUrlTemplate();
            vocabElement.setAttribute("urlTemplate", urlTemplate);
            vocabElement.setAttribute("managedInternally", String.valueOf(vocabulary.isManagedInternally()));
            vocabElement.setAttribute("internalNamespace", String.valueOf(vocabulary.isInternalNameSpace()));
            vocabElement.setAttribute("chosenFromList", String.valueOf(vocabulary.isChosenFromList()));
            vocabsElement.appendChild(vocabElement);

            for (VocabularyTerm term : vocabulary.getTerms())
            {
                Element termElement = doc.createElement("term");
                termElement.setAttribute("code", term.getCode());
                termElement.setAttribute("label", term.getLabel());
                termElement.setAttribute("description", term.getDescription());
                termElement.setAttribute("ordinal", String.valueOf(term.getOrdinal()));
                termElement.setAttribute("url", createUrl(urlTemplate, term.getCode()));
                vocabElement.appendChild(termElement);
            }
        }
    }

    private String createUrl(String urlTemplate, String code)
    {
        if (urlTemplate == null)
        {
            return null;
        }
        String url = urlTemplate.replaceAll(BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code);
        return url.replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, code);
    }

    private void appendMaterialTypes(Document doc, Element rootElement)
    {
        MaterialTypeFetchOptions fetchOptions = new MaterialTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<MaterialType> types = v3Api.searchMaterialTypes(sessionToken, MATERIAL_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        Element typesElement = doc.createElement("xmd:materialTypes");
        rootElement.appendChild(typesElement);
        for (MaterialType type : types)
        {
            Element typeElement = createTypeElement(doc, typesElement, "xmd:materialType", type);
            typeElement.setAttribute("validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
        }
    }

    private void appendExperimentTypes(Document doc, Element rootElement)
    {
        ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<ExperimentType> types = v3Api.searchExperimentTypes(sessionToken, EXPERIMENT_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        Element typesElement = doc.createElement("xmd:collectionTypes");
        rootElement.appendChild(typesElement);
        for (ExperimentType type : types)
        {
            Element typeElement = createTypeElement(doc, typesElement, "xmd:collectionType", type);
            typeElement.setAttribute("validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
        }
    }

    private void appendSampleTypes(Document doc, Element rootElement)
    {
        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<SampleType> types = v3Api.searchSampleTypes(sessionToken, SAMPLE_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        Element typesElement = doc.createElement("xmd:objectTypes");
        rootElement.appendChild(typesElement);
        for (SampleType type : types)
        {
            Element typeElement = createTypeElement(doc, typesElement, "xmd:objectType", type);
            typeElement.setAttribute("listable", String.valueOf(type.isListable()));
            typeElement.setAttribute("showContainer", String.valueOf(type.isShowContainer()));
            typeElement.setAttribute("showParents", String.valueOf(type.isShowParents()));
            typeElement.setAttribute("showParentMetadata", String.valueOf(type.isShowParentMetadata()));
            typeElement.setAttribute("subcodeUnique", String.valueOf(type.isSubcodeUnique()));
            typeElement.setAttribute("autoGeneratedCode", String.valueOf(type.isAutoGeneratedCode()));
            typeElement.setAttribute("generatedCodePrefix", type.getGeneratedCodePrefix());
            typeElement.setAttribute("validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
        }
    }

    private void appendDataSetTypes(Document doc, Element rootElement)
    {
        DataSetTypeFetchOptions fetchOptions = new DataSetTypeFetchOptions();
        fetchOptions.withPropertyAssignments().withPropertyType();
        fetchOptions.withPropertyAssignments().withPlugin();
        fetchOptions.withValidationPlugin();
        List<DataSetType> types = v3Api.searchDataSetTypes(sessionToken, DATA_SET_TYPE_SEARCH_CRITERIA, fetchOptions).getObjects();
        if (types.isEmpty())
        {
            return;
        }
        Element typesElement = doc.createElement("xmd:dataSetTypes");
        rootElement.appendChild(typesElement);
        for (DataSetType type : types)
        {
            Element typeElement = createTypeElement(doc, typesElement, "xmd:dataSetType", type);
            typeElement.setAttribute("mainDataSetPattern", type.getMainDataSetPattern());
            typeElement.setAttribute("mainDataSetPath", type.getMainDataSetPath());
            typeElement.setAttribute("deletionDisallowed", String.valueOf(type.isDisallowDeletion()));
            typeElement.setAttribute("validationPlugin", type.getValidationPlugin() != null ? type.getValidationPlugin().getName() : null);
        }
    }

    private <T extends ICodeHolder & IDescriptionHolder & IPropertyAssignmentsHolder> Element createTypeElement(
            Document doc, Element rootElement, String elementType, T type)
    {
        Element typeElement = doc.createElement(elementType);
        rootElement.appendChild(typeElement);
        typeElement.setAttribute("code", type.getCode());
        typeElement.setAttribute("description", type.getDescription());
        appendPropertyAssignments(doc, typeElement, type.getPropertyAssignments());
        return typeElement;
    }

    private void appendPropertyAssignments(Document doc, Element rootElement, List<PropertyAssignment> propertyAssignments)
    {
        Element propertyAssignmentsElement = doc.createElement("xmd:propertyAssignments");
        rootElement.appendChild(propertyAssignmentsElement);
        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            Element propertyAssignmentElement = doc.createElement("xmd:propertyAssignment");
            propertyAssignmentsElement.appendChild(propertyAssignmentElement);
            propertyAssignmentElement.setAttribute("propertyTypeCode", propertyAssignment.getPropertyType().getCode());
            propertyAssignmentElement.setAttribute("ordinal", String.valueOf(propertyAssignment.getOrdinal()));
            propertyAssignmentElement.setAttribute("section", propertyAssignment.getSection());
            propertyAssignmentElement.setAttribute("showInEdit", String.valueOf(propertyAssignment.isShowInEditView()));
            propertyAssignmentElement.setAttribute("mandatory", String.valueOf(propertyAssignment.isMandatory()));
            propertyAssignmentElement.setAttribute("showRawValueInForms", String.valueOf(propertyAssignment.isShowRawValueInForms()));
            Plugin plugin = propertyAssignment.getPlugin();
            if (plugin != null)
            {
                propertyAssignmentElement.setAttribute("plugin", plugin.getPermId().getPermId());
                propertyAssignmentElement.setAttribute("pluginType", plugin.getPluginType().toString());
            }
        }
    }

}
