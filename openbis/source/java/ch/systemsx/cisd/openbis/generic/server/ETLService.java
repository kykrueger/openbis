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

package ch.systemsx.cisd.openbis.generic.server;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_SERVICE_NAME;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.server.business.DataStoreServerSessionManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IWebService;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ETLService extends AbstractServer<IETLService> implements IETLService
{
    @Private
    static final String PROCESSING_PATH = "$processing-path-for-";

    @Private
    static final String PROCESSING_PATH_TEMPLATE = PROCESSING_PATH + "%s";

    @Private
    static final String PROCESSING_PARAMETERS_TEMPLATE = "$processing-parameters-for-%s";

    @Private
    static final String PROCESSING_DESCRIPTION_TEMPLATE = "$processing-description-for-%s";

    private static final String ENCODING = "utf-8";

    private final ISessionManager<Session> sessionManager;

    private final DataStoreServerSessionManager dssSessionManager;

    private final IDAOFactory daoFactory;

    private final ICommonBusinessObjectFactory boFactory;

    private final IDataStoreServiceFactory dssFactory;

    public ETLService(ISessionManager<Session> sessionManager,
            DataStoreServerSessionManager dssSessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory)
    {
        this(sessionManager, dssSessionManager, daoFactory, boFactory,
                new IDataStoreServiceFactory()
                    {
                        public IDataStoreService create(String serverURL)
                        {
                            return HttpInvokerUtils.createServiceStub(IDataStoreService.class,
                                    serverURL + "/" + DATA_STORE_SERVER_SERVICE_NAME, 5);
                        }
                    });
    }

    ETLService(ISessionManager<Session> sessionManager,
            DataStoreServerSessionManager dssSessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory)
    {
        super(sessionManager, daoFactory);
        this.sessionManager = sessionManager;
        this.dssSessionManager = dssSessionManager;
        this.daoFactory = daoFactory;
        this.boFactory = boFactory;
        this.dssFactory = dssFactory;
    }

    @Override
    protected Class<IETLService> getProxyInterface()
    {
        return IETLService.class;
    }

    public IETLService createLogger(boolean invocationSuccessful)
    {
        return new ETLServiceLogger(getSessionManager(), invocationSuccessful);
    }

    @Override
    public int getVersion()
    {
        return IWebService.VERSION;
    }

    public DatabaseInstancePE getHomeDatabaseInstance(String sessionToken)
    {
        return daoFactory.getHomeDatabaseInstance();
    }

    public void registerDataStoreServer(String sessionToken, int port, final String dssSessionToken)
    {
        Session session = sessionManager.getSession(sessionToken);
        String remoteHost = session.getRemoteHost();
        final String dssURL = "https://" + remoteHost + ":" + port;
        DataStoreServerSession dssSession = dssSessionManager.tryToGetSession(dssURL);
        if (dssSession == null)
        {
            final IDataStoreService service = dssFactory.create(dssURL);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Obtain version of Data Store Server at " + dssURL);
            }
            int dssVersion = service.getVersion(dssSessionToken);
            if (IDataStoreService.VERSION != dssVersion)
            {
                String msg =
                        "Data Store Server version is " + dssVersion + " instead of "
                                + IDataStoreService.VERSION;
                notificationLog.error(msg);
                throw new ConfigurationFailureException(msg);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data Store Server (version " + dssVersion + ") registered for "
                        + dssURL);
            }
            dssSession = new DataStoreServerSession(dssURL, service);
            dssSessionManager.registerDataStoreServer(dssSession);
        }
        dssSession.setSessionToken(dssSessionToken);
    }

    public String authenticate(String user, String password) throws UserFailureException
    {
        Session session = tryToAuthenticate(user, password);
        return session == null ? null : session.getSessionToken();
    }

    public void closeSession(String sessionToken) throws UserFailureException
    {
        logout(sessionToken);
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        sessionManager.getSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getExternalDataDAO().createDataSetCode();
    }

    public ExperimentPE tryToGetBaseExperiment(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        ExperimentPE experiment = tryToLoadExperimentBySampleIdentifier(session, sampleIdentifier);
        enrichWithPropertiesAndProcessingInstructions(experiment);
        return experiment;

    }

    private ExperimentPE tryToLoadExperimentBySampleIdentifier(final Session session,
            SampleIdentifier sampleIdentifier)
    {
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
        final SamplePE sample = sampleBO.tryToGetSample();
        return sample == null ? null : sample.getExperiment();
    }

    private void enrichWithPropertiesAndProcessingInstructions(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return;
        }
        HibernateUtils.initialize(experiment.getProperties());
        final List<ProcessingInstructionDTO> instructions =
                new ArrayList<ProcessingInstructionDTO>();
        final IAttachmentDAO experimentAttachmentDAO =
                daoFactory.getAttachmentDAO();
        final List<AttachmentPE> attachments =
                experimentAttachmentDAO.listAttachments(experiment);
        for (final AttachmentPE attachment : attachments)
        {
            final String fileName = attachment.getFileName();
            if (fileName.startsWith(PROCESSING_PATH))
            {
                final ProcessingInstructionDTO processingInstruction =
                        new ProcessingInstructionDTO();
                BeanUtils.fillBean(ProcessingInstructionDTO.class, processingInstruction,
                        attachment);
                processingInstruction.setProcedureTypeCode(fileName.substring(PROCESSING_PATH
                        .length()));
                instructions.add(processingInstruction);
            }
        }
        for (final ProcessingInstructionDTO instruction : instructions)
        {
            final String procedureType = instruction.getProcedureTypeCode();
            instruction.setPath(loadText(experiment, PROCESSING_PATH_TEMPLATE, procedureType));
            instruction.setDescription(loadText(experiment, PROCESSING_DESCRIPTION_TEMPLATE,
                    procedureType));
            instruction.setParameters(load(experiment, PROCESSING_PARAMETERS_TEMPLATE,
                    procedureType));
        }
        experiment.setProcessingInstructions(instructions.toArray(new ProcessingInstructionDTO[0]));
    }

    private final String createKey(final String template, final String procedureTypeCode)
    {
        return String.format(template, procedureTypeCode);
    }

    private final String createText(final byte[] textBytes)
    {
        try
        {
            return textBytes == null ? null : new String(textBytes, ENCODING);
        } catch (UnsupportedEncodingException ex)
        {
            throw new EnvironmentFailureException("Unsupported character encoding: " + ENCODING);
        }
    }

    private final String loadText(final ExperimentPE experiment, final String template,
            final String procedureTypeCode)
    {
        final byte[] value = load(experiment, template, procedureTypeCode);
        return createText(value);
    }

    private final byte[] load(final ExperimentPE experiment, final String template,
            final String procedureTypeCode)
    {
        final String key = createKey(template, procedureTypeCode);
        final IAttachmentDAO experimentAttachmentDAO =
                daoFactory.getAttachmentDAO();
        final AttachmentPE attachment =
                experimentAttachmentDAO.tryFindAttachmentByOwnerAndFileName(experiment, key);
        if (attachment != null)
        {
            return attachment.getAttachmentContent().getValue();
        }
        return null;
    }

    public SamplePropertyPE[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        SamplePE top = sample.getTop();
        if (top == null)
        {
            return new SamplePropertyPE[0];
        }
        HibernateUtils.initialize(top.getProperties());
        return top.getProperties().toArray(new SamplePropertyPE[0]);
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            ExternalData externalData) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = sessionManager.getSession(sessionToken);
        ExperimentPE experiment = tryToLoadExperimentBySampleIdentifier(session, sampleIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for sample " + sampleIdentifier);
        }
        if (experiment.getInvalidation() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is invalid.");
        }
        final ISampleBO sampleBO = boFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        final SamplePE cellPlate = sampleBO.getSample();
        final IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        SourceType sourceType = externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.define(externalData, cellPlate, sourceType);
        externalDataBO.save();
        final String dataSetCode = externalDataBO.getExternalData().getCode();
        assert dataSetCode != null : "Data set code not specified.";
    }

    public ExternalDataPE tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = sessionManager.getSession(sessionToken); // assert authenticated

        IExternalDataBO externalDataBO = boFactory.createExternalDataBO(session);
        externalDataBO.loadByCode(dataSetCode);
        externalDataBO.enrichWithParentsAndExperiment();
        return externalDataBO.getExternalData();
    }

}
