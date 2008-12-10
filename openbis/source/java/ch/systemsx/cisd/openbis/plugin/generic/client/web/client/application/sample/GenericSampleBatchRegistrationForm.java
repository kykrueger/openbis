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

import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm.InfoBoxResetListener;

/**
 * The <i>generic</i> sample batch registration panel.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleBatchRegistrationForm extends LayoutContainer
{
    private static final String SESSION_KEY = "sample-batch-registration";

    private static final String FIELD_LABEL_TEMPLATE = "File {0}";

    private static final String FIELD_NAME_TEMPLATE = SESSION_KEY + "_{0}";

    private static final int NUMBER_OF_FIELDS = 1;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private FormPanel formPanel;

    private Button submitButton;

    private final InfoBox infoBox;

    private final SampleType sampleType;

    public GenericSampleBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(new FlowLayout(5));
        setScrollMode(Scroll.AUTO);
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        add(infoBox = createInfoBox());
        add(createUI());
    }

    private final static InfoBox createInfoBox()
    {
        final InfoBox infoBox = new InfoBox();
        return infoBox;
    }

    private final Component createUI()
    {
        submitButton = createButton();
        formPanel = createFormPanel(submitButton);
        final FieldSet fieldSet = createFieldSet();
        for (int i = 0; i < NUMBER_OF_FIELDS; i++)
        {
            fieldSet.add(createFileUploadField(i));
        }
        formPanel.add(fieldSet);
        return formPanel;
    }

    private final static FieldSet createFieldSet()
    {
        final FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading("Upload files");
        fieldSet.setLayout(createFormLayout());
        return fieldSet;
    }

    private final static FormLayout createFormLayout()
    {
        final FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(GenericSampleRegistrationForm.LABEL_WIDTH);
        formLayout.setDefaultWidth(GenericSampleRegistrationForm.FIELD_WIDTH);
        return formLayout;
    }

    private final FormPanel createFormPanel(final Button button)
    {
        final FormPanel panel = new FormPanel();
        panel.setLayout(new FlowLayout());
        panel.setWidth(GenericSampleRegistrationForm.LABEL_WIDTH
                + GenericSampleRegistrationForm.FIELD_WIDTH + 50);
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setAction(GenericConstants.createServicePath("upload"));
        panel.setEncoding(Encoding.MULTIPART);
        panel.setMethod(Method.POST);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        final HiddenField<String> sessionKeyField = createHiddenField();
        panel.add(sessionKeyField);
        panel.addButton(button);
        // Does some action after the form has been successfully submitted. Note that the response
        // coming from the server could be an error message. Even in case of error on the server
        // side this listener will be informed.
        panel.addListener(Events.Submit, new Listener<FormEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final FormEvent be)
                {
                    final String msg = be.resultHtml;
                    // Was not successful
                    if (StringUtils.isBlank(msg) == false)
                    {
                        infoBox.displayError(msg);
                        // final Document document = XMLParser.parse(msg);
                        // System.out.println(document.getElementsByTagName("message").item(0)
                        // .getFirstChild().getNodeValue());
                        setUploadEnabled(true);
                    } else
                    {
                        viewContext.getService().registerSamples(sampleType,
                                sessionKeyField.getValue(),
                                new RegisterSamplesCallback(viewContext));
                    }
                }
            });
        return panel;
    }

    private final static HiddenField<String> createHiddenField()
    {
        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName("sessionKey");
        hiddenField.setValue(SESSION_KEY);
        return hiddenField;
    }

    private final Button createButton()
    {
        final Button button =
                new Button(viewContext.getMessageProvider().getMessage("button_submit"));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        setUploadEnabled(false);
                        formPanel.submit();
                    }
                }
            });
        return button;
    }

    private final FileUploadField createFileUploadField(final int counter)
    {
        final FileUploadField file = new FileUploadField();
        file.addListener(Events.OnClick, new InfoBoxResetListener(infoBox));
        file.setAllowBlank(counter > 0);
        final int number = counter + 1;
        file.setFieldLabel(Format.substitute(FIELD_LABEL_TEMPLATE, number));
        file.setName(Format.substitute(FIELD_NAME_TEMPLATE, number));
        return file;
    }

    private void setUploadEnabled(final boolean enabled)
    {
        submitButton.setEnabled(enabled);
    }

    //
    // Helper classes
    //

    private final class RegisterSamplesCallback extends
            AbstractAsyncCallback<List<BatchRegistrationResult>>
    {
        RegisterSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<List<BatchRegistrationResult>>(infoBox));
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final List<BatchRegistrationResult> result)
        {
            final StringBuilder builder = new StringBuilder();
            for (final BatchRegistrationResult batchRegistrationResult : result)
            {
                builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>:");
                builder.append(batchRegistrationResult.getMessage());
                builder.append("<br />");
            }
            infoBox.displayInfo(builder.toString());
            formPanel.reset();
        }

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            setUploadEnabled(true);
        }
    }

}
