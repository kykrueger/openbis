package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.PlateGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.BasicDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
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
            PythonInterpreter interpreter)
    {
        return new JythonPlateDatasetFactory(getRegistratorState());
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory<DataSetInformation>
    {
        private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<FeatureVectorDataSetInformation> featureVectorDatasetFactory;

        public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState)
        {
            super(registratorState);
            this.imageDatasetFactory =
                    new JythonObjectFactory<ImageDataSetInformation>(this.registratorState)
                        {
                            @Override
                            protected ImageDataSetInformation createDataSetInformation()
                            {
                                return new ImageDataSetInformation();
                            }
                        };
            this.featureVectorDatasetFactory =
                    new JythonObjectFactory<FeatureVectorDataSetInformation>(this.registratorState)
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
            List<Location> locations =
                    extractLocations(registrationDetails.getDataSetInformation().getImages());
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

        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                IFeaturesBuilder featureBuilder, File incomingDatasetFolder)
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
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                String dataSetPath, Properties properties) throws IOException
        {
            List<FeatureDefinition> featureDefinitions =
                    CsvFeatureVectorParser.parse(new File(dataSetPath), properties);
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

        /**
         * Factory method that creates a new registration details object for non-image datasets.
         * 
         * @deprecated used only in Matt's dropbox to register analysis datasets. Will be removed.
         */
        @Deprecated
        public DataSetRegistrationDetails<BasicDataSetInformation> createBasicRegistrationDetails()
        {
            DataSetRegistrationDetails<BasicDataSetInformation> registrationDetails =
                    new DataSetRegistrationDetails<BasicDataSetInformation>();
            BasicDataSetInformation dataSetInfo = new BasicDataSetInformation();
            setDatabaseInstance(dataSetInfo);
            registrationDetails.setDataSetInformation(dataSetInfo);
            return registrationDetails;
        }
    }
}
