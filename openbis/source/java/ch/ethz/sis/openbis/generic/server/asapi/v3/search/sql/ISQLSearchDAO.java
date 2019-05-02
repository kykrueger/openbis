package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

public interface ISQLSearchDAO
{
    /** search for entity ids using the specified criteria */
    public List<Long> searchForEntityIds(final String userId, DetailedSearchCriteria criteria,
            EntityKind entityKind, List<IAssociationCriteria> associationCriterias);
    
    /**
     * Returns the maximum size of a search result set. The standard implementations returns <code>hibernate.search.maxResults</code> of
     * <code>service.properties</code>.
     */
    public int getResultSetSizeLimit();
}
