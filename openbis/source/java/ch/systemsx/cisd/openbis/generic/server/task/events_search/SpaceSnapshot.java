package ch.systemsx.cisd.openbis.generic.server.task.events_search;

class SpaceSnapshot extends AbstractSnapshot
{

    public Long spaceTechId;

    public String spaceCode;

    @Override protected String getKey()
    {
        return spaceCode;
    }
}