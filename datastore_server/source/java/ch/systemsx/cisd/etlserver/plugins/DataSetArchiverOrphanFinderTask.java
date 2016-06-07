package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class DataSetArchiverOrphanFinderTask implements IMaintenanceTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSetArchiverOrphanFinderTask.class);

    public static final String EMAIL_ADDRESSES_KEY = "email-addresses";

    private static final String SEPARATOR = ",";

    private List<EMailAddress> emailAddresses;

    private IMailClient mailClient;
    
    public DataSetArchiverOrphanFinderTask()
    {
        this(ServiceProvider.getDataStoreService().createEMailClient());
    }
    
    DataSetArchiverOrphanFinderTask(IMailClient mailClient)
    {
        this.mailClient = mailClient;
        
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
        emailAddresses = getEMailAddresses(properties);
    }

    @Transactional
    @Override
    public void execute()
    {
        operationLog.info(DataSetArchiverOrphanFinderTask.class.getSimpleName() + " Started");

        // 1.Directories.
        operationLog.info("1. Directories, obtain archiver directory.");
        String destination = DataStoreServer.getConfigParameter("archiver.final-destination", null);
        if (destination == null)
        {
            destination = DataStoreServer.getConfigParameter("archiver.destination", null);
        }

        if (destination == null)
        {
            operationLog.info("No destination directory found, this task can't execute.");
            return;
        }

        // 2.1 Database.
        operationLog.info("2.1 Database, obtain a list of the multi dataset containers on the database.");
        List<String> containers = MultiDataSetArchiverDataSourceUtil.getContainerList();
        Set<String> multiDatasetsContainersOnDB = new HashSet<String>();
        if (containers != null)
        {
            for (String container : containers)
            {
                multiDatasetsContainersOnDB.add(container.toLowerCase());
            }
        }

        operationLog.info("2.2 Database, obtain a list of the archived datasets on the database.");
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> presentDTOs = service.listPhysicalDataSetsByArchivingStatus(null, Boolean.TRUE);
        Set<String> presentInArchiveOnDB = new HashSet<String>();
        if (presentDTOs != null)
        {
            for (SimpleDataSetInformationDTO presentDTO : presentDTOs)
            {
                presentInArchiveOnDB.add(presentDTO.getDataSetCode().toLowerCase());
            }
        }

        // 3.Verify if the files on destination are on multi dataset archiver containers or a normal archived dataset.
        operationLog.info("3. Verify if the files on destination are on multi dataset archiver containers or a normal archived dataset.");
        File[] filesOnDisk = new File(destination).listFiles();
        Set<String> presentInArchiveFS = new HashSet<String>();
        List<File> onFSandNotDB = new ArrayList<File>();
        for (File file : filesOnDisk)
        {
            String fileName = file.getName().toLowerCase();
            presentInArchiveFS.add(fileName); // To be used in step 4
            if (multiDatasetsContainersOnDB.contains(fileName))
            {
                operationLog.debug("Found multi dataset archiver container: " + file.getName());
            } else
            {
                String dataSetCode = file.getName().substring(0, file.getName().length() - 4);
                if ((fileName.endsWith(".tar") || fileName.endsWith(".zip"))
                        && presentInArchiveOnDB.contains(dataSetCode))
                {
                    operationLog.debug("Found archived dataset: " + file.getName());
                } else
                {
                    onFSandNotDB.add(file);
                    operationLog.debug("Not found on DB for FS: " + file.getName());
                }
            }
        }

        // 4.Verify if the datasets archived on the database are on the file system.
        operationLog.info("4. Verify if the datasets archived on the database are on the file system.");
        List<String> multiOnDBandNotFS = new ArrayList<String>();
        for (String multiDatasetsContainerOnDB : multiDatasetsContainersOnDB)
        {
            if (presentInArchiveFS.contains(multiDatasetsContainerOnDB) == false)
            {
                operationLog.debug("Multi - Not found in FS for DB: " + multiDatasetsContainerOnDB);
                multiOnDBandNotFS.add(multiDatasetsContainerOnDB);
            }
        }

        List<String> singleOnDBandNotFS = new ArrayList<String>();
        for (String presentOnDB : presentInArchiveOnDB)
        {
            String fileNameTar = presentOnDB + ".tar";
            String fileNameZip = presentOnDB + ".zip";

            if (!presentInArchiveFS.contains(fileNameTar) && !presentInArchiveFS.contains(fileNameZip))
            {
                operationLog.debug("Single - Not found in FS for DB: " + presentOnDB);
                singleOnDBandNotFS.add(presentOnDB);
            }
        }

        // 5. Send email with not found files.
        if (onFSandNotDB.size() > 0)
        {
            operationLog.info("5. Send email with not found files.");
            String subject = "openBIS Data Set Archiv Orphan Finder found files";
            String content = "";
            for (File notFound : onFSandNotDB)
            {
                content += "Found in the archive but not in the database: " + notFound.getName() + "\t" + notFound.length() + "\n";
            }
            for (String notFound : multiOnDBandNotFS)
            {
                content += "Found in the database but not in the multi data set archive: " + notFound + "\n";
            }
            for (String notFound : singleOnDBandNotFS)
            {
                content += "Found in the database but not in the archive: " + notFound + "\n";
            }
            for (EMailAddress recipient : emailAddresses)
            {
                mailClient.sendEmailMessage(subject, content, null, null, recipient);
                operationLog.info("Mail sent to " + recipient.tryGetEmailAddress());
            }
        }

        operationLog.info(DataSetArchiverOrphanFinderTask.class.getSimpleName() + " Finished");
    }

    private List<EMailAddress> getEMailAddresses(Properties properties)
    {
        String[] tokens =
                PropertyUtils.getMandatoryProperty(properties, EMAIL_ADDRESSES_KEY)
                        .split(SEPARATOR);
        List<EMailAddress> addresses = new ArrayList<EMailAddress>();
        for (String token : tokens)
        {
            addresses.add(new EMailAddress(token.trim()));
        }
        return addresses;
    }

}
