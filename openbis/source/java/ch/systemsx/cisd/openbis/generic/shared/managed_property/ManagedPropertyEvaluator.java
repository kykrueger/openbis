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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Class for evaluating scripts that control managed properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2011-01-14, Piotr Buczek: it should be possible to reuse evaluator by set of properties
public class ManagedPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ManagedPropertyEvaluator.class);

    private final String scriptExpression;

    /**
     * The name of the script that expects the property to be there and updates ui configuration.
     */
    private static final String CONFIGURE_UI_EXPRESSION = "configureUI()";

    /**
     * The name of the script that expects the property to be there and updates the value.
     */
    private static final String UPDATE_VALUE_EXPRESSION = "updateValue()";

    private static final String PROPERTY_VARIABLE_NAME = "property";

    public ManagedPropertyEvaluator(String scriptExpression)
    {
        this.scriptExpression = scriptExpression;
    }

    public void evalConfigureProperty(IManagedProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property ui configuration'%s'.",
                    managedProperty));
        }

        Evaluator evaluator =
                new Evaluator(CONFIGURE_UI_EXPRESSION, ScriptUtilityFactory.class, scriptExpression);
        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.eval();
    }

    public void evaluateUpdateProperty(IManagedProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property value update '%s'.",
                    managedProperty));
        }

        Evaluator evaluator =
                new Evaluator(UPDATE_VALUE_EXPRESSION, ScriptUtilityFactory.class, scriptExpression);
        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.eval();
    }

    //
    // utlilities
    //

    /**
     * This utility class should shouldn't expose anything outside of Managed Property API. No
     * method should return or take as an argument anything outside of the API.
     */
    public static class ScriptUtilityFactory
    {
        public static ISimpleTableModelBuilderAdaptor createTableBuilder()
        {
            return SimpleTableModelBuilderAdaptor.create();
        }
    }
}
