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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContentWithExperiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class GenePlateLocationsLoader
{
    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, ExperimentIdentifier experimentIdentifier,
            boolean enrichWithImages)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId, experimentIdentifier, enrichWithImages);
    }

    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, String experimentPermId, boolean enrichWithImages)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId, experimentPermId, enrichWithImages);
    }

    public static List<WellContentWithExperiment> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId);
    }

    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            PlateMaterialsSearchCriteria materialCriteria, boolean enrichWithImages)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(materialCriteria, enrichWithImages);
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

    private List<WellContent> getPlateLocations(PlateMaterialsSearchCriteria materialCriteria,
            boolean enrichWithImages)
    {
        List<WellContent> locations = loadLocations(materialCriteria);
        if (enrichWithImages)
        {
            externalDataTable.loadByExperimentTechId(materialCriteria.getExperimentId());
            List<ExternalDataPE> datasets = externalDataTable.getExternalData();
            List<ExternalDataPE> imageDatasets = filterImageDatasets(datasets);
            return enrichWithImages(locations, imageDatasets);
        } else
        {
            return locations;
        }
    }

    private List<WellContent> getPlateLocations(TechId geneMaterialId, String experimentPermId,
            boolean enrichWithImages)
    {
        List<WellContent> locations = loadLocations(geneMaterialId, experimentPermId);
        if (enrichWithImages)
        {
            List<ExternalDataPE> imageDatasets = loadImageDatasets(locations, externalDataTable);
            return enrichWithImages(locations, imageDatasets);
        } else
        {
            return locations;
        }
    }

    private List<WellContentWithExperiment> getPlateLocations(TechId geneMaterialId)
    {
        List<WellContentWithExperiment> locations = loadLocations(geneMaterialId);
        return locations;
    }

    private List<WellContent> getPlateLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier, boolean enrichWithImages)
    {
        List<WellContent> locations = loadLocations(geneMaterialId, experimentIdentifier);
        if (enrichWithImages)
        {
            List<ExternalDataPE> imageDatasets = loadImageDatasets(locations, externalDataTable);
            return enrichWithImages(locations, imageDatasets);
        } else
        {
            return locations;
        }
    }

    private List<WellContent> enrichWithImages(List<WellContent> locations,
            List<ExternalDataPE> imageDatasets)
    {
        Map<Long/* plate id */, List<ExternalDataPE>> plateToDatasetMap =
                createPlateToDatasetMap(imageDatasets);
        List<ExternalDataPE> usedDatasets = extractUsedDatasets(locations, plateToDatasetMap);
        Map<String, PlateImageParameters> imageParams = loadImagesReport(usedDatasets);
        return enrichWithImages(locations, plateToDatasetMap, imageParams);
    }

    private static List<WellContent> enrichWithImages(List<WellContent> wellContents,
            Map<Long/* plate id */, List<ExternalDataPE>> plateToDatasetMap,
            Map<String, PlateImageParameters> imageParams)
    {
        List<WellContent> wellsWithImages = new ArrayList<WellContent>();
        for (WellContent wellContent : wellContents)
        {
            List<ExternalDataPE> datasets = plateToDatasetMap.get(wellContent.getPlate().getId());
            boolean imagesExist = false;
            // there can be more than one dataset with images for each well - in such a case we will
            // have one well content duplicated for each dataset
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

    private static List<ExternalDataPE> extractUsedDatasets(List<WellContent> wellContents,
            Map<Long/* plate id */, List<ExternalDataPE>> plateToDatasetMap)
    {
        List<ExternalDataPE> datasets = new ArrayList<ExternalDataPE>();
        for (WellContent wellContent : wellContents)
        {
            List<ExternalDataPE> plateDatasets =
                    plateToDatasetMap.get(wellContent.getPlate().getId());
            if (plateDatasets != null)
            {
                datasets.addAll(plateDatasets);
            }
        }
        return datasets;
    }

    private static Map<Long/* sample id */, List<ExternalDataPE>> createPlateToDatasetMap(
            List<ExternalDataPE> datasets)
    {
        Map<Long, List<ExternalDataPE>> map = new HashMap<Long, List<ExternalDataPE>>();
        for (ExternalDataPE dataset : datasets)
        {
            SamplePE sample = dataset.tryGetSample();
            if (sample != null)
            {
                Long sampleId = HibernateUtils.getId(sample);

                List<ExternalDataPE> plateDatasets = map.get(sampleId);
                if (plateDatasets == null)
                {
                    plateDatasets = new ArrayList<ExternalDataPE>();
                    map.put(sampleId, plateDatasets);
                }
                plateDatasets.add(dataset);
            }
        }
        return map;
    }

    private Map<String/* dataset code */, PlateImageParameters> loadImagesReport(
            List<ExternalDataPE> usedDataSets)
    {
        List<PlateImageParameters> imageParameters = new ArrayList<PlateImageParameters>();
        List<ExternalDataPE> distinctDataSets = getDistinctDataSets(usedDataSets);
        for (ExternalDataPE dataSet : distinctDataSets)
        {
            final IHCSDatasetLoader loader = businessObjectFactory.createHCSDatasetLoader(dataSet);
            imageParameters.add(PlateImageParametersFactory.create(loader));
        }
        return asDatasetToParamsMap(imageParameters);
    }

    private List<ExternalDataPE> getDistinctDataSets(List<ExternalDataPE> usedDataSets)
    {
        List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        Set<String> dataSetCodes = new HashSet<String>();
        for (ExternalDataPE dataSet : usedDataSets)
        {
            String code = dataSet.getCode();
            if (dataSetCodes.contains(code) == false)
            {
                dataSetCodes.add(code);
                result.add(dataSet);
            }
        }
        return result;
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

    private List<WellContent> loadLocations(PlateMaterialsSearchCriteria materialCriteria)
    {
        String[] materialCodes = materialCriteria.getMaterialCodes();
        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialCodes(
                        materialCriteria.getExperimentId().getId(), materialCodes,
                        materialCriteria.getMaterialTypeCodes());

        return convert(locations);
    }

    private List<WellContent> loadLocations(TechId geneMaterialId, String experimentPermId)
    {
        final long experimentId = loadExperimentIdByPermId(experimentPermId);

        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialId(geneMaterialId.getId(),
                        experimentId);

        return convert(locations);
    }

    private List<WellContentWithExperiment> loadLocations(TechId geneMaterialId)
    {
        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialId(geneMaterialId.getId());

        return convertWithExp(locations);
    }

    private List<WellContent> loadLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        final long experimentId = loadExperimentId(experimentIdentifier);

        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialId(geneMaterialId.getId(),
                        experimentId);

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
        sortByMaterialName(wellLocations);
        return wellLocations;
    }

    private List<WellContentWithExperiment> convertWithExp(
            DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations)
    {
        List<WellContentWithExperiment> wellLocations = new ArrayList<WellContentWithExperiment>();
        for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent location : locations)
        {
            wellLocations.add(convertWithExp(location));
        }
        sortByMaterialName(wellLocations);
        return wellLocations;
    }

    private static void sortByMaterialName(List<? extends WellContent> wellLocations)
    {
        Collections.sort(wellLocations, new Comparator<WellContent>()
            {
                public int compare(WellContent o1, WellContent o2)
                {
                    EntityReference m1 = o1.getMaterialContent();
                    EntityReference m2 = o2.getMaterialContent();
                    return m1.getCode().compareTo(m2.getCode());
                }
            });
    }

    private static WellContent convert(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        WellLocation location = ScreeningUtils.tryCreateLocationFromMatrixCoordinate(loc.well_code);
        EntityReference well =
                new EntityReference(loc.well_id, loc.well_code, loc.well_type_code,
                        EntityKind.SAMPLE, loc.well_perm_id);
        EntityReference plate =
                new EntityReference(loc.plate_id, loc.plate_code, loc.plate_type_code,
                        EntityKind.SAMPLE, loc.plate_perm_id);
        EntityReference materialContent =
                new EntityReference(loc.material_content_id, loc.material_content_code,
                        loc.material_content_type_code, EntityKind.MATERIAL, null);
        return new WellContent(location, well, plate, materialContent);
    }

    private static WellContentWithExperiment convertWithExp(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        WellContent content = convert(loc);
        final Experiment experiment = convertExperiment(loc);
        return new WellContentWithExperiment(content.tryGetLocation(), content.getWell(), content
                .getPlate(), content.getMaterialContent(), experiment);
    }

    private static Experiment convertExperiment(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        final Space space = new Space();
        space.setCode(loc.space_code);
        final Project project = new Project();
        project.setSpace(space);
        project.setCode(loc.proj_code);
        final Experiment experiment = new Experiment();
        experiment.setId(loc.exp_id);
        experiment.setCode(loc.exp_code);
        experiment.setPermId(loc.exp_perm_id);
        experiment.setProject(project);
        return experiment;
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

    private long loadExperimentIdByPermId(String experimentPermId)
    {
        final ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryGetByPermID(experimentPermId);
        if (experiment == null)
        {
            throw new UserFailureException("Unkown experiment for permId '" + experimentPermId
                    + "'.");
        }
        return experiment.getId();
    }
}
