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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ClientPluginProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.LoadableColumnConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The grid part of samples browser.
 * 
 * @author Christian Ribeaud
 */
class SampleBrowserGrid extends LayoutContainer
{
    private final GenericViewContext viewContext;

    private ContentPanel contentPanel;

    private Grid<SampleModel> grid;

    private final GridConfiguration oldConfiguration;

    private final GridConfiguration newConfiguration;

    private final CommonColumns commonColumns;

    private final ParentColumns parentColumns;

    private final PropertyColumns propertyColumns;

    public SampleBrowserGrid(final GenericViewContext viewContext, CommonColumns commonColumns,
            ParentColumns parentColumns, PropertyColumns propertyColumns)
    {
        this.viewContext = viewContext;
        this.commonColumns = commonColumns;
        this.parentColumns = parentColumns;
        this.propertyColumns = propertyColumns;
        oldConfiguration = new GridConfiguration(); // Already loaded
        newConfiguration = new GridConfiguration(); // Requested
        setLayout(new FitLayout());
    }

    private void display(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        newConfiguration.update(sampleType, selectedGroupCode, showGroup, showInstance);
        final ColumnModel columnModel = createColumnModel(sampleType);
        final String header = createHeader(sampleType, selectedGroupCode, showGroup, showInstance);
        getContentPanel().setHeading(header);
        final List<SampleModel> models =
                grid != null && grid.getStore() != null ? grid.getStore().getModels() : null;

        final RpcProxy<Object, List<SampleModel>> proxy = new RpcProxy<Object, List<SampleModel>>()
            {
                @Override
                public final void load(final Object loadConfig,
                        final AsyncCallback<List<SampleModel>> callback)
                {
                    if (oldConfiguration.majorChange(newConfiguration))
                    {
                        viewContext.getService().listSamples(sampleType, selectedGroupCode,
                                showGroup, showInstance, propertyColumns.getDirtyColumns(),
                                new ListSamplesCallback(viewContext, callback));
                        return;
                    }

                    if (oldConfiguration.minorChange(newConfiguration))
                    {
                        removeUnnecessarySamples(models);
                    }

                    if (propertyColumns.isDirty())
                    {
                        viewContext.getService().updateSamples(extractSamplesFromModel(models),
                                propertyColumns.getDirtyColumns(),
                                new ListSamplesCallback(viewContext, callback));
                        return;
                    }

                    callback.onSuccess(models);
                }

                private List<Sample> extractSamplesFromModel(final List<SampleModel> modelList)
                {
                    List<Sample> samples = new ArrayList<Sample>();
                    for (SampleModel model : modelList)
                    {
                        samples.add((Sample) model.get(SampleModel.OBJECT));
                    }
                    return samples;
                }
            };
        final ListLoader<?> loader = createListLoader(proxy);
        final ListStore<SampleModel> sampleStore = new ListStore<SampleModel>(loader);
        createOrReconfigureGrid(columnModel, sampleStore);
        loader.load();
        layout();

    }

    protected void removeUnnecessarySamples(List<SampleModel> models)
    {
        Iterator<SampleModel> iterator = models.iterator();
        while (iterator.hasNext())
        {
            SampleModel next = iterator.next();
            final Boolean isGroupLevelSample = (Boolean) next.get(SampleModel.IS_GROUP_SAMPLE);
            final boolean isInstanceLevelSample = isGroupLevelSample == false;
            if (isGroupLevelSample && newConfiguration.isIncludeGroup() == false
                    || isInstanceLevelSample && newConfiguration.isIncludeInstance() == false)
            {
                iterator.remove();
            }
        }
        oldConfiguration.update(newConfiguration);
    }

    @SuppressWarnings("unchecked")
    private final static ListLoader<?> createListLoader(
            final RpcProxy<Object, List<SampleModel>> proxy)
    {
        return new BaseListLoader(proxy);
    }

    private final void createOrReconfigureGrid(final ColumnModel columnModel,
            final ListStore<SampleModel> sampleStore)
    {
        if (grid == null)
        {
            grid = new Grid<SampleModel>(sampleStore, columnModel);
            grid.setLoadMask(true);
            grid.addListener(Events.CellClick, new Listener<GridEvent>()
                {

                    //
                    // Listener
                    //

                    public final void handleEvent(final GridEvent be)
                    {
                        final SampleModel sampleModel =
                                (SampleModel) be.grid.getStore().getAt(be.rowIndex);
                        final SampleType sampleType =
                                (SampleType) sampleModel.get(SampleModel.SAMPLE_TYPE);
                        final String sampleIdentifier =
                                sampleModel.get(SampleModel.SAMPLE_IDENTIFIER);
                        final String code = sampleType.getCode();
                        final IClientPluginFactory pluginFactory =
                                ClientPluginProvider.getPluginFactory(code);
                        pluginFactory.createViewClientForSampleType(code).viewSample(
                                sampleIdentifier);
                    }
                });
            getContentPanel().add(grid);
        } else
        {
            grid.reconfigure(sampleStore, columnModel);
        }
    }

    private final ContentPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            contentPanel = new ContentPanel();
            contentPanel.setLayout(new FitLayout());
            add(contentPanel);
        }
        return contentPanel;
    }

    private String createHeader(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        final StringBuilder sb = new StringBuilder("Samples");
        sb.append(" of type ");
        sb.append(sampleType.getCode());
        if (showGroup)
        {
            sb.append(" belonging to the group ");
            sb.append(selectedGroupCode);
        }
        if (showInstance)
        {
            if (showGroup)
            {
                sb.append(" or shared");
            } else
            {
                sb.append(" which are shared among all the groups");
            }
        }
        return sb.toString();
    }

    public final void refresh(final SampleType sampleType, final String selectedGroupCode,
            final boolean showGroup, final boolean showInstance)
    {
        display(sampleType, selectedGroupCode, showGroup, showInstance);
    }

    private final ColumnModel createColumnModel(final SampleType type)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.addAll(commonColumns.getColumns());
        configs.addAll(parentColumns.getColumns());
        configs.addAll(propertyColumns.getColumns());
        return new ColumnModel(configs);
    }

    private final class ListSamplesCallback extends AbstractAsyncCallback<List<Sample>>
    {
        private final AsyncCallback<List<SampleModel>> delegate;

        ListSamplesCallback(final GenericViewContext viewContext,
                final AsyncCallback<List<SampleModel>> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final List<Sample> result)
        {
            final List<SampleModel> sampleModels = new ArrayList<SampleModel>(result.size());
            for (final Sample sample : result)
            {
                sampleModels.add(new SampleModel(sample));
            }
            oldConfiguration.update(newConfiguration);
            updateLoadedPropertyColumns();
            delegate.onSuccess(sampleModels);
        }
    }

    private void updateLoadedPropertyColumns()
    {
        if (propertyColumns.isDirty())
        {
            for (LoadableColumnConfig cc : propertyColumns.getColumns())
            {
                if (cc.isDirty())
                {
                    cc.setLoaded(true);
                }
            }
        }
    }

}