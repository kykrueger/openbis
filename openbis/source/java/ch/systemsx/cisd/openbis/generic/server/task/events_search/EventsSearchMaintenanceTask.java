/*
 * Copyright 2018 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import org.apache.log4j.Logger;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author pkupczyk
 */
public class EventsSearchMaintenanceTask implements IMaintenanceTask
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final IDataSource dataSource;

    public EventsSearchMaintenanceTask()
    {
        this.dataSource = new DataSource();
    }

    EventsSearchMaintenanceTask(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override public void setUp(String pluginName, Properties properties)
    {
    }

    @Override
    public void execute()
    {
        dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
            try
            {
                LastTimestamps lastTimestamps = new LastTimestamps(dataSource);
                Snapshots snapshots = new Snapshots(dataSource);

                List<EventProcessor> processors = Arrays.asList(
                        new SpaceDeletionProcessor(dataSource),
                        new ProjectDeletionProcessor(dataSource),
                        new ExperimentDeletionProcessor(dataSource),
                        new SampleDeletionProcessor(dataSource),
                        new DataSetDeletionProcessor(dataSource),
                        new GenericDeletionProcessor(dataSource, EntityType.MATERIAL),
                        new GenericDeletionProcessor(dataSource, EntityType.ATTACHMENT),
                        new GenericDeletionProcessor(dataSource, EntityType.PROPERTY_TYPE),
                        new GenericDeletionProcessor(dataSource, EntityType.VOCABULARY),
                        new GenericDeletionProcessor(dataSource, EntityType.AUTHORIZATION_GROUP),
                        new GenericDeletionProcessor(dataSource, EntityType.METAPROJECT));

                for (EventProcessor processor : processors)
                {
                    processor.process(lastTimestamps, snapshots);
                }

                return null;
            } catch (Throwable e)
            {
                operationLog.error("Execution failed", e);
                throw e;
            }
        });
    }

}
