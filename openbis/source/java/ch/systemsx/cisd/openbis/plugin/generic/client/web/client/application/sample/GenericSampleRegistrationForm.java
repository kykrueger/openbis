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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample registration panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends FormPanel
{
    private static final String PREFIX = "generic-sample-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String CODE_FIELD_ID = ID_PREFIX + "code";

    public static final String SHARED_CHECKBOX_ID = ID_PREFIX + "shared";

    public static final String PARENT_CONTAINER_FIELD_ID = ID_PREFIX + "parent-container";

    public static final String PARENT_GENERATOR_FIELD_ID = ID_PREFIX + "parent-generator";

    public static final String SAVE_BUTTON_ID = ID_PREFIX + "save-button";

    private static final String ETPT = "PROPERTY_TYPE";

    private static boolean SELECT_GROUP_BY_DEFAULT = true;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private GroupSelectionWidget groupSelectionWidget;

    private ArrayList<Field<?>> propertyFields;

    private TextField<String> parentContainer;

    private TextField<String> parentGenerator;

    private TextField<String> codeField;

    private CheckBox sharedCheckbox;

    private MultiField<Field<?>> groupMultiField;

    private InfoBox infoBox;

    private static final String MANDATORY_LABEL_SEPARATOR = ": *";

    static final int LABEL_WIDTH = 100;

    static final int FIELD_WIDTH = 400;

    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        configureForm();
        createFormFields();
    }

    private final void createFormFields()
    {
        infoBox = new InfoBox();
        codeField = new CodeField(viewContext.getMessageProvider().getMessage("code"));
        codeField.setId(CODE_FIELD_ID);
        codeField.addListener(Events.Focus, new Listener<BaseEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final BaseEvent be)
                {
                    infoBox.fade();
                }
            });

        groupSelectionWidget = new GroupSelectionWidget(viewContext.getCommonViewContext());
        groupSelectionWidget.setEnabled(SELECT_GROUP_BY_DEFAULT);

        sharedCheckbox = new CheckBox();
        sharedCheckbox.setId(SHARED_CHECKBOX_ID);
        sharedCheckbox.setBoxLabel("Shared");
        sharedCheckbox.setValue(SELECT_GROUP_BY_DEFAULT == false);
        sharedCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                //
                // Listener
                //

                public final void handleEvent(final BaseEvent be)
                {
                    groupSelectionWidget
                            .setEnabled(sharedCheckbox.getValue().booleanValue() == false);
                }
            });

        groupMultiField = new MultiField<Field<?>>();
        groupMultiField.setFieldLabel(viewContext.getMessageProvider().getMessage("group"));
        groupMultiField.add(sharedCheckbox);
        groupMultiField.add(groupSelectionWidget);
        groupMultiField.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
        groupMultiField.setValidator(new Validator<Field<?>, MultiField<Field<?>>>()
            {
                //
                // Validator
                //

                public final String validate(final MultiField<Field<?>> field, final String value)
                {
                    if (sharedCheckbox.getValue() == false
                            && groupSelectionWidget.tryGetSelected() == null)
                    {
                        return "Group must be chosen or shared selected";
                    }
                    return null;
                }
            });

        parentGenerator =
                new VarcharField(viewContext.getMessageProvider().getMessage(
                        "generated_from_sample"), false);
        parentGenerator.setId(PARENT_GENERATOR_FIELD_ID);

        parentContainer =
                new VarcharField(viewContext.getMessageProvider().getMessage("part_of_sample"),
                        false);
        parentContainer.setId(PARENT_CONTAINER_FIELD_ID);

        propertyFields = new ArrayList<Field<?>>();
        for (final SampleTypePropertyType stpt : sampleType.getSampleTypePropertyTypes())
        {
            propertyFields.add(createProperty(stpt));
        }
    }

    private final void configureForm()
    {
        setHeaderVisible(false);
        setBodyBorder(false);
        setWidth(LABEL_WIDTH + FIELD_WIDTH + 40);

        setLabelWidth(LABEL_WIDTH);
        setFieldWidth(FIELD_WIDTH);
        setButtonAlign(HorizontalAlignment.RIGHT);
        final Button saveButton =
                new Button(viewContext.getMessageProvider().getMessage("button_save"));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(SAVE_BUTTON_ID);
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    submitForm();
                }
            });
        final Button resetButton =
                new Button(viewContext.getMessageProvider().getMessage("button_reset"));
        resetButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    reset();
                }
            });
        addButton(resetButton);
        addButton(saveButton);
    }

    private final String createSampleIdentifier()
    {
        final boolean shared = sharedCheckbox.getValue();
        final Group group = groupSelectionWidget.tryGetSelected();
        final String code = codeField.getValue();
        final StringBuilder builder = new StringBuilder("/");
        if (shared == false)
        {
            builder.append(group.getCode() + "/");
        }
        builder.append(code);
        return builder.toString().toUpperCase();
    }

    private final static String createSuccessfullRegistrationInfo(final boolean shared,
            final String code, final Group group)
    {
        if (shared)
        {
            return "Shared sample <b>" + code + "</b> successfully registered";

        } else
        {
            return "Sample <b>" + code + "</b> successfully registered in group <b>"
                    + group.getCode() + "</b>";
        }
    }

    private final String valueToString(final Object value)
    {
        if (value == null)
        {
            return null;
        } else if (value instanceof Date)
        {
            return DateRenderer.renderDate((Date) value);
        } else
        {
            return value.toString();
        }
    }

    private final void submitForm()
    {
        if (isValid())
        {
            final SampleToRegister sampleToRegister =
                    new SampleToRegister(createSampleIdentifier(), sampleType.getCode(),
                            parentGenerator.getValue(), parentContainer.getValue());
            for (final Field<?> field : propertyFields)
            {
                if (field.getValue() != null)
                {
                    final SampleTypePropertyType stpt = field.getData(ETPT);
                    final SampleProperty sampleProperty = new SampleProperty();
                    sampleProperty.setValue(valueToString(field.getValue()));
                    sampleProperty.setEntityTypePropertyType(stpt);
                    sampleToRegister.addProperty(sampleProperty);
                }
            }
            viewContext.getService().registerSample(sampleToRegister,
                    new RegisterSampleCallback(viewContext));
        }
    }

    private final void resetForm(final String info)
    {
        createFormFields();
        addFormFields();
        infoBox.display(info);
        layout();
    }

    public void addFormFields()
    {
        removeAll();
        add(infoBox);
        add(codeField);
        add(groupMultiField);
        add(parentGenerator);
        add(parentContainer);
        for (final Field<?> propertyField : propertyFields)
        {
            add(propertyField);
        }
    }

    private final Field<?> createProperty(final SampleTypePropertyType stpt)
    {
        final Field<?> field;
        final DataTypeCode dataType = stpt.getPropertyType().getDataType().getCode();
        final boolean isMandatory = stpt.isMandatory();
        final String label = stpt.getPropertyType().getLabel();
        switch (dataType)
        {
            case BOOLEAN:
                field = new CheckBoxField(label, isMandatory);
                break;
            case VARCHAR:
                field = new VarcharField(label, isMandatory);
                break;
            case TIMESTAMP:
                field = new DateFormField(label, isMandatory);
                break;
            case CONTROLLEDVOCABULARY:
                field =
                        new ControlledVocabullaryField(label, isMandatory, stpt.getPropertyType()
                                .getVocabulary().getTerms());
                break;
            case INTEGER:
                field = new IntegerField(label, isMandatory);
                break;
            case REAL:
                field = new RealField(label, isMandatory);
                break;
            default:
                field = new VarcharField(label, isMandatory);
                break;
        }
        field.setData(ETPT, stpt);
        field.setTitle(stpt.getPropertyType().getCode());
        return field;
    }

    //
    // FormPanel
    //

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    //
    // Helper classes
    //

    public final class RegisterSampleCallback extends AbstractAsyncCallback<Void>
    {
        RegisterSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {

            final String message =
                    createSuccessfullRegistrationInfo(sharedCheckbox.getValue(), codeField
                            .getValue(), groupSelectionWidget.tryGetSelected());
            resetForm(message);
        }
    }

    private final static class CheckBoxField extends CheckBox
    {
        CheckBoxField(final String label, final boolean mandatory)
        {
            setFieldLabel(label);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
            }
        }
    }

    private final static class DateFormField extends DateField
    {
        DateFormField(final String label, final boolean mandatory)
        {
            setFieldLabel(label);
            setValidateOnBlur(true);
            setAutoValidate(true);
            getPropertyEditor().setFormat(DateRenderer.SHORT_DATE_TIME_FORMAT);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
                setAllowBlank(false);
            }
        }
    }

    private final static class ControlledVocabullaryField extends AdapterField
    {

        ControlledVocabullaryField(final String label, final boolean mandatory,
                final List<VocabularyTerm> terms)
        {
            super(createListBox(terms, mandatory));
            setFieldLabel(label);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
            }
        }

        private static final ListBox createListBox(final List<VocabularyTerm> terms,
                final boolean mandatory)
        {
            final ListBox box = new ListBox();
            if (mandatory == false)
            {
                box.addItem(GWTUtils.NONE_LIST_ITEM);
            }
            for (final VocabularyTerm term : terms)
            {
                box.addItem(term.getCode());
            }
            return box;
        }

        //
        // AdapterField
        //

        @Override
        public final Object getValue()
        {
            final String stringValue = super.getValue().toString();
            if (GWTUtils.NONE_LIST_ITEM.equals(stringValue))
            {
                return null;
            }
            return stringValue;
        }

    }

    private static class BasicTextField<T> extends TextField<T>
    {

        BasicTextField(final String label, final boolean mandatory)
        {
            setFieldLabel(label);
            setMaxLength(1024);
            setValidateOnBlur(true);
            setAutoValidate(true);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
                setAllowBlank(false);
            }
        }
    }

    private static class VarcharField extends BasicTextField<String>
    {
        VarcharField(final String label, final boolean mandatory)
        {
            super(label, mandatory);
        }
    }

    private final static class CodeField extends VarcharField
    {

        CodeField(final String label)
        {
            super(label, true);
            final String codePattern = GenericConstants.CODE_PATTERN;
            setRegex(codePattern);
            getMessages().setRegexText(
                    Format.substitute("Value does not match pattern {0}.", codePattern));
        }
    }

    private final static class RealField extends BasicTextField<Double>
    {
        RealField(final String label, final boolean mandatory)
        {
            super(label, mandatory);
            setValidator(new Validator<Double, Field<Double>>()
                {

                    //
                    // Validator
                    //

                    public final String validate(final Field<Double> field, final String val)
                    {
                        try
                        {
                            Double.parseDouble(val);
                            return null;
                        } catch (final NumberFormatException e)
                        {
                            return "Real number required";
                        }
                    }
                });
        }
    }

    private final static class IntegerField extends BasicTextField<Integer>
    {
        IntegerField(final String label, final boolean mandatory)
        {
            super(label, mandatory);
            setValidator(new Validator<Integer, Field<Integer>>()
                {

                    //
                    // Validator
                    //

                    public final String validate(final Field<Integer> field, final String val)
                    {
                        try
                        {
                            Integer.parseInt(val);
                            return null;
                        } catch (final NumberFormatException e)
                        {
                            return "Integer required";
                        }
                    }
                });
        }
    }

}
