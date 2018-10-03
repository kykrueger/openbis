from parsers import VocabularyDefinitionToCreationParser, ExperimentDefinitionToCreationParser, SampleDefinitionToCreationParser


def extractVocabularies(creations, entity_types):
    vocabulariesHelperMap = {}
    vocabulariesHelperMap = _fillVocabularies(vocabulariesHelperMap, _getVocabularyTypes(entity_types))
    vocabulariesHelperMap = _fillVocabularies(vocabulariesHelperMap, creations[VocabularyDefinitionToCreationParser.type])
    return vocabulariesHelperMap


def _getVocabularyTypes(entity_types):
    vocabulary_types = []
    for entity_type in entity_types:
        for propertyAssignment in entity_type.propertyAssignments:
            if propertyAssignment.propertyType.dataType == DataType.CONTROLLEDVOCABULARY:
                vocabulary_types.append(propertyAssignment.propertyType.vocabulary)
    return vocabulary_types


def getValue(dictionary, key, default={}):
    if not key in dictionary:
        dictionary[key] = default
    return dictionary[key]


def setValueInVocabularyIfNone(dictionary, key, value):
    if key in dictionary:
        error_msg = "ambiguous vocabulary, code or label: " + key \
                    + " already exists with value " + dictionary[key] + " wanted to set it again as " + value
        raise Exception(error_msg)
    dictionary[key] = value

    
def _fillVocabularies(vocabularyTermCodesMap, vocabularies):
    for vocabulary in vocabularies:
        voc = getValue(vocabularyTermCodesMap, vocabulary.code)
        for term in vocabulary.terms:
            setValueInVocabularyIfNone(voc, term.code, term.code)
            setValueInVocabularyIfNone(voc, term.code, term.label)
    return vocabularyTermCodesMap
