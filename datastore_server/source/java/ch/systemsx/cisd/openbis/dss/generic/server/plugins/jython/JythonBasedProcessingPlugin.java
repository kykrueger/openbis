/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which delegates the processing to a Jython script.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedProcessingPlugin implements IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonBasedProcessingPlugin.class);

    public JythonBasedProcessingPlugin(Properties properties, File storeRoot)
    {
    }

    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        operationLog.info("Processing of the following datasets has been requested: " + datasets);
        final IHierarchicalContentProvider contentProvider =
                ServiceProvider.getHierarchicalContentProvider();

        ProcessingStatus result = new ProcessingStatus();

        for (DatasetDescription dataset : datasets)
        {
            String dataSetCode = dataset.getDataSetCode();
            IHierarchicalContent content = null;
            try
            {
                content = contentProvider.asContent(dataSetCode);
                IDataSet iDataSet = JythonBasedPluginUtils.createDataSet(dataset, content);
                result.addDatasetStatus(dataSetCode, delegateProcessing(iDataSet));
            } finally
            {
                if (content != null)
                {
                    content.close();
                }
            }
        }
        operationLog.info("Processing done.");
        return null;
    }

    private Status delegateProcessing(IDataSet iDataSet)
    {
        return Status.OK; // TODO
    }

}
