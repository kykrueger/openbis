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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import javax.xml.bind.annotation.XmlElement;

/**
 * <pre>
 *   &lt;ExpandedLaneSummary&gt;
 *      &lt;Read&gt;
 *      &lt;/Read&gt;
 *   &lt;/ExpandedLaneSummary&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

class LaneResultsSummary
{
    private Read read;

    @XmlElement(name = "Read")
    public Read getRead()
    {
        return read;
    }

    public void setRead(Read read)
    {
        this.read = read;
    }
}
