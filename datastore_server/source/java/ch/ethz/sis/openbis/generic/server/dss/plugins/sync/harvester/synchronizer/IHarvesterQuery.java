/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * @author Franz-Josef Elmer
 */
public interface IHarvesterQuery extends BaseQuery
{
    public static final String UPDATE2 =
            "set registration_timestamp = ?{1.registrationTimestamp}, pers_id_registerer = ?{1.registratorId} "
                    + "where perm_id = ?{1.permId}";

    @Select("select id,user_id as userId from persons")
    public List<PersonRecord> listAllUsers();

    @Select("select id,code from material_types")
    public List<MaterialTypeRecord> listAllMaterialTypes();

    @Update(sql = "update materials set registration_timestamp = ?{1.registrationTimestamp}, pers_id_registerer = ?{1.registratorId} "
            + "where code = ?{1.permId} and maty_id = ?{1.typeId}", batchUpdate = true)
    public void updateMaterialRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update projects " + UPDATE2, batchUpdate = true)
    public void updateProjectRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update experiments_all " + UPDATE2, batchUpdate = true)
    public void updateExperimentRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update samples_all " + UPDATE2, batchUpdate = true)
    public void updateSampleRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update data_all set registration_timestamp = ?{1.registrationTimestamp}, pers_id_registerer = ?{1.registratorId} "
            + "where code = ?{1.permId}", batchUpdate = true)
    public void updateDataSetRegistrations(List<RegistrationDTO> registrations);
}
