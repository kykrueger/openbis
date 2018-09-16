from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId, SampleIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser


class OpenbisLogicHandler(object):

    def __init__(self, creations, existing_elements):
        self.creations = creations
        self.existing_elements = existing_elements

    def rewrite_parentchild_creationid_to_permid(self):
        if ProjectDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ProjectDefinitionToCreationParser.type]:
                for existing_element in self.existing_elements[SpaceDefinitionToCreationParser.type]:
                    if existing_element.code == creation.spaceId.creationId:
                        creation.spaceId = SpacePermId(str(existing_element.permId))
                        break
        if ExperimentDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ExperimentDefinitionToCreationParser.type]:
                for existing_element in self.existing_elements[ProjectDefinitionToCreationParser.type]:
                    if existing_element.code == creation.projectId.creationId:
                        creation.projectId = ProjectPermId(str(existing_element.permId))
                        break
        if SampleDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[SampleDefinitionToCreationParser.type]:
                if creation.spaceId is not None:
                    for existing_element in self.existing_elements[SpaceDefinitionToCreationParser.type]:
                        if existing_element.code == creation.spaceId.creationId:
                            creation.spaceId = SpacePermId(str(existing_element.permId))
                            break
                if creation.projectId is not None:
                    for existing_element in self.existing_elements[ProjectDefinitionToCreationParser.type]:
                        if existing_element.code == creation.projectId.creationId:
                            creation.projectId = ProjectPermId(str(existing_element.permId))
                            break
                if creation.experimentId is not None:
                    for existing_element in self.existing_elements[ExperimentDefinitionToCreationParser.type]:
                        if existing_element.code == creation.experimentId.creationId:
                            creation.experimentId = ExperimentPermId(str(existing_element.permId))
                            break

                rewritten_children = []
                if creation.childIds is not None:
                    for child in creation.childIds:
                        new_id = None
                        for existing_element in self.existing_elements[SampleDefinitionToCreationParser.type]:
                            if existing_element.permId.permId == child.creationId:
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == child.creationId:
                                new_id = existing_element.identifier
                                break

                        if new_id is None:
                            rewritten_children.append(child)
                        else:
                            rewritten_children.append(new_id)

                rewritten_parents = []
                if creation.parentIds is not None:
                    for parent in creation.parentIds:
                        new_id = None
                        for existing_element in self.existing_elements[SampleDefinitionToCreationParser.type]:
                            if existing_element.permId.permId == parent.creationId:
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == parent.creationId:
                                new_id = existing_element.identifier
                                break

                        if new_id is None:
                            rewritten_parents.append(parent)
                        else:
                            rewritten_parents.append(new_id)
                creation.setChildIds(rewritten_children)
                creation.setParentIds(rewritten_parents)

        return self.creations

    def remove_existing_elements_from_creations(self):
        for creations_type, existing_elements in self.existing_elements.items():
            if creations_type == SampleDefinitionToCreationParser.type:
                existing_object_codes = [object.identifier.identifier for object in existing_elements]
                self.creations[creations_type] = list(filter(lambda creation: creation.code is None or self._create_sample_identifier_string(creation) not in existing_object_codes, self.creations[creations_type]))
            else:
                distinct_property_name = self._get_distinct_property_name(creations_type)
                self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type, existing_elements, distinct_property_name)
        return self.creations

    def _get_distinct_property_name(self, creation_type):
        if creation_type == ScriptDefinitionToCreationParser.type:
            return 'name'
        else:
            return 'code'

    def _create_sample_identifier_string(self, creation):
        spaceId = creation.spaceId.creationId if creation.spaceId is not None else None
        projectId = creation.projectId.creationId if creation.projectId is not None else None
#         experimentId = creation.experimentId.creationId if creation.experimentId is not None else None
        code = creation.code
        sample_identifier = SampleIdentifier(spaceId, projectId, None, code)
        return sample_identifier.identifier

    def _filter_creations_from_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(object, attr) for object in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) not in existing_object_codes, self.creations[creations_type]))
