/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableData;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Allows to specify one criterion.
 * 
 * @author Izabela Adamczyk
 */
public class CriterionWidget extends HorizontalPanel
{

    private static final String PREFIX = "data_set_search_criterion";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String VALUE_FIELD_ID_PREFIX = ID + "_value";

    public static final String REMOVE_BUTTON_ID_PREFIX = ID + "_remove";

    public static final String ADD_BUTTON_ID_PREFIX = ID + "_add";

    private final CriteriaWidget parent;

    private final DataSetSearchFieldsSelectionWidget nameField;

    private final String idSuffix;

    private final TextField<String> valueField;

    private final Button removeButton;

    private int generatedChildren;

    public CriterionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            CriteriaWidget parent, String idSuffix)
    {
        this(parent, idSuffix, new DataSetSearchFieldsSelectionWidget(viewContext, idSuffix));
    }

    private CriterionWidget(final CriteriaWidget parent, String idSuffix,
            DataSetSearchFieldsSelectionWidget nameField)
    {
        generatedChildren = 0;
        this.parent = parent;
        this.idSuffix = idSuffix;
        this.nameField = nameField;
        this.valueField = new TextField<String>();
        valueField.setId(VALUE_FIELD_ID_PREFIX + idSuffix);

        final TableData tableData =
                new TableData(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);
        tableData.setPadding(1);
        add(this.nameField, tableData);
        this.nameField.setWidth(300);
        add(valueField, tableData);
        valueField.setWidth(150);
        add(createAddButton(idSuffix), tableData);
        add(removeButton = createRemoveButton(idSuffix), tableData);
    }

    /**
     * Allows to enable/disable "remove" button.
     */
    public void enableRemoveButton(boolean enable)
    {
        removeButton.setEnabled(enable);
    }

    private Button createRemoveButton(String id)
    {
        final Button button = new Button("-", new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    remove();
                }
            });
        button.setId(REMOVE_BUTTON_ID_PREFIX + id);
        return button;
    }

    private Button createAddButton(String id)
    {
        final Button button = new Button("+", new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    createNew();
                }
            });
        button.setId(ADD_BUTTON_ID_PREFIX + id);
        return button;
    }

    private String getChildId()
    {
        return idSuffix + "_" + generatedChildren;
    }

    /**
     * Adds a new {@link CriterionWidget} coping data from given the <em>name field</em>.
     */
    private void createNew()
    {
        CriterionWidget newCriterion =
                new CriterionWidget(parent, getChildId(), new DataSetSearchFieldsSelectionWidget(
                        nameField, getChildId()));
        parent.addCriterion(newCriterion);
        generatedChildren++;
    }

    private void remove()
    {
        parent.removeCriterion(this);
    }

    /**
     * Resets the state of criterion <em>name</em> and <em>value</em>.
     */
    public void reset()
    {
        valueField.reset();
        nameField.reset();
    }

    /**
     * Returns {@link DataSetSearchCriterion} for selected <em>name</em> and <em>value</em>. If
     * either <em>name</em> or <em>value</em> is not specified, returns null.
     */
    public DataSetSearchCriterion tryGetValue()
    {

        final String selectedValue = valueField.getValue();
        final DataSetSearchField selectedFieldName = nameField.tryGetSelectedField();
        if (selectedFieldName != null && StringUtils.isBlank(selectedValue) == false)
        {
            return new DataSetSearchCriterion(selectedFieldName, selectedValue);
        }
        return null;

    }

    public String tryGetDescription()
    {
        DataSetSearchCriterion criterion = tryGetValue();
        String name = nameField.tryGetSelectedCode();
        if (criterion == null || name == null)
        {
            return null;
        }

        return name + " = " + criterion.getValue();
    }

    public List<PropertyType> getAvailablePropertyTypes()
    {
        return nameField.getAvailablePropertyTypes();
    }

}