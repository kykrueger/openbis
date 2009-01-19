/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;

/**
 * An interface that contains all data access operations on {@link ProcedurePE}s.
 * 
 * @author Christian Ribeaud
 */
public interface IProcedureDAO
{

    /**
     * Creates a new procedure in the persistent layer based on the specified DTO.
     * <p>
     * As side effect {@link ProcedurePE#setId(Long)} will be invoked with the <i>unique identifier</i>
     * returned by the persistent layer.
     * </p>
     */
    public void createProcedure(ProcedurePE procedure) throws DataAccessException;

    /**
     * Returns all procedures connected with given experiments.
     * 
     * @param experiment
     */
    public List<ProcedurePE> listProcedures(ExperimentPE experiment) throws DataAccessException;
}