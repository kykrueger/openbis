package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

import java.util.List;
import java.util.Set;

public class PostgresSearchDAO implements ISQLSearchDAO
{

    /*
     *
     */
    public Set<Long> queryDBWithNonRecursiveCriteria(EntityKind entityKind, List<ISearchCriteria> criteria,
            SearchOperator operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetSizeLimit()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
