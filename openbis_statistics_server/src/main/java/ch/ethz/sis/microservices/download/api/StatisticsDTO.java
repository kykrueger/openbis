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

package ch.ethz.sis.microservices.download.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class StatisticsDTO implements Serializable
{
    private static final long serialVersionUID = -2984621476335382352L;

    private String serverId;

    private Long submissionTimestamp;

    private int totalUsersCount;

    private int activeUsersCount;

    private String idAddress;

    private String geolocation;

    public String getServerId()
    {
        return serverId;
    }

    public void setServerId(final String serverId)
    {
        this.serverId = serverId;
    }

    public Long getSubmissionTimestamp()
    {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(final Long submissionTimestamp)
    {
        this.submissionTimestamp = submissionTimestamp;
    }

    public int getTotalUsersCount()
    {
        return totalUsersCount;
    }

    public void setTotalUsersCount(final int totalUsersCount)
    {
        this.totalUsersCount = totalUsersCount;
    }

    public int getActiveUsersCount()
    {
        return activeUsersCount;
    }

    public void setActiveUsersCount(final int activeUsersCount)
    {
        this.activeUsersCount = activeUsersCount;
    }

    public String getIdAddress()
    {
        return idAddress;
    }

    public void setIdAddress(final String idAddress)
    {
        this.idAddress = idAddress;
    }

    public String getGeolocation()
    {
        return geolocation;
    }

    public void setGeolocation(final String geolocation)
    {
        this.geolocation = geolocation;
    }
}
