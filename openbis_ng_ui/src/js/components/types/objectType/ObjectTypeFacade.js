import { dto, facade } from '../../../services/openbis.js'

export default class ObjectTypeFacade {
  loadType(typeId) {
    const id = new dto.EntityTypePermId(typeId)
    const fo = new dto.SampleTypeFetchOptions()
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

  executeOperations(operations, options) {
    return facade.executeOperations(operations, options)
  }
}
