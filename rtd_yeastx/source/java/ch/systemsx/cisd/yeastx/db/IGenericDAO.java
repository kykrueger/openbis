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

package ch.systemsx.cisd.yeastx.db;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

/**
 * Interface for the "generic" methods (i.e. experiment, sample and data set).
 * 
 * @author Bernd Rinn
 */
public interface IGenericDAO extends BaseQuery
{

    @Select("select * from EXPERIMENTS where PERM_ID = ?{1}")
    public DMExperimentDTO getExperimentByPermId(String experimentPermId);

    @Select("select * from EXPERIMENTS where ID = ?{1}")
    public DMExperimentDTO getExperimentById(long id);

    @Select("insert into EXPERIMENTS (PERM_ID, NAME) values (?{1.permId}, ?{1.name}) returning ID")
    public long addExperiment(DMExperimentDTO experiment);

    @Select("select * from SAMPLES where PERM_ID = ?{1}")
    public DMSampleDTO getSampleByPermId(String samplePermId);

    @Select("select * from SAMPLES where ID = ?{1}")
    public DMSampleDTO getSampleById(long id);

    @Select("insert into SAMPLES (PERM_ID, NAME, EXPE_ID) values (?{1.permId}, ?{1.name}, ?{1.experiment.id}) "
            + "returning ID")
    public long addSample(DMSampleDTO sample);

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public DMDataSetDTO getDataSetByPermId(String dataSetPermId);

    @Select("select * from DATA_SETS where ID = ?{1}")
    public DMDataSetDTO getDataSetById(long id);

    @Select("insert into DATA_SETS (PERM_ID, EXPE_ID, SAMP_ID) values "
            + "(?{1.permId}, ?{1.experiment.id}, ?{1.sample.id}) returning ID")
    public long addDataSet(DMDataSetDTO dataSet);

}
