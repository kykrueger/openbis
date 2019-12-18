import _ from 'lodash'
import { dto } from '../../../services/openbis.js'

export default class ObjectTypeHandlerSave {
  constructor(state, setState, facade, loadHandler, validateHandler) {
    this.type = this.prepareType(state.type)
    this.properties = this.prepareProperties(
      state.type,
      state.properties,
      state.sections
    )
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
    const propertyCodePrefix = type.code + '.'

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

          if (!code.startsWith(propertyCodePrefix)) {
            code = propertyCodePrefix + code
          }
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

  preparePropertyChanges(loadedPropertyTypes) {
    const toDelete = {}
    const toCreate = {}
    const toUpdate = {}

    function isUpdatePossible(type, property) {
      const typeVocabulary = type.vocabulary ? type.vocabulary.code : null
      const typeMaterialType = type.materialType ? type.materialType.code : null
      const originalPlugin = property.original ? property.original.plugin : null
      return (
        type.dataType === property.dataType &&
        typeVocabulary === property.vocabulary &&
        typeMaterialType === property.materialType &&
        originalPlugin === property.plugin
      )
    }

    const propertiesMap = this.properties.reduce((map, property) => {
      map[property.code] = property
      return map
    }, {})

    const loadedPropertyTypesMap = loadedPropertyTypes.reduce(
      (map, loadedProperty) => {
        map[loadedProperty.code] = loadedProperty
        return map
      },
      {}
    )

    this.properties.forEach(property => {
      const loadedProperty = loadedPropertyTypesMap[property.code]
      if (!loadedProperty) {
        toCreate[property.code] = property
      }
    })

    loadedPropertyTypes.forEach(loadedPropertyType => {
      const property = propertiesMap[loadedPropertyType.code]

      if (property) {
        if (isUpdatePossible(loadedPropertyType, property)) {
          toUpdate[property.code] = property
        } else {
          toDelete[property.code] = property.code
          toCreate[property.code] = property
        }
      } else {
        toDelete[loadedPropertyType.code] = loadedPropertyType.code
      }
    })

    return { toCreate, toUpdate, toDelete }
  }

  deletePropertyAssignmentsOperations(toDelete) {
    const assignmentIds = []

    Object.values(toDelete).forEach(code => {
      assignmentIds.push(
        new dto.PropertyAssignmentPermId(
          new dto.EntityTypePermId(this.type.code, dto.EntityKind.SAMPLE),
          new dto.PropertyTypePermId(code)
        )
      )
    })

    const update = new dto.SampleTypeUpdate()
    update.setTypeId(
      new dto.EntityTypePermId(this.type.code, dto.EntityKind.SAMPLE)
    )
    update.getPropertyAssignments().remove(assignmentIds)
    update.getPropertyAssignments().setForceRemovingAssignments(true)

    return [new dto.UpdateSampleTypesOperation([update])]
  }

  createUpdateDeletePropertyTypesOperations(toCreate, toUpdate, toDelete) {
    const operations = []

    Object.values(toDelete).forEach(code => {
      const ids = [new dto.PropertyTypePermId(code)]
      const options = new dto.PropertyTypeDeletionOptions()
      options.setReason('deleted via ng_ui')
      operations.push(new dto.DeletePropertyTypesOperation(ids, options))
    })

    Object.values(toCreate).forEach(property => {
      const creation = new dto.PropertyTypeCreation()
      creation.setCode(property.code)
      creation.setLabel(property.label)
      creation.setDescription(property.description)
      creation.setDataType(property.dataType)
      creation.setSchema(property.schema)
      creation.setTransformation(property.transformation)

      if (
        property.dataType === dto.DataType.CONTROLLEDVOCABULARY &&
        property.vocabulary
      ) {
        creation.setVocabularyId(new dto.VocabularyPermId(property.vocabulary))
      }
      if (
        property.dataType === dto.DataType.MATERIAL &&
        property.materialType
      ) {
        creation.setMaterialTypeId(
          new dto.EntityTypePermId(
            property.materialType,
            dto.EntityKind.MATERIAL
          )
        )
      }
      operations.push(new dto.CreatePropertyTypesOperation([creation]))
    })

    Object.values(toUpdate).forEach(property => {
      const update = new dto.PropertyTypeUpdate()
      if (property.code) {
        update.setTypeId(new dto.PropertyTypePermId(property.code))
      }
      update.setLabel(property.label)
      update.setDescription(property.description)
      update.setSchema(property.schema)
      update.setTransformation(property.transformation)
      operations.push(new dto.UpdatePropertyTypesOperation([update]))
    })

    return operations
  }

  updateTypeAndAssignmentsOperations() {
    const updateProperties = this.properties.map((property, index) => {
      let updateProperty = new dto.PropertyAssignmentCreation()
      updateProperty.setOrdinal(index + 1)
      updateProperty.setMandatory(property.mandatory)
      updateProperty.setInitialValueForExistingEntities(
        property.initialValueForExistingEntities
      )
      updateProperty.setShowInEditView(property.showInEditView)
      updateProperty.setShowRawValueInForms(property.showRawValueInForms)
      updateProperty.setSection(property.section)

      if (property.code) {
        updateProperty.setPropertyTypeId(
          new dto.PropertyTypePermId(property.code)
        )
      }

      if (property.plugin) {
        updateProperty.setPluginId(new dto.PluginPermId(property.plugin))
      }

      return updateProperty
    })

    const update = new dto.SampleTypeUpdate()
    update.setTypeId(
      new dto.EntityTypePermId(this.type.code, dto.EntityKind.SAMPLE)
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
        ? new dto.PluginPermId(this.type.validationPlugin)
        : null
    )
    update.getPropertyAssignments().set(updateProperties)

    return [new dto.UpdateSampleTypesOperation([update])]
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
        return this.doExecute()
          .then(() => {
            return this.validateHandler.setEnabled(false)
          })
          .then(() => {
            return this.loadHandler.execute()
          })
      })
      .catch(error => {
        this.facade.catch(error)
      })
      .finally(() => {
        this.setState({
          loading: false
        })
      })
  }

  doExecute() {
    return this.facade
      .loadPropertyTypes(this.type.code)
      .then(loadedPropertyTypes => {
        const { toCreate, toUpdate, toDelete } = this.preparePropertyChanges(
          loadedPropertyTypes
        )
        const deletePropertyAssignmentsOperations = this.deletePropertyAssignmentsOperations(
          toDelete
        )
        const createUpdateDeletePropertyTypesOperations = this.createUpdateDeletePropertyTypesOperations(
          toCreate,
          toUpdate,
          toDelete
        )
        const updateTypeAndAssignmentsOperations = this.updateTypeAndAssignmentsOperations()

        const options = new dto.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)

        return this.facade.executeOperations(
          [
            ...deletePropertyAssignmentsOperations,
            ...createUpdateDeletePropertyTypesOperations,
            ...updateTypeAndAssignmentsOperations
          ],
          options
        )
      })
  }
}
