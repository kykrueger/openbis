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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.ExposablePropertyPaceholderConfigurer;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IQueryUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;
import ch.systemsx.cisd.openbis.plugin.query.shared.translator.QueryTranslator;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryServer extends AbstractServer<IQueryServer> implements IQueryServer
{
    /** property with database keys/names separated by delimiter */
    private static final String DATABASE_KEYS = "query-databases";

    private static final String LABEL_PROPERTY_KEY = "label";

    private static final String CREATOR_MINIMAL_ROLE_KEY = "creator-minimal-role";

    private static final String DEFAULT_CREATOR_MINIMAL_ROLE = "POWER_USER";

    private static final String DATA_SPACE_KEY = "data-space";

    @Resource(name = "propertyConfigurer")
    private ExposablePropertyPaceholderConfigurer configurer;

    private DatabaseDefinition databaseDefinition;

    /** @deprecated don't use it directly - use getter instead */
    @Deprecated
    private IDAO dao;

    public QueryServer()
    {
    }

    QueryServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin, IDAO dao)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.dao = dao;
    }

    public IQueryServer createLogger(IInvocationLoggerContext context)
    {
        return new QueryServerLogger(getSessionManager(), context);
    }

    public String tryToGetQueryDatabaseLabel(String sessionToken)
    {
        checkSession(sessionToken);

        DatabaseDefinition definition = tryToGetDatabaseDefinition();
        return definition == null ? null : definition.getLabel();
    }

    public List<QueryExpression> listQueries(String sessionToken)
    {
        checkSession(sessionToken);

        try
        {
            List<QueryPE> queries = getDAOFactory().getQueryDAO().listQueries();
            return QueryTranslator.translate(queries);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    public void registerQuery(String sessionToken, NewQuery expression)
    {
        Session session = getSession(sessionToken);

        QueryPE query = new QueryPE();
        query.setName(expression.getName());
        query.setDescription(expression.getDescription());
        query.setExpression(expression.getExpression());
        query.setPublic(expression.isPublic());
        query.setRegistrator(session.tryGetPerson());
        query.setQueryType(expression.getQueryType());
        try
        {
            getDAOFactory().getQueryDAO().createQuery(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition '"
                    + expression.getName() + "'", null);
        }
    }

    public void deleteQueries(String sessionToken, List<TechId> filterIds)
    {
        checkSession(sessionToken);

        IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
        try
        {
            for (TechId techId : filterIds)
            {
                QueryPE query = queryDAO.getByTechId(techId);
                queryDAO.delete(query);
            }
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition", null);
        }
    }

    public void updateQuery(String sessionToken, IQueryUpdates updates)
    {
        checkSession(sessionToken);

        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(TechId.create(updates));

            query.setName(updates.getName());
            query.setDescription(updates.getDescription());
            query.setExpression(updates.getExpression());
            query.setPublic(updates.isPublic());
            query.setQueryType(updates.getQueryType());
            queryDAO.validateAndSaveUpdatedEntity(query);
        } catch (DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex, "Query definition '"
                    + updates.getName() + "'", null);
        }
    }

    public TableModel queryDatabase(String sessionToken, String sqlQuery,
            QueryParameterBindings bindings)
    {
        checkSession(sessionToken);
        try
        {
            return queryDatabase(sqlQuery, bindings);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings)
    {
        checkSession(sessionToken);
        try
        {
            IQueryDAO queryDAO = getDAOFactory().getQueryDAO();
            QueryPE query = queryDAO.getByTechId(queryId);
            String expression = StringEscapeUtils.unescapeHtml(query.getExpression());
            return queryDatabase(expression, bindings);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
    }

    private TableModel queryDatabase(String sqlQuery, QueryParameterBindings bindings)
    {
        return getDAO().query(sqlQuery, bindings);
    }

    private IDAO getDAO()
    {
        if (dao == null)
        {
            DatabaseDefinition definition = tryToGetDatabaseDefinition();
            if (definition == null)
            {
                throw new UnsupportedOperationException("Undefined query database");
            }
            dao = new DAO(definition.getConfigurationContext().getDataSource());
        }
        return dao;
    }

    private DatabaseDefinition tryToGetDatabaseDefinition()
    {
        if (databaseDefinition == null)
        {
            Properties resolvedProps = configurer.getResolvedProps();
            SectionProperties[] sectionsProperties =
                    PropertyParametersUtil.extractSectionProperties(resolvedProps, DATABASE_KEYS,
                            true);
            DatabaseDefinition[] definitions = new DatabaseDefinition[sectionsProperties.length];
            for (int i = 0; i < definitions.length; i++)
            {
                final String databaseKey = sectionsProperties[i].getKey();
                final Properties databaseProperties = sectionsProperties[i].getProperties();

                final SimpleDatabaseConfigurationContext configurationContext =
                        new SimpleDatabaseConfigurationContext(databaseProperties);
                final String label =
                        PropertyUtils.getMandatoryProperty(databaseProperties, LABEL_PROPERTY_KEY);
                final String creatorMinimalRole =
                        PropertyUtils.getProperty(databaseProperties, CREATOR_MINIMAL_ROLE_KEY,
                                DEFAULT_CREATOR_MINIMAL_ROLE);
                final String dataSpaceOrNull =
                        PropertyUtils.getProperty(databaseProperties, DATA_SPACE_KEY);

                definitions[i] =
                        new DatabaseDefinition(configurationContext, databaseKey, label,
                                creatorMinimalRole, dataSpaceOrNull);
            }
            if (definitions.length > 0)
            {
                databaseDefinition = definitions[0];
                // FIXME add support for multiple DBs
            }
        }
        return databaseDefinition;
    }

}
