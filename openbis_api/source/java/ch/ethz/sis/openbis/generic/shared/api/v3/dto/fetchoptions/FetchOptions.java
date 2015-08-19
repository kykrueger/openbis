package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dto.fetchoptions.FetchOptions")
public abstract class FetchOptions<OBJECT> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Integer count;

    private Integer from;

    private Integer cacheMode;

    @SuppressWarnings("hiding")
    public FetchOptions<OBJECT> count(Integer count)
    {
        this.count = count;
        return this;
    }

    public Integer getCount()
    {
        return count;
    }

    @SuppressWarnings("hiding")
    public FetchOptions<OBJECT> from(Integer from)
    {
        this.from = from;
        return this;
    }

    public Integer getFrom()
    {
        return from;
    }

    public FetchOptions<OBJECT> cacheMode(Integer mode)
    {
        this.cacheMode = mode;
        return this;
    }

    public Integer getCacheMode()
    {
        return cacheMode;
    }

    public abstract SortOptions<OBJECT> sortBy();

    public abstract SortOptions<OBJECT> getSortBy();

}
