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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFile;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpNonExistingFile;

class V3ExperimentLevelResolver implements V3Resolver
{
    private IExperimentId experimentId;

    public V3ExperimentLevelResolver(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    @Override
    public V3FtpFile resolve(String fullPath, String[] subPath, FtpPathResolverContext context)
    {
        if (subPath.length == 0)
        {
            ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
            fetchOptions.withDataSets();

            Map<IExperimentId, Experiment> experiments =
                    context.getV3Api().getExperiments(context.getSessionToken(), Collections.singletonList(experimentId), fetchOptions);
            Experiment exp = experiments.get(experimentId);

            if (exp == null)
            {
                return new V3FtpNonExistingFile(fullPath, null);
            }

            V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
            for (DataSet dataSet : exp.getDataSets())
            {
                response.addDirectory(dataSet.getCode());
            }
            return response;
        } else
        {
            String dataSetCode = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            return new V3DataSetContentResolver(dataSetCode).resolve(fullPath, remaining, context);
        }
    }
}