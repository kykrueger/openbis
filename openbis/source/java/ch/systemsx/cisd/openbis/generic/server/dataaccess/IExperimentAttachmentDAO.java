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
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;

/**
 * Data access object for managing persistency of {@link AttachmentPE} instances.
 * 
 * @author Franz-Josef Elmer
 */
public interface IExperimentAttachmentDAO
{
    /**
     * Returns a list of the descriptions of all {@link AttachmentPE} object associated with the
     * specified experiment. The result is detached from the hibernate session.
     * 
     * @param experiment Technical ID of the experiment whose properties are requested.
     */
    public List<AttachmentPE> listExperimentAttachments(ExperimentPE experiment)
            throws DataAccessException;

    /**
     * Creates a persistent version of the specified attachment. Registrator and version are not
     * needed.
     * 
     * @param attachment The property to register.
     * @param owner Owner of the attachment. Should be a persistent object.
     */
    public void createExperimentAttachment(AttachmentPE attachment, ExperimentPE owner)
            throws DataAccessException;

    /**
     * Finds the attachment requested by the specified experiment code and file name. If multiple
     * versions of this attachment exist, this will always return the latest version.
     * 
     * @param experiment technical ID of the associated experiment.
     * @return <code>null</code> if no attachment is found. The result is detached from the
     *         hibernate session.
     */
    public AttachmentPE tryFindExpAttachmentByExpAndFileName(ExperimentPE experiment,
            String fileName) throws DataAccessException;

    /**
     * Finds the attachment requested by the specified experiment code, file name and version.
     * 
     * @param experiment technical ID of the associated experiment.
     * @return <code>null</code> if no attachment is found. The result is detached from the
     *         hibernate session.
     */
    public AttachmentPE tryFindExpAttachmentByExpAndFileNameAndVersion(ExperimentPE experiment,
            String fileName, int version) throws DataAccessException;
}
