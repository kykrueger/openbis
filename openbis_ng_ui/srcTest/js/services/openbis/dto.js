import AuthorizationGroup from 'as/dto/authorizationgroup/AuthorizationGroup'
import AuthorizationGroupFetchOptions from 'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions'
import AuthorizationGroupSearchCriteria from 'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria'
import CreateDataSetTypesOperation from 'as/dto/dataset/create/CreateDataSetTypesOperation'
import CreateExperimentTypesOperation from 'as/dto/experiment/create/CreateExperimentTypesOperation'
import CreateMaterialTypesOperation from 'as/dto/material/create/CreateMaterialTypesOperation'
import CreatePropertyTypesOperation from 'as/dto/property/create/CreatePropertyTypesOperation'
import CreateSampleTypesOperation from 'as/dto/sample/create/CreateSampleTypesOperation'
import CustomASServiceCode from 'as/dto/service/id/CustomASServiceCode'
import CustomASServiceExecutionOptions from 'as/dto/service/CustomASServiceExecutionOptions'
import DataSetFetchOptions from 'as/dto/dataset/fetchoptions/DataSetFetchOptions'
import DataSetSearchCriteria from 'as/dto/dataset/search/DataSetSearchCriteria'
import DataSetType from 'as/dto/dataset/DataSetType'
import DataSetTypeCreation from 'as/dto/dataset/create/DataSetTypeCreation'
import DataSetTypeDeletionOptions from 'as/dto/dataset/delete/DataSetTypeDeletionOptions'
import DataSetTypeFetchOptions from 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'
import DataSetTypeSearchCriteria from 'as/dto/dataset/search/DataSetTypeSearchCriteria'
import DataSetTypeUpdate from 'as/dto/dataset/update/DataSetTypeUpdate'
import DataType from 'as/dto/property/DataType'
import DeleteDataSetTypesOperation from 'as/dto/dataset/delete/DeleteDataSetTypesOperation'
import DeleteExperimentTypesOperation from 'as/dto/experiment/delete/DeleteExperimentTypesOperation'
import DeleteMaterialTypesOperation from 'as/dto/material/delete/DeleteMaterialTypesOperation'
import DeletePropertyTypesOperation from 'as/dto/property/delete/DeletePropertyTypesOperation'
import DeleteSampleTypesOperation from 'as/dto/sample/delete/DeleteSampleTypesOperation'
import EntityKind from 'as/dto/entitytype/EntityKind'
import EntityTypePermId from 'as/dto/entitytype/id/EntityTypePermId'
import ExperimentFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentFetchOptions'
import ExperimentSearchCriteria from 'as/dto/experiment/search/ExperimentSearchCriteria'
import ExperimentType from 'as/dto/experiment/ExperimentType'
import ExperimentTypeCreation from 'as/dto/experiment/create/ExperimentTypeCreation'
import ExperimentTypeDeletionOptions from 'as/dto/experiment/delete/ExperimentTypeDeletionOptions'
import ExperimentTypeFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'
import ExperimentTypeSearchCriteria from 'as/dto/experiment/search/ExperimentTypeSearchCriteria'
import ExperimentTypeUpdate from 'as/dto/experiment/update/ExperimentTypeUpdate'
import MaterialFetchOptions from 'as/dto/material/fetchoptions/MaterialFetchOptions'
import MaterialSearchCriteria from 'as/dto/material/search/MaterialSearchCriteria'
import MaterialType from 'as/dto/material/MaterialType'
import MaterialTypeCreation from 'as/dto/material/create/MaterialTypeCreation'
import MaterialTypeDeletionOptions from 'as/dto/material/delete/MaterialTypeDeletionOptions'
import MaterialTypeFetchOptions from 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'
import MaterialTypeSearchCriteria from 'as/dto/material/search/MaterialTypeSearchCriteria'
import MaterialTypeUpdate from 'as/dto/material/update/MaterialTypeUpdate'
import Person from 'as/dto/person/Person'
import PersonFetchOptions from 'as/dto/person/fetchoptions/PersonFetchOptions'
import PersonPermId from 'as/dto/person/id/PersonPermId'
import PersonSearchCriteria from 'as/dto/person/search/PersonSearchCriteria'
import PersonUpdate from 'as/dto/person/update/PersonUpdate'
import Plugin from 'as/dto/plugin/Plugin'
import PluginFetchOptions from 'as/dto/plugin/fetchoptions/PluginFetchOptions'
import PluginPermId from 'as/dto/plugin/id/PluginPermId'
import PluginSearchCriteria from 'as/dto/plugin/search/PluginSearchCriteria'
import PluginType from 'as/dto/plugin/PluginType'
import PropertyAssignment from 'as/dto/property/PropertyAssignment'
import PropertyAssignmentCreation from 'as/dto/property/create/PropertyAssignmentCreation'
import PropertyAssignmentFetchOptions from 'as/dto/property/fetchoptions/PropertyAssignmentFetchOptions'
import PropertyAssignmentPermId from 'as/dto/property/id/PropertyAssignmentPermId'
import PropertyAssignmentSearchCriteria from 'as/dto/property/search/PropertyAssignmentSearchCriteria'
import PropertyType from 'as/dto/property/PropertyType'
import PropertyTypeCreation from 'as/dto/property/create/PropertyTypeCreation'
import PropertyTypeDeletionOptions from 'as/dto/property/delete/PropertyTypeDeletionOptions'
import PropertyTypeFetchOptions from 'as/dto/property/fetchoptions/PropertyTypeFetchOptions'
import PropertyTypePermId from 'as/dto/property/id/PropertyTypePermId'
import PropertyTypeSearchCriteria from 'as/dto/property/search/PropertyTypeSearchCriteria'
import PropertyTypeUpdate from 'as/dto/property/update/PropertyTypeUpdate'
import SampleFetchOptions from 'as/dto/sample/fetchoptions/SampleFetchOptions'
import SampleSearchCriteria from 'as/dto/sample/search/SampleSearchCriteria'
import SampleType from 'as/dto/sample/SampleType'
import SampleTypeCreation from 'as/dto/sample/create/SampleTypeCreation'
import SampleTypeDeletionOptions from 'as/dto/sample/delete/SampleTypeDeletionOptions'
import SampleTypeFetchOptions from 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'
import SampleTypeSearchCriteria from 'as/dto/sample/search/SampleTypeSearchCriteria'
import SampleTypeUpdate from 'as/dto/sample/update/SampleTypeUpdate'
import SearchDataSetsOperation from 'as/dto/dataset/search/SearchDataSetsOperation'
import SearchExperimentsOperation from 'as/dto/experiment/search/SearchExperimentsOperation'
import SearchMaterialsOperation from 'as/dto/material/search/SearchMaterialsOperation'
import SearchResult from 'as/dto/common/search/SearchResult'
import SearchSamplesOperation from 'as/dto/sample/search/SearchSamplesOperation'
import SynchronousOperationExecutionOptions from 'as/dto/operation/SynchronousOperationExecutionOptions'
import UpdateDataSetTypesOperation from 'as/dto/dataset/update/UpdateDataSetTypesOperation'
import UpdateExperimentTypesOperation from 'as/dto/experiment/update/UpdateExperimentTypesOperation'
import UpdateMaterialTypesOperation from 'as/dto/material/update/UpdateMaterialTypesOperation'
import UpdatePropertyTypesOperation from 'as/dto/property/update/UpdatePropertyTypesOperation'
import UpdateSampleTypesOperation from 'as/dto/sample/update/UpdateSampleTypesOperation'
import Vocabulary from 'as/dto/vocabulary/Vocabulary'
import VocabularyFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions'
import VocabularyPermId from 'as/dto/vocabulary/id/VocabularyPermId'
import VocabularySearchCriteria from 'as/dto/vocabulary/search/VocabularySearchCriteria'
import VocabularyTermFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions'
import VocabularyTermSearchCriteria from 'as/dto/vocabulary/search/VocabularyTermSearchCriteria'
import WebAppSettingCreation from 'as/dto/webapp/create/WebAppSettingCreation'

const dto = {
  AuthorizationGroup,
  AuthorizationGroupFetchOptions,
  AuthorizationGroupSearchCriteria,
  CreateDataSetTypesOperation,
  CreateExperimentTypesOperation,
  CreateMaterialTypesOperation,
  CreatePropertyTypesOperation,
  CreateSampleTypesOperation,
  CustomASServiceCode,
  CustomASServiceExecutionOptions,
  DataSetFetchOptions,
  DataSetSearchCriteria,
  DataSetType,
  DataSetTypeCreation,
  DataSetTypeDeletionOptions,
  DataSetTypeFetchOptions,
  DataSetTypeSearchCriteria,
  DataSetTypeUpdate,
  DataType,
  DeleteDataSetTypesOperation,
  DeleteExperimentTypesOperation,
  DeleteMaterialTypesOperation,
  DeletePropertyTypesOperation,
  DeleteSampleTypesOperation,
  EntityKind,
  EntityTypePermId,
  ExperimentFetchOptions,
  ExperimentSearchCriteria,
  ExperimentType,
  ExperimentTypeCreation,
  ExperimentTypeDeletionOptions,
  ExperimentTypeFetchOptions,
  ExperimentTypeSearchCriteria,
  ExperimentTypeUpdate,
  MaterialFetchOptions,
  MaterialSearchCriteria,
  MaterialType,
  MaterialTypeCreation,
  MaterialTypeDeletionOptions,
  MaterialTypeFetchOptions,
  MaterialTypeSearchCriteria,
  MaterialTypeUpdate,
  Person,
  PersonFetchOptions,
  PersonPermId,
  PersonSearchCriteria,
  PersonUpdate,
  Plugin,
  PluginFetchOptions,
  PluginPermId,
  PluginSearchCriteria,
  PluginType,
  PropertyAssignment,
  PropertyAssignmentCreation,
  PropertyAssignmentFetchOptions,
  PropertyAssignmentPermId,
  PropertyAssignmentSearchCriteria,
  PropertyType,
  PropertyTypeCreation,
  PropertyTypeDeletionOptions,
  PropertyTypeFetchOptions,
  PropertyTypePermId,
  PropertyTypeSearchCriteria,
  PropertyTypeUpdate,
  SampleFetchOptions,
  SampleSearchCriteria,
  SampleType,
  SampleTypeCreation,
  SampleTypeDeletionOptions,
  SampleTypeFetchOptions,
  SampleTypeSearchCriteria,
  SampleTypeUpdate,
  SearchDataSetsOperation,
  SearchExperimentsOperation,
  SearchMaterialsOperation,
  SearchResult,
  SearchSamplesOperation,
  SynchronousOperationExecutionOptions,
  UpdateDataSetTypesOperation,
  UpdateExperimentTypesOperation,
  UpdateMaterialTypesOperation,
  UpdatePropertyTypesOperation,
  UpdateSampleTypesOperation,
  Vocabulary,
  VocabularyFetchOptions,
  VocabularyPermId,
  VocabularySearchCriteria,
  VocabularyTermFetchOptions,
  VocabularyTermSearchCriteria,
  WebAppSettingCreation
}

export default dto
