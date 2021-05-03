package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.IEventId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.event.search.EventSearchCriteria")
public class EventSearchCriteria extends AbstractObjectSearchCriteria<IEventId>
{

    private static final long serialVersionUID = 1L;

    public EventSearchCriteria withOrOperator()
    {
        return (EventSearchCriteria) withOperator(SearchOperator.OR);
    }

    public EventSearchCriteria withAndOperator()
    {
        return (EventSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("EVENT");
        return builder;
    }

}
