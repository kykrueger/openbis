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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.Properties;

import javax.sql.DataSource;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.etlserver.AbstractPostRegistrationDataSetHandlerForFileBasedUndo;
import ch.systemsx.cisd.etlserver.IDataSetUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;


/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetHandler extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo implements IDataSetUploader, IFileManager
{
    private final IEncapsulatedOpenBISService service;

    private final DataSource dataSource;

    private final TimeSeriesDataSetUploaderParameters parameters;
    
    private final TimePointDataDropBoxFeeder dropBoxFeeder;
    
    private TimeSeriesDataSetUploader uploader;

    TimeSeriesDataSetHandler(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, DBUtils.createAndInitDBContext(properties).getDataSource(), service);
    }
    
    TimeSeriesDataSetHandler(Properties properties, DataSource dataSource,
            IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.dataSource = dataSource;
        this.service = service;
        parameters = new TimeSeriesDataSetUploaderParameters(properties, true);
        dropBoxFeeder = new TimePointDataDropBoxFeeder(properties, this, service);
    }
    
    @Private TimeSeriesDataSetUploader createUploader()
    {
        return new TimeSeriesDataSetUploader(dataSource, service, parameters);
    }
    
    @Override
    public IFileOperations getFileOperations()
    {
        return super.getFileOperations();
    }
    
    @Override
    public void addFileForUndo(File file)
    {
        super.addFileForUndo(file);
    }

    public void commit()
    {
        if (uploader != null)
        {
            uploader.commit();
        }
    }

    public void rollback()
    {
        if (uploader != null)
        {
            uploader.rollback();
        }
    }

    public void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException
    {
        handle(dataSet, dataSetInformation);
    }

    public Status handle(File originalData, DataSetInformation dataSetInformation)
    {
        uploader = createUploader();
        uploader.upload(originalData, dataSetInformation, dropBoxFeeder);
        return Status.OK;
    }
    
}
