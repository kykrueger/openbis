from parsers import SpaceDefinitionToCreationParser, ProjectDefinitionToCreationParser, \
    ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser, CreationToUpdateParser

from utils.openbis_utils import create_sample_identifier_string

FAIL_IF_EXISTS = "FAIL_IF_EXISTS"
IGNORE_EXISTING = "IGNORE_EXISTING"
UPDATE_IF_EXISTS = "UPDATE_IF_EXISTS"


class OpenbisDuplicatesHandler(object):

    def __init__(self, creations, existing_elements, duplicates_strategy=FAIL_IF_EXISTS):
        self.creations = creations
        self.existing_elements = existing_elements
        self.duplicates_strategy = duplicates_strategy

    def rewrite_parentchild_creationid_to_permid(self):
        if ProjectDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ProjectDefinitionToCreationParser.type]:
                for existing_element in self.existing_elements[SpaceDefinitionToCreationParser.type]:
                    if existing_element.code == str(creation.spaceId):
                        creation.spaceId = existing_element.permId
                        break
        if ExperimentDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[ExperimentDefinitionToCreationParser.type]:
                for existing_element in self.existing_elements[ProjectDefinitionToCreationParser.type]:
                    if existing_element.code == str(creation.projectId):
                        creation.projectId = existing_element.permId
                        break
        if SampleDefinitionToCreationParser.type in self.creations:
            for creation in self.creations[SampleDefinitionToCreationParser.type]:
                if creation.spaceId is not None:
                    for existing_element in self.existing_elements[SpaceDefinitionToCreationParser.type]:
                        if existing_element.code == str(creation.spaceId):
                            creation.spaceId = existing_element.permId
                            break
                if creation.projectId is not None:
                    for existing_element in self.existing_elements[ProjectDefinitionToCreationParser.type]:
                        if existing_element.code == str(creation.projectId):
                            creation.projectId = existing_element.permId
                            break
                if creation.experimentId is not None:
                    for existing_element in self.existing_elements[ExperimentDefinitionToCreationParser.type]:
                        if existing_element.code == str(creation.experimentId):
                            creation.experimentId = existing_element.permId
                            break

                rewritten_children = []
                if creation.childIds is not None:
                    for child in creation.childIds:
                        new_id = None
                        for existing_element in self.existing_elements[SampleDefinitionToCreationParser.type]:
                            if existing_element.permId.permId == str(child):
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == str(child):
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
                            if existing_element.permId.permId == str(parent):
                                new_id = existing_element.permId
                                break
                            elif existing_element.identifier.identifier == str(parent):
                                new_id = existing_element.identifier
                                break

                        if new_id is None:
                            rewritten_parents.append(parent)
                        else:
                            rewritten_parents.append(new_id)
                creation.setChildIds(rewritten_children)
                creation.setParentIds(rewritten_parents)

        return self.creations

    def handle_existing_elements_in_creations(self):

        if self.duplicates_strategy == UPDATE_IF_EXISTS:
            duplicates_list = {}
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == SampleDefinitionToCreationParser.type:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    duplicates_list[creations_type] = list(filter(
                        lambda creation: creation.code is not None or create_sample_identifier_string(
                            creation) in existing_object_codes, self.creations[creations_type]))
                    self.creations[creations_type] = list(filter(
                        lambda creation: creation.code is None or create_sample_identifier_string(
                            creation) not in existing_object_codes, self.creations[creations_type]))
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    duplicates_list[creations_type] = self.get_creations_for_existing_objects(creations_type, existing_elements, distinct_property_name)
                    self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type,
                                                                                                  existing_elements,
                                                                                                  distinct_property_name)

                updates = CreationToUpdateParser.parse(duplicates_list, self.existing_elements)
                for update_type, update in updates.items():
                    self.creations[update_type] = update

        if self.duplicates_strategy == FAIL_IF_EXISTS:
            duplicates_list = {}
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == SampleDefinitionToCreationParser.type:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    duplicates = list(filter(lambda creation: creation.code is not None and create_sample_identifier_string(creation) in existing_object_codes, self.creations[creations_type]))
                    if duplicates:
                        duplicates_list[creations_type] = duplicates
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    duplicates = self.get_creations_for_existing_objects(creations_type, existing_elements, distinct_property_name)
                    if duplicates:
                        duplicates_list[creations_type] = duplicates
            if duplicates_list:
                raise Exception("Some of the objects you are trying to create already exist on the server. An error is being thrown when FAIL_IF_EXISTS flag is on. Existing elements are: " + str(duplicates_list))

        if self.duplicates_strategy == IGNORE_EXISTING:
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == SampleDefinitionToCreationParser.type:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    self.creations[creations_type] = list(filter(
                        lambda creation: creation.code is None or create_sample_identifier_string(
                            creation) not in existing_object_codes, self.creations[creations_type]))
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type,
                                                                                                  existing_elements,
                                                                                                  distinct_property_name)
        return self.creations

    def _get_distinct_property_name(self, creation_type):
        if creation_type == ScriptDefinitionToCreationParser.type:
            return 'name'
        else:
            return 'code'

    def _filter_creations_from_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(obj, attr) for obj in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) not in existing_object_codes,
                           self.creations[creations_type]))

    def get_creations_for_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(obj, attr) for obj in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) in existing_object_codes,
                           self.creations[creations_type]))
