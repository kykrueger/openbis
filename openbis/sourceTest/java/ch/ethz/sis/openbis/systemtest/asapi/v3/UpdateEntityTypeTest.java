/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public abstract class UpdateEntityTypeTest<UPDATE extends IEntityTypeUpdate, TYPE extends IEntityType> extends AbstractTest
{
    protected abstract EntityKind getEntityKind();

    protected abstract UPDATE newTypeUpdate();

    protected abstract EntityTypePermId getTypeId();

    protected abstract void createEntity(String sessionToken, IEntityTypeId entityType, String propertyType, String propertyValue);

    protected abstract void updateTypes(String sessionToken, List<UPDATE> updates);

    protected abstract TYPE getType(String sessionToken, EntityTypePermId typeId);

    protected abstract void updateTypeSpecificFields(UPDATE update, int variant);

    protected abstract void assertTypeSpecificFields(TYPE type, UPDATE update, int variant);

    protected abstract String getValidationPluginOrNull(String sessionToken, EntityTypePermId typeId);

    protected abstract AbstractEntitySearchCriteria<?> createSearchCriteria(EntityTypePermId typeId);

    protected abstract List<? extends IPropertiesHolder> searchEntities(String sessionToken, AbstractEntitySearchCriteria<?> searchCriteria);

    @Test
    public void testUpdateWithUnspecifiedId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {// When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Missing type id.");
    }

    @Test
    public void testUpdateWithUnknownId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();

        update.setTypeId(new EntityTypePermId("UNDEFINED", getTypeId().getEntityKind()));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {// When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                "Object with EntityTypePermId = [" + update.getTypeId() + "] has not been found.");
    }

    @Test
    public void testUpdateWithIdWrongEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(new EntityTypePermId(typeId.getPermId(), nextEntityKind(typeId.getEntityKind())));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity kind " + typeId.getEntityKind() + " expected: ");
    }

    @Test
    public void testUpdateDescription()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setDescription("new description " + System.currentTimeMillis());
        updateTypeSpecificFields(update, 0);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        TYPE type = getType(sessionToken, typeId);
        assertEquals(type.getDescription(), update.getDescription().getValue());
        assertTypeSpecificFields(type, update, 0);
    }

    @Test
    public void testUpdateDescriptionUsingEntityTypePermIdWithoutEntityKind()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = new EntityTypePermId(getTypeId().getPermId());
        update.setTypeId(typeId);
        update.setDescription("new description " + System.currentTimeMillis());
        updateTypeSpecificFields(update, 0);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        TYPE type = getType(sessionToken, typeId);
        assertEquals(type.getDescription(), update.getDescription().getValue());
        assertTypeSpecificFields(type, update, 0);
    }

    @Test
    public void testUpdateWithValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("validateOK"));
        updateTypeSpecificFields(update, 1);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), "validateOK");
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testUpdateRemovingValidationPlugin()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("validateOK"));
        updateTypeSpecificFields(update, 1);
        updateTypes(sessionToken, Arrays.asList(update));
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), "validateOK");

        update = newTypeUpdate();
        update.setTypeId(typeId);
        update.getValidationPluginId().setValue(null);

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), null);
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testUpdateWithValidationPluginOfIncorrectType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        update.setValidationPluginId(new PluginPermId("properties"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity type validation plugin has to be of type 'Entity Validator'. "
                        + "The specified plugin with id 'properties' is of type 'Dynamic Property Evaluator'");
    }

    @Test
    public void testUpdateWithValidationPluginOfIncorrectEntityType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        EntityKind incorrectEntityKind = getIncorrectEntityKind();
        update.setValidationPluginId(new PluginPermId("test" + incorrectEntityKind));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Entity type validation plugin has entity kind set to '" + incorrectEntityKind.name()
                        + "'. Expected a plugin where entity kind is either '" + getEntityKind().name() + "' or null");
    }

    @Test
    public void testUpdateWithValidationPluginOfCorrectEntityType()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        EntityKind correctEntityKind = getCorrectEntityKind();
        String pluginPermId = null;
        if (correctEntityKind != null)
        {
            pluginPermId = "test" + correctEntityKind;
            update.setValidationPluginId(new PluginPermId(pluginPermId));
        }

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        assertEquals(getValidationPluginOrNull(sessionToken, typeId), pluginPermId);
        TYPE type = getType(sessionToken, typeId);
        assertTypeSpecificFields(type, update, 1);
    }

    @Test
    public void testAddAndRemovePropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        assignmentCreation.setMandatory(false);
        assignmentCreation.setSection("test");
        assignmentCreation.setOrdinal(3);
        assignmentCreation.setShowRawValueInForms(false);
        assignmentCreation.setShowInEditView(false);
        update.getPropertyAssignments().setForceRemovingAssignments(true);
        update.getPropertyAssignments().add(assignmentCreation);
        update.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, new PropertyTypePermId("description")));
        Map<String, String> renderedAssignments = getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken);
        renderedAssignments.remove("DESCRIPTION");
        renderedAssignments.put("SIZE", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: SIZE, mandatory: false, showInEditView: false, showRawValueInForms: false");

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        List<String> expected = getSortedRenderedAssignments(renderedAssignments);
        List<String> actual = getSortedRenderedAssignments(sessionToken);
        assertEquals(actual.toString(), expected.toString());
    }

    @Test
    public void testRemovePropertyTypeAssignmentFailsBecauseOfEntitiesWithSuchProperty()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        String propertyType = "DESCRIPTION";
        createEntity(sessionToken, typeId, propertyType, "new property");
        update.setTypeId(typeId);
        update.getPropertyAssignments().remove(new PropertyAssignmentPermId(typeId, new PropertyTypePermId(propertyType)));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Can not remove property type " + propertyType + " from type " + typeId.getPermId());
    }

    @Test
    public void testAddAlreadyExistingPropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        PropertyType propertyType = getType(sessionToken, typeId).getPropertyAssignments().get(0).getPropertyType();
        String propertyTypePermId = propertyType.getCode();
        update.setTypeId(typeId);
        PropertyAssignmentCreation assignmentCreation = new PropertyAssignmentCreation();
        assignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyTypePermId));
        update.getPropertyAssignments().add(assignmentCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                "Property type '" + propertyTypePermId + "' is already assigned to "
                        + getEntityKind().getLabel() + " type '" + typeId.getPermId() + "'.");
    }

    @Test
    public void testSetPropertyTypeAssignment()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation newCreation = new PropertyAssignmentCreation();
        newCreation.setPropertyTypeId(new PropertyTypePermId("SIZE"));
        newCreation.setInitialValueForExistingEntities("42");
        newCreation.setMandatory(true);
        newCreation.setShowRawValueInForms(true);
        newCreation.setShowInEditView(false);
        newCreation.setSection("test");
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setPropertyTypeId(new PropertyTypePermId("$PLATE_GEOMETRY"));
        replaceCreation.setMandatory(false);
        replaceCreation.setShowInEditView(true);
        update.getPropertyAssignments().set(newCreation, replaceCreation);
        update.getPropertyAssignments().setForceRemovingAssignments(true);
        Map<String, String> renderedAssignments = getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken);
        renderedAssignments.remove("DESCRIPTION");
        renderedAssignments.remove("BACTERIUM");
        renderedAssignments.remove("ANY_MATERIAL");
        renderedAssignments.remove("ORGANISM");
        renderedAssignments.remove("DELETION_TEST");
        renderedAssignments.remove("COMMENT");
        renderedAssignments.remove("COMPOUND_HCS");
        renderedAssignments.remove("GENE_SYMBOL");
        renderedAssignments.put("SIZE", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: SIZE, mandatory: true, showInEditView: false, showRawValueInForms: true");
        renderedAssignments.put("$PLATE_GEOMETRY", "PropertyAssignment entity type: " + typeId.getPermId()
                + ", property type: $PLATE_GEOMETRY, mandatory: false, showInEditView: true, showRawValueInForms: false");

        // When
        updateTypes(sessionToken, Arrays.asList(update));

        // Then
        List<String> expected = getSortedRenderedAssignments(renderedAssignments);
        List<String> actual = getSortedRenderedAssignments(sessionToken);
        assertEquals(actual.toString(), expected.toString());
    }

    @Test
    public void testReplacingPropertyTypeWithPropertyTypeIdNull()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setMandatory(false);
        update.getPropertyAssignments().set(replaceCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "PropertyTypeId cannot be null.");
    }

    @Test
    public void testReplacingPropertyTypeWithUnknownPropertyTypeIdClass()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        UPDATE update = newTypeUpdate();
        EntityTypePermId typeId = getTypeId();
        update.setTypeId(typeId);
        PropertyAssignmentCreation replaceCreation = new PropertyAssignmentCreation();
        replaceCreation.setPropertyTypeId(new IPropertyTypeId()
            {
                private static final long serialVersionUID = 1L;
            });
        update.getPropertyAssignments().set(replaceCreation);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            },
                // Then
                "Unknown type of property type id: ch.ethz.sis.openbis.systemtest.asapi.v3.UpdateEntityTypeTest$");
    }

    @Test(dataProvider = "usersNotAllowedToUpdate")
    public void testUpdateWithUserCausingAuthorizationFailure(final String user)
    {
        EntityTypePermId typeId = getTypeId();
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    UPDATE update = newTypeUpdate();
                    update.setTypeId(typeId);
                    update.setDescription("new description " + System.currentTimeMillis());

                    // When
                    updateTypes(sessionToken, Arrays.asList(update));
                }
            }, typeId, patternContains("checking access"));
    }

    @DataProvider
    Object[][] usersNotAllowedToUpdate()
    {
        return createTestUsersProvider(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
    }

    protected <T> T getNewValue(FieldUpdateValue<T> fieldUpdateValue, T currentValue)
    {
        return fieldUpdateValue != null && fieldUpdateValue.isModified() ? fieldUpdateValue.getValue() : currentValue;
    }

    private EntityKind getIncorrectEntityKind()
    {
        if (EntityKind.EXPERIMENT.equals(getEntityKind()))
        {
            return EntityKind.SAMPLE;
        } else
        {
            return EntityKind.EXPERIMENT;
        }
    }

    private EntityKind getCorrectEntityKind()
    {
        if (EntityKind.EXPERIMENT.equals(getEntityKind()))
        {
            return EntityKind.EXPERIMENT;
        } else if (EntityKind.SAMPLE.equals(getEntityKind()))
        {
            return EntityKind.SAMPLE;
        } else
        {
            return null;
        }
    }

    private List<String> getSortedRenderedAssignments(String sessionToken)
    {
        return getSortedRenderedAssignments(getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(sessionToken));
    }

    private List<String> getSortedRenderedAssignments(Map<String, String> currentRenderedPropertyAssignments)
    {
        List<String> renderedAssignments = new ArrayList<String>(currentRenderedPropertyAssignments.values());
        Collections.sort(renderedAssignments);
        return renderedAssignments;
    }

    private Map<String, String> getCurrentRenderedPropertyAssignmentsByPropertyTypeCode(String sessionToken)
    {
        Map<String, String> result = new HashMap<String, String>();
        TYPE type = getType(sessionToken, getTypeId());
        List<PropertyAssignment> assignments = type.getPropertyAssignments();
        for (PropertyAssignment propertyAssignment : assignments)
        {
            PropertyType propertyType = propertyAssignment.getPropertyType();
            String code = propertyType.getCode();
            StringBuilder builder = new StringBuilder(propertyAssignment.toString());
            builder.append(", showInEditView: ").append(propertyAssignment.isShowInEditView());
            builder.append(", showRawValueInForms: ").append(propertyAssignment.isShowRawValueInForms());
            result.put(code, builder.toString());
        }
        return result;
    }

    private ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind nextEntityKind(
            ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind entityKind)
    {
        ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind[] values =
                ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.values();
        return values[(entityKind.ordinal() + 1) % values.length];
    }

}
