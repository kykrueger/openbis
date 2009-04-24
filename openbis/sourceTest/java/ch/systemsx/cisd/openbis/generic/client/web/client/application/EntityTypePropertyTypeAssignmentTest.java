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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PropertyTypeAssignmentColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.CheckPropertyTypeAssignmentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.FillPropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A {@link AbstractGWTTestCase} extension to test Entity Property Type Assignment.
 * 
 * @author Izabela Adamczyk
 */
public class EntityTypePropertyTypeAssignmentTest extends AbstractGWTTestCase
{

    private static final String CISD = "CISD";

    private static final String CONTROL_LAYOUT_C1 = "C1";

    private static final String NO_COMMENT = "No comment.";

    private static final String COMPOUND_HCS = "COMPOUND_HCS";

    private static final String USER_COMMENT = "USER.COMMENT";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    private static final EntityKind EXPERIMENT = EntityKind.EXPERIMENT;

    private static final EntityKind SAMPLE = EntityKind.SAMPLE;

    private final void prepareListingAfterAssignment(String propertyTypeCode,
            String entityTypeCode, EntityKind entityKind, int expectedEntries, boolean isMandatory)
    {
        remoteConsole.prepare(new OpenTab(ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS,
                PropertyTypeAssignmentForm.AssignPropertyTypeCallback.class));
        CheckPropertyTypeAssignmentTable table = new CheckPropertyTypeAssignmentTable();
        table.expectedRow(new Row().withCell(
                PropertyTypeAssignmentColDefKind.PROPERTY_TYPE_CODE.id(), propertyTypeCode)
                .withCell(PropertyTypeAssignmentColDefKind.ENTITY_TYPE_CODE.id(), entityTypeCode)
                .withCell(PropertyTypeAssignmentColDefKind.ENTITY_KIND.id(),
                        entityKind.getDescription()).withCell(
                        PropertyTypeAssignmentColDefKind.IS_MANDATORY.id(),
                        SimpleYesNoRenderer.render(isMandatory)));
        remoteConsole.prepare(table.expectedSize(expectedEntries));
    }

    public final void testAssignExperimentPropertyType()
    {
        loginAndGotoTab(ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE);
        final boolean mandatory = true;
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(mandatory, USER_COMMENT,
                COMPOUND_HCS, "a comment", EXPERIMENT));
        prepareListingAfterAssignment(USER_COMMENT, COMPOUND_HCS, EXPERIMENT, 30, mandatory);
        launchTest(20000);
    }

    public final void testGlobalValueAssignmentSamplePropertyType()
    {
        loginAndGotoTab(ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE);
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(false, USER_COMMENT,
                CONTROL_LAYOUT, NO_COMMENT, SAMPLE));
        remoteConsole.prepare(new OpenTab(ActionMenuKind.SAMPLE_MENU_BROWSE,
                PropertyTypeAssignmentForm.AssignPropertyTypeCallback.class));
        remoteConsole.prepare(new ListSamples(CISD, CONTROL_LAYOUT));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow(CONTROL_LAYOUT_C1).identifier(CISD, CISD).valid()
                .withUserPropertyCell(USER_COMMENT, NO_COMMENT));
        launchTest(20000);
    }
}
