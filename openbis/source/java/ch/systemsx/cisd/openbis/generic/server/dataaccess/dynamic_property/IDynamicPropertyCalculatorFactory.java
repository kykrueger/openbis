/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.ICommonPropertyBasedHotDeployPluginFactory;

/**
 * @author Pawel Glyzewski
 */
public interface IDynamicPropertyCalculatorFactory extends
        ICommonPropertyBasedHotDeployPluginFactory<IDynamicPropertyCalculatorHotDeployPlugin>
{
    public IDynamicPropertyCalculator getCalculator(EntityTypePropertyTypePE etpt);

    public IDynamicPropertyCalculator getCalculator(PluginType pluginType, String scriptName,
            String script);

}
