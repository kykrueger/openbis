from parsers import ScriptDefinitionToCreationType, SampleDefinitionToCreationType, VocabularyTermDefinitionToCreationType


class DuplicatesHandler(object):

    @staticmethod
    def get_distinct_creations(creations_grouped_by_type):
        distinct_creations = {}
        for creation_type, creations in creations_grouped_by_type.items():
            if creation_type == ScriptDefinitionToCreationType:
                distinct_creations[creation_type] = dict((creation.name, creation) for creation in creations).values()
            elif creation_type == VocabularyTermDefinitionToCreationType:
                distinct_creations[creation_type] = dict(((creation.code, str(creation.vocabularyId)), creation) for creation in creations).values()
            elif creation_type not in [SampleDefinitionToCreationType]:
                distinct_creations[creation_type] = dict((creation.code, creation) for creation in creations).values()
            else:
                distinct_creations[creation_type] = list(creations)
        return distinct_creations
