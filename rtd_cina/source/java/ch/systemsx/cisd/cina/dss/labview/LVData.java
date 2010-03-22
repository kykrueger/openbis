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

package ch.systemsx.cisd.cina.dss.labview;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@XmlRootElement(name = "LVData", namespace = "http://www.ni.com/LVData")
public class LVData
{
    private String version;

    private List<Cluster> clusters;

    @XmlElement(name = "Version", namespace = "http://www.ni.com/LVData")
    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    @XmlElement(name = "Cluster", namespace = "http://www.ni.com/LVData")
    public List<Cluster> getClusters()
    {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters)
    {
        this.clusters = clusters;
    }

}
