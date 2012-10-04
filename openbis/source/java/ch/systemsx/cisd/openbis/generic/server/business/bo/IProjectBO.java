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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Business object of a project. Holds an instance of {@link ProjectPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IProjectBO extends IEntityBusinessObject
{

    /**
     * Defines a new project of specified code in a specified group. After invocation of this method
     * {@link IBusinessObject#save()} should be invoked to store the new project in the Data Access
     * Layer.
     * 
     * @throws UserFailureException if <code>projectCode</code> does already exist or project group
     *             is unspecified and home group is undefined.
     */
    public void define(final ProjectIdentifier projectIdentifier, String description,
            String leaderIdOrNull) throws UserFailureException;

    /**
     * Returns the loaded project.
     */
    public ProjectPE getProject();

    /**
     * Loads a project given by its identifier.
     * 
     * @throws UserFailureException if no project found.
     */
    public void loadByProjectIdentifier(ProjectIdentifier project);

    /**
     * Loads a project given by its perm id.
     * 
     * @throws UserFailureException if no project found.
     */
    public void loadByPermId(String permId);

    /**
     * Returns attachment (with content) given defined by filename and version (or latest one if
     * version is <code>null</code>.
     */
    public AttachmentPE getProjectFileAttachment(String fileName, Integer versionOrNull);

    /** Loads attachments */
    public void enrichWithAttachments();

    /**
     * Adds the specified attachment to the project.
     */
    public void addAttachment(AttachmentPE att);

    /**
     * Updates the project.
     */
    public void update(ProjectUpdatesDTO updates);

    /**
     * Deletes project for specified reason.
     * 
     * @param projectId project technical identifier
     * @throws UserFailureException if project with given technical identifier is not found.
     */
    public void deleteByTechId(TechId projectId, String reason);
}
