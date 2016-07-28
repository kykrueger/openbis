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
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3.file.V3FtpFile;

class V3RootLevelResolver extends V3Resolver
{
    public V3RootLevelResolver(FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
    }

    @Override
    public V3FtpFile resolve(String fullPath, String[] subPath)
    {
        if (subPath.length == 0)
        {
            List<Space> spaces =
                    api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions()).getObjects();

            V3FtpDirectoryResponse response = new V3FtpDirectoryResponse(fullPath);
            for (Space space : spaces)
            {
                response.addDirectory(space.getCode());
            }
            response.addDirectory("__PLUGIN__");
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);

            if (item.equals("__PLUGIN__"))
            {
                V3PluginResolver resolver = new V3PluginResolver(resolverContext);
                return resolver.resolve(fullPath, remaining);
            } else
            {
                V3SpaceLevelResolver resolver = new V3SpaceLevelResolver(item, resolverContext);
                return resolver.resolve(fullPath, remaining);
            }
        }
    }
}