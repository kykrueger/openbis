/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * @author Franz-Josef Elmer
 */
public interface IPropertyQuery extends ObjectQuery
{
    @Select(sql = "select p.id as objectId, p.value as propertyValue "
            + "from experiment_properties p "
            + "join experiment_type_property_types etpt on p.etpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where pt.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listPlainExperimentProperties(String propertyTypeCode);

    @Update(sql = "update experiment_properties set value = ?{1.propertyValue} where id = ?{1.objectId}", batchUpdate = true)
    public void updatePlainExperimentProperties(List<PropertyRecord> convertedProperties);

    @Select(sql = "select p.id as objectId, p.value as propertyValue "
            + "from sample_properties p "
            + "join sample_type_property_types etpt on p.stpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where pt.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listPlainSampleProperties(String propertyTypeCode);

    @Update(sql = "update sample_properties set value = ?{1.propertyValue} where id = ?{1.objectId}", batchUpdate = true)
    public void updatePlainSampleProperties(List<PropertyRecord> convertedProperties);

    @Select(sql = "select p.id as objectId, p.value as propertyValue "
            + "from data_set_properties p "
            + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where pt.code = ?{1}", fetchSize = FETCH_SIZE)
    public List<PropertyRecord> listPlainDataSetProperties(String propertyTypeCode);

    @Update(sql = "update data_set_properties set value = ?{1.propertyValue} where id = ?{1.objectId}", batchUpdate = true)
    public void updatePlainDataSetProperties(List<PropertyRecord> convertedProperties);

}
