package ch.ethz.sis.openbis.generic.as.api.v3.dto.common.fetchoptions;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.common.fetchoptions.EmptyFetchOptions")
public class EmptyFetchOptions extends FetchOptions<Void>
{

    private static final long serialVersionUID = 1L;

    @Override
    public SortOptions<Void> sortBy()
    {
        return null;
    }

    @Override
    public SortOptions<Void> getSortBy()
    {
        return null;
    }

}