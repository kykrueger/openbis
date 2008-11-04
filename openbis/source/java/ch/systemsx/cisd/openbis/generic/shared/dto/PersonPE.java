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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A <i>Persistence Entity</i> which represents a person.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PERSONS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.USER_COLUMN, ColumnNames.DATABASE_INSTANCE_COLUMN }) })
public final class PersonPE extends HibernateAbstractRegistrationHolder implements
        Comparable<PersonPE>, IIdHolder, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    /**
     * The user id of the system user.
     */
    public static final String SYSTEM_USER_ID = "system";

    private GroupPE homeGroup;

    private transient Long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private boolean systemUser;

    private DatabaseInstancePE databaseInstance;

    private List<RoleAssignmentPE> roleAssignments = RoleAssignmentPE.EMPTY_LIST;

    private final void setSystemUser(final boolean systemUser)
    {
        this.systemUser = systemUser;
    }

    @Column(name = ColumnNames.FIRST_NAME_COLUMN)
    @Length(max = 30, message = ValidationMessages.FIRST_NAME_LENGTH_MESSAGE)
    @Field(index = Index.TOKENIZED)
    public final String getFirstName()
    {
        return firstName;
    }

    public final void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    @Column(name = ColumnNames.LAST_NAME_COLUMN)
    @Length(max = 30, message = ValidationMessages.LAST_NAME_LENGTH_MESSAGE)
    @Field(index = Index.TOKENIZED)
    public final String getLastName()
    {
        return lastName;
    }

    public final void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    @Email(message = ValidationMessages.EMAIL_EMAIL_MESSAGE)
    @Length(max = 50, message = ValidationMessages.EMAIL_LENGTH_MESSAGE)
    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String email)
    {
        this.email = email;
    }

    @Column(name = ColumnNames.USER_COLUMN)
    @Length(max = 20, message = ValidationMessages.USER_ID_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.USER_ID_NOT_NULL_MESSAGE)
    public final String getUserId()
    {
        return userId;
    }

    public final void setUserId(final String userId)
    {
        this.userId = userId;
        setSystemUser(SYSTEM_USER_ID.equals(userId));
    }

    @Transient
    public final boolean isSystemUser()
    {
        return systemUser;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.DATABASE_INSTANCE_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.DATABASE_INSTANCE_COLUMN, updatable = false)
    public final DatabaseInstancePE getDatabaseInstance()
    {
        return databaseInstance;
    }

    public final void setDatabaseInstance(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN)
    public final GroupPE getHomeGroup()
    {
        return homeGroup;
    }

    public final void setHomeGroup(final GroupPE homeGroup)
    {
        this.homeGroup = homeGroup;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "person", cascade = CascadeType.ALL)
    public final List<RoleAssignmentPE> getRoleAssignments()
    {
        return roleAssignments;
    }

    public final void setRoleAssignments(final List<RoleAssignmentPE> rolesAssignments)
    {
        this.roleAssignments = rolesAssignments;
    }

    //
    // IIdHolder
    //

    @SequenceGenerator(name = SequenceNames.PERSON_SEQUENCE, sequenceName = SequenceNames.PERSON_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PERSON_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof PersonPE == false)
        {
            return false;
        }
        final PersonPE that = (PersonPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(userId, that.userId);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(userId);
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("userId", userId);
        builder.append("firstName", firstName);
        builder.append("lastName", lastName);
        builder.append("email", email);
        builder.append("systemUser", systemUser);
        return builder.toString();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final PersonPE o)
    {
        final String thatUserID = o.userId;
        if (userId == null)
        {
            return thatUserID == null ? 0 : -1;
        }
        if (thatUserID == null)
        {
            return 1;
        }
        return userId.compareTo(thatUserID);
    }
}
