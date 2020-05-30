import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'

import TypeFormUtil from './TypeFormUtil.js'

export default class TypeFormControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(type, params) {
    if (type === 'type') {
      const { field, value } = params
      this._handleChangeType(field, value)
    } else if (type === 'section') {
      const { id, field, value } = params
      this._handleChangeSection(id, field, value)
    } else if (type === 'property') {
      const { id, field, value } = params
      this._handleChangeProperty(id, field, value)
    }
  }

  _handleChangeType(field, value) {
    this.context.setState(state => ({
      ...state,
      type: {
        ...state.type,
        [field]: {
          ...state.type[field],
          value
        }
      }
    }))
  }

  _handleChangeSection(id, field, value) {
    let { sections } = this.context.getState()
    let newSections = Array.from(sections)

    let index = sections.findIndex(section => section.id === id)
    let section = sections[index]
    let newSection = {
      ...section,
      [field]: {
        ...section[field],
        value
      }
    }
    newSections[index] = newSection

    this.context.setState(state => ({
      ...state,
      sections: newSections
    }))
  }

  _handleChangeProperty(id, field, newValue) {
    const { properties } = this.context.getState()
    const newProperties = Array.from(properties)

    const index = properties.findIndex(property => property.id === id)
    const oldProperty = properties[index]

    let newProperty = {
      ...oldProperty,
      [field]: {
        ...oldProperty[field],
        value: newValue
      }
    }

    newProperty = this._handleChangePropertyScope(oldProperty, newProperty)
    newProperty = this._handleChangePropertyDataType(oldProperty, newProperty)
    newProperty = this._handleChangePropertyMandatory(oldProperty, newProperty)

    newProperties[index] = newProperty

    this.context.setState(state => ({
      ...state,
      properties: newProperties
    }))
  }

  _handleChangePropertyScope(oldProperty, newProperty) {
    const { type, assignments, usages } = this.context.getState()

    const oldScope = oldProperty.scope.value
    const newScope = newProperty.scope.value

    const oldCode = oldProperty.code.value
    const newCode = newProperty.code.value

    if (oldScope !== newScope || oldCode !== newCode) {
      let globalPropertyType = null

      if (newScope === 'global') {
        const { globalPropertyTypes } = this.controller.getDictionaries()

        globalPropertyType = globalPropertyTypes.find(
          propertyType => propertyType.code === newCode
        )

        if (globalPropertyType) {
          globalPropertyType = {
            label: {
              value: _.get(globalPropertyType, 'label', null)
            },
            description: {
              value: _.get(globalPropertyType, 'description', null)
            },
            dataType: {
              value: _.get(globalPropertyType, 'dataType', null)
            },
            vocabulary: {
              value: _.get(globalPropertyType, 'vocabulary.code', null)
            },
            materialType: {
              value: _.get(globalPropertyType, 'materialType.code', null)
            },
            schema: {
              value: _.get(globalPropertyType, 'schema', null)
            },
            transformation: {
              value: _.get(globalPropertyType, 'transformation', null)
            }
          }

          newProperty = {
            ...newProperty,
            label: {
              ...newProperty.label,
              value: globalPropertyType.label.value
            },
            description: {
              ...newProperty.description,
              value: globalPropertyType.description.value
            },
            dataType: {
              ...newProperty.dataType,
              value: globalPropertyType.dataType.value
            },
            vocabulary: {
              ...newProperty.vocabulary,
              value: globalPropertyType.vocabulary.value
            },
            materialType: {
              ...newProperty.materialType,
              value: globalPropertyType.materialType.value
            },
            schema: {
              ...newProperty.schema,
              value: globalPropertyType.schema.value
            },
            transformation: {
              ...newProperty.transformation,
              value: globalPropertyType.transformation.value
            }
          }
        }
      }

      const propertyCode =
        newScope === 'local'
          ? TypeFormUtil.addTypePrefix(type.code.value, newProperty.code.value)
          : newProperty.code.value

      const propertyAssignments =
        (assignments && assignments[propertyCode]) || 0

      const propertyUsagesLocal =
        (usages &&
          usages.propertyLocal &&
          usages.propertyLocal[propertyCode]) ||
        0
      const propertyUsagesGlobal =
        (usages &&
          usages.propertyGlobal &&
          usages.propertyGlobal[propertyCode]) ||
        0

      const enabled = newProperty.original
        ? propertyUsagesGlobal === 0 && propertyAssignments === 1
        : propertyUsagesGlobal === 0 && propertyAssignments === 0

      newProperty = {
        ...newProperty,
        scope: {
          ...newProperty.scope,
          globalPropertyType: globalPropertyType
        },
        dataType: {
          ...newProperty.dataType,
          enabled
        },
        vocabulary: {
          ...newProperty.vocabulary,
          enabled
        },
        materialType: {
          ...newProperty.materialType,
          enabled
        },
        plugin: {
          ...newProperty.plugin,
          enabled
        },
        assignments: propertyAssignments,
        usagesLocal: propertyUsagesLocal,
        usagesGlobal: propertyUsagesGlobal
      }
    }
    return newProperty
  }

  _handleChangePropertyDataType(oldProperty, newProperty) {
    const oldDataType = oldProperty.dataType.value
    const newDataType = newProperty.dataType.value

    if (oldDataType !== newDataType) {
      newProperty = {
        ...newProperty,
        vocabulary: {
          ...newProperty.vocabulary,
          visible: newDataType === openbis.DataType.CONTROLLEDVOCABULARY
        },
        materialType: {
          ...newProperty.materialType,
          visible: newDataType === openbis.DataType.MATERIAL
        },
        schema: {
          ...newProperty.schema,
          visible: newDataType === openbis.DataType.XML
        },
        transformation: {
          ...newProperty.transformation,
          visible: newDataType === openbis.DataType.XML
        }
      }
    }
    return newProperty
  }

  _handleChangePropertyMandatory(oldProperty, newProperty) {
    const oldMandatory = oldProperty.mandatory.value
    const newMandatory = newProperty.mandatory.value

    if (oldMandatory !== newMandatory) {
      const { type } = this.context.getState()
      const typeIsUsed = type.usages > 0
      const propertyIsNew = !newProperty.original
      const propertyIsMandatory = newProperty.mandatory.value
      const propertyWasMandatory = newProperty.original
        ? newProperty.original.mandatory.value
        : false

      newProperty = {
        ...newProperty,
        initialValueForExistingEntities: {
          ...newProperty.initialValueForExistingEntities,
          visible:
            typeIsUsed &&
            propertyIsMandatory &&
            (propertyIsNew || !propertyWasMandatory)
        }
      }
    }
    return newProperty
  }
}
