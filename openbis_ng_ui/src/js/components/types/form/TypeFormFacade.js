import openbis from '@src/js/services/openbis.js'

export default class TypeFormFacade {
  loadType(typeId) {
    const id = new openbis.EntityTypePermId(typeId)
    const fo = new openbis.SampleTypeFetchOptions()
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

    return openbis.getSampleTypes([id], fo).then(map => {
      return map[typeId]
    })
  }

  loadUsages(typeId) {
    function createTypeUsedOperation(typeId) {
      const criteria = new openbis.SampleSearchCriteria()
      criteria
        .withType()
        .withCode()
        .thatEquals(typeId)

      const fo = new openbis.SampleFetchOptions()
      fo.count(0)

      return new openbis.SearchSamplesOperation(criteria, fo)
    }

    function createPropertyUsedOperation(propertyTypeCode) {
      const criteria = new openbis.SampleSearchCriteria()
      criteria.withProperty(propertyTypeCode)

      const fo = new openbis.SampleFetchOptions()
      fo.count(0)

      return new openbis.SearchSamplesOperation(criteria, fo)
    }

    const id = new openbis.EntityTypePermId(typeId)
    const fo = new openbis.SampleTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()

    return openbis.getSampleTypes([id], fo).then(map => {
      const type = map[typeId]

      if (type) {
        const operations = []

        operations.push(createTypeUsedOperation(typeId))
        type.getPropertyAssignments().forEach(assignment => {
          operations.push(
            createPropertyUsedOperation(assignment.getPropertyType().getCode())
          )
        })

        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)

        return openbis.executeOperations(operations, options).then(result => {
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
    let criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.ENTITY_VALIDATION)
    let fo = new openbis.PluginFetchOptions()
    return openbis.searchPlugins(criteria, fo)
  }

  loadDynamicPlugins() {
    let criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.DYNAMIC_PROPERTY)
    let fo = new openbis.PluginFetchOptions()
    return openbis.searchPlugins(criteria, fo)
  }

  loadVocabularies() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis.searchVocabularies(criteria, fo)
  }

  loadVocabularyTerms(vocabulary) {
    let criteria = new openbis.VocabularyTermSearchCriteria()
    let fo = new openbis.VocabularyTermFetchOptions()

    criteria
      .withVocabulary()
      .withCode()
      .thatEquals(vocabulary)

    return openbis.searchVocabularyTerms(criteria, fo)
  }

  loadMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis.searchMaterialTypes(criteria, fo)
  }

  loadMaterials(materialType) {
    let criteria = new openbis.MaterialSearchCriteria()
    let fo = new openbis.MaterialFetchOptions()

    criteria
      .withType()
      .withCode()
      .thatEquals(materialType)

    return openbis.searchMaterials(criteria, fo)
  }

  executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }

  catch(error) {
    return openbis.catch(error)
  }
}
