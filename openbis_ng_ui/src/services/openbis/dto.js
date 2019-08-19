const CLASS_FULL_NAMES = [
  'as/dto/person/search/PersonSearchCriteria',
  'as/dto/person/fetchoptions/PersonFetchOptions',
  'as/dto/person/id/PersonPermId',
  'as/dto/person/update/PersonUpdate',
  'as/dto/authorizationgroup/search/AuthorizationGroupSearchCriteria',
  'as/dto/authorizationgroup/fetchoptions/AuthorizationGroupFetchOptions',
  'as/dto/entitytype/id/EntityTypePermId',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/sample/update/SampleTypeUpdate',
  'as/dto/sample/search/SampleTypeSearchCriteria',
  'as/dto/sample/fetchoptions/SampleTypeFetchOptions',
  'as/dto/experiment/search/ExperimentTypeSearchCriteria',
  'as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions',
  'as/dto/dataset/search/DataSetTypeSearchCriteria',
  'as/dto/dataset/fetchoptions/DataSetTypeFetchOptions',
  'as/dto/material/search/MaterialTypeSearchCriteria',
  'as/dto/material/fetchoptions/MaterialTypeFetchOptions',
  'as/dto/property/PropertyType',
  'as/dto/property/id/PropertyTypePermId',
  'as/dto/property/search/PropertyTypeSearchCriteria',
  'as/dto/property/fetchoptions/PropertyTypeFetchOptions',
  'as/dto/property/create/PropertyAssignmentCreation',
  'as/dto/material/fetchoptions/MaterialFetchOptions',
  'as/dto/material/search/MaterialSearchCriteria',
  'as/dto/vocabulary/search/VocabularyTermSearchCriteria',
  'as/dto/vocabulary/fetchoptions/VocabularyTermFetchOptions',
  'as/dto/plugin/id/PluginPermId',
  'as/dto/webapp/create/WebAppSettingCreation',
]

class Dto {

  init(){
    let _this = this

    let load = function(index){
      return new Promise((resolve, reject) => {
        if(index < CLASS_FULL_NAMES.length){
          let classFullName = CLASS_FULL_NAMES[index]
          let className = classFullName.substring(classFullName.lastIndexOf('/') + 1)
          /* eslint-disable-next-line no-undef */
          requirejs([classFullName], clazz => {
            _this[className] = clazz
            return load(index + 1).then(resolve, reject)
          }, error => {
            reject(error)
          })
        }else{
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
  dto[className] = function(){}
})

export { dto }
