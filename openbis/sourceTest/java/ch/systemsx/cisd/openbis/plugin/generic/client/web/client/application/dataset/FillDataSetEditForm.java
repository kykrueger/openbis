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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for editing data set.
 * 
 * @author Piotr Buczek
 */
public final class FillDataSetEditForm extends AbstractDefaultTestCommand
{
    private final String formId;

    private final List<PropertyField> properties;

    // private String newSampleOrNull;

    private String modifiedParentsOrNull;

    // private String newFileFormatTypeOrNull;

    public FillDataSetEditForm()
    {
        this(TechId.createWildcardTechId());
    }

    private FillDataSetEditForm(final TechId dataSetId)
    {
        this.formId = GenericDataSetEditForm.createId(dataSetId, EntityKind.DATA_SET);
        this.properties = new ArrayList<PropertyField>();
        addCallbackClass(GenericDataSetEditForm.DataSetInfoCallback.class);
        addCallbackClass(GenericDataSetEditForm.ListParentsCallback.class);
    }

    public final FillDataSetEditForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    public final void execute()
    {
        String simpleId = formId.substring(GenericDataSetEditForm.ID_PREFIX.length());
        for (final PropertyField property : properties)
        {
            final Widget widget =
                    GWTTestUtil.getWidgetWithID(formId + property.getPropertyFieldId());
            if (widget instanceof Field<?>)
            {
                ((Field<?>) widget).setRawValue(property.getPropertyFieldValue());
            } else
            {
                throw new IllegalStateException("Wrong widget type");
            }
        }
        if (modifiedParentsOrNull != null)
        {
            System.err.println(modifiedParentsOrNull);
            final TextArea parentsField =
                    (TextArea) GWTTestUtil.getWidgetWithID(DataSetParentsArea.createId(simpleId));
            parentsField.setRawValue(modifiedParentsOrNull);
        }
        GWTTestUtil.clickButtonWithID(formId + AbstractRegistrationForm.SAVE_BUTTON);
    }

    public final FillDataSetEditForm modifyParents(final String modifiedParents)
    {
        this.modifiedParentsOrNull = modifiedParents;
        return this;
    }

}
