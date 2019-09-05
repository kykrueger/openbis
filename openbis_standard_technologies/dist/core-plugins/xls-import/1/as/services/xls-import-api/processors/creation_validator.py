from parsers import PropertyTypeDefinitionToCreationType
from ch.systemsx.cisd.common.exceptions import UserFailureException


def validate_creations(creations):
    '''
        This validator checks for:
         - whether property types with same code are all the same across xls

        It throws an exception if this is false.
    '''
    if PropertyTypeDefinitionToCreationType not in creations:
        return
    property_type_creations = creations[PropertyTypeDefinitionToCreationType]
    different_duplicates = set()
    for property_type in property_type_creations:
        for second_property_type in property_type_creations:
            if property_type.code == second_property_type.code:
                difference_info = {}
                attributes_to_check = ['label', 'description', 'dataType', 'internalNameSpace', 'vocabularyId',
                                       'metaData']
                not_equal_attributes_pairs = set()
                for attribute in attributes_to_check:
                    attribute_of_property_type = getattr(property_type, attribute)
                    attribute_of_second_property_type = getattr(second_property_type, attribute)
                    if attribute_of_property_type != attribute_of_second_property_type:
                        not_equal_attributes_pairs.add(
                            frozenset([attribute_of_property_type, attribute_of_second_property_type]))

                if not_equal_attributes_pairs:
                    difference_info['Property Label'] = property_type.code
                    difference_info['errors'] = not_equal_attributes_pairs
                    different_duplicates.add((property_type.code, frozenset(not_equal_attributes_pairs)))

    if different_duplicates:
        error_msg = "Following property types have ambiguous definition: \n" + \
                    '\n'.join(
                        ['Property Code: ' + str(diff_info[0]) + '\n' + '\n'.join(
                            [' != '.join(['"' + attribute_value + '"' for attribute_value in duplicates_attributes])
                             for duplicates_attributes in diff_info[1]])
                         for diff_info in different_duplicates]) + '\n'

        raise UserFailureException(error_msg)
