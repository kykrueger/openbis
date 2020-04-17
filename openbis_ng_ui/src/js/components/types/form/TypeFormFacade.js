import openbis from '@src/js/services/openbis.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormFacade {
  loadType(object) {
    const strategy = this._getStrategy(object)
    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
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

    return strategy.getTypes([id], fo).then(map => {
      return map[object.id]
    })
  }

  loadUsages(object) {
    const strategy = this._getStrategy(object)

    function createTypeUsedOperation(typeId) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria
        .withType()
        .withCode()
        .thatEquals(typeId)

      const fo = strategy.createEntityFetchOptions()
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    function createPropertyUsedOperation(propertyTypeCode) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withProperty(propertyTypeCode)

      const fo = strategy.createEntityFetchOptions()
      fo.count(0)

      return strategy.createEntitySearchOperation(criteria, fo)
    }

    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withPropertyAssignments().withPropertyType()

    return strategy.getTypes([id], fo).then(map => {
      const type = map[object.id]

      if (type) {
        const operations = []

        operations.push(createTypeUsedOperation(object.id))
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

  _getStrategy(object) {
    const strategies = new TypeFormControllerStrategies()
    strategies.setObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.setCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.setDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.setMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(object.type)
  }
}

class ObjectTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.SampleTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.SampleFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.SampleSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchSamplesOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getSampleTypes(ids, fo)
  }
}

class CollectionTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.ExperimentTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.ExperimentFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.ExperimentSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchExperimentsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getExperimentTypes(ids, fo)
  }
}

class DataSetTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.DataSetTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.DataSetFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.DataSetSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchDataSetsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getDataSetTypes(ids, fo)
  }
}

class MaterialTypeStrategy {
  createTypeFetchOptions() {
    return new openbis.MaterialTypeFetchOptions()
  }
  createEntityFetchOptions() {
    return new openbis.MaterialFetchOptions()
  }
  createEntitySearchCriteria() {
    return new openbis.MaterialSearchCriteria()
  }
  createEntitySearchOperation(criteria, fo) {
    return new openbis.SearchMaterialsOperation(criteria, fo)
  }
  getTypes(ids, fo) {
    return openbis.getMaterialTypes(ids, fo)
  }
}
