package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ELNFixes {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ELNAnnotationsMigration.class);

    public static void beforeUpgrade() throws Exception {
        operationLog.info("ELNFixes beforeUpgrade START");
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = api.loginAsSystem();
        storageValidationLevelFix(sessionToken, api);
        operationLog.info("ELNFixes beforeUpgrade FINISH");
    }

    private static final String STORAGE_VALIDATION_LEVEL_PROPERTY_CODE = "$STORAGE.STORAGE_VALIDATION_LEVEL";
    private static final String STORAGE_VALIDATION_LEVEL_DEFAULT_VALUE = "RACK";

    private static void storageValidationLevelFix(String sessionToken, IApplicationServerInternalApi api) {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("STORAGE");

        SampleFetchOptions options = new SampleFetchOptions();
        options.withProperties();

        SearchResult<Sample> storages = api.searchSamples(sessionToken, criteria, options);

        List<SampleUpdate> storageUpdates = new ArrayList<>();
        for (Sample storage:storages.getObjects()) {
            if(storage.getProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE) == null ||
                    storage.getProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE).isEmpty()) {
                SampleUpdate storageUpdate = new SampleUpdate();
                storageUpdate.setSampleId(storage.getPermId());
                storageUpdate.setProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE, STORAGE_VALIDATION_LEVEL_DEFAULT_VALUE);
                storageUpdates.add(storageUpdate);
            }
        }

        if (!storageUpdates.isEmpty()) {
            api.updateSamples(sessionToken, storageUpdates);
        }
        operationLog.info("ELNFixes storageValidationLevelFix: " + storageUpdates.size());
    }
}
