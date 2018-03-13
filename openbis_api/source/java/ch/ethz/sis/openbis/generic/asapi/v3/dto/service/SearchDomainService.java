/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service;

import java.io.Serializable;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.SearchDomainServiceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.service.SearchDomainService")
public class SearchDomainService implements Serializable
{
    private static final long serialVersionUID = 1L;

    private SearchDomainServiceFetchOptions fetchOptions;
    
    private String name;

    private String label;
    
    private String possibleSearchOptionsKey;
    
    private List<SearchDomainServiceParameter> parameters;

    public SearchDomainServiceFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(SearchDomainServiceFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getPossibleSearchOptionsKey()
    {
        return possibleSearchOptionsKey;
    }

    public void setPossibleSearchOptionsKey(String possibleSearchOptionsKey)
    {
        this.possibleSearchOptionsKey = possibleSearchOptionsKey;
    }

    public List<SearchDomainServiceParameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<SearchDomainServiceParameter> parameters)
    {
        this.parameters = parameters;
    }
    
    @Override
    public String toString()
    {
        return "SearchDomainService: " + name;
    }
}
