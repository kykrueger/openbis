/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.common.spring.SpringEoDSQLExceptionTranslator;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityOperationsLogDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyHistoryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPostRegistrationDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * {@link IDAOFactory} implementation working with {@link DatabaseConfigurationContext} and
 * {@link SessionFactory}.
 * 
 * @author Franz-Josef Elmer
 */
public final class DAOFactory extends AuthorizationDAOFactory implements IDAOFactory
{
    static
    {
        SpringEoDSQLExceptionTranslator.activate();
    }

    private final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler;

    private final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler;

    private final ISampleTypeDAO sampleTypeDAO;

    private final IHibernateSearchDAO hibernateSearchDAO;

    private final IPropertyTypeDAO propertyTypeDAO;

    private final Map<EntityKind, IEntityTypeDAO> entityTypeDAOs =
            new HashMap<EntityKind, IEntityTypeDAO>();

    private final Map<EntityKind, IEntityPropertyTypeDAO> entityPropertyTypeDAOs =
            new HashMap<EntityKind, IEntityPropertyTypeDAO>();

    private final IVocabularyDAO vocabularyDAO;

    private final IVocabularyTermDAO vocabularyTermDAO;

    private final IAttachmentDAO attachmentDAO;

    private final DataSetTypeDAO dataSetTypeDAO;

    private final FileFormatTypeDAO fileFormatTypeDAO;

    private final LocatorTypeDAO locatorTypeDAO;

    private final IMaterialDAO materialDAO;

    private final ICodeSequenceDAO codeSequenceDAO;

    private final IDataStoreDAO dataStoreDAO;

    private final IPermIdDAO permIdDAO;

    private final IEventDAO eventDAO;

    private final IAuthorizationGroupDAO authorizationGroupDAO;

    private final IScriptDAO scriptDAO;

    private final ICorePluginDAO corePluginDAO;

    private final IPostRegistrationDAO postRegistrationDAO;

    private final IEntityOperationsLogDAO entityOperationsLogDAO;

    private EntityPropertyHistoryDAO entityPropertyHistoryDAO;

    public DAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory, HibernateSearchContext hibernateSearchContext,
            final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler,
            final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler)
    {
        super(context, sessionFactory, fullTextIndexUpdateScheduler,
                dynamicPropertyEvaluationScheduler);
        this.dynamicPropertyEvaluationScheduler = dynamicPropertyEvaluationScheduler;
        this.fullTextIndexUpdateScheduler = fullTextIndexUpdateScheduler;
        final DatabaseInstancePE databaseInstance = getHomeDatabaseInstance();
        sampleTypeDAO = new SampleTypeDAO(sessionFactory, databaseInstance);
        hibernateSearchDAO = new HibernateSearchDAO(sessionFactory, hibernateSearchContext);
        propertyTypeDAO = new PropertyTypeDAO(sessionFactory, databaseInstance);
        vocabularyDAO = new VocabularyDAO(sessionFactory, databaseInstance);
        vocabularyTermDAO = new VocabularyTermDAO(sessionFactory, databaseInstance);
        attachmentDAO = new AttachmentDAO(sessionFactory, databaseInstance);
        dataSetTypeDAO = new DataSetTypeDAO(sessionFactory, databaseInstance);
        fileFormatTypeDAO = new FileFormatTypeDAO(sessionFactory, databaseInstance);
        locatorTypeDAO = new LocatorTypeDAO(sessionFactory, databaseInstance);
        materialDAO = new MaterialDAO(getPersistencyResources(), databaseInstance);
        codeSequenceDAO = new CodeSequenceDAO(sessionFactory, databaseInstance);
        dataStoreDAO = new DataStoreDAO(sessionFactory, databaseInstance);
        permIdDAO = new PermIdDAO(sessionFactory, databaseInstance);
        eventDAO = new EventDAO(sessionFactory, databaseInstance);
        authorizationGroupDAO = new AuthorizationGroupDAO(sessionFactory, databaseInstance);
        scriptDAO = new ScriptDAO(sessionFactory, databaseInstance);
        corePluginDAO = new CorePluginDAO(sessionFactory, databaseInstance);
        postRegistrationDAO = new PostRegistrationDAO(sessionFactory, databaseInstance);
        entityOperationsLogDAO = new EntityOperationsLogDAO(sessionFactory, databaseInstance);
        final EntityKind[] entityKinds = EntityKind.values();
        for (final EntityKind entityKind : entityKinds)
        {
            final EntityTypeDAO dao =
                    new EntityTypeDAO(entityKind, sessionFactory, databaseInstance);
            entityTypeDAOs.put(entityKind, dao);
            entityPropertyTypeDAOs.put(entityKind, new EntityPropertyTypeDAO(entityKind,
                    getPersistencyResources(), databaseInstance));
        }
        entityPropertyHistoryDAO =
                new EntityPropertyHistoryDAO(getPersistencyResources(), databaseInstance);
    }

    //
    // IDAOFactory
    //

    @Override
    public final ISampleTypeDAO getSampleTypeDAO()
    {
        return sampleTypeDAO;
    }

    @Override
    public final IHibernateSearchDAO getHibernateSearchDAO()
    {
        return hibernateSearchDAO;
    }

    @Override
    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(final EntityKind entityKind)
    {
        return entityPropertyTypeDAOs.get(entityKind);
    }

    @Override
    public IEntityPropertyHistoryDAO getEntityPropertyHistoryDAO()
    {
        return entityPropertyHistoryDAO;
    }

    @Override
    public IEntityTypeDAO getEntityTypeDAO(final EntityKind entityKind)
    {
        return entityTypeDAOs.get(entityKind);
    }

    @Override
    public IPropertyTypeDAO getPropertyTypeDAO()
    {
        return propertyTypeDAO;
    }

    @Override
    public final IVocabularyDAO getVocabularyDAO()
    {
        return vocabularyDAO;
    }

    @Override
    public final IVocabularyTermDAO getVocabularyTermDAO()
    {
        return vocabularyTermDAO;
    }

    @Override
    public final IAttachmentDAO getAttachmentDAO()
    {
        return attachmentDAO;
    }

    @Override
    public IDataSetTypeDAO getDataSetTypeDAO()
    {
        return dataSetTypeDAO;
    }

    @Override
    public IFileFormatTypeDAO getFileFormatTypeDAO()
    {
        return fileFormatTypeDAO;
    }

    @Override
    public ILocatorTypeDAO getLocatorTypeDAO()
    {
        return locatorTypeDAO;
    }

    @Override
    public IMaterialDAO getMaterialDAO()
    {
        return materialDAO;
    }

    @Override
    public ICodeSequenceDAO getCodeSequenceDAO()
    {
        return codeSequenceDAO;
    }

    @Override
    public IDataStoreDAO getDataStoreDAO()
    {
        return dataStoreDAO;
    }

    @Override
    public IPermIdDAO getPermIdDAO()
    {
        return permIdDAO;
    }

    @Override
    public IEventDAO getEventDAO()
    {
        return eventDAO;
    }

    @Override
    public IAuthorizationGroupDAO getAuthorizationGroupDAO()
    {
        return authorizationGroupDAO;
    }

    @Override
    public IScriptDAO getScriptDAO()
    {
        return scriptDAO;
    }

    public IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluationScheduler()
    {
        return dynamicPropertyEvaluationScheduler;
    }

    public IFullTextIndexUpdateScheduler getFullTextIndexUpdateScheduler()
    {
        return fullTextIndexUpdateScheduler;
    }

    @Override
    public ICorePluginDAO getCorePluginDAO()
    {
        return corePluginDAO;
    }

    @Override
    public IPostRegistrationDAO getPostRegistrationDAO()
    {
        return postRegistrationDAO;
    }

    @Override
    public IEntityOperationsLogDAO getEntityOperationsLogDAO()
    {
        return entityOperationsLogDAO;
    }
}
