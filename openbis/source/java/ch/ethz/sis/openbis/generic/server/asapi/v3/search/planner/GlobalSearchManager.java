package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;

import java.util.*;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchManager implements IGlobalSearchManager
{

    private static final String PERM_ID_FIELD_NAME = "Perm ID";

    private static final String DATA_SET_KIND_FIELD_NAME = "DataSet kind";

    private static final String IDENTIFIER_FIELD_NAME = "Identifier";

    private static final String CODE_FIELD_NAME = "Code";

    private static final String PROPERTY_NAME = "Property";

    protected final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISQLSearchDAO searchDAO;

//    @Autowired
//    private SampleTypeSearchManager sampleTypeSearchManager;
//
//    @Autowired
//    private ExperimentTypeSearchManager experimentTypeSearchManager;
//
//    @Autowired
//    private DataSetTypeSearchManager dataSetTypeSearchManager;
//
//    @Autowired
//    private MaterialTypeSearchManager materialTypeSearchManager;

    public GlobalSearchManager(final ISQLAuthorisationInformationProviderDAO authProvider, final ISQLSearchDAO searchDAO)
    {
        this.searchDAO = searchDAO;
        this.authProvider = authProvider;
    }

    @Override
    public Set<Map<String, Object>> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final GlobalSearchCriteria criteria,
            final String idsColumnName, final TableMapper tableMapper)
    {
        final Set<Map<String, Object>> mainCriteriaIntermediateResults = searchDAO.queryDBWithNonRecursiveCriteria(userId,
                criteria, tableMapper, idsColumnName, authorisationInformation);

        // If we have results, we use them
        // If we don't have results and criteria are not empty, there are no results.
        final Set<Map<String, Object>> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        return filterResultsByUserRights(authorisationInformation, resultBeforeFiltering);
    }

    @Override
    public Set<Map<String, Object>> sortIDs(final Set<Map<String, Object>> filteredIDs, final SortOptions<GlobalSearchObject> sortOptions)
    {
        // TODO: implement sorting.
        return filteredIDs;
    }

    /**
     * Checks whether a collection contains any values.
     *
     * @param collection collection to be checked for values.
     * @return {@code false} if collection is {@code null} or empty, true otherwise.
     */
    protected static boolean containsValues(final Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    private Set<Map<String, Object>> filterResultsByUserRights(final AuthorisationInformation authorisationInformation,
            final Set<Map<String, Object>> result)
    {
        if (authorisationInformation.isInstanceRole())
        {
            return result;
        } else
        {
            final Set<Long> allIds = result.stream().map(fieldMap -> (Long) fieldMap.get(ID_COLUMN)).collect(Collectors.toSet());
            final Set<Long> filteredIds = doFilterIDsByUserRights(allIds, authorisationInformation);
            return result.stream().filter(fieldMap -> filteredIds.contains((Long) fieldMap.get(ID_COLUMN))).collect(Collectors.toSet());
        }
    }

    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        // TODO: filtering is needed.
        return ids;
    }

    @Override
    public Collection<MatchingEntity> map(final Collection<Map<String, Object>> records)
    {
        final List<MatchingEntity> result = records.stream().map((fieldsMap) ->
        {
            final MatchingEntity matchingEntity = new MatchingEntity();
            matchingEntity.setCode((String) fieldsMap.get(CODE_COLUMN));
            final EntityKind entityKind = EntityKind.valueOf((String) fieldsMap.get(OBJECT_KIND_ALIAS));
            matchingEntity.setEntityKind(entityKind);
            matchingEntity.setId((Long) fieldsMap.get(ID_COLUMN));
            matchingEntity.setPermId((String) fieldsMap.get(PERM_ID_COLUMN));

            final String entityTypesCode = (String) fieldsMap.get(ENTITY_TYPES_CODE_ALIAS);
            if (entityTypesCode != null)
            {
                matchingEntity.setEntityType(new BasicEntityType(entityTypesCode));
            }

            matchingEntity.setIdentifier((String) fieldsMap.get(IDENTIFIER_ALIAS));

//            SearchDomainSearchResult searchResult = searchDomain.getSearchResult();
//            matchingEntity.setSearchDomain(searchResult.getSearchDomain().getLabel());

            matchingEntity.setScore((Float) fieldsMap.get(RANK_ALIAS));

            final List<PropertyMatch> matches = new ArrayList<>();

            mapMatch(fieldsMap, matches, VALUE_HEADLINE_ALIAS,
                    PROPERTY_NAME + " '" + fieldsMap.get(PROPERTY_LABEL_ALIAS) + "'");

            switch (entityKind)
            {
                case MATERIAL:
                {
                    mapMatch(fieldsMap, matches, IDENTIFIER_ALIAS, IDENTIFIER_FIELD_NAME);
                    break;
                }
                
                case EXPERIMENT:
                    // Falls through.
                case SAMPLE:
                {
                    mapMatch(fieldsMap, matches, CODE_MATCH_ALIAS, CODE_FIELD_NAME);
                    mapMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                    break;
                }
                case DATA_SET:
                {
                    mapMatch(fieldsMap, matches, DATA_SET_KIND_MATCH_ALIAS, DATA_SET_KIND_FIELD_NAME);
                    mapMatch(fieldsMap, matches, CODE_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                    mapMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                    break;
                }
            }

            matchingEntity.setMatches(matches);
            return matchingEntity;
        }).sorted(new SimpleComparator<MatchingEntity, Double>()
        {
            @Override
            public Double evaluate(MatchingEntity item)
            {
                return -item.getScore();
            }
        }).collect(Collectors.toList());
        return result;
    }

    private void mapMatch(final Map<String, Object> fieldsMap, final List<PropertyMatch> matches,
            final String matchAlias, final String code)
    {
        final Object fieldMatch = fieldsMap.get(matchAlias);
        if (fieldMatch != null)
        {
            final String codeMatchString = (String) fieldMatch;
            final PropertyMatch propertyMatch = new PropertyMatch();
            propertyMatch.setCode(code);
            propertyMatch.setValue(codeMatchString);

            final Span span = new Span();
            span.setStart(0);
            span.setEnd(codeMatchString.length());
            propertyMatch.setSpans(Collections.singletonList(span));
            matches.add(propertyMatch);
        }
    }

}
