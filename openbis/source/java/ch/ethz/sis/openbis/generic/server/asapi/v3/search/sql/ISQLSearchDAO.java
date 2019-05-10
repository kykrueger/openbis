package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

import java.util.List;
import java.util.Set;

public interface ISQLSearchDAO
{

    /*
     *
     */
    Set<Long> queryDBWithNonRecursiveCriteria(EntityKind entityKind, List<ISearchCriteria> criteria,
            SearchOperator operator);
    
    /**
     * Returns the maximum size of a search result set. The standard implementations returns <code>hibernate.search.maxResults</code> of
     * <code>service.properties</code>.
     */
    int getResultSetSizeLimit();

}
