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

package ch.systemsx.cisd.openbis.systemtest.base.auth;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author anttil
 */
public class RuleBuilder
{

    public static AuthorizationRule rule(GuardedDomain domain, RoleWithHierarchy role)
    {
        return new BasicAuthorizationRule(domain, role);
    }

    public static AuthorizationRule and(AuthorizationRule rule1, AuthorizationRule rule2,
            AuthorizationRule... rest)
    {
        AuthorizationRule main = new AndAuthorizationRule(rule1, rule2);
        for (AuthorizationRule rule : rest)
        {
            main = new AndAuthorizationRule(main, rule);
        }
        return main;
    }

    public static AuthorizationRule or(AuthorizationRule rule1, AuthorizationRule rule2,
            AuthorizationRule... rest)
    {
        AuthorizationRule main = new OrAuthorizationRule(rule1, rule2);
        for (AuthorizationRule rule : rest)
        {
            main = new OrAuthorizationRule(main, rule);
        }
        return main;
    }

    public static AuthorizationRule not(AuthorizationRule rule)
    {
        return new NotAuthorizationRule(rule);
    }

}
