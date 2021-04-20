package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Date;

abstract class AbstractSnapshot
{
    public Date from;

    public Date to;

    protected abstract String getKey();
}
