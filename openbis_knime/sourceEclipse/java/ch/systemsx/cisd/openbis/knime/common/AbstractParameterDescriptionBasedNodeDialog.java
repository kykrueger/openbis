/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.knime.server.FieldType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * Abstract super class of node dialogs with a description and a set of parameters different for each description.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractParameterDescriptionBasedNodeDialog<D extends Serializable>
        extends AbstractDescriptionBasedNodeDialog<D>
{
    private ParameterBindings parameterBindings = new ParameterBindings();

    private Map<String, IField> parameterFields = new HashMap<String, IField>();

    private JPanel parametersPanel;

    protected AbstractParameterDescriptionBasedNodeDialog(String tabTitle)
    {
        super(tabTitle);
    }

    protected abstract List<FieldDescription> getFieldDescriptions(D description);

    protected abstract String getDescriptionComboBoxLabel();

    protected String getParametersSectionLabel()
    {
        return getDescriptionComboBoxLabel() + " Parameters";
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel, JComboBox comboBoxWithDescriptions)
    {
        comboBoxWithDescriptions.addItemListener(new ItemListener()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        try
                        {
                            updateParametersPanel((D) e.getItem());
                        } catch (Throwable ex)
                        {
                            showException(ex);
                        }
                    }
                }
            });
        JPanel querySelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        querySelectionPanel.add(new JLabel(getDescriptionComboBoxLabel() + ":"));
        querySelectionPanel.add(comboBoxWithDescriptions);
        queryPanel.add(querySelectionPanel, BorderLayout.NORTH);
        parametersPanel = new JPanel(new GridBagLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel northWestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northWestPanel.setBorder(BorderFactory.createTitledBorder(getParametersSectionLabel()));
        northWestPanel.add(parametersPanel);
        northPanel.add(northWestPanel, BorderLayout.NORTH);
        queryPanel.add(northPanel, BorderLayout.CENTER);
    }

    private void updateParametersPanel(final D description)
    {
        new ActionExecutor().executeAsync(new ILoadingBuildingAction<List<FieldDescription>>()
            {
                @Override
                public List<FieldDescription> load()
                {
                    return getFieldDescriptions(description);
                }

                @Override
                public void build(List<FieldDescription> fieldDescriptions)
                {
                    parametersPanel.removeAll();
                    parameterFields.clear();
                    parametersPanel.getParent().setVisible(fieldDescriptions.isEmpty() == false);
                    for (FieldDescription fieldDescription : fieldDescriptions)
                    {
                        String name = fieldDescription.getName();
                        IField field = parameterFields.get(name);
                        if (field == null)
                        {
                            String fieldParameters = fieldDescription.getFieldParameters();
                            FieldType fieldType = fieldDescription.getFieldType();
                            field = createField(fieldType, fieldParameters);
                            parameterFields.put(name, field);
                        }
                        String value = parameterBindings.tryToGetBinding(name);
                        if (value != null)
                        {
                            field.setValue(value);
                        }
                        addField(parametersPanel, name, field.getComponent());
                    }
                    parametersPanel.invalidate();
                    parametersPanel.getParent().getParent().validate();
                }
            }, new DefaultAsyncNodeAction(this));
    }

    private IField createField(FieldType fieldType, String fieldParameters)
    {
        IQueryApiFacadeProvider facadeProvider = new IQueryApiFacadeProvider()
            {
                @Override
                public IQueryApiFacade getQueryFacade()
                {
                    return createFacade();
                }
            };
        IAsyncNodeAction asyncNodeAction = new DefaultAsyncNodeAction(this);
        switch (fieldType)
        {
            case VOCABULARY:
                return new VocabularyField(fieldParameters);
            case EXPERIMENT:
                return new EntityField(DataSetOwnerType.EXPERIMENT, facadeProvider, asyncNodeAction);
            case SAMPLE:
                return new EntityField(DataSetOwnerType.SAMPLE, facadeProvider, asyncNodeAction);
            case DATA_SET:
                return new EntityField(DataSetOwnerType.DATA_SET, facadeProvider, asyncNodeAction);
            default:
                return new TextField();
        }
    }

    @Override
    protected void loadMoreSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        parameterBindings.loadValidatedSettingsFrom(settings);

    }

    @Override
    protected void saveMoreSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        parameterBindings.removeAllBindings();
        Set<Entry<String, IField>> entrySet = parameterFields.entrySet();
        for (Entry<String, IField> entry : entrySet)
        {
            parameterBindings.bind(entry.getKey(), entry.getValue().getValue());
        }
        parameterBindings.saveSettingsTo(settings);
    }

}
