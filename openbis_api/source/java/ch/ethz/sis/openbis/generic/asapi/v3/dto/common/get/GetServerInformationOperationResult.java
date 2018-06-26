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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.get.GetServerInformationOperationResult")
public class GetServerInformationOperationResult implements IOperationResult
{

    private static final long serialVersionUID = 1L;

    private Map<String, String> serverInformation;

    @SuppressWarnings("unused")
    private GetServerInformationOperationResult()
    {
    }

    public GetServerInformationOperationResult(Map<String, String> serverInformation)
    {
        this.serverInformation = serverInformation;
    }

    public Map<String, String> getServerInformation()
    {
        return serverInformation;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

}
