import _ from 'lodash'
import pages from '@src/js/common/consts/pages.js'
import actions from '@src/js/store/actions/actions.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'
import TypeFormUtil from './TypeFormUtil.js'

export default class TypeFormControllerSave {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object

    let { type, properties, sections } = this.context.getState()

    this.type = this._prepareType(type)
    this.properties = this._prepareProperties(type, properties, sections)
  }

  async execute() {
    await this.context.setState({
      validate: true
    })

    if (!this.controller.validate(true)) {
      return
    }

    await this.context.setState({
      loading: true
    })

    const strategy = this._getStrategy()
    const operations = this._createOperations()
    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)

    const oldObject = this.object
    const newObject = {
      type: strategy.getExistingObjectType(),
      id: this.type.code.value
    }

    return this.facade
      .executeOperations(operations, options)
      .then(async () => {
        this.controller.object = newObject
        await this.controller.load()
        await this.context.setState({
          loading: false
        })
        this._dispatchActions(oldObject, newObject)
      })
      .catch(error => {
        this.context.setState({
          loading: false
        })
        this.context.dispatch(actions.errorChange(error))
      })
  }

  _prepareValue(field) {
    const trim = str => {
      const trimmed = str.trim()
      return trimmed.length > 0 ? trimmed : null
    }

    if (field) {
      if (_.isString(field)) {
        return trim(field)
      } else if (_.isObject(field) && _.isString(field.value)) {
        return {
          ...field,
          value: trim(field.value)
        }
      }
    }

    return field
  }

  _prepareType(type) {
    let code = type.code.value

    if (code) {
      code = code.toUpperCase()
    }

    const newType = _.mapValues(
      {
        ...type,
        code: {
          value: code
        }
      },
      this._prepareValue
    )

    return newType
  }

  _prepareProperties(type, properties, sections) {
    const propertiesMap = properties.reduce((map, property) => {
      map[property.id] = property
      return map
    }, {})

    const results = []
    sections.forEach(section => {
      section.properties.forEach(propertyId => {
        const property = propertiesMap[propertyId]

        let code = property.code.value
        if (code) {
          code = code.toUpperCase()
        }

        const newProperty = _.mapValues(
          {
            ...property,
            code: {
              value: code
            },
            section: section.name.value
          },
          this._prepareValue
        )

        results.push(newProperty)
      })
    })

    return results
  }

  _addTypePrefix(property) {
    return {
      ...property,
      code: {
        ...property.code,
        value: TypeFormUtil.addTypePrefix(
          this.type.code.value,
          property.code.value
        )
      }
    }
  }

  _hasPropertyChanged(property, path) {
    const originalValue = property.original
      ? _.get(property.original, path)
      : null
    const currentValue = _.get(property, path)
    return originalValue.value !== currentValue.value
  }

  _isPropertyTypeUpdateNeeded(property) {
    return (
      this._hasPropertyChanged(property, 'dataType') ||
      this._hasPropertyChanged(property, 'vocabulary') ||
      this._hasPropertyChanged(property, 'materialType') ||
      this._hasPropertyChanged(property, 'plugin') ||
      this._hasPropertyChanged(property, 'label') ||
      this._hasPropertyChanged(property, 'description') ||
      this._hasPropertyChanged(property, 'schema') ||
      this._hasPropertyChanged(property, 'transformation')
    )
  }

  _isPropertyTypeUpdatePossible(property) {
    if (
      this._hasPropertyChanged(property, 'dataType') ||
      this._hasPropertyChanged(property, 'vocabulary') ||
      this._hasPropertyChanged(property, 'materialType') ||
      this._hasPropertyChanged(property, 'plugin')
    ) {
      return false
    }
    return true
  }

  _createOperations() {
    const operations = []
    const assignments = []

    if (this.type.original) {
      this.type.original.properties.forEach(originalProperty => {
        const property = _.find(this.properties, ['id', originalProperty.id])
        if (!property) {
          operations.push(
            this._deletePropertyAssignmentOperation(originalProperty)
          )
          if (originalProperty.assignments === 1) {
            operations.push(this._deletePropertyTypeOperation(originalProperty))
          }
        }
      })
    }

    this.properties.forEach((property, index) => {
      if (property.original) {
        if (this._isPropertyTypeUpdateNeeded(property)) {
          if (this._isPropertyTypeUpdatePossible(property)) {
            operations.push(this._updatePropertyTypeOperation(property))
          } else {
            operations.push(this._deletePropertyAssignmentOperation(property))
            operations.push(this._deletePropertyTypeOperation(property))
            operations.push(this._createPropertyTypeOperation(property))
          }
        }
        assignments.push(this._propertyAssignmentCreation(property, index))
      } else {
        if (property.scope.value === 'local') {
          const propertyWithPrefix = this._addTypePrefix(property)
          operations.push(this._createPropertyTypeOperation(propertyWithPrefix))
          assignments.push(
            this._propertyAssignmentCreation(propertyWithPrefix, index)
          )
        } else if (property.scope.value === 'global') {
          if (property.scope.globalPropertyType) {
            assignments.push(this._propertyAssignmentCreation(property, index))
          } else {
            operations.push(this._createPropertyTypeOperation(property))
            assignments.push(this._propertyAssignmentCreation(property, index))
          }
        }
      }
    })

    if (this.type.original) {
      operations.push(this._updateTypeOperation(assignments))
    } else {
      operations.push(this._createTypeOperation(assignments))
    }

    return operations
  }

  _deletePropertyAssignmentOperation(property) {
    const strategy = this._getStrategy()
    const assignmentId = new openbis.PropertyAssignmentPermId(
      new openbis.EntityTypePermId(
        this.type.code.value,
        strategy.getEntityKind()
      ),
      new openbis.PropertyTypePermId(property.code.value)
    )

    const update = strategy.createTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(
        this.type.code.value,
        strategy.getEntityKind()
      )
    )
    update.getPropertyAssignments().remove([assignmentId])
    update
      .getPropertyAssignments()
      .setForceRemovingAssignments(property.usagesLocal > 0)

    return strategy.createTypeUpdateOperation([update])
  }

  _createPropertyTypeOperation(property) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(property.code.value)
    creation.setLabel(property.label.value)
    creation.setDescription(property.description.value)
    creation.setDataType(property.dataType.value)
    creation.setSchema(property.schema.value)
    creation.setTransformation(property.transformation.value)

    if (
      property.dataType.value === openbis.DataType.CONTROLLEDVOCABULARY &&
      property.vocabulary.value
    ) {
      creation.setVocabularyId(
        new openbis.VocabularyPermId(property.vocabulary.value)
      )
    }
    if (
      property.dataType.value === openbis.DataType.MATERIAL &&
      property.materialType.value
    ) {
      creation.setMaterialTypeId(
        new openbis.EntityTypePermId(
          property.materialType.value,
          openbis.EntityKind.MATERIAL
        )
      )
    }
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  _updatePropertyTypeOperation(property) {
    const update = new openbis.PropertyTypeUpdate()
    if (property.code.value) {
      update.setTypeId(new openbis.PropertyTypePermId(property.code.value))
    }
    update.setLabel(property.label.value)
    update.setDescription(property.description.value)
    update.setSchema(property.schema.value)
    update.setTransformation(property.transformation.value)
    return new openbis.UpdatePropertyTypesOperation([update])
  }

  _deletePropertyTypeOperation(property) {
    const id = new openbis.PropertyTypePermId(property.code.value)
    const options = new openbis.PropertyTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePropertyTypesOperation([id], options)
  }

  _propertyAssignmentCreation(property, index) {
    let creation = new openbis.PropertyAssignmentCreation()
    creation.setOrdinal(index + 1)
    creation.setMandatory(property.mandatory.value)
    creation.setInitialValueForExistingEntities(
      property.initialValueForExistingEntities.value
    )
    creation.setShowInEditView(property.showInEditView.value)
    creation.setShowRawValueInForms(property.showRawValueInForms.value)
    creation.setSection(property.section)

    if (property.code.value) {
      creation.setPropertyTypeId(
        new openbis.PropertyTypePermId(property.code.value)
      )
    }

    if (property.plugin.value) {
      creation.setPluginId(new openbis.PluginPermId(property.plugin.value))
    }

    return creation
  }

  _createTypeOperation(assignments) {
    const strategy = this._getStrategy()
    const creation = strategy.createTypeCreation()
    creation.setCode(this.type.code.value)
    creation.setDescription(this.type.description.value)
    creation.setValidationPluginId(
      this.type.validationPlugin.value
        ? new openbis.PluginPermId(this.type.validationPlugin.value)
        : null
    )
    creation.setPropertyAssignments(assignments.reverse())
    strategy.setTypeAttributes(creation, this.type)
    return strategy.createTypeCreateOperation([creation])
  }

  _updateTypeOperation(assignments) {
    const strategy = this._getStrategy()
    const update = strategy.createTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(
        this.type.code.value,
        strategy.getEntityKind()
      )
    )
    update.setDescription(this.type.description.value)
    update.setValidationPluginId(
      this.type.validationPlugin.value
        ? new openbis.PluginPermId(this.type.validationPlugin.value)
        : null
    )
    update.getPropertyAssignments().set(assignments.reverse())
    strategy.setTypeAttributes(update, this.type)
    return strategy.createTypeUpdateOperation([update])
  }

  _dispatchActions(oldObject, newObject) {
    const strategy = this._getStrategy()
    if (oldObject.type === strategy.getNewObjectType()) {
      this.context.dispatch(
        actions.objectCreate(
          pages.TYPES,
          oldObject.type,
          oldObject.id,
          newObject.type,
          newObject.id
        )
      )
    } else if (oldObject.type === strategy.getExistingObjectType()) {
      this.context.dispatch(
        actions.objectUpdate(pages.TYPES, oldObject.type, oldObject.id)
      )
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
  getEntityKind() {
    return openbis.EntityKind.SAMPLE
  }

  getNewObjectType() {
    return objectTypes.NEW_OBJECT_TYPE
  }

  getExistingObjectType() {
    return objectTypes.OBJECT_TYPE
  }

  createTypeCreation() {
    return new openbis.SampleTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateSampleTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.SampleTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateSampleTypesOperation(updates)
  }

  setTypeAttributes(object, type) {
    object.setListable(type.listable.value)
    object.setShowContainer(type.showContainer.value)
    object.setShowParents(type.showParents.value)
    object.setShowParentMetadata(type.showParentMetadata.value)
    object.setAutoGeneratedCode(type.autoGeneratedCode.value)
    object.setGeneratedCodePrefix(type.generatedCodePrefix.value)
    object.setSubcodeUnique(type.subcodeUnique.value)
  }
}

class CollectionTypeStrategy {
  getEntityKind() {
    return openbis.EntityKind.EXPERIMENT
  }

  getNewObjectType() {
    return objectTypes.NEW_COLLECTION_TYPE
  }

  getExistingObjectType() {
    return objectTypes.COLLECTION_TYPE
  }

  createTypeCreation() {
    return new openbis.ExperimentTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateExperimentTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.ExperimentTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateExperimentTypesOperation(updates)
  }

  setTypeAttributes() {}
}

class DataSetTypeStrategy {
  getEntityKind() {
    return openbis.EntityKind.DATA_SET
  }

  getNewObjectType() {
    return objectTypes.NEW_DATA_SET_TYPE
  }

  getExistingObjectType() {
    return objectTypes.DATA_SET_TYPE
  }

  createTypeCreation() {
    return new openbis.DataSetTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateDataSetTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.DataSetTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateDataSetTypesOperation(updates)
  }

  setTypeAttributes(object, type) {
    object.setMainDataSetPattern(type.mainDataSetPattern.value)
    object.setMainDataSetPath(type.mainDataSetPath.value)
    object.setDisallowDeletion(type.disallowDeletion.value)
  }
}

class MaterialTypeStrategy {
  getEntityKind() {
    return openbis.EntityKind.MATERIAL
  }

  getNewObjectType() {
    return objectTypes.NEW_MATERIAL_TYPE
  }

  getExistingObjectType() {
    return objectTypes.MATERIAL_TYPE
  }

  createTypeCreation() {
    return new openbis.MaterialTypeCreation()
  }

  createTypeCreateOperation(creations) {
    return new openbis.CreateMaterialTypesOperation(creations)
  }

  createTypeUpdate() {
    return new openbis.MaterialTypeUpdate()
  }

  createTypeUpdateOperation(updates) {
    return new openbis.UpdateMaterialTypesOperation(updates)
  }

  setTypeAttributes() {}
}
