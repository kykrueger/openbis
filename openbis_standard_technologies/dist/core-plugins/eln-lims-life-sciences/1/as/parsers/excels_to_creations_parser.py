from .definition_to_creation import DefinitionToCreationParser
from .excel_to_poi import ExcelToPoiParser
from .poi_to_definition import PoiToDefinitionParser


def merge_dicts(product, dict_to_merge):
    for dict_key, dict_val in dict_to_merge.items():
        if dict_key not in product:
            product[dict_key] = dict_val
        else:
            product[dict_key].extend(dict_val)
    return product


def get_creations_from(xls_files):
    creations = {}
    for excel_file_path in xls_files:
        poi_definitions = ExcelToPoiParser.parse(excel_file_path)
        definitions = PoiToDefinitionParser.parse(poi_definitions)
        partial_creations = DefinitionToCreationParser.parse(definitions)
        creations = merge_dicts(creations, partial_creations)
    return creations
