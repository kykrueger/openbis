import ObjectTypeFacade from './ObjectTypeFacade.js'
import { dto, facade } from '../../../services/openbis.js'

export default class ObjectTypeHandlerSave {
  constructor(state) {
    this.facade = new ObjectTypeFacade()
    this.type = this.prepareType(state.type)
    this.properties = this.prepareProperties(
      state.type,
      state.properties,
      state.sections
    )
  }

  prepareType(type) {
    let code = type.code

    if (code) {
      code = code.toUpperCase()
    }

    return {
      ...type,
      code
    }
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

        results.push({
          ...property,
          code,
          section: section.name
        })
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
      return (
        type.dataType === property.dataType &&
        typeVocabulary === property.vocabulary &&
        typeMaterialType === property.materialType
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

      if (property.pluginId) {
        updateProperties.setPluginId(new dto.PluginPermId(property.pluginId))
      }

      return updateProperty
    })

    const update = new dto.SampleTypeUpdate()
    update.setTypeId(
      new dto.EntityTypePermId(this.type.code, dto.EntityKind.SAMPLE)
    )
    update.setDescription(this.type.description)
    update.setAutoGeneratedCode(this.type.autoGeneratedCode)
    update.setGeneratedCodePrefix(this.type.generatedCodePrefix)
    update.setSubcodeUnique(this.type.subcodeUnique)
    update.getPropertyAssignments().set(updateProperties)

    return [new dto.UpdateSampleTypesOperation([update])]
  }

  execute() {
    return this.facade
      .loadTypePropertyTypes(this.type.code)
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

        return facade.executeOperations(
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
