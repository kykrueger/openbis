/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.util.Arrays;
import java.util.Collection;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.Credentials;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.ICredentials;

import ch.systemsx.cisd.openbis.knime.common.AbstractTestNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.OpenbisServiceFacadeFactory;

/**
 * Stand alone application for testing {@link DataSetRegistrationNodeDialog}.
 *
 * @author Franz-Josef Elmer
 */
public class TestDataSetRegistrationNodeDialog extends AbstractTestNodeDialog
{

    public static void main(String[] args) throws NotConfigurableException
    {
        AbstractTestNodeDialog.createAndShow(new TestDataSetRegistrationNodeDialog());
    }

    @Override
    public NodeDialogPane create() throws NotConfigurableException
    {
        return new DataSetRegistrationNodeDialog(new OpenbisServiceFacadeFactory())
            {
                {
                    NodeSettings settings = createSettings();
                    loadSettingsFrom(settings, (PortObjectSpec[]) null);
                }

                @Override
                protected String[] getUrls()
                {
                    return new String[] { "http://localhost:8888" };
                }

                @Override
                protected Collection<String> getAllCredentialsNames()
                {
                    return Arrays.asList("test");
                }

                @Override
                protected ICredentials getCredentials()
                {
                    return new Credentials("_", "test", "a");
                }

                @Override
                protected boolean withFileVariable()
                {
                    return true;
                }

                @Override
                protected Collection<FlowVariable> getFlowVariables()
                {
                    return Arrays.asList(new FlowVariable("answer", 42),
                            new FlowVariable("fname", "here/and/there"),
                            new FlowVariable("file-name", "this/and/that"));
                }
            };
    }
}
