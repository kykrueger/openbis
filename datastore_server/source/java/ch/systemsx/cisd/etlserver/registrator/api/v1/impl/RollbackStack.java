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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Queue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;

/**
 * A package internal class to manage the rollback stack.
 * <p>
 * Since there is no persistent stack class, the rollback stack is implemented with two queues,
 * optimized for pushing onto the stack (poping from the stack is slower).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class RollbackStack
{
    // The files that store the persistent queue. Used for discarding the queues.
    private final File queue1File;

    private final File queue2File;

    // These are not final because they get swapped around.
    private Queue<StackElement> liveLifo;

    private Queue<StackElement> tempLifo;

    /**
     * Constructor for a rollback stack that uses queue1File and queue2File for the persistent
     * queues.
     * 
     * @param queue1File File for a persistent queue
     * @param queue2File File for a persistent queue
     */
    public RollbackStack(File queue1File, File queue2File)
    {
        this.queue1File = queue1File;
        this.queue2File = queue2File;

        Queue<StackElement> queue1 =
                ExtendedBlockingQueueFactory.createPersistRecordBased(queue1File, 16, false);
        Queue<StackElement> queue2 =
                ExtendedBlockingQueueFactory.createPersistRecordBased(queue2File, 16, false);

        // If both queues are empty, it doesn't matter which is which
        if (bothQueuesAreEmpty(queue1, queue2))
        {
            liveLifo = queue1;
            tempLifo = queue2;
        } else
        {
            // The live one should be the non-empty one
            if (queue2.isEmpty())
            {
                liveLifo = queue1;
                tempLifo = queue2;
            } else if (queue1.isEmpty())
            {
                liveLifo = queue2;
                tempLifo = queue1;
            } else
            {
                // Both queues are non-empty -- this means a shutdown happened during a rollback.
                // One queue should have an element at the head with order 0. Take this to be the
                // temp queue at the time of shutdown and continue from there
                if (queue2.peek().order == 0)
                {
                    liveLifo = queue1;
                    tempLifo = queue2;
                } else
                {
                    assert queue1.peek().order == 0;
                    liveLifo = queue2;
                    tempLifo = queue1;
                }

                // Finish moving elements from the live to the temp queue
                while (liveLifo.size() > 0)
                {
                    // Put it into the temp
                    tempLifo.add(liveLifo.peek());
                    // Remove from the live
                    liveLifo.remove();
                }

                // Finish initialization -- make the temp queue the live one
                swapStacks();
            }
        }
    }

    /**
     * The size of the rollback stack
     */
    public int size()
    {
        return liveLifo.size();
    }

    /**
     * Push the command onto the stack and execute it.
     */
    public void pushAndExecuteCommand(ITransactionalCommand cmd)
    {
        // Push is simple -- just put the new command onto the live stack
        StackElement elt = new StackElement(cmd, liveLifo.size());
        liveLifo.add(elt);
        cmd.execute();
    }

    /**
     * Rollback the top of the stack and pop it from the stack
     */
    public ITransactionalCommand rollbackAndPop()
    {
        // Pop is a bit more complicated, since the element we want to pop, the *head* of he stack,
        // is at the *tail* of the queue. We first need to move all other elements to the temp stack
        // and do this such that an interruption of the process does not result in any loss of data
        // (it could result in a duplication, though).

        // The stack is empty -- return null;
        if (liveLifo.size() < 1)
        {
            return null;
        }

        // Get all but the last element from the queue
        while (liveLifo.size() > 1)
        {
            // Put it into the temp
            tempLifo.add(liveLifo.peek());
            // Remove from the live
            liveLifo.remove();
        }

        // This is the tail of the queue, i.e., the head of the stack.
        StackElement elt = liveLifo.peek();

        try
        {
            // Roll it back
            elt.command.rollback();
        } catch (Throwable ex)
        {
            Logger operationLog = getOperationLog();
            // If any problems happen rolling back a command, log them
            operationLog.error("Encountered error rolling back command " + elt.toString(), ex);
        }

        // Remove it from the live stack
        liveLifo.remove();

        // Make the live the temp
        swapStacks();

        // return the command
        return elt.command;
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of
     * execution.
     */
    public void rollbackAll()
    {
        getOperationLog().info("Rolling back stack " + this);
        // Pop and rollback all
        while (size() > 0)
        {
            rollbackAndPop();
        }
    }

    /**
     * Throws away the persistent stack.
     */
    public void discard()
    {
        liveLifo = null;
        tempLifo = null;
        queue1File.delete();
        queue2File.delete();
    }

    /**
     * A stack element combines the command with an order. The order is used to implement the
     * ordering in the queue.
     * <p>
     * The queue of rollback actions should be LIFO (a stack), but there is no LIFO queue in the
     * available libraries that we can use for this purpose. Thus, we use a priority queue, which is
     * available, and define the priority comparison operator such that it results in a LIFO queue.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class StackElement implements Serializable, Comparable<StackElement>
    {
        private static final long serialVersionUID = 1L;

        private final ITransactionalCommand command;

        private final int order;

        private StackElement(ITransactionalCommand command, int order)
        {
            this.command = command;
            this.order = order;
        }

        public int compareTo(StackElement o)
        {
            // The order should be the reverse of the step order (later steps should come first in
            // the ordering).
            if (o.order < this.order)
            {
                return -1;
            } else if (o.order > this.order)
            {
                return 1;
            } else
            {
                return 0;
            }
        }

        @Override
        public String toString()
        {
            return "StackElement [command=" + command + ", order=" + order + "]";
        }

    }

    /**
     * Make the temp stack the live one.
     */
    private void swapStacks()
    {
        Queue<StackElement> swap = liveLifo;
        liveLifo = tempLifo;
        tempLifo = swap;
    }

    private static boolean bothQueuesAreEmpty(Queue<StackElement> queue1, Queue<StackElement> queue2)
    {
        return queue1.isEmpty() && queue2.isEmpty();
    }

    private Logger getOperationLog()
    {
        return DataSetRegistrationTransaction.operationLog;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(liveLifo.toArray());
        return sb.toString();
    }

    /**
     * Do not call this method, it will leave the queue in an incorrect state, but we want to do
     * this for testing purposes.
     * 
     * @deprecated
     */
    @Deprecated
    void doNotCallJustForTesting_MoveOneElement()
    {
        // Put it into the temp
        tempLifo.add(liveLifo.peek());
        // Remove from the live
        liveLifo.remove();
    }

    /**
     * Do not call this method, it will leave the queue in an incorrect state, but we want to do
     * this for testing purposes.
     * 
     * @deprecated
     */
    @Deprecated
    void doNotCallJustForTesting_PartiallyMoveOneElement()
    {
        // Put it into the temp
        tempLifo.add(liveLifo.peek());
        // DON'T Remove from the live
        // liveLifo.remove();
    }
}
