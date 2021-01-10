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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.plugin.evaluate.EvaluatePluginOperationResult")
public class EvaluatePluginOperationResult implements IOperationResult
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PluginEvaluationResult result;

    @SuppressWarnings("unused")
    private EvaluatePluginOperationResult()
    {
    }

    public EvaluatePluginOperationResult(PluginEvaluationResult result)
    {
        this.result = result;
    }

    @JsonIgnore
    public PluginEvaluationResult getResult()
    {
        return result;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("result", result).toString();
    }

}