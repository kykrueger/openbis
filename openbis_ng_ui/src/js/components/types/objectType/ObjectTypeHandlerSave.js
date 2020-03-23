import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'

export default class ObjectTypeHandlerSave {
  constructor(getState, setState, facade, loadHandler, validateHandler) {
    let { type, properties, sections } = getState()
    this.type = this.prepareType(type)
    this.properties = this.prepareProperties(type, properties, sections)
    this.setState = setState
    this.facade = facade
    this.loadHandler = loadHandler
    this.validateHandler = validateHandler
  }

  prepareValue(value) {
    if (value) {
      if (_.isString(value)) {
        const trimmedValue = value.trim()
        return trimmedValue.length > 0 ? trimmedValue : null
      }
    }
    return value
  }

  prepareType(type) {
    let code = type.code

    if (code) {
      code = code.toUpperCase()
    }

    const newType = _.mapValues(
      {
        ...type,
        code
      },
      this.prepareValue
    )

    return newType
  }

  prepareProperties(type, properties, sections) {
    const propertiesMap = properties.reduce((map, property) => {
      map[property.id] = property
      return map
    }, {})

    const results = []
    sections.forEach(section => {
      section.properties.forEach(propertyId => {
        const property = propertiesMap[propertyId]

        let code = property.code
        if (code) {
          code = code.toUpperCase()
        }

        const newProperty = _.mapValues(
          {
            ...property,
            code,
            section: section.name
          },
          this.prepareValue
        )

        results.push(newProperty)
      })
    })

    return results
  }

  getTypePrefix() {
    return this.type.code + '.'
  }

  hasTypePrefix(property) {
    return property.code && property.code.startsWith(this.getTypePrefix())
  }

  addTypePrefix(property) {
    if (property.code && !this.hasTypePrefix(property)) {
      return {
        ...property,
        code: this.getTypePrefix() + property.code
      }
    } else {
      return property
    }
  }

  hasPropertyChanged(property, path) {
    const originalValue = property.original
      ? _.get(property.original, path)
      : null
    const currentValue = _.get(property, path)
    return originalValue !== currentValue
  }

  isPropertyTypeUpdateNeeded(property) {
    return (
      this.hasPropertyChanged(property, 'dataType') ||
      this.hasPropertyChanged(property, 'vocabulary') ||
      this.hasPropertyChanged(property, 'materialType') ||
      this.hasPropertyChanged(property, 'plugin') ||
      this.hasPropertyChanged(property, 'label') ||
      this.hasPropertyChanged(property, 'description') ||
      this.hasPropertyChanged(property, 'schema') ||
      this.hasPropertyChanged(property, 'transformation')
    )
  }

  isPropertyTypeUpdatePossible(property) {
    if (
      this.hasPropertyChanged(property, 'dataType') ||
      this.hasPropertyChanged(property, 'vocabulary') ||
      this.hasPropertyChanged(property, 'materialType') ||
      this.hasPropertyChanged(property, 'plugin')
    ) {
      return false
    }
    return true
  }

  createOperations() {
    const operations = []
    const assignments = []

    if (this.type.original) {
      this.type.original.properties.forEach(originalProperty => {
        const property = _.find(this.properties, ['id', originalProperty.id])
        if (!property) {
          if (this.hasTypePrefix(originalProperty)) {
            operations.push(
              this.deletePropertyAssignmentOperation(originalProperty)
            )
            operations.push(this.deletePropertyTypeOperation(originalProperty))
          } else {
            operations.push(
              this.deletePropertyAssignmentOperation(originalProperty)
            )
          }
        }
      })
    }

    this.properties.forEach((property, index) => {
      if (property.original) {
        if (this.hasTypePrefix(property)) {
          if (this.isPropertyTypeUpdateNeeded(property)) {
            if (this.isPropertyTypeUpdatePossible(property)) {
              operations.push(this.updatePropertyTypeOperation(property))
            } else {
              operations.push(this.deletePropertyAssignmentOperation(property))
              operations.push(this.deletePropertyTypeOperation(property))
              operations.push(this.createPropertyTypeOperation(property))
            }
          }
          assignments.push(this.propertyAssignmentCreation(property, index))
        } else {
          if (this.isPropertyTypeUpdateNeeded(property)) {
            const propertyWithPrefix = this.addTypePrefix(property)
            operations.push(this.deletePropertyAssignmentOperation(property))
            operations.push(
              this.createPropertyTypeOperation(propertyWithPrefix)
            )
            assignments.push(
              this.propertyAssignmentCreation(propertyWithPrefix, index)
            )
          } else {
            assignments.push(this.propertyAssignmentCreation(property, index))
          }
        }
      } else {
        const propertyWithPrefix = this.addTypePrefix(property)
        operations.push(this.createPropertyTypeOperation(propertyWithPrefix))
        assignments.push(
          this.propertyAssignmentCreation(propertyWithPrefix, index)
        )
      }
    })

    operations.push(this.updateTypeOperation(assignments))

    return operations
  }

  deletePropertyAssignmentOperation(property) {
    const assignmentId = new openbis.PropertyAssignmentPermId(
      new openbis.EntityTypePermId(this.type.code, openbis.EntityKind.SAMPLE),
      new openbis.PropertyTypePermId(property.code)
    )

    const update = new openbis.SampleTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(this.type.code, openbis.EntityKind.SAMPLE)
    )
    update.getPropertyAssignments().remove([assignmentId])
    update
      .getPropertyAssignments()
      .setForceRemovingAssignments(property.usages > 0)

    return new openbis.UpdateSampleTypesOperation([update])
  }

  createPropertyTypeOperation(property) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(property.code)
    creation.setLabel(property.label)
    creation.setDescription(property.description)
    creation.setDataType(property.dataType)
    creation.setSchema(property.schema)
    creation.setTransformation(property.transformation)

    if (
      property.dataType === openbis.DataType.CONTROLLEDVOCABULARY &&
      property.vocabulary
    ) {
      creation.setVocabularyId(
        new openbis.VocabularyPermId(property.vocabulary)
      )
    }
    if (
      property.dataType === openbis.DataType.MATERIAL &&
      property.materialType
    ) {
      creation.setMaterialTypeId(
        new openbis.EntityTypePermId(
          property.materialType,
          openbis.EntityKind.MATERIAL
        )
      )
    }
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  updatePropertyTypeOperation(property) {
    const update = new openbis.PropertyTypeUpdate()
    if (property.code) {
      update.setTypeId(new openbis.PropertyTypePermId(property.code))
    }
    update.setLabel(property.label)
    update.setDescription(property.description)
    update.setSchema(property.schema)
    update.setTransformation(property.transformation)
    return new openbis.UpdatePropertyTypesOperation([update])
  }

  deletePropertyTypeOperation(property) {
    const id = new openbis.PropertyTypePermId(property.code)
    const options = new openbis.PropertyTypeDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeletePropertyTypesOperation([id], options)
  }

  propertyAssignmentCreation(property, index) {
    let creation = new openbis.PropertyAssignmentCreation()
    creation.setOrdinal(index + 1)
    creation.setMandatory(property.mandatory)
    creation.setInitialValueForExistingEntities(
      property.initialValueForExistingEntities
    )
    creation.setShowInEditView(property.showInEditView)
    creation.setShowRawValueInForms(property.showRawValueInForms)
    creation.setSection(property.section)

    if (property.code) {
      creation.setPropertyTypeId(new openbis.PropertyTypePermId(property.code))
    }

    if (property.plugin) {
      creation.setPluginId(new openbis.PluginPermId(property.plugin))
    }

    return creation
  }

  updateTypeOperation(assignments) {
    const update = new openbis.SampleTypeUpdate()
    update.setTypeId(
      new openbis.EntityTypePermId(this.type.code, openbis.EntityKind.SAMPLE)
    )
    update.setDescription(this.type.description)
    update.setListable(this.type.listable)
    update.setShowContainer(this.type.showContainer)
    update.setShowParents(this.type.showParents)
    update.setShowParentMetadata(this.type.showParentMetadata)
    update.setAutoGeneratedCode(this.type.autoGeneratedCode)
    update.setGeneratedCodePrefix(this.type.generatedCodePrefix)
    update.setSubcodeUnique(this.type.subcodeUnique)
    update.setValidationPluginId(
      this.type.validationPlugin
        ? new openbis.PluginPermId(this.type.validationPlugin)
        : null
    )
    update.getPropertyAssignments().set(assignments.reverse())

    return new openbis.UpdateSampleTypesOperation([update])
  }

  execute() {
    return this.validateHandler
      .setEnabled(true)
      .then(() => {
        if (!this.validateHandler.execute(true)) {
          return
        }
        this.setState({
          loading: true
        })

        const operations = this.createOperations()
        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)

        return this.facade
          .executeOperations(operations, options)
          .then(() => {
            return this.validateHandler.setEnabled(false)
          })
          .then(() => {
            return this.loadHandler.execute()
          })
      })
      .catch(error => {
        this.openbis.catch(error)
      })
      .finally(() => {
        this.setState({
          loading: false
        })
      })
  }
}
