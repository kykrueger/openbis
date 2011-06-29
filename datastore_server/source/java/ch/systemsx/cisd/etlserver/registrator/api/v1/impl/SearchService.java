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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionFilter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.MaterialIdentifierCollection;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class SearchService implements ISearchService
{
    private final IEncapsulatedOpenBISService openBisService;

    public SearchService(IEncapsulatedOpenBISService openBisService)
    {
        this.openBisService = openBisService;
    }

    public List<IExperimentImmutable> listExperiments(String projectIdentifierString)
    {
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(projectIdentifierString.toUpperCase())
                        .createIdentifier();
        List<Experiment> serverExperiments = openBisService.listExperiments(projectIdentifier);
        ArrayList<IExperimentImmutable> experiments =
                new ArrayList<IExperimentImmutable>(serverExperiments.size());
        for (Experiment experiment : serverExperiments)
        {
            experiments.add(new ExperimentImmutable(experiment));
        }
        return experiments;
    }

    public List<IDataSetImmutable> searchForDataSets(String property, String value,
            String typeOrNull)
    {
        SearchCriteria sc = new SearchCriteria();
        if (null != typeOrNull)
        {
            sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                    typeOrNull));
        }
        sc.addMatchClause(MatchClause.createPropertyMatch(property, value));
        return searchForDataSets(sc);
    }

    public List<ISampleImmutable> searchForSamples(String property, String value, String typeOrNull)
    {
        SearchCriteria sc = new SearchCriteria();
        if (null != typeOrNull)
        {
            sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                    typeOrNull));
        }
        sc.addMatchClause(MatchClause.createPropertyMatch(property, value));
        return searchForSamples(sc);
    }

    public List<IDataSetImmutable> searchForDataSets(SearchCriteria searchCriteria)
    {
        List<ExternalData> serverDataSets = openBisService.searchForDataSets(searchCriteria);
        ArrayList<IDataSetImmutable> dataSets =
                new ArrayList<IDataSetImmutable>(serverDataSets.size());
        for (ExternalData dataSet : serverDataSets)
        {
            dataSets.add(new DataSetImmutable(dataSet));
        }
        return dataSets;
    }

    public List<ISampleImmutable> searchForSamples(SearchCriteria searchCriteria)
    {
        List<Sample> serverSamples = openBisService.searchForSamples(searchCriteria);
        ArrayList<ISampleImmutable> samples = new ArrayList<ISampleImmutable>(serverSamples.size());
        for (Sample sample : serverSamples)
        {
            samples.add(new SampleImmutable(sample));
        }
        return samples;
    }

    public List<IMaterialImmutable> listMaterials(MaterialIdentifierCollection identifierCollection)
    {
        final Set<String> identifiers = new HashSet<String>(identifierCollection.getIdentifiers());
        Set<String> searchedTypes = extractMaterialTypes(identifierCollection);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> accumulatedResults =
                findAllMaterials(searchedTypes);

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> filteredByIdentifier =
                CollectionUtils
                        .filter(accumulatedResults,
                                new ICollectionFilter<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material>()
                                    {

                                        public boolean isPresent(Material element)
                                        {
                                            return identifiers.contains(element.getIdentifier());
                                        }

                                    });
        return translate(filteredByIdentifier);
    }

    private Set<String> extractMaterialTypes(MaterialIdentifierCollection identifierCollection)
    {
        Set<String> searchedTypes = new HashSet<String>();
        for (String stringIdentifier : identifierCollection.getIdentifiers())
        {
            MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(stringIdentifier);
            searchedTypes.add(identifier.getTypeCode());
        }
        return searchedTypes;
    }

    private List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> findAllMaterials(
            Set<String> searchedTypes)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> accumulatedResults =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material>();
        for (String typeCode : searchedTypes)
        {
            MaterialType materialType = new MaterialType();
            materialType.setCode(typeCode);
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material> materialsOfType =
                    openBisService.listMaterials(new ListMaterialCriteria(materialType), true);
            accumulatedResults.addAll(materialsOfType);
        }
        return accumulatedResults;
    }

    private List<IMaterialImmutable> translate(List<Material> materials)
    {
        List<IMaterialImmutable> result = new ArrayList<IMaterialImmutable>();
        for (Material material : materials)
        {
            result.add(new MaterialImmutable(material));
        }
        return result;
    }

}
