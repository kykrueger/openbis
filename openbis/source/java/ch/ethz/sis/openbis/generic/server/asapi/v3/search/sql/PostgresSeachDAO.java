package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

public class PostgresSeachDAO implements ISQLSearchDAO
{
    @Override
    /** search for entity ids using the specified criteria */
    public List<Long> searchForEntityIds(final String userId, DetailedSearchCriteria criteria,
            EntityKind entityKind, List<IAssociationCriteria> associationCriterias) {
        return null;
    }

    @Override
    public int getResultSetSizeLimit()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
