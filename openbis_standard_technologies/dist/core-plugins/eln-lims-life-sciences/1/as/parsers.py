from copy import deepcopy
from org.python.core.io import FileIO
from org.apache.poi.ss.usermodel import WorkbookFactory
from org.apache.poi.ss.usermodel import CellType
from org.apache.commons.lang import StringUtils
from org.apache.poi.ss.util import NumberToTextConverter
from java.lang import UnsupportedOperationException
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import CreateVocabulariesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyTermCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleTypeCreation, CreateSampleTypesOperation, \
    SampleCreation, CreateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import PropertyAssignmentCreation, PropertyTypeCreation, CreatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id import VocabularyPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype import EntityKind
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import ExperimentTypeCreation, CreateExperimentTypesOperation, \
    ExperimentCreation, CreateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import DataSetTypeCreation, CreateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation, \
    CreateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import ProjectCreation, \
    CreateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id import CreationId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import PluginCreation, \
    CreatePluginsOperation
from file_handling import get_script, get_filename_from_path
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id import PluginPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin import PluginType


class ExcelToPoiParser(object):

    @staticmethod
    def parse(excel_file_path):
        workbook = WorkbookFactory.create(FileIO(excel_file_path, 'r').asInputStream())

        data_colection = []
        for sheet in workbook.sheetIterator():
            # get whole one definition
            definitions = []
            i = 0
            sheet_done = False
            while not sheet_done:
                definition_rows = []
                while not ExcelToPoiParser.is_row_empty(sheet.getRow(i)):
                    row = sheet.getRow(i)
                    first_cell = row.getCell(0)
                    if first_cell is None or first_cell == CellType.BLANK:
                        continue
                    else:
                        definition_rows.append(row)
                        i += 1
                i += 1  # skip empty row
                        
                if not definition_rows:
                    sheet_done = True
                else:
                    definitions.append(definition_rows)
        
        workbook.close()

        definitions_stripped = []
        for definition in definitions:
            definition_stripped = []
            for row in definition:
                row_stripped = {}
                for cell in row.cellIterator():
                    cell_value = ExcelToPoiParser.extract_string_value_from(cell)
                    cell_value = cell_value if cell_value != '' else None
                    cell_col = cell.getColumnIndex()
                    row_stripped[cell_col] = cell_value
                definition_stripped.append(row_stripped)
            definitions_stripped.append(definition_stripped)
              
        return definitions_stripped
            
    @staticmethod
    def extract_string_value_from(cell):
        cell_type = cell.getCellTypeEnum()
        if cell_type == CellType.BLANK:
            return "";
        elif cell_type == CellType.BOOLEAN:
            return str(cell.getBooleanCellValue());
        elif cell_type == CellType.NUMERIC:
            return NumberToTextConverter.toText(cell.getNumericCellValue());
        elif cell_type == CellType.STRING:
            return cell.getStringCellValue();
        elif cell_type == CellType.FORMULA:
            raise SyntaxError("Excel formulas are not supported but one was found in cell " + extractCellPosition(cell));
        elif cell_type == CellType.ERROR:
            raise SyntaxError("There is an error in cell " + extractCellPosition(cell));
        else:
            raise SyntaxError("Unknown data type of cell " + extractCellPosition(cell));
             
    @staticmethod
    def is_row_empty(row):
        if row is None:
            return True
        if row.getLastCellNum <= 0:
            return True

        return all(ExcelToPoiParser.is_cell_empty(cell) for cell in row.cellIterator())
    
    @staticmethod
    def is_cell_empty(cell):
        if cell is not None and cell.getCellType() != CellType.BLANK and StringUtils.isNotBlank(cell.toString()):
            return False;
        return True


class Definition(object):
    '''
        Used to hold values for object(Vocabulary, SampleType etc.) creation.
    '''

    def __init__(self):
        self.type = None
        self.attributes = {}
        self.properties = []
     
    def __str__(self):
        return "\n".join([
            "Definition type:",
            str(self.type),
            "Attributes:",
            str(self.attributes),
            "Properties:",
            str(self.properties),
            "==================" * 3])

    __repr__ = __str__
        

class DefinitionParserFactory(object):
        
        @staticmethod
        def get_parser(definition_type):
            if definition_type in ['VOCABULARY_TYPE', 'SAMPLE_TYPE', 'EXPERIMENT_TYPE', 'DATASET_TYPE', 'SPACE', 'PROJECT', 'EXPERIMENT', 'SAMPLE']:
                return GeneralDefinitionParser
            else:
                raise UnsupportedOperationException("Definition of " + str(definition_type) + " is not supported (probably yet).")


class GeneralDefinitionParser(object):

    @staticmethod
    def parse(poi_definition):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        ATTRIBUTES_HEADER_ROW = 1
        ATTRIBUTES_VALUES_ROW = 2
        PROPERTIES_HEADER_ROW = 3
        PROPERTIES_VALUES_ROW_START = 4
        
        definition = Definition()
        definition.type = poi_definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]
        
        for col, header in poi_definition[ATTRIBUTES_HEADER_ROW].items():
            cell_value = poi_definition[ATTRIBUTES_VALUES_ROW][col]
            definition.attributes[header] = cell_value
        
        if GeneralDefinitionParser.hasProperties(poi_definition):
            properties_headers = poi_definition[PROPERTIES_HEADER_ROW]
            
            for property_definitions in poi_definition[PROPERTIES_VALUES_ROW_START:]:
                property = {}
                for col, header in properties_headers.items():
                    property[header] = property_definitions[col]
                definition.properties.append(property)
        
        return definition
        
    @staticmethod
    def hasProperties(poi_definition):
        PROPERTIES_HEADER_ROW = 3
        return len(poi_definition) > PROPERTIES_HEADER_ROW


class PoiCleaner(object):
    
    @staticmethod
    def hasProperties(poi_definition):
        PROPERTIES_HEADER_ROW = 3
        return len(poi_definition) > PROPERTIES_HEADER_ROW
    
    @staticmethod
    def clean_data(xls_definitions):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        ATTRIBUTES_HEADER_ROW = 1
        ATTRIBUTES_VALUES_ROW = 2
        PROPERTIES_HEADER_ROW = 3
        PROPERTIES_VALUES_ROW_START = 4
        
        xls_def = deepcopy(xls_definitions)
        '''
            First row can only have definition type, rest is trimmed
        '''
        for definition in xls_def:
            definition[DEFINITION_TYPE_ROW] = {0:definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]}
            '''
                Header rows cannot have empty cells
            '''
            PoiCleaner.delete_empty_cells_from(definition, ATTRIBUTES_HEADER_ROW)
            if PoiCleaner.hasProperties(definition):
                PoiCleaner.delete_empty_cells_from(definition, PROPERTIES_HEADER_ROW)
            '''
                Values that do not have corresponding headers are removed.
            '''
            PoiCleaner.delete_cells_if_no_header(definition, ATTRIBUTES_HEADER_ROW, ATTRIBUTES_VALUES_ROW)
            if PoiCleaner.hasProperties(definition):
                for property_value_row_num in range(PROPERTIES_VALUES_ROW_START, len(definition)):
                    PoiCleaner.delete_cells_if_no_header(definition, PROPERTIES_HEADER_ROW, property_value_row_num)
            '''
                All attributes and properties should have corresponding value in values row when header exists.
                If there's no corresponding value, None is inserted.
            '''
            PoiCleaner.create_cells_if_no_value_but_header_exists(definition, ATTRIBUTES_HEADER_ROW, ATTRIBUTES_VALUES_ROW)
            if PoiCleaner.hasProperties(definition):
                for property_value_row_num in range(PROPERTIES_VALUES_ROW_START, len(definition)):
                    PoiCleaner.create_cells_if_no_value_but_header_exists(definition, PROPERTIES_HEADER_ROW, property_value_row_num)
            '''
                Headers to lowercase
            '''
            definition[ATTRIBUTES_HEADER_ROW] = PoiCleaner.dict_values_to_lowercase(definition[ATTRIBUTES_HEADER_ROW])
            if PoiCleaner.hasProperties(definition):
                definition[PROPERTIES_HEADER_ROW] = PoiCleaner.dict_values_to_lowercase(definition[PROPERTIES_HEADER_ROW])
                
        return xls_def
    
    @staticmethod
    def delete_empty_cells_from(definition, row_number):
        header_cell_cols_to_pop = []
        for cell_col, cell in definition[row_number].items():
            if cell is None or cell == '':
                header_cell_cols_to_pop.append(cell_col)
        
        for cell_col in header_cell_cols_to_pop:
            del definition[row_number][cell_col]
            
        return definition
        
    @staticmethod   
    def delete_cells_if_no_header(definition, header_row_number, value_row_number):
        values_cell_cols_to_pop = []
        for cell_col in definition[value_row_number]:
            if cell_col not in definition[header_row_number]:
                values_cell_cols_to_pop.append(cell_col)

        for cell_col in values_cell_cols_to_pop:
            del definition[value_row_number][cell_col]
            
        return definition
    
    @staticmethod    
    def create_cells_if_no_value_but_header_exists(definition, header_row_number, value_row_number):
        values_cell_cols_to_insert = []
        for cell_col in definition[header_row_number]:
            if cell_col not in definition[value_row_number]:
                values_cell_cols_to_insert.append(cell_col)

        for cell_col in values_cell_cols_to_insert:
            definition[value_row_number][cell_col] = None

        return definition

    @staticmethod
    def dict_values_to_lowercase(row):
        return dict((k, v.lower()) for (k, v) in row.items())


class PoiToDefinitionParser(object):

    @staticmethod
    def parse(uncleaned_poi_definitions):
        '''
            Expecting definitions to be in such layout:
            [
                `DEFINITIONS_LIST`
                [
                    `DEFINITION`
                    {
                        `ROWS`
                        (column, row) : string_value,
                        .
                        .
                        .
                    }
                ],
                .
                .
                .
            ]
        '''
        poi_definitions = PoiCleaner.clean_data(uncleaned_poi_definitions)
        definitons = []
        for poi_definition in poi_definitions:
            FIRST_ROW = 0
            FIRST_CELL = 0
            definition_type = poi_definition[FIRST_ROW][FIRST_CELL]
            definition_parser = DefinitionParserFactory.get_parser(definition_type)
            definition = definition_parser.parse(poi_definition)
            definitons.append(definition)

        return definitons


class DefinitionToCreationParser(object):

    @staticmethod
    def parse(definitions):
        creations = {}
        
        for definition in definitions:
            # One definition may contain more than one creation
            parsers = DefinitionToCreationParserFactory.getParsers(definition)
            for parser in parsers:
                creation = parser.parse(definition)
                if creation is None or creation == []:
                    continue
                creation_type = parser.get_type()
                if creation_type not in creations:
                    creations[creation_type] = []
                creations[creation_type].extend(creation if type(creation) == list else [creation])

        return creations

    @staticmethod
    def get_boolean_from_string(text):
        return True if text.lower() == u'true' else False

    
class PropertyTypeDefinitionToCreationParser(object):

    type = "PropertyTypeCreation"

    @staticmethod
    def parse(definition):
        property_creations = []
        for property in definition.properties:
            property_type_creation = PropertyTypeCreation()
            property_type_creation.code = property[u'code']
            if property[u'code'].startswith(u'$'):
                property_type_creation.setInternalNameSpace(True)
            property_type_creation.setLabel(property[u'property label'])
            property_type_creation.setDataType(DataType.valueOf(property[u'property type']))
            property_type_creation.setVocabularyId(VocabularyPermId(property[u'vocabulary code']) if property[u'vocabulary code'] is not None else None)
            property_type_creation.setDescription(property[u'description'])
            property_creations.append(property_type_creation)

        return property_creations

    @staticmethod
    def get_type():
        return PropertyTypeDefinitionToCreationParser.type


class VocabularyDefinitionToCreationParser(object):
    
    type = "VocabularyCreation"
    
    @staticmethod
    def parse(definition):
        vocabulary_creation = VocabularyCreation()
        vocabulary_creation.setCode(definition.attributes[u'code'])
        vocabulary_creation.setDescription(definition.attributes[u'description'])
        
        vocabulary_creations_terms = []
        for property in definition.properties:
            vocabulary_creation_term = VocabularyTermCreation()
            vocabulary_creation_term.setCode(property[u'code'])
            vocabulary_creation_term.setLabel(property[u'label'])
            vocabulary_creation_term.setDescription(property[u'description'])
            vocabulary_creations_terms.append(vocabulary_creation_term)
            
        vocabulary_creation.setTerms(vocabulary_creations_terms)

        return vocabulary_creation
    
    @staticmethod
    def get_type():
        return VocabularyDefinitionToCreationParser.type


class PropertyAssignmentDefinitionToCreationParser(object):
    
    type = "PropertyAssignmentCreation"
    
    @staticmethod
    def parse(property):
        property_assingment_creation = PropertyAssignmentCreation()
        is_mandatory = DefinitionToCreationParser.get_boolean_from_string(property[u'mandatory'])
        property_assingment_creation.setMandatory(is_mandatory)
        should_show_in_edit_view = DefinitionToCreationParser.get_boolean_from_string(property[u'show in edit views'])
        property_assingment_creation.setShowInEditView(should_show_in_edit_view)
        property_assingment_creation.setSection(property[u'section'])
        property_assingment_creation.setPropertyTypeId(PropertyTypePermId(property[u'code']))
        
        return property_assingment_creation
    
    @staticmethod
    def get_type():
        return PropertyAssignmentDefinitionToCreationParser.type


class SampleTypeDefinitionToCreationParser(object):
    
    type = "SampleTypeCreation"
   
    @staticmethod
    def parse(definition):
        sample_creation = SampleTypeCreation()
        sample_creation.setCode(definition.attributes[u'code'])
        should_auto_generate_codes = DefinitionToCreationParser.get_boolean_from_string(definition.attributes[u'auto generate codes'])
        sample_creation.setAutoGeneratedCode(should_auto_generate_codes)
        validation_script_path = definition.attributes[u'validation script']
#         if validation_script_path is not None:
#             sample_creation.setValidationPluginId(PluginPermId(get_filename_from_path(validation_script_path)))

        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        sample_creation.setPropertyAssignments(property_assingment_creations)
        return sample_creation
    
    @staticmethod
    def get_type():
        return SampleTypeDefinitionToCreationParser.type


class ExperimentTypeDefinitionToCreationParser(object):
    
    type = "ExperimentTypeCreation"

    @staticmethod
    def parse(definition):
        experiment_type_creation = ExperimentTypeCreation()
        experiment_type_creation.setCode(definition.attributes[u'code'])

        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        experiment_type_creation.setPropertyAssignments(property_assingment_creations)
        return experiment_type_creation

    @staticmethod
    def get_type():
        return ExperimentTypeDefinitionToCreationParser.type


class DatasetTypeDefinitionToCreationParser(object):
    
    type = "DatasetTypeCreation"

    @staticmethod
    def parse(definition):
        dataset_type_creation = DataSetTypeCreation()
        dataset_type_creation.setCode(definition.attributes[u'code'])
        
        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        dataset_type_creation.setPropertyAssignments(property_assingment_creations)
        return dataset_type_creation
    
    @staticmethod
    def get_type():
        return DatasetTypeDefinitionToCreationParser.type

    
class SpaceDefinitionToCreationParser(object):
    
    type = "SpaceCreation"
    
    @staticmethod
    def parse(definition):
        space_creation = SpaceCreation()
        space_creation.setCode(definition.attributes[u'code'])
        space_creation.setDescription(definition.attributes[u'description'])
        creation_id = definition.attributes[u'code']
        space_creation.setCreationId(CreationId(creation_id))
        return space_creation
        
    @staticmethod
    def get_type():
        return SpaceDefinitionToCreationParser.type

    
class ProjectDefinitionToCreationParser(object):
    
    type = "ProjectCreation"
    
    @staticmethod
    def parse(definition):
        project_creation = ProjectCreation()
        project_creation.setCode(definition.attributes[u'code'])
        project_creation.setDescription(definition.attributes[u'description'])
        project_creation.setSpaceId(CreationId(definition.attributes[u'space']))
        creation_id = definition.attributes[u'code']
        project_creation.setCreationId(CreationId(creation_id))
        return project_creation

    @staticmethod
    def get_type():
        return ProjectDefinitionToCreationParser.type

    
class ExperimentDefinitionToCreationParser(object):
    
    type = "ExperimentCreation"
    
    @staticmethod
    def parse(definition):
        experiments = []
        mandatory_attributes = [u'code', u'project']
        for experiment_properties in definition.properties:
            experiment_creation = ExperimentCreation()
            experiment_creation.setTypeId(EntityTypePermId(definition.attributes[u'experiment type']))
            experiment_creation.setCode(experiment_properties[u'code'])
            experiment_creation.setProjectId(CreationId(experiment_properties[u'project']))
            creation_id = experiment_properties[u'code']
            experiment_creation.setCreationId(CreationId(creation_id))
            for property, value in experiment_properties.items():
                if property not in mandatory_attributes:
                    experiment_creation.setProperty(property, value)
            experiments.append(experiment_creation)
        return experiments
    
    @staticmethod
    def get_type():
        return ExperimentDefinitionToCreationParser.type

    
class SampleDefinitionToCreationParser(object):
    
    type = "SampleCreation"
    
    @staticmethod
    def parse(definition):
        samples = []
        mandatory_attributes = [u'code', u'space', u'project', u'experiment', u'auto generate code', u'parents', u'children']
        for sample_properties in definition.properties:
            sample_creation = SampleCreation()
            sample_creation.setTypeId(EntityTypePermId(definition.attributes[u'sample type']))
            if u'code' in sample_properties and sample_properties[u'code'] is not None and sample_properties[u'code'] != '':
                sample_creation.setCode(sample_properties[u'code'])
                creation_id = sample_properties[u'code']
                sample_creation.setCreationId(CreationId(creation_id))
            if u'auto generate code' in sample_properties and sample_properties[u'auto generate code'] is not None and sample_properties[u'auto generate code'] != '':
                sample_creation.setAutoGeneratedCode(DefinitionToCreationParser.get_boolean_from_string(sample_properties[u'auto generate code']))
            if u'space' in sample_properties and sample_properties[u'space'] is not None:
                sample_creation.setSpaceId(CreationId(sample_properties[u'space']))
            if u'project' in sample_properties and sample_properties[u'project'] is not None:
                sample_creation.setProjectId(CreationId(sample_properties[u'project']))
            if u'experiment' in sample_properties and sample_properties[u'experiment'] is not None:
                sample_creation.setExperimentId(CreationId(sample_properties[u'experiment']))
            for property, value in sample_properties.items():
                if property not in mandatory_attributes:
                    sample_creation.setProperty(property, value)
            samples.append(sample_creation)
        return samples
    
    @staticmethod
    def get_type():
        return SampleDefinitionToCreationParser.type

    
class ScriptDefinitionToCreationParser(object):
    
    type = "ScriptCreation"
    
    @staticmethod
    def parse(definition):
        scripts = []
        validation_script_path = definition.attributes[u'validation script']
        if validation_script_path is not None:
            validation_script_creation = PluginCreation()
            script_file = open(get_script(validation_script_path))
            script = script_file.read()
            script_file.close()
            validation_script_creation.setName(get_filename_from_path(validation_script_path))
            validation_script_creation.setScript(script)
            validation_script_creation.setPluginType(PluginType.ENTITY_VALIDATION)
            scripts.append(validation_script_creation)
        
        for property in definition.properties:
            dynamic_prop_script_path = property[u'dynamic script']
            if dynamic_prop_script_path is not None:
                validation_script_creation = PluginCreation()
                script_file = open(get_script(dynamic_prop_script_path))
                script = script_file.read()
                script_file.close()
                validation_script_creation.setName(get_filename_from_path(dynamic_prop_script_path))
                validation_script_creation.setScript(script)
                validation_script_creation.setPluginType(PluginType.DYNAMIC_PROPERTY)
                scripts.append(validation_script_creation)

        return scripts

    @staticmethod
    def parse_script():
        pass
    
    @staticmethod
    def get_type():
        return ScriptDefinitionToCreationParser.type


class DefinitionToCreationParserFactory(object):
    
    @staticmethod
    def getParsers(definition):
        if definition.type == u'VOCABULARY_TYPE':
            return [VocabularyDefinitionToCreationParser]
        elif definition.type == u'SAMPLE_TYPE':
            return [SampleTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, ScriptDefinitionToCreationParser]
        elif definition.type == u'EXPERIMENT_TYPE':
            return [ExperimentTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser]
        elif definition.type == u'DATASET_TYPE':
            return [DatasetTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser]
        elif definition.type == u'SPACE':
            return [SpaceDefinitionToCreationParser]
        elif definition.type == u'PROJECT':
            return [ProjectDefinitionToCreationParser]
        elif definition.type == u'EXPERIMENT':
            return [ExperimentDefinitionToCreationParser]
        elif definition.type == u'SAMPLE':
            return [SampleDefinitionToCreationParser]
        else:
            raise UnsupportedOperationException("Definition of " + str(definition.type) + " is not supported (probably yet).")


class DuplicatesHandler(object):
    
    @staticmethod
    def get_distinct_creations(creations):
        distinct_creations = {}
        for creation_type, creations in creations.items():
            if creation_type == ScriptDefinitionToCreationParser.type:
                distinct_creations[creation_type] = dict((creation.name, creation) for creation in creations).values()
            elif creation_type not in [SampleDefinitionToCreationParser.type]:
                distinct_creations[creation_type] = dict((creation.code, creation) for creation in creations).values()
            else:
                distinct_creations[creation_type] = list(creations)
        return distinct_creations


class CreationToOperationParser(object):

    @staticmethod
    def parse(creations):
        creation_operations = []
        if VocabularyDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateVocabulariesOperation(creations[VocabularyDefinitionToCreationParser.type]))
        if PropertyTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreatePropertyTypesOperation(creations[PropertyTypeDefinitionToCreationParser.type]))
        if SampleTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSampleTypesOperation(creations[SampleTypeDefinitionToCreationParser.type]))
        if ExperimentTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateExperimentTypesOperation(creations[ExperimentTypeDefinitionToCreationParser.type]))
        if DatasetTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateDataSetTypesOperation(creations[DatasetTypeDefinitionToCreationParser.type]))
        if SpaceDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSpacesOperation(creations[SpaceDefinitionToCreationParser.type]))
        if ProjectDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateProjectsOperation(creations[ProjectDefinitionToCreationParser.type]))
        if ExperimentDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateExperimentsOperation(creations[ExperimentDefinitionToCreationParser.type]))
        if SampleDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSamplesOperation(creations[SampleDefinitionToCreationParser.type]))
        if ScriptDefinitionToCreationParser.type in creations:
            creation_operations.append(CreatePluginsOperation(creations[ScriptDefinitionToCreationParser.type]))
        return creation_operations
