package ethz.ch;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.http.HttpMethod;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;

public class DatasetCreationHelper
{
    private static String serviceURL;
    
    public static void setDssURL(String dssURL) {
        serviceURL = dssURL + "/datastore_server/store_share_file_upload";
    }
    
    public static final String SESSION_ID_PARAM = "sessionID";

    public static final String DATA_SET_TYPE_PARAM = "dataSetType";

    public static final String IGNORE_FILE_PATH_PARAM = "ignoreFilePath";

    public static final String FOLDER_PATH_PARAM = "folderPath";

    public static final String UPLOAD_ID_PARAM = "uploadID";
    
    public static void createDataset(IDataStoreServerApi v3dss, String sessionToken, String sampleIdentifier, String dataSetType, String fileName, byte[] content, Map<String,String> properties) throws Exception {
        String uploadId = UUID.randomUUID().toString();
        String folder = sampleIdentifier.substring(1).replace('/', '+');
        uploadFile(sessionToken, uploadId, dataSetType, false, "O+" + folder + "+ATTACHMENT" , fileName, content);
            
        UploadedDataSetCreation uploadedDataSetCreation = new UploadedDataSetCreation();
        uploadedDataSetCreation.setTypeId(new EntityTypePermId(dataSetType, EntityKind.DATA_SET));
        uploadedDataSetCreation.setSampleId(new SampleIdentifier(sampleIdentifier));
        uploadedDataSetCreation.setUploadId(uploadId);
        uploadedDataSetCreation.setProperties(properties);
            
        v3dss.createUploadedDataSet(sessionToken, uploadedDataSetCreation);
    }
    
    private static ContentResponse uploadFile(String sessionToken, String uploadId, String dataSetType, Boolean ignoreFilePath, String folderPath,
            String fileName, byte[] content)
            throws InterruptedException, TimeoutException, ExecutionException
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        MultiPartContentProvider multiPart = new MultiPartContentProvider();
        multiPart.addFilePart(fileName, fileName, new BytesContentProvider(content), null);
        multiPart.close();

        Request request = client.newRequest(serviceURL).method(HttpMethod.POST);

        if (sessionToken != null)
        {
            request.param(SESSION_ID_PARAM, sessionToken);
        }
        if (uploadId != null)
        {
            request.param(UPLOAD_ID_PARAM, uploadId);
        }
        if (ignoreFilePath != null)
        {
            request.param(IGNORE_FILE_PATH_PARAM, String.valueOf(ignoreFilePath));
        }
        if (folderPath != null)
        {
            request.param(FOLDER_PATH_PARAM, String.valueOf(folderPath));
        }
        if (dataSetType != null)
        {
            request.param(DATA_SET_TYPE_PARAM, dataSetType);
        }
        request.content(multiPart);
        return request.send();
    }
}
