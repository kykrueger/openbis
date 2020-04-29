import openbis from '@src/js/services/openbis.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormFacade {
  loadType(object) {
    const strategy = this._getStrategy(object.type)
    const id = new openbis.EntityTypePermId(object.id)
    const fo = strategy.createTypeFetchOptions()
    fo.withValidationPlugin()
    fo.withPropertyAssignments().withPlugin()
    fo.withPropertyAssignments().withPropertyType().withMaterialType()
    fo.withPropertyAssignments().withPropertyType().withVocabulary()
    fo.withPropertyAssignments().sortBy().ordinal()

    return strategy.getTypes([id], fo).then(map => {
      return map[object.id]
    })
  }

  loadUsages(object) {
    const strategy = this._getStrategy(object.type)

    function createTypeUsedOperation(typeId) {
      const criteria = strategy.createEntitySearchCriteria()
      criteria.withType().withCode().thatEquals(typeId)

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

  loadGlobalPropertyTypes() {
    const criteria = new openbis.PropertyTypeSearchCriteria()
    const fo = new openbis.PropertyTypeFetchOptions()
    fo.withMaterialType()
    fo.withVocabulary()
    return openbis.searchPropertyTypes(criteria, fo).then(results => {
      return results.getObjects().filter(propertyType => {
        return !propertyType.getCode().includes('.')
      })
    })
  }

  loadValidationPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.ENTITY_VALIDATION)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  loadDynamicPlugins(type) {
    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(openbis.PluginType.DYNAMIC_PROPERTY)
    const fo = new openbis.PluginFetchOptions()
    return this._loadPlugins(criteria, fo, type)
  }

  _loadPlugins(criteria, fo, type) {
    const strategy = this._getStrategy(type)
    return openbis.searchPlugins(criteria, fo).then(results => {
      return results.getObjects().filter(plugin => {
        return plugin.getEntityKinds().includes(strategy.getEntityKind())
      })
    })
  }

  loadVocabularies() {
    let criteria = new openbis.VocabularySearchCriteria()
    let fo = new openbis.VocabularyFetchOptions()
    return openbis
      .searchVocabularies(criteria, fo)
      .then(result => result.objects)
  }

  loadVocabularyTerms(vocabulary) {
    let criteria = new openbis.VocabularyTermSearchCriteria()
    let fo = new openbis.VocabularyTermFetchOptions()

    criteria.withVocabulary().withCode().thatEquals(vocabulary)

    return openbis
      .searchVocabularyTerms(criteria, fo)
      .then(result => result.objects)
  }

  loadMaterialTypes() {
    let criteria = new openbis.MaterialTypeSearchCriteria()
    let fo = new openbis.MaterialTypeFetchOptions()
    return openbis
      .searchMaterialTypes(criteria, fo)
      .then(result => result.objects)
  }

  loadMaterials(materialType) {
    let criteria = new openbis.MaterialSearchCriteria()
    let fo = new openbis.MaterialFetchOptions()

    criteria.withType().withCode().thatEquals(materialType)

    return openbis.searchMaterials(criteria, fo).then(result => result.objects)
  }

  executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }

  catch(error) {
    return openbis.catch(error)
  }

  _getStrategy(type) {
    const strategies = new TypeFormControllerStrategies()
    strategies.setObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.setCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.setDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.setMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(type)
  }
}

class ObjectTypeStrategy {
  getEntityKind() {
    return openbis.EntityKind.SAMPLE
  }
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
  getEntityKind() {
    return openbis.EntityKind.EXPERIMENT
  }
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
  getEntityKind() {
    return openbis.EntityKind.DATA_SET
  }
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
  getEntityKind() {
    return openbis.EntityKind.MATERIAL
  }
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
