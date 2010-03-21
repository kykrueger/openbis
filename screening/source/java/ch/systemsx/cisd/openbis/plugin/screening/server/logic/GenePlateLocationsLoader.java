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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class GenePlateLocationsLoader
{
    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, ExperimentIdentifier experimentIdentifier)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId, experimentIdentifier);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private final IExternalDataTable externalDataTable;

    private GenePlateLocationsLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
        this.externalDataTable = businessObjectFactory.createExternalDataTable(session);

    }

    private List<WellContent> getPlateLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        List<WellContent> locations = loadLocations(geneMaterialId, experimentIdentifier);
        List<ExternalDataPE> imageDatasets = loadImageDatasets(locations, externalDataTable);
        Map<String, PlateImageParameters> imageParams = loadImagesReport(imageDatasets);
        return enrichWithImages(locations, imageDatasets, imageParams);
    }

    private static List<WellContent> enrichWithImages(List<WellContent> wellContents,
            List<ExternalDataPE> imageDatasets, Map<String, PlateImageParameters> imageParams)
    {
        Map<Long/* plate id */, List<ExternalDataPE>> plateToDatasetMap =
                createPlateToDatasetMap(imageDatasets);
        List<WellContent> wellsWithImages = new ArrayList<WellContent>();
        for (WellContent wellContent : wellContents)
        {
            List<ExternalDataPE> datasets = plateToDatasetMap.get(wellContent.getPlate().getId());
            boolean imagesExist = false;
            // there can be more than one dataset with images for each well - in such a case we will
            // have one well content duplicate for each dataset
            if (datasets != null)
            {
                for (ExternalDataPE dataset : datasets)
                {
                    PlateImageParameters imageParameters = imageParams.get(dataset.getCode());
                    if (imageParameters != null)
                    {
                        DatasetReference datasetReference =
                                ScreeningUtils.createDatasetReference(dataset);
                        DatasetImagesReference wellImages =
                                DatasetImagesReference.create(datasetReference, imageParameters);
                        WellContent wellWithImages = wellContent.cloneWithImages(wellImages);
                        wellsWithImages.add(wellWithImages);
                        imagesExist = true;
                    }
                }
            }
            // if there are no datasets for the well content, we add it without images
            if (imagesExist == false)
            {
                wellsWithImages.add(wellContent);
            }
        }
        return wellsWithImages;
    }

    private static Map<Long/* sample id */, List<ExternalDataPE>> createPlateToDatasetMap(
            List<ExternalDataPE> datasets)
    {
        Map<Long, List<ExternalDataPE>> map = new HashMap<Long, List<ExternalDataPE>>();
        for (ExternalDataPE dataset : datasets)
        {
            Long sampleId = dataset.tryGetSample().getId();
            List<ExternalDataPE> plateDatasets = map.get(sampleId);
            if (plateDatasets == null)
            {
                plateDatasets = new ArrayList<ExternalDataPE>();
                map.put(sampleId, plateDatasets);
            }
            plateDatasets.add(dataset);
        }
        return map;
    }

    private Map<String/* dataset code */, PlateImageParameters> loadImagesReport(
            List<ExternalDataPE> imageDatasets)
    {
        if (imageDatasets.size() == 0)
        {
            return new HashMap<String, PlateImageParameters>();
        }
        List<String> datasetCodes = asCodes(imageDatasets);
        // NOTE: assumes that all datasets are from the same datastore
        String datastoreCode = imageDatasets.get(0).getDataStore().getCode();
        List<PlateImageParameters> imageParameters =
                DatasetLoader.loadPlateImageParameters(datasetCodes, datastoreCode, externalDataTable);
        return asDatasetToParamsMap(imageParameters);
    }

    private static Map<String/* dataset code */, PlateImageParameters> asDatasetToParamsMap(
            List<PlateImageParameters> imageParameters)
    {
        Map<String, PlateImageParameters> map = new HashMap<String, PlateImageParameters>();
        for (PlateImageParameters params : imageParameters)
        {
            map.put(params.getDatasetCode(), params);
        }
        return map;
    }

    private static List<String> asCodes(List<ExternalDataPE> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (ExternalDataPE dataset : datasets)
        {
            codes.add(dataset.getCode());
        }
        return codes;
    }

    private static List<ExternalDataPE> loadImageDatasets(List<WellContent> locations,
            IExternalDataTable externalDataTable)
    {
        List<ExternalDataPE> imageDatasets = new ArrayList<ExternalDataPE>();
        List<TechId> plateIds = extractPlateIds(locations);
        for (TechId plateId : plateIds)
        {
            List<ExternalDataPE> datasets =
                    PlateContentLoader.loadDatasets(plateId, externalDataTable);
            imageDatasets.addAll(filterImageDatasets(datasets));
        }
        return imageDatasets;
    }

    private static List<ExternalDataPE> filterImageDatasets(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            if (dataset.getDataSetType().getCode().equalsIgnoreCase(
                    ScreeningConstants.IMAGE_DATASET_TYPE))
            {
                result.add(dataset);
            }
        }
        return result;
    }

    private static List<TechId> extractPlateIds(List<WellContent> locations)
    {
        Set<Long> ids = new HashSet<Long>();
        for (WellContent loc : locations)
        {
            ids.add(loc.getPlate().getId());
        }
        return asTechIdList(ids);
    }

    private static List<TechId> asTechIdList(Set<Long> ids)
    {
        List<TechId> result = new ArrayList<TechId>();
        Iterator<Long> iter = ids.iterator();
        while (iter.hasNext())
        {
            result.add(new TechId(iter.next()));
        }
        return result;
    }

    private List<WellContent> loadLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        long experimentId = loadExperimentId(experimentIdentifier);

        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocations(geneMaterialId.getId(), experimentId);

        return convert(locations);
    }

    private List<WellContent> convert(
            DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations)
    {
        List<WellContent> wellLocations = new ArrayList<WellContent>();
        for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent location : locations)
        {
            wellLocations.add(convert(location));
        }
        return wellLocations;
    }

    private static WellContent convert(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        WellLocation location = ScreeningUtils.tryCreateLocationFromMatrixCoordinate(loc.well_code);
        EntityReference well =
                new EntityReference(loc.well_id, loc.well_code, loc.well_type_code,
                        EntityKind.SAMPLE);
        EntityReference plate =
                new EntityReference(loc.plate_id, loc.plate_code, loc.plate_type_code,
                        EntityKind.SAMPLE);
        EntityReference materialContent =
                new EntityReference(loc.material_content_id, loc.material_content_code,
                        loc.material_content_type_code, EntityKind.MATERIAL);
        return new WellContent(location, well, plate, materialContent);
    }

    private static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    private long loadExperimentId(ExperimentIdentifier experimentIdentifier)
    {
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(experimentIdentifier);
        return experimentBO.getExperiment().getId();
    }
}
