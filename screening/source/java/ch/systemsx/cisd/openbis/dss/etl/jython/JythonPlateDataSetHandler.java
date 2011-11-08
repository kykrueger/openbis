package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.Hdf5ThumbnailGenerator;
import ch.systemsx.cisd.openbis.dss.etl.PlateGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailFilePaths;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDatasetFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{
    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            PythonInterpreter interpreter, DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return new JythonPlateDatasetFactory(getRegistratorState(),
                userProvidedDataSetInformationOrNull);
    }

    private static class JythonPlateDatasetFactory extends JythonObjectFactory<DataSetInformation>
            implements IImagingDatasetFactory
    {
        private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<FeatureVectorDataSetInformation> featureVectorDatasetFactory;

        public JythonPlateDatasetFactory(
                OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
            this.imageDatasetFactory =
                    new JythonObjectFactory<ImageDataSetInformation>(this.registratorState,
                            this.userProvidedDataSetInformationOrNull)
                        {
                            @Override
                            protected ImageDataSetInformation createDataSetInformation()
                            {
                                return new ImageDataSetInformation();
                            }
                        };
            this.featureVectorDatasetFactory =
                    new JythonObjectFactory<FeatureVectorDataSetInformation>(this.registratorState,
                            this.userProvidedDataSetInformationOrNull)
                        {
                            @Override
                            protected FeatureVectorDataSetInformation createDataSetInformation()
                            {
                                return new FeatureVectorDataSetInformation();
                            }
                        };
        }

        /** By default a starndard dataset is created. */
        @Override
        protected DataSetInformation createDataSetInformation()
        {
            return new DataSetInformation();
        }

        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
                SimpleImageDataConfig imageDataSet, File incomingDatasetFolder)
        {
            return SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                    incomingDatasetFolder, imageDatasetFactory);
        }

        /** a simple method to register the described image dataset in a separate transaction */
        public boolean registerImageDataset(SimpleImageDataConfig imageDataSet,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails =
                    createImageRegistrationDetails(imageDataSet, incomingDatasetFolder);
            return registerImageDataset(imageDatasetDetails, incomingDatasetFolder, service);
        }

        public boolean registerImageDataset(
                DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationTransaction<ImageDataSetInformation> transaction =
                    service.transaction(incomingDatasetFolder, imageDatasetFactory);
            IDataSet newDataset = transaction.createNewDataSet(imageDatasetDetails);
            transaction.moveFile(incomingDatasetFolder.getPath(), newDataset);
            return transaction.commit();
        }

        /**
         * @return a constant which can be used as a vocabulary term value for $PLATE_GEOMETRY
         *         property of a plate/
         * @throws UserFailureException if all available geometries in openBIS are too small (there
         *             is a well outside).
         */
        public String figureGeometry(
                DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails)
        {
            List<ImageFileInfo> images =
                    registrationDetails.getDataSetInformation().getImageDataSetStructure()
                            .getImages();
            List<Location> locations = extractLocations(images);
            List<String> plateGeometries =
                    loadPlateGeometries(registratorState.getGlobalState().getOpenBisService());
            return PlateGeometryOracle.figureGeometry(locations, plateGeometries);
        }

        private static List<String> loadPlateGeometries(IEncapsulatedOpenBISService openbisService)
        {
            Collection<VocabularyTerm> terms =
                    openbisService.listVocabularyTerms(ScreeningConstants.PLATE_GEOMETRY);
            List<String> plateGeometries = new ArrayList<String>();
            for (VocabularyTerm v : terms)
            {
                plateGeometries.add(v.getCode());
            }
            return plateGeometries;
        }

        private static List<Location> extractLocations(List<ImageFileInfo> images)
        {
            List<Location> locations = new ArrayList<Location>();
            for (ImageFileInfo image : images)
            {
                locations.add(image.tryGetWellLocation());
            }
            return locations;
        }

        // ----

        public IFeaturesBuilder createFeaturesBuilder()
        {
            return new FeaturesBuilder();
        }

        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
                IFeaturesBuilder featureBuilder)
        {
            FeaturesBuilder myFeatureBuilder = (FeaturesBuilder) featureBuilder;
            List<FeatureDefinition> featureDefinitions =
                    myFeatureBuilder.getFeatureDefinitionValuesList();
            return createFeatureVectorRegistrationDetails(featureDefinitions);
        }

        /**
         * Parses the feature vactors from the specified CSV file. CSV format can be configured with
         * following properties:
         * 
         * <pre>
         *   # Separator character between headers and row cells.
         *   separator = ,
         *   ignore-comments = true
         *   # Header of the column denoting the row of a well.
         *   well-name-row = row
         *   # Header of the column denoting the column of a well.
         *   well-name-col = col
         *   well-name-col-is-alphanum = true
         * </pre>
         * 
         * @throws IOException if file cannot be parsed
         */
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
                String csvFilePath, Properties properties) throws IOException
        {
            List<FeatureDefinition> featureDefinitions =
                    CsvFeatureVectorParser.parse(new File(csvFilePath), properties);
            return createFeatureVectorRegistrationDetails(featureDefinitions);
        }

        private DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                List<FeatureDefinition> featureDefinitions)
        {
            DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                    featureVectorDatasetFactory.createDataSetRegistrationDetails();
            FeatureVectorDataSetInformation featureVectorDataSet =
                    registrationDetails.getDataSetInformation();
            featureVectorDataSet.setFeatures(featureDefinitions);
            registrationDetails
                    .setDataSetType(ScreeningConstants.DEFAULT_ANALYSIS_WELL_DATASET_TYPE);
            registrationDetails.setMeasuredData(false);
            return registrationDetails;
        }

        // -------- backward compatibility methods

        /**
         * This method exists just for backward compatibility. It used to have the second parameter,
         * which is now ignored.
         * 
         * @deprecated use {@link #createFeatureVectorDatasetDetails(IFeaturesBuilder)} instead.
         */
        @SuppressWarnings("unused")
        @Deprecated
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                IFeaturesBuilder featureBuilder, Object incomingDatasetFolder)
        {
            return createFeatureVectorDatasetDetails(featureBuilder);
        }

        /**
         * @deprecated Changed to {@link #createFeatureVectorDatasetDetails(String, Properties)} due
         *             to naming convention change.
         */
        @SuppressWarnings("unused")
        @Deprecated
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                String csvFilePath, Properties properties) throws IOException
        {
            return createFeatureVectorDatasetDetails(csvFilePath, properties);
        }

    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createDataSetRegistrationService(
            File incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new JythonDataSetRegistrationService<DataSetInformation>(this, incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                new PythonInterpreter(), getGlobalState())
            {
                @Override
                protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                        File rollBackStackParentFolder,
                        File workingDirectory,
                        File stagingDirectory,
                        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
                {
                    return new ImagingDataSetRegistrationTransaction(rollBackStackParentFolder,
                            workingDirectory, stagingDirectory, this, registrationDetailsFactory);
                }
            };
    }

    /**
     * Imaging-specific transactions.
     */
    private static class ImagingDataSetRegistrationTransaction extends
            DataSetRegistrationTransaction<DataSetInformation> implements
            IImagingDataSetRegistrationTransaction
    {
        private final JythonPlateDatasetFactory registrationDetailsFactory;

        public ImagingDataSetRegistrationTransaction(File rollBackStackParentFolder,
                File workingDirectory, File stagingDirectory,
                DataSetRegistrationService<DataSetInformation> registrationService,
                IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
        {
            super(rollBackStackParentFolder, workingDirectory, stagingDirectory,
                    registrationService, registrationDetailsFactory);

            assert registrationDetailsFactory instanceof JythonPlateDatasetFactory : "JythonPlateDatasetFactory expected, but got: "
                    + registrationDetailsFactory.getClass().getCanonicalName();

            this.registrationDetailsFactory =
                    (JythonPlateDatasetFactory) registrationDetailsFactory;
        }

        public IDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
                File incomingFolderWithImages)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> details =
                    SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                            incomingFolderWithImages,
                            registrationDetailsFactory.imageDatasetFactory);
            return createNewImageDataSet(details);
        }

        public IDataSet createNewImageDataSet(
                DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails)
        {
            ImageDataSetInformation imageDataSetInformation =
                    imageRegistrationDetails.getDataSetInformation();
            ImageDataSetStructure imageDataSetStructure =
                    imageDataSetInformation.getImageDataSetStructure();
            File incomingDirectory = imageDataSetInformation.getIncomingDirectory();
            List<String> containedDataSetCodes = new ArrayList<String>();

            // create thumbnails dataset if needed
            IDataSet thumbnailDataset = null;
            boolean generateThumbnails = imageDataSetStructure.areThumbnailsGenerated();
            if (generateThumbnails)
            {
                thumbnailDataset = createThumbnailDataset();

                ThumbnailFilePaths thumbnailPaths =
                        generateThumbnails(imageDataSetStructure, incomingDirectory,
                                thumbnailDataset);
                imageDataSetInformation.setThumbnailFilePaths(thumbnailPaths);
                containedDataSetCodes.add(thumbnailDataset.getDataSetCode());
            }
            // create main dataset (with original images)
            IDataSet mainDataset = super.createNewDataSet(imageRegistrationDetails);
            String originalDatasetPathPrefix =
                    ScreeningConstants.ORIGINAL_DATA_DIR + File.separator
                            + incomingDirectory.getName();
            moveFile(incomingDirectory.getAbsolutePath(), mainDataset, originalDatasetPathPrefix);
            containedDataSetCodes.add(mainDataset.getDataSetCode());

            if (thumbnailDataset != null)
            {
                setSameDatasetOwner(mainDataset, thumbnailDataset);
            }

            IDataSet containerDataset =
                    createImageContainerDataset(mainDataset, imageDataSetInformation,
                            containedDataSetCodes);
            imageDataSetInformation.setContainerDatasetPermId(containerDataset.getDataSetCode());
            imageDataSetInformation.setDatasetRelativeImagesFolderPath(new File(
                    originalDatasetPathPrefix));

            return containerDataset;
        }

        private ThumbnailFilePaths generateThumbnails(ImageDataSetStructure imageDataSetStructure,
                File incomingDirectory, IDataSet thumbnailDataset)
        {
            String thumbnailFile =
                    createNewFile(thumbnailDataset, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);

            List<RelativeImageFile> images = asRelativeImageFile(imageDataSetStructure.getImages());
            ThumbnailFilePaths thumbnailPaths =
                    Hdf5ThumbnailGenerator.tryGenerateThumbnails(images, incomingDirectory,
                            thumbnailFile, imageDataSetStructure.getImageStorageConfiguraton(),
                            thumbnailDataset.getDataSetCode());
            return thumbnailPaths;
        }

        private static List<RelativeImageFile> asRelativeImageFile(List<ImageFileInfo> images)
        {
            List<RelativeImageFile> imageFiles = new ArrayList<RelativeImageFile>();
            for (ImageFileInfo image : images)
            {
                imageFiles.add(RelativeImageFile.create(image));
            }
            return imageFiles;
        }

        private IDataSet createThumbnailDataset()
        {
            IDataSet thumbnailDataset =
                    createNewDataSet(ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE);
            thumbnailDataset
                    .setFileFormatType(ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_FILE_FORMAT);
            thumbnailDataset.setMeasuredData(false);

            return thumbnailDataset;
        }

        private IDataSet createImageContainerDataset(IDataSet mainDataset,
                ImageDataSetInformation imageDataSetInformation, List<String> containedDataSetCodes)
        {
            String containerDatasetTypeCode = findContainerDatasetTypeCode(imageDataSetInformation);
            IDataSet containerDataset = createNewDataSet(containerDatasetTypeCode);
            setSameDatasetOwner(mainDataset, containerDataset);
            moveDatasetRelations(mainDataset, containerDataset);

            containerDataset.setContainedDataSetCodes(containedDataSetCodes);
            return containerDataset;
        }

        // Copies properties and relations to datasets from the main dataset to the container and
        // resets them in the main dataset.
        private static void moveDatasetRelations(IDataSet mainDataset, IDataSet containerDataset)
        {
            containerDataset.setParentDatasets(mainDataset.getParentDatasets());
            mainDataset.setParentDatasets(Collections.<String> emptyList());

            for (String propertyCode : mainDataset.getAllPropertyCodes())
            {
                containerDataset.setPropertyValue(propertyCode,
                        mainDataset.getPropertyValue(propertyCode));
                mainDataset.setPropertyValue(propertyCode, null);
            }
        }

        private static String findContainerDatasetTypeCode(
                ImageDataSetInformation imageDataSetInformation)
        {
            String mainDatasetTypeCode = imageDataSetInformation.getDataSetType().getCode();
            String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
            if (mainDatasetTypeCode.toUpperCase().startsWith(prefix) == false)
            {
                throw UserFailureException.fromTemplate(
                        "The image dataset type '%s' does not start with '%s'.",
                        mainDatasetTypeCode, prefix);
            }
            if (mainDatasetTypeCode.toUpperCase().contains(
                    ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER))
            {
                throw UserFailureException
                        .fromTemplate(
                                "The specified image dataset type '%s' should not be of container type, but contains '%s' in the type code.",
                                mainDatasetTypeCode,
                                ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER);
            }
            return prefix + ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER
                    + mainDatasetTypeCode.substring(prefix.length());
        }

        private static void setSameDatasetOwner(IDataSet templateDataset,
                IDataSet destinationDataset)
        {
            destinationDataset.setExperiment(templateDataset.getExperiment());
            destinationDataset.setSample(templateDataset.getSample());

        }

        @Override
        public IDataSet createNewDataSet(
                DataSetRegistrationDetails<? extends DataSetInformation> registrationDetails)
        {
            if (registrationDetails.getDataSetInformation() instanceof ImageDataSetInformation)
            {
                @SuppressWarnings("unchecked")
                DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails =
                        (DataSetRegistrationDetails<ImageDataSetInformation>) registrationDetails;
                return createNewImageDataSet(imageRegistrationDetails);
            } else
            {
                return super.createNewDataSet(registrationDetails);
            }
        }

    }

}
