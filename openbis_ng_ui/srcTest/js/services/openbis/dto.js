import PersonSearchCriteria from 'as/dto/person/search/PersonSearchCriteria'
import PersonFetchOptions from 'as/dto/person/fetchoptions/PersonFetchOptions'
import PersonPermId from 'as/dto/person/id/PersonPermId'
import PersonUpdate from 'as/dto/person/update/PersonUpdate'
import AuthorizationGroupSearchCriteria from 'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria'
import AuthorizationGroupFetchOptions from 'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions'
import EntityTypePermId from 'as/dto/entitytype/id/EntityTypePermId'
import EntityKind from 'as/dto/entitytype/EntityKind'
import SampleFetchOptions from 'as/dto/sample/fetchoptions/SampleFetchOptions'
import SearchSamplesOperation from 'as/dto/sample/search/SearchSamplesOperation'
import SampleTypeUpdate from 'as/dto/sample/update/SampleTypeUpdate'
import UpdateSampleTypesOperation from 'as/dto/sample/update/UpdateSampleTypesOperation'
import SampleSearchCriteria from 'as/dto/sample/search/SampleSearchCriteria'
import SampleTypeSearchCriteria from 'as/dto/sample/search/SampleTypeSearchCriteria'
import SampleTypeFetchOptions from 'as/dto/sample/fetchoptions/SampleTypeFetchOptions'
import ExperimentTypeSearchCriteria from 'as/dto/experiment/search/ExperimentTypeSearchCriteria'
import ExperimentTypeFetchOptions from 'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions'
import DataSetTypeSearchCriteria from 'as/dto/dataset/search/DataSetTypeSearchCriteria'
import DataSetTypeFetchOptions from 'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions'
import MaterialTypeSearchCriteria from 'as/dto/material/search/MaterialTypeSearchCriteria'
import MaterialTypeFetchOptions from 'as/dto/material/fetchoptions/MaterialTypeFetchOptions'
import PropertyType from 'as/dto/property/PropertyType'
import PropertyTypeCreation from 'as/dto/property/create/PropertyTypeCreation'
import CreatePropertyTypesOperation from 'as/dto/property/create/CreatePropertyTypesOperation'
import DeletePropertyTypesOperation from 'as/dto/property/delete/DeletePropertyTypesOperation'
import PropertyTypeDeletionOptions from 'as/dto/property/delete/PropertyTypeDeletionOptions'
import UpdatePropertyTypesOperation from 'as/dto/property/update/UpdatePropertyTypesOperation'
import PropertyTypeUpdate from 'as/dto/property/update/PropertyTypeUpdate'
import PropertyTypePermId from 'as/dto/property/id/PropertyTypePermId'
import PropertyAssignmentPermId from 'as/dto/property/id/PropertyAssignmentPermId'
import PropertyTypeSearchCriteria from 'as/dto/property/search/PropertyTypeSearchCriteria'
import PropertyTypeFetchOptions from 'as/dto/property/fetchoptions/PropertyTypeFetchOptions'
import PropertyAssignmentCreation from 'as/dto/property/create/PropertyAssignmentCreation'
import MaterialFetchOptions from 'as/dto/material/fetchoptions/MaterialFetchOptions'
import MaterialSearchCriteria from 'as/dto/material/search/MaterialSearchCriteria'
import VocabularyPermId from 'as/dto/vocabulary/id/VocabularyPermId'
import VocabularySearchCriteria from 'as/dto/vocabulary/search/VocabularySearchCriteria'
import VocabularyFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions'
import VocabularyTermSearchCriteria from 'as/dto/vocabulary/search/VocabularyTermSearchCriteria'
import VocabularyTermFetchOptions from 'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions'
import PluginPermId from 'as/dto/plugin/id/PluginPermId'
import PluginType from 'as/dto/plugin/PluginType'
import PluginSearchCriteria from 'as/dto/plugin/search/PluginSearchCriteria'
import PluginFetchOptions from 'as/dto/plugin/fetchoptions/PluginFetchOptions'
import WebAppSettingCreation from 'as/dto/webapp/create/WebAppSettingCreation'
import CustomASServiceCode from 'as/dto/service/id/CustomASServiceCode'
import CustomASServiceExecutionOptions from 'as/dto/service/CustomASServiceExecutionOptions'
import SynchronousOperationExecutionOptions from 'as/dto/operation/SynchronousOperationExecutionOptions'
import DataType from 'as/dto/property/DataType'

const dto = {
  PersonSearchCriteria,
  PersonFetchOptions,
  PersonPermId,
  PersonUpdate,
  AuthorizationGroupSearchCriteria,
  AuthorizationGroupFetchOptions,
  EntityTypePermId,
  EntityKind,
  SampleFetchOptions,
  SearchSamplesOperation,
  SampleTypeUpdate,
  UpdateSampleTypesOperation,
  SampleSearchCriteria,
  SampleTypeSearchCriteria,
  SampleTypeFetchOptions,
  ExperimentTypeSearchCriteria,
  ExperimentTypeFetchOptions,
  DataSetTypeSearchCriteria,
  DataSetTypeFetchOptions,
  MaterialTypeSearchCriteria,
  MaterialTypeFetchOptions,
  PropertyType,
  PropertyTypeCreation,
  CreatePropertyTypesOperation,
  DeletePropertyTypesOperation,
  PropertyTypeDeletionOptions,
  UpdatePropertyTypesOperation,
  PropertyTypeUpdate,
  PropertyTypePermId,
  PropertyAssignmentPermId,
  PropertyTypeSearchCriteria,
  PropertyTypeFetchOptions,
  PropertyAssignmentCreation,
  MaterialFetchOptions,
  MaterialSearchCriteria,
  VocabularyPermId,
  VocabularySearchCriteria,
  VocabularyFetchOptions,
  VocabularyTermSearchCriteria,
  VocabularyTermFetchOptions,
  PluginPermId,
  PluginType,
  PluginSearchCriteria,
  PluginFetchOptions,
  WebAppSettingCreation,
  CustomASServiceCode,
  CustomASServiceExecutionOptions,
  SynchronousOperationExecutionOptions,
  DataType
}

export default dto
