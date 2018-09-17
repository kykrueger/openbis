from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id import CreationId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import DataSetTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import ExperimentTypeCreation, ExperimentCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin import PluginType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import PluginCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id import PluginPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import ProjectCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import PropertyAssignmentCreation, PropertyTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleTypeCreation, SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyTermCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id import VocabularyPermId
from file_handling import get_script, get_filename_from_path
from java.lang import UnsupportedOperationException


def get_boolean_from_string(text):
    return True if text.lower() == u'true' else False


class DefinitionToCreationParserFactory(object):

    @staticmethod
    def getParsers(definition):
        if definition.type == u'VOCABULARY_TYPE':
            return [VocabularyDefinitionToCreationParser]
        elif definition.type == u'SAMPLE_TYPE':
            return [SampleTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, ScriptDefinitionToCreationParser]
        elif definition.type == u'EXPERIMENT_TYPE':
            return [ExperimentTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, ScriptDefinitionToCreationParser]
        elif definition.type == u'DATASET_TYPE':
            return [DatasetTypeDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, ScriptDefinitionToCreationParser]
        elif definition.type == u'SPACE':
            return [SpaceDefinitionToCreationParser]
        elif definition.type == u'PROJECT':
            return [ProjectDefinitionToCreationParser]
        elif definition.type == u'EXPERIMENT':
            return [ExperimentDefinitionToCreationParser]
        elif definition.type == u'SAMPLE':
            return [SampleDefinitionToCreationParser]
        elif definition.type == u'PROPERTY_TYPE':
            return [PropertyTypeDefinitionToCreationParser]
        else:
            raise UnsupportedOperationException("Definition of " + str(definition.type) + " is not supported (probably yet).")


class PropertyTypeDefinitionToCreationParser(object):

    type = "PropertyTypeCreation"

    @staticmethod
    def parse(definition):
        property_creations = []
        for property in definition.properties:
            property_type_creation = PropertyTypeCreation()
            property_type_creation.code = property[u'code']
            if property[u'code'].startswith(u'$'):
                property_type_creation.internalNameSpace = True
            property_type_creation.label = property[u'property label']
            property_type_creation.dataType = DataType.valueOf(property[u'property type'])
            property_type_creation.vocabularyId = VocabularyPermId(property[u'vocabulary code']) if property[u'vocabulary code'] is not None else None
            property_type_creation.description = property[u'description']
            property_creations.append(property_type_creation)

        return property_creations

    @staticmethod
    def get_type():
        return PropertyTypeDefinitionToCreationParser.type


class VocabularyDefinitionToCreationParser(object):

    type = "VocabularyCreation"

    @staticmethod
    def parse(definition):
        code = definition.attributes[u'code']
        vocabulary_creation = VocabularyCreation()
        vocabulary_creation.code = code
        if code.startswith(u'$'):
            vocabulary_creation.internalNameSpace = True
        vocabulary_creation.description = definition.attributes[u'description']

        vocabulary_creations_terms = []
        for property in definition.properties:
            vocabulary_creation_term = VocabularyTermCreation()
            vocabulary_creation_term.code = property[u'code']
            vocabulary_creation_term.label = property[u'label']
            vocabulary_creation_term.description = property[u'description']
            vocabulary_creations_terms.append(vocabulary_creation_term)

        vocabulary_creation.terms = vocabulary_creations_terms

        return vocabulary_creation

    @staticmethod
    def get_type():
        return VocabularyDefinitionToCreationParser.type


class PropertyAssignmentDefinitionToCreationParser(object):

    type = "PropertyAssignmentCreation"

    @staticmethod
    def parse(property):
        code = property[u'code']
        property_assingment_creation = PropertyAssignmentCreation()
        is_mandatory = get_boolean_from_string(property[u'mandatory'])
        property_assingment_creation.mandatory = is_mandatory
        should_show_in_edit_view = get_boolean_from_string(property[u'show in edit views'])
        property_assingment_creation.showInEditView = should_show_in_edit_view
        property_assingment_creation.section = property[u'section']
        property_assingment_creation.propertyTypeId = PropertyTypePermId(code)
        if u'dynamic script' in property and property[u'dynamic script'] is not None:
            dynamic_script_path = property[u'dynamic script']
            property_assingment_creation.pluginId = PluginPermId(ScriptDefinitionToCreationParser.get_name_for(code, dynamic_script_path))

        return property_assingment_creation

    @staticmethod
    def get_type():
        return PropertyAssignmentDefinitionToCreationParser.type


class SampleTypeDefinitionToCreationParser(object):

    type = "SampleTypeCreation"

    @staticmethod
    def parse(definition):
        code = definition.attributes[u'code']
        sample_creation = SampleTypeCreation()
        sample_creation.code = code
        should_auto_generate_codes = get_boolean_from_string(definition.attributes[u'auto generate codes'])
        sample_creation.autoGeneratedCode = should_auto_generate_codes
        if u'validation script' in definition.attributes and definition.attributes[u'validation script'] is not None:
            validation_script_path = definition.attributes[u'validation script']
            sample_creation.validationPluginId = PluginPermId(ScriptDefinitionToCreationParser.get_name_for(code, validation_script_path))

        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        sample_creation.propertyAssignments = property_assingment_creations
        return sample_creation

    @staticmethod
    def get_type():
        return SampleTypeDefinitionToCreationParser.type


class ExperimentTypeDefinitionToCreationParser(object):

    type = "ExperimentTypeCreation"

    @staticmethod
    def parse(definition):
        experiment_type_creation = ExperimentTypeCreation()
        experiment_type_creation.code = definition.attributes[u'code']

        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        experiment_type_creation.propertyAssignments = property_assingment_creations
        return experiment_type_creation

    @staticmethod
    def get_type():
        return ExperimentTypeDefinitionToCreationParser.type


class DatasetTypeDefinitionToCreationParser(object):

    type = "DatasetTypeCreation"

    @staticmethod
    def parse(definition):
        dataset_type_creation = DataSetTypeCreation()
        dataset_type_creation.code = definition.attributes[u'code']

        property_assingment_creations = []
        for property in definition.properties:
            property_assingment_creation = PropertyAssignmentDefinitionToCreationParser.parse(property)
            property_assingment_creations.append(property_assingment_creation)

        dataset_type_creation.propertyAssignments = property_assingment_creations
        return dataset_type_creation

    @staticmethod
    def get_type():
        return DatasetTypeDefinitionToCreationParser.type


class SpaceDefinitionToCreationParser(object):

    type = "SpaceCreation"

    @staticmethod
    def parse(definition):
        space_creation = SpaceCreation()
        space_creation.code = definition.attributes[u'code']
        space_creation.description = definition.attributes[u'description']
        creation_id = definition.attributes[u'code']
        space_creation.creationId = CreationId(creation_id)
        return space_creation

    @staticmethod
    def get_type():
        return SpaceDefinitionToCreationParser.type


class ProjectDefinitionToCreationParser(object):

    type = "ProjectCreation"

    @staticmethod
    def parse(definition):
        project_creation = ProjectCreation()
        project_creation.code = definition.attributes[u'code']
        project_creation.description = definition.attributes[u'description']
        project_creation.spaceId = CreationId(definition.attributes[u'space'])
        creation_id = definition.attributes[u'code']
        project_creation.creationId = CreationId(creation_id)
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
            experiment_creation.typeId = EntityTypePermId(definition.attributes[u'experiment type'])
            experiment_creation.code = experiment_properties[u'code']
            experiment_creation.projectId = CreationId(experiment_properties[u'project'])
            creation_id = experiment_properties[u'code']
            experiment_creation.creationId = CreationId(creation_id)
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
        mandatory_attributes = [u'$', u'code', u'space', u'project', u'experiment', u'auto generate code', u'parents', u'children']
        for sample_properties in definition.properties:
            sample_creation = SampleCreation()
            sample_creation.typeId = EntityTypePermId(definition.attributes[u'sample type'])
            if u'code' in sample_properties and sample_properties[u'code'] is not None:
                sample_creation.code = sample_properties[u'code']
                creation_id = sample_properties[u'code']
                sample_creation.creationId = CreationId(creation_id)
            if u'$' in sample_properties and sample_properties[u'$'] is not None:
                # may overwrite creationId from code, which is intended
                sample_creation.creationId = CreationId(sample_properties[u'$'])
            if u'auto generate code' in sample_properties and sample_properties[u'auto generate code'] is not None and sample_properties[u'auto generate code'] != '':
                sample_creation.autoGeneratedCode = get_boolean_from_string(sample_properties[u'auto generate code'])
            if u'space' in sample_properties and sample_properties[u'space'] is not None:
                sample_creation.spaceId = CreationId(sample_properties[u'space'])
            if u'project' in sample_properties and sample_properties[u'project'] is not None:
                sample_creation.projectId = CreationId(sample_properties[u'project'])
            if u'experiment' in sample_properties and sample_properties[u'experiment'] is not None:
                sample_creation.experimentId = CreationId(sample_properties[u'experiment'])
            if u'parents' in sample_properties and sample_properties[u'parents'] is not None:
                parent_creationids = []
                parents = sample_properties[u'parents'].split('\n')
                for parent in parents:
                    parent_creationids.append(CreationId(parent))
                sample_creation.parentIds = parent_creationids
            if u'children' in sample_properties and sample_properties[u'children'] is not None:
                child_creationids = []
                children = sample_properties[u'children'].split('\n')
                for child in children:
                    child_creationids.append(CreationId(child))
                sample_creation.childIds = child_creationids

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
        if u'validation script' in definition.attributes:
            validation_script_path = definition.attributes[u'validation script']
            if validation_script_path is not None:
                code = definition.attributes[u'code']
                validation_script_creation = PluginCreation()
                script_file = open(get_script(validation_script_path))
                script = script_file.read()
                script_file.close()
                validation_script_creation.name = ScriptDefinitionToCreationParser.get_name_for(code, validation_script_path)
                validation_script_creation.script = script
                validation_script_creation.pluginType = PluginType.ENTITY_VALIDATION
                scripts.append(validation_script_creation)

        for property in definition.properties:
            if u'dynamic script' in property:
                dynamic_prop_script_path = property[u'dynamic script']
                code = property[u'code']
                if dynamic_prop_script_path is not None:
                    validation_script_creation = PluginCreation()
                    script_file = open(get_script(dynamic_prop_script_path))
                    script = script_file.read()
                    script_file.close()
                    validation_script_creation.name = ScriptDefinitionToCreationParser.get_name_for(code, dynamic_prop_script_path)
                    validation_script_creation.script = script
                    validation_script_creation.pluginType = PluginType.DYNAMIC_PROPERTY
                    scripts.append(validation_script_creation)

        return scripts

    @staticmethod
    def get_name_for(owner_code, script_path):
        return owner_code + '.' + get_filename_from_path(script_path)

    @staticmethod
    def get_type():
        return ScriptDefinitionToCreationParser.type
