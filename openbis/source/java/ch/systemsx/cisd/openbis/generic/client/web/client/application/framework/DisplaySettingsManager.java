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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailViewConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * Manager of {@link DisplaySettings}. The manager itself is stateless. It only changes the wrapped
 * {@link DisplaySettings} object. The two attributes of this class are assumed to be de facto
 * singletons. The display setting manager will be created as often as components to be managed are
 * created.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManager
{

    private static final int QUITE_TIME_BEFORE_SETTINGS_SAVED_MS = 10000;

    private final DisplaySettings displaySettings;

    private final IDelayedUpdater updater;

    private final WebClientConfiguration webClientConfiguration;

    /**
     * Private, we need this interface to make tests easier. We wrap {@link DelayedTask} which
     * requires the access to the browser.
     */
    public interface IDelayedUpdater
    {
        /** Cancels any running timers and starts a new one. */
        void executeDelayed(int delayMs);
    }

    /**
     * Creates an instance for the specified display settings.
     */
    public DisplaySettingsManager(DisplaySettings displaySettings,
            final IDelegatedAction settingsUpdater, WebClientConfiguration webClientConfiguration)
    {
        this(displaySettings, createDelayedUpdater(settingsUpdater), webClientConfiguration);
    }

    @SuppressWarnings("deprecation")
    private static IDelayedUpdater createDelayedUpdater(final IDelegatedAction settingsUpdater)
    {
        if (ClientStaticState.isSimpleMode())
        {
            return new IDelayedUpdater()
                {
                    public void executeDelayed(int delayMs)
                    {
                        // in simple view mode settings are temporary - don't save them at all
                    }
                };
        } else
        {
            final DelayedTask delayedTask = new DelayedTask(new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent event)
                    {
                        settingsUpdater.execute();
                    }
                });
            return new IDelayedUpdater()
                {
                    public void executeDelayed(int delayMs)
                    {
                        delayedTask.delay(delayMs);
                    }
                };
        }
    }

    /**
     * Private, for tests only
     */
    public DisplaySettingsManager(DisplaySettings displaySettings, final IDelayedUpdater updater,
            WebClientConfiguration webClientConfiguration)
    {
        if (displaySettings == null)
        {
            throw new IllegalArgumentException("Unspecified display manager.");
        }
        this.displaySettings = displaySettings;
        this.webClientConfiguration = webClientConfiguration;
        this.updater = updater;
    }

    /**
     * This method is deprecated and will be removed when AbstractGridBrowser will be used
     * everywhere.
     */
    public <M extends ModelData> void prepareGrid(final String displayTypeID, final Grid<M> grid)
    {
        final ArrayList<String> emptyFilters = new ArrayList<String>();
        IDisplaySettingsGetter displaySettingsUpdater = new IDisplaySettingsGetter()
            {
                public ColumnModel getColumnModel()
                {
                    return grid.getColumnModel();
                }

                public List<String> getFilteredColumnIds()
                {
                    return emptyFilters;
                }

                public Object getModifier()
                {
                    return this;
                }
            };
        GridDisplaySettings settings =
                tryApplySettings(displayTypeID, grid.getColumnModel(), emptyFilters);
        if (settings != null)
        {
            grid.reconfigure(grid.getStore(), new ColumnModel(settings.getColumnConfigs()));
        }
        registerGridSettingsChangesListener(displayTypeID, displaySettingsUpdater);
    }

    /**
     * Register listeners which monitors all the column configuration changes and makes them
     * persistent.
     */
    public void registerGridSettingsChangesListener(final String displayTypeID,
            final IDisplaySettingsGetter grid)
    {

        Listener<ColumnModelEvent> listener = new Listener<ColumnModelEvent>()
            {
                public void handleEvent(ColumnModelEvent event)
                {
                    // When FAKE width change event is fired display settings are NOT updated.
                    // check: AbstractBrowserGrid.refreshColumnHeaderWidths()
                    if (isFakeWidthChangeEvent(event))
                    {
                        return;
                    }
                    storeSettings(displayTypeID, grid, true);
                }

                /**
                 * Is specified <code>event</code> a fake width change event that does not change
                 * width?
                 */
                private boolean isFakeWidthChangeEvent(ColumnModelEvent event)
                {
                    if (event.getType() == Events.WidthChange)
                    {
                        List<ColumnSetting> colSettings = getColumnSettings(displayTypeID);
                        if (colSettings != null && colSettings.get(event.getColIndex()) != null)
                        {
                            int oldWidth = colSettings.get(event.getColIndex()).getWidth();
                            int newWidth = event.getWidth();
                            return oldWidth == newWidth;
                        }
                    }

                    return false;
                }
            };
        ColumnModel columnModel = grid.getColumnModel();
        columnModel.addListener(Events.WidthChange, listener);
    }

    /**
     * Synchronizes the initial grid display settings with the settings stored at the specified
     * display type ID. Stored settings (if any) override the current settings.
     */
    public GridDisplaySettings tryApplySettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds)
    {
        List<ColumnSetting> columnSettings = getColumnSettings(displayTypeID);
        if (columnSettings == null)
        {
            return null;
        }
        return tryApplySettings(columnSettings, columnModel, filteredColumnIds);
    }

    public static class GridDisplaySettings
    {
        List<ColumnConfig> columnConfigs;

        List<String> filteredColumnIds;

        public GridDisplaySettings(List<ColumnConfig> columnConfigs, List<String> filteredColumnIds)
        {
            this.columnConfigs = columnConfigs;
            this.filteredColumnIds = filteredColumnIds;
        }

        public List<ColumnConfig> getColumnConfigs()
        {
            return columnConfigs;
        }

        public void setColumnConfigs(List<ColumnConfig> columnConfigs)
        {
            this.columnConfigs = columnConfigs;
        }

        public List<String> getFilteredColumnIds()
        {
            return filteredColumnIds;
        }

        public void setFilteredColumnIds(List<String> filteredColumnIds)
        {
            this.filteredColumnIds = filteredColumnIds;
        }
    }

    /**
     * Update grid columns and filters by applying the specified settings.
     * 
     * @param filteredColumnIds used only to check if the user settings are different form the
     *            defaults
     */
    private static GridDisplaySettings tryApplySettings(List<ColumnSetting> columnSettings,
            ColumnModel columnModel, List<String> filteredColumnIds)
    {
        boolean refreshNeeded = false;
        List<ColumnConfig> newColumnConfigList = new ArrayList<ColumnConfig>();
        Set<Integer> indices = new HashSet<Integer>();
        List<String> newFilteredColumnIds = new ArrayList<String>();
        for (int i = 0; i < columnSettings.size(); i++)
        {
            ColumnSetting columnSetting = columnSettings.get(i);
            // update column using the settings stored for it
            String columnID = columnSetting.getColumnID();
            int index = columnModel.getIndexById(columnID);
            if (index >= 0)
            {
                if (i != index)
                {
                    refreshNeeded = true;
                }
                indices.add(index);
                ColumnConfig columnConfig = columnModel.getColumn(index);
                boolean hidden = columnSetting.isHidden();
                if (columnConfig.isHidden() != hidden)
                {
                    columnConfig.setHidden(hidden);
                    refreshNeeded = true;
                }
                int width = columnSetting.getWidth();
                if (columnConfig.getWidth() != width)
                {
                    columnConfig.setWidth(width);
                    refreshNeeded = true;
                }
                newColumnConfigList.add(columnConfig);
                if (columnSetting.hasFilter())
                {
                    newFilteredColumnIds.add(columnID);
                }
            }
        }
        // add columns for which no settings were stored at the end
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            if (indices.contains(i) == false)
            {
                newColumnConfigList.add(columnModel.getColumn(i));
            }
        }
        if (newFilteredColumnIds.equals(filteredColumnIds) == false)
        {
            refreshNeeded = true;
        }
        if (refreshNeeded)
        {
            return new GridDisplaySettings(newColumnConfigList, newFilteredColumnIds);
        } else
        {
            return null;
        }
    }

    public void storeSettings(final String displayTypeID, final IDisplaySettingsGetter grid,
            boolean delayed)
    {
        int delayMs = delayed ? QUITE_TIME_BEFORE_SETTINGS_SAVED_MS : 1; // zero not allowed
        storeSettings(displayTypeID, grid.getColumnModel(), grid.getFilteredColumnIds(),
                grid.getModifier(), delayMs);
    }

    public void storeActiveTabSettings(String tabGroupDisplayID, String selectedTabDisplayID,
            Object modifier)
    {
        updateActiveTabSettings(tabGroupDisplayID, selectedTabDisplayID, modifier);
        updater.executeDelayed(QUITE_TIME_BEFORE_SETTINGS_SAVED_MS);
    }

    private void storeSettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds, Object modifier, int delayMs)
    {
        List<ColumnSetting> columnSettings = createColumnsSettings(columnModel, filteredColumnIds);
        updateColumnSettings(displayTypeID, columnSettings, modifier);
        updater.executeDelayed(delayMs);
    }

    public void storeDropDownSettings(String dropDownSettingsID, String newValue)
    {
        updateDropDownSettings(dropDownSettingsID, newValue);
        updater.executeDelayed(QUITE_TIME_BEFORE_SETTINGS_SAVED_MS);
    }

    public void storeSettings()
    {
        updater.executeDelayed(1); // 0 not allowed
    }

    private static List<ColumnSetting> createColumnsSettings(ColumnModel columnModel,
            List<String> filteredColumnIdsList)
    {
        Set<String> filteredColumnIds = new HashSet<String>(filteredColumnIdsList);
        List<ColumnSetting> columnSettings = new ArrayList<ColumnSetting>();
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnConfig columnConfig = columnModel.getColumn(i);
            ColumnSetting columnSetting = new ColumnSetting();
            columnSetting.setColumnID(columnConfig.getId());
            columnSetting.setHidden(columnConfig.isHidden());
            columnSetting.setWidth(columnConfig.getWidth());
            boolean hasFilter = filteredColumnIds.contains(columnConfig.getId());
            columnSetting.setHasFilter(hasFilter);
            columnSettings.add(columnSetting);
        }
        return columnSettings;
    }

    // delegator

    /**
     * @returns columns settings for given display id
     *          <p>
     *          NOTE: Returned value should be used read only, or modification time should be set
     *          manually after a modification.
     */
    @SuppressWarnings("deprecation")
    public final List<ColumnSetting> getColumnSettings(String gridDisplayTypeID)
    {
        return displaySettings.getColumnSettings().get(gridDisplayTypeID);
    }

    /**
     * update column settings for given display id (modification date is updated automatically)
     */
    @SuppressWarnings("deprecation")
    public final void updateColumnSettings(String gridDisplayTypeID,
            List<ColumnSetting> newSettings, Object modifier)
    {
        displaySettings.getColumnSettings().put(gridDisplayTypeID, newSettings);
    }

    /**
     * @returns tab settings for given panel - which tab should be selected<br>
     * <br>
     *          NOTE: Returned value should be used read only, or modification time should be set
     *          manually after a modification.
     */
    @SuppressWarnings("deprecation")
    public final String getActiveTabSettings(String tabGroupDisplayTypeID)
    {
        return displaySettings.getTabSettings().get(tabGroupDisplayTypeID);
    }

    /**
     * @returns hidden tabs for given panel - which tab should be selected<br>
     * <br>
     *          NOTE: Returned value should be used read only
     */
    public final DetailViewConfiguration tryGetDetailViewSettings(String entityDetailViewID)
    {
        return webClientConfiguration.getViews().get(entityDetailViewID);
    }

    /**
     * update section settings for given display id (modification date is updated automatically)
     */
    @SuppressWarnings("deprecation")
    private final void updateActiveTabSettings(String tabGroupDisplayID,
            String selectedTabDisplayID, Object modifier)
    {
        displaySettings.getTabSettings().put(tabGroupDisplayID, selectedTabDisplayID);
    }

    // TODO 2010-09-27, Piotr Buczek: store is not invoked
    /**
     * @return True if the given section is collapsed
     */
    @SuppressWarnings("deprecation")
    public final Boolean tryGetPanelCollapsedSetting(String panelId)
    {
        return displaySettings.getPanelCollapsedSettings().get(panelId);
    }

    @SuppressWarnings("deprecation")
    public final void updatePanelCollapsedSetting(String panelId, Boolean value)
    {
        displaySettings.getPanelCollapsedSettings().put(panelId, value);
    }

    @SuppressWarnings("deprecation")
    public final Integer tryGetPanelSizeSetting(String panelId)
    {
        return displaySettings.getPanelSizeSettings().get(panelId);
    }

    @SuppressWarnings("deprecation")
    public final void updatePanelSizeSetting(String panelId, Integer value)
    {
        displaySettings.getPanelSizeSettings().put(panelId, value);
    }

    //

    /**
     * @returns section settings for given display id<br>
     */
    @SuppressWarnings("deprecation")
    public final boolean isUseWildcardSearchMode()
    {
        return displaySettings.isUseWildcardSearchMode();
    }

    /**
     * update section settings for given display id (modification date is updated automatically)
     */
    @SuppressWarnings("deprecation")
    public final void updateUseWildcardSearchMode(Boolean newValue)
    {
        displaySettings.setUseWildcardSearchMode(newValue);
    }

    /**
     * Should error messages from custom columns be displayed in debugging or user format?
     */
    public final boolean isDisplayCustomColumnDebuggingErrorMessages()
    {
        return displaySettings.isDisplayCustomColumnDebuggingErrorMessages();
    }

    public final void setDisplayCustomColumnDebuggingErrorMessages(boolean isDebugging)
    {
        displaySettings.setDisplayCustomColumnDebuggingErrorMessages(isDebugging);
    }

    @SuppressWarnings("deprecation")
    public final RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        return displaySettings.getRealNumberFormatingParameters();
    }

    @SuppressWarnings("deprecation")
    public String getDropDownSettings(String dropDownID)
    {
        return displaySettings.getDropDownSettings().get(dropDownID);
    }

    @SuppressWarnings("deprecation")
    private void updateDropDownSettings(String dropDownSettingsID, String newValue)
    {
        displaySettings.getDropDownSettings().put(dropDownSettingsID, newValue);
    }

}
