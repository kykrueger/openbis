/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationExecutionError;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionAvailability;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionDetails;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionProgress;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionSummary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionDetailsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionSummaryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.OperationExecutionPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgressListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgressStack;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.ProgressFormatter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionStore implements IOperationExecutionStore, Runnable
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationExecutionStore.class);

    @Autowired
    private IOperationExecutionConfig config;

    @Autowired
    private IOperationExecutionDBStore dbStore;

    @Autowired
    private IOperationExecutionFSStore fsStore;

    private Thread progressThread;

    private Map<OperationExecutionPermId, IProgress> progressMap = new HashMap<OperationExecutionPermId, IProgress>();

    public OperationExecutionStore()
    {
    }

    OperationExecutionStore(IOperationExecutionConfig config, IOperationExecutionDBStore dbStore, IOperationExecutionFSStore fsStore)
    {
        this.config = config;
        this.dbStore = dbStore;
        this.fsStore = fsStore;
        init();
    }

    @PostConstruct
    private void init()
    {
        progressThread = new Thread(this);
        progressThread.setName(config.getProgressThreadName());
        progressThread.setDaemon(true);
        progressThread.start();
    }

    @Override
    public void executionNew(final IOperationContext context, final OperationExecutionPermId executionId, final List<? extends IOperation> operations,
            final IOperationExecutionOptions options)
    {
        checkContext(context);
        checkExecutionId(executionId);
        checkOperations(operations);
        checkOptions(options);

        context.addProgressListener(new IProgressListener()
            {

                @Override
                public void onProgress(IProgressStack progressStack)
                {
                    if (progressStack.size() > 0)
                    {
                        IProgress progress = progressStack.iterator().next();

                        if (progress != null)
                        {
                            synchronized (progressMap)
                            {
                                progressMap.put(executionId, progress);
                            }
                        }
                    }
                }
            });

        List<String> operationsMessages = new ArrayList<String>();
        for (IOperation operation : operations)
        {
            operationsMessages.add(operation.getMessage());
        }

        int availabilityTime = config.getAvailabilityTimeOrDefault(options.getAvailabilityTime());
        int summaryAvailabilityTime = config.getSummaryAvailabilityTimeOrDefault(options.getSummaryAvailabilityTime());
        int detailsAvailabilityTime = config.getDetailsAvailabilityTimeOrDefault(options.getDetailsAvailabilityTime());

        dbStore.executionNew(executionId.getPermId(), context.getSession().tryGetPerson().getId(), options.getDescription(), operationsMessages,
                availabilityTime, summaryAvailabilityTime, detailsAvailabilityTime);
        fsStore.executionNew(executionId.getPermId(), operations);

        operationLog.info("Execution " + executionId + " is new");
    }

    @Override
    public void executionScheduled(IOperationContext context, OperationExecutionPermId executionId)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionScheduled(executionId.getPermId());

        operationLog.info("Execution " + executionId + " has been scheduled");
    }

    @Override
    public void executionRunning(IOperationContext context, OperationExecutionPermId executionId)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionRunning(executionId.getPermId());

        operationLog.info("Execution " + executionId + " is running");
    }

    private void executionProgressed(OperationExecutionPermId executionId, IProgress progress)
    {
        dbStore.executionProgressed(executionId.getPermId(), ProgressFormatter.format(progress));
        fsStore.executionProgressed(executionId.getPermId(),
                new OperationExecutionProgress(ProgressFormatter.format(progress), progress.getNumItemsProcessed(),
                        progress.getTotalItemsToProcess()));

        operationLog.info("Execution " + executionId + " progressed (" + ProgressFormatter.formatShort(progress) + ")");
    }

    @Override
    public void executionFailed(IOperationContext context, OperationExecutionPermId executionId, IOperationExecutionError error)
    {
        checkContext(context);
        checkExecutionId(executionId);
        checkError(error);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionFailed(executionId.getPermId(), error.getMessage());
        fsStore.executionFailed(executionId.getPermId(), error);

        operationLog.error("Execution " + executionId + " has failed");
        operationLog.error(error.getMessage());
    }

    @Override
    public void executionFinished(IOperationContext context, OperationExecutionPermId executionId, List<? extends IOperationResult> results)
    {
        checkContext(context);
        checkExecutionId(executionId);
        checkResults(results);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkExecution(executionId, execution);
        checkAccess(context, execution);

        List<String> resultsMessages = new ArrayList<String>();
        for (IOperationResult result : results)
        {
            resultsMessages.add(result.getMessage());
        }

        dbStore.executionFinished(executionId.getPermId(), resultsMessages);
        fsStore.executionFinished(executionId.getPermId(), results);

        operationLog.info("Execution " + executionId + " has finished");
    }

    @Override
    public void executionAvailability(IOperationContext context, OperationExecutionPermId executionId, OperationExecutionAvailability availability)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkAvailability(executionId, execution.getAvailability(), availability);
        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionAvailability(executionId.getPermId(), convertAvailability(availability));
        fsStore.executionAvailability(executionId.getPermId(), availability);

        operationLog.info("Execution " + executionId + " availability has changed to " + availability);
    }

    @Override
    public void executionSummaryAvailability(IOperationContext context, OperationExecutionPermId executionId,
            OperationExecutionAvailability summaryAvailability)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkAvailability(executionId, execution.getSummaryAvailability(), summaryAvailability);
        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionSummaryAvailability(executionId.getPermId(), convertAvailability(summaryAvailability));
        fsStore.executionSummaryAvailability(executionId.getPermId(), summaryAvailability);

        operationLog.info("Execution " + executionId + " summary availability has changed to " + summaryAvailability);
    }

    @Override
    public void executionDetailsAvailability(IOperationContext context, OperationExecutionPermId executionId,
            OperationExecutionAvailability detailsAvailability)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE execution = dbStore.getExecution(executionId.getPermId());

        checkAvailability(executionId, execution.getDetailsAvailability(), detailsAvailability);
        checkExecution(executionId, execution);
        checkAccess(context, execution);

        dbStore.executionDetailsAvailability(executionId.getPermId(), convertAvailability(detailsAvailability));
        fsStore.executionDetailsAvailability(executionId.getPermId(), detailsAvailability);

        operationLog.info("Execution " + executionId + " details availability has changed to " + detailsAvailability);
    }

    @Override
    public OperationExecution getExecution(IOperationContext context, IOperationExecutionId executionId, OperationExecutionFetchOptions fo)
    {
        checkContext(context);
        checkExecutionId(executionId);

        OperationExecutionPE executionPE = dbStore.getExecution(((OperationExecutionPermId) executionId).getPermId());

        if (executionPE != null)
        {
            return translate(context, executionPE, fo);
        }

        return null;
    }

    @Override
    public List<OperationExecution> getExecutions(IOperationContext context, OperationExecutionFetchOptions fetchOptions)
    {
        checkContext(context);

        List<OperationExecutionPE> executionPEs = dbStore.getExecutions();
        return translate(context, executionPEs, fetchOptions);
    }

    @Override
    public List<OperationExecution> getExecutionsToBeTimeOutPending(IOperationContext context, OperationExecutionFetchOptions fetchOptions)
    {
        checkContext(context);

        List<OperationExecutionPE> executionPEs = dbStore.getExecutionsToBeTimeOutPending();
        return translate(context, executionPEs, fetchOptions);
    }

    @Override
    public List<OperationExecution> getExecutionsToBeTimedOut(IOperationContext context, OperationExecutionFetchOptions fetchOptions)
    {
        checkContext(context);

        List<OperationExecutionPE> executionPEs = dbStore.getExecutionsToBeTimedOut();
        return translate(context, executionPEs, fetchOptions);
    }

    @Override
    public List<OperationExecution> getExecutionsToBeDeleted(IOperationContext context, OperationExecutionFetchOptions fetchOptions)
    {
        checkContext(context);

        List<OperationExecutionPE> executionPEs = dbStore.getExecutionsToBeDeleted();
        return translate(context, executionPEs, fetchOptions);
    }

    private List<OperationExecution> translate(IOperationContext context, Collection<OperationExecutionPE> executionPEs,
            OperationExecutionFetchOptions fetchOptions)
    {
        List<OperationExecution> executions = new ArrayList<OperationExecution>();

        for (OperationExecutionPE executionPE : executionPEs)
        {
            OperationExecution execution = translate(context, executionPE, fetchOptions);

            if (execution != null)
            {
                executions.add(execution);
            }
        }

        Collections.sort(executions, new Comparator<OperationExecution>()
            {
                @Override
                public int compare(OperationExecution o1, OperationExecution o2)
                {
                    return o1.getCreationDate().compareTo(o2.getCreationDate());
                }
            });

        return executions;
    }

    private OperationExecution translate(IOperationContext context, OperationExecutionPE executionPE, OperationExecutionFetchOptions fetchOptions)
    {
        if (false == canAccess(context, executionPE))
        {
            return null;
        }

        OperationExecutionPermId executionId = new OperationExecutionPermId(executionPE.getCode());

        OperationExecution execution = new OperationExecution();
        execution.setPermId(executionId);
        execution.setCode(executionPE.getCode());
        execution.setState(convertState(executionPE.getState()));
        execution.setDescription(executionPE.getDescription());
        execution.setAvailability(convertAvailability(executionPE.getAvailability()));
        execution.setAvailabilityTime(executionPE.getAvailabilityTime().intValue());
        execution.setSummaryAvailability(convertAvailability(executionPE.getSummaryAvailability()));
        execution.setSummaryAvailabilityTime(executionPE.getSummaryAvailabilityTime().intValue());
        execution.setDetailsAvailability(convertAvailability(executionPE.getDetailsAvailability()));
        execution.setDetailsAvailabilityTime(executionPE.getDetailsAvailabilityTime().intValue());
        execution.setCreationDate(executionPE.getCreationDate());
        execution.setStartDate(executionPE.getStartDate());
        execution.setFinishDate(executionPE.getFinishDate());
        execution.setFetchOptions(fetchOptions);

        if (fetchOptions != null)
        {
            if (fetchOptions.hasSummary())
            {
                OperationExecutionSummary summary = translateSummary(context, executionPE, fetchOptions.withSummary());
                execution.setSummary(summary);
            }

            if (fetchOptions.hasDetails())
            {
                OperationExecutionDetails details = translateDetails(context, executionPE, fetchOptions.withDetails());
                execution.setDetails(details);
            }
        }

        return execution;
    }

    private OperationExecutionSummary translateSummary(IOperationContext context, OperationExecutionPE executionPE,
            OperationExecutionSummaryFetchOptions fetchOptions)
    {
        OperationExecutionAvailability summaryAvailability = convertAvailability(executionPE.getSummaryAvailability());

        if (OperationExecutionAvailability.TIMED_OUT.equals(summaryAvailability)
                || OperationExecutionAvailability.DELETED.equals(summaryAvailability))
        {
            return null;
        }

        OperationExecutionSummary summary = new OperationExecutionSummary();
        summary.setFetchOptions(fetchOptions);

        if (fetchOptions.hasOperations())
        {
            summary.setOperations(executionPE.getSummaryOperationsList());
        }
        if (fetchOptions.hasProgress())
        {
            IProgress progress = null;

            synchronized (progressMap)
            {
                progress = progressMap.get(new OperationExecutionPermId(executionPE.getCode()));
            }

            if (progress != null)
            {
                summary.setProgress(ProgressFormatter.format(progress));
            } else
            {
                summary.setProgress(executionPE.getSummaryProgress());
            }
        }
        if (fetchOptions.hasError())
        {
            summary.setError(executionPE.getSummaryError());
        }
        if (fetchOptions.hasResults())
        {
            summary.setResults(executionPE.getSummaryResultsList());
        }

        return summary;
    }

    private OperationExecutionDetails translateDetails(IOperationContext context, OperationExecutionPE executionPE,
            OperationExecutionDetailsFetchOptions fetchOptions)
    {
        OperationExecutionAvailability detailsAvailability = convertAvailability(executionPE.getDetailsAvailability());

        if (OperationExecutionAvailability.TIMED_OUT.equals(detailsAvailability)
                || OperationExecutionAvailability.DELETED.equals(detailsAvailability))
        {
            return null;
        }

        OperationExecutionFSFetchOptions fetchOptionsFS = new OperationExecutionFSFetchOptions();

        if (fetchOptions.hasOperations())
        {
            fetchOptionsFS.withOperations();
        }
        if (fetchOptions.hasProgress())
        {
            fetchOptionsFS.withProgress();
        }
        if (fetchOptions.hasError())
        {
            fetchOptionsFS.withError();
        }
        if (fetchOptions.hasResults())
        {
            fetchOptionsFS.withResults();
        }

        OperationExecutionFS executionFS = fsStore.getExecution(executionPE.getCode(), fetchOptionsFS);

        if (executionFS == null)
        {
            return null;
        }

        OperationExecutionDetails details = new OperationExecutionDetails();
        details.setFetchOptions(fetchOptions);

        if (fetchOptions.hasOperations())
        {
            details.setOperations(executionFS.getOperations());
        }
        if (fetchOptions.hasProgress())
        {
            IProgress progress = null;

            synchronized (progressMap)
            {
                progress = progressMap.get(new OperationExecutionPermId(executionPE.getCode()));
            }

            if (progress != null)
            {
                details.setProgress(new OperationExecutionProgress(ProgressFormatter.format(progress),
                        progress.getNumItemsProcessed(), progress.getTotalItemsToProcess()));
            } else
            {
                details.setProgress(executionFS.getProgress());
            }
        }
        if (fetchOptions.hasError())
        {
            details.setError(executionFS.getError());
        }
        if (fetchOptions.hasResults())
        {
            details.setResults(executionFS.getResults());
        }

        return details;
    }

    private void checkContext(IOperationContext context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
    }

    private void checkExecutionId(IOperationExecutionId executionId)
    {
        if (executionId == null)
        {
            throw new IllegalArgumentException("Execution id cannot be null");
        }

        if (false == (executionId instanceof OperationExecutionPermId))
        {
            throw new UnsupportedObjectIdException(executionId);
        }
    }

    private void checkExecution(IOperationExecutionId executionId, OperationExecutionPE executionPE)
    {
        if (executionPE == null)
        {
            throw new IllegalArgumentException("Operation execution with id " + executionId + " does not exist in the database.");
        }
    }

    private void checkOperations(List<? extends IOperation> operations)
    {
        if (operations == null || operations.isEmpty())
        {
            throw new IllegalArgumentException("Operations cannot be null or empty");
        }
    }

    private void checkOptions(IOperationExecutionOptions options)
    {
        if (options == null)
        {
            throw new IllegalArgumentException("Options cannot be null");
        }
    }

    private void checkError(IOperationExecutionError error)
    {
        if (error == null)
        {
            throw new IllegalArgumentException("Error cannot be null");
        }
    }

    private void checkResults(List<? extends IOperationResult> results)
    {
        if (results == null || results.isEmpty())
        {
            throw new IllegalArgumentException("Results cannot be null or empty");
        }
    }

    private void checkAvailability(OperationExecutionPermId executionId,
            ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability previousCore,
            OperationExecutionAvailability next)
    {
        if (next == null)
        {
            throw new IllegalArgumentException("Opeation execution with id " + executionId + " cannot have availability set to null");
        }

        OperationExecutionAvailability previous = convertAvailability(previousCore);

        if (next.equals(previous))
        {
            return;
        } else if (false == next.hasPrevious(previous))
        {
            throw new IllegalArgumentException(
                    "Opeation execution with id " + executionId + " cannot have availability changed from " + previous + " to " + next);
        }
    }

    private ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState convertState(OperationExecutionState state)
    {
        return ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionState.valueOf(state.name());
    }

    private ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability convertAvailability(
            OperationExecutionAvailability availability)
    {
        return ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability.valueOf(availability.name());
    }

    private OperationExecutionAvailability convertAvailability(
            ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionAvailability availability)
    {
        return OperationExecutionAvailability.valueOf(availability.name());
    }

    private void checkAccess(IOperationContext context, OperationExecutionPE executionPE)
    {
        if (false == canAccess(context, executionPE))
        {
            throw new UnauthorizedObjectAccessException(new OperationExecutionPermId(executionPE.getCode()));
        }
    }

    private boolean canAccess(IOperationContext context, OperationExecutionPE executionPE)
    {
        PersonPE person = context.getSession().tryGetPerson();

        // users can see their own executions
        if (person.getId().equals(executionPE.getOwner().getId()))
        {
            return true;
        }

        // instance observer users and stronger can see all the executions
        AuthorizationServiceUtils authorization = new AuthorizationServiceUtils(null, context.getSession().tryGetPerson());
        return authorization.doesUserHaveRole(RoleCode.OBSERVER.toString(), null);
    }

    @Override
    public void run()
    {
        while (false == Thread.currentThread().isInterrupted())
        {
            Map<OperationExecutionPermId, IProgress> progressMapCopy = new HashMap<OperationExecutionPermId, IProgress>();

            synchronized (progressMap)
            {
                progressMapCopy.putAll(progressMap);
                progressMap.clear();
            }

            if (false == progressMapCopy.isEmpty())
            {
                operationLog.info("Progress synchronization with database and file system has been started (" + progressMapCopy.size()
                        + " execution(s) to be synchronized).");

                int successCount = 0;
                int failureCount = 0;

                for (Map.Entry<OperationExecutionPermId, IProgress> progressEntry : progressMapCopy.entrySet())
                {
                    OperationExecutionPermId executionId = progressEntry.getKey();
                    IProgress progress = progressEntry.getValue();

                    try
                    {
                        executionProgressed(executionId, progress);

                        successCount++;

                    } catch (Throwable t)
                    {
                        operationLog.error("Couldn't synchronize progress for execution with id " + executionId, t);
                        failureCount++;
                    }
                }

                operationLog.info("Progress synchronization with database and file system has been finished (" + successCount
                        + " execution(s) has been successfully synchronized, synchronization of " + failureCount + " execution(s) has failed).");
            }
            try
            {
                Thread.sleep(config.getProgressInterval() * 1000);
            } catch (InterruptedException ex)
            {
                return;
            }
        }
    }

    public void shutdown()
    {
        if (progressThread != null)
        {
            progressThread.interrupt();
        }
    }

}
