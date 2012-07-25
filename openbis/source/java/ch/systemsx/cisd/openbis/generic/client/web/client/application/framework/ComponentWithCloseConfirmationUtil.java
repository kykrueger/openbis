/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

/**
 * @author pkupczyk
 */
public class ComponentWithCloseConfirmationUtil
{

    public static boolean isComponentWithCloseConfirmation(Object object)
    {
        return object instanceof IComponentWithCloseConfirmation;
    }

    public static IComponentWithCloseConfirmation asComponentWithCloseConfirmation(Object object)
    {
        if (isComponentWithCloseConfirmation(object))
        {
            return (IComponentWithCloseConfirmation) object;
        } else
        {
            return null;
        }
    }

    public static boolean shouldAskForCloseConfirmation(Object object)
    {
        if (isComponentWithCloseConfirmation(object))
        {
            return asComponentWithCloseConfirmation(object).shouldAskForCloseConfirmation();
        } else
        {
            return false;
        }
    }

}
