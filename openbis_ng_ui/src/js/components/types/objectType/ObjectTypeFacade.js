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

  loadTypePropertyTypes(typeId) {
    const criteria = new dto.PropertyTypeSearchCriteria()
    criteria.withCode().thatStartsWith(typeId + '.')

    const fo = new dto.PropertyTypeFetchOptions()
    fo.withVocabulary()
    fo.withMaterialType()

    return facade.searchPropertyTypes(criteria, fo).then(result => {
      return result.objects
    })
  }

  loadTypeLatestEntity(typeId) {
    const criteria = new dto.SampleSearchCriteria()
    criteria
      .withType()
      .withCode()
      .thatEquals(typeId)

    const fo = new dto.SampleFetchOptions()
    fo.sortBy()
      .modificationDate()
      .desc()
    fo.count(1)

    return facade.searchSamples(criteria, fo).then(result => {
      if (result.objects.length > 0) {
        return result.objects[0]
      } else {
        return null
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
