package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EventSortOptions extends SortOptions<Event>
{
    @JsonIgnore
    public static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    public SortOrder registrationDate()
    {
        return getOrCreateSorting(REGISTRATION_DATE);
    }

    public SortOrder getRegistrationDate()
    {
        return getSorting(REGISTRATION_DATE);
    }

}
