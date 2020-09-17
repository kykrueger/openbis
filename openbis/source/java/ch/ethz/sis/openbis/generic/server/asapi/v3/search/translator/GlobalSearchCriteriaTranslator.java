package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsExactlyValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKindCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchWildCardsCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.FLOAT4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildTypeCodeIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.DATA_TYPES_TABLE;

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

    public static final String SAMPLE_IDENTIFIER_MATCH_ALIAS = "sample_identifier_match";

    public static final String SAMPLE_MATCH_ALIAS = "sample_match";

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

    private static final String SAMPLES_TABLE_ALIAS = "samp";

    private static final String SPACE_TABLE_ALIAS = "space";

    private static final String PROJECT_TABLE_ALIAS = "proj";

    private static final String CONTAINER_TABLE_ALIAS = "cont";

    private static final String ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS = "etpt";

    private static final String ATTRIBUTE_TYPES_TABLE_ALIAS = "prty";

    private static final String DATA_TYPES_TABLE_ALIAS = "daty";

    private static final String TS_HEADLINE_OPTIONS = "HighlightAll=TRUE, StartSel=" + START_SEL
            +", StopSel=" + STOP_SEL;

    /** Magnitude of difference between important and less important fields. */
    private static final float IMPORTANCE_MULTIPLIER = 10.0f;

    /** Rank for other than ID attribute matches. */
    private static final float ATTRIBUTE_RANK = 0.6f;

    /** Rank for ID properties. */
    private static final float ID_PROPERTY_RANK = ATTRIBUTE_RANK * IMPORTANCE_MULTIPLIER;

    /** Rank for ID matches. */
    private static final float ID_RANK = ID_PROPERTY_RANK * IMPORTANCE_MULTIPLIER;

    private static final Logger LOG = LogFactory.getLogger(LogCategory.OPERATION, GlobalSearchCriteriaTranslator.class);

    private GlobalSearchCriteriaTranslator()
    {
        throw new UnsupportedOperationException();
    }

    public static SelectQuery translate(final TranslationContext translationContext)
    {
        if (translationContext.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final boolean withWildcards = translationContext.getCriteria().stream().anyMatch((criterion) -> criterion instanceof GlobalSearchWildCardsCriteria);
        if (withWildcards)
        {
            LOG.warn("Full text search with wildcards is not supported.");
        }

        final StringBuilder sqlBuilder = new StringBuilder(LP);
        final Spliterator<ISearchCriteria> spliterator = translationContext.getCriteria().stream()
                .filter((criterion) -> !(criterion instanceof GlobalSearchWildCardsCriteria)
                        && !(criterion instanceof GlobalSearchObjectKindCriteria)).spliterator();

        if (spliterator.tryAdvance((criterion) -> translateCriterion(sqlBuilder, translationContext, criterion)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) ->
            {
                sqlBuilder.append(RP).append(NL).append(UNION).append(NL).append(LP).append(NL);
                translateCriterion(sqlBuilder, translationContext, criterion);
            });
        }
        sqlBuilder.append(RP);

        return new SelectQuery(sqlBuilder.toString(), translationContext.getArgs());
    }

    private static void translateCriterion(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final ISearchCriteria criterion)
    {
        if (criterion instanceof GlobalSearchTextCriteria)
        {
            final GlobalSearchTextCriteria globalSearchTextCriterion = (GlobalSearchTextCriteria) criterion;

            // Fields
            buildSelect(sqlBuilder, translationContext, globalSearchTextCriterion, true);
            buildFrom(sqlBuilder, translationContext, globalSearchTextCriterion, true);
            buildWhere(sqlBuilder, translationContext, globalSearchTextCriterion, true);

            sqlBuilder.append(UNION).append(NL);

            // Properties
            buildSelect(sqlBuilder, translationContext, globalSearchTextCriterion, false);
            buildFrom(sqlBuilder, translationContext, globalSearchTextCriterion, false);
            buildWhere(sqlBuilder, translationContext, globalSearchTextCriterion, false);
        }
    }

    private static void buildSelect(final StringBuilder sqlBuilder, final TranslationContext translationContext, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final AbstractStringValue stringValue = criterion.getFieldValue();
        final List<Object> args = translationContext.getArgs();

        final boolean hasSpaces = hasSpaces(tableMapper);
        final boolean hasProjects = hasProjects(tableMapper);

        sqlBuilder.append(SELECT).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                .append(COMMA).append(NL);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN)
                        .append(COMMA).append(NL);
                break;
            }
            case DATA_SET:
            {
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN)
                        .append(COMMA).append(NL);
                break;
            }
        }

        sqlBuilder.append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                .append(ENTITY_TYPES_CODE_ALIAS).append(COMMA).append(NL);

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(COMMA).append(NL);
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERSON_REGISTERER_COLUMN).append(COMMA).append(NL);
        sqlBuilder.append(SQ).append(tableMapper.getEntityKind()).append(SQ).append(SP).append(OBJECT_KIND_ALIAS)
                .append(COMMA).append(NL);

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

        final String[] criterionValues = criterion.getFieldValue().getValue().toLowerCase().trim().split("\\s+");
        if (forAttributes)
        {
            final String thenValue = ID_RANK + DOUBLE_COLON + FLOAT4;
            final String elseValue = ATTRIBUTE_RANK + DOUBLE_COLON + FLOAT4;
            final String[] matchingColumns = (tableMapper == SAMPLE || tableMapper == EXPERIMENT)
                    ? new String[] { MAIN_TABLE_ALIAS + PERIOD + CODE_COLUMN,
                            MAIN_TABLE_ALIAS + PERIOD + PERM_ID_COLUMN }
                    : new String[] { MAIN_TABLE_ALIAS + PERIOD + CODE_COLUMN };
            buildCaseWhenIn(sqlBuilder, args, criterionValues, matchingColumns, thenValue, elseValue);
            sqlBuilder.append(SP).append(RANK_ALIAS).append(COMMA).append(NL);

            buildAttributesMatchSelection(sqlBuilder, criterionValues, translationContext.getTableMapper(), args);
            sqlBuilder.append(COMMA).append(NL);

            if (tableMapper == SAMPLE)
            {
                final String sampleIdentifierColumnReference = MAIN_TABLE_ALIAS + PERIOD + SAMPLE_IDENTIFIER_COLUMN;
                buildCaseWhenIn(sqlBuilder, args, criterionValues,
                        new String[] { LOWER + LP + sampleIdentifierColumnReference + RP },
                        sampleIdentifierColumnReference, NULL);
                sqlBuilder.append(SP).append(SAMPLE_IDENTIFIER_MATCH_ALIAS).append(COMMA).append(NL);
            }

            if (tableMapper == SAMPLE || tableMapper == EXPERIMENT || tableMapper == DATA_SET)
            {
                sqlBuilder.append(NULL).append(SP).append(SAMPLE_MATCH_ALIAS).append(COMMA).append(NL);
            }

            sqlBuilder.append(NULL).append(SP).append(PROPERTY_TYPE_LABEL_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(PROPERTY_VALUE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_CODE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_LABEL_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CV_DESCRIPTION_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(NULL).append(SP).append(VALUE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(CODE_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(LABEL_HEADLINE_ALIAS).append(COMMA).append(NL);
            sqlBuilder.append(NULL).append(SP).append(DESCRIPTION_HEADLINE_ALIAS).append(COMMA).append(NL);

            sqlBuilder.append(0.0f).append(DOUBLE_COLON).append(FLOAT4).append(SP).append(VALUE_MATCH_RANK_ALIAS)
                    .append(COMMA).append(NL);
            sqlBuilder.append(0.0f).append(DOUBLE_COLON).append(FLOAT4).append(SP).append(CODE_MATCH_RANK_ALIAS)
                    .append(COMMA).append(NL);
            sqlBuilder.append(0.0f).append(DOUBLE_COLON).append(FLOAT4).append(SP).append(LABEL_MATCH_RANK_ALIAS)
                    .append(COMMA).append(NL);
            sqlBuilder.append(0.0f).append(DOUBLE_COLON).append(FLOAT4).append(SP).append(DESCRIPTION_MATCH_RANK_ALIAS);
        } else
        {
            buildPropertyMatchRank(sqlBuilder, stringValue, criterionValues, args);

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

            if (tableMapper == SAMPLE)
            {
                sqlBuilder.append(NULL).append(SP).append(SAMPLE_IDENTIFIER_MATCH_ALIAS).append(COMMA).append(NL);
            }

            if (tableMapper == SAMPLE || tableMapper == EXPERIMENT || tableMapper == DATA_SET)
            {
                buildSampleMatch(sqlBuilder, criterionValues, args);
                sqlBuilder.append(COMMA).append(NL);
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

            final boolean useHeadline = translationContext.isUseHeadline();
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

    private static void buildSampleMatch(final StringBuilder sqlBuilder, final String[] values,
            final List<Object> args)
    {
        sqlBuilder.append(CASE).append(NL);
        appendWhenThen(sqlBuilder, PERM_ID_COLUMN, values, args);
        appendWhenThen(sqlBuilder, CODE_COLUMN, values, args);
        appendWhenThen(sqlBuilder, SAMPLE_IDENTIFIER_COLUMN, values, args);
        sqlBuilder.append('\t').append(ELSE).append(SP).append(NULL).append(NL);
        sqlBuilder.append(END).append(SP).append(SAMPLE_MATCH_ALIAS);
    }

    private static void appendWhenThen(final StringBuilder sqlBuilder, final String column, final String[] values,
            final List<Object> args)
    {
        sqlBuilder.append('\t').append(WHEN).append(SP).append(LOWER).append(LP)
                .append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(column).append(RP).append(SP).append(IN).append(SP)
                .append(SELECT_UNNEST)
                .append(THEN).append(SP).append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(column).append(NL);
        args.add(values);
    }

    /**
     * Builds the rank calculation part of the property matching query part.
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param stringValue string value to search by.
     * @param splitValues string value split to separate values.
     * @param args query arguments.
     */
    private static void buildPropertyMatchRank(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final String[] splitValues, final List<Object> args)
    {
        sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP)
                .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP)
                .append(IN).append(SP).append(SELECT_UNNEST)
                .append(SP).append(OR).append(SP)
                .append(DATA_TYPES_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(IN).append(SP)
                .append(LP).append(SQ).append(MATERIAL.name()).append(SQ).append(COMMA).append(SP).append(SQ)
                .append(SAMPLE.name()).append(SQ).append(RP);
        args.add(splitValues);
        sqlBuilder.append(SP).append(THEN).append(SP).append(ID_PROPERTY_RANK).append(DOUBLE_COLON)
                .append(FLOAT4).append(SP);
        sqlBuilder.append(ELSE).append(SP);
        buildHeadlineTsRank(sqlBuilder, stringValue, args, PROPERTIES_TABLE_ALIAS + PERIOD + VALUE_COLUMN, "");
        sqlBuilder.append(PLUS).append(NL);
        buildHeadlineTsRank(sqlBuilder, stringValue, args,
                CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + CODE_COLUMN, "");
        sqlBuilder.append(ASTERISK).append(SP).append(IMPORTANCE_MULTIPLIER).append(DOUBLE_COLON).append(FLOAT4)
                .append(SP).append(PLUS).append(NL);
        buildHeadlineTsRank(sqlBuilder, stringValue, args,
                CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS + PERIOD + LABEL_COLUMN, "");
        sqlBuilder.append(ASTERISK).append(SP).append(IMPORTANCE_MULTIPLIER).append(DOUBLE_COLON).append(FLOAT4)
                .append(NL);

        sqlBuilder.append(END).append(SP).append(RANK_ALIAS).append(COMMA).append(NL);
    }

    private static void buildHeadlineTsRank(final StringBuilder sqlBuilder, final AbstractStringValue stringValue, final List<Object> args,
            final String field, final String alias)
    {
        sqlBuilder.append(COALESCE).append(LP).append(TS_RANK).append(LP)
                .append(TO_TSVECTOR).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(field).append(RP).append(COMMA).append(SP)
                .append(TO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP).append(RP).append(COMMA).append(SP)
                .append(0.0f).append(DOUBLE_COLON).append(FLOAT4).append(RP)
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
     * @param criterionValues full text search values to be put to the full text search matching function.
     * @param tableMapper the table mapper.
     * @param args query arguments.
     */
    private static void buildAttributesMatchSelection(final StringBuilder sqlBuilder, final String[] criterionValues,
            final TableMapper tableMapper, final List<Object> args)
    {
        final String thenValue = MAIN_TABLE_ALIAS + PERIOD + CODE_COLUMN;
        buildCaseWhenIn(sqlBuilder, args, criterionValues, new String[]{thenValue}, thenValue, NULL);
        sqlBuilder.append(SP).append(CODE_MATCH_ALIAS);

        switch (tableMapper)
        {
            case SAMPLE:
            case EXPERIMENT:
            {
                sqlBuilder.append(COMMA).append(NL);

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP).append(LOWER).append(LP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(RP);
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

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP).append(LOWER).append(LP);
                sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(DATA_SET_KIND_COLUMN).append(RP);
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

    /**
     * Builds the following part of the query.
     * <pre>
     *     CASE WHEN
     *     matchingColumns[0] IN (SELECT UNNEST(?))
     *     OR
     *     ...
     *     OR
     *     matchingColumns[n] IN (SELECT UNNEST(?))
     *     THEN thenValue ELSE elseValue END
     * </pre>
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param args query arguments.
     * @param criterionValues full text search values to be put to the full text search matching function.
     * @param matchingColumns array of columns at least one of which should be equal to one of {@code criterionValues}.
     * @param thenValue resulting value for the true case.
     * @param elseValue resulting value for the false case.
     */
    private static void buildCaseWhenIn(final StringBuilder sqlBuilder, final List<Object> args,
            final String[] criterionValues, final String[] matchingColumns, final String thenValue,
            final String elseValue)
    {
        sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

        final Spliterator<String> spliterator = Arrays.stream(matchingColumns).spliterator();

        if (spliterator.tryAdvance(matchingColumn -> appendMatchingColumnCondition(sqlBuilder, matchingColumn, args,
                criterionValues)))
        {
            spliterator.forEachRemaining(matchingColumn ->
            {
                sqlBuilder.append(SP).append(OR).append(SP);
                appendMatchingColumnCondition(sqlBuilder, matchingColumn, args, criterionValues);
            });
        }

        sqlBuilder.append(SP).append(THEN).append(SP).append(thenValue);
        sqlBuilder.append(SP).append(ELSE).append(SP).append(elseValue).append(SP).append(END);
    }

    /**
     * Builds the following part of the query and adds the corresponding array of values to the query arguments list.
     * <pre>
     *     matchingColumns[0] IN (SELECT UNNEST(?))
     * </pre>
     * @param sqlBuilder {@link StringBuilder string builder} containing SQL to be operated on.
     * @param matchingColumn column which should be equal to one of values in the query parameter ('?').
     * @param args query arguments.
     * @param criterionValues full text search values to be put to the full text search matching function.
     */
    private static void appendMatchingColumnCondition(final StringBuilder sqlBuilder,
            final String matchingColumn, final List<Object> args, final String[] criterionValues)
    {
        sqlBuilder.append(LOWER).append(SP).append(LP).append(matchingColumn).append(RP);
        sqlBuilder.append(SP).append(IN).append(SP).append(LP)
                .append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                .append(RP);
        args.add(criterionValues);
    }

    private static void buildFrom(final StringBuilder sqlBuilder, final TranslationContext translationContext, final GlobalSearchTextCriteria criterion,
            final boolean forAttributes)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();

        final String entitiesTable = tableMapper.getEntitiesTable();
        final String projectsTableName = PROJECT.getEntitiesTable();

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);

        if (!forAttributes)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getValuesTable()).append(SP).append(PROPERTIES_TABLE_ALIAS).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ)
                    .append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(tableMapper.getValuesTableEntityIdField()).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesAttributeTypesTable()).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(PROPERTIES_TABLE_ALIAS).append(PERIOD).append(tableMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(SP)
                    .append(EQ).append(SP).append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getAttributeTypesTable()).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(SP).append(ON).append(SP)
                    .append(ENTITY_TYPES_ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(SP).append(EQ).append(SP)
                    .append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            sqlBuilder.append(LEFT_JOIN).append(SP).append(DATA_TYPES_TABLE).append(SP).append(DATA_TYPES_TABLE_ALIAS)
                    .append(SP).append(ON).append(SP).append(ATTRIBUTE_TYPES_TABLE_ALIAS).append(PERIOD)
                    .append(DATA_TYPE_COLUMN).append(SP).append(EQ).append(SP)
                    .append(DATA_TYPES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);

            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableNames.CONTROLLED_VOCABULARY_TERM_TABLE).append(SP)
                    .append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(SP).append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS).append(PERIOD)
                    .append(VOCABULARY_TERM_COLUMN).append(SP).append(EQ).append(SP).append(CONTROLLED_VOCABULARY_TERMS_TABLE_ALIAS).append(PERIOD)
                    .append(ID_COLUMN).append(NL);

            if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                    || tableMapper == TableMapper.DATA_SET)
            {
                sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable()).append(SP)
                        .append(SAMPLES_TABLE_ALIAS).append(SP).append(ON).append(SP).append(PROPERTIES_TABLE_ALIAS)
                        .append(PERIOD).append(SAMPLE_PROP_COLUMN).append(SP).append(EQ).append(SP)
                        .append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(NL);
            }
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

        sqlBuilder.append(LEFT_JOIN).append(SP).append(tableMapper.getEntityTypesTable()).append(SP)
                .append(ENTITY_TYPES_TABLE_ALIAS)
                .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                .append(tableMapper.getEntitiesTableEntityTypeIdField())
                .append(SP).append(EQ).append(SP).append(ENTITY_TYPES_TABLE_ALIAS).append(PERIOD)
                .append(ID_COLUMN).append(NL);

        if (tableMapper == SAMPLE)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(SAMPLE.getEntitiesTable())
                    .append(SP).append(CONTAINER_TABLE_ALIAS)
                    .append(SP).append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD)
                    .append(PART_OF_SAMPLE_COLUMN)
                    .append(SP).append(EQ).append(SP).append(CONTAINER_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN)
                    .append(NL);
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

    private static void buildWhere(final StringBuilder sqlBuilder, final TranslationContext translationContext,
            final GlobalSearchTextCriteria criterion, final boolean forAttributes)
    {
        final List<Object> args = translationContext.getArgs();

        sqlBuilder.append(WHERE).append(SP);
        if (forAttributes)
        {
            buildAttributesMatchCondition(sqlBuilder, criterion, args);
        } else
        {
            buildTsVectorMatch(sqlBuilder, criterion.getFieldValue(), translationContext.getTableMapper(), args);
        }
        sqlBuilder.append(NL);
    }

    private static void buildTsVectorMatch(final StringBuilder sqlBuilder, final AbstractStringValue stringValue,
            final TableMapper tableMapper, final List<Object> args)
    {
        sqlBuilder.append(GlobalSearchCriteriaTranslator.PROPERTIES_TABLE_ALIAS)
                .append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                .append(DOUBLE_AT).append(SP);
        buildTsQueryPart(sqlBuilder, stringValue, args);

        if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
                || tableMapper == TableMapper.DATA_SET)
        {
            sqlBuilder.append(SP).append(OR).append(SP);

            sqlBuilder.append(SAMPLES_TABLE_ALIAS).append(PERIOD).append(TS_VECTOR_COLUMN).append(SP)
                    .append(DOUBLE_AT).append(SP).append(QU).append(DOUBLE_COLON).append(TSQUERY);
            args.add(toTsQueryText(stringValue));
        }
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
                ? '\'' + stringValue.getValue().toLowerCase().replaceAll("'", "''") + '\''
                : stringValue.getValue().toLowerCase().replaceAll("['&|:!()<>]", " ").trim().replaceAll("\\s+", " | ");
    }

}
