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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;

/**
 * @author Franz-Josef Elmer
 *
 */
public class ProjectDeliverer extends AbstractEntityDeliverer<Project>
{

    ProjectDeliverer(DeliveryContext context)
    {
        super(context, "project");
    }

    @Override
    protected List<Project> getAllEntities(String sessionToken)
    {
        ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.sortBy().permId();
        return context.getV3api().searchProjects(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    @Override
    protected void deliverEntities(XMLStreamWriter writer, String sessionToken, Set<String> spaces, List<Project> projects) throws XMLStreamException
    {
        List<ProjectPermId> permIds = projects.stream().map(Project::getPermId).collect(Collectors.toList());
        Collection<Project> fullProjects = context.getV3api().getProjects(sessionToken, permIds, createFullFetchOptions()).values();
        int count = 0;
        for (Project project : fullProjects)
        {
            if (spaces.contains(project.getSpace().getCode()))
            {
                String permId = project.getPermId().getPermId();
                startUrlElement(writer, "PROJECT", permId, project.getModificationDate());
                startXdElement(writer);
                writer.writeAttribute("code", project.getCode());
                addAttribute(writer, "desc", project.getDescription());
                addKind(writer, "PROJECT");
                addModifier(writer, project);
                addRegistrationDate(writer, project);
                addRegistrator(writer, project);
                addSpace(writer, project.getSpace());
                ConnectionsBuilder connectionsBuilder = new ConnectionsBuilder();
                connectionsBuilder.addConnections(project.getExperiments());
                connectionsBuilder.addConnections(project.getSamples().stream()
                        .filter(s -> s.getExperiment() == null).collect(Collectors.toList()));
                connectionsBuilder.writeTo(writer);
                addAttachments(writer, project.getAttachments());
                writer.writeEndElement();
                writer.writeEndElement();
                count++;
            }
        }
        operationLog.info(count + " of " + projects.size() + " projects have been delivered.");
    }

    private ProjectFetchOptions createFullFetchOptions()
    {
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments();
        fetchOptions.withRegistrator();
        fetchOptions.withModifier();
        fetchOptions.withSpace();
        fetchOptions.withExperiments();
        fetchOptions.withSamples().withExperiment();
        return fetchOptions;
    }

}
