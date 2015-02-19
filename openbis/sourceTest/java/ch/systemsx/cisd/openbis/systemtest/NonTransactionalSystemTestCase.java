/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Abstract super class of head-less system tests.
 * 
 * @author Franz-Josef Elmer
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
//@TransactionConfiguration(transactionManager = "transaction-manager")
@TestExecutionListeners({SqlScriptsTestExecutionListener.class})

public abstract class NonTransactionalSystemTestCase extends AbstractTestNGSpringContextTests
{
    

    /**
     * The {@code JdbcTemplate} that this base class manages, available to subclasses.
     * @since 3.2
     */
    protected JdbcTemplate jdbcTemplate;

    private String sqlScriptEncoding;


    /**
     * Set the {@code DataSource}, typically provided via Dependency Injection.
     * <p>This method also instantiates the {@link #jdbcTemplate} instance variable.
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Specify the encoding for SQL scripts, if different from the platform encoding.
     * @see #executeSqlScript
     */
    public void setSqlScriptEncoding(String sqlScriptEncoding) {
        this.sqlScriptEncoding = sqlScriptEncoding;
    }

    /**
     * Convenience method for counting the rows in the given table.
     * @param tableName table name to count rows in
     * @return the number of rows in the table
     * @see JdbcTestUtils#countRowsInTable
     */
    protected int countRowsInTable(String tableName) {
        return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
    }

    /**
     * Convenience method for counting the rows in the given table, using the
     * provided {@code WHERE} clause.
     * <p>See the Javadoc for {@link JdbcTestUtils#countRowsInTableWhere} for details.
     * @param tableName the name of the table to count rows in
     * @param whereClause the {@code WHERE} clause to append to the query
     * @return the number of rows in the table that match the provided
     * {@code WHERE} clause
     * @since 3.2
     * @see JdbcTestUtils#countRowsInTableWhere
     */
    protected int countRowsInTableWhere(String tableName, String whereClause) {
        return JdbcTestUtils.countRowsInTableWhere(this.jdbcTemplate, tableName, whereClause);
    }

    /**
     * Convenience method for deleting all rows from the specified tables.
     * <p>Use with caution outside of a transaction!
     * @param names the names of the tables from which to delete
     * @return the total number of rows deleted from all specified tables
     * @see JdbcTestUtils#deleteFromTables
     */
    protected int deleteFromTables(String... names) {
        return JdbcTestUtils.deleteFromTables(this.jdbcTemplate, names);
    }

    /**
     * Convenience method for deleting all rows from the given table, using the
     * provided {@code WHERE} clause.
     * <p>Use with caution outside of a transaction!
     * <p>See the Javadoc for {@link JdbcTestUtils#deleteFromTableWhere} for details.
     * @param tableName the name of the table to delete rows from
     * @param whereClause the {@code WHERE} clause to append to the query
     * @param args arguments to bind to the query (leaving it to the {@code
     * PreparedStatement} to guess the corresponding SQL type); may also contain
     * {@link org.springframework.jdbc.core.SqlParameterValue SqlParameterValue}
     * objects which indicate not only the argument value but also the SQL type
     * and optionally the scale.
     * @return the number of rows deleted from the table
     * @since 4.0
     * @see JdbcTestUtils#deleteFromTableWhere
     */
    protected int deleteFromTableWhere(String tableName, String whereClause, Object... args) {
        return JdbcTestUtils.deleteFromTableWhere(jdbcTemplate, tableName, whereClause, args);
    }

    /**
     * Convenience method for dropping all of the specified tables.
     * <p>Use with caution outside of a transaction!
     * @param names the names of the tables to drop
     * @since 3.2
     * @see JdbcTestUtils#dropTables
     */
    protected void dropTables(String... names) {
        JdbcTestUtils.dropTables(this.jdbcTemplate, names);
    }

    /**
     * Execute the given SQL script.
     * <p>Use with caution outside of a transaction!
     * <p>The script will normally be loaded by classpath.
     * <p><b>Do not use this method to execute DDL if you expect rollback.</b>
     * @param sqlResourcePath the Spring resource path for the SQL script
     * @param continueOnError whether or not to continue without throwing an
     * exception in the event of an error
     * @throws DataAccessException if there is an error executing a statement
     * @see ResourceDatabasePopulator
     * @see #setSqlScriptEncoding
     */
    protected void executeSqlScript(String sqlResourcePath, boolean continueOnError) throws DataAccessException {
        Resource resource = this.applicationContext.getResource(sqlResourcePath);
        new ResourceDatabasePopulator(continueOnError, false, this.sqlScriptEncoding, resource).execute(jdbcTemplate.getDataSource());
    }
    
    
    protected static final String SESSION_KEY = "session-key";

    protected ICommonServerForInternalUse commonServer;

    protected IGenericServer genericServer;

    protected ICommonClientService commonClientService;

    protected IGenericClientService genericClientService;

    protected IServiceForDataStoreServer etlService;

    protected MockHttpServletRequest request;

    protected String systemSessionToken;

    @BeforeSuite
    public void beforeSuite()
    {
        TestInitializer.init();
    }

    @BeforeMethod(alwaysRun = true)
    @BeforeClass
    public void loginAsSystem()
    {
        systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
    }

    /**
     * Sets a {@link MockHttpServletRequest} for the specified context provider
     */
    @Autowired
    public final void setRequestContextProvider(final SpringRequestContextProvider contextProvider)
    {
        request = new MockHttpServletRequest();
        contextProvider.setRequest(request);
    }

    /**
     * Sets <code>commonServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonServer(final ICommonServerForInternalUse commonServer)
    {
        this.commonServer = commonServer;
    }

    /**
     * Sets <code>genericServer</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericServer(final IGenericServer genericServer)
    {
        this.genericServer = genericServer;
    }

    /**
     * Sets <code>commonClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setCommonClientService(final ICommonClientService commonClientService)
    {
        this.commonClientService = commonClientService;
    }

    /**
     * Sets <code>genericClientService</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setGenericClientService(final IGenericClientService genericClientService)
    {
        this.genericClientService = genericClientService;
    }

    @Autowired
    public void setETLService(IServiceForDataStoreServer etlService)
    {
        this.etlService = etlService;

    }

    protected SessionContext logIntoCommonClientService()
    {
        SessionContext context = commonClientService.tryToLogin("test", "a");
        AssertJUnit.assertNotNull(context);
        return context;
    }

    protected void logOutFromCommonClientService()
    {
        commonClientService.logout(new DisplaySettings(), false);
    }

    protected void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    public final class NewSampleBuilder
    {
        private NewSample sample = new NewSample();

        private List<IEntityProperty> propertis = new ArrayList<IEntityProperty>();

        public NewSample get()
        {
            return sample;
        }

        public NewSampleBuilder(String identifier)
        {
            sample.setIdentifier(identifier);
        }

        public NewSampleBuilder type(String type)
        {
            SampleType sampleType = new SampleType();
            sampleType.setCode(type);
            sample.setSampleType(sampleType);
            return this;
        }

        public NewSampleBuilder experiment(String identifier)
        {
            sample.setExperimentIdentifier(identifier);
            return this;
        }

        public NewSampleBuilder container(String identifier)
        {
            sample.setContainerIdentifier(identifier);
            return this;
        }

        public NewSampleBuilder parents(String... parentIdentifiers)
        {
            sample.setParentsOrNull(parentIdentifiers);
            return this;
        }

        public NewSampleBuilder property(String key, String value)
        {
            propertis.add(new PropertyBuilder(key).value(value).getProperty());
            return this;
        }

        public void register()
        {
            sample.setProperties(propertis.toArray(new IEntityProperty[propertis.size()]));
            genericClientService.registerSample(SESSION_KEY, sample);
        }
    }

    /**
     * Register a person with specified user ID.
     * 
     * @return userID
     */
    protected String registerPerson(String userID)
    {
        commonServer.registerPerson(systemSessionToken, userID);
        return userID;
    }

    protected void assignInstanceRole(String userID, RoleCode roleCode)
    {
        commonServer.registerInstanceRole(systemSessionToken, roleCode,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRole(String userID, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRoleToGroup(String groupCode, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier, Grantee.createAuthorizationGroup(groupCode));
    }

    protected String registerAuthorizationGroupWithUsers(String groupName, List<String> userIds)
    {
        NewAuthorizationGroup newGroup = new NewAuthorizationGroup();
        newGroup.setCode(groupName);
        newGroup.setDescription(groupName);
        commonServer.registerAuthorizationGroup(systemSessionToken, newGroup);

        List<AuthorizationGroup> groups = commonServer.listAuthorizationGroups(systemSessionToken);
        AuthorizationGroup group = null;
        for (AuthorizationGroup groupCandidate : groups)
        {
            if (groupCandidate.getCode().equals(groupName))
            {
                group = groupCandidate;
                break;
            }
        }

        commonServer.addPersonsToAuthorizationGroup(systemSessionToken, TechId.create(group), userIds);

        return groupName;
    }

    /**
     * Authenticates as specified user.
     * 
     * @return session token
     */
    protected String authenticateAs(String user)
    {
        return commonServer.tryAuthenticate(user, "password").getSessionToken();
    }

    protected NewSampleBuilder sample(String identifier)
    {
        return new NewSampleBuilder(identifier);
    }

    protected void uploadFile(String fileName, String fileContent)
    {
        UploadedFilesBean bean = new UploadedFilesBean();
        bean.addMultipartFile(new MockMultipartFile(fileName, fileName, null, fileContent
                .getBytes()));
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY, bean);
    }

    protected <T extends Serializable> List<T> asList(TypedTableResultSet<T> resultSet)
    {
        List<T> list = new ArrayList<T>();
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : resultSet.getResultSet()
                .getList())
        {
            list.add(gridRowModel.getOriginalObject().getObjectOrNull());
        }
        return list;
    }

    protected <T extends CodeWithRegistration<?>> T getOriginalObjectByCode(
            TypedTableResultSet<T> resultSet, String code)
    {
        GridRowModels<TableModelRowWithObject<T>> list = resultSet.getResultSet().getList();
        List<String> codes = new ArrayList<String>();
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : list)
        {
            T originalObject = gridRowModel.getOriginalObject().getObjectOrNull();
            String objectCode = originalObject.getCode();
            if (objectCode.equals(code))
            {
                return originalObject;
            }
            codes.add(objectCode);
        }
        AssertJUnit.fail("No row with code " + code + " found in " + codes);
        return null;
    }

    protected <T extends CodeWithRegistration<?>> void assertObjectWithCodeDoesNotExists(
            TypedTableResultSet<T> resultSet, String code)
    {
        GridRowModels<TableModelRowWithObject<T>> list = resultSet.getResultSet().getList();
        List<String> codes = new ArrayList<String>();
        boolean rowFound = false;
        for (GridRowModel<TableModelRowWithObject<T>> gridRowModel : list)
        {
            T originalObject = gridRowModel.getOriginalObject().getObjectOrNull();
            String objectCode = originalObject.getCode();
            if (objectCode.equals(code))
            {
                rowFound = true;
            }
            codes.add(objectCode);
        }
        if (rowFound)
        {
            AssertJUnit.fail("Row with code " + code + " was found in " + codes);
        }
    }

    protected void assertProperties(String expectedProperties,
            IEntityPropertiesHolder propertiesHolder)
    {
        List<IEntityProperty> properties =
                new ArrayList<IEntityProperty>(propertiesHolder.getProperties());
        Collections.sort(properties, new Comparator<IEntityProperty>()
            {
                @Override
                public int compare(IEntityProperty p1, IEntityProperty p2)
                {
                    return p1.getPropertyType().getCode().compareTo(p2.getPropertyType().getCode());
                }
            });
        assertEquals(expectedProperties, properties.toString());
    }

    protected void assertProperty(IEntityPropertiesHolder propertiesHolder, String key, String value)
    {
        List<IEntityProperty> properties = propertiesHolder.getProperties();
        List<String> propertyCodes = new ArrayList<String>();
        for (IEntityProperty property : properties)
        {
            String code = property.getPropertyType().getCode();
            if (code.equals(key))
            {
                assertEquals("Property " + key, value, property.tryGetAsString());
                return;
            }
            propertyCodes.add(code);
        }
        AssertJUnit.fail("No property " + key + " found in " + propertyCodes);
    }

    protected void assertEntities(String expectedEntities,
            Collection<? extends IIdentifierHolder> entities)
    {
        List<String> identifiers = new ArrayList<String>();
        for (IIdentifierHolder entity : entities)
        {
            identifiers.add(entity.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals(expectedEntities, identifiers.toString());
    }

    protected List<PropertyHistory> getMaterialPropertiesHistory(long materialID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from material_properties_history as h "
                                + " join material_type_property_types as etpt on h.mtpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.mate_id = ?",
                                new HistoryRowMapper(), materialID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getExperimentPropertiesHistory(long experimentID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from experiment_properties_history as h "
                                + " join experiment_type_property_types as etpt on h.etpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.expe_id = ?",
                                new HistoryRowMapper(), experimentID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getSamplePropertiesHistory(long sampleID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from sample_properties_history as h "
                                + " join sample_type_property_types as etpt on h.stpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.samp_id = ?",
                                new HistoryRowMapper(), sampleID);
        sort(list);
        return list;
    }

    protected List<PropertyHistory> getDataSetPropertiesHistory(long dataSetID)
    {
        List<PropertyHistory> list =
                jdbcTemplate
                        .query("select t.code, h.value, h.vocabulary_term, h.material, h.pers_id_author,"
                                + " h.valid_from_timestamp, h.valid_until_timestamp"
                                + " from data_set_properties_history as h "
                                + " join data_set_type_property_types as etpt on h.dstpt_id = etpt.id"
                                + " join property_types as t on etpt.prty_id = t.id where h.ds_id = ?",
                                new HistoryRowMapper(), dataSetID);
        sort(list);
        return list;
    }

    private void sort(List<PropertyHistory> list)
    {
        Collections.sort(list, new Comparator<PropertyHistory>()
            {
                @Override
                public int compare(PropertyHistory o1, PropertyHistory o2)
                {
                    return o1.toString().compareTo(o2.toString());
                }
            });
    }

    protected EntityTypePropertyType<?> getETPT(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
    {
        List<EntityTypePropertyType<?>> etpts =
                commonServer.listEntityTypePropertyTypes(systemSessionToken);
        for (EntityTypePropertyType<?> etpt : etpts)
        {
            if (etpt.getEntityKind().equals(entityKind) == false)
            {
                continue;
            }
            if (etpt.getPropertyType().getCode().equals(propertyTypeCode) == false)
            {
                continue;
            }
            if (etpt.getEntityType().getCode().equals(entityTypeCode))
            {
                return etpt;
            }
        }
        throw new IllegalArgumentException("No assignment for " + entityTypeCode + " with "
                + propertyTypeCode + ".");
    }

}