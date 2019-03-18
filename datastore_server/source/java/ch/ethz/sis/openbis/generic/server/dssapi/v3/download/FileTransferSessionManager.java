/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3.download;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.InvalidUserSessionException;

/**
 * @author Franz-Josef Elmer
 *
 */
@Component
public class FileTransferSessionManager implements IFileTransferSessionManager
{
    private final Map<String, String> sessionTokenBySessionId = new HashMap<>();

    @Override
    public void validateBeforeDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
    {
        validateDuringDownload(userSessionId);
    }

    @Override
    public void validateDuringDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
    {
        if (sessionTokenBySessionId.containsKey(userSessionId.getId()) == false)
        {
            throw new InvalidUserSessionException(userSessionId);
        }
    }

    @Override
    public String createFileTransferUserSessionId(String sessionToken)
    {
        String fileTransferUserSessionId = UUID.randomUUID().toString();
        sessionTokenBySessionId.put(fileTransferUserSessionId, sessionToken);
        return fileTransferUserSessionId;
    }
    
    @Override
    public String getSessionToken(String fileTransferUserSessionId)
    {
        return sessionTokenBySessionId.get(fileTransferUserSessionId);
    }

}
