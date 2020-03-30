package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.*;

public class GlobalSearchManager extends AbstractSearchManager<GlobalSearchObject> implements IGlobalSearchManager
{

    public GlobalSearchManager(final ISQLAuthorisationInformationProviderDAO authProvider, final ISQLSearchDAO searchDAO)
    {
        super(authProvider, searchDAO);
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final GlobalSearchCriteria criteria,
            final String idsColumnName, final TableMapper tableMapper)
    {
        ///////////// TODO: probably subcriteria should be used in this query not the main criteria.
        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId, criteria, tableMapper,
                idsColumnName, authorisationInformation);

        // If we have results, we use them
        // If we don't have results and criteria are not empty, there are no results.
        final Set<Long> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        return filterIDsByUserRights(userId, authorisationInformation, resultBeforeFiltering);
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        // TODO: filtering is needed.
        return ids;
    }

    @Override
    public Set<Long> sortIDs(final Set<Long> filteredIDs, final SortOptions<GlobalSearchObject> sortOptions)
    {
        // TODO: implement sorting.
        return filteredIDs;
    }

    @Override
    public List<Long> translate(final List<Long> ids)
    {
        return null;
    }

}
