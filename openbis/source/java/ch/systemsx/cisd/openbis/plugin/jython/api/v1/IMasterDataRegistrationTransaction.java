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

package ch.systemsx.cisd.openbis.plugin.jython.api.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IMasterDataRegistrationTransaction
{

    IExperimentType createNewExperimentType(String code);

    IExperimentTypeImmutable getExperimentType(String code);

    ISampleType createNewSampleType(String code);

    ISampleTypeImmutable getSampleType(String code);

    IDataSetType createNewDataSetType(String code);

    IDataSetTypeImmutable getDataSetType(String code);

    IMaterialType createNewMaterialType(String code);

    IMaterialTypeImmutable getMaterialType(String code);

    IPropertyType createNewPropertyType(String code, DataType dataType);

    IPropertyTypeImmutable getPropertyType(String code);

    IPropertyAssignment assignToExperimentType(IExperimentTypeImmutable experimentType,
            IPropertyTypeImmutable propertyType);

    IPropertyAssignment assignToSampleType(ISampleTypeImmutable sampleType,
            IPropertyTypeImmutable propertyType);

    IPropertyAssignment assignToDataSetType(IDataSetTypeImmutable dataSetType,
            IPropertyTypeImmutable propertyType);

    IPropertyAssignment assignToMaterialType(IMaterialTypeImmutable materialType,
            IPropertyTypeImmutable propertyType);

    // controlled vocabularies not yet implemented
    // IVocabulary createNewVocabulary(String code)
    // IVocabularyImmutable getVocabulary(String code)

    // FileTypes not yet implemented
    // IFileType createFileType(String code)
    // IFileTypeImmutable getFileType(String code)
}
