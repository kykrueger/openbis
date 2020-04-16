package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchTextCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchCriteriaTranslator
{
    private static final String REG_CONFIG = "simple";

    public static final String OBJECT_KIND_ALIAS = "object_kind";

    public static final String RANK_ALIAS = "rank";

    public static final String IDENTIFIER_ALIAS = "identifier";

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

        final StringBuilder sqlBuilder = new StringBuilder();

        final Spliterator<ISearchCriteria> spliterator = vo.getCriteria().stream().spliterator();
        if (spliterator.tryAdvance((criterion) -> translateCriterion(sqlBuilder, vo, (GlobalSearchTextCriteria) criterion)))
        {
            StreamSupport.stream(spliterator, false).forEach((criterion) -> {
                sqlBuilder.append(NL);
                switch (vo.getOperator())
                {
                    case AND:
                    {
                        sqlBuilder.append(INTERSECT);
                        break;
                    }
                    case OR:
                    {
                        sqlBuilder.append(UNION);
                        break;
                    }
                }
                sqlBuilder.append(NL);

                translateCriterion(sqlBuilder, vo, (GlobalSearchTextCriteria) criterion);
            });
        }

        return new SelectQuery(sqlBuilder.toString(), vo.getArgs());
    }

    private static void translateCriterion(final StringBuilder sqlBuilder, final TranslationVo vo, final GlobalSearchTextCriteria criterion)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final Object value = criterion.getFieldValue().getValue();
        final List<Object> args = vo.getArgs();

        final String entitiesTable = tableMapper.getEntitiesTable();
        final String samplesTableName = TableMapper.SAMPLE.getEntitiesTable();
        final String projectsTableName = TableMapper.PROJECT.getEntitiesTable();
        final String experimentsTableName = TableMapper.EXPERIMENT.getEntitiesTable();

        final boolean hasSpaces = entitiesTable.equals(samplesTableName) || entitiesTable.equals(projectsTableName);
        final boolean hasProjects = entitiesTable.equals(samplesTableName) || entitiesTable.equals(experimentsTableName);
        final String spacesTableAlias = hasSpaces ? "spc" : null;
        final String projectsTableAlias = hasProjects ? "prj" : null;
        final String samplesTableAlias = tableMapper == TableMapper.SAMPLE ? MAIN_TABLE_ALIAS : null;

        // SELECT

        sqlBuilder.append(SELECT).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(ID_COLUMN).append(COMMA).append(SP)
                .append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(COMMA).append(SP)
                .append(SQ).append(tableMapper.getEntityKind()).append(SQ).append(SP).append(OBJECT_KIND_ALIAS).append(COMMA).append(SP);

        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD)
                .append(AttributesMapper.getColumnName(AttributesMapper.PERM_ID_ATTRIBUTE, entitiesTable, null)).append(COMMA).append(SP);

        buildFullIdentifierConcatenationString(sqlBuilder, spacesTableAlias, projectsTableAlias, samplesTableAlias);
        sqlBuilder.append(IDENTIFIER_ALIAS).append(COMMA).append(SP);

        sqlBuilder.append(TS_RANK).append(LP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(TSVECTOR_DOCUMENT).append(COMMA).append(SP);

        buildTsQueryPart(sqlBuilder, value, args);
        sqlBuilder.append(RP).append(SP).append(RANK_ALIAS).append(NL);

        // FROM

        sqlBuilder.append(FROM).append(SP).append(entitiesTable).append(SP).append(MAIN_TABLE_ALIAS).append(NL);

        if (hasProjects)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(projectsTableName).append(SP).append(projectsTableAlias).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(PROJECT_COLUMN).append(SP).append(EQ).append(SP)
                    .append(projectsTableAlias).append(PERIOD).append(ID_COLUMN).append(NL);
        }

        if (hasSpaces)
        {
            sqlBuilder.append(LEFT_JOIN).append(SP).append(TableMapper.SPACE.getEntitiesTable()).append(SP).append(spacesTableAlias).append(SP)
                    .append(ON).append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(SPACE_COLUMN).append(SP).append(EQ).append(SP)
                    .append(spacesTableAlias).append(PERIOD).append(ID_COLUMN).append(NL);
        }

        // WHERE

        sqlBuilder.append(WHERE).append(SP).append(TSVECTOR_DOCUMENT).append(SP).append(DOUBLE_AT).append(SP);

        buildTsQueryPart(sqlBuilder, value, args);
    }

    private static void buildTsQueryPart(final StringBuilder sqlBuilder, final Object value, final List<Object> args)
    {
        sqlBuilder.append(PLAINTO_TSQUERY).append(LP).append(SQ).append(REG_CONFIG).append(SQ).append(COMMA).append(SP)
                .append(QU).append(RP);
        args.add(value);
    }
}
