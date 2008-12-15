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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.widget.form.TextField;

/**
 * A basic {@link TextField} extension for registering text (<code>String</code>).
 * 
 * @author Christian Ribeaud
 */
public class VarcharField extends TextField<String>
{
    public static final String MANDATORY_LABEL_SEPARATOR = ": *";

    public VarcharField(final String label, final boolean mandatory)
    {
        configureField(this, label, mandatory);
    }

    static <T> void configureField(final TextField<T> textField, final String fieldLabel,
            final boolean mandatory)
    {
        textField.setFieldLabel(fieldLabel);
        textField.setMaxLength(1024);
        textField.setValidateOnBlur(true);
        textField.setAutoValidate(true);
        if (mandatory)
        {
            textField.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
            textField.setAllowBlank(false);
        }
    }
}