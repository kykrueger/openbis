package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchCriteriaTranslator
{
    public static final String RANK_ALIAS = "rank";

    public static final String IDENTIFIER_ALIAS = "identifier";

    public static final String OBJECT_KIND_ALIAS = "object_kind";

    public static final String CODE_MATCH_ALIAS = "code_match";

    public static final String PERM_ID_MATCH_ALIAS = "perm_id_match";

    public static final String DATA_SET_KIND_MATCH_ALIAS = "data_set_kind_match";

    private static final String REG_CONFIG = "simple";

    private static final String SEARCH_STRING_ALIAS = "search_string";

    private static final String VALUE_HEADLINE_ALIAS = "value_headline";

    private static final String CODE_HEADLINE_ALIAS = "code_headline";

    private static final String LABEL_HEADLINE_ALIAS = "label_headline";

    private static final String DESCRIPTION_HEADLINE_ALIAS = "label_headline";

    private static final String PROPERTIES_TABLE_ALIAS = "prop";

    private static final String CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS = "cvte";

    private static final String SPACE_TABLE_ALIAS = "space";

    private static final String PROJECT_TABLE_ALIAS = "proj";

//    private static final String START_SEL = "<{(";
//
//    private static final String STOP_SEL = ")}>";

//    private static final String TS_HEADLINE_OPTIONS = "HighlightAll=TRUE, StartSel=" + START_SEL
//            +", StopSel=" + STOP_SEL;

    private GlobalSearchCriteriaTranslator()
    {
        throw new UnsupportedOperationException();
    }

    public static SelectQuery translate(final TranslationVo vo)
    {
        if (vo.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final StringBuilder sqlBuilder = new StringBuilder(LP);
        final Spliterator<ISearchCriteria> spliterator = vo.getCriteria().stream().spliterator();
        if (spliterator.tryAdvance((criterion) -> translateCriterion(sqlBuilder, vo, (GlobalSearchTextCriteria) criterion)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) -> {
                sqlBuilder.append(RP).append(NL).append(UNION).append(NL).append(LP).append(NL);
                translateCriterion(sqlBuilder, vo, (GlobalSearchTextCriteria) criterion);
            });
        }
        sqlBuilder.append(RP);

        return new SelectQuery(sqlBuilder.toString(), vo.getArgs());
    }

    private static void translateCriterion(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion)
    {
        buildSelect(sqlBuilder, vo, criterion, true);
        buildFrom(sqlBuilder, vo, criterion, true);
        buildWhere(sqlBuilder, vo, criterion, true);

        sqlBuilder.append(UNION).append(NL);

        buildSelect(sqlBuilder, vo, criterion, false);
        buildFrom(sqlBuilder, vo, criterion, false);
        buildWhere(sqlBuilder, vo, criterion, false);
    }

    private static void buildSelect(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final Object value = criterion.getFieldValue().getValue();
        final List<Object> args = vo.getArgs();

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);

        sqlBuilder.append(SELECT).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(COMMA).append(SP);

        switch (tableMapper)
        {
            case SAMPLE:
                // Falls through
            case EXPERIMENT:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(COMMA).append(SP);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN).append(COMMA).append(SP);
                break;
            }
        }

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(COMMA).append(SP)
                .append(SQ).append(tableMapper.getEntityKind()).append(SQ).append(SP).append(OBJECT_KIND_ALIAS).append(COMMA).append(SP);

        buildFullIdentifierConcatenationString(sqlBuilder, hasSpaces || hasProjects ? SPACE_TABLE_ALIAS : null,
                hasProjects ? PROJECT_TABLE_ALIAS : null,
                (tableMapper == TableMapper.SAMPLE) ? MAIN_TABLE_ALIAS : null);
        sqlBuilder.append(IDENTIFIER_ALIAS).append(COMMA).append(NL);

        if (forAttributes)
        {
            sqlBuilder.append(1).append(DOUBLE_COLON).append(FLOAT_4).append(SP).append(RANK_ALIAS).append(COMMA).append(NL);

            buildAttributesMatchSelection(sqlBuilder, criterion, vo.getTableMapper(), args);
            sqlBuilder.append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(VALUE_HEADLINE_ALIAS).append(COMMA).append(SP);
            sqlBuilder.append(NULL).append(SP).append(CODE_HEADLINE_ALIAS).append(COMMA).append(SP);
            sqlBuilder.append(NULL).append(SP).append(LABEL_HEADLINE_ALIAS).append(COMMA).append(SP);
            sqlBuilder.append(NULL).append(SP).append(DESCRIPTION_HEADLINE_ALIAS);
        } else
        {
            buildTsRank(sqlBuilder, PROPERTIES_TABLE_ALIAS, CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS, value, args);
            sqlBuilder.append(RANK_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(CODE_MATCH_ALIAS).append(COMMA).append(NL);
            switch (tableMapper)
            {
                case SAMPLE:
                    // Falls through
                case EXPERIMENT:
                {
                    sqlBuilder.append(NULL).append(SP).append(PERM_ID_MATCH_ALIAS).append(COMMA).append(NL);
                    break;
                }
                case DATA_SET:
                {
                    sqlBuilder.append(NULL).append(SP).append(DATA_SET_KIND_MATCH_ALIAS).append(COMMA).append(NL);
                    break;
                }
            }

            buildTsHeadline(sqlBuilder, value, args, PROPERTIES_TABLE_ALIAS + PERIOD + VALUE_COLUMN, VALUE_HEADLINE_ALIAS);
            sqlBuilder.append(COMMA).append(SP);
            buildTsHeadline(sqlBuilder, value, args, CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + CODE_COLUMN, CODE_HEADLINE_ALIAS);
            sqlBuilder.append(COMMA).append(SP);
            buildTsHeadline(sqlBuilder, value, args, CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + LABEL_COLUMN, LABEL_HEADLINE_ALIAS);
            sqlBuilder.append(COMMA).append(SP);
            buildTsHeadline(sqlBuilder, value, args, CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + DESCRIPTION_COLUMN,
                    DESCRIPTION_HEADLINE_ALIAS);
        }

        sqlBuilder.append(NL);
    }

    private static void buildTsHeadline(final StringBuilder sqlBuilder, final Object value, final List<Object> args, final String field,
            final String alias)
    {
        sqlBuilder.append(TS_HEADLINE).append(LP).append(field).append(COMMA).append(SP);
        buildTsQueryPart(sqlBuilder, value, args);
        sqlBuilder.append(RP).append(SP).append(alias);
    }

    /**
     * Appends condition for the attributes that they may match.
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param criterion the full text search criterion.
     * @param tableMapper the table mapper.
     * @param args query arguments.
     */
    private static void buildAttributesMatchCondition(final StringBuilder sqlBuilder, final GlobalSearchTextCriteria criterion,
            final TableMapper tableMapper, final List<Object> args)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);

        switch (tableMapper)
        {
            case SAMPLE:
                // Falls through
            case EXPERIMENT:
            {
                sqlBuilder.append(SP).append(OR).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(SP).append(OR).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN);
                TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);
                break;
            }
        }
    }

    /**
     * Appends selection text for the attributes that they may match.
     *
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param criterion the full text search criterion.
     * @param tableMapper the table mapper.
     * @param args query arguments.
     */
    private static void buildAttributesMatchSelection(final StringBuilder sqlBuilder, final GlobalSearchTextCriteria criterion,
            final TableMapper tableMapper, final List<Object> args)
    {
        sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);
        sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
        sqlBuilder.append(SP).append(CODE_MATCH_ALIAS);

        switch (tableMapper)
        {
            case SAMPLE:
                // Falls through
            case EXPERIMENT:
            {
                sqlBuilder.append(COMMA).append(NL);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);
                sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
                sqlBuilder.append(SP).append(PERM_ID_MATCH_ALIAS);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(COMMA).append(NL);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN);
                TranslatorUtils.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder, args);
                sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN);
                sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
                sqlBuilder.append(SP).append(DATA_SET_KIND_MATCH_ALIAS);
                break;
            }
        }
    }

    private static void buildTsRank(final StringBuilder sqlBuilder, final String alias1, final String alias2, final Object value, final List<Object> args)
    {
        sqlBuilder.append(TS_RANK).append(LP).append(COALESCE).append(LP)
                .append(alias1).append(PERIOD).append(TSVECTOR_DOCUMENT).append(COMMA).append(SP).append(SQ).append(SQ).append(RP)
                .append(SP).append(BARS).append(SP)
                .append(COALESCE).append(LP).append(alias2).append(PERIOD).append(TSVECTOR_DOCUMENT).append(COMMA).append(SP).append(SQ).append(SQ)
                .append(RP)
                .append(COMMA).append(SP);
        buildTsQueryPart(sqlBuilder, value, args);
        sqlBuilder.append(RP);
    }

    private static void buildFrom(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = vo.getTableMapper();

        final String entitiesTable = tableMapper.getEntitiesTable();
        final String projectsTableName = TableMapper.PROJECT.getEntitiesTable();

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);

        if (!forAttributes)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getValuesTable()).append(SP).append(PROPERTIES_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ)
                    .append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(tableMapper.getValuesTableEntityIdField()).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableNames.CONTROLLED_VOCABULARY_TERM_TABLE).append(SP)
                    .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(SP).append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(VOCABULARY_TERM_COLUMN).append(SP).append(EQ).append(SP).append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD)
                    .append(ID_COLUMN).append(NL);
        }

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);

        if (hasProjects)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(projectsTableName).append(SP).append(PROJECT_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(EQ).append(SP)
                    .append(PROJECT_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            if (!hasSpaces)
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SPACE.getEntitiesTable()).append(SP).append(SPACE_TABLE_ALIAS).append(SP)
                        .append(ON).append(SP).append(PROJECT_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(EQ).append(SP)
                        .append(SPACE_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            }
        }

        if (hasSpaces)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SPACE.getEntitiesTable()).append(SP).append(SPACE_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(EQ).append(SP)
                    .append(SPACE_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
        }
    }

    private static boolean hasProjects(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return TableMapper.SAMPLE.getEntitiesTable().equals(entitiesTable) || TableMapper.EXPERIMENT.getEntitiesTable().equals(entitiesTable);
    }

    private static boolean hasSpaces(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return TableMapper.SAMPLE.getEntitiesTable().equals(entitiesTable) || TableMapper.PROJECT.getEntitiesTable().equals(entitiesTable);
    }

    private static void buildWhere(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final Object value = criterion.getFieldValue().getValue();
        final List<Object> args = vo.getArgs();

        sqlBuilder.append(WHERE).append(SP);
        if (forAttributes)
        {
            buildAttributesMatchCondition(sqlBuilder, criterion, vo.getTableMapper(), args);
        } else
        {
            buildTsVectorMatch(sqlBuilder, value, args);
        }
        sqlBuilder.append(NL);
    }

    private static void buildTsVectorMatch(final StringBuilder sqlBuilder, final Object value, final List<Object> args)
    {
        sqlBuilder.append(COALESCE).append(LP).append(GlobalSearchCriteriaTranslator.PROPERTIES_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN)
                .append(COMMA).append(SP).append(SQ).append(SQ).append(RP)
                .append(SP).append(BARS).append(SP)
                .append(COALESCE).append(LP).append(GlobalSearchCriteriaTranslator.CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD)
                .append(TS_VECTOR_COLUMN).append(COMMA).append(SP).append(SQ).append(SQ).append(RP)
                .append(SP).append(DOUBLE_AT).append(SP);
        buildTsQueryPart(sqlBuilder, value, args);
    }

    private static void buildTsQueryPart(final StringBuilder sqlBuilder, final Object value, final List<Object> args)
    {
        sqlBuilder.append(PLAINTO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP);
        args.add(value);
    }

}
