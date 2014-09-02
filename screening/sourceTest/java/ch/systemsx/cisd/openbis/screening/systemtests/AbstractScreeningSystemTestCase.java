/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.image.ImageHistogram;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.server.Utils;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.server.IAnalysisSettingSetter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;

/**
 * System test case for screening. Starts both AS and DSS.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScreeningSystemTestCase extends SystemTestCase
{
    private static final class FailureReport
    {
        private File referenceImage;
        private StringBuilder failureReport = new StringBuilder();

        FailureReport(File referenceImage)
        {
            this.referenceImage = referenceImage;
        }
        
        boolean isFailure()
        {
            return failureReport.length() > 0;
        }
        
        private void addFailureMessage(String failureMessage)
        {
            failureReport.append(failureMessage).append('\n');
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("/===== Failure Report for image ").append(referenceImage).append('\n');
            builder.append(failureReport).append("\\============================\n");
            return builder.toString();
        }
        
    }
    
    /**
     * Helper class to load an image from the image download servlet.
     *
     * @author Franz-Josef Elmer
     */
    protected static final class ImageLoader
    {
        private final URLMethodWithParameters url;
        private final List<String> channels = new ArrayList<String>();
        private boolean mergeChannels = true;
        private int wellRow = 1;
        private int wellColumn = 1;
        private int tileRow = 1;
        private int tileColumn = 1;
        private String mode = "thumbnail160x160";

        public ImageLoader(AbstractExternalData dataSet, String sessionToken)
        {
            url = new URLMethodWithParameters(dataSet.getDataStore().getHostUrl() 
                    + "/" + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
            url.addParameter(Utils.SESSION_ID_PARAM, sessionToken);
            url.addParameter(ImageServletUrlParameters.DATASET_CODE_PARAM, dataSet.getCode());
        }
        
        public BufferedImage load()
        {
            try
            {
                return ImageIO.read(createURL());
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        
        private URL createURL()
        {
            if (mergeChannels)
            {
                url.addParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM, "true");
            } else
            {
                for (String channel : channels)
                {
                    url.addParameter(ImageServletUrlParameters.CHANNEL_PARAM, channel);
                }
            }
            url.addParameter(ImageServletUrlParameters.WELL_ROW_PARAM, Integer.toString(wellRow));
            url.addParameter(ImageServletUrlParameters.WELL_COLUMN_PARAM, Integer.toString(wellColumn));
            url.addParameter(ImageServletUrlParameters.TILE_ROW_PARAM, Integer.toString(tileRow));
            url.addParameter(ImageServletUrlParameters.TILE_COL_PARAM, Integer.toString(tileColumn));
            url.addParameter("mode", mode);
            try
            {
                return new URL(url.toString());
            } catch (MalformedURLException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        
        public ImageLoader wellRow(int newWellRow)
        {
            wellRow = newWellRow;
            return this;
        }
        
        public ImageLoader wellColumn(int newWellColumn)
        {
            wellColumn = newWellColumn;
            return this;
        }
        
        public ImageLoader tileRow(int newTileRow)
        {
            tileRow = newTileRow;
            return this;
        }
        
        public ImageLoader tileColumn(int newTileColumn)
        {
            tileColumn = newTileColumn;
            return this;
        }
        
        public ImageLoader mode(String newMode)
        {
            mode = newMode;
            return this;
        }
        
        public ImageLoader channel(String channel)
        {
            mergeChannels = false;
            channels.add(channel);
            return this;
        }
    }
    
    /**
     * Helper class to load and check a reference image (from a file) and and actual image (from image download servlet).
     * Several image pairs can be tested. An extensive human readable failure report will be created. 
     *
     * @author Franz-Josef Elmer
     */
    protected static final class ImageChecker
    {
        private final StringBuilder failureReport = new StringBuilder();
        
        /**
         * Asserts no failures occurred for all invocations of {@link #check(File, ImageLoader)}.
         * Otherwise an {@link AssertionError} is thrown.
         */
        public void assertNoFailures()
        {
            AssertJUnit.assertEquals("", failureReport.toString());
        }
        
        /**
         * Checks that the specified reference image is equals to the image loaded by the specified image loader.
         * A report is created in case of a failure.
         */
        public void check(File referenceImage, ImageLoader imageLoader)
        {
            FailureReport report = new FailureReport(referenceImage);
            try
            {
                BufferedImage expectedImage = ImageIO.read(referenceImage);
                BufferedImage actualImage = imageLoader.load();
                checkEquals(report, expectedImage, actualImage);
            } catch (IOException ex)
            {
                report.addFailureMessage("Couldn't load image: " + ex);
            } finally
            {
                if (report.isFailure())
                {
                    failureReport.append(report);
                }
            }
        }
        
        private void checkEquals(FailureReport report, BufferedImage expectedImage, BufferedImage actualImage)
        {
            if (expectedImage == null || actualImage == null)
            {
                report.addFailureMessage("Expected and/or actual image is undefined: Expected image: <" + expectedImage 
                        + ">, actual image: <" + actualImage + ">");
                return;
            }
            checkEquals(report, "Image height", expectedImage.getHeight(), actualImage.getHeight());
            checkEquals(report, "Image width", expectedImage.getWidth(), actualImage.getWidth());
            checkEquals(report, "Type", expectedImage.getType(), actualImage.getType());
            ColorModel expectedColorModel = expectedImage.getColorModel();
            ColorModel actualColorModel = actualImage.getColorModel();
            checkEquals(report, "Pixel size", expectedColorModel.getPixelSize(), actualColorModel.getPixelSize());
            ColorSpace expectedColorSpace = expectedColorModel.getColorSpace();
            ColorSpace actualColorSpace = actualColorModel.getColorSpace();
            checkEquals(report, "Color space type", expectedColorSpace.getType(), actualColorSpace.getType());
            ImageHistogram expectedHistogram = ImageHistogram.calculateHistogram(expectedImage);
            ImageHistogram actualHistogram = ImageHistogram.calculateHistogram(actualImage);
            String expectedRenderedHistogram = expectedHistogram.renderAsASCIIChart(49, 9);
            String actualRenderedHistogram = actualHistogram.renderAsASCIIChart(49, 9);
            if (expectedRenderedHistogram.equals(actualRenderedHistogram))
            {
                return;
            }
            // difference can be caused by a value close to discretization border. Thus try another height.
            expectedRenderedHistogram = expectedHistogram.renderAsASCIIChart(49, 10);
            actualRenderedHistogram = actualHistogram.renderAsASCIIChart(49, 10);
            if (expectedRenderedHistogram.equals(actualRenderedHistogram))
            {
                return;
            }
            report.addFailureMessage("expected histogram of " + expectedImage + ":\n" + expectedRenderedHistogram 
                    + "actual histogram of " + actualImage + ":\n" + actualRenderedHistogram);
        }
        
        private void checkEquals(FailureReport report, String message, Object expected, Object actual)
        {
            if (expected == null ? expected == actual : expected.equals(actual))
            {
                return;
            }
            report.addFailureMessage(message + ":\n  Expected: <" + expected + ">\n    Actual: <" + actual + ">");
        }
    }
    
    protected IScreeningClientService screeningClientService;
    protected IScreeningServer screeningServer;
    protected IScreeningApiServer screeningApiServer;
    protected IAnalysisSettingSetter analysisSettingServer;
    protected String sessionToken;
    protected IScreeningOpenbisServiceFacade screeningFacade;
    protected ICommonServer commonServer;
    protected IGenericServer genericServer;

    @BeforeMethod
    public void setUpServices()
    {
        commonServer = (ICommonServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        genericServer = (IGenericServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER);
        screeningClientService =
                (IScreeningClientService) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVICE);
        ((SpringRequestContextProvider) applicationContext.getBean("request-context-provider"))
                .setRequest(new MockHttpServletRequest());
        Object bean = applicationContext.getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningServer) bean;
        screeningApiServer = (IScreeningApiServer) bean;
        analysisSettingServer = (IAnalysisSettingSetter) bean;
        sessionToken = screeningClientService.tryToLogin("admin", "a").getSessionID();
        screeningFacade =
                ScreeningOpenbisServiceFacade.tryCreateForTest(sessionToken,
                        TestInstanceHostUtils.getOpenBISUrl(), screeningApiServer);
    }

    /**
     * Return the location of the openBIS application context config.
     */
    @Override
    protected String getApplicationContextLocation()
    {
        return "classpath:screening-applicationContext.xml";
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    @Override
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initEmptyDbWithIndex();
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-" + getClass().getSimpleName());
    }
    
}
