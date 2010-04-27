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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleImageHtmlRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * A dialog which shows the content of the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog extends Dialog
{
    public static void showContentDialog(final WellData wellData, DefaultChannelState channelState,
            final ScreeningViewContext viewContext)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        final WellContentDialog contentDialog =
                new WellContentDialog(wellData.tryGetMetadata(), wellData.getExperiment(),
                        viewContext);
        LayoutContainer descriptionContainer = contentDialog.createContentDescription();
        container.add(descriptionContainer);

        int dialogWidth;
        int dialogHeight;

        final WellImages images = wellData.tryGetImages();
        if (images != null)
        {
            int imgW = 200;
            int imgH = 120;
            LayoutContainer imageViewer =
                    createImageViewer(images, channelState, viewContext, imgW, imgH);
            container.add(imageViewer);

            dialogWidth = imgW * Math.max(2, images.getTileColsNum());
            dialogHeight = imgH * images.getTileRowsNum() + 200;
        } else
        {
            dialogWidth = 250;
            dialogHeight = 160;
        }
        String title = "Well Content: " + wellData.getWellContentDescription();
        contentDialog.setupContentAndShow(container, dialogWidth, dialogHeight, title);
    }

    // ----------------

    private final WellMetadata metadataOrNull;

    private final ExperimentIdentifier experimentIdentifier;

    private final ScreeningViewContext viewContext;

    private WellContentDialog(WellMetadata metadataOrNull,
            ExperimentIdentifier experimentIdentifier, ScreeningViewContext viewContext)
    {
        this.metadataOrNull = metadataOrNull;
        this.experimentIdentifier = experimentIdentifier;
        this.viewContext = viewContext;
    }

    private void setupContentAndShow(LayoutContainer container, int width, int height, String title)
    {
        setHeading(title);
        setLayout(new FitLayout());
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        add(container);
        setWidth(width);
        setHeight(height);
        show();
    }

    private LayoutContainer createContentDescription()
    {
        LayoutContainer container = new LayoutContainer();
        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setCellPadding(2);
        container.setLayout(tableLayout);
        TableData cellLayout = new TableData();
        cellLayout.setMargin(2);
        if (metadataOrNull != null)
        {
            container.add(new Text("Well: "), cellLayout);
            container.add(createEntityLink(metadataOrNull.getWellSample()));

            Material content = metadataOrNull.tryGetContent();
            if (content != null)
            {
                Material gene = metadataOrNull.tryGetGene();
                if (gene != null)
                {
                    container.add(new Text("Inhibited gene: "), cellLayout);
                    container.add(createGeneViewerLink(gene));

                    container.add(new Text("Gene details: "), cellLayout);
                    container.add(createEntityExternalLink(gene));
                }

                container.add(new Text("Content: "), cellLayout);
                container.add(createEntityLink(content));
            }
        } else
        {
            container.add(new Text("No metadata available."));
        }
        return container;
    }

    private Widget createEntityExternalLink(Material gene)
    {
        String url = viewContext.getMessage(Dict.GENE_LIBRARY_URL, gene.getCode());
        return new Html(LinkRenderer.renderAsLinkWithAnchor("gene database", url, true));
    }

    private Widget createGeneViewerLink(final IEntityInformationHolder gene)
    {
        return LinkRenderer.getLinkWidget(gene.getCode(), new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    WellContentDialog.this.hide();
                    ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory
                            .openGeneMaterialViewer(gene, experimentIdentifier, viewContext);
                }
            });
    }

    private Widget createEntityLink(IEntityInformationHolder entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

    // -------------

    private static LayoutContainer createImageViewer(final WellImages images,
            DefaultChannelState channelState, final IViewContext<?> viewContext,
            final int imageWidth, final int imageHeight)
    {
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(int channel)
                {
                    return createTilesGrid(images, channel, viewContext, imageWidth, imageHeight);
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState, images
                .getChannelsNum());
    }

    /** @param channel Channel numbers start with 1. Channel 0 consists of all other channels merged. */
    public static LayoutContainer createTilesGrid(WellImages images, int channel,
            IViewContext<?> viewContext, int imageWidth, int imageHeight)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(images.getTileColsNum()));
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                Component tileContent;
                String sessionId = getSessionId(viewContext);
                String imageURL =
                        createDatastoreImageUrl(images, channel, row, col, imageWidth, imageHeight,
                                sessionId);
                tileContent = new Html(imageURL);
                tileContent.setHeight("" + imageHeight);
                PlateStyleSetter.setPointerCursor(tileContent);
                container.add(tileContent);
            }
        }
        return container;
    }

    /** generates URL of an image on Data Store server */
    private static String createDatastoreImageUrl(WellImages images, int channel, int tileRow,
            int tileCol, int width, int height, String sessionID)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(images.getDownloadUrl() + "/"
                        + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
        methodWithParameters.addParameter("sessionID", sessionID);
        methodWithParameters.addParameter("dataset", images.getDatasetCode());
        if (channel == 0)
        {
            methodWithParameters.addParameter("channel", images.getChannelsNum());
            methodWithParameters.addParameter("mergeChannels", "true");
        } else
        {
            methodWithParameters.addParameter("channel", channel);
        }
        methodWithParameters.addParameter("wellRow", images.getWellLocation().getRow());
        methodWithParameters.addParameter("wellCol", images.getWellLocation().getColumn());
        methodWithParameters.addParameter("tileRow", tileRow);
        methodWithParameters.addParameter("tileCol", tileCol);
        String linkURL = methodWithParameters.toString();
        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);

        String imageURL = methodWithParameters.toString();
        return SimpleImageHtmlRenderer.createEmbededImageHtml(imageURL, linkURL);
    }

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }
}
