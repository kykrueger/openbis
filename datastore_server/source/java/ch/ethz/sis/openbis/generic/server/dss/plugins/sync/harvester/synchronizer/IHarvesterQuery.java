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
    public static final String SETTERS_WITHOUT_MODIFIER = "set modification_timestamp = ?{1.modificationTimestamp}, "
            + "registration_timestamp = ?{1.registrationTimestamp}, "
            + "pers_id_registerer = ?{1.registratorId} ";
    public static final String SETTERS_WITH_MODIFIER = SETTERS_WITHOUT_MODIFIER + ", pers_id_modifier = ?{1.modifierId} ";

    @Select("select id,user_id as userId from persons")
    public List<PersonRecord> listAllUsers();

    @Select("select id,code from material_types")
    public List<MaterialTypeRecord> listAllMaterialTypes();

    @Update(sql = "update materials " + SETTERS_WITHOUT_MODIFIER + " where code = ?{1.permId} and maty_id = ?{1.typeId}", batchUpdate = true)
    public void updateMaterialRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update projects " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateProjectRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update experiments_all " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateExperimentRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update samples_all " + SETTERS_WITH_MODIFIER + "where perm_id = ?{1.permId}", batchUpdate = true)
    public void updateSampleRegistrations(List<RegistrationDTO> registrations);

    @Update(sql = "update data_all " + SETTERS_WITH_MODIFIER + "where code = ?{1.permId}", batchUpdate = true)
    public void updateDataSetRegistrations(List<RegistrationDTO> registrations);
}
