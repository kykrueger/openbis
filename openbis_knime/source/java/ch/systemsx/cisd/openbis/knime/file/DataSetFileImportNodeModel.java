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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.activation.MimetypesFileTypeMap;

import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
import org.knime.core.data.uri.URIPortObjectSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.knime.common.Util;

/**
 * Node model for importing a file/folder from Data Store Server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetFileImportNodeModel extends AbstractOpenBisNodeModel
{
    static final String DATA_SET_CODE_KEY = "data-set-code";

    static final String FILE_PATH_KEY = "file-path";

    static final String DOWNLOADS_PATH_KEY = "downloads-path";

    static final String ABSOLUTE_FILE_PATH_KEY = "absolute-file-path";

    static final String REUSE_FILE = "reuse-file";

    private final IOpenbisServiceFacadeFactory serviceFacadeFactory;

    private String dataSetCode = "";

    private String filePath = "";

    private String dowloadsPath = "";

    private boolean reuseFile;

    public DataSetFileImportNodeModel(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super(new PortType[] {}, new PortType[]
        { new PortType(URIPortObject.class) });
        this.serviceFacadeFactory = serviceFacadeFactory;
    }

    @Override
    protected void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException
    {
        dataSetCode = settings.getString(DATA_SET_CODE_KEY);
        filePath = settings.getString(FILE_PATH_KEY);
        dowloadsPath = settings.getString(DOWNLOADS_PATH_KEY);
        reuseFile = settings.getBoolean(REUSE_FILE, false);
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
    {
        settings.addString(DATA_SET_CODE_KEY, dataSetCode);
        settings.addString(FILE_PATH_KEY, filePath);
        settings.addString(DOWNLOADS_PATH_KEY, dowloadsPath);
        settings.addString(ABSOLUTE_FILE_PATH_KEY, dowloadsPath + "/" + dataSetCode + "/" + filePath);
        settings.addBoolean(REUSE_FILE, reuseFile);
    }

    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException
    {
        String type = createType();
        return new PortObjectSpec[]
        { new URIPortObjectSpec(type) };
    }

    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception
    {
        File file = new File(dowloadsPath, dataSetCode + "/" + filePath);
        if (reuseFile == false || file.exists() == false)
        {
            downloadTo(file);
        }
        String type = createType();
        logger.info("Content MIME type: " + type);
        return new PortObject[]
        { new URIPortObject(new URIPortObjectSpec(type), Arrays.asList(new URIContent(file.toURI(), type))) };
    }

    private void downloadTo(File file) throws Exception
    {
        InputStream in = null;
        OutputStream out = null;
        IOpenbisServiceFacade facade = null;
        try
        {
            facade = serviceFacadeFactory.createFacade(url, userID, password);
            DataSet dataSet = facade.getDataSet(dataSetCode);
            if (dataSet == null)
            {
                throw new IllegalArgumentException("Unknown data set '" + dataSetCode + "'.");
            }
            pushDataSetMetaDataToVariables(dataSet);
            in = dataSet.getFile(filePath);
            file.getParentFile().mkdirs();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, size);
            }
        } finally
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }
            if (facade != null)
            {
                facade.logout();
            }
        }
    }

    private void pushDataSetMetaDataToVariables(DataSet dataSet)
    {
        addFlowVariable(Util.VARIABLE_PREFIX + DataSetOwnerType.DATA_SET.name(),
                dataSet.getCode());
        addFlowVariable(Util.VARIABLE_PREFIX + DataSetOwnerType.EXPERIMENT.name(),
                dataSet.getExperimentIdentifier());
        String sampleIdentifierOrNull = dataSet.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
            addFlowVariable(Util.VARIABLE_PREFIX + DataSetOwnerType.SAMPLE.name(),
                    sampleIdentifierOrNull);
        }
    }

    private String createType()
    {
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
    }

}
