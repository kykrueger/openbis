/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Result of a search on a search domain.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("SearchDomainSearchResult")
public class SearchDomainSearchResult implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String searchDomain;
    
    private ISearchDomainResultLocation resultLocation;

    public String getSearchDomain()
    {
        return searchDomain;
    }
    
    public void setSearchDomain(String searchDomain)
    {
        this.searchDomain = searchDomain;
    }
    
    public ISearchDomainResultLocation getResultLocation()
    {
        return resultLocation;
    }

    public void setResultLocation(ISearchDomainResultLocation resultLocation)
    {
        this.resultLocation = resultLocation;
    }

    @Override
    public String toString()
    {
        return "Search Domain: " + getSearchDomain() + ", Result location: [" + resultLocation + "]";
    }

}
