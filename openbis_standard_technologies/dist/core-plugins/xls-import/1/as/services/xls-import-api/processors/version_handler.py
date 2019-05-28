from parsers import versionable_types
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update import VocabularyTermUpdate
from parsers import VocabularyTermCreationToUpdateType, ScriptDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, VocabularyDefinitionToCreationType
from utils.openbis_utils import get_metadata_vocabulary_name_for

FORCE = -1


class VersionHandler(object):

    def __init__(self, creations, creations_metadata, existing_elements, xls_version_vocabulary):
        self.creations = creations
        self.creations_metadata = creations_metadata
        self.existing_elements = existing_elements
        self.xls_version_vocabulary = xls_version_vocabulary

    def check_and_filter_versioned_creations(self):
        vocabulary_term_updates = []
        ignored = []
        for creation_type, creations_collection in self.creations.items():
            if creation_type not in versionable_types:
                continue

            for creation in creations_collection:
                code = creation.code
                version_term_code = get_metadata_vocabulary_name_for(creation_type, creation)
                if creation_type == VocabularyTermDefinitionToCreationType:
                    creation_metadata = self.creations_metadata[VocabularyDefinitionToCreationType][str(creation.vocabularyId)].terms[code]
                else:
                    creation_metadata = self.creations_metadata[creation_type][code]
                entity_excel_version = creation_metadata.version

                # TODO
                # This may be null if we have the same previously submitted xls
                # but new entity kinds have been added

                openbis_version_term = next(
                    (term for term in self.xls_version_vocabulary.terms if term.code == version_term_code), None)
                openbis_version = int(openbis_version_term.label)

                if entity_excel_version == FORCE:
                    continue

                if entity_excel_version > openbis_version:
                    vocabulary_term_update = VocabularyTermUpdate()
                    vocabulary_term_update.vocabularyTermId = openbis_version_term.permId
                    vocabulary_term_update.setLabel(str(entity_excel_version))
                    vocabulary_term_updates.append(vocabulary_term_update)
                    continue

                ignored.append(code)

                self.creations[creation_type] = list(filter(lambda c: c != creation, self.creations[creation_type]))
                if hasattr(creation, 'validationPluginId'):
                    pluginId = str(creation.validationPluginId)
                    self.creations[ScriptDefinitionToCreationType] = list(
                        filter(lambda creation: creation.name != pluginId,
                               self.creations[ScriptDefinitionToCreationType]))

                if hasattr(creation, 'propertyAssignments'):
                    for propertyAssignment in creation.propertyAssignments:
                        if hasattr(propertyAssignment, 'pluginId'):
                            pluginId = str(propertyAssignment.pluginId)
                            self.creations[ScriptDefinitionToCreationType] = list(
                                filter(lambda creation: creation.name != pluginId,
                                       self.creations[ScriptDefinitionToCreationType]))
        if ignored:
            print("=======================xls-parser-service====================")
            print("Following elements were not updated due to version mismatch")
            print(", ".join(ignored))
            print("=======================xls-parser-service====================")
        if vocabulary_term_updates:
            self.creations[VocabularyTermCreationToUpdateType] = vocabulary_term_updates

        return self.creations
