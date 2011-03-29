/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDataSetRegistrationDetailsFactory<T extends DataSetInformation>
        implements IDataSetRegistrationDetailsFactory<T>
{
    protected final OmniscientTopLevelDataSetRegistratorState registratorState;

    protected final DataSetInformation userProvidedDataSetInformationOrNull;

    public AbstractDataSetRegistrationDetailsFactory(
            OmniscientTopLevelDataSetRegistratorState registratorState,
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        this.registratorState = registratorState;
        this.userProvidedDataSetInformationOrNull = userProvidedDataSetInformationOrNull;
    }

    /**
     * Factory method that creates a new registration details object.
     */
    public DataSetRegistrationDetails<T> createDataSetRegistrationDetails()
    {
        DataSetRegistrationDetails<T> registrationDetails = new DataSetRegistrationDetails<T>();
        T dataSetInfo = createDataSetInformation();
        if (null != userProvidedDataSetInformationOrNull)
        {
            applyUserProvidedValues(dataSetInfo);
        }
        if (null == dataSetInfo.getDataSetType())
        {
            setDataSetTypeToDefaultValue(dataSetInfo);
        }
        setDatabaseInstance(dataSetInfo);
        registrationDetails.setDataSetInformation(dataSetInfo);
        return registrationDetails;
    }

    public DataSet<T> createDataSet(DataSetRegistrationDetails<T> registrationDetails,
            File stagingFile)
    {
        return new DataSet<T>(registrationDetails, stagingFile);
    }

    /**
     * The field userProvidedDataSetInformationOrNull is non-null. Apply the values to the
     * dataSetInfo. Subclasses may override.
     */
    protected void applyUserProvidedValues(T dataSetInfo)
    {
        SampleIdentifier sampleId = userProvidedDataSetInformationOrNull.getSampleIdentifier();
        if (null != sampleId)
        {
            dataSetInfo.setSampleCode(sampleId.getSampleCode());
            dataSetInfo.setSpaceCode(sampleId.getSpaceLevel().getSpaceCode());
            dataSetInfo.setInstanceCode(sampleId.getSpaceLevel().getDatabaseInstanceCode());
        }

        ExperimentIdentifier experimentId =
                userProvidedDataSetInformationOrNull.getExperimentIdentifier();
        if (null != experimentId)
        {
            dataSetInfo.setExperimentIdentifier(experimentId);
        }

        DataSetType type = userProvidedDataSetInformationOrNull.getDataSetType();
        if (null != type)
        {
            dataSetInfo.setDataSetType(type);
        }

        List<NewProperty> props = userProvidedDataSetInformationOrNull.getDataSetProperties();
        if (false == props.isEmpty())
        {
            dataSetInfo.setDataSetProperties(props);
        }
    }

    /**
     * Set the data set type value to the default value. Subclasses may override.
     */
    protected void setDataSetTypeToDefaultValue(T dataSetInfo)
    {
        dataSetInfo.setDataSetType(new DataSetType(DataSetTypeCode.UNKNOWN.getCode()));
    }

    protected final void setDatabaseInstance(DataSetInformation dataSetInfo)
    {
        dataSetInfo.setInstanceCode(registratorState.getHomeDatabaseInstance().getCode());
        dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());
    }

    /**
     * Factory method that creates a new data set information object.
     */
    protected abstract T createDataSetInformation();

}
