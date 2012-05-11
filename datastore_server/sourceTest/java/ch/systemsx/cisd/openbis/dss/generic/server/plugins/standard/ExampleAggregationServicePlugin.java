/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ExampleAggregationServicePlugin extends AbstractAggregationServiceReportingPlugin
{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new plugin.
     * 
     * @param properties
     * @param storeRoot
     */
    public ExampleAggregationServicePlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("String");
        builder.addHeader("Integer");

        IRowBuilder row = builder.addRow();
        row.setCell("String", "Hello");
        row.setCell("Integer", 20);

        row = builder.addRow();
        row.setCell("String", "World");
        row.setCell("Integer", 30);

        return builder.getTableModel();
    }

}
