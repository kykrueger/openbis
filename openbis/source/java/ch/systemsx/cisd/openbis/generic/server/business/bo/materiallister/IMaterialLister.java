/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * A class for fast material listing.
 * 
 * @author Tomasz Pylak
 */
public interface IMaterialLister
{
    /**
     * Returns a sorted list of {@link Material}s matching given criteria.
     * 
     * @param withProperties if true material properties will be fetched as well.
     */
    public List<Material> list(ListMaterialCriteria criteria, boolean withProperties);
}
