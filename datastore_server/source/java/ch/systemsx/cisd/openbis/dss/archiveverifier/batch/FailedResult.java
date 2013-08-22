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

package ch.systemsx.cisd.openbis.dss.archiveverifier.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Result of verification with errors.
 * 
 * @author anttil
 */
public class FailedResult implements IResult
{

    private final File file;

    private final List<String> errors;

    public FailedResult()
    {
        this(null, new ArrayList<String>());
    }

    public FailedResult(String error)
    {
        this(null, Arrays.asList(error));
    }

    public FailedResult(File file, List<String> errors)
    {
        this.file = file;
        this.errors = errors;
    }

    @Override
    public boolean success()
    {
        return false;
    }

    @Override
    public String getFile()
    {
        if (this.file != null)
        {
            return file.getAbsolutePath();
        } else
        {
            return null;
        }
    }

    @Override
    public List<String> getErrors()
    {
        return this.errors;
    }

    @Override
    public String toString()
    {
        return "FailedResult: " + file + ": " + errors;
    }

}
