package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.text.SimpleDateFormat;

abstract class EntityDeletionProcessor<SNAPSHOT extends AbstractSnapshot> extends EventProcessor
{

    protected static final SimpleDateFormat REGISTRATION_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    protected static final SimpleDateFormat VALID_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    EntityDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

}
