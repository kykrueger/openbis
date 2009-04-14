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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentVersionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IAttachmentHolder;

/**
 * {@link SectionPanel} containing attachments.
 * 
 * @author Izabela Adamczyk
 */
public class AttachmentsSection extends SectionPanel
{
    public static final String ATTACHMENTS_ID_PREFIX =
            GenericConstants.ID_PREFIX + "attachment-section_";

    // FIXME: add attachmentHolderKind

    private final IAttachmentHolder attachmentHolder;

    private final IMessageProvider messageProvider;

    public AttachmentsSection(final IAttachmentHolder attachmentHolder,
            final IViewContext<?> viewContext)
    {
        super("Attachments");
        this.attachmentHolder = attachmentHolder;
        messageProvider = viewContext;
        if (attachmentHolder.getAttachments().size() > 0)
        {
            add(createAttachmentsGrid());
        } else
        {
            add(new Html(messageProvider.getMessage(Dict.NO_ATTACHMENTS_FOUND, attachmentHolder
                    .getAttachmentHolderKind().name().toLowerCase())), new RowData(-1, -1,
                    new Margins(3)));
        }
    }

    ContentPanel createVersionsPanel()
    {
        ContentPanel panel = new ContentPanel();
        panel.setVisible(false);
        panel.setCollapsible(true);
        return panel;
    }

    private Component createAttachmentsGrid()
    {
        final ListStore<AttachmentModel> attachmentStore = new ListStore<AttachmentModel>();
        attachmentStore.add(AttachmentModel.convert(attachmentHolder.getAttachments()));
        final Grid<AttachmentModel> attachmentGrid =
                new Grid<AttachmentModel>(attachmentStore, new ColumnModel(
                        defineAttachmentColumns()));
        final CellSelectionModel<AttachmentModel> selectionModel =
                new CellSelectionModel<AttachmentModel>();
        attachmentGrid.setSelectionModel(selectionModel);
        attachmentGrid.addListener(Events.CellClick, new Listener<GridEvent>()
            {
                public void handleEvent(final GridEvent be)
                {
                    String column = attachmentGrid.getColumnModel().getColumn(be.colIndex).getId();
                    final AttachmentModel selectedItem =
                            (AttachmentModel) be.grid.getStore().getAt(be.rowIndex);
                    Attachment selectedAttachment =
                            (Attachment) selectedItem.get(ModelDataPropertyNames.OBJECT);
                    int version = selectedAttachment.getVersion();
                    String fileName = selectedAttachment.getFileName();

                    if (ModelDataPropertyNames.FILE_NAME.equals(column))
                    {
                        downloadAttachment(fileName, version);
                    } else if (ModelDataPropertyNames.VERSIONS.equals(column))
                    {
                        List<Attachment> versions = cast(selectedItem);
                        showVersionsPanel(fileName, versions);

                    }
                    attachmentGrid.getSelectionModel().deselectAll();
                }

                private void showVersionsPanel(final String fileName,
                        final List<Attachment> allFiles)
                {
                    final String tabTitle = attachmentHolder.getIdentifier() + ">" + fileName;
                    final ITabItemFactory tabFactory = new ITabItemFactory()
                        {
                            public ITabItem create()
                            {
                                Component component = createVersionsPanel(allFiles, fileName);
                                return DefaultTabItem.createUnaware(tabTitle, component, false);
                            }

                            public String getId()
                            {
                                return createAttachmentVersionTabId(fileName, attachmentHolder
                                        .getIdentifier());
                            }
                        };
                    DispatcherHelper.dispatchNaviEvent(tabFactory);
                }

                private List<Attachment> cast(final AttachmentModel selectedItem)
                {
                    List<Attachment> files = selectedItem.get(ModelDataPropertyNames.VERSIONS);
                    return files;
                }
            });
        attachmentGrid.setId(createAttachmentGridId(attachmentHolder.getIdentifier()));
        return attachmentGrid;
    }

    // @Private
    static String createAttachmentGridId(String identifier)
    {
        return ATTACHMENTS_ID_PREFIX + identifier;
    }

    private List<ColumnConfig> defineAttachmentVersionColumns()
    {
        final ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(createVersionFileName(messageProvider));
        columns.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
        columns.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
        return columns;
    }

    private ContentPanel createVersionsPanel(final List<Attachment> oldVersions,
            final String fileName)
    {

        ContentPanel panel = new ContentPanel();
        panel.setHeading("Versions of file '" + fileName + "' from "
                + attachmentHolder.getAttachmentHolderKind().name().toLowerCase() + " '"
                + attachmentHolder.getIdentifier() + "'");
        final ListStore<AttachmentVersionModel> attachmentStore =
                new ListStore<AttachmentVersionModel>();
        attachmentStore.add(AttachmentVersionModel.convert(oldVersions));
        final Grid<AttachmentVersionModel> attachmentGrid =
                new Grid<AttachmentVersionModel>(attachmentStore, new ColumnModel(
                        defineAttachmentVersionColumns()));
        final CellSelectionModel<AttachmentVersionModel> selectionModel =
                new CellSelectionModel<AttachmentVersionModel>();
        attachmentGrid.setSelectionModel(selectionModel);
        selectionModel.bindGrid(attachmentGrid);
        attachmentGrid.addListener(Events.CellClick, new Listener<GridEvent>()
            {
                public void handleEvent(final GridEvent be)
                {
                    String column = attachmentGrid.getColumnModel().getColumn(be.colIndex).getId();
                    if (ModelDataPropertyNames.VERSION_FILE_NAME.equals(column))
                    {
                        final AttachmentVersionModel selectedItem =
                                (AttachmentVersionModel) be.grid.getStore().getAt(be.rowIndex);
                        Attachment selectedAttachment =
                                (Attachment) selectedItem.get(ModelDataPropertyNames.OBJECT);
                        int version = selectedAttachment.getVersion();
                        downloadAttachment(fileName, version);
                    }
                    attachmentGrid.getSelectionModel().deselectAll();
                }
            });
        panel.setId(createAttachmentVersionTabId(fileName, attachmentHolder.getIdentifier()));
        panel.add(attachmentGrid);
        return panel;
    }

    // @Private
    static String createAttachmentVersionTabId(final String fileName, String experimentIdentifier)
    {
        return GenericConstants.ID_PREFIX + "attachment-versions-" + experimentIdentifier + "_"
                + fileName;
    }

    private void downloadAttachment(String fileName, int version)
    {
        WindowUtils.openWindow(createURL(version, fileName, attachmentHolder));
    }

    private final static String createURL(final int version, final String fileName,
            final IAttachmentHolder exp)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.VERSION_PARAMETER, version);
        methodWithParameters.addParameter(GenericConstants.FILE_NAME_PARAMETER, fileName);
        methodWithParameters.addParameter(GenericConstants.ATTACHMENT_HOLDER_PARAMETER, exp
                .getAttachmentHolderKind().name());
        methodWithParameters.addParameter(GenericConstants.IDENTIFIER_PARAMETER, exp
                .getIdentifier());
        return methodWithParameters.toString();
    }

    private List<ColumnConfig> defineAttachmentColumns()
    {
        final ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(createFileNameColumnConfig(messageProvider));
        columns.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
        columns.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
        columns.add(createOlderVersionsColumnConfig(messageProvider));
        return columns;
    }

    static private ColumnConfig createVersionFileName(final IMessageProvider messageProvider)
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage(Dict.VERSION_FILE_NAME),
                        ModelDataPropertyNames.VERSION_FILE_NAME);
        column.setWidth(200);
        column.setRenderer(new GridCellRenderer<AttachmentVersionModel>()
            {

                public String render(final AttachmentVersionModel model, final String property,
                        final ColumnData config, final int rowIndex, final int colIndex,
                        final ListStore<AttachmentVersionModel> store)
                {
                    Object value = model.get(property);
                    if (value == null)
                    {
                        return "";
                    }
                    return LinkRenderer.renderAsLink((String) value);
                }
            });
        return column;

    }

    static private ColumnConfig createFileNameColumnConfig(final IMessageProvider messageProvider)
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage(Dict.FILE_NAME), ModelDataPropertyNames.FILE_NAME);
        column.setRenderer(new GridCellRenderer<AttachmentModel>()
            {

                public String render(final AttachmentModel model, final String property,
                        final ColumnData config, final int rowIndex, final int colIndex,
                        final ListStore<AttachmentModel> store)
                {
                    Object value = model.get(property);
                    if (value == null)
                    {
                        return "";
                    }
                    return LinkRenderer.renderAsLink((String) value);
                }
            });
        return column;
    }

    static private ColumnConfig createOlderVersionsColumnConfig(
            final IMessageProvider messageProvider)
    {
        final ColumnConfig column =
                ColumnConfigFactory.createDefaultColumnConfig(messageProvider
                        .getMessage(Dict.VERSIONS), ModelDataPropertyNames.VERSIONS);
        column.setWidth(180);

        column.setRenderer(new GridCellRenderer<AttachmentModel>()
            {

                @SuppressWarnings("unchecked")
                public String render(final AttachmentModel model, final String property,
                        final ColumnData config, final int rowIndex, final int colIndex,
                        final ListStore<AttachmentModel> store)
                {
                    Object value = model.get(property);
                    if (value == null)
                    {
                        return "";
                    }
                    List<Attachment> versions = (List<Attachment>) value;
                    if (versions.size() == 0)
                    {
                        return "-";
                    } else
                    {
                        final String message =
                                messageProvider.getMessage(Dict.VERSIONS_TEMPLATE, versions.size());
                        return LinkRenderer.renderAsLink(message);
                    }
                }
            });
        return column;
    }
}