/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.SingleExperimentSearchCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class PlateMaterialReviewer2 extends TypedTableGrid<WellContent>
{
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "PlateMaterialReviewer2Grid";
    public static final String GRID_ID = BROWSER_ID + "-grid";
    private static final String ALL_EXPERIMENTS_TEXT = "All experiments";
    private static final String CHOOSE_ONE_EXPERIMENT_TEXT = "Choose one experiment...";
    
    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithIdentifier experiment, String[] materialItemList,
            String[] materialTypeCodes, boolean exactMatchOnly)
    {
        ExperimentSearchCriteria experimentCriteria =
                ExperimentSearchCriteria.createExperiment(experiment.getId(),
                        experiment.getIdentifier());
        MaterialSearchCriteria materialCriteria =
                MaterialSearchCriteria.createCodesCriteria(materialItemList, materialTypeCodes,
                        exactMatchOnly);
        return create(viewContext, experimentCriteria, materialCriteria);
    }

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull, TechId materialId)
    {
        return create(viewContext, experimentCriteriaOrNull,
                MaterialSearchCriteria.createIdCriteria(materialId));
    }

    private static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria)
    {
        PlateMaterialReviewer2 reviewer =
                new PlateMaterialReviewer2(viewContext, experimentCriteriaOrNull, materialCriteria);
        return reviewer.asDisposableWithToolbar(reviewer.createToolbar());
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final MaterialSearchCriteria materialCriteria;

    private ExperimentSearchCriteria experimentCriteriaOrNull;
    
    private SingleExperimentSearchCriteria singleExperimentChooserStateOrNull;
    
    private ChannelComboBox channelChooser;

    private PlateMaterialReviewer2(IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            MaterialSearchCriteria materialCriteria)
    {
        super(viewContext.getCommonViewContext(), GRID_ID, experimentCriteriaOrNull != null,
                DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER);
        this.viewContext = viewContext;
        this.experimentCriteriaOrNull = experimentCriteriaOrNull;
        this.materialCriteria = materialCriteria;
        setId(BROWSER_ID);
        channelChooser = new ChannelComboBox();
        registerClickListeners();
    }

    private ToolBar createToolbar()
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(createExperimentChooser());
        toolbar.add(new Label("Channel:"));
        toolbar.add(channelChooser);
        return toolbar;
    }

    private Component createExperimentChooser()
    {
        LayoutContainer container = new LayoutContainer();
        container.setWidth(400);

        ExperimentChooserFieldAdaptor singleExperimentChooser = createSingleExperimentChooser();
        RadioGroup experimentRadioChooser = createExperimentRadio(singleExperimentChooser);

        container.add(experimentRadioChooser);
        container.add(singleExperimentChooser.getField());
        return container;
    }

    private ExperimentChooserFieldAdaptor createSingleExperimentChooser()
    {
        ExperimentChooserFieldAdaptor experimentChooser =
                ExperimentChooserField.create("", true, null, viewContext.getCommonViewContext());
        final ExperimentChooserField chooserField = experimentChooser.getChooserField();
        chooserField.addChosenEntityListener(new IChosenEntityListener<Experiment>()
            {
                public void entityChosen(Experiment experiment)
                {
                    if (experiment != null)
                    {
                        chooseSingleExperiment(chooserField, experiment);
                    }
                }
            });

        chooserField.setEditable(false);
        if (experimentCriteriaOrNull != null && experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            updateSingleExperimentChooser(chooserField, experimentCriteriaOrNull.tryGetExperiment());
        } else
        {
            // we search in all experiments or single experiment has not been chosen
            this.singleExperimentChooserStateOrNull = null;
            chooserField.reset();
        }
        if (experimentCriteriaOrNull == null || experimentCriteriaOrNull.tryGetExperiment() != null)
        {
            chooserField.setEmptyText(CHOOSE_ONE_EXPERIMENT_TEXT);
        } else
        {
            chooserField.setEmptyText(ALL_EXPERIMENTS_TEXT);
        }
        return experimentChooser;
    }

    private void chooseSingleExperiment(final ExperimentChooserField chooserField,
            Experiment experiment)
    {
        SingleExperimentSearchCriteria singleExperiment =
                new SingleExperimentSearchCriteria(experiment.getId(), experiment.getIdentifier());
        updateSingleExperimentChooser(chooserField, singleExperiment);
        this.experimentCriteriaOrNull =
                ExperimentSearchCriteria.createExperiment(singleExperiment);
        refresh();
    }

    private void updateSingleExperimentChooser(ExperimentChooserField chooserField,
            SingleExperimentSearchCriteria singleExperiment)
    {
        this.singleExperimentChooserStateOrNull = singleExperiment;
        chooserField.updateValue(new ExperimentIdentifier(singleExperiment.getExperimentIdentifier()));
    }

    private boolean isAllExperimentsChoosen()
    {
        return experimentCriteriaOrNull != null
                && experimentCriteriaOrNull.tryGetExperiment() == null;
    }

    private RadioGroup createExperimentRadio(
            final ExperimentChooserFieldAdaptor singleExperimentChooser)
    {
        final RadioGroup experimentRadio = new RadioGroup();
        experimentRadio.setSelectionRequired(true);
        experimentRadio.setOrientation(Orientation.HORIZONTAL);

        final Radio allExps = new Radio();
        allExps.setBoxLabel(ALL_EXPERIMENTS_TEXT);
        experimentRadio.add(allExps);

        final Radio oneExps = new Radio();
        oneExps.setBoxLabel("Single experiment");
        experimentRadio.add(oneExps);

        experimentRadio.setValue(isAllExperimentsChoosen() ? allExps : oneExps);
        experimentRadio.setAutoHeight(true);
        experimentRadio.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    if (allExps.getValue())
                    {
                        singleExperimentChooser.getChooserField().setEnabled(false);
                        singleExperimentChooser.getChooserField()
                                .setEmptyText(ALL_EXPERIMENTS_TEXT);
                        experimentCriteriaOrNull = ExperimentSearchCriteria.createAllExperiments();
                        refresh();
                    } else
                    {
                        singleExperimentChooser.getChooserField().setEmptyText(
                                CHOOSE_ONE_EXPERIMENT_TEXT);

                        singleExperimentChooser.getChooserField().setEnabled(true);
                        if (singleExperimentChooserStateOrNull == null)
                        {
                            experimentCriteriaOrNull = null;
                        } else
                        {
                            experimentCriteriaOrNull =
                                    ExperimentSearchCriteria
                                            .createExperiment(singleExperimentChooserStateOrNull);
                            refresh();
                        }
                    }
                }
            });
        return experimentRadio;
    }
    
    private void registerClickListeners()
    {
        registerLinkClickListenerFor("WELL_CONTENT_MATERIAL",
                new ICellListener<TableModelRowWithObject<WellContent>>()
                    {
                        public void handle(TableModelRowWithObject<WellContent> wellContent, boolean specialKeyPressed)
                        {
                            Material contentMaterial = wellContent.getObjectOrNull().getMaterialContent();
                            ExperimentReference experiment = wellContent.getObjectOrNull().getExperiment();
                            ExperimentSearchCriteria experimentCriteria =
                                    ExperimentSearchCriteria.createExperiment(experiment.getId(),
                                            experiment.getExperimentIdentifier());

                            ClientPluginFactory.openPlateLocationsMaterialViewer(contentMaterial,
                                    experimentCriteria, viewContext);
                        }
                    });
    }


    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<WellContent>> columnDefs = super.createColumnsDefinition();
        columnDefs.setGridCellRendererFor("WELL_CONTENT_MATERIAL", LinkRenderer
                .createLinkRenderer());

        return columnDefs;
    }

    @Override
    protected void listTableRows(
            IResultSetConfig<String, TableModelRowWithObject<WellContent>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<WellContent>> callback)
    {
        assert experimentCriteriaOrNull != null : "experiment not specified";

        PlateMaterialsSearchCriteria searchCriteria =
                new PlateMaterialsSearchCriteria(experimentCriteriaOrNull, materialCriteria);
        viewContext.getService().listPlateWells2(resultSetConfig, searchCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<WellContent>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO Auto-generated method stub
        
    }

}
