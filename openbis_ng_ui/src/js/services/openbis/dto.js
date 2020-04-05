const CLASS_FULL_NAMES = [
  'as/dto/person/search/PersonSearchCriteria',
  'as/dto/person/fetchoptions/PersonFetchOptions',
  'as/dto/person/id/PersonPermId',
  'as/dto/person/update/PersonUpdate',
  'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria',
  'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions',
  'as/dto/entitytype/id/EntityTypePermId',
  'as/dto/entitytype/EntityKind',
  'as/dto/sample/fetchoptions/SampleFetchOptions',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/sample/search/SearchSamplesOperation',
  'as/dto/sample/create/SampleTypeCreation',
  'as/dto/sample/update/SampleTypeUpdate',
  'as/dto/sample/create/CreateSampleTypesOperation',
  'as/dto/sample/update/UpdateSampleTypesOperation',
  'as/dto/sample/search/SampleSearchCriteria',
  'as/dto/sample/search/SampleTypeSearchCriteria',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/experiment/search/ExperimentTypeSearchCriteria',
  'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions',
  'as/dto/dataset/search/DataSetTypeSearchCriteria',
  'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions',
  'as/dto/material/search/MaterialTypeSearchCriteria',
  'as/dto/material/fetchoptions/MaterialTypeFetchOptions',
  'as/dto/property/PropertyType',
  'as/dto/property/create/PropertyTypeCreation',
  'as/dto/property/create/CreatePropertyTypesOperation',
  'as/dto/property/delete/DeletePropertyTypesOperation',
  'as/dto/property/delete/PropertyTypeDeletionOptions',
  'as/dto/property/update/UpdatePropertyTypesOperation',
  'as/dto/property/update/PropertyTypeUpdate',
  'as/dto/property/id/PropertyTypePermId',
  'as/dto/property/id/PropertyAssignmentPermId',
  'as/dto/property/search/PropertyTypeSearchCriteria',
  'as/dto/property/fetchoptions/PropertyTypeFetchOptions',
  'as/dto/property/create/PropertyAssignmentCreation',
  'as/dto/material/fetchoptions/MaterialFetchOptions',
  'as/dto/material/search/MaterialSearchCriteria',
  'as/dto/vocabulary/id/VocabularyPermId',
  'as/dto/vocabulary/search/VocabularySearchCriteria',
  'as/dto/vocabulary/fetchoptions/VocabularyFetchOptions',
  'as/dto/vocabulary/search/VocabularyTermSearchCriteria',
  'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions',
  'as/dto/plugin/id/PluginPermId',
  'as/dto/plugin/PluginType',
  'as/dto/plugin/search/PluginSearchCriteria',
  'as/dto/plugin/fetchoptions/PluginFetchOptions',
  'as/dto/webapp/create/WebAppSettingCreation',
  'as/dto/service/id/CustomASServiceCode',
  'as/dto/service/CustomASServiceExecutionOptions',
  'as/dto/operation/SynchronousOperationExecutionOptions',
  'as/dto/property/DataType'
]

class Dto {
  _init() {
    let _this = this

    let load = function(index) {
      return new Promise((resolve, reject) => {
        if (index < CLASS_FULL_NAMES.length) {
          let classFullName = CLASS_FULL_NAMES[index]
          let className = classFullName.substring(
            classFullName.lastIndexOf('/') + 1
          )
          /* eslint-disable-next-line no-undef */
          requirejs(
            [classFullName],
            clazz => {
              _this[className] = clazz
              return load(index + 1).then(resolve, reject)
            },
            error => {
              reject(error)
            }
          )
        } else {
          resolve()
        }
      })
    }

    return load(0)
  }
}

const dto = new Dto()

CLASS_FULL_NAMES.forEach(classFullName => {
  let className = classFullName.substring(classFullName.lastIndexOf('/') + 1)
  dto[className] = function() {}
})

export default dto
