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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyTermModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VocabularyTermSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowerToolBar extends ToolBar
{
    private static final AggregateFunction DEFAULT_AGGREGATE_FUNCTION = AggregateFunction.MEAN;

    private static final class AggregateOnTreatmentTypeSelectionWidget extends
            VocabularyTermSelectionWidget
    {

        private final VocabularyTermModel nothingTermModel;

        AggregateOnTreatmentTypeSelectionWidget(
                IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
        {
            super("treatmentType", "treatmentType", false, null, viewContext, null, null);
            setAllowBlank(false);
            setForceSelection(true);
            VocabularyTerm nothingTerm = new VocabularyTerm();
            nothingTerm.setLabel("(nothing)");
            nothingTermModel = new VocabularyTermModel(nothingTerm);
            setValue(nothingTermModel);
            viewContext.getService().getTreatmentTypeVocabulary(
                    new AbstractAsyncCallback<Vocabulary>(viewContext)
                        {
                            @Override
                            protected void process(Vocabulary vocabulary)
                            {
                                setVocabulary(vocabulary);
                            }
                        });

        }

        @Override
        protected List<VocabularyTermModel> convertItems(List<VocabularyTerm> result)
        {
            List<VocabularyTermModel> terms = super.convertItems(result);
            terms.add(0, nothingTermModel);
            return terms;
        }

    }

    private abstract static class SimpleModel<T> extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        private static final String PROPERTY = "property";

        private final T object;

        SimpleModel(T object)
        {
            this.object = object;
            set(PROPERTY, render(object));
        }

        public final T getObject()
        {
            return object;
        }

        protected abstract String render(T modelObject);
    }

    private static final class FalseDiscoveryRateModel extends SimpleModel<Double>
    {
        private static final long serialVersionUID = 1L;

        FalseDiscoveryRateModel(Double falseDiscoveryRate)
        {
            super(falseDiscoveryRate);
        }

        @Override
        protected String render(Double modelObject)
        {
            int percent = (int) (100 * modelObject);
            int percentFraction = (int) (1000 * modelObject + 0.5) - 10 * percent;
            return Integer.toString(percent)
                    + (percentFraction == 0 ? "" : "." + Integer.toString(percentFraction)) + "%";
        }
    }

    private static final class AggregateFunctionModel extends SimpleModel<AggregateFunction>
    {
        private static final long serialVersionUID = 1L;

        AggregateFunctionModel(AggregateFunction aggregateFunction)
        {
            super(aggregateFunction);
        }

        @Override
        protected String render(AggregateFunction modelObject)
        {
            return modelObject.getLabel();
        }
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    private final ComboBox<FalseDiscoveryRateModel> fdrComboBox;

    private final ComboBox<AggregateFunctionModel> aggregateFunctionComboBox;

    private final VocabularyTermSelectionWidget treatmentTypeComboBox;

    private final CheckBox aggregateOriginalCheckBox;

    private ProteinByExperimentBrowserGrid browserGrid;

    private Experiment experiment;

    private ProteinSummaryGrid summaryGrid;

    ProteinByExperimentBrowerToolBar(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experiment)
    {
        this.viewContext = viewContext;
        this.experiment = experiment;
        setBorders(true);
        // WORKAROUND to get all elements in the toolbar present independent of the original width
        // of the parent
        setWidth(3000);
        add(new LabelToolItem(viewContext.getMessage(Dict.FDR_FILTER_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        fdrComboBox = createFDRComboBox(new SelectionChangedListener<FalseDiscoveryRateModel>()
            {

                @Override
                public void selectionChanged(SelectionChangedEvent<FalseDiscoveryRateModel> se)
                {
                    update();
                }
            });
        add(fdrComboBox);
        add(new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));
        add(new LabelToolItem(viewContext.getMessage(Dict.AGGREGATE_FUNCTION_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        aggregateFunctionComboBox =
                createAggregateFunctionComboBox(new SelectionChangedListener<AggregateFunctionModel>()
                    {

                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<AggregateFunctionModel> se)
                        {
                            update();
                        }

                    });
        add(aggregateFunctionComboBox);
        add(new LabelToolItem(viewContext.getMessage(Dict.AGGREGATE_ON_TREATMENT_TYPE_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        treatmentTypeComboBox = new AggregateOnTreatmentTypeSelectionWidget(viewContext);
        treatmentTypeComboBox
                .addSelectionChangedListener(new SelectionChangedListener<VocabularyTermModel>()
                    {

                        @Override
                        public void selectionChanged(SelectionChangedEvent<VocabularyTermModel> se)
                        {
                            update();

                        }
                    });
        add(treatmentTypeComboBox);
        add(new LabelToolItem(viewContext.getMessage(Dict.AGGREGATE_ORIGINAL_LABEL)
                + GenericConstants.LABEL_SEPARATOR));
        aggregateOriginalCheckBox = new CheckBox();
        aggregateOriginalCheckBox.addListener(Events.Change, new Listener<BaseEvent>()
            {

                @Override
                public void handleEvent(BaseEvent be)
                {
                    update();
                }
            });
        add(aggregateOriginalCheckBox);
    }

    private ComboBox<FalseDiscoveryRateModel> createFDRComboBox(
            SelectionChangedListener<FalseDiscoveryRateModel> changedListener)
    {
        ComboBox<FalseDiscoveryRateModel> comboBox = new ComboBox<FalseDiscoveryRateModel>();
        ListStore<FalseDiscoveryRateModel> listStore = new ListStore<FalseDiscoveryRateModel>();
        FalseDiscoveryRateModel fdr0 = new FalseDiscoveryRateModel(0.0);
        listStore.add(fdr0);
        listStore.add(new FalseDiscoveryRateModel(0.01));
        listStore.add(new FalseDiscoveryRateModel(0.025));
        listStore.add(new FalseDiscoveryRateModel(0.05));
        listStore.add(new FalseDiscoveryRateModel(0.1));
        comboBox.setStore(listStore);
        comboBox.setDisplayField(SimpleModel.PROPERTY);
        comboBox.setValue(fdr0);
        comboBox.addSelectionChangedListener(changedListener);
        comboBox.setEnabled(true);
        comboBox.setWidth(70);
        comboBox.setTriggerAction(TriggerAction.ALL);
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(comboBox, viewContext.getMessage(Dict.FDR_COMBO_INFO),
                infoIcon.createImage(), 3, 3);
        return comboBox;
    }

    private ComboBox<AggregateFunctionModel> createAggregateFunctionComboBox(
            SelectionChangedListener<AggregateFunctionModel> changedListener)
    {
        ComboBox<AggregateFunctionModel> comboBox = new ComboBox<AggregateFunctionModel>();
        ListStore<AggregateFunctionModel> store = new ListStore<AggregateFunctionModel>();
        AggregateFunctionModel defaultModel = null;
        for (AggregateFunction aggregateFunction : AggregateFunction.values())
        {
            AggregateFunctionModel model = new AggregateFunctionModel(aggregateFunction);
            store.add(model);
            if (aggregateFunction == DEFAULT_AGGREGATE_FUNCTION)
            {
                defaultModel = model;
            }
        }
        comboBox.setStore(store);
        comboBox.setDisplayField(SimpleModel.PROPERTY);
        comboBox.setValue(defaultModel);
        comboBox.addSelectionChangedListener(changedListener);
        comboBox.setWidth(100);
        comboBox.setEnabled(true);
        comboBox.setTriggerAction(TriggerAction.ALL);
        return comboBox;
    }

    Experiment getExperimentOrNull()
    {
        return experiment;
    }

    void setBrowserGrid(ProteinByExperimentBrowserGrid browserGrid)
    {
        this.browserGrid = browserGrid;
    }

    void setSummaryGrid(ProteinSummaryGrid summaryGrid)
    {
        this.summaryGrid = summaryGrid;
    }

    void update()
    {
        if (experiment != null)
        {
            final TechId experimentID = TechId.create(experiment);
            browserGrid.setLoadMaskImmediately(true);
            browserGrid.setPostRefreshCallback(new IDataRefreshCallback()
                {
                    @Override
                    public void postRefresh(boolean wasSuccessful)
                    {
                        if (summaryGrid != null)
                        {
                            summaryGrid.setLoadMaskImmediately(true);
                            summaryGrid.update(experimentID);
                        }
                    }
                });
            double falseDiscoveryRate = getSelection(fdrComboBox, 0.0);
            AggregateFunction aggregateFunction =
                    getSelection(aggregateFunctionComboBox, DEFAULT_AGGREGATE_FUNCTION);
            VocabularyTermModel value = treatmentTypeComboBox.getValue();
            String treatmentTypeCode = value == null ? null : value.getTerm().getCode();
            AsyncCallback<List<AbundanceColumnDefinition>> callback =
                    new AbundanceColumnDefinitionsCallback(viewContext, browserGrid, experimentID,
                            falseDiscoveryRate, aggregateFunction, treatmentTypeCode,
                            aggregateOriginalCheckBox.getValue());
            viewContext.getService().getAbundanceColumnDefinitionsForProteinByExperiment(
                    experimentID, treatmentTypeCode, callback);
        }
    }

    ListProteinByExperimentCriteria getCriteria()
    {
        ListProteinByExperimentCriteria criteria = new ListProteinByExperimentCriteria();
        criteria.setExperimentID(TechId.create(experiment));
        criteria.setFalseDiscoveryRate(getSelection(fdrComboBox, 0.0));
        criteria.setAggregateFunction(getSelection(aggregateFunctionComboBox, DEFAULT_AGGREGATE_FUNCTION));
        VocabularyTermModel value = treatmentTypeComboBox.getValue();
        String treatmentTypeCode = value == null ? null : value.getTerm().getCode();
        criteria.setTreatmentTypeCode(treatmentTypeCode);
        criteria.setAggregateOriginal(aggregateOriginalCheckBox.getValue());
        return criteria;
    }

    private <T> T getSelection(ComboBox<? extends SimpleModel<T>> comboBox, T defaultValue)
    {
        List<? extends SimpleModel<T>> selection = comboBox.getSelection();
        return selection.isEmpty() ? defaultValue : selection.get(0).getObject();
    }

    private static final class AbundanceColumnDefinitionsCallback extends
            AbstractAsyncCallback<List<AbundanceColumnDefinition>>
    {
        private final ProteinByExperimentBrowserGrid browserGrid;

        private final TechId experimentID;

        private final double falseDiscoveryRate;

        private final AggregateFunction aggregateFunction;

        private final String treatmentTypeCode;

        private final boolean aggregateOriginal;

        public AbundanceColumnDefinitionsCallback(IViewContext<?> viewContext,
                ProteinByExperimentBrowserGrid browserGrid, TechId experimentID, double falseDiscoveryRate,
                AggregateFunction aggregateFunction, String treatmentTypeCode,
                boolean aggregateOriginal)
        {
            super(viewContext);
            this.browserGrid = browserGrid;
            this.experimentID = experimentID;
            this.falseDiscoveryRate = falseDiscoveryRate;
            this.aggregateFunction = aggregateFunction;
            this.treatmentTypeCode = treatmentTypeCode;
            this.aggregateOriginal = aggregateOriginal;
        }

        @Override
        protected void process(List<AbundanceColumnDefinition> result)
        {
            browserGrid.update(experimentID, falseDiscoveryRate, aggregateFunction,
                    treatmentTypeCode, aggregateOriginal, result);
        }

    }
}
