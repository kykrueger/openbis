/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;

/**
 * Task which feeds a reporting database with recently added/changed Materials.
 * 
 * @author Franz-Josef Elmer
 */
public class MaterialReportingTask implements IMaintenanceTask
{
    @Private
    static final String MAPPING_FILE_KEY = "mapping-file";

    private static final class Column
    {
        private final String name;

        private final int index;

        private DataTypeCode dataTypeCode;

        Column(String name, int index)
        {
            this.name = name;
            this.index = index;
        }
    }

    private enum IndexingSchema
    {
        INSERT(0, 2), UPDATE(-1, 1);

        private final int codeIndex;

        private final int offset;

        private IndexingSchema(int codeIndex, int offset)
        {
            this.codeIndex = codeIndex;
            this.offset = offset;
        }

        public int getCodeIndex(int size)
        {
            return 1 + (size + 1 + codeIndex) % (size + 1);
        }

        public int getPropertyIndexOffset()
        {
            return offset;
        }
    }

    @Private
    static final class MappingInfo
    {
        private final String materialTypeCode;

        private final String tableName;

        private final String codeColumnName;

        private final Map<String, Column> propertyMapping = new LinkedHashMap<String, Column>();

        MappingInfo(String materialTypeCode, String tableName, String codeColumnName)
        {
            this.materialTypeCode = materialTypeCode;
            this.tableName = tableName;
            this.codeColumnName = codeColumnName;
        }

        String getTableName()
        {
            return tableName;
        }

        String getCodeColumnName()
        {
            return codeColumnName;
        }

        boolean hasProperties()
        {
            return propertyMapping.isEmpty() == false;
        }

        void addPropertyMapping(String propertyTypeCode, String propertyColumnName)
        {
            Column column = new Column(propertyColumnName, propertyMapping.size());
            propertyMapping.put(propertyTypeCode, column);
        }

        void injectDataTypeCodes(Map<String, DataTypeCode> columns)
        {
            DataTypeCode codeColumnType = columns.remove(codeColumnName);
            if (codeColumnType == null)
            {
                throw new EnvironmentFailureException("Missing column '" + codeColumnName
                        + "' in table '" + tableName + "' of report database.");
            }
            if (codeColumnType.equals(DataTypeCode.VARCHAR) == false)
            {
                throw new EnvironmentFailureException("Column '" + codeColumnName + "' of table '"
                        + tableName + "' is not of type VARCHAR.");
            }
            Collection<Column> values = propertyMapping.values();
            for (Column column : values)
            {
                DataTypeCode dataTypeCode = columns.get(column.name);
                if (dataTypeCode == null)
                {
                    throw new EnvironmentFailureException("Missing column '" + column.name
                            + "' in table '" + tableName + "' of report database.");
                }
                column.dataTypeCode = dataTypeCode;
            }
        }

        Map<String, Map<String, Object>> groupByMaterials(List<Map<String, Object>> rows)
        {
            HashMap<String, Map<String, Object>> result =
                    new HashMap<String, Map<String, Object>>();
            for (Map<String, Object> map : rows)
            {
                String materialCode = map.remove(codeColumnName).toString();
                result.put(materialCode, map);
            }
            return result;
        }

        String createSelectStatement(List<Material> materials)
        {
            StringBuilder builder = new StringBuilder("select * from ");
            builder.append(tableName).append(" where ");
            builder.append(codeColumnName).append(" in ");
            String delim = "(";
            for (Material material : materials)
            {
                builder.append(delim).append('\'').append(material.getCode()).append('\'');
                delim = ", ";
            }
            builder.append(")");
            return builder.toString();
        }

        String createInsertStatement()
        {
            StringBuilder builder = new StringBuilder("insert into ").append(tableName);
            builder.append(" (").append(codeColumnName);
            for (Column column : propertyMapping.values())
            {
                builder.append(", ").append(column.name);
            }
            builder.append(") values(?");
            for (int i = 0; i < propertyMapping.size(); i++)
            {
                builder.append(", ?");
            }
            builder.append(")");
            return builder.toString();
        }

        String createUpdateStatement()
        {
            StringBuilder builder = new StringBuilder("update ").append(tableName);
            String delim = " set ";
            for (Column column : propertyMapping.values())
            {
                builder.append(delim).append(column.name).append("=?");
                delim = ", ";
            }
            builder.append(" where ").append(codeColumnName).append("=?");
            return builder.toString();
        }

        BatchPreparedStatementSetter createSetter(final List<Material> materials,
                final IndexingSchema indexing)
        {
            return new BatchPreparedStatementSetter()
                {
                    public void setValues(PreparedStatement ps, int index) throws SQLException
                    {
                        Material material = materials.get(index);
                        List<IEntityProperty> properties = material.getProperties();
                        ps.setObject(indexing.getCodeIndex(propertyMapping.size()),
                                material.getCode());
                        int propertyIndexOffset = indexing.getPropertyIndexOffset();
                        for (int i = 0; i < propertyMapping.size(); i++)
                        {
                            ps.setObject(i + propertyIndexOffset, null);
                        }
                        for (IEntityProperty property : properties)
                        {
                            PropertyType propertyType = property.getPropertyType();
                            String code = propertyType.getCode();
                            Column column = propertyMapping.get(code);
                            if (column != null)
                            {
                                String value = getValue(property);
                                Serializable typedValue =
                                        DataTypeUtils.convertValueTo(column.dataTypeCode, value);
                                if (typedValue instanceof Date)
                                {
                                    ps.setObject(column.index + propertyIndexOffset, typedValue,
                                            Types.TIMESTAMP);
                                } else
                                {
                                    ps.setObject(column.index + propertyIndexOffset, typedValue);
                                }
                            }
                        }
                    }

                    public int getBatchSize()
                    {
                        return materials.size();
                    }
                };
        }

        private String getValue(IEntityProperty property)
        {
            String value;
            switch (property.getPropertyType().getDataType().getCode())
            {
                case CONTROLLEDVOCABULARY:
                    value = property.getVocabularyTerm().getCodeOrLabel();
                    break;
                case MATERIAL:
                    value = property.getMaterial().getCode();
                    break;
                default:
                    value = property.getValue();
            }
            return value;
        }

    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaterialReportingTask.class);

    private final ICommonServerForInternalUse server;

    private Map<String, MappingInfo> mapping;

    private SimpleDatabaseConfigurationContext dbConfigurationContext;

    private JdbcTemplate jdbcTemplate;

    public MaterialReportingTask()
    {
        this(CommonServiceProvider.getCommonServer());
    }

    public MaterialReportingTask(ICommonServerForInternalUse server)
    {
        this.server = server;
    }

    public void setUp(String pluginName, Properties properties)
    {
        dbConfigurationContext = new SimpleDatabaseConfigurationContext(properties);
        // String readTimestampSql = PropertyUtils.getMandatoryProperty(properties,
        // "read-timestamp-sql");
        // String writeTimestampSql = PropertyUtils.getMandatoryProperty(properties,
        // "write-timestamp-sql");
        String mappingFileName = PropertyUtils.getMandatoryProperty(properties, MAPPING_FILE_KEY);
        mapping = readMappingFile(mappingFileName);
        Map<String, Map<String, DataTypeCode>> metaData = retrieveDatabaseMetaData();
        for (MappingInfo mappingInfo : mapping.values())
        {
            String tableName = mappingInfo.getTableName();
            Map<String, DataTypeCode> columns = metaData.get(tableName);
            if (columns == null)
            {
                throw new EnvironmentFailureException("Missing table '" + tableName
                        + "' in report database.");
            }
            mappingInfo.injectDataTypeCodes(columns);
        }
        jdbcTemplate = new JdbcTemplate(dbConfigurationContext.getDataSource());
    }

    private Map<String, Map<String, DataTypeCode>> retrieveDatabaseMetaData()
    {
        Collection<MappingInfo> values = mapping.values();
        final Set<String> tableNames = new HashSet<String>();
        for (MappingInfo mappingInfo : values)
        {
            tableNames.add(mappingInfo.getTableName());
        }
        try
        {
            final Map<String, Map<String, DataTypeCode>> map =
                    new HashMap<String, Map<String, DataTypeCode>>();
            JdbcUtils.extractDatabaseMetaData(dbConfigurationContext.getDataSource(),
                    new DatabaseMetaDataCallback()
                        {

                            public Object processMetaData(DatabaseMetaData metaData)
                                    throws SQLException, MetaDataAccessException
                            {
                                ResultSet rs = metaData.getColumns(null, null, null, null);
                                while (rs.next())
                                {
                                    String tableName = rs.getString("TABLE_NAME").toLowerCase();
                                    if (tableNames.contains(tableName))
                                    {
                                        Map<String, DataTypeCode> columns = map.get(tableName);
                                        if (columns == null)
                                        {
                                            columns = new TreeMap<String, DataTypeCode>();
                                            map.put(tableName, columns);
                                        }
                                        String columnName =
                                                rs.getString("COLUMN_NAME").toLowerCase();
                                        DataTypeCode dataTypeCode =
                                                DataTypeUtils.getDataTypeCode(rs
                                                        .getInt("DATA_TYPE"));
                                        columns.put(columnName, dataTypeCode);
                                    }
                                }
                                return null;
                            }
                        });
            return map;
        } catch (MetaDataAccessException ex)
        {
            throw new ConfigurationFailureException("Couldn't retrieve meta data of database.", ex);
        }
    }

    public void execute()
    {
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            return;
        }
        String sessionToken = contextOrNull.getSessionToken();

        Map<String, List<Material>> materialsByType =
                getRecentlyAddedOrChangedMaterials(sessionToken);
        for (Entry<String, List<Material>> entry : materialsByType.entrySet())
        {
            String materialTypeCode = entry.getKey();
            final List<Material> materials = entry.getValue();
            MappingInfo mappingInfo = mapping.get(materialTypeCode);
            if (mappingInfo != null)
            {
                addOrUpdate(mappingInfo, materials);
                operationLog.info(materials.size() + " materials of type " + materialTypeCode
                        + " reported.");
            }
        }
    }

    private void addOrUpdate(MappingInfo mappingInfo, final List<Material> materials)
    {
        String sql = mappingInfo.createSelectStatement(materials);
        List<Map<String, Object>> rows = retrieveRowsToBeUpdated(sql);
        Map<String, Map<String, Object>> reportedMaterials = mappingInfo.groupByMaterials(rows);
        List<Material> newMaterials = new ArrayList<Material>();
        List<Material> updateMaterials = new ArrayList<Material>();
        for (Material material : materials)
        {
            Map<String, Object> reportedMaterial = reportedMaterials.get(material.getCode());
            if (reportedMaterial != null)
            {
                updateMaterials.add(material);
            } else
            {
                newMaterials.add(material);
            }
        }
        if (updateMaterials.isEmpty() == false && mappingInfo.hasProperties())
        {
            String updateStatement = mappingInfo.createUpdateStatement();
            jdbcTemplate.batchUpdate(updateStatement,
                    mappingInfo.createSetter(updateMaterials, IndexingSchema.UPDATE));
        }
        if (newMaterials.isEmpty() == false)
        {
            String insertStatement = mappingInfo.createInsertStatement();
            jdbcTemplate.batchUpdate(insertStatement,
                    mappingInfo.createSetter(newMaterials, IndexingSchema.INSERT));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> retrieveRowsToBeUpdated(String sql)
    {
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, new ColumnMapRowMapper()
            {
                @Override
                protected String getColumnKey(String columnName)
                {
                    return columnName.toLowerCase();
                }
            });
        return rows;
    }

    private Map<String, List<Material>> getRecentlyAddedOrChangedMaterials(String sessionToken)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        DetailedSearchCriterion criterion =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(MaterialAttributeSearchFieldKind.MODIFICATION_DATE),
                        CompareType.MORE_THAN_OR_EQUAL, readTimestamp(), "0");
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        List<Material> materials = server.searchForMaterials(sessionToken, criteria);
        Map<String, List<Material>> result = new TreeMap<String, List<Material>>();
        for (Material material : materials)
        {
            String typeCode = material.getMaterialType().getCode();
            List<Material> list = result.get(typeCode);
            if (list == null)
            {
                list = new ArrayList<Material>();
                result.put(typeCode, list);
            }
            list.add(material);
        }
        return result;
    }

    private String readTimestamp()
    {
        return "2012-02-22 10:33:44.6667";
    }

    @Private
    static Map<String, MappingInfo> readMappingFile(String mappingFileName)
    {
        Map<String, MaterialReportingTask.MappingInfo> map =
                new HashMap<String, MaterialReportingTask.MappingInfo>();
        List<String> lines = FileUtilities.loadToStringList(new File(mappingFileName));
        MappingInfo currentMappingInfo = null;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            ExecptionFactory factory = new ExecptionFactory(mappingFileName, line, i);
            if (line.startsWith("#") || line.trim().length() == 0)
            {
                continue;
            }
            if (line.startsWith("["))
            {
                if (line.endsWith("]") == false)
                {
                    throw factory.exception("Missing ']'");
                }
                String[] splittedLine =
                        splitAndCheck(line.substring(0, line.length() - 1).substring(1), ":", 2,
                                factory);
                String materialTypeCode =
                        trimAndCheck(splittedLine[0], factory, "material type code");
                splittedLine = splitAndCheck(splittedLine[1], ",", 2, factory);
                String tableName = trimAndCheck(splittedLine[0], factory, "table name");
                String codeColumnName = trimAndCheck(splittedLine[1], factory, "code column name");
                currentMappingInfo =
                        new MappingInfo(materialTypeCode, tableName.toLowerCase(),
                                codeColumnName.toLowerCase());
                map.put(materialTypeCode, currentMappingInfo);
            } else if (currentMappingInfo != null)
            {
                String[] splittedLine = splitAndCheck(line, ":", 2, factory);
                String propertyTypeCode =
                        trimAndCheck(splittedLine[0], factory, "property type code");
                currentMappingInfo.addPropertyMapping(propertyTypeCode, splittedLine[1].trim()
                        .toLowerCase());
            } else
            {
                throw factory.exception("Missing first material type table definition of form "
                        + "'[<material type tode>: <table name>, <code column name>]'");
            }
        }
        return map;
    }

    private static String[] splitAndCheck(String string, String delimiter, int numberOfItems,
            ExecptionFactory factory)
    {
        String[] splittedString = string.split(delimiter);
        if (splittedString.length < numberOfItems)
        {
            throw factory.exception(numberOfItems + " items separated by '" + delimiter
                    + "' expected.");
        }
        return splittedString;
    }

    private static String trimAndCheck(String string, ExecptionFactory factory, String name)
    {
        String trimmedString = string.trim();
        if (trimmedString.length() == 0)
        {
            throw factory.exception("Missing " + name + ".");
        }
        return trimmedString;
    }

    private static final class ExecptionFactory
    {
        private final String mappingFileName;

        private final String line;

        private final int lineIndex;

        ExecptionFactory(String mappingFileName, String line, int lineIndex)
        {
            this.mappingFileName = mappingFileName;
            this.line = line;
            this.lineIndex = lineIndex;
        }

        ConfigurationFailureException exception(String message)
        {
            return new ConfigurationFailureException("Error in mapping file '" + mappingFileName
                    + "' at line " + (lineIndex + 1) + " '" + line + "': " + message);
        }
    }

    @Private
    void closeDatabaseConnections()
    {
        if (dbConfigurationContext != null)
        {
            dbConfigurationContext.closeConnections();
        }
    }

}
