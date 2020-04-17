import _ from 'lodash'
import objectTypes from '@src/js/common/consts/objectType.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerLoad {
  constructor(controller) {
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async execute() {
    await this.context.setState({
      loading: true,
      validate: false
    })

    return this._load()
      .then(([loadedType, loadedUsages]) => {
        const sections = []
        const properties = []
        let section = null
        let property = null
        let sectionsCounter = 0
        let propertiesCounter = 0

        if (loadedType && loadedType.propertyAssignments) {
          loadedType.propertyAssignments.forEach(loadedAssignment => {
            property = this._createProperty(
              'property-' + propertiesCounter++,
              loadedAssignment,
              loadedUsages
            )
            properties.push(property)

            if (!section || section.name !== loadedAssignment.section) {
              section = this._createSection(
                'section-' + sectionsCounter++,
                loadedAssignment
              )
              sections.push(section)
            }

            section.properties.push(property.id)
            property.section = section.id
            property.original = {
              ...property
            }
          })
        }

        const type = this._createType(loadedType, loadedUsages)

        if (loadedType) {
          type.original = {
            ...type,
            properties
          }
        }

        const { selection } = this.context.getState()

        return this.context.setState(() => ({
          type,
          properties,
          propertiesCounter,
          sections,
          sectionsCounter,
          selection: selection ? selection : null,
          removeSectionDialogOpen: false,
          removePropertyDialogOpen: false
        }))
      })
      .catch(error => {
        this.facade.catch(error)
      })
      .finally(() => {
        this.context.setState({
          loading: false
        })
      })
  }

  _load() {
    const strategy = this._getStrategy()

    if (strategy.getNewObjectType() === this.object.type) {
      return Promise.resolve([null, null])
    } else if (strategy.getExistingObjectType() === this.object.type) {
      return Promise.all([
        this.facade.loadType(this.object),
        this.facade.loadUsages(this.object)
      ])
    }
  }

  _createType(loadedType, loadedUsages) {
    const strategy = this._getStrategy()
    const type = {
      code: _.get(loadedType, 'code', null),
      objectType: this.object.type,
      description: _.get(loadedType, 'description', null),
      validationPlugin: _.get(loadedType, 'validationPlugin.name', null),
      errors: {},
      usages: (loadedUsages && loadedUsages.type) || 0
    }
    strategy.setTypeAttributes(type, loadedType, loadedUsages)
    return type
  }

  _createSection(id, loadedAssignment) {
    return {
      id: id,
      name: loadedAssignment.section,
      properties: []
    }
  }

  _createProperty(id, loadedAssignment, loadedUsages) {
    const propertyType = loadedAssignment.propertyType
    return {
      id: id,
      code: _.get(propertyType, 'code', null),
      label: _.get(propertyType, 'label', null),
      description: _.get(propertyType, 'description', null),
      dataType: _.get(propertyType, 'dataType', null),
      plugin: _.get(loadedAssignment, 'plugin.name', null),
      vocabulary: _.get(propertyType, 'vocabulary.code', null),
      materialType: _.get(propertyType, 'materialType.code', null),
      schema: _.get(propertyType, 'schema', null),
      transformation: _.get(propertyType, 'transformation', null),
      mandatory: _.get(loadedAssignment, 'mandatory', false),
      showInEditView: _.get(loadedAssignment, 'showInEditView', false),
      showRawValueInForms: _.get(
        loadedAssignment,
        'showRawValueInForms',
        false
      ),
      errors: {},
      usages:
        (loadedUsages &&
          loadedUsages.property &&
          loadedUsages.property[loadedAssignment.propertyType.code]) ||
        0
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.setObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.setCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.setDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.setMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  getNewObjectType() {
    return objectTypes.NEW_OBJECT_TYPE
  }

  getExistingObjectType() {
    return objectTypes.OBJECT_TYPE
  }

  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      listable: _.get(loadedType, 'listable', false),
      showContainer: _.get(loadedType, 'showContainer', false),
      showParents: _.get(loadedType, 'showParents', false),
      showParentMetadata: _.get(loadedType, 'showParentMetadata', false),
      autoGeneratedCode: _.get(loadedType, 'autoGeneratedCode', false),
      generatedCodePrefix: _.get(loadedType, 'generatedCodePrefix', null),
      subcodeUnique: _.get(loadedType, 'subcodeUnique', false)
    })
  }
}

class CollectionTypeStrategy {
  getNewObjectType() {
    return objectTypes.NEW_COLLECTION_TYPE
  }

  getExistingObjectType() {
    return objectTypes.COLLECTION_TYPE
  }

  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  getNewObjectType() {
    return objectTypes.NEW_DATA_SET_TYPE
  }

  getExistingObjectType() {
    return objectTypes.DATA_SET_TYPE
  }

  setTypeAttributes(object, loadedType) {
    Object.assign(object, {
      mainDataSetPattern: _.get(loadedType, 'mainDataSetPattern', null),
      mainDataSetPath: _.get(loadedType, 'mainDataSetPath', null),
      disallowDeletion: _.get(loadedType, 'disallowDeletion', false)
    })
  }
}

class MaterialTypeStrategy {
  getNewObjectType() {
    return objectTypes.NEW_MATERIAL_TYPE
  }

  getExistingObjectType() {
    return objectTypes.MATERIAL_TYPE
  }

  setTypeAttributes() {}
}
