/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project;

import java.io.Serializable;
import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.person.IPersonId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.entity.project.ProjectCreation")
public class ProjectCreation implements Serializable
{
    private static final long serialVersionUID = 1L;

    private ISpaceId spaceId;

    private String code;

    private String description;

    private IPersonId leaderId;

    private List<AttachmentCreation> attachments;

    public ISpaceId getSpaceId()
    {
        return spaceId;
    }

    public void setSpaceId(ISpaceId spaceId)
    {
        this.spaceId = spaceId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public IPersonId getLeaderId()
    {
        return leaderId;
    }

    public void setLeaderId(IPersonId leaderId)
    {
        this.leaderId = leaderId;
    }

    public List<AttachmentCreation> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<AttachmentCreation> attachments)
    {
        this.attachments = attachments;
    }

}