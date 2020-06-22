package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKindCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchWildCardsCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildTypeCodeIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchCriteriaTranslator
{
    public static final String START_SEL = "<SEL--";

    public static final String STOP_SEL = "--SEL>";

    public static final String RANK_ALIAS = "rank";

    public static final String IDENTIFIER_ALIAS = "identifier";

    public static final String OBJECT_KIND_ALIAS = "object_kind";

    public static final String SPACE_CODE_ALIAS = "space_code";

    public static final String CODE_MATCH_ALIAS = "code_match";

    public static final String PERM_ID_MATCH_ALIAS = "perm_id_match";

    public static final String DATA_SET_KIND_MATCH_ALIAS = "data_set_kind_match";

    public static final String ENTITY_TYPES_CODE_ALIAS = "enty_code";

    public static final String PROPERTY_TYPE_LABEL_ALIAS = "property_type_label";

    public static final String CV_CODE_ALIAS = "cv_code";

    public static final String CV_LABEL_ALIAS = "cv_label";

    public static final String CV_DESCRIPTION_ALIAS = "cv_description";

    public static final String PROPERTY_VALUE_ALIAS = "property_value";

    public static final String VALUE_HEADLINE_ALIAS = "value_headline";

    public static final String LABEL_HEADLINE_ALIAS = "label_headline";

    public static final String CODE_HEADLINE_ALIAS = "code_headline";

    public static final String DESCRIPTION_HEADLINE_ALIAS = "description_headline";

    public static final String VALUE_MATCH_RANK_ALIAS = "value_match_rank";

    public static final String CODE_MATCH_RANK_ALIAS = "code_match_rank";

    public static final String LABEL_MATCH_RANK_ALIAS = "label_match_rank";
    
    public static final String DESCRIPTION_MATCH_RANK_ALIAS = "description_match_rank";

    private static final String REG_CONFIG = "english";

    private static final String PROPERTIES_TABLE_ALIAS = "prop";

    private static final String ENTITY_TYPES_TABLE_ALIAS = "enty";

    private static final String CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS = "cvte";

    private static final String SPACE_TABLE_ALIAS = "space";

    private static final String PROJECT_TABLE_ALIAS = "proj";

    private static final String CONTAINER_TABLE_ALIAS = "cont";

    private static final String ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS = "etpt";

    private static final String ATTRIBUTE_TYPES_TABLE_ALIAS = "prty";

    private static final String TS_HEADLINE_OPTIONS = "HighlightAll=TRUE, StartSel=" + START_SEL
            +", StopSel=" + STOP_SEL;

    /** Rank for ID matches. */
    private static final float ID_RANK = 10.0f;

    /** Rank for other than ID attribute matches. */
    private static final float ATTRIBUTE_RANK = 0.1f;

    /** Rank for ID properties. */
    private static final float ID_PROPERTY_RANK = 1f;

    private static final Logger LOG = LogFactory.getLogger(LogCategory.OPERATION, GlobalSearchCriteriaTranslator.class);

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

        final boolean withWildcards = vo.getCriteria().stream().anyMatch((criterion) -> criterion instanceof GlobalSearchWildCardsCriteria);
        if (withWildcards)
        {
            LOG.warn("Full text search with wildcards is not supported.");
        }

        final StringBuilder sqlBuilder = new StringBuilder(LP);
        final Spliterator<ISearchCriteria> spliterator = vo.getCriteria().stream()
                .filter((criterion) -> !(criterion instanceof GlobalSearchWildCardsCriteria)
                        && !(criterion instanceof GlobalSearchObjectKindCriteria)).spliterator();

        if (spliterator.tryAdvance((criterion) -> translateCriterion(sqlBuilder, vo, criterion)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) ->
            {
                sqlBuilder.append(RP).append(NL).append(UNION).append(NL).append(LP).append(NL);
                translateCriterion(sqlBuilder, vo, criterion);
            });
        }
        sqlBuilder.append(RP);

        return new SelectQuery(sqlBuilder.toString(), vo.getArgs());
    }

    private static void translateCriterion(final StringBuilder sqlBuilder, final TranslationVo vo,
            final ISearchCriteria criterion)
    {
        if (criterion instanceof GlobalSearchTextCriteria)
        {
            final GlobalSearchTextCriteria globalSearchTextCriterion = (GlobalSearchTextCriteria) criterion;

            // Fields
            buildSelect(sqlBuilder, vo, globalSearchTextCriterion, true);
            buildFrom(sqlBuilder, vo, globalSearchTextCriterion, true);
            buildWhere(sqlBuilder, vo, globalSearchTextCriterion, true);

            sqlBuilder.append(UNION).append(NL);

            // Properties
            buildSelect(sqlBuilder, vo, globalSearchTextCriterion, false);
            buildFrom(sqlBuilder, vo, globalSearchTextCriterion, false);
            buildWhere(sqlBuilder, vo, globalSearchTextCriterion, false);
        }
    }

    private static void buildSelect(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final AbstractStringValue stringValue = criterion.getFieldValue();
        final List<Object> args = vo.getArgs();

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);

        sqlBuilder.append(SELECT).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(COMMA).append(SP);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(COMMA).append(SP);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN).append(COMMA)
                        .append(SP);
                break;
            }
            case MATERIAL:
            {
                sqlBuilder.append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                        .append(ENTITY_TYPES_CODE_ALIAS).append(COMMA).append(SP);
                break;
            }
        }

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(COMMA).append(SP)
                .append(SQ).append(tableMapper.getEntityKind()).append(SQ).append(SP).append(OBJECT_KIND_ALIAS).append(COMMA).append(SP);

        sqlBuilder.append(UPPER).append(LP);
        if (tableMapper == TableMapper.MATERIAL)
        {
            buildTypeCodeIdentifierConcatenationString(sqlBuilder, ENTITY_TYPES_TABLE_ALIAS);
        } else
        {
            buildFullIdentifierConcatenationString(sqlBuilder, hasSpaces || hasProjects ? SPACE_TABLE_ALIAS : null,
                    hasProjects ? PROJECT_TABLE_ALIAS : null,
                    (tableMapper == SAMPLE) ? CONTAINER_TABLE_ALIAS : null);
        }
        sqlBuilder.append(RP).append(SP).append(IDENTIFIER_ALIAS).append(COMMA).append(NL);

        if (hasSpaces || hasProjects)
        {
            sqlBuilder.append(SPACE_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        } else
        {
            sqlBuilder.append(NULL);
        }
        sqlBuilder.append(SP).append(SPACE_CODE_ALIAS).append(COMMA).append(NL);

        if (forAttributes)
        {
            final float attributesRank = (tableMapper == SAMPLE || tableMapper == EXPERIMENT)
                    ? ID_RANK : ATTRIBUTE_RANK;
            sqlBuilder.append(attributesRank).append(DOUBLE_COLON).append(FLOAT_4).append(SP).append(RANK_ALIAS)
                    .append(COMMA).append(NL);

            buildAttributesMatchSelection(sqlBuilder, criterion, vo.getTableMapper(), args);
            sqlBuilder.append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(PROPERTY_TYPE_LABEL_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(PROPERTY_VALUE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_CODE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_DESCRIPTION_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(VALUE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CODE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(LABEL_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(DESCRIPTION_HEADLINE_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(0).append(SP).append(VALUE_MATCH_RANK_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(0).append(SP).append(CODE_MATCH_RANK_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(0).append(SP).append(LABEL_MATCH_RANK_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(0).append(SP).append(DESCRIPTION_MATCH_RANK_ALIAS);
        } else
        {
            sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP)
                    .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                    .append(EQ).append(SP).append(QU);
            args.add(stringValue.getValue());
            sqlBuilder.append(SP).append(THEN).append(SP).append(ID_PROPERTY_RANK).append(DOUBLE_COLON)
                    .append(FLOAT_4).append(SP);
            sqlBuilder.append(ELSE).append(SP);
            buildTsRank(sqlBuilder, stringValue, args);
            sqlBuilder.append(SP).append(END).append(SP).append(RANK_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(CODE_MATCH_ALIAS).append(COMMA).append(NL);
            switch (tableMapper)
            {
                case SAMPLE:
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

            sqlBuilder.append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(LABEL_COLUMN).append(SP)
                    .append(PROPERTY_TYPE_LABEL_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(VALUE_COLUMN).append(SP)
                    .append(PROPERTY_VALUE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                    .append(CV_CODE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(LABEL_COLUMN).append(SP)
                    .append(CV_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(DESCRIPTION_COLUMN)
                    .append(SP).append(CV_DESCRIPTION_ALIAS).append(COMMA).append(NL);

            final boolean useHeadline = vo.getGlobalSearchObjectFetchOptions().hasMatch();
            buildTsHeadline(sqlBuilder, stringValue, args, PROPERTIES_TABLE_ALIAS + PERIOD + VALUE_COLUMN,
                    VALUE_HEADLINE_ALIAS, useHeadline);
            sqlBuilder.append(COMMA).append(NL);
            buildTsHeadline(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + CODE_COLUMN, CODE_HEADLINE_ALIAS,
                    useHeadline);
            sqlBuilder.append(COMMA).append(NL);
            buildTsHeadline(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + LABEL_COLUMN, LABEL_HEADLINE_ALIAS,
                    useHeadline);
            sqlBuilder.append(COMMA).append(NL);
            buildTsHeadline(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + DESCRIPTION_COLUMN, DESCRIPTION_HEADLINE_ALIAS,
                    useHeadline);
            sqlBuilder.append(COMMA).append(NL);

            buildHeadlineTsRank(sqlBuilder, stringValue, args, PROPERTIES_TABLE_ALIAS + PERIOD + VALUE_COLUMN,
                    VALUE_MATCH_RANK_ALIAS);
            sqlBuilder.append(COMMA).append(NL);
            buildHeadlineTsRank(sqlBuilder, stringValue, args, CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + CODE_COLUMN,
                    CODE_MATCH_RANK_ALIAS);
            sqlBuilder.append(COMMA).append(NL);
            buildHeadlineTsRank(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + LABEL_COLUMN, LABEL_MATCH_RANK_ALIAS);
            sqlBuilder.append(COMMA).append(NL);
            buildHeadlineTsRank(sqlBuilder, stringValue, args,
                    CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + DESCRIPTION_COLUMN,
                    DESCRIPTION_MATCH_RANK_ALIAS);
        }

        sqlBuilder.append(NL);
    }

    private static void buildHeadlineTsRank(final StringBuilder sqlBuilder, final AbstractStringValue stringValue, final List<Object> args,
            final String field, final String alias)
    {
        sqlBuilder.append(COALESCE).append(LP).append(TS_RANK).append(LP)
                .append(TO_TSVECTOR).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(field).append(RP).append(COMMA).append(SP)
                .append(TO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP).append(RP).append(COMMA).append(SP).append(0).append(RP)
                .append(SP).append(alias);
        args.add(toTsQueryText(stringValue));
    }

    private static void buildTsHeadline(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args, final String field, final String alias, final boolean useHeadline)
    {
        if (useHeadline)
        {
            sqlBuilder.append(TS_HEADLINE).append(LP).append(field).append(COMMA).append(SP);
            buildTsQueryPart(sqlBuilder, stringValue, args);
            sqlBuilder.append(COMMA).append(SP).append(SQ).append(TS_HEADLINE_OPTIONS).append(SQ);
            sqlBuilder.append(RP);
        } else
        {
            sqlBuilder.append(NULL);
        }
        sqlBuilder.append(SP).append(alias);
    }

    /**
     * Appends condition for the attributes that they may match.
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param criterion the full text search criterion.
     * @param args query arguments.
     */
    private static void buildAttributesMatchCondition(final StringBuilder sqlBuilder,
            final GlobalSearchTextCriteria criterion, final List<Object> args)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN).append(SP).append(DOUBLE_AT)
                .append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
        args.add(toTsQueryText(criterion.getFieldValue()));
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
        final String[] criterionValues = criterion.getFieldValue().getValue().trim().split("\\s+");

        sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                .append(RP);
        sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);
        sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
        sqlBuilder.append(SP).append(CODE_MATCH_ALIAS);
        args.add(criterionValues);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {
                sqlBuilder.append(COMMA).append(NL);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                        .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                        .append(RP);
                sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN);
                sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
                sqlBuilder.append(SP).append(PERM_ID_MATCH_ALIAS);

                args.add(criterionValues);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(COMMA).append(NL);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN);
                sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                        .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                        .append(RP);
                sqlBuilder.append(SP).append(THEN).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN);
                sqlBuilder.append(SP).append(ELSE).append(SP).append(NULL).append(SP).append(END);
                sqlBuilder.append(SP).append(DATA_SET_KIND_MATCH_ALIAS);
                args.add(criterionValues);
                break;
            }
        }
    }

    private static void buildTsRank(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args)
    {
        sqlBuilder.append(TS_RANK).append(LP).append(COALESCE).append(LP)
                .append(GlobalSearchCriteriaTranslator.PROPERTIES_TABLE_ALIAS).append(PERIOD).append(TSVECTOR_DOCUMENT)
                .append(COMMA).append(SP).append(SQ).append(SQ).append(RP)
                .append(COMMA).append(SP);
        buildTsQueryPart(sqlBuilder, stringValue, args);
        sqlBuilder.append(RP);
    }

    private static void buildFrom(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = vo.getTableMapper();

        final String entitiesTable = tableMapper.getEntitiesTable();
        final String projectsTableName = PROJECT.getEntitiesTable();

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);

        if (!forAttributes)
        {
            sqlBuilder.append(INNER_JOIN).append(SP).append(tableMapper.getValuesTable()).append(SP).append(PROPERTIES_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ)
                    .append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(tableMapper.getValuesTableEntityIdField()).append(NL);

            sqlBuilder.append(INNER_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(SP)
                    .append(EQ).append(SP).append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            sqlBuilder.append(INNER_JOIN).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(SP).append(EQ).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);

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

        switch (tableMapper)
        {
            case MATERIAL:
            {
                sqlBuilder.append(INNER_JOIN).append(SP).append(tableMapper.getEntityTypesTable()).append(SP)
                        .append(ENTITY_TYPES_TABLE_ALIAS)
                        .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                        .append(tableMapper.getEntitiesTableEntityTypeIdField())
                        .append(SP).append(EQ).append(SP).append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD)
                        .append(ID_COLUMN).append(NL);
                break;
            }
            case SAMPLE:
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable())
                        .append(SP).append(CONTAINER_TABLE_ALIAS)
                        .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                        .append(PART_OF_SAMPLE_COLUMN)
                        .append(SP).append(EQ).append(SP).append(CONTAINER_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                        .append(NL);
                break;
            }
        }
    }

    private static boolean hasProjects(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return SAMPLE.getEntitiesTable().equals(entitiesTable) || EXPERIMENT.getEntitiesTable().equals(entitiesTable);
    }

    private static boolean hasSpaces(final TableMapper tableMapper)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();
        return SAMPLE.getEntitiesTable().equals(entitiesTable) || PROJECT.getEntitiesTable().equals(entitiesTable);
    }

    private static void buildWhere(final StringBuilder sqlBuilder, final TranslationVo vo,
            final GlobalSearchTextCriteria criterion, final boolean forAttributes)
    {
        final List<Object> args = vo.getArgs();

        sqlBuilder.append(WHERE).append(SP);
        if (forAttributes)
        {
            buildAttributesMatchCondition(sqlBuilder, criterion, args);
        } else
        {
            buildTsVectorMatch(sqlBuilder, criterion.getFieldValue(), args);
        }
        sqlBuilder.append(NL);
    }

    private static void buildTsVectorMatch(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args)
    {
        sqlBuilder.append(GlobalSearchCriteriaTranslator.PROPERTIES_TABLE_ALIAS)
                .append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                .append(DOUBLE_AT).append(SP);
        buildTsQueryPart(sqlBuilder, stringValue, args);
    }

    private static void buildTsQueryPart(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final List<Object> args)
    {
        sqlBuilder.append(TO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP);
        args.add(toTsQueryText(stringValue));
    }

    private static String toTsQueryText(final AbstractStringValue stringValue)
    {
        return (StringContainsExactlyValue.class.isAssignableFrom(stringValue.getClass()))
                ? '\'' + stringValue.getValue().replaceAll("'", "''") + '\''
                : stringValue.getValue().replaceAll("['&|:!()<>]", " ").trim().replaceAll("\\s+", " | ");
    }

}
