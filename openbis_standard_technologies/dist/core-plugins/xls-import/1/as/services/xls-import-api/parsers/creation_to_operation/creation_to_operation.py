from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import CreateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import CreateExperimentTypesOperation, CreateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import CreatePluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import CreateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import CreatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import CreateSampleTypesOperation, CreateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import CreateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import CreateVocabulariesOperation
from ..definition_to_creation import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser
from ..creation_to_update import VocabularyCreationToUpdateParser, VocabularyTermCreationToUpdateParser, \
                        PropertyTypeCreationToUpdateParser, SampleTypeCreationToUpdateParser, ExperimentTypeCreationToUpdateParser, \
                        DatasetTypeCreationToUpdateParser, SpaceCreationToUpdateParser, ProjectCreationToUpdateParser, ExperimentCreationToUpdateParser, \
                        SampleCreationToUpdateParser, ScriptCreationToUpdateParser
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update import UpdateVocabulariesOperation, \
    UpdateVocabularyTermsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update import UpdatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update import UpdateSampleTypesOperation, \
    UpdateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update import UpdateExperimentTypesOperation, \
    UpdateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update import UpdateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update import UpdateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update import UpdateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update import UpdatePluginsOperation


class CreationOrUpdateToOperationParser(object):

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
        if VocabularyCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateVocabulariesOperation(creations[VocabularyCreationToUpdateParser.type]))
        if VocabularyTermCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateVocabularyTermsOperation(creations[VocabularyTermCreationToUpdateParser.type]))
        if PropertyTypeCreationToUpdateParser.type in creations:
            creation_operations.append(UpdatePropertyTypesOperation(creations[PropertyTypeCreationToUpdateParser.type]))
        if SampleTypeCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateSampleTypesOperation(creations[SampleTypeCreationToUpdateParser.type]))
        if ExperimentTypeCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateExperimentTypesOperation(creations[ExperimentTypeCreationToUpdateParser.type]))
        if DatasetTypeCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateDataSetTypesOperation(creations[DatasetTypeCreationToUpdateParser.type]))
        if SpaceCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateSpacesOperation(creations[SpaceCreationToUpdateParser.type]))
        if ProjectCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateProjectsOperation(creations[ProjectCreationToUpdateParser.type]))
        if ExperimentCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateExperimentsOperation(creations[ExperimentCreationToUpdateParser.type]))
        if SampleCreationToUpdateParser.type in creations:
            creation_operations.append(UpdateSamplesOperation(creations[SampleCreationToUpdateParser.type]))
        if ScriptCreationToUpdateParser.type in creations:
            creation_operations.append(UpdatePluginsOperation(creations[ScriptCreationToUpdateParser.type]))

        return creation_operations
