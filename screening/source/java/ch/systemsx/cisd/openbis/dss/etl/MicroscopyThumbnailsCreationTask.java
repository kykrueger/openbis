/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAcquiredImageDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;

/**
 * @author Franz-Josef Elmer
 */
public class MicroscopyThumbnailsCreationTask extends AbstractMaintenanceTaskWithStateFile
{

    private String dataSetContainerType;

    private String dataSetThumbnailType;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        defineStateFile(properties, getDirectoryProvider().getStoreRoot());
        dataSetContainerType = properties.getProperty("data-set-container-type", "MICROSCOPY_IMG_CONTAINER");
        dataSetThumbnailType = properties.getProperty("data-set-thumbnail-type", "MICROSCOPY_IMG_THUMBNAIL");
    }

    @Override
    public void execute()
    {
        String sessionToken = login();
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(dataSetContainerType);
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(getLastRegistrationDate(new Date(0)));
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withComponents().withType();
        fetchOptions.sortBy().registrationDate();
        List<DataSet> containerDataSets = getService().searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        for (DataSet containerDataSet : containerDataSets)
        {
            if (hasNoThumbnails(containerDataSet))
            {
                String containerCode = containerDataSet.getCode();
                System.err.println("has no thumbnails: " + containerCode);
                IImagingReadonlyQueryDAO imageDb = getImageDb();
                ImageDataSetStructure imageDataSetStructure = Utils.getImageDataSetStructure(imageDb, containerCode);
                ImageDataSetInformation imageDataSetInformation = new ImageDataSetInformation();
                imageDataSetInformation.setImageDataSetStructure(imageDataSetStructure);
                
                System.err.println(imageDataSetStructure);
                IHierarchicalContent content = getHierarchicalContentProvider().asContent(containerCode);
                IHierarchicalContentNode rootNode = content.getRootNode();
                printFiles(rootNode, "");
            }
//            updateTimeStampFile(renderTimeStampAndCode(containerDataSet.getRegistrationDate(), containerDataSet.getCode()));
        }
        
    }
    
    private void printFiles(IHierarchicalContentNode node, String indentation)
    {
        System.out.println(indentation+node.getName());
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            for (IHierarchicalContentNode childNode : childNodes)
            {
                printFiles(childNode, indentation + "  ");
            }
        }
    }
    
    private boolean hasNoThumbnails(DataSet containerDataSet)
    {
        List<DataSet> components = containerDataSet.getComponents();
        for (DataSet component : components)
        {
            if (dataSetThumbnailType.equals(component.getType().getCode()))
            {
                return false;
            }
        }
        return true;
    }

    private String login()
    {
        ICredentials credentials = getEtlServerCredentials();
        return getService().login(credentials.getUserId(), credentials.getPassword());
    }

    protected IApplicationServerApi getService()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    protected IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }
    
    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }
    
    protected IImagingReadonlyQueryDAO getImageDb()
    {
        return DssScreeningUtils.getQuery();
    }

}
