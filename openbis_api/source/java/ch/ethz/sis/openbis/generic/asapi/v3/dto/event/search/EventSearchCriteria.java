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

    public EventEventTypeSearchCriteria withEventType()
    {
        return with(new EventEventTypeSearchCriteria());
    }

    public EventEntityTypeSearchCriteria withEntityType()
    {
        return with(new EventEntityTypeSearchCriteria());
    }

    public EventEntitySpaceSearchCriteria withEntitySpace()
    {
        return with(new EventEntitySpaceSearchCriteria());
    }

    public EventEntitySpacePermIdSearchCriteria withEntitySpacePermId()
    {
        return with(new EventEntitySpacePermIdSearchCriteria());
    }

    public EventEntityProjectSearchCriteria withEntityProject()
    {
        return with(new EventEntityProjectSearchCriteria());
    }

    public EventEntityProjectPermIdSearchCriteria withEntityProjectPermId()
    {
        return with(new EventEntityProjectPermIdSearchCriteria());
    }

    public EventEntityRegistratorSearchCriteria withEntityRegistrator()
    {
        return with(new EventEntityRegistratorSearchCriteria());
    }

    public EventEntityRegistrationDateSearchCriteria withEntityRegistrationDate()
    {
        return with(new EventEntityRegistrationDateSearchCriteria());
    }

    public EventRegistratorSearchCriteria withRegistrator()
    {
        return with(new EventRegistratorSearchCriteria());
    }

    public EventRegistrationDateSearchCriteria withRegistrationDate()
    {
        return with(new EventRegistrationDateSearchCriteria());
    }

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
