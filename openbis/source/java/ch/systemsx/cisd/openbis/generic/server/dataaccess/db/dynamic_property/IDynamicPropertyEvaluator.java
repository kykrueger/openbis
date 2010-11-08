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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;

/**
 * Interface for evaluation of dynamic properties of an entity.
 * 
 * @author Piotr Buczek
 */
public interface IDynamicPropertyEvaluator
{

    /**
     * Evaluates all dynamic properties of specified entity. Replaces placeholders with evaluated
     * values.
     */
    public <T extends IEntityInformationWithPropertiesHolder> void evaluateProperties(T entity);

    /**
     * Evaluates value of specified dynamic property on specified entity.
     * 
     * @return computed value
     */
    public String evaluateProperty(IEntityAdaptor entityAdaptor,
            EntityTypePropertyTypePE dynamicPropertyETPT);

}
