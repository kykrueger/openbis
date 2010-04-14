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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

/**
 * Represents the configuration data for a graph generated from tabular data.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphConfiguration
{
    private final String title;

    // the column that is used as the x-axis
    private final String xAxisColumn;

    // the column that is used as the y-axis
    private final String yAxisColumn;

    private final int imageWidth;

    private final int imageHeight;

    /**
     * Constructor for a configuration.
     * 
     * @param title The title for the graph
     * @param xAxisColumn The data column used for the x-axis
     * @param yAxisColumn The data column used for the x-axis
     * @param imageWidth The desired width of the resulting image
     * @param imageHeight The desired height of the resulting image
     */
    protected TabularDataGraphConfiguration(String title, String xAxisColumn,
            String yAxisColumn, int imageWidth, int imageHeight)
    {
        this.title = title;
        this.xAxisColumn = xAxisColumn;
        this.yAxisColumn = yAxisColumn;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    /**
     * The title for the resulting image.
     */
    protected String getTitle()
    {
        return title;
    }

    /**
     * The name of the column from which the x values come.
     */
    protected String getXAxisColumn()
    {
        return xAxisColumn;
    }

    /**
     * The name of the column from which the y values come.
     */
    protected String getYAxisColumn()
    {
        return yAxisColumn;
    }

    /**
     * The width of the resulting image.
     */
    protected int getImageWidth()
    {
        return imageWidth;
    }

    /**
     * The height of the resulting image.
     */
    protected int getImageHeight()
    {
        return imageHeight;
    }
}
