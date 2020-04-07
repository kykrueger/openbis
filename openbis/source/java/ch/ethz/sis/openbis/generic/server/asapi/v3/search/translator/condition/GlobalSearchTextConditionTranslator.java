package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.VARCHAR;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.TS_VECTOR_COLUMN;

public class GlobalSearchTextConditionTranslator implements IConditionTranslator<GlobalSearchTextCriteria>
{
    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final GlobalSearchTextCriteria criterion, final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final GlobalSearchTextCriteria criterion, final TableMapper tableMapper, final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ANY_FIELD:
            {
                final String criterionFieldName = criterion.getFieldName();
                final AbstractStringValue value = criterion.getFieldValue();
                normalizeValue(value, TS_VECTOR_COLUMN);

                TranslatorUtils.translateStringComparison(CriteriaTranslator.MAIN_TABLE_ALIAS, TS_VECTOR_COLUMN, value, VARCHAR, sqlBuilder, args);
                break;
            }

            case ATTRIBUTE:
            case PROPERTY:
            case ANY_PROPERTY:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    private static void normalizeValue(final AbstractStringValue value, final String columnName)
    {
        if (columnName.equals(ColumnNames.CODE_COLUMN) && value.getValue().startsWith("/"))
        {
            value.setValue(value.getValue().substring(1));
        }
    }

}
