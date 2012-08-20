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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Grid displaying sample types.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class SampleTypeGrid extends AbstractEntityTypeGrid<SampleType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "sample-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    private static final Boolean DEFAULT_LISTABLE_VALUE = true;

    private static final Boolean DEFAULT_AUTO_GENERATE_CODES_VALUE = false;

    private static final Boolean DEFAULT_SHOW_PARENT_METADATA_VALUE = false;

    private static final Boolean DEFAULT_UNIQUE_SUBCODES_VALUE = false;

    private static final String DEFAULT_GENERATED_CODE_PREFIX_VALUE = "S";

    private static final Boolean DEFAULT_SHOW_CONTAINER_VALUE = false;

    private static final Boolean DEFAULT_SHOW_PARENTS_VALUE = true;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleTypeGrid grid = new SampleTypeGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private SampleTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<SampleType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<SampleType>> callback)
    {
        viewContext.getService().listSampleTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<SampleType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportSampleTypes(exportCriteria, callback);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected void register(SampleType sampleType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerSampleType(sampleType, registrationCallback);
    }

    @Override
    protected SampleType createNewEntityType()
    {
        return new SampleType();
    }

    @Override
    protected Window createEditEntityTypeDialog(final EntityKind entityKind,
            final SampleType sampleType)
    {
        final String code = sampleType.getCode();
        String title =
                viewContext.getMessage(Dict.EDIT_TYPE_TITLE_TEMPLATE, entityKind.getDescription(),
                        code);
        return new AbstractRegistrationDialog(viewContext, title, postRegistrationCallback)
            {
                private final DescriptionField descriptionField;

                private final CheckBoxField showContainerField;

                private final CheckBoxField showParentsField;

                private final CheckBoxField listableField;

                private final CheckBoxField subcodeUniqueField;

                private final CheckBoxField autoGeneratedCodeField;

                private final CheckBoxField showParentMetadataField;

                private final TextField<String> generatedCodePrefixField;

                {
                    descriptionField = createDescriptionField(viewContext);
                    FieldUtil.setValueWithUnescaping(descriptionField, sampleType.getDescription());
                    addField(descriptionField);

                    listableField =
                            SampleTypeDialogFieldHelper.createListableField(viewContext,
                                    sampleType.isListable());
                    addField(listableField);

                    showContainerField =
                            SampleTypeDialogFieldHelper.createShowContainerField(viewContext,
                                    sampleType.isShowContainer());
                    addField(showContainerField);

                    showParentsField =
                            SampleTypeDialogFieldHelper.createShowParentsField(viewContext,
                                    sampleType.isShowParents());
                    addField(showParentsField);

                    subcodeUniqueField =
                            SampleTypeDialogFieldHelper.createUniqueSubcodesField(viewContext,
                                    sampleType.isSubcodeUnique());
                    addField(subcodeUniqueField);

                    autoGeneratedCodeField =
                            SampleTypeDialogFieldHelper.createAutoGeneratedCodeField(viewContext,
                                    sampleType.isAutoGeneratedCode());
                    addField(autoGeneratedCodeField);

                    showParentMetadataField =
                            SampleTypeDialogFieldHelper.createShowParentMetadataField(viewContext,
                                    sampleType.isShowParentMetadata());
                    addField(showParentMetadataField);

                    generatedCodePrefixField =
                            SampleTypeDialogFieldHelper.createGeneratedCodePrefixField(viewContext,
                                    sampleType.getGeneratedCodePrefix());
                    addField(generatedCodePrefixField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(AsyncCallback<Void> registrationCallback)
                {
                    sampleType.setDescription(descriptionField.getValue());
                    sampleType.setListable(listableField.getValue());
                    sampleType.setSubcodeUnique(subcodeUniqueField.getValue());
                    sampleType.setAutoGeneratedCode(autoGeneratedCodeField.getValue());
                    sampleType.setShowParentMetadata(showParentMetadataField.getValue());
                    sampleType.setGeneratedCodePrefix(generatedCodePrefixField.getValue());
                    sampleType.setShowParents(showParentsField.getValue());
                    sampleType.setShowContainer(showContainerField.getValue());
                    viewContext.getService().updateEntityType(entityKind, sampleType,
                            registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.SAMPLE_TYPE,
                            HelpPageIdentifier.HelpPageAction.EDIT);
                }
            };
    }

    @Override
    protected Window createRegisterEntityTypeDialog(String title, SampleType newEntityType)
    {
        return new AddTypeDialog<SampleType>(viewContext, title, postRegistrationCallback,
                newEntityType)
            {

                private CheckBoxField showContainerField;

                private CheckBoxField showParentsField;

                private CheckBoxField listableField;

                private CheckBoxField subcodeUniqueField;

                private CheckBoxField autoGenerateCodesField;

                private CheckBoxField showParentMetadataField;

                private TextField<String> generatedCodePrefixField;

                {
                    listableField =
                            SampleTypeDialogFieldHelper.createListableField(viewContext,
                                    DEFAULT_LISTABLE_VALUE);
                    addField(listableField);

                    showContainerField =
                            SampleTypeDialogFieldHelper.createShowContainerField(viewContext,
                                    DEFAULT_SHOW_CONTAINER_VALUE);
                    addField(showContainerField);

                    showParentsField =
                            SampleTypeDialogFieldHelper.createShowParentsField(viewContext,
                                    DEFAULT_SHOW_PARENTS_VALUE);
                    addField(showParentsField);

                    subcodeUniqueField =
                            SampleTypeDialogFieldHelper.createUniqueSubcodesField(viewContext,
                                    DEFAULT_UNIQUE_SUBCODES_VALUE);
                    addField(subcodeUniqueField);

                    autoGenerateCodesField =
                            SampleTypeDialogFieldHelper.createAutoGeneratedCodeField(viewContext,
                                    DEFAULT_AUTO_GENERATE_CODES_VALUE);
                    addField(autoGenerateCodesField);

                    showParentMetadataField =
                            SampleTypeDialogFieldHelper.createShowParentMetadataField(viewContext,
                                    DEFAULT_SHOW_PARENT_METADATA_VALUE);
                    addField(showParentMetadataField);

                    generatedCodePrefixField =
                            SampleTypeDialogFieldHelper.createGeneratedCodePrefixField(viewContext,
                                    DEFAULT_GENERATED_CODE_PREFIX_VALUE);
                    addField(generatedCodePrefixField);

                    DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                            createHelpPageIdentifier());
                }

                @Override
                protected void register(SampleType sampleType,
                        AsyncCallback<Void> registrationCallback)
                {
                    sampleType.setShowParents(showParentsField.getValue());
                    sampleType.setShowContainer(showContainerField.getValue());
                    sampleType.setListable(listableField.getValue());
                    sampleType.setAutoGeneratedCode(autoGenerateCodesField.getValue());
                    sampleType.setShowParentMetadata(showParentMetadataField.getValue());
                    sampleType.setSubcodeUnique(subcodeUniqueField.getValue());
                    sampleType.setGeneratedCodePrefix(generatedCodePrefixField.getValue());
                    SampleTypeGrid.this.register(sampleType, registrationCallback);
                }

                private HelpPageIdentifier createHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.SAMPLE_TYPE,
                            HelpPageIdentifier.HelpPageAction.REGISTER);
                }
            };
    }

    //
    // Helpers
    //

    private static final class SampleTypeDialogFieldHelper
    {
        public static CheckBoxField createListableField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.LISTABLE);
            return createCheckBoxField(title, value);
        }

        public static CheckBoxField createUniqueSubcodesField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.SUBCODE_UNIQUE_LABEL);
            return createCheckBoxField(title, value);
        }

        public static CheckBoxField createAutoGeneratedCodeField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.AUTO_GENERATE_CODES_LABEL);
            return createCheckBoxField(title, value);
        }

        public static CheckBoxField createShowParentMetadataField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.SHOW_PARENT_METADATA_LABEL);
            return createCheckBoxField(title, value);
        }

        private static CheckBoxField createCheckBoxField(final String title, Boolean value)
        {
            final CheckBoxField field = new CheckBoxField(title, false);
            field.setValue(value);
            return field;
        }

        public static TextField<String> createGeneratedCodePrefixField(
                final IViewContext<ICommonClientServiceAsync> viewContext, String value)
        {
            final String title = viewContext.getMessage(Dict.GENERATED_CODE_PREFIX);
            final TextField<String> field = new CodeField(viewContext, title);
            field.setValue(value);
            return field;

        }

        public static CheckBoxField createShowParentsField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.SHOW_PARENTS);
            return createCheckBoxField(title, value);
        }

        public static CheckBoxField createShowContainerField(
                final IViewContext<ICommonClientServiceAsync> viewContext, Boolean value)
        {
            final String title = viewContext.getMessage(Dict.SHOW_CONTAINER);
            return createCheckBoxField(title, value);
        }
    }

}
