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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.AbstractHashable;

/**
 * This class defines in its constructor minimum information that we must know about a
 * <code>Principal</code>.
 * <p>
 * It is also possible to put additional <code>Object</code> properties related to this
 * <code>Principal</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class Principal extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private Map<String, String> properties;

    // for serialization only
    @Deprecated
    public Principal()
    {
    }

    /**
     * Constructor which accepts mandatory parameters but no properties
     * 
     * @param userId Must not be <code>null</code>.
     * @param firstName can not be <code>null</code>.
     * @param lastName can not be <code>null</code>.
     * @param email can not be <code>null</code>.
     */
    public Principal(final String userId, final String firstName, final String lastName,
            final String email)
    {
        this(userId, firstName, lastName, email, Collections.<String, String> emptyMap());
    }

    /**
     * Standard constructor which accepts mandatory parameters and properties.
     * 
     * @param userId Must not be <code>null</code>.
     * @param firstName can not be <code>null</code>.
     * @param lastName can not be <code>null</code>.
     * @param email can not be <code>null</code>.
     * @param properties can not be <code>null</code>.
     */
    public Principal(final String userId, final String firstName, final String lastName,
            final String email, final Map<String, String> properties)
    {
        assert userId != null;
        assert firstName != null;
        assert lastName != null;
        assert email != null;
        assert properties != null;

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.properties = properties;
    }

    /**
     * Returns the id of the user.
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Returns <code>email</code>.
     */
    public final String getEmail()
    {
        return email;
    }

    /**
     * Returns <code>firstName</code>.
     */
    public final String getFirstName()
    {
        return firstName;
    }

    /**
     * Returns <code>lastName</code>.
     */
    public final String getLastName()
    {
        return lastName;
    }

    /**
     * Returns the property for given <var>key</var>, or <code>null</code>, if no property exists
     * for this <var>key</var>.
     */
    public final String getProperty(final String key)
    {
        return (properties != null) ? properties.get(key) : null;
    }

    /** Retuns an unmodifiable <code>Set</code> of present properties. */
    public final Set<String> getPropertyNames()
    {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Deprecated
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Deprecated
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    @Deprecated
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Deprecated
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @Deprecated
    public void setEmail(String email)
    {
        this.email = email;
    }

    @Deprecated
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
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