import objectTypes from '@src/js/common/consts/objectType.js'

export default class TypeFormControllerStrategies {
  constructor() {
    this.objectTypeStrategy = null
    this.collectionTypeStrategy = null
    this.dataSetTypeStrategy = null
    this.materialTypeStrategy = null
  }

  static isObjectType(type) {
    return (
      type === objectTypes.NEW_OBJECT_TYPE || type === objectTypes.OBJECT_TYPE
    )
  }

  static isCollectionType(type) {
    return (
      type === objectTypes.NEW_COLLECTION_TYPE ||
      type === objectTypes.COLLECTION_TYPE
    )
  }

  static isDataSetType(type) {
    return (
      type === objectTypes.NEW_DATA_SET_TYPE ||
      type === objectTypes.DATA_SET_TYPE
    )
  }

  static isMaterialType(type) {
    return (
      type === objectTypes.NEW_MATERIAL_TYPE ||
      type === objectTypes.MATERIAL_TYPE
    )
  }

  setObjectTypeStrategy(strategy) {
    this.objectTypeStrategy = strategy
  }

  setCollectionTypeStrategy(strategy) {
    this.collectionTypeStrategy = strategy
  }

  setDataSetTypeStrategy(strategy) {
    this.dataSetTypeStrategy = strategy
  }

  setMaterialTypeStrategy(strategy) {
    this.materialTypeStrategy = strategy
  }

  getStrategy(type) {
    if (TypeFormControllerStrategies.isObjectType(type)) {
      return this.objectTypeStrategy
    } else if (TypeFormControllerStrategies.isCollectionType(type)) {
      return this.collectionTypeStrategy
    } else if (TypeFormControllerStrategies.isDataSetType(type)) {
      return this.dataSetTypeStrategy
    } else if (TypeFormControllerStrategies.isMaterialType(type)) {
      return this.materialTypeStrategy
    } else {
      throw 'Unsupported type: ' + type
    }
  }
}
