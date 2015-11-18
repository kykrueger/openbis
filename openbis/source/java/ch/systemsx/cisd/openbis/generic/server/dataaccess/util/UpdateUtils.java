/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;

import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexUpdater;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UpdateUtils
{

    public static void waitUntilIndexUpdaterIsIdle(BeanFactory applicationContext, Logger operationLog)
    {
        FullTextIndexUpdater indexUpdater = (FullTextIndexUpdater) applicationContext.getBean("full-text-index-updater");
        DynamicPropertyEvaluationScheduler dynamicPropertyScheduler =
                (DynamicPropertyEvaluationScheduler) applicationContext.getBean("dynamic-property-scheduler");
    
        if (indexUpdater != null)
        {
            while (true)
            {
                IExtendedBlockingQueue<DynamicPropertyEvaluationOperation> dynamicPropertiesQueue = dynamicPropertyScheduler.getEvaluatorQueue();
                int indexingQueueSize = indexUpdater.getQueueSize();
    
                try
                {
                    if (dynamicPropertiesQueue != null && dynamicPropertiesQueue.size() > 0)
                    {
                        operationLog.info("Waiting on dynamic properties updater. Still " + dynamicPropertiesQueue.size()
                                + " update operations in the queue.");
                        Thread.sleep(1000);
                    } else if (indexingQueueSize > 0)
                    {
                        operationLog.info("Waiting on index updater. Still " + indexingQueueSize + " update operations in the queue.");
                        Thread.sleep(1000);
                    } else
                    {
                        return;
                    }
                } catch (Exception ex)
                {
                    // silently ignore
                    return;
                }
            }
        }
    }

}