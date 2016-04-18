/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.jython.evaluator;

import org.springframework.beans.factory.BeanInitializationException;

import ch.systemsx.cisd.common.jython.v25.Jython25EvaluatorFactory;
import ch.systemsx.cisd.common.jython.v27.Jython27EvaluatorFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author Jakub Straszewski
 */
public class JythonEvaluatorSpringComponent
{
    public JythonEvaluatorSpringComponent(ExposablePropertyPlaceholderConfigurer propertyConfigurer)
    {
        String jythonVersion = propertyConfigurer.getResolvedProps().getProperty("jython-version");
        if ("2.7".equals(jythonVersion))
        {
            Evaluator.setFactory(new Jython27EvaluatorFactory());
        } else if ("2.5".equals(jythonVersion))
        {
            Evaluator.setFactory(new Jython25EvaluatorFactory());
        } else
        {
            throw new BeanInitializationException(
                    "The jython-version property must be specified in service.properties - possible values are 2.5 and 2.7. Since openBIS version 16.04 recommended jython version is 2.7.");
        }
    }
}
