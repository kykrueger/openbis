/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link IEntityPropertyTypeDAO} implementation.
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
final class EntityPropertyTypeDAO extends AbstractDAO implements IEntityPropertyTypeDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EntityPropertyTypeDAO.class);

    private final EntityKind entityKind;

    private final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler;

    public EntityPropertyTypeDAO(final EntityKind entityKind,
            final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources.getSessionFactory(), databaseInstance);
        this.entityKind = entityKind;
        this.dynamicPropertyEvaluationScheduler =
                persistencyResources.getDynamicPropertyEvaluationScheduler();
    }

    private final <T extends EntityTypePropertyTypePE> Class<T> getEntityTypePropertyTypeAssignmentClass()
    {
        return entityKind.getEntityTypePropertyTypeAssignmentClass();
    }

    //
    // IEntityPropertyTypeDAO
    //

    @Override
    public final List<EntityTypePropertyTypePE> listEntityPropertyTypes(
            final EntityTypePE entityType) throws DataAccessException
    {
        assert entityType != null : "Unspecified EntityType";

        final List<EntityTypePropertyTypePE> assignments =
                cast(getHibernateTemplate().find(
                        String.format("from %s etpt where etpt.entityTypeInternal = ?",
                                getEntityTypePropertyTypeAssignmentClass().getSimpleName()),
                        toArray(entityType)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d assignments have been found for entity type '%s'.", assignments.size(),
                    entityType));
        }
        return assignments;
    }

    @Override
    public List<String> listPropertyTypeCodes() throws DataAccessException
    {
        final List<EntityTypePropertyTypePE> assignments =
                cast(getHibernateTemplate().loadAll(getEntityTypePropertyTypeAssignmentClass()));
        Set<String> propertyTypeCodes = new HashSet<String>();

        for (EntityTypePropertyTypePE assignment : assignments)
        {
            propertyTypeCodes.add(assignment.getPropertyType().getCode());
        }

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d property types have been found for entity kind '%s'.",
                    propertyTypeCodes.size(), entityKind));
        }
        return new ArrayList<String>(propertyTypeCodes);
    }

    @Override
    public EntityTypePropertyTypePE tryFindAssignment(EntityTypePE entityType,
            PropertyTypePE propertyType)
    {
        assert entityType != null : "Unspecified entity type.";
        assert propertyType != null : "Unspecified property type.";

        final Criteria criteria =
                getSession().createCriteria(getEntityTypePropertyTypeAssignmentClass());
        criteria.add(Restrictions.eq("propertyTypeInternal", propertyType));
        criteria.add(Restrictions.eq("entityTypeInternal", entityType));
        final EntityTypePropertyTypePE etpt = (EntityTypePropertyTypePE) criteria.uniqueResult();
        return etpt;
    }

    @Override
    public final void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException
    {
        assert entityPropertyTypeAssignement != null : "Unspecified EntityTypePropertyType";
        validatePE(entityPropertyTypeAssignement);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(entityPropertyTypeAssignement);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("ADD: assignment of property '"
                    + entityPropertyTypeAssignement.getPropertyType().getCode()
                    + "' with entity type '"
                    + entityPropertyTypeAssignement.getEntityType().getCode() + "'.");
        }
    }

    @Override
    public List<Long> listEntityIds(final EntityTypePE entityType) throws DataAccessException
    {
        assert entityType != null : "Unspecified entity type.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(entityKind.getEntityClass());
        criteria.add(Restrictions.eq(entityKind.getEntityTypeFieldName(), entityType));
        criteria.setProjection(Projections.id());
        final List<Long> list = cast(getHibernateTemplate().findByCriteria(criteria));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("LIST: found %s ids of entities of type '%s'.",
                    list.size(), entityType));
        }
        return list;
    }

    @Override
    public void scheduleDynamicPropertiesEvaluation(final EntityTypePropertyTypePE assignment)
            throws DataAccessException
    {
        assert assignment != null : "Unspecified assignment.";
        if (assignment.isDynamic()) // sanity check
        {
            List<Long> entityIds = listEntityIds(assignment);
            scheduleDynamicPropertiesEvaluation(entityIds);
        }
    }

    private List<Long> listEntityIds(final EntityTypePropertyTypePE assignment)
            throws DataAccessException
    {
        assert assignment != null : "Unspecified assignment.";

        String query =
                String.format(
                        "SELECT pv.entity.id FROM %s pa join pa.propertyValues pv WHERE pa = ?",
                        entityKind.getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        final List<Long> list = cast(getHibernateTemplate().find(query, toArray(assignment)));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "LIST: found %s ids of entities of type '%s' assigned to property '%s'.",
                    list.size(), assignment.getEntityType(), assignment.getPropertyType()));
        }
        return list;
    }

    @Override
    public List<Long> listIdsOfEntitiesWithoutPropertyValue(
            final EntityTypePropertyTypePE assignment) throws DataAccessException
    {
        assert assignment != null : "Unspecified assignment.";

        String query =
                String.format(
                        "SELECT e.id FROM %s e WHERE e.%s = ? AND e not in (SELECT p.entity FROM %s p WHERE p.entityTypePropertyType = ?)",
                        entityKind.getEntityClass().getSimpleName(), entityKind
                                .getEntityTypeFieldName(), entityKind.getEntityPropertyClass()
                                .getSimpleName());
        final List<Long> list =
                cast(getHibernateTemplate().find(query,
                        toArray(assignment.getEntityType(), assignment)));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "LIST: found %s ids of entities of type '%s' assigned to property '%s'.",
                    list.size(), assignment.getEntityType(), assignment.getPropertyType()));
        }
        return list;
    }

    @Override
    public void createProperties(final EntityPropertyPE property, final List<Long> entityIds)
    {
        assert property != null : "Given property data can not be null.";

        final Long etptId = property.getEntityTypePropertyType().getId();
        final Long registratorId = property.getRegistrator().getId();

        final EntityKindPropertyTableNames propertyTableNames =
                getEntityKindPropertyTableNames(entityKind);
        final String tableName = propertyTableNames.getPropertiesTable();
        final String sequenceName = propertyTableNames.getPropertiesSequence();
        final String entityColumn = propertyTableNames.getEntityColumn();
        final String propertyTypeColumn = propertyTableNames.getPropertyTypeColumn();

        final String valueColumn;
        final Serializable valueObject;
        if (property.getVocabularyTerm() != null)
        {
            valueColumn = "cvte_id";
            valueObject = property.getVocabularyTerm().getId();
        } else if (property.getMaterialValue() != null)
        {
            valueColumn = "mate_prop_id";
            valueObject = property.getMaterialValue().getId();
        } else
        {
            assert property.getValue() != null;
            valueColumn = "value";
            valueObject = property.getValue();
        }

        final String sql =
                String.format(
                        "INSERT INTO %s (id, pers_id_registerer, pers_id_author, %s, %s, %s) "
                                + "VALUES (nextval('%s'), :registratorId, :registratorId, :entityId, :etptId, :value)",
                        tableName, entityColumn, propertyTypeColumn, valueColumn, sequenceName);

        // inserts are performed using stateless session for better memory management
        executeStatelessAction(new StatelessHibernateCallback()
            {
                @Override
                public Object doInStatelessSession(StatelessSession session)
                {
                    final SQLQuery sqlQuery = session.createSQLQuery(sql);
                    sqlQuery.setParameter("registratorId", registratorId);
                    sqlQuery.setParameter("etptId", etptId);
                    sqlQuery.setParameter("value", valueObject);
                    int counter = 0;
                    for (Long entityId : entityIds)
                    {
                        sqlQuery.setParameter("entityId", entityId);
                        sqlQuery.executeUpdate();
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug(String.format(
                                    "Created property '%s' for %s with id %s", property,
                                    entityKind.getLabel(), entityId));
                        }
                        if (++counter % 1000 == 0)
                        {
                            operationLog.info(String.format(
                                    "%d %s properties have been created...", counter,
                                    entityKind.getLabel()));
                            if (operationLog.isDebugEnabled())
                            {
                                operationLog.debug(getMemoryUsageMessage());
                            }
                        }
                    }
                    return null;
                }
            });

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Created %s %s properties : %s", entityIds.size(),
                    entityKind.getLabel(), property));
        }

        scheduleDynamicPropertiesEvaluation(entityIds);
    }

    private String getMemoryUsageMessage()
    {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024l * 1024l;
        long totalMemory = runtime.totalMemory() / mb;
        long freeMemory = runtime.freeMemory() / mb;
        long maxMemory = runtime.maxMemory() / mb;
        return "MEMORY (in MB): free:" + freeMemory + " total:" + totalMemory + " max:" + maxMemory;
    }

    @Override
    public void fillTermUsageStatistics(List<VocabularyTermWithStats> termsWithStats,
            VocabularyPE vocabulary)
    {
        assert termsWithStats != null : "Unspecified terms.";
        assert vocabulary != null : "Unspecified vocabulary.";
        assert termsWithStats.size() == vocabulary.getTerms().size() : "Sizes of terms to be filled and vocabulary terms don't match.";

        Map<Long, VocabularyTermWithStats> termsById =
                new HashMap<Long, VocabularyTermWithStats>(termsWithStats.size());
        for (VocabularyTermWithStats termWithStats : termsWithStats)
        {
            Long id = termWithStats.getTerm().getId();
            termsById.put(id, termWithStats);
        }

        final DetachedCriteria criteria =
                DetachedCriteria.forClass(entityKind.getEntityPropertyClass());
        // alias is the easiest way to restrict on association using criteria
        criteria.createAlias("vocabularyTerm", "term");
        criteria.add(Restrictions.eq("term.vocabularyInternal", vocabulary));
        ProjectionList projectionList = Projections.projectionList();
        projectionList.add(Projections.rowCount());
        projectionList.add(Projections.groupProperty("term.id"));
        criteria.setProjection(projectionList);

        final List<Object[]> results = cast(getHibernateTemplate().findByCriteria(criteria));

        for (Object[] result : results)
        {
            Integer numberOfUsages = ((Number) result[0]).intValue();
            Long termId = (Long) result[1];
            termsById.get(termId).registerUsage(entityKind, numberOfUsages);
        }
    }

    @Override
    public List<EntityPropertyPE> listPropertiesByVocabularyTerm(String vocabularyTermCode)
    {
        // we have to fetch props.entity, because hibernate search has some problems with reindexing
        // otherwise
        String query =
                String.format(
                        "from %s props join fetch props.entity where props.vocabularyTerm.code = ?",
                        entityKind.getEntityPropertyClass().getSimpleName());
        //
        List<EntityPropertyPE> properties =
                cast(getHibernateTemplate().find(query, toArray(vocabularyTermCode)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Term '%s' is used in %d properties of kind %s.",
                    vocabularyTermCode, properties.size(), entityKind));
        }
        return properties;
    }

    @Override
    public void updateProperties(List<EntityPropertyPE> properties)
    {
        final HibernateTemplate template = getHibernateTemplate();
        for (EntityPropertyPE entityProperty : properties)
        {
            template.save(entityProperty);
        }
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("UPDATE: " + properties.size() + " of kind " + entityKind
                    + " updated.");
        }
    }

    @Override
    public void increaseOrdinals(EntityTypePE entityType, Long fromOrdinal, int increment)
    {
        assert entityType != null : "Unspecified entity type.";
        assert fromOrdinal != null : "Unspecified ordinal.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();

        String query =
                String.format("UPDATE %s etpt SET etpt.ordinal = etpt.ordinal + ? "
                        + "WHERE etpt.entityTypeInternal = ? AND etpt.ordinal >= ?", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        final int updatedRows =
                hibernateTemplate.bulkUpdate(query,
                        toArray(new Long(increment), entityType, fromOrdinal));
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.debug(String.format(
                    "%d etpt(s) updated for entity type '%s' with ordinal increased by %d.",
                    updatedRows, entityType.getCode(), increment));
        }
    }

    @Override
    public Long getMaxOrdinal(EntityTypePE entityType)
    {
        assert entityType != null : "Unspecified entity type.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        String query =
                String.format("select max(etpt.ordinal) from %s etpt "
                        + "WHERE etpt.entityTypeInternal = ?", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName());

        List<Long> resultList = cast(hibernateTemplate.find(query, entityType));
        Long maxOrdinal = resultList.get(0);
        return maxOrdinal == null ? 0L : maxOrdinal;
    }

    @Override
    public final void validateAndSaveUpdatedEntity(EntityTypePropertyTypePE entity)
    {
        assert entity != null : "entity is null";

        validatePE(entity);
        getHibernateTemplate().flush();
    }

    @Override
    public int countAssignmentValues(String entityTypeCode, String propertyTypeCode)
    {
        assert entityTypeCode != null : "Unspecified entity type.";
        assert propertyTypeCode != null : "Unspecified property type.";

        String query =
                String.format("SELECT count(pv) FROM %s pa join pa.propertyValues pv "
                        + "WHERE pa.propertyTypeInternal.simpleCode = ? "
                        + "AND pa.entityTypeInternal.code = ?", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        return ((Long) (getHibernateTemplate().find(query,
                toArray(propertyTypeCode, entityTypeCode)).get(0))).intValue();
    }

    @Override
    public void delete(EntityTypePropertyTypePE assignment)
    {
        HibernateTemplate template = getHibernateTemplate();

        List<Long> entityIds = listEntityIds(assignment);
        template.bulkUpdate(String.format("DELETE FROM %s WHERE entityTypePropertyType = ?",
                entityKind.getEntityPropertyClass().getSimpleName()), assignment);
        template.flush();
        template.clear();
        template.delete(assignment);

        scheduleDynamicPropertiesEvaluation(entityIds);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DELETE: assignment between " + entityKind + " of type "
                    + assignment.getEntityType().getCode() + " and property type "
                    + assignment.getPropertyType().getCode());
        }
    }

    // helpers

    private void scheduleDynamicPropertiesEvaluation(List<Long> entityIds)
    {
        scheduleDynamicPropertiesEvaluationForIds(dynamicPropertyEvaluationScheduler,
                getIndexedEntityClass(entityKind), entityIds);
    }

    private static <T extends IEntityInformationWithPropertiesHolder> Class<T> getIndexedEntityClass(
            EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return cast(ExternalDataPE.class);
            default:
                return entityKind.getEntityClass();
        }
    }

    private static EntityKindPropertyTableNames getEntityKindPropertyTableNames(
            EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return new EntityKindPropertyTableNames(TableNames.DATA_SET_PROPERTIES_TABLE,
                        SequenceNames.DATA_SET_PROPERTY_SEQUENCE,
                        ColumnNames.DATA_SET_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.DATA_SET_COLUMN);
            case EXPERIMENT:
                return new EntityKindPropertyTableNames(TableNames.EXPERIMENT_PROPERTIES_TABLE,
                        SequenceNames.EXPERIMENT_PROPERTY_SEQUENCE,
                        ColumnNames.EXPERIMENT_TYPE_PROPERTY_TYPE_COLUMN,
                        ColumnNames.EXPERIMENT_COLUMN);
            case MATERIAL:
                return new EntityKindPropertyTableNames(TableNames.MATERIAL_PROPERTIES_TABLE,
                        SequenceNames.MATERIAL_PROPERTY_SEQUENCE,
                        ColumnNames.MATERIAL_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.MATERIAL_COLUMN);
            case SAMPLE:
                return new EntityKindPropertyTableNames(TableNames.SAMPLE_PROPERTIES_TABLE,
                        SequenceNames.SAMPLE_PROPERTY_SEQUENCE,
                        ColumnNames.SAMPLE_TYPE_PROPERTY_TYPE_COLUMN, ColumnNames.SAMPLE_COLUMN);
        }
        return null; // can't happen
    }

    private static class EntityKindPropertyTableNames
    {

        private final String propertiesTable;

        private final String propertiesSequence;

        private final String propertyTypeColumn;

        private final String entityColumn;

        public EntityKindPropertyTableNames(String propertiesTable, String propertiesSequence,
                String propertyTypeColumn, String entityColumn)
        {
            this.propertiesTable = propertiesTable;
            this.propertiesSequence = propertiesSequence;
            this.propertyTypeColumn = propertyTypeColumn;
            this.entityColumn = entityColumn;
        }

        public String getPropertiesTable()
        {
            return propertiesTable;
        }

        public String getPropertiesSequence()
        {
            return propertiesSequence;
        }

        public String getPropertyTypeColumn()
        {
            return propertyTypeColumn;
        }

        public String getEntityColumn()
        {
            return entityColumn;
        }

    }

}
