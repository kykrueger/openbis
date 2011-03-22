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

package ch.systemsx.cisd.openbis.generic.client.console;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Franz-Josef Elmer
 */
public class RegisterPropertyType implements ICommand
{

    private static final String WITH_DATA_TYPE = " with data type ";

    @SuppressWarnings("unused")
    private static final String LABEL = ", label = ";

    public void execute(ICommonServer server, String sessionToken, ScriptContext context,
            String argument)
    {
        PropertyType propertyType = new PropertyType();
        int indexOfWithDataType = argument.indexOf(WITH_DATA_TYPE);
        if (indexOfWithDataType < 0)
        {
            throw new IllegalArgumentException("'with data type' misspelled");
        }
        String propertyCode = argument.substring(0, indexOfWithDataType);
        propertyType.setCode(propertyCode);
        String dataTypeCode = argument.substring(indexOfWithDataType + WITH_DATA_TYPE.length());
        propertyType.setLabel(propertyCode);
        propertyType.setDescription(" ");
        DataTypeCode dataType = DataTypeCode.valueOf(dataTypeCode);
        propertyType.setDataType(new DataType(dataType));
        server.registerPropertyType(sessionToken, propertyType);

    }

}
