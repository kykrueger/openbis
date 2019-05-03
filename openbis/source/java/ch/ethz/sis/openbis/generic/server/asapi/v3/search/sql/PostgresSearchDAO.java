package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

import java.util.List;

public class PostgresSearchDAO implements ISQLSearchDAO
{

    @Override
    /** search for entity ids using the specified criteria */
    public List<Long> searchForEntityIds(final String userId, ISearchCriteria criteria,
            EntityKind entityKind, List<IAssociationCriteria> associationCriteria) {
        return null;
    }

    @Override
    public int getResultSetSizeLimit()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
