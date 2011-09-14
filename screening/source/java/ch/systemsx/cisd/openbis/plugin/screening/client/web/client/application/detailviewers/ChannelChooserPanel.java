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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * An UI panel for selecting image channels.
 * 
 * @author Kaloyan Enimanev
 */
public class ChannelChooserPanel extends LayoutContainer
{
    private static class ComboBoxGroup extends MultiField<Widget>
    {
        private ComboBoxGroup()
        {
            super();

            this.setSpacing(5);
        }
    }

    /**
     * Can be used from external classes wishing to be notified when the channel selection changes.
     */
    public static interface ChannelSelectionListener
    {
        void selectionChanged(List<String> channels, String imageTransformationCodeOrNull);
    }

    public static final String DEFAULT_TRANSFORMATION_CODE = "$DEFAULT$";

    private static final LabeledItem<ImageTransformationInfo> DEFAULT_TRANSFORMATION =
            convertToLabeledItem(new ImageTransformationInfo(DEFAULT_TRANSFORMATION_CODE, "Default",
                    "Default transformation or original picture if not tranformed.", "", false));

    private final IMessageProvider messageProvider;

    private IDefaultChannelState defaultChannelState;

    private CheckBoxGroup channelsCheckBoxGroup;

    private SimpleComboBox<String> channelsComboBox;

    private SimpleModelComboBox<ImageTransformationInfo> transformationsComboBox;

    private LabelField adjustLabel = new LabelField("Adjust:");

    private Map<String, Set<ImageTransformationInfo>> transformationsForChannels =
            new HashMap<String, Set<ImageTransformationInfo>>();

    private List<ChannelSelectionListener> channelSelectionListeners =
            new ArrayList<ChannelSelectionListener>();

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {
            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    private final Listener<BaseEvent> transformationSelection = new Listener<BaseEvent>()
        {
            public void handleEvent(BaseEvent be)
            {
                defaultChannelState.setDefaultTransformation(getSelectedValues().get(0),
                        transformationsComboBox.getSimpleValue().getItem().getCode());
                notifySelectionListeners(getSelectedValues(), tryGetSelectedTransformationCode());
            }

        };

    public ChannelChooserPanel(IMessageProvider messageProvider,
            IDefaultChannelState defChannelState)
    {
        this(messageProvider, defChannelState, Collections.<String> emptyList(), Collections
                .<String> emptyList(), null);
    }

    public ChannelChooserPanel(IMessageProvider messageProvider,
            IDefaultChannelState defChannelState, List<String> names,
            List<String> selectedChannelsOrNull, ImageDatasetParameters imageDatasetParameters)
    {
        this.messageProvider = messageProvider;
        this.defaultChannelState = defChannelState;

        setAutoHeight(true);
        setAutoWidth(true);

        channelsComboBox = createChannelsComboBox();

        transformationsComboBox =
                new SimpleModelComboBox<ImageTransformationInfo>(this.messageProvider,
                        new ArrayList<LabeledItem<ImageTransformationInfo>>(), null);
        transformationsComboBox.setTriggerAction(TriggerAction.ALL);
        transformationsComboBox.setAllowBlank(false);
        transformationsComboBox.setEditable(false);
        transformationsComboBox.addListener(Events.SelectionChange, transformationSelection);

        ComboBoxGroup group = new ComboBoxGroup();
        group.add(channelsComboBox);
        group.add(adjustLabel);
        group.add(transformationsComboBox);

        add(group);

        channelsCheckBoxGroup = createCheckBoxGroup();
        add(channelsCheckBoxGroup);

        addChannels(names, imageDatasetParameters);
        updateChannelSelection(selectedChannelsOrNull);
    }

    private SimpleComboBox<String> createChannelsComboBox()
    {
        SimpleComboBox<String> comboBox = new SimpleComboBox<String>();

        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setAllowBlank(false);
        comboBox.setEditable(false);
        comboBox.setEmptyText("Choose...");
        comboBox.addListener(Events.SelectionChange, selectionChangeListener);

        return comboBox;
    }

    private CheckBoxGroup createCheckBoxGroup()
    {
        CheckBoxGroup group = new CheckBoxGroup();
        return group;
    }

    /**
     * adds a {@link ChannelSelectionListener} that will be receiving notifications when the
     * selection changes.
     */
    public void addSelectionChangedListener(ChannelSelectionListener selectionListener)
    {
        channelSelectionListeners.add(selectionListener);
    }

    /**
     * a quite specific method, currently only needed by the well-search grid.
     */
    public void addCodes(ImageDatasetParameters imageParameters)
    {
        addChannels(imageParameters.getChannelsCodes(), imageParameters);
        updateChannelSelection(null);
    }

    /**
     * @return the current channel selection. If all channels are selected, returns a list
     *         containing a single element - {@link ScreeningConstants#MERGED_CHANNELS}.
     */
    public List<String> getSelectedValues()
    {
        String comboBoxValue = channelsComboBox.getSimpleValue();
        if (comboBoxValue == null)
        {
            return Collections.<String> emptyList();
        }
        if (ScreeningConstants.MERGED_CHANNELS.equals(comboBoxValue))
        {
            // multiple channel selection
            return getMergedChannelSelection();
        } else
        {
            // single channel selection
            return Collections.singletonList(comboBoxValue);
        }
    }

    private List<String> getMergedChannelSelection()
    {
        boolean allSelected = true;
        List<String> channels = new ArrayList<String>();
        for (CheckBox cb : getAllCheckBoxes())
        {
            if (cb.getValue() == true)
            {
                channels.add(cb.getBoxLabel());
            } else
            {
                allSelected = false;
            }
        }

        if (allSelected)
        {
            // all channels selected
            // do not list them one-by-one. use the more general "MERGED_CHANNELS" term instead
            return Collections.singletonList(ScreeningConstants.MERGED_CHANNELS);
        }
        return channels;
    }

    private void addChannels(List<String> codes, ImageDatasetParameters imageParameters)
    {
        addCodeToComboBox(ScreeningConstants.MERGED_CHANNELS);
        if (codes == null || codes.isEmpty())
        {
            return;
        }

        List<CheckBox> newCheckBoxes = new ArrayList<CheckBox>();
        for (String code : codes)
        {
            boolean codeAdded = addCodeToComboBox(code);

            if (imageParameters != null)
            {
                Set<ImageTransformationInfo> transformationsForChannel =
                        transformationsForChannels.get(code);
                if (transformationsForChannel == null)
                {
                    transformationsForChannel = new LinkedHashSet<ImageTransformationInfo>();
                    transformationsForChannels.put(code, transformationsForChannel);
                }
                transformationsForChannel.addAll(imageParameters
                        .getAvailableImageTransformationsFor(code));
            }

            if (codeAdded)
            {
                // also add a checkBockbox for the channel
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(code);
                checkBox.addListener(Events.Change, selectionChangeListener);
                newCheckBoxes.add(checkBox);
            }
        }

        updateCheckBoxGroup(newCheckBoxes);
    }

    private boolean addCodeToComboBox(String code)
    {
        if (channelsComboBox.findModel(code) == null)
        {
            channelsComboBox.add(code);
            return true;
        }
        return false;
    }

    private void updateChannelSelection(List<String> selectedChannels)
    {
        List<String> channels = selectedChannels;
        if (channels == null || channels.size() == 0)
        {
            if (defaultChannelState != null)
            {
                channels = defaultChannelState.tryGetDefaultChannels();
            }
        }

        String comboBoxValue = ScreeningConstants.MERGED_CHANNELS;
        if (channels != null && channels.size() == 1)
        {
            comboBoxValue = channels.get(0);
        }

        channelsComboBox.setSimpleValue(comboBoxValue);
        initializeCheckBoxValues(channels);
        updateTransformationComboBox();
    }

    private void initializeCheckBoxValues(List<String> selectedChannels)
    {
        boolean selectAllChannels = (selectedChannels == null) || (selectedChannels.size() < 2);

        for (CheckBox cb : getAllCheckBoxes())
        {
            @SuppressWarnings("null")
            boolean checked = selectAllChannels || selectedChannels.contains(cb.getBoxLabel());
            cb.setValue(checked);
        }

    }

    private void selectionChanged()
    {
        List<String> selection = getSelectedValues();
        defaultChannelState.setDefaultChannels(selection);

        String selectedComboValue = channelsComboBox.getSimpleValue();
        boolean showCheckBoxGroup = ScreeningConstants.MERGED_CHANNELS.equals(selectedComboValue);
        channelsCheckBoxGroup.setVisible(showCheckBoxGroup);

        ensureAtLeastOneCheckboxChecked();
        updateTransformationComboBox();

        notifySelectionListeners(selection, tryGetSelectedTransformationCode());
    }

    public String tryGetSelectedTransformationCode()
    {
        if (transformationsComboBox.isVisible())
        {
            String code =
                    transformationsComboBox.getSelection().get(0).getValue().getItem().getCode();

            if (DEFAULT_TRANSFORMATION_CODE.equals(code))
            {
                return null;
            } else
            {
                return code;
            }
        }

        return null;
    }

    private void notifySelectionListeners(List<String> selection,
            String imageTransformationCodeOrNull)
    {
        for (ChannelSelectionListener listener : channelSelectionListeners)
        {
            listener.selectionChanged(selection, imageTransformationCodeOrNull);
        }
    }

    private void updateCheckBoxGroup(List<CheckBox> newCheckBoxes)
    {
        if (newCheckBoxes.isEmpty())
        {
            return;
        }

        boolean recreateCheckBoxGroup = channelsCheckBoxGroup.isRendered();

        if (recreateCheckBoxGroup)
        {
            // we must create a new CheckBoxGroup because the old one
            // wouldn't accept new checkboxes after rendering
            newCheckBoxes.addAll(0, getAllCheckBoxes());
            remove(channelsCheckBoxGroup);
            channelsCheckBoxGroup = createCheckBoxGroup();
        }

        for (CheckBox cb : newCheckBoxes)
        {
            channelsCheckBoxGroup.add(cb);
        }

        if (recreateCheckBoxGroup)
        {
            add(channelsCheckBoxGroup);
            layout(true);
        }

    }

    private void ensureAtLeastOneCheckboxChecked()
    {
        List<CheckBox> checkboxes = getAllCheckBoxes();
        int selected = 0;

        for (CheckBox cb : checkboxes)
        {
            if (cb.getValue())
            {
                selected++;
            }
        }

        if (selected > 1)
        {
            for (CheckBox cb : checkboxes)
            {
                cb.setEnabled(true);
            }
        } else
        {
            for (CheckBox cb : checkboxes)
            {
                if (cb.getValue())
                {
                    cb.setEnabled(false);
                }
            }
        }

    }

    private List<CheckBox> getAllCheckBoxes()
    {
        List<CheckBox> result = new ArrayList<CheckBox>();
        for (Field<?> field : channelsCheckBoxGroup.getAll())
        {
            if (field instanceof CheckBox)
            {
                result.add((CheckBox) field);
            }
        }
        return result;
    }

    private void updateTransformationComboBox()
    {
        List<String> selectedValues = getSelectedValues();

        transformationsComboBox.removeAll();
        transformationsComboBox.clearState();

        List<LabeledItem<ImageTransformationInfo>> model =
                new ArrayList<LabeledItem<ImageTransformationInfo>>();
        Set<ImageTransformationInfo> infos = null;
        if (selectedValues.size() == 1
                && (infos = transformationsForChannels.get(selectedValues.get(0))) != null
                && infos.size() > 0)
        {
            model.add(DEFAULT_TRANSFORMATION);
            for (ImageTransformationInfo imageTransformationInfo : infos)
            {
                model.add(convertToLabeledItem(imageTransformationInfo));
            }
            if (model.size() <= 1)
            {
                transformationsComboBox.setVisible(false);
                adjustLabel.setVisible(false);
                return;
            }
            transformationsComboBox.add(model);
            setTransformationsVisible(true);

            selectTransformation(selectedValues.get(0));
        } else
        {
            setTransformationsVisible(false);
        }
    }

    private void setTransformationsVisible(boolean visible)
    {
        transformationsComboBox.setVisible(visible);
        adjustLabel.setVisible(visible);
    }

    private void selectTransformation(String channelCode)
    {
        boolean selected = false;
        String code = defaultChannelState.tryGetDefaultTransformation(channelCode);
        if (code != null)
        {
            for (SimpleComboValue<LabeledItem<ImageTransformationInfo>> info : transformationsComboBox
                    .getStore().getModels())
            {
                if (info.getValue().getItem().getCode().equals(code))
                {
                    selected = true;
                    // transformationsComboBox.select(info);
                    transformationsComboBox.setSelection(Collections.singletonList(info));
                    break;
                }
            }
        }
        if (false == selected)
        {
            for (SimpleComboValue<LabeledItem<ImageTransformationInfo>> info : transformationsComboBox
                    .getStore().getModels())
            {
                if (info.getValue().getItem().isDefault())
                {
                    // transformationsComboBox.select(info);
                    transformationsComboBox.setSelection(Collections.singletonList(info));
                    selected = true;
                    break;
                }
            }
        }

        if (false == selected)
        {
            transformationsComboBox.setSelection(Collections.singletonList(transformationsComboBox
                    .getStore().getModels().get(0)));
        }
    }

    private static LabeledItem<ImageTransformationInfo> convertToLabeledItem(
            ImageTransformationInfo imageTransformationInfo)
    {
        return new LabeledItem<ImageTransformationInfo>(imageTransformationInfo,
                imageTransformationInfo.getLabel(), imageTransformationInfo.getDescription());
    }
}