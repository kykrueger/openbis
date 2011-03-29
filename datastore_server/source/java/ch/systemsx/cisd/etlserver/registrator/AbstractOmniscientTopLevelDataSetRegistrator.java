/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator;

import static ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.STORAGE_PROCESSOR_KEY;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.AbstractTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.DataSetRegistrationRollbacker;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IPostRegistrationAction;
import ch.systemsx.cisd.etlserver.IPreRegistrationAction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.PropertiesBasedETLServerPlugin;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.utils.PostRegistrationExecutor;
import ch.systemsx.cisd.etlserver.utils.PreRegistrationExecutor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Abstract superclass for data set handlers that manage the entire data set registration process
 * themselves.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractOmniscientTopLevelDataSetRegistrator<T extends DataSetInformation>
        extends AbstractTopLevelDataSetRegistrator
{
    static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractOmniscientTopLevelDataSetRegistrator.class);

    /**
     * Object that contains the global state available for data set handlers.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class OmniscientTopLevelDataSetRegistratorState
    {
        private final TopLevelDataSetRegistratorGlobalState globalState;

        private final IStorageProcessorTransactional storageProcessor;

        private final ReentrantLock registrationLock;

        private final IFileOperations fileOperations;

        private final IPreRegistrationAction preRegistrationAction;

        private final IPostRegistrationAction postRegistrationAction;

        private final IDataStrategyStore dataStrategyStore;

        private final MarkerFileUtility markerFileUtility;

        private final DatabaseInstance homeDatabaseInstance;

        private OmniscientTopLevelDataSetRegistratorState(
                TopLevelDataSetRegistratorGlobalState globalState,
                IStorageProcessorTransactional storageProcessor, ReentrantLock registrationLock,
                IFileOperations fileOperations)
        {
            this.globalState = globalState;
            this.storageProcessor = storageProcessor;
            this.registrationLock = registrationLock;
            this.fileOperations = fileOperations;
            this.dataStrategyStore =
                    new DataStrategyStore(globalState.getOpenBisService(),
                            globalState.getMailClient());
            this.preRegistrationAction =
                    PreRegistrationExecutor.create(globalState.getPreRegistrationScript());
            this.postRegistrationAction =
                    PostRegistrationExecutor.create(globalState.getPostRegistrationScript());
            this.markerFileUtility =
                    new MarkerFileUtility(operationLog, notificationLog, fileOperations,
                            storageProcessor);
            this.homeDatabaseInstance = globalState.getOpenBisService().getHomeDatabaseInstance();
        }

        public TopLevelDataSetRegistratorGlobalState getGlobalState()
        {
            return globalState;
        }

        public IStorageProcessorTransactional getStorageProcessor()
        {
            return storageProcessor;
        }

        public ReentrantLock getRegistrationLock()
        {
            return registrationLock;
        }

        public IFileOperations getFileOperations()
        {
            return fileOperations;
        }

        public IPreRegistrationAction getPreRegistrationAction()
        {
            return preRegistrationAction;
        }

        public IPostRegistrationAction getPostRegistrationAction()
        {
            return postRegistrationAction;
        }

        public IDataStrategyStore getDataStrategyStore()
        {
            return dataStrategyStore;
        }

        public MarkerFileUtility getMarkerFileUtility()
        {
            return markerFileUtility;
        }

        public DatabaseInstance getHomeDatabaseInstance()
        {
            return homeDatabaseInstance;
        }
    }

    public static class DoNothingDelegatedAction implements IDelegatedActionWithResult<Boolean>
    {
        public Boolean execute()
        {
            return true; // do nothing
        }
    }

    public static class NoOpDelegate implements ITopLevelDataSetRegistratorDelegate
    {

        public void didRegisterDataSets(List<DataSetInformation> dataSetInformations)
        {
        }

    }

    private final OmniscientTopLevelDataSetRegistratorState state;

    private boolean stopped;

    /**
     * Constructor.
     * 
     * @param globalState
     */
    protected AbstractOmniscientTopLevelDataSetRegistrator(
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        IStorageProcessorTransactional storageProcessor =
                PropertiesBasedETLServerPlugin.create(IStorageProcessorTransactional.class,
                        globalState.getThreadParameters().getThreadProperties(),
                        STORAGE_PROCESSOR_KEY, true);
        storageProcessor.setStoreRootDirectory(globalState.getStoreRootDir());

        state =
                new OmniscientTopLevelDataSetRegistratorState(globalState, storageProcessor,
                        new ReentrantLock(), FileOperations.getMonitoredInstanceForCurrentThread());

        DataSetRegistrationTransaction.rollbackDeadTransactions(globalState.getStoreRootDir());

    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        return state;
    }

    public Lock getRegistrationLock()
    {
        return state.registrationLock;
    }

    /**
     * A file has arrived in the drop box. Handle it.
     * <p>
     * Setup necessary for data set handling is done, then the handleDataSet method (a subclass
     * responsibility) is invoked.
     */
    public final void handle(File incomingDataSetFileOrIsFinishedFile)
    {
        if (stopped)
        {
            return;
        }
        final File isFinishedFile = incomingDataSetFileOrIsFinishedFile;
        final File incomingDataSetFile;
        final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction;

        if (getGlobalState().isUseIsFinishedMarkerFile())
        {
            incomingDataSetFile =
                    state.getMarkerFileUtility().getIncomingDataSetPathFromMarker(isFinishedFile);
            cleanAfterwardsAction = new IDelegatedActionWithResult<Boolean>()
                {
                    public Boolean execute()
                    {
                        return state.getMarkerFileUtility().deleteAndLogIsFinishedMarkerFile(
                                isFinishedFile);
                    }
                };
        } else
        {
            incomingDataSetFile = incomingDataSetFileOrIsFinishedFile;
            cleanAfterwardsAction = new DoNothingDelegatedAction();
        }

        handle(incomingDataSetFile, null, new NoOpDelegate(), cleanAfterwardsAction);
    }

    /**
     * A file has arrived via RPC, handle it!
     * <p>
     * The handleDataSet method (a subclass responsibility) is invoked.
     */
    public final void handle(File incomingDataSetFile, DataSetInformation callerDataSetInformation,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        if (stopped)
        {
            return;
        }

        DataSetRegistrationService<T> service =
                handle(incomingDataSetFile, callerDataSetInformation, delegate,
                        new DoNothingDelegatedAction());
        if (service.didErrorsArise())
        {
            throw new EnvironmentFailureException("Could not process file "
                    + incomingDataSetFile.getName(), service.getEncounteredErrors().get(0));
        }
    }

    /**
     * Set up the infrastructure and forward control to subclasses. Clients can query the service
     * for information about what happened.
     */
    private DataSetRegistrationService<T> handle(File incomingDataSetFile,
            DataSetInformation callerDataSetInformationOrNull,
            ITopLevelDataSetRegistratorDelegate delegate,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction)
    {
        DataSetRegistrationService<T> service =
                createDataSetRegistrationService(incomingDataSetFile,
                        callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);

        try
        {
            handleDataSet(incomingDataSetFile, service);
            service.commit();
        } catch (Throwable ex)
        {
            operationLog.error("Could not process file " + incomingDataSetFile, ex);
            rollback(service, ex);
        }

        return service;
    }

    public boolean isStopped()
    {
        return stopped;
    }

    public boolean isRemote()
    {
        return true;
    }

    //
    // ISelfTestable
    //
    public final void check() throws ConfigurationFailureException, EnvironmentFailureException
    {
        new TopLevelDataSetChecker(operationLog, state.storageProcessor, state.fileOperations)
                .runCheck();
    }

    /**
     * Rollback a failure when trying to register an individual data set.
     * <p>
     * Subclasses may override, but should call super.
     * 
     * @param dataSetRegistrationService
     */
    public void rollback(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
    {
        updateStopped(throwable instanceof InterruptedExceptionUnchecked);

        new DataSetRegistrationRollbacker(stopped, registrationAlgorithm,
                registrationAlgorithm.getIncomingDataSetFile(), notificationLog, operationLog,
                throwable).doRollback();
    }

    /**
     * Rollback a failure when trying to commit a transaction.
     * <p>
     * Subclasses may override, but should call super.
     */

    public void rollbackTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        updateStopped(ex instanceof InterruptedExceptionUnchecked);
    }

    /**
     * Rollback a failure that occurs outside of any *particular* data set registration, but with
     * the whole processing of the incoming folder itself.
     * <p>
     * Subclasses may override, but should call super.
     */
    protected void rollback(DataSetRegistrationService<T> service, Throwable throwable)
    {
        updateStopped(throwable instanceof InterruptedExceptionUnchecked);

        service.abort();
    }

    /**
     * Create the data set registration service.
     * 
     * @param incomingDataSetFile
     * @param callerDataSetInformationOrNull
     */
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            File incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        @SuppressWarnings("unchecked")
        DataSetRegistrationService<T> service =
                new DataSetRegistrationService(this, incomingDataSetFile,
                        new DefaultDataSetRegistrationDetailsFactory(getRegistratorState(),
                                callerDataSetInformationOrNull), cleanAfterwardsAction, delegate);
        return service;
    }

    /**
     * Register the data with openBIS
     */
    public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
            NewExternalData data) throws Throwable
    {
        getGlobalState().getOpenBisService().registerDataSet(dataSetInformation, data);
    }

    /**
     * Update the value of stopped using the argument.
     * <p>
     * To be called by subclasses.
     */
    protected void updateStopped(boolean update)
    {
        stopped |= update;
    }

    /**
     * For subclasses to implement.
     * 
     * @throws Throwable
     */
    protected abstract void handleDataSet(File dataSetFile, DataSetRegistrationService<T> service)
            throws Throwable;
}
