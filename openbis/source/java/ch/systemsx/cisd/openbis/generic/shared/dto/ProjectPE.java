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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A <i>Persistence Entity</i> which represents a project.
 * 
 * @author Christian Ribeaud
 */
@Entity
@Table(name = TableNames.PROJECTS_TABLE, uniqueConstraints =
    { @UniqueConstraint(columnNames =
        { ColumnNames.CODE_COLUMN, ColumnNames.GROUP_COLUMN }) })
public final class ProjectPE extends HibernateAbstractRegistratrationHolder implements
        Comparable<ProjectPE>, IIdAndCodeHolder, Serializable
{
    public static final ProjectPE[] EMPTY_ARRAY = new ProjectPE[0];

    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private transient Long id;

    private GroupPE group;

    private String code;

    private String description;

    private PersonPE projectLeader;

    /** The number of experiments this project contains. */
    private int size;

    private DataStorePE dataStore;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.DATA_STORE_COLUMN, updatable = false)
    public final DataStorePE getDataStore()
    {
        return dataStore;
    }

    public final void setDataStore(final DataStorePE dataStorePE)
    {
        this.dataStore = dataStorePE;
    }

    public final void setCode(final String code)
    {
        this.code = code;
    }

    /**
     * Sets the group which this <code>ProjectDTO</code> is related to.
     * 
     * @throws AssertionError if <code>groupId</code> is defined but unequal
     *             <code>group.getId()</code>.
     */
    public final void setGroup(final GroupPE group)
    {
        this.group = group;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.GROUP_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.GROUP_COLUMN, updatable = false)
    public final GroupPE getGroup()
    {
        return group;
    }

    @Transient
    public final int getSize()
    {
        return size;
    }

    public final void setSize(final int size)
    {
        this.size = size;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = ColumnNames.PERSON_LEADER_COLUMN, updatable = false)
    public final PersonPE getProjectLeader()
    {
        return projectLeader;
    }

    public final void setProjectLeader(final PersonPE projectLeader)
    {
        this.projectLeader = projectLeader;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = 1000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
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
        if (obj instanceof ProjectPE == false)
        {
            return false;
        }
        final ProjectPE that = (ProjectPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("description", getDescription());
        return builder.toString();
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final ProjectPE o)
    {
        return AbstractIdAndCodeHolder.compare(this, o);
    }

    //
    // IIdHolder
    //

    @SequenceGenerator(name = SequenceNames.PROJECT_SEQUENCE, sequenceName = SequenceNames.PROJECT_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.PROJECT_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Column(unique = true)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 40, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public final String getCode()
    {
        return code;
    }
}
