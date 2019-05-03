/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

/**
 * @author Viktor Kovtun
 */
public class MaterialSearchManager extends AbstractSearchManager<IMaterialLister>
{

    public MaterialSearchManager(final ISQLSearchDAO searchDAO, final IMaterialLister lister)
    {
        super(searchDAO, lister);
    }

    public List<Material> searchForMaterials(final String userId, final MaterialSearchCriteria criteria)
            throws DataAccessException
    {
        List<Long> materialIds = searchDAO.searchForEntityIds(userId, criteria,
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL,
                Collections.emptyList());

        return lister.list(ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }

}
