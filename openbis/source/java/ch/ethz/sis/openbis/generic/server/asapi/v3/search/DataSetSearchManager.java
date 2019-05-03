///*
// * Copyright 2011 ETH Zuerich, CISD
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package ch.ethz.sis.openbis.generic.server.asapi.v3.search;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
//import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetChildrenSearchCriteria;
//import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetParentsSearchCriteria;
//import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
//import org.springframework.dao.DataAccessException;
//
//import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
//import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
//import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
//import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
//
///**
// * Manages detailed search for complex data set search criterion.
// *
// * @author Viktor Kovtun
// */
//public class DataSetSearchManager extends AbstractSearchManager<IDatasetLister>
//{
//    private final IRelationshipHandler CHILDREN_RELATIONSHIP_HANDLER = new IRelationshipHandler()
//        {
//
//            @Override
//            public Collection<Long> findRelatedIdsByCriteria(String userId,
//                    DetailedSearchCriteria criteria,
//                    List<DetailedSearchSubCriteria> otherSubCriterias)
//            {
//                return findDataSetIds(userId, criteria, otherSubCriterias);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> dataSetIds)
//            {
//                return lister.listChildrenIds(dataSetIds);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> childrenDataSetIds)
//            {
//                return lister.listParentIds(childrenDataSetIds);
//            }
//        };
//
//    private final IRelationshipHandler PARENT_RELATIONSHIP_HANDLER = new IRelationshipHandler()
//        {
//
//            @Override
//            public Collection<Long> findRelatedIdsByCriteria(String userId,
//                    DetailedSearchCriteria criteria,
//                    List<DetailedSearchSubCriteria> otherSubCriterias)
//            {
//                return findDataSetIds(userId, criteria, otherSubCriterias);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> dataSetIds)
//            {
//                return lister.listParentIds(dataSetIds);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> parentDataSetIds)
//            {
//                return lister.listChildrenIds(parentDataSetIds);
//            }
//        };
//
//    private final IRelationshipHandler CONTAINER_RELATIONSHIP_HANDLER = new IRelationshipHandler()
//        {
//
//            @Override
//            public Collection<Long> findRelatedIdsByCriteria(String userId,
//                    DetailedSearchCriteria criteria,
//                    List<DetailedSearchSubCriteria> otherSubCriterias)
//            {
//                return findDataSetIds(userId, criteria, otherSubCriterias);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> dataSetIds)
//            {
//                return lister.listContainerIds(dataSetIds);
//            }
//
//            @Override
//            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> parentDataSetIds)
//            {
//                return lister.listComponetIds(parentDataSetIds);
//            }
//        };
//
//    public DataSetSearchManager(ISQLSearchDAO searchDAO, IDatasetLister lister)
//    {
//        super(searchDAO, lister);
//    }
//
//    public List<AbstractExternalData> searchForDataSets(String userId, DetailedSearchCriteria criteria)
//            throws DataAccessException
//    {
//        return lister.listByDatasetIds(searchForDataSetIds(userId, criteria));
//    }
//
//    public Collection<Long> searchForDataSetIds(String userId, DetailedSearchCriteria criteria)
//    {
//        DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
//        DetailedSearchCriteria childCriteria = new DetailedSearchCriteria();
//        DetailedSearchCriteria containerCriteria = new DetailedSearchCriteria();
//        List<DetailedSearchSubCriteria> otherSubCriterias =
//                new ArrayList<DetailedSearchSubCriteria>();
////        groupDataSetSubCriteria(criteria.getSubCriterias(), parentCriteria, childCriteria, containerCriteria,
////                otherSubCriterias);
//
//        boolean hasMainCriteria = false == criteria.getCriteria().isEmpty() || false == otherSubCriterias.isEmpty();
//        boolean hasParentCriteria = false == parentCriteria.isEmpty();
//        boolean hasChildCriteria = false == childCriteria.isEmpty();
//        boolean hasContainerCriteria = false == containerCriteria.isEmpty();
//
//        Collection<Long> dataSetIds = null;
//
//        if (hasMainCriteria || (hasMainCriteria == false && hasParentCriteria == false && hasChildCriteria == false
//                && hasContainerCriteria == false))
//        {
//            dataSetIds = findDataSetIds(userId, criteria, otherSubCriterias);
//            if (dataSetIds == null)
//            {
//                dataSetIds = Collections.emptyList();
//            }
//        }
//
//        if (hasParentCriteria)
//        {
//            final DataSetSearchCriteria sampleSearchCriteria = new DataSetSearchCriteria();
//            sampleSearchCriteria.getCriteria().addAll(parentCriteria);
//            dataSetIds = filterSearchResultsByCriteria(userId, dataSetIds, sampleSearchCriteria,
//                    PARENT_RELATIONSHIP_HANDLER);
//        }
//
//        if (hasChildCriteria)
//        {
//            final DataSetSearchCriteria sampleSearchCriteria = new DataSetSearchCriteria();
//            sampleSearchCriteria.getCriteria().addAll(parentCriteria);
//            dataSetIds = filterSearchResultsByCriteria(userId, dataSetIds, sampleSearchCriteria,
//                    CHILDREN_RELATIONSHIP_HANDLER);
//        }
//
//        if (hasContainerCriteria)
//        {
//            final DataSetSearchCriteria sampleSearchCriteria = new DataSetSearchCriteria();
//            sampleSearchCriteria.getCriteria().addAll(parentCriteria);
//            dataSetIds = filterSearchResultsByCriteria(userId, dataSetIds, sampleSearchCriteria,
//                    CONTAINER_RELATIONSHIP_HANDLER);
//        }
//
//        Collection<Long> result = restrictResultSetIfNecessary(dataSetIds);
//        return result;
//    }
//
//    private List<Long> findDataSetIds(String userId, DetailedSearchCriteria criteria,
//            List<DetailedSearchSubCriteria> otherSubCriterias)
//    {
//        List<IAssociationCriteria> associations =
//                new ArrayList<IAssociationCriteria>();
//        for (DetailedSearchSubCriteria subCriteria : otherSubCriterias)
//        {
//            // TODO: rewrite method findAssociatedEntities().
////            associations.add(findAssociatedEntities(userId, subCriteria));
//        }
//
//        if (criteria.getCriteria().isEmpty() && otherSubCriterias.isEmpty())
//        {
//            // if no criteria were provided find all data sets
//            criteria.getCriteria().add(
//                    new DetailedSearchCriterion(DetailedSearchField
//                            .createAttributeField(DataSetAttributeSearchFieldKind.CODE), "*"));
//        }
//
//        final List<Long> dataSetIds =
//                searchDAO.searchForEntityIds(userId, criteria,
//                        DtoConverters.convertEntityKind(EntityKind.DATA_SET), associations);
//        return dataSetIds;
//    }
//
//    private void groupDataSetSubCriteria(final Collection<ISearchCriteria> allCriteria,
//            final Collection<ISearchCriteria> parentCriteria, final Collection<ISearchCriteria> childCriteria,
//            final Collection<ISearchCriteria> otherCriteria) {
//        parentCriteria.clear();
//        childCriteria.clear();
//        otherCriteria.clear();
//        for (ISearchCriteria subCriterion : allCriteria) {
//            if (subCriterion instanceof DataSetParentsSearchCriteria) {
//                mergeCriteria(parentCriteria, subCriterion);
//            } else if (subCriterion instanceof DataSetChildrenSearchCriteria) {
//                mergeCriteria(childCriteria, subCriterion);
//            } else {
//                otherCriteria.add(subCriterion);
//            }
//        }
//    }
//
//}
