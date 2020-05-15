package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyMatch;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Span;

import java.util.*;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchManager implements IGlobalSearchManager
{

    private static final String PERM_ID_PROPERTY_NAME = "Perm ID";
    
    private static final String IDENTIFIER_PROPERTY_NAME = "Identifier";

    private static final String CODE_PROPERTY_NAME = "Code";

    protected final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISQLSearchDAO searchDAO;

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
    public List<MatchingEntity> map(final List<Map<String, Object>> records)
    {
        final List<MatchingEntity> result = records.stream().map((fieldsMap) -> {
            final MatchingEntity matchingEntity = new MatchingEntity();
            matchingEntity.setCode((String) fieldsMap.get(CODE_COLUMN));
            final EntityKind entityKind = EntityKind.valueOf((String) fieldsMap.get(OBJECT_KIND_ALIAS));
            matchingEntity.setEntityKind(entityKind);
//            matchingEntity.setEntityType();
            matchingEntity.setId((Long) fieldsMap.get(ID_COLUMN));
            matchingEntity.setPermId((String) fieldsMap.get(PERM_ID_COLUMN));

            matchingEntity.setIdentifier((String) fieldsMap.get(IDENTIFIER_ALIAS));
            
//            SearchDomainSearchResult searchResult = searchDomain.getSearchResult();
//            matchingEntity.setSearchDomain(searchResult.getSearchDomain().getLabel());

            matchingEntity.setScore((Float) fieldsMap.get(RANK_ALIAS));

            final List<PropertyMatch> matches = new ArrayList<>();

            final Object codeMatch = fieldsMap.get(CODE_MATCH_ALIAS);
            if (codeMatch != null) {
                final String codeMatchString = (String) codeMatch;
                final PropertyMatch match = new PropertyMatch();
                match.setValue(codeMatchString);

                switch (entityKind)
                {
                    case EXPERIMENT:
                        // Falls through.
                    case SAMPLE:
                    {
                        match.setCode(CODE_PROPERTY_NAME);
                        break;
                    }
                    case MATERIAL:
                        // Falls through.
                    case DATA_SET:
                    {
                        match.setCode(PERM_ID_PROPERTY_NAME);
                        break;
                    }
                }

                final Span span = new Span();
                span.setStart(0);
                span.setEnd(codeMatchString.length());
                match.setSpans(Collections.singletonList(span));
                matches.add(match);
            }

            matchingEntity.setMatches(matches);

            return matchingEntity;
        }).collect(Collectors.toList());
        result.sort(new SimpleComparator<MatchingEntity, Double>()
        {
            @Override
            public Double evaluate(MatchingEntity item)
            {
                return -item.getScore();
            }
        });
        return result;
    }

}
