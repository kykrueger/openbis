import { dto, facade } from '../../../services/openbis.js'

export default class ObjectTypeFacade {
  loadType(typeId) {
    const id = new dto.EntityTypePermId(typeId)
    const fo = new dto.SampleTypeFetchOptions()
    fo.withValidationPlugin()
    fo.withPropertyAssignments().withPlugin()
    fo.withPropertyAssignments()
      .withPropertyType()
      .withMaterialType()
    fo.withPropertyAssignments()
      .withPropertyType()
      .withVocabulary()
    fo.withPropertyAssignments()
      .sortBy()
      .ordinal()

    return facade.getSampleTypes([id], fo).then(map => {
      return map[typeId]
    })
  }

  loadUsages(typeId) {
    function createTypeUsedOperation(typeId) {
      const criteria = new dto.SampleSearchCriteria()
      criteria
        .withType()
        .withCode()
        .thatEquals(typeId)

      const fo = new dto.SampleFetchOptions()
      fo.count(0)

      return new dto.SearchSamplesOperation(criteria, fo)
    }

    function createPropertyUsedOperation(propertyTypeCode) {
      const criteria = new dto.SampleSearchCriteria()
      criteria.withProperty(propertyTypeCode).thatEquals('*')

      const fo = new dto.SampleFetchOptions()
      fo.count(0)

      return new dto.SearchSamplesOperation(criteria, fo)
    }

    const id = new dto.EntityTypePermId(typeId)
    const fo = new dto.SampleTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()

    return facade.getSampleTypes([id], fo).then(map => {
      const type = map[typeId]

      if (type) {
        const operations = []

        operations.push(createTypeUsedOperation(typeId))
        type.getPropertyAssignments().forEach(assignment => {
          operations.push(
            createPropertyUsedOperation(assignment.getPropertyType().getCode())
          )
        })

        const options = new dto.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)

        return facade.executeOperations(operations, options).then(result => {
          const results = result.getResults()
          const map = { property: {} }

          map.type = results[0].getSearchResult().getTotalCount()
          type.getPropertyAssignments().forEach((assignment, index) => {
            map.property[assignment.getPropertyType().getCode()] = results[
              index + 1
            ]
              .getSearchResult()
              .getTotalCount()
          })

          return map
        })
      } else {
        return {}
      }
    })
  }

  loadValidationPlugins() {
    let criteria = new dto.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(dto.PluginType.ENTITY_VALIDATION)
    let fo = new dto.PluginFetchOptions()
    return facade.searchPlugins(criteria, fo)
  }

  loadDynamicPlugins() {
    let criteria = new dto.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(dto.PluginType.DYNAMIC_PROPERTY)
    let fo = new dto.PluginFetchOptions()
    return facade.searchPlugins(criteria, fo)
  }

  loadVocabularies() {
    let criteria = new dto.VocabularySearchCriteria()
    let fo = new dto.VocabularyFetchOptions()
    return facade.searchVocabularies(criteria, fo)
  }

  loadVocabularyTerms(vocabulary) {
    let criteria = new dto.VocabularyTermSearchCriteria()
    let fo = new dto.VocabularyTermFetchOptions()

    criteria
      .withVocabulary()
      .withCode()
      .thatEquals(vocabulary)

    return facade.searchVocabularyTerms(criteria, fo)
  }

  loadMaterialTypes() {
    let criteria = new dto.MaterialTypeSearchCriteria()
    let fo = new dto.MaterialTypeFetchOptions()
    return facade.searchMaterialTypes(criteria, fo)
  }

  loadMaterials(materialType) {
    let criteria = new dto.MaterialSearchCriteria()
    let fo = new dto.MaterialFetchOptions()

    criteria
      .withType()
      .withCode()
      .thatEquals(materialType)

    return facade.searchMaterials(criteria, fo)
  }

  executeOperations(operations, options) {
    return facade.executeOperations(operations, options)
  }

  catch(error) {
    return facade.catch(error)
  }
}
