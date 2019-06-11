from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import CreateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update import UpdateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import CreateExperimentTypesOperation, \
    CreateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update import UpdateExperimentTypesOperation, \
    UpdateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import CreatePluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update import UpdatePluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import CreateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update import UpdateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import CreatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update import UpdatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import CreateSampleTypesOperation, CreateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update import UpdateSampleTypesOperation, UpdateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import CreateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update import UpdateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import CreateVocabulariesOperation, \
    CreateVocabularyTermsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update import UpdateVocabulariesOperation, \
    UpdateVocabularyTermsOperation

from ..creation_to_update import PropertyTypeCreationToUpdateType, VocabularyCreationToUpdateType, \
    SampleTypeCreationToUpdateType, ExperimentTypeCreationToUpdateType, DatasetTypeCreationToUpdateType, \
    SpaceCreationToUpdateType, ProjectCreationToUpdateType, ExperimentCreationToUpdateType, \
    SampleCreationToUpdateType, ScriptCreationToUpdateType, VocabularyTermCreationToUpdateType
from ..definition_to_creation import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, SampleTypeDefinitionToCreationType, ExperimentTypeDefinitionToCreationType, \
    DatasetTypeDefinitionToCreationType, SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, \
    ExperimentDefinitionToCreationType, SampleDefinitionToCreationType, ScriptDefinitionToCreationType


class CreationOrUpdateToOperationParser(object):

    @staticmethod
    def parse(creations):
        creation_operations = []
        if VocabularyDefinitionToCreationType in creations:
            creation_operations.append(CreateVocabulariesOperation(creations[VocabularyDefinitionToCreationType]))
        if VocabularyTermDefinitionToCreationType in creations:
            creation_operations.append(
                CreateVocabularyTermsOperation(creations[VocabularyTermDefinitionToCreationType]))
        if PropertyTypeDefinitionToCreationType in creations:
            creation_operations.append(CreatePropertyTypesOperation(creations[PropertyTypeDefinitionToCreationType]))
        if SampleTypeDefinitionToCreationType in creations:
            creation_operations.append(CreateSampleTypesOperation(creations[SampleTypeDefinitionToCreationType]))
        if ExperimentTypeDefinitionToCreationType in creations:
            creation_operations.append(
                CreateExperimentTypesOperation(creations[ExperimentTypeDefinitionToCreationType]))
        if DatasetTypeDefinitionToCreationType in creations:
            creation_operations.append(CreateDataSetTypesOperation(creations[DatasetTypeDefinitionToCreationType]))
        if SpaceDefinitionToCreationType in creations:
            creation_operations.append(CreateSpacesOperation(creations[SpaceDefinitionToCreationType]))
        if ProjectDefinitionToCreationType in creations:
            creation_operations.append(CreateProjectsOperation(creations[ProjectDefinitionToCreationType]))
        if ExperimentDefinitionToCreationType in creations:
            creation_operations.append(CreateExperimentsOperation(creations[ExperimentDefinitionToCreationType]))
        if SampleDefinitionToCreationType in creations:
            creation_operations.append(CreateSamplesOperation(creations[SampleDefinitionToCreationType]))
        if ScriptDefinitionToCreationType in creations:
            creation_operations.append(CreatePluginsOperation(creations[ScriptDefinitionToCreationType]))
        if VocabularyCreationToUpdateType in creations:
            creation_operations.append(UpdateVocabulariesOperation(creations[VocabularyCreationToUpdateType]))
        if VocabularyTermCreationToUpdateType in creations:
            creation_operations.append(UpdateVocabularyTermsOperation(creations[VocabularyTermCreationToUpdateType]))
        if PropertyTypeCreationToUpdateType in creations:
            creation_operations.append(UpdatePropertyTypesOperation(creations[PropertyTypeCreationToUpdateType]))
        if SampleTypeCreationToUpdateType in creations:
            creation_operations.append(UpdateSampleTypesOperation(creations[SampleTypeCreationToUpdateType]))
        if ExperimentTypeCreationToUpdateType in creations:
            creation_operations.append(UpdateExperimentTypesOperation(creations[ExperimentTypeCreationToUpdateType]))
        if DatasetTypeCreationToUpdateType in creations:
            creation_operations.append(UpdateDataSetTypesOperation(creations[DatasetTypeCreationToUpdateType]))
        if SpaceCreationToUpdateType in creations:
            creation_operations.append(UpdateSpacesOperation(creations[SpaceCreationToUpdateType]))
        if ProjectCreationToUpdateType in creations:
            creation_operations.append(UpdateProjectsOperation(creations[ProjectCreationToUpdateType]))
        if ExperimentCreationToUpdateType in creations:
            creation_operations.append(UpdateExperimentsOperation(creations[ExperimentCreationToUpdateType]))
        if SampleCreationToUpdateType in creations:
            creation_operations.append(UpdateSamplesOperation(creations[SampleCreationToUpdateType]))
        if ScriptCreationToUpdateType in creations:
            creation_operations.append(UpdatePluginsOperation(creations[ScriptCreationToUpdateType]))

        return creation_operations
