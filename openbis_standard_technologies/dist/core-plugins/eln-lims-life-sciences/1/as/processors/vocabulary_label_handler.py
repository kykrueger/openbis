from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from parsers import ExperimentDefinitionToCreationParser, SampleDefinitionToCreationParser

class VocabularyLabelHandler(object):

    @staticmethod
    def rewrite_vocabularies(creations_map, vocabulariesHelperMap):
        thingsThatContainVocabularies = [ExperimentDefinitionToCreationParser.type, SampleDefinitionToCreationParser.type ]
        for creations_type, creations in creations_map.iteritems():
            if creations_type in thingsThatContainVocabularies:
                for creation in creations:
                    for key, value in creation.properties:
                        # TO-DO : Unfinished
                        for term in vocabulary_property_type.vocabulary.terms:
                            if vocabulary_label.lower() == term.label.lower():
                                creation.properties[vocabulary_property_type.code.lower()] = term.code
                                break
        return creations_map

