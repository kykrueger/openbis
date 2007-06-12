/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class defines in its constructor minimum information that we must know about a principal.
 * <p>
 * It is also possible to put additional <code>Object</code> properties regarding this <code>Principal</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class Principal
{
    private final String firstName;

    private final String lastName;

    private final String email;

    private final Map<String, Object> properties;

    public Principal(final String firstName, final String lastName, final String email)
    {
        assert firstName != null;
        assert lastName != null;
        assert email != null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.properties = new HashMap<String, Object>();
    }

    /** Returns <code>email</code>. */
    public final String getEmail()
    {
        return email;
    }

    /** Returns <code>firstName</code>. */
    public final String getFirstName()
    {
        return firstName;
    }

    /** Returns <code>lastName</code>. */
    public final String getLastName()
    {
        return lastName;
    }

    /** Binds given <var>property</var> to this <code>Principal</code>. */
    public final void setProperty(String key, Object property)
    {
        properties.put(key, property);
    }

    /** Returns an <code>Object</code> property for given <var>key</var>. */
    public final Object getProperty(String key)
    {
        return properties.get(key);
    }
    
    //
    // Object
    //

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}