/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.GroupTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SamplePropertyTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_SERVICE)
public final class GenericClientService extends AbstractClientService implements
        IGenericClientService
{
    @Resource(name = ResourceNames.GENERIC_SERVER)
    private IGenericServer genericServer;

    private final static RoleCode translateRoleSetCode(final String code)
    {
        if ("INSTANCE_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("GROUP_ADMIN".compareTo(code) == 0)
        {
            return RoleCode.ADMIN;
        } else if ("USER".compareTo(code) == 0)
        {
            return RoleCode.USER;
        } else if ("OBSERVER".compareTo(code) == 0)
        {
            return RoleCode.OBSERVER;
        } else
        {
            throw new IllegalArgumentException("Unknown role set");
        }
    }

    private final List<SampleIdentifier> extractIdentifiers(final List<Sample> samples)
    {
        final List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
        for (final Sample s : samples)
        {
            identifiers.add(extractIdentifier(s));
        }
        return identifiers;
    }

    private final SampleIdentifier extractIdentifier(final Sample s)
    {
        if (s.getGroup() != null)
        {
            final String groupInstanceCode =
                    s.getGroup().getInstance() == null ? null : s.getGroup().getInstance()
                            .getCode();
            final String groupCode = s.getGroup().getCode();
            final GroupIdentifier groupIdentifer =
                    new GroupIdentifier(new DatabaseInstanceIdentifier(groupInstanceCode),
                            groupCode);
            return new SampleIdentifier(groupIdentifer, s.getCode());
        } else
        {
            final String instanceCode = s.getDatabaseInstance().getCode();
            return new SampleIdentifier(new DatabaseInstanceIdentifier(instanceCode), s.getCode());
        }
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return genericServer;
    }

    //
    // IGenericClientService
    //

    public final List<Group> listGroups(final String databaseInstanceCode)
    {
        try
        {
            final DatabaseInstanceIdentifier identifier =
                    new DatabaseInstanceIdentifier(databaseInstanceCode);
            final List<Group> result = new ArrayList<Group>();
            final List<GroupPE> groups = genericServer.listGroups(getSessionToken(), identifier);
            for (final GroupPE group : groups)
            {
                result.add(GroupTranslator.translate(group));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroup(final String groupCode, final String descriptionOrNull,
            final String groupLeaderOrNull)
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerGroup(sessionToken, groupCode, descriptionOrNull,
                    groupLeaderOrNull);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            final List<Person> result = new ArrayList<Person>();
            final List<PersonPE> persons = genericServer.listPersons(getSessionToken());
            for (final PersonPE person : persons)
            {
                result.add(PersonTranslator.translate(person));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerPerson(final String code)
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerPerson(sessionToken, code);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<RoleAssignment> listRoles()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<RoleAssignment> result = new ArrayList<RoleAssignment>();
            final List<RoleAssignmentPE> roles = genericServer.listRoles(getSessionToken());
            for (final RoleAssignmentPE role : roles)
            {
                result.add(RoleAssignmentTranslator.translate(role));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            genericServer.registerGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerInstanceRole(sessionToken, translateRoleSetCode(roleSetCode),
                    person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void deleteGroupRole(final String roleSetCode, final String group,
            final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final GroupIdentifier groupIdentifier =
                    new GroupIdentifier(DatabaseInstanceIdentifier.HOME, group);
            final String sessionToken = getSessionToken();
            genericServer.deleteGroupRole(sessionToken, translateRoleSetCode(roleSetCode),
                    groupIdentifier, person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final void deleteInstanceRole(final String roleSetCode, final String person)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.deleteInstanceRole(sessionToken, translateRoleSetCode(roleSetCode),
                    person);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<SampleType> listSampleTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<SampleTypePE> sampleTypes = genericServer.listSampleTypes(getSessionToken());
            final List<SampleType> result = new ArrayList<SampleType>();
            for (final SampleTypePE sampleTypePE : sampleTypes)
            {
                result.add(SampleTypeTranslator.translate(sampleTypePE));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<Sample> listSamples(final SampleType sampleType, final String groupCode,
            final boolean includeGroup, final boolean includeInstance,
            final List<PropertyType> propertyCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final List<SampleOwnerIdentifier> ownerIdentifiers =
                    new ArrayList<SampleOwnerIdentifier>();
            if (includeGroup)
            {
                ownerIdentifiers.add(new SampleOwnerIdentifier(new GroupIdentifier(
                        DatabaseInstanceIdentifier.HOME, groupCode)));
            }
            if (includeInstance)
            {
                ownerIdentifiers.add(new SampleOwnerIdentifier(DatabaseInstanceIdentifier
                        .createHome()));
            }
            final List<SamplePE> samplePEs =
                    genericServer.listSamples(getSessionToken(), ownerIdentifiers,
                            SampleTypeTranslator.translate(sampleType));

            final List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
            for (final SamplePE s : samplePEs)
            {
                identifiers.add(s.getSampleIdentifier());
            }
            final Map<SampleIdentifier, List<SamplePropertyPE>> samplesProperties =
                    genericServer.listSamplesProperties(getSessionToken(), identifiers,
                            PropertyTypeTranslator.translate(propertyCodes));

            final List<Sample> result = new ArrayList<Sample>();
            for (final SamplePE sample : samplePEs)
            {
                result.add(SampleTranslator.translate(sample, sampleType, samplesProperties));
            }
            return result;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public List<Sample> updateSamples(final List<Sample> samples,
            final List<PropertyType> propertyCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        if (samples.size() == 0 || propertyCodes.size() == 0)
        {
            return samples;
        }
        final List<SampleIdentifier> identifiers = extractIdentifiers(samples);
        // TODO 2008-10-22, Izabela Adamczyk: make sample type the parameter of the method
        final SampleType sampleType = samples.get(0).getSampleType();
        final Map<SampleIdentifier, List<SamplePropertyPE>> samplesProperties =
                genericServer.listSamplesProperties(getSessionToken(), identifiers,
                        PropertyTypeTranslator.translate(propertyCodes));
        for (final Sample s : samples)
        {
            final List<SamplePropertyPE> list = samplesProperties.get(extractIdentifier(s));
            s.getProperties().addAll(
                    list == null ? new ArrayList<SampleProperty>() : SamplePropertyTranslator
                            .translate(list, sampleType));
        }
        return samples;
    }

    public final SampleGeneration getSampleInfo(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final SampleGenerationDTO sampleGeneration =
                    genericServer.getSampleInfo(getSessionToken(), identifier);
            return BeanUtils.createBean(SampleGeneration.class, sampleGeneration, DtoConverters
                    .getSampleConverter());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }
}
