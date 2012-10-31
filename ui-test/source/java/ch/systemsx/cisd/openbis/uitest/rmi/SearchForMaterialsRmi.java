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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.help.Lambda;
import ch.systemsx.cisd.openbis.uitest.rmi.eager.MaterialRmi;
import ch.systemsx.cisd.openbis.uitest.type.Material;

/**
 * @author anttil
 */
public class SearchForMaterialsRmi implements Command<List<Material>>
{
    @Inject
    private String session;

    @Inject
    private IGeneralInformationService generalInformationService;

    @Inject
    private ICommonServer commonServer;

    private String code;

    public SearchForMaterialsRmi(String code)
    {
        this.code = code;
    }

    @Override
    public List<Material> execute()
    {
        SearchCriteria criteria = new SearchCriteria();
        criteria.addMatchClause(
                MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));

        return Lambda.foreach(
                generalInformationService.searchForMaterials(session, criteria),
                new Lambda<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material, Material>()
                    {
                        @Override
                        public Material apply(
                                ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material input)
                        {
                            return new MaterialRmi(input, session, commonServer);
                        }
                    }
                );
    }
}
