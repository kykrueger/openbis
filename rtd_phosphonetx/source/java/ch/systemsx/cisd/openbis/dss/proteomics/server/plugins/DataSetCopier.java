/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.proteomics.server.plugins;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.DESTINATION_KEY;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDropboxProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetCopier extends AbstractDropboxProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    public DataSetCopier(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory(), new SshCommandExecutorFactory());
    }

    @Private
    DataSetCopier(Properties properties, File storeRoot, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        super(properties, storeRoot, new LocalAndRemoteCopier(properties, pathCopierFactory,
                sshCommandExecutorFactory));
    }

    @Override
    protected String getProcessingDescription(DatasetDescription dataset,
            DataSetProcessingContext context)
    {
        return "Copy to " + properties.getProperty(DESTINATION_KEY);
    }

}
