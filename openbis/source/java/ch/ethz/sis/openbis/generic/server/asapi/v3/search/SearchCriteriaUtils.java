package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteriaUtils
{

    private SearchCriteriaUtils()
    {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    public static List<ISearchCriteria> getOtherCriteriaThan(AbstractCompositeSearchCriteria compositeSearchCriteria,
            Class<? extends ISearchCriteria>... clazzes)
    {
        List<ISearchCriteria> criterias = new ArrayList<>();
        for (ISearchCriteria criteria : compositeSearchCriteria.getCriteria())
        {
            boolean isInstanceOfOneOf = false;
            for (Class<? extends ISearchCriteria> clazz : clazzes)
            {
                if (clazz.isInstance(criteria))
                {
                    isInstanceOfOneOf = true;
                    break;
                }
            }
            if (false == isInstanceOfOneOf)
            {
                criterias.add(criteria);
            }
        }
        return criterias;
    }

    public static <SEARCH_CRITERIA extends ISearchCriteria> List<SEARCH_CRITERIA> getCriteria(
            AbstractCompositeSearchCriteria compositeSearchCriteria, Class<SEARCH_CRITERIA> clazz)
    {
        List<SEARCH_CRITERIA> criterias = new ArrayList<>();
        for (ISearchCriteria criteria : compositeSearchCriteria.getCriteria())
        {
            if (clazz.isInstance(criteria))
            {
                criterias.add((SEARCH_CRITERIA) criteria);
            }
        }
        return criterias;
    }

}
