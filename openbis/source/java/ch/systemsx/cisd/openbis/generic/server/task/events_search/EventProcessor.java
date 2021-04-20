package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import com.fasterxml.jackson.databind.ObjectMapper;

abstract class EventProcessor
{

    protected static final int BATCH_SIZE = 1000;

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected IDataSource dataSource;

    EventProcessor(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    abstract void process(LastTimestamps lastTimestamps, Snapshots snapshots);
}