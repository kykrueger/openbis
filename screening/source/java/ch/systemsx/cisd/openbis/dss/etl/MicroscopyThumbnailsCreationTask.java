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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageContainerDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonIngestionService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * @author Franz-Josef Elmer
 */
public class MicroscopyThumbnailsCreationTask extends AbstractMaintenanceTaskWithStateFile
{

    private String dataSetContainerType;

    private String dataSetThumbnailType;

    private Properties properties;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        this.properties = properties;
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
        fetchOptions.withComponents().withExperiment();
        fetchOptions.withComponents().withSample();
        fetchOptions.sortBy().registrationDate();
        List<DataSet> containerDataSets = getService().searchDataSets(sessionToken, searchCriteria, fetchOptions).getObjects();
        for (DataSet containerDataSet : containerDataSets)
        {
            if (hasNoThumbnails(containerDataSet) && containerDataSet.getComponents().isEmpty() == false)
            {
                cerateThumbnailDataSet(sessionToken, containerDataSet);
            }
            updateTimeStampFile(renderTimeStampAndCode(containerDataSet.getRegistrationDate(), containerDataSet.getCode()));
        }
    }

    private void cerateThumbnailDataSet(String sessionToken, DataSet containerDataSet)
    {
        String containerCode = containerDataSet.getCode();
        IImagingReadonlyQueryDAO imageDb = getImageDb();
        ImageDataSetStructure imageDataSetStructure = Utils.getImageDataSetStructure(imageDb, containerCode);
        SimpleImageContainerDataConfig config = new SimpleImageContainerDataConfig();

        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = createScriptRunner(imageDataSetStructure, config);
        Properties ingestionServiceProperties = createIngestionServiceProperties();
        File storeRoot = getDirectoryProvider().getStoreRoot();
        ScreeningJythonIngestionService ingestionService =
                new ScreeningJythonIngestionService(ingestionServiceProperties, storeRoot, scriptRunnerFactory)
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public TableModel process(IDataSetRegistrationTransactionV2 transaction,
                                Map<String, Object> parameters, DataSetProcessingContext context)
                        {
                            // Populate object 'config' by the jython script based on 'imageDataSetStructure'
                            super.process(transaction, parameters, context);
                            composeThumbnailDataSet(transaction, containerDataSet, imageDataSetStructure, config, context);
                            return null;
                        }
                    };
        IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider();
        DataSetProcessingContext context =
                new DataSetProcessingContext(contentProvider, null, null, null, null, null, sessionToken);
        ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
    }

    private void composeThumbnailDataSet(IDataSetRegistrationTransactionV2 transaction, DataSet containerDataSet,
            ImageDataSetStructure imageDataSetStructure, SimpleImageDataConfig config, DataSetProcessingContext context)
    {
        String containerCode = containerDataSet.getCode();
        IDataSetUpdatable container = transaction.getDataSetForUpdate(containerCode);
        DataSet mainDataSet = containerDataSet.getComponents().get(0);
        IImageGenerationAlgorithm imageGenerationAlgorithm = config.getImageGenerationAlgorithm();
        if (imageGenerationAlgorithm != null)
        {
            List<IDataSet> thumbnailDatasets = new ArrayList<>();
            IImageProvider imageProvider = new ImageCache();
            IHierarchicalContent content = context.getHierarchicalContentProviderUnfiltered().asContent(containerCode);
            imageGenerationAlgorithm.setContent(content);
            ImageDataSetInformation imageDataSetInformation = new ImageDataSetInformation();
            imageDataSetInformation.setImageDataSetStructure(imageDataSetStructure);
            List<BufferedImage> images = imageGenerationAlgorithm.generateImages(imageDataSetInformation,
                    thumbnailDatasets, imageProvider);
            IDataSet dataSet = Utils.createDataSetAndImageFiles(transaction, imageGenerationAlgorithm, images);
            List<String> components = new ArrayList<>(container.getContainedDataSetCodes());
            components.add(dataSet.getDataSetCode());
            container.setContainedDataSetCodes(components);
            ISearchService searchService = transaction.getSearchService();
            dataSet.setExperiment(searchService.getExperimentByPermId(mainDataSet.getExperiment().getPermId().getPermId()));
            dataSet.setSample(searchService.getSampleByPermId(mainDataSet.getSample().getPermId().getPermId()));
        }
    }

    private Properties createIngestionServiceProperties()
    {
        Properties ingestionServiceProperties = new Properties(properties);
        ingestionServiceProperties.setProperty(DefaultStorageProcessor.DO_NOT_CREATE_ORIGINAL_DIR_KEY, "true");
        return ingestionServiceProperties;
    }

    private ScreeningPluginScriptRunnerFactory createScriptRunner(ImageDataSetStructure imageDataSetStructure, SimpleImageContainerDataConfig config)
    {
        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = new ScreeningPluginScriptRunnerFactory(
                JythonBasedProcessingPlugin.getScriptPathProperty(properties))
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected IJythonEvaluator createEvaluator(String scriptString, String[] jythonPath, DataSetProcessingContext context)
                {
                    IJythonEvaluator evaluator = super.createEvaluator(scriptString, jythonPath, context);
                    evaluator.set("image_data_set_structure", imageDataSetStructure);
                    evaluator.set("image_config", config);
                    return evaluator;
                }
            };
        return scriptRunnerFactory;
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
