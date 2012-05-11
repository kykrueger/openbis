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
import static ch.systemsx.cisd.etlserver.ThreadParameters.ON_ERROR_DECISION_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AbstractDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.AbstractTopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.DssUniqueFilenameGenerator;
import ch.systemsx.cisd.etlserver.IDataStrategyStore;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.PropertiesBasedETLServerPlugin;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IPrePostRegistrationHook;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner.IRollbackDelegate;
import ch.systemsx.cisd.etlserver.registrator.IDataSetOnErrorActionDecision.ErrorType;
import ch.systemsx.cisd.etlserver.registrator.api.v1.SecondaryTransactionFailure;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTransactionState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
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

        private final IDataStrategyStore dataStrategyStore;

        private final MarkerFileUtility markerFileUtility;

        private final DatabaseInstance homeDatabaseInstance;

        private final ValidationScriptRunner validationScriptRunner;

        private final IDataSetOnErrorActionDecision onErrorActionDecision;

        private OmniscientTopLevelDataSetRegistratorState(
                TopLevelDataSetRegistratorGlobalState globalState,
                IStorageProcessorTransactional storageProcessor, ReentrantLock registrationLock,
                IFileOperations fileOperations, IDataSetOnErrorActionDecision onErrorActionDecision)
        {
            this.globalState = globalState;
            this.storageProcessor = storageProcessor;
            this.registrationLock = registrationLock;
            this.fileOperations = fileOperations;
            this.dataStrategyStore =
                    new DataStrategyStore(globalState.getOpenBisService(),
                            globalState.getMailClient());
            this.markerFileUtility =
                    new MarkerFileUtility(operationLog, notificationLog, fileOperations,
                            storageProcessor);
            this.homeDatabaseInstance = globalState.getOpenBisService().getHomeDatabaseInstance();
            this.validationScriptRunner =
                    ValidationScriptRunner.createValidatorFromScriptPaths(globalState
                            .getValidationScriptsOrNull());
            this.onErrorActionDecision = onErrorActionDecision;
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

        public ValidationScriptRunner getValidationScriptRunner()
        {
            return validationScriptRunner;
        }

        public IDataSetOnErrorActionDecision getOnErrorActionDecision()
        {
            return onErrorActionDecision;
        }
    }

    public static class DoNothingDelegatedAction extends AbstractDelegatedActionWithResult<Boolean>
    {
        public DoNothingDelegatedAction()
        {
            super(true);
        }
    }

    /**
     * The clean-up action after registration.
     * <p>
     * If registration succeeded, the originalInboxFile is deleted. If registration failed, the
     * hardlink copy is deleted, leaving the orignalInboxFile.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class PostRegistrationCleanUpAction extends
            AbstractDelegatedActionWithResult<Boolean>
    {
        private final DataSetFile incoming;

        private final IDelegatedActionWithResult<Boolean> wrappedAction;

        public PostRegistrationCleanUpAction(DataSetFile incoming,
                IDelegatedActionWithResult<Boolean> wrappedAction)
        {
            super(true);
            this.incoming = incoming;
            this.wrappedAction = wrappedAction;
        }

        @Override
        public Boolean execute(boolean didOperationSucceed)
        {
            boolean operationSuccessful = true;
            if (didOperationSucceed)
            {
                // Registration succeeded -- delete original file
                operationSuccessful =
                        FileUtilities.deleteRecursively(incoming.getRealIncomingFile());

                // If the parent of the hardlink copy file, which we generated, is empty, delete it
                // too
                File hardlinkCopyParent = incoming.getLogicalIncomingFile().getParentFile();
                if (hardlinkCopyParent.list().length < 1)
                {
                    hardlinkCopyParent.delete();
                }
            } else
            {
                // Registration failed -- remove the copy, leaving the original.
                operationSuccessful =
                        FileUtilities.deleteRecursively(incoming.getLogicalIncomingFile());
            }
            boolean wrappedActionResult = wrappedAction.execute(didOperationSucceed);

            return operationSuccessful && wrappedActionResult;
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

        Properties onErrorDecisionProperties =
                ExtendedProperties.getSubset(globalState.getThreadParameters()
                        .getThreadProperties(), ON_ERROR_DECISION_KEY + ".", true);
        IDataSetOnErrorActionDecision onErrorDecision =
                ClassUtils.create(
                        IDataSetOnErrorActionDecision.class,
                        globalState.getThreadParameters().getOnErrorActionDecisionClass(
                                ConfiguredOnErrorActionDecision.class), onErrorDecisionProperties);

        state =
                new OmniscientTopLevelDataSetRegistratorState(globalState, storageProcessor,
                        new ReentrantLock(), FileOperations.getMonitoredInstanceForCurrentThread(),
                        onErrorDecision);

        state.fileOperations.mkdirs(getRollBackStackParentFolder());
        
        DataSetRegistrationTransaction
                .rollbackDeadTransactions(getRollBackStackParentFolder());

    }

    public OmniscientTopLevelDataSetRegistratorState getRegistratorState()
    {
        return state;
    }

    public Lock getRegistrationLock()
    {
        return state.registrationLock;
    }

    public File getRollBackStackParentFolder()
    {
        return getGlobalState().getDssInternalTempDir();
    }
    
    /**
     * returns the recovery marker file if found, or null otherwise. It first checks if the incoming
     * is the marker file, then if there is a marker file corresponding to this incoming file
     */
    private boolean isRecoveryMarkerFile(File incoming)
    {
        return state.getGlobalState().getStorageRecoveryManager().isRecoveryFile(incoming);
    }

    private boolean hasRecoveryMarkerFile(File incoming)
    {
        return new File(incoming.getAbsolutePath()
                + IDataSetStorageRecoveryManager.PROCESSING_MARKER).exists();
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

        if (isRecoveryMarkerFile(incomingDataSetFileOrIsFinishedFile))
        {
            handleRecovery(incomingDataSetFileOrIsFinishedFile);
            return;
        } else if (hasRecoveryMarkerFile(incomingDataSetFileOrIsFinishedFile))
        {
            operationLog.info("Ignore file, as the recovery marker exists for it: "
                    + incomingDataSetFileOrIsFinishedFile.getAbsolutePath());
            // will handle only the recovery file - don't do anything
            return;
        } else if (false == incomingDataSetFileOrIsFinishedFile.exists())
        {
            operationLog.info("The file doesn't exist: "
                    + incomingDataSetFileOrIsFinishedFile.getAbsolutePath());
            // it can mean that the recovery has already cleaned this file
            return;
        }

        final File isFinishedFile = incomingDataSetFileOrIsFinishedFile;
        final File incomingDataSetFile;
        final IDelegatedActionWithResult<Boolean> markerFileCleanupAction;

        // Figure out what the real incoming data is -- if we use a marker file, it will tell us the
        // name
        if (getGlobalState().isUseIsFinishedMarkerFile())
        {
            incomingDataSetFile =
                    state.getMarkerFileUtility().getIncomingDataSetPathFromMarker(isFinishedFile);

            markerFileCleanupAction = new IDelegatedActionWithResult<Boolean>()
                {
                    public Boolean execute(boolean didOperationSucceed)
                    {
                        boolean markerDeleteSucceeded =
                                state.getMarkerFileUtility().deleteAndLogIsFinishedMarkerFile(
                                        isFinishedFile);
                        return markerDeleteSucceeded;
                    }
                };
        } else
        {
            incomingDataSetFile = incomingDataSetFileOrIsFinishedFile;
            markerFileCleanupAction = new DoNothingDelegatedAction();
        }

        // read from configuration prestaging parameter.
        DataSetRegistrationPreStagingBehavior preStagingUsage =
                state.getGlobalState().getThreadParameters()
                        .getDataSetRegistrationPreStagingBehavior();

        if (preStagingUsage == DataSetRegistrationPreStagingBehavior.USE_ORIGINAL)
        {
            DataSetFile incoming = new DataSetFile(incomingDataSetFile);
            handle(incoming, null, new NoOpDelegate(), markerFileCleanupAction);
        } else
        {
            // If we should the prestaging phase, we make a hardlink copy in prestaging area
            File copyOfIncoming = copyIncomingFileToPreStaging(incomingDataSetFile);

            DataSetFile dsf = new DataSetFile(incomingDataSetFile, copyOfIncoming);

            // For cleanup we use the postRegistrationCleanUpAction wich clears the prestaging area.
            PostRegistrationCleanUpAction cleanupAction =
                    new PostRegistrationCleanUpAction(dsf, markerFileCleanupAction);
            handle(dsf, null, new NoOpDelegate(), cleanupAction);
        }
    }

    private File copyIncomingFileToPreStaging(File incomingDataSetFile)
    {
        TopLevelDataSetRegistratorGlobalState globalState = state.getGlobalState();
        File preStagingRootDir = globalState.getPreStagingDir();
        String incomingDirName =
                new DssUniqueFilenameGenerator(globalState.getThreadParameters().getThreadName(),
                        incomingDataSetFile.getName(), null).generateFilename();
        File preStagingDir = new File(preStagingRootDir, incomingDirName);
        preStagingDir.mkdir();

        // Try to find a hardlink maker
        IImmutableCopier hardlinkMaker = FastRecursiveHardLinkMaker.tryCreate();
        boolean linkWasMade = false;
        if (null != hardlinkMaker)
        {
            // Use the hardlink maker if we got one
            Status status = hardlinkMaker.copyImmutably(incomingDataSetFile, preStagingDir, null);
            linkWasMade = status.isOK();
        }

        if (false == linkWasMade)
        {
            FileUtilities.copyFileTo(incomingDataSetFile, preStagingDir, true);
        }

        return new File(preStagingDir, incomingDataSetFile.getName());
    }

    /**
     * A file has arrived via RPC, handle it!
     * <p>
     * The handleDataSet method (a subclass responsibility) is invoked.
     */
    public final void handle(File incomingDataSetFile, DataSetInformation callerDataSetInformation,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        handle(new DataSetFile(incomingDataSetFile), callerDataSetInformation, delegate);
    }

    /**
     * A file has arrived via RPC, handle it!
     * <p>
     * The handleDataSet method (a subclass responsibility) is invoked.
     */
    public final void handle(DataSetFile incomingDataSetFile,
            DataSetInformation callerDataSetInformation,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        if (stopped)
        {
            return;
        }

        // In this case, don't make a hardlink copy, since the user has the file on their local
        // machine

        DataSetRegistrationService<T> service =
                handle(incomingDataSetFile, callerDataSetInformation, delegate,
                        new DoNothingDelegatedAction());
        if (service.didErrorsArise())
        {
            Throwable firstError = service.getEncounteredErrors().get(0);
            throw new EnvironmentFailureException("Could not process file "
                    + incomingDataSetFile.getLogicalIncomingFile().getName(),
                    asSerializableException(firstError));
        }
    }

    /**
     * Not all instances of PyExceptions are serializable, because they keep references to
     * non-serializable objects e.g. java.lang.reflect.Method.
     * <p>
     * Subclasses may need to override if they encounter other kinds of exceptions that cannot
     * happen in this generic context.
     */
    protected Throwable asSerializableException(Throwable throwable)
    {
        if (throwable instanceof UserFailureException)
        {
            return new RuntimeException(throwable.getMessage());
        }
        Throwable cause = throwable;
        while (cause.getCause() != null)
        {
            cause = cause.getCause();
        }
        return new RuntimeException(cause.toString());
    }

    private void handleRecovery(final File recoveryMarkerFile)
    {
        DataSetStoragePrecommitRecoveryState<T> recoveryState =
                state.getGlobalState().getStorageRecoveryManager()
                        .extractPrecommittedCheckpoint(recoveryMarkerFile);

        // TODO: cleanup also with the incoming marker file

        final File recoveryFile =
                state.getGlobalState().getStorageRecoveryManager()
                        .getRecoveryFileFromMarker(recoveryMarkerFile);

        if (false == recoveryFile.exists())
        {
            //TODO: is it safe to throw from here?
            operationLog.error("recovery file does not exist. " + recoveryFile);
            throw new IllegalStateException("Recovery file " + recoveryFile + " doesn't exist");
        }

        operationLog.info("will recover from broken registration. Found marker file "
                + recoveryMarkerFile + " and " + recoveryFile);

        IDelegatedActionWithResult<Boolean> recoveryMarkerFileCleanupAction =
                new IDelegatedActionWithResult<Boolean>()
                    {
                        public Boolean execute(boolean didOperationSucceed)
                        {
                            recoveryMarkerFile.delete();
                            recoveryFile.delete();
                            return true;
                        }
                    };

        PostRegistrationCleanUpAction cleanupAction =
                new PostRegistrationCleanUpAction(recoveryState.getIncomingDataSetFile(),
                        recoveryMarkerFileCleanupAction);

        handleRecoveryState(recoveryState, cleanupAction);
    }

    private void handleRecoveryState(DataSetStoragePrecommitRecoveryState<T> recoveryState,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction)
    {
        // TODO: Jobs left to do here:
        // rollback
        // jython hooks

        DssRegistrationLogger logger = recoveryState.getRegistrationLogger(state);

        logger.log("The registration has been disturbed. Will try to recover...");
        
        // rollback delegate
        final List<Throwable> encounteredErrors = new ArrayList<Throwable>();

        IRollbackDelegate<T> rollbackDelegate = new IRollbackDelegate<T>()
            {
                public void didRollbackStorageAlgorithmRunner(
                        DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex,
                        ErrorType errorType)
                {
                    encounteredErrors.add(ex);
                }
            };

        // hookAdaptor
        final AbstractOmniscientTopLevelDataSetRegistrator<T> myself = this;
        IPrePostRegistrationHook<T> hookAdaptor = new IPrePostRegistrationHook<T>()
            {
                public void executePreRegistration(DataSetRegistrationTransaction<T> transaction)
                {
                    // myself.didPreRegistration(null, transaction);
                }

                public void executePostRegistration(DataSetRegistrationTransaction<T> transaction)
                {
                    // myself.didPostRegistration(null, transaction);
                }
            };

        DataSetStorageAlgorithmRunner<T> runner =
                new DataSetStorageAlgorithmRunner<T>(recoveryState.getIncomingDataSetFile(), // incoming
                        recoveryState.getDataSetStorageAlgorithms(state), // algorithms
                        rollbackDelegate, // rollback delegate,
                        recoveryState.getRollbackStack(), // rollbackstack
                        logger, // registrationLogger
                        state.getGlobalState().getOpenBisService(), // openBisService
                        hookAdaptor, // the hooks
                        state.getGlobalState().getStorageRecoveryManager());
        boolean registrationSuccessful = false;

        operationLog.info("Recovery succesfully deserialized the state of the registration");
        try
        {
            List<String> dataSetCodes = recoveryState.getDataSetCodes();
            //TODO: we need to check for something more than just a registered datasets, for the registrations that doesn't register anything.
            //OTOH: do we need to recover in this case? (as there is nothing to store)
            //maybe then we should only checkpoint for recovery if there ARE datasets  registered?
            List<ExternalData> registeredDataSets =
                    state.getGlobalState().getOpenBisService().listDataSetsByCode(dataSetCodes);
            if (registeredDataSets.isEmpty())
            {
                operationLog.info("Recovery hasn't found registration artifacts in the application server. Registration of metadata was not succesfull.");
                
                IRollbackStackDelegate rollbackStackDelegate = new AbstractTransactionState.LiveTransactionRollbackDelegate(state.getGlobalState().getStagingDir());
                
                recoveryState.getRollbackStack().rollbackAll(rollbackStackDelegate);
                // encounteredErrors.add(ex);
                //
                // UnstoreDataAction action =
                // registratorContext.getOnErrorActionDecision().computeUndoAction(errorType, ex);
                // DataSetStorageRollbacker rollbacker =
                // new DataSetStorageRollbacker(registratorContext, operationLog, action,
                // incomingDataSetFile.getRealIncomingFile(), null, ex, errorType);
                // operationLog.info(rollbacker.getErrorMessageForLog());
                // rollbacker.doRollback(dssRegistrationLog);
                //
                // invokeRollbackTransactionFunction
                //
                // // rollback during metadata registration
            } else
            {
                operationLog.info("Recovery has found datasets in the AS. The registration of metadata was succesfull.");
                runner.storeAfterRegistration();
                logger.registerSuccess();
                registrationSuccessful = true;
            }
        } catch (Throwable error)
        {
            System.err.println("Caught an error! " + error);
            error.printStackTrace();
            // in this case we should ignore, and run the recovery again after some time
            encounteredErrors.add(error);
        }

        logger.logDssRegistrationResult(encounteredErrors);

        // TODO: recreate this clean afterwards function
        cleanAfterwardsAction.execute(registrationSuccessful);
    }

    /**
     * Set up the infrastructure and forward control to subclasses. Clients can query the service
     * for information about what happened.
     */
    private DataSetRegistrationService<T> handle(DataSetFile incomingDataSetFile,
            DataSetInformation callerDataSetInformationOrNull,
            ITopLevelDataSetRegistratorDelegate delegate,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction)
    {
        DataSetRegistrationService<T> service =
                createDataSetRegistrationService(incomingDataSetFile,
                        callerDataSetInformationOrNull, cleanAfterwardsAction, delegate);

        try
        {
            ValidationScriptRunner validationScriptRunner =
                    ValidationScriptRunner.createValidatorFromScriptPaths(getGlobalState()
                            .getValidationScriptsOrNull());

            List<ValidationError> validationErrors =
                    validationScriptRunner.validate(incomingDataSetFile.getLogicalIncomingFile());
            if (validationErrors.size() > 0)
            {
                handleValidationErrors(validationErrors, incomingDataSetFile, service);
            } else
            {
                handleDataSet(incomingDataSetFile.getLogicalIncomingFile(), service);
                service.commit();
            }
        } catch (Throwable ex)
        {
            operationLog.error("Could not process file " + incomingDataSetFile, ex);
            rollback(service, ex);

            // If we are here, it is because there was an error thrown in Java before trying to
            // register the data set. This is considered a script error
            UnstoreDataAction action =
                    getRegistratorState().getOnErrorActionDecision().computeUndoAction(
                            ErrorType.REGISTRATION_SCRIPT_ERROR, ex);
            DataSetStorageRollbacker rollbacker =
                    new DataSetStorageRollbacker(getRegistratorState(), operationLog, action,
                            incomingDataSetFile.getRealIncomingFile(), null, ex,
                            ErrorType.REGISTRATION_SCRIPT_ERROR);
            operationLog.info(rollbacker.getErrorMessageForLog());

            service.getDssRegistrationLog().log("Processing failed : " + ex.toString());
            rollbacker.doRollback(service.getDssRegistrationLog());

            service.getDssRegistrationLog().registerFailure();
        }

        service.cleanAfterRegistrationIfNecessary();

        return service;
    }

    /**
     * Validation errors were found in the incoming data set file, display them. Subclasses may
     * override.
     */
    protected void handleValidationErrors(List<ValidationError> validationErrors,
            DataSetFile incomingDataSetFile, DataSetRegistrationService<T> service)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation script [");
        sb.append(getGlobalState().getValidationScriptsOrNull());
        sb.append("] found errors in incoming data set [");
        sb.append(incomingDataSetFile.getLogicalIncomingFile());
        sb.append("]:\n");
        for (ValidationError error : validationErrors)
        {
            sb.append("\t");
            sb.append(error.getErrorMessage());
            sb.append("\n");
        }

        UnstoreDataAction action =
                getRegistratorState().getOnErrorActionDecision().computeUndoAction(
                        ErrorType.INVALID_DATA_SET, null);
        DataSetStorageRollbacker rollbacker =
                new DataSetStorageRollbacker(getRegistratorState(), operationLog, action,
                        incomingDataSetFile.getRealIncomingFile(), null, null,
                        ErrorType.INVALID_DATA_SET);
        sb.append(rollbacker.getErrorMessageForLog());
        operationLog.info(sb.toString());
        rollbacker.doRollback(service.getDssRegistrationLog());
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
     * Rollback a failure when trying to commit a transaction.
     * <p>
     * Subclasses may override, but should call super.
     */

    public void didRollbackTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithm, Throwable ex)
    {
        updateStopped(ex instanceof InterruptedExceptionUnchecked);
    }

    /**
     * A method called after a successful commit of a transaction.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    public void didCommitTransaction(DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction)
    {
    }

    /**
     * A method called just before the registration of datasets in application server.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    public void didPreRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction)
    {
    }

    /**
     * A method called just after the successful registration of datasets in application server.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    public void didPostRegistration(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction)
    {
    }

    /**
     * A method called when there is an error in one of the secondary transactions.
     * <p>
     * Subclasses can override and implement their own handling logic.
     */
    public void didEncounterSecondaryTransactionErrors(
            DataSetRegistrationService<T> dataSetRegistrationService,
            DataSetRegistrationTransaction<T> transaction,
            List<SecondaryTransactionFailure> secondaryErrors)
    {
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

        service.abort(throwable);
    }

    /**
     * Create the data set registration service.
     * 
     * @param incomingDataSetFile
     * @param callerDataSetInformationOrNull
     */
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            final IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        @SuppressWarnings(
            { "unchecked" })
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
