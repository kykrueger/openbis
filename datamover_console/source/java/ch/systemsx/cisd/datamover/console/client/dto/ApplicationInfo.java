/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Franz-Josef Elmer
 */
public class ApplicationInfo implements IsSerializable
{
    private String version;

    private int refreshTimeInterval;

    public final String getVersion()
    {
        return version;
    }

    public final void setVersion(String version)
    {
        this.version = version;
    }

    public final int getRefreshTimeInterval()
    {
        return refreshTimeInterval;
    }

    public final void setRefreshTimeInterval(int refreshTimeInterval)
    {
        this.refreshTimeInterval = refreshTimeInterval;
    }
}
