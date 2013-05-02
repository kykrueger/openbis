package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DataSetKindSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class NewEntityTypeForm extends ContentPanel
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "new-entity-type-form";

    private final IViewContext<ICommonClientServiceAsync> viewContext;
    
    //
    // Entity Type
    //
    private final EntityKind kind;

    private NewETNewPTAssigments newTypeWithAssigments;

    //
    // Entity Form Related
    //
    private AbstractEntityTypeGrid<? extends EntityType> typeGrid;

    private FormPanel dialogForm;
    
    //
    // Form Creation
    //
    private NewEntityTypeForm(EntityKind kind, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.kind = kind;
        this.viewContext = viewContext;
        
        // Main panel
        setLayout(new BorderLayout());
        this.setHeaderVisible(false);
        this.setBorders(false);
        this.setBodyBorder(false);
        
        initForm();
    }

    public static DatabaseModificationAwareComponent create(EntityKind kind,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        NewEntityTypeForm form = new NewEntityTypeForm(kind, viewContext);
        return new DatabaseModificationAwareComponent(form, null);
    }

    private void initForm()
    {
        
        //Cleanup for the form and for the in memory structure
        removeAll();
        newTypeWithAssigments = new NewETNewPTAssigments();
        newTypeWithAssigments.setAssigments(new ArrayList<NewPTNewAssigment>());
        
        // Top panel
        initEntityTypeForm();
        dialogForm.setHeaderVisible(false);
        dialogForm.setBorders(false);
        dialogForm.setBodyBorder(false);
        dialogForm.setLabelWidth(180);
        add(dialogForm, BorderLayoutDataFactory.create(LayoutRegion.NORTH, 350));

        // Central panel
        PropertyTypeAssignmentGrid grid = (PropertyTypeAssignmentGrid) PropertyTypeAssignmentGrid.create(viewContext, newTypeWithAssigments.getEntity(), newTypeWithAssigments.getAssigments()).getComponent();
        final Component centerPanel = grid;
        add(centerPanel, BorderLayoutDataFactory.create(LayoutRegion.CENTER, 170));

        // Bottom panel
        final FormPanel bottomPanel = getSaveButton();
        add(bottomPanel, BorderLayoutDataFactory.create(LayoutRegion.SOUTH, 70));

        layout();
    }

    private void initEntityTypeForm()
    {
        AddEntityTypeDialog<? extends EntityType> dialog = null;

        switch (kind)
        {
            case SAMPLE:
                newTypeWithAssigments.setEntity(new SampleType());
                typeGrid = (SampleTypeGrid) SampleTypeGrid.create(viewContext).getComponent();
                dialog = ((SampleTypeGrid) typeGrid).getNewDialog((SampleType) newTypeWithAssigments.getEntity());
                break;
            case DATA_SET:
                newTypeWithAssigments.setEntity(new DataSetType());
                typeGrid = (DataSetTypeGrid) DataSetTypeGrid.create(viewContext).getComponent();
                dialog = ((DataSetTypeGrid) typeGrid).getNewDialog((DataSetType) newTypeWithAssigments.getEntity());
                break;
            case EXPERIMENT:
                newTypeWithAssigments.setEntity(new ExperimentType());
                typeGrid = (ExperimentTypeGrid) ExperimentTypeGrid.create(viewContext).getComponent();
                dialog = ((ExperimentTypeGrid) typeGrid).getNewDialog((ExperimentType) newTypeWithAssigments.getEntity());
                break;
            case MATERIAL:
                newTypeWithAssigments.setEntity(new MaterialType());
                typeGrid = (MaterialTypeGrid) MaterialTypeGrid.create(viewContext).getComponent();
                dialog = (AddEntityTypeDialog<MaterialType>) ((MaterialTypeGrid) typeGrid).getNewDialog((MaterialType) newTypeWithAssigments.getEntity());
                break;
        }

        newTypeWithAssigments.getEntity().setCode("" + System.currentTimeMillis()); // Just needed so the grid don't break
                                                          // afterwards
        List<Component> dialogFormIntoList = dialog.getItems();
        dialogForm = (FormPanel) dialogFormIntoList.get(0);
    }

    private FormPanel getSaveButton()
    {
        final FormPanel formWithButtons = new FormPanel();
        formWithButtons.setHeaderVisible(false);
        formWithButtons.setBorders(false);
        formWithButtons.setBodyBorder(false);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setMinButtonWidth(100);
        buttonBar.setAlignment(HorizontalAlignment.RIGHT);
        formWithButtons.add(buttonBar);

        final Button save = new Button("Save", new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (dialogForm.isValid())
                    {
                        //Update Entity Type
                        setEntityFromForm();
                        //Update Entity Type Code at the Property Types
                        for(NewPTNewAssigment assigment:newTypeWithAssigments.getAssigments()) {
                            assigment.getAssignment().setEntityTypeCode(newTypeWithAssigments.getEntity().getCode());
                        }
                        //Register/Update Call
                        viewContext.getService().registerEntitytypeAndAssignPropertyTypes(newTypeWithAssigments, new AsyncCallbackEntityType());
                    }
                }
            });
        save.setWidth("100px");
        save.setHeight("20px");
        buttonBar.add(save);

        return formWithButtons;
    }

    //
    // Save Functionality
    //
    private void setEntityFromForm()
    {
        List<Field<?>> formFields = dialogForm.getFields();

        switch (kind)
        {
            case SAMPLE:
                SampleType toSaveSample = new SampleType();
                toSaveSample.setCode((String) formFields.get(0).getValue());
                toSaveSample.setDescription((String) formFields.get(1).getValue());
                toSaveSample.setValidationScript((Script) formFields.get(2).getValue());
                toSaveSample.setListable((Boolean) formFields.get(3).getValue());
                toSaveSample.setShowContainer((Boolean) formFields.get(4).getValue());
                toSaveSample.setShowParents((Boolean) formFields.get(5).getValue());
                toSaveSample.setSubcodeUnique((Boolean) formFields.get(6).getValue());
                toSaveSample.setAutoGeneratedCode((Boolean) formFields.get(7).getValue());
                toSaveSample.setShowParentMetadata((Boolean) formFields.get(8).getValue());
                toSaveSample.setGeneratedCodePrefix((String) formFields.get(9).getValue());
                newTypeWithAssigments.setEntity(toSaveSample);
                break;
            case DATA_SET:
                DataSetType toSaveDataSet = new DataSetType();
                toSaveDataSet.setCode((String) formFields.get(0).getValue());
                toSaveDataSet.setDescription((String) formFields.get(1).getValue());
                toSaveDataSet.setValidationScript((Script) formFields.get(2).getValue());
                toSaveDataSet.setDataSetKind(((DataSetKindSelectionWidget)formFields.get(3)).getValue().getBaseObject());
                toSaveDataSet.setDeletionDisallow((Boolean) formFields.get(4).getValue());
                toSaveDataSet.setMainDataSetPattern((String) formFields.get(5).getValue());
                toSaveDataSet.setMainDataSetPath((String) formFields.get(6).getValue());
                newTypeWithAssigments.setEntity(toSaveDataSet);
                break;
            case EXPERIMENT:
                ExperimentType toSaveExperiment = new ExperimentType();
                toSaveExperiment.setCode((String) formFields.get(0).getValue());
                toSaveExperiment.setDescription((String) formFields.get(1).getValue());
                toSaveExperiment.setValidationScript((Script) formFields.get(2).getValue());
                newTypeWithAssigments.setEntity(toSaveExperiment);
                break;
            case MATERIAL:
                MaterialType toSaveMaterial = new MaterialType();
                toSaveMaterial.setCode((String) formFields.get(0).getValue());
                toSaveMaterial.setDescription((String) formFields.get(1).getValue());
                toSaveMaterial.setValidationScript((Script) formFields.get(2).getValue());
                newTypeWithAssigments.setEntity(toSaveMaterial);
                break;
        }
    }
    
    private class AsyncCallbackEntityType implements AsyncCallback<String>
    {
        @Override
        public void onFailure(Throwable throwable)
        {
            String message = "Error";
            if (throwable instanceof UserFailureException)
            {
                UserFailureException userException = (UserFailureException) throwable;
                String details = GWTUtils.translateToHtmlLineBreaks(userException.getMessage());
                if (details != null)
                {
                    message = details;
                }
            }
            MessageBox.alert("Error", message, null);
        }

        @Override
        public void onSuccess(String result)
        {
            initForm();
        }
    }
}
