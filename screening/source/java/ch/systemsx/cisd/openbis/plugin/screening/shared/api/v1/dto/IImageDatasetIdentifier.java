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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeName;



/**
 * Contains data which uniquely define an image dataset.
 * 
 * @author Tomasz Pylak
 */
@JsonTypeName("IImageDatasetIdentifier")
@JsonSubTypes(value = {@JsonSubTypes.Type(ImageDatasetReference.class)})
public interface IImageDatasetIdentifier extends IDatasetIdentifier
{
}
