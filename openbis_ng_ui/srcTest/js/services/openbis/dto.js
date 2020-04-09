import Person from 'as/dto/person/Person'
import PersonSearchCriteria from 'as/dto/person/search/PersonSearchCriteria'
import PersonFetchOptions from 'as/dto/person/fetchoptions/PersonFetchOptions'
import PersonPermId from 'as/dto/person/id/PersonPermId'
import PersonUpdate from 'as/dto/person/update/PersonUpdate'
import AuthorizationGroup from 'as/dto/authorizationgroup/AuthorizationGroup'
import AuthorizationGroupSearchCriteria from 'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria'
import AuthorizationGroupFetchOptions from 'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions'
import EntityTypePermId from 'as/dto/entitytype/id/EntityTypePermId'
import EntityKind from 'as/dto/entitytype/EntityKind'
import SampleFetchOptions from 'as/dto/sample/fetchoptions/SampleFetchOptions'
import SearchSamplesOperation from 'as/dto/sample/search/SearchSamplesOperation'
import SampleType from 'as/dto/sample/SampleType'
import SampleTypeCreation from 'as/dto/sample/create/SampleTypeCreation'
import SampleTypeUpdate from 'as/dto/sample/update/SampleTypeUpdate'
import CreateSampleTypesOperation from 'as/dto/sample/create/CreateSampleTypesOperation'
import UpdateSampleTypesOperation from 'as/dto/sample/update/UpdateSampleTypesOperation'
import SampleSearchCriteria from 'as/dto/sample/search/SampleSearchCriteria'
import SampleTypeSearchCriteria from 'as/dto/sample/search/SampleTypeSearchCriteria'
import SampleTypeFetchOptions from 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'
import SampleTypeDeletionOptions from 'as/dto/sample/delete/SampleTypeDeletionOptions'
import ExperimentType from 'as/dto/experiment/ExperimentType'
import ExperimentTypeSearchCriteria from 'as/dto/experiment/search/ExperimentTypeSearchCriteria'
import ExperimentTypeFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'
import ExperimentTypeDeletionOptions from 'as/dto/experiment/delete/ExperimentTypeDeletionOptions'
import DataSetType from 'as/dto/dataset/DataSetType'
import DataSetTypeSearchCriteria from 'as/dto/dataset/search/DataSetTypeSearchCriteria'
import DataSetTypeFetchOptions from 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'
import DataSetTypeDeletionOptions from 'as/dto/dataset/delete/DataSetTypeDeletionOptions'
import MaterialType from 'as/dto/material/MaterialType'
import MaterialTypeSearchCriteria from 'as/dto/material/search/MaterialTypeSearchCriteria'
import MaterialTypeFetchOptions from 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'
import MaterialTypeDeletionOptions from 'as/dto/material/delete/MaterialTypeDeletionOptions'
import PropertyType from 'as/dto/property/PropertyType'
import PropertyTypeCreation from 'as/dto/property/create/PropertyTypeCreation'
import CreatePropertyTypesOperation from 'as/dto/property/create/CreatePropertyTypesOperation'
import DeletePropertyTypesOperation from 'as/dto/property/delete/DeletePropertyTypesOperation'
import PropertyTypeDeletionOptions from 'as/dto/property/delete/PropertyTypeDeletionOptions'
import UpdatePropertyTypesOperation from 'as/dto/property/update/UpdatePropertyTypesOperation'
import PropertyTypeUpdate from 'as/dto/property/update/PropertyTypeUpdate'
import PropertyTypePermId from 'as/dto/property/id/PropertyTypePermId'
import PropertyAssignment from 'as/dto/property/PropertyAssignment'
import PropertyAssignmentPermId from 'as/dto/property/id/PropertyAssignmentPermId'
import PropertyTypeSearchCriteria from 'as/dto/property/search/PropertyTypeSearchCriteria'
import PropertyTypeFetchOptions from 'as/dto/property/fetchoptions/PropertyTypeFetchOptions'
import PropertyAssignmentCreation from 'as/dto/property/create/PropertyAssignmentCreation'
import MaterialFetchOptions from 'as/dto/material/fetchoptions/MaterialFetchOptions'
import MaterialSearchCriteria from 'as/dto/material/search/MaterialSearchCriteria'
import Vocabulary from 'as/dto/vocabulary/Vocabulary'
import VocabularyPermId from 'as/dto/vocabulary/id/VocabularyPermId'
import VocabularySearchCriteria from 'as/dto/vocabulary/search/VocabularySearchCriteria'
import VocabularyFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions'
import VocabularyTermSearchCriteria from 'as/dto/vocabulary/search/VocabularyTermSearchCriteria'
import VocabularyTermFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions'
import Plugin from 'as/dto/plugin/Plugin'
import PluginPermId from 'as/dto/plugin/id/PluginPermId'
import PluginType from 'as/dto/plugin/PluginType'
import PluginSearchCriteria from 'as/dto/plugin/search/PluginSearchCriteria'
import PluginFetchOptions from 'as/dto/plugin/fetchoptions/PluginFetchOptions'
import WebAppSettingCreation from 'as/dto/webapp/create/WebAppSettingCreation'
import CustomASServiceCode from 'as/dto/service/id/CustomASServiceCode'
import CustomASServiceExecutionOptions from 'as/dto/service/CustomASServiceExecutionOptions'
import SynchronousOperationExecutionOptions from 'as/dto/operation/SynchronousOperationExecutionOptions'
import DataType from 'as/dto/property/DataType'
import SearchResult from 'as/dto/common/search/SearchResult'

const dto = {
  Person,
  PersonSearchCriteria,
  PersonFetchOptions,
  PersonPermId,
  PersonUpdate,
  AuthorizationGroup,
  AuthorizationGroupSearchCriteria,
  AuthorizationGroupFetchOptions,
  EntityTypePermId,
  EntityKind,
  SampleFetchOptions,
  SearchSamplesOperation,
  SampleType,
  SampleTypeCreation,
  SampleTypeUpdate,
  CreateSampleTypesOperation,
  UpdateSampleTypesOperation,
  SampleSearchCriteria,
  SampleTypeSearchCriteria,
  SampleTypeFetchOptions,
  SampleTypeDeletionOptions,
  ExperimentType,
  ExperimentTypeSearchCriteria,
  ExperimentTypeFetchOptions,
  ExperimentTypeDeletionOptions,
  DataSetType,
  DataSetTypeSearchCriteria,
  DataSetTypeFetchOptions,
  DataSetTypeDeletionOptions,
  MaterialType,
  MaterialTypeSearchCriteria,
  MaterialTypeFetchOptions,
  MaterialTypeDeletionOptions,
  PropertyType,
  PropertyTypeCreation,
  CreatePropertyTypesOperation,
  DeletePropertyTypesOperation,
  PropertyTypeDeletionOptions,
  UpdatePropertyTypesOperation,
  PropertyTypeUpdate,
  PropertyTypePermId,
  PropertyAssignment,
  PropertyAssignmentPermId,
  PropertyTypeSearchCriteria,
  PropertyTypeFetchOptions,
  PropertyAssignmentCreation,
  MaterialFetchOptions,
  MaterialSearchCriteria,
  Vocabulary,
  VocabularyPermId,
  VocabularySearchCriteria,
  VocabularyFetchOptions,
  VocabularyTermSearchCriteria,
  VocabularyTermFetchOptions,
  Plugin,
  PluginPermId,
  PluginType,
  PluginSearchCriteria,
  PluginFetchOptions,
  WebAppSettingCreation,
  CustomASServiceCode,
  CustomASServiceExecutionOptions,
  SynchronousOperationExecutionOptions,
  DataType,
  SearchResult
}

export default dto
