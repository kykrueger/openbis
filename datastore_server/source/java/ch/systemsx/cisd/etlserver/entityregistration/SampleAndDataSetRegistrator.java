/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler;
import ch.systemsx.cisd.etlserver.TransferredDataSetHandler;
import ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetControlFileProcessor.ControlFileRegistrationProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Utitlity class for registering one sample/dataset combination
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrator extends AbstractSampleAndDataSetProcessor implements
        TransferredDataSetHandler.IDataSetRegistrator
{
    private final ControlFileRegistrationProperties properties;

    private final SampleDataSetPair sampleDataSetPair;

    // State that is updated during the registration
    private boolean didSucceed = false;

    private Exception failureException = null;

    SampleAndDataSetRegistrator(File folder, ControlFileRegistrationProperties properties,
            SampleDataSetPair sampleDataSetPair)
    {
        super(properties.getGlobalProperties(), folder);
        this.properties = properties;
        this.sampleDataSetPair = sampleDataSetPair;
    }

    /**
     * Commit this line. If there was a failure return the exception, otherwise return null.
     * 
     * @return An exception if one was encountered, otherwise null.
     */
    public Exception register()
    {
        File dataSetFile = new File(folder, sampleDataSetPair.getFolderName());
        Exception isEmptyException = checkDataSetFileNotEmpty(dataSetFile);
        if (null != isEmptyException)
        {
            return isEmptyException;
        }

        if (globalState.getDelegator() instanceof IExtensibleDataSetHandler)
        {
            IExtensibleDataSetHandler handler =
                    (IExtensibleDataSetHandler) globalState.getDelegator();
            handler.handleDataSet(dataSetFile, sampleDataSetPair.getDataSetInformation(), this);
            if (didSucceed)
            {
                logDataRegistered();
            }
        }
        return failureException;
    }

    private Exception checkDataSetFileNotEmpty(File dataSetFile)
    {
        if (0 == dataSetFile.list().length)
        {
            return new UserFailureException("The data set folder cannot be empty");
        }
        return null;
    }

    private void logDataRegistered()
    {
        String message = String.format("Registered sample/data set pair %s", sampleDataSetPair);
        globalState.getOperationLog().info(message);
    }

    public void registerDataSetInApplicationServer(NewExternalData data)
    {
        data.setDataSetType(properties.getDataSetType());
        if (null != sampleDataSetPair.getFileFormatTypeCode())
        {
            FileFormatType fileFormatType =
                    new FileFormatType(sampleDataSetPair.getFileFormatTypeCode());

            data.setFileFormatType(fileFormatType);
        }
        try
        {
            Sample sample =
                    globalState.getOpenbisService().registerSampleAndDataSet(
                            sampleDataSetPair.getNewSample(), data,
                            properties.getUser().getUserId());
            // Update the data set information -- it will be needed later in the data set processing
            sampleDataSetPair.getDataSetInformation().setSample(sample);
            didSucceed = true;
        } catch (UserFailureException e)
        {
            didSucceed = false;
            failureException = e;
            throw e;
        } catch (Exception e)
        {
            didSucceed = false;
            failureException = e;
            throw new CheckedExceptionTunnel(e);
        }
    }

}
