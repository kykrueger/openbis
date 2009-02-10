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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.TableData;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchField;

/**
 * @author Izabela Adamczyk
 */
public class CriterionWidget extends HorizontalPanel
{

    private final CriteriaWidget parent;

    private DataSetSearchFieldsSelectionWidget nameField;

    private final String idSuffix;

    private int generatedChildren;

    private TextField<String> valueField;

    private Button removeButton;

    public CriterionWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            CriteriaWidget parent, String idSuffix)
    {
        this(parent, idSuffix, new DataSetSearchFieldsSelectionWidget(viewContext, idSuffix));
    }

    public CriterionWidget(CriteriaWidget parent, String idSuffix,
            DataSetSearchFieldsSelectionWidget nameField)
    {
        generatedChildren = 0;
        this.parent = parent;
        this.idSuffix = idSuffix;
        final TableData tableData =
                new TableData(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);
        tableData.setPadding(1);
        add(this.nameField = nameField, tableData);
        nameField.setWidth(300);
        add(valueField = new TextField<String>(), tableData);
        valueField.setWidth(150);
        add(createAddButton(), tableData);
        add(removeButton = createRemoveButton(), tableData);
    }

    public void enableRemoveButton(boolean enable)
    {
        removeButton.setEnabled(enable);
    }

    private Button createRemoveButton()
    {
        return new Button("-")
            {
                @Override
                protected void onClick(ComponentEvent ce)
                {
                    super.onClick(ce);
                    remove();
                }
            };
    }

    private Button createAddButton()
    {
        return new Button("+")
            {
                @Override
                protected void onClick(ComponentEvent ce)
                {
                    super.onClick(ce);
                    createNew();
                }
            };
    }

    String getChildId()
    {
        return idSuffix + "_" + generatedChildren;
    }

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

    public void reset()
    {
        valueField.reset();
        nameField.reset();
    }

    public DataSetSearchCriterion tryGetValue()
    {

        final String selectedValue = valueField.getValue();
        final DataSetSearchField selectedFieldName = nameField.tryGetSelectedField();
        if (selectedFieldName != null && StringUtils.isBlank(selectedValue) == false)
        {
            final DataSetSearchCriterion result = new DataSetSearchCriterion();
            result.setField(selectedFieldName);
            result.setValue(selectedValue);
            return result;
        }
        return null;

    }
}