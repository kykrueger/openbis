package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;

import java.util.List;

public interface ISQLSearchDAO
{

    /** search for entity ids using the specified criteria */
    List<Long> searchForEntityIds(String userId, ISearchCriteria criteria,
            EntityKind entityKind, List<IAssociationCriteria> associationCriteria);
    
    /**
     * Returns the maximum size of a search result set. The standard implementations returns <code>hibernate.search.maxResults</code> of
     * <code>service.properties</code>.
     */
    int getResultSetSizeLimit();

}
