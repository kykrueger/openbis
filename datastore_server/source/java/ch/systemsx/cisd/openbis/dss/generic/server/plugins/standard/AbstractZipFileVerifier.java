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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import de.schlichtherle.util.zip.ZipFile;

/**
 * @author anttil
 */
public abstract class AbstractZipFileVerifier implements IArchiveFileVerifier
{

    public abstract List<String> verify(ZipFile file);

    @Override
    public final List<String> verify(File file)
    {
        List<String> errors = new ArrayList<String>();
        ZipFile zip;
        try
        {
            zip = new ZipFile(file);
        } catch (IOException ex)
        {
            errors.add("Reading zip file failed: " + ex.getMessage());
            return errors;
        }

        return verify(zip);
    }

    protected long calculateCRC32(InputStream input) throws IOException
    {
        BufferedInputStream inStream = new BufferedInputStream(input);
        int BLOCK_SIZE = 128 * 1024;
        int len;
        byte[] buffer = new byte[BLOCK_SIZE];

        CRC32 crc32 = new CRC32();
        crc32.reset();

        while ((len = inStream.read(buffer, 0, BLOCK_SIZE)) > 0)
        {
            crc32.update(buffer, 0, len);
            buffer = new byte[BLOCK_SIZE];
        }

        return crc32.getValue();
    }
}
