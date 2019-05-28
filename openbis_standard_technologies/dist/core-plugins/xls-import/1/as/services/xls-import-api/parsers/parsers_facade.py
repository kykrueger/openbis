from .definition_to_creation import DefinitionToCreationParser
from .excel_to_poi import ExcelToPoiParser
from .poi_to_definition import PoiToDefinitionParser

from .definition_to_creation_metadata import DefinitionToCreationMetadataParser


def get_definitions_from(xls_byte_arrays):
    definitions = []
    for excel_byte_array in xls_byte_arrays:
        poi_definitions = ExcelToPoiParser.parse(excel_byte_array)
        partial_definitions = PoiToDefinitionParser.parse(poi_definitions)
        definitions.extend(partial_definitions)
    return definitions


def get_creations_from(definitions, context):
    pass
    return DefinitionToCreationParser.parse(definitions, context)


def get_creation_metadata_from(definitions):
    return DefinitionToCreationMetadataParser.parse(definitions)
