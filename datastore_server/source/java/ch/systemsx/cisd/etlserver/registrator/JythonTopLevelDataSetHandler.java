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

import java.io.File;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandler<T extends DataSetInformation> extends
        AbstractOmniscientTopLevelDataSetRegistrator<T>
{
    /**
     * The name of the function to define to hook into the service rollback mechanism.
     */
    private static final String ROLLBACK_SERVICE_FUNCTION_NAME = "rollback_service";

    /**
     * The name of the function to define to hook into the transaction rollback mechanism.
     */
    private static final String ROLLBACK_TRANSACTION_FUNCTION_NAME = "rollback_transaction";

    private static final String FACTORY_VARIABLE_NAME = "factory";

    /**
     * The name of the local variable under which the service is made available to the script.
     */
    private static final String SERVICE_VARIABLE_NAME = "service";

    /**
     * The name of the local variable under which the global state
     */
    private static final String STATE_VARIABLE_NAME = "state";

    /**
     * The name of the local variable under which the incoming directory is made available to the
     * script.
     */
    private static final String INCOMING_DATA_SET_VARIABLE_NAME = "incoming";

    // The key for the script in the properties file
    public static final String SCRIPT_PATH_KEY = "script-path";

    private final File scriptFile;

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        String path =
                PropertyUtils.getMandatoryProperty(globalState.getThreadParameters()
                        .getThreadProperties(), SCRIPT_PATH_KEY);
        scriptFile = new File(path);
        if (scriptFile.isFile() == false)
        {
            throw ConfigurationFailureException.fromTemplate("Script file '%s' does not exist!",
                    path);
        }

    }

    @Override
    protected void handleDataSet(File dataSetFile, DataSetRegistrationService<T> genericService)
            throws Throwable
    {
        // Load the script
        String scriptString = FileUtilities.loadToString(scriptFile);

        JythonDataSetRegistrationService<T> service =
                (JythonDataSetRegistrationService<T>) genericService;

        // Configure the evaluator
        PythonInterpreter interpreter = service.interpreter;
        interpreter.set(SERVICE_VARIABLE_NAME, service);
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());
        interpreter.set(FACTORY_VARIABLE_NAME, service.getDataSetRegistrationDetailsFactory());

        try
        {
            // Invoke the evaluator
            interpreter.exec(scriptString);
        } catch (Throwable ex)
        {
            operationLog
                    .error(String
                            .format("Cannot register dataset from a file '%s'. Error in jython dropbox has occured:\n%s",
                                    dataSetFile.getPath(), ex.toString()));
            throw ex;
        }
    }

    /**
     * Create a registration service that includes a python interpreter (we need the interpreter in
     * the service so we can use it in error handling).
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            File incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());
        JythonDataSetRegistrationService<T> service =
                createJythonDataSetRegistrationService(incomingDataSetFile,
                        callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                        interpreter);
        return service;
    }

    /**
     * Create a Jython registration service that includes access to the interpreter.
     */
    protected JythonDataSetRegistrationService<T> createJythonDataSetRegistrationService(
            File incomingDataSetFile, DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter)
    {
        JythonDataSetRegistrationService<T> service =
                new JythonDataSetRegistrationService<T>(this, incomingDataSetFile,
                        userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                        interpreter);
        return service;
    }

    @Override
    protected void rollback(DataSetRegistrationService<T> service, Throwable throwable)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function = tryJythonFunction(interpreter, ROLLBACK_SERVICE_FUNCTION_NAME);
        if (null != function)
        {
            invokeRollbackServiceFunction(function, service, throwable);
        }

        super.rollback(service, throwable);
    }

    @Override
    public void rollbackTransaction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
    {
        invokeRollbackTransactionFunction(service, transaction, algorithmRunner, ex);
        super.rollbackTransaction(service, transaction, algorithmRunner, ex);
    }

    private void invokeRollbackTransactionFunction(DataSetRegistrationService<T> service,
            DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable ex)
    {
        PythonInterpreter interpreter = getInterpreterFromService(service);
        PyFunction function = tryJythonFunction(interpreter, ROLLBACK_TRANSACTION_FUNCTION_NAME);
        if (null != function)
        {
            invokeRollbackTransactionFunction(function, service, transaction, algorithmRunner, ex);
        } else
        {
            // No Rollback transaction function was called, see if the rollback service function was
            // defined, and call it.
            function = tryJythonFunction(interpreter, ROLLBACK_SERVICE_FUNCTION_NAME);
            if (null != function)
            {
                invokeRollbackServiceFunction(function, service, ex);
            }
        }
    }

    private PyFunction tryJythonFunction(PythonInterpreter interpreter, String functionName)
    {
        try
        {
            PyFunction function = (PyFunction) interpreter.get(functionName, PyFunction.class);
            return function;
        } catch (Exception e)
        {
            return null;
        }

    }

    /**
     * Pulled out as a separate method so tests can hook in.
     */
    protected void invokeRollbackServiceFunction(PyFunction function,
            DataSetRegistrationService<T> service, Throwable throwable)
    {
        function.__call__(Py.java2py(service), Py.java2py(throwable));
    }

    /**
     * Pulled out as a separate method so tests can hook in.
     */
    protected void invokeRollbackTransactionFunction(PyFunction function,
            DataSetRegistrationService<T> service, DataSetRegistrationTransaction<T> transaction,
            DataSetStorageAlgorithmRunner<T> algorithmRunner, Throwable throwable)
    {
        function.__call__(Py.java2py(service), Py.java2py(transaction),
                Py.java2py(algorithmRunner), Py.java2py(throwable));
    }

    /**
     * Pulled out as a separate method so tests can hook in.
     */
    protected void invokeRollbackDataSetRegistrationFunction(PyFunction function,
            DataSetRegistrationService<T> service,
            DataSetRegistrationAlgorithm registrationAlgorithm, Throwable throwable)
    {
        function.__call__(Py.java2py(service), Py.java2py(registrationAlgorithm),
                Py.java2py(throwable));
    }

    /**
     * Set the factory available to the python script. Subclasses may want to override.
     */
    @SuppressWarnings("unchecked")
    protected IDataSetRegistrationDetailsFactory<T> createObjectFactory(
            PythonInterpreter interpreter, DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return (IDataSetRegistrationDetailsFactory<T>) new JythonObjectFactory<DataSetInformation>(
                getRegistratorState(), userProvidedDataSetInformationOrNull)
            {
                @Override
                protected DataSetInformation createDataSetInformation()
                {
                    return new DataSetInformation();
                }
            };
    }

    public abstract static class JythonObjectFactory<T extends DataSetInformation> extends
            AbstractDataSetRegistrationDetailsFactory<T>
    {
        public JythonObjectFactory(OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails<T> createRegistrationDetails()
        {
            return createDataSetRegistrationDetails();
        }
    }

    protected static class JythonDataSetRegistrationService<T extends DataSetInformation> extends
            DataSetRegistrationService<T>
    {
        private final PythonInterpreter interpreter;

        /**
         * @param registrator
         * @param globalCleanAfterwardsAction
         */
        public JythonDataSetRegistrationService(JythonTopLevelDataSetHandler<T> registrator,
                File incomingDataSetFile, DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate, PythonInterpreter interpreter)
        {
            super(registrator, incomingDataSetFile, registrator.createObjectFactory(interpreter,
                    userProvidedDataSetInformationOrNull), globalCleanAfterwardsAction, delegate);
            this.interpreter = interpreter;
        }

        public PythonInterpreter getInterpreter()
        {
            return interpreter;
        }
    }

    protected PythonInterpreter getInterpreterFromService(DataSetRegistrationService<T> service)
    {
        PythonInterpreter interpreter = ((JythonDataSetRegistrationService<T>) service).interpreter;
        return interpreter;
    }
}
