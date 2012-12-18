/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IAtomicEvaluation;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IEvaluationRunner;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyFunctions;

/**
 * Pool of Jython Evaluators for managed properties. When the pool is created, it is filled with new
 * Evaluator instances for every managed property script. Enables thread-safe execution of calls to
 * python functions defined in these scripts.
 * 
 * @author anttil
 */
public class JythonEvaluatorPool
{
    private static Logger log = LogFactory.getLogger(LogCategory.OPERATION,
            JythonEvaluatorPool.class);

    public static int DEFAULT_POOL_SIZE = 100;

    public static JythonEvaluatorPool INSTANCE;

    private Map<String, EvaluatorState> cache;

    private Lock cacheLock;

    public JythonEvaluatorPool(IDAOFactory daoFactory, String poolSize)
    {
        this(daoFactory, createCache(poolSize));
    }

    public JythonEvaluatorPool(IDAOFactory daoFactory, Map<String, EvaluatorState> cache)
    {
        this.cache = cache;
        this.cacheLock = new ReentrantLock();

        for (ScriptPE script : daoFactory.getScriptDAO().listEntities(ScriptType.MANAGED_PROPERTY,
                null))
        {
            try
            {
                cache.put(script.getScript(),
                        new EvaluatorState(createEvaluatorFor(script.getScript())));
            } catch (EvaluatorException e)
            {
                log.warn("Could not create evaluator for script " + script.getName(), e);
            }
        }
        log.info("Initialization successful with " + cache.size() + " evaluators");
        INSTANCE = this;
    }

    /**
     * Return runner that can be used to evaluate python functions using evaluator in the pool.
     */
    public IEvaluationRunner getRunner(final String script)
    {
        return new IEvaluationRunner()
            {
                @Override
                public <T> T evaluate(IAtomicEvaluation<T> evaluation)
                {
                    return JythonEvaluatorPool.this.evaluate(script, evaluation);
                }
            };
    }

    /**
     * Evaluate python functions from a script using an Evaluator instance in the pool. If the
     * Evaluator instance does not exist, create it. Give access to the instance for only one thread
     * at a time.
     */
    private <T> T evaluate(String script, IAtomicEvaluation<T> evaluation)
    {
        EvaluatorState state = cache.get(script);
        if (state == null)
        {
            cacheLock.lock();
            try
            {
                state = cache.get(script);
                if (state == null)
                {
                    state = new EvaluatorState(createEvaluatorFor(script));
                    cache.put(script, state);
                }
            } finally
            {
                cacheLock.unlock();
            }
        }

        Lock lock = state.getLock();
        lock.lock();
        try
        {
            return evaluation.evaluate(state.getCleanInstance());
        } finally
        {
            lock.unlock();
        }
    }

    private Evaluator createEvaluatorFor(String script)
    {
        return new Evaluator("", ManagedPropertyFunctions.class, script);
    }

    /**
     * The pooled object. Contains the Evaluator and its initial state to which it is always
     * returned to when a new evaluation is started.
     */
    public static class EvaluatorState
    {
        private final Evaluator evaluator;

        private final Collection<String> globals;

        private final Lock lock;

        public EvaluatorState(Evaluator evaluator)
        {
            this.evaluator = evaluator;
            this.globals = evaluator.getGlobalVariables();
            this.lock = new ReentrantLock();
        }

        /**
         * Returns an evaluator instance in its initial state.
         */
        public synchronized Evaluator getCleanInstance()
        {
            for (String value : evaluator.getGlobalVariables())
            {
                if (globals.contains(value) == false)
                {
                    evaluator.delete(value);
                }
            }
            return this.evaluator;
        }

        public Lock getLock()
        {
            return lock;
        }
    }

    public static Map<String, EvaluatorState> createCache(String poolSize)
    {
        int size = DEFAULT_POOL_SIZE;
        try
        {
            int number = Integer.parseInt(poolSize);
            if (number > 0)
            {
                size = number;
            }
        } catch (NumberFormatException e)
        {
        }

        return createCache(size);
    }

    public static Map<String, EvaluatorState> createCache(final int poolSize)
    {
        return new LinkedHashMap<String, EvaluatorState>(16, 0.75f, true)
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, EvaluatorState> eldest)
                {
                    return size() > poolSize;
                }
            };
    }
}
