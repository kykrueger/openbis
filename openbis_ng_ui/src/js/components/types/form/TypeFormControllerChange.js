import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import TypeFormUtil from './TypeFormUtil.js'

export default class TypeFormControllerChange extends PageControllerChange {
  execute(type, params) {
    if (type === 'type') {
      const { field, value } = params
      this.changeObjectField('type', field, value)
    } else if (type === 'section') {
      const { id, field, value } = params
      this.changeCollectionItemField('sections', id, field, value)
    } else if (type === 'property') {
      const { id, field, value } = params
      this.changeCollectionItemField(
        'properties',
        id,
        field,
        value,
        (oldProperty, newProperty) => {
          newProperty = this._handleChangePropertyScope(
            oldProperty,
            newProperty
          )
          newProperty = this._handleChangePropertyDataType(
            oldProperty,
            newProperty
          )
          newProperty = this._handleChangePropertyMandatory(
            oldProperty,
            newProperty
          )
          return newProperty
        }
      )
    } else if (type === 'preview') {
      const { field, value } = params
      this.changeObjectField('preview', field, value)
    }
  }

  _handleChangePropertyScope(oldProperty, newProperty) {
    const { type, assignments, usages } = this.context.getState()

    const oldScope = oldProperty.scope.value
    const newScope = newProperty.scope.value

    const oldCode = oldProperty.code.value
    const newCode = newProperty.code.value

    if (oldScope !== newScope || oldCode !== newCode) {
      let globalPropertyType = null

      if (oldScope !== newScope) {
        newProperty = {
          ...newProperty,
          code: {
            ...newProperty.code,
            value: null
          },
          label: {
            ...newProperty.label,
            value: null
          },
          description: {
            ...newProperty.description,
            value: null
          },
          dataType: {
            ...newProperty.dataType,
            value: null
          },
          plugin: {
            ...newProperty.plugin,
            value: null
          },
          vocabulary: {
            ...newProperty.vocabulary,
            value: null
          },
          materialType: {
            ...newProperty.materialType,
            value: null
          },
          sampleType: {
            ...newProperty.sampleType,
            value: null
          },
          schema: {
            ...newProperty.schema,
            value: null
          },
          transformation: {
            ...newProperty.transformation,
            value: null
          }
        }
      }

      if (oldCode !== newCode && newScope === 'global') {
        const { globalPropertyTypes } = this.controller.getDictionaries()

        let oldExisting = globalPropertyTypes.find(
          propertyType => propertyType.code === oldCode
        )
        let newExisting = globalPropertyTypes.find(
          propertyType => propertyType.code === newCode
        )

        if (oldExisting && !newExisting) {
          newProperty = {
            ...newProperty,
            label: {
              ...newProperty.label,
              value: null
            },
            description: {
              ...newProperty.description,
              value: null
            },
            dataType: {
              ...newProperty.dataType,
              value: null
            },
            plugin: {
              ...newProperty.plugin,
              value: null
            },
            vocabulary: {
              ...newProperty.vocabulary,
              value: null
            },
            materialType: {
              ...newProperty.materialType,
              value: null
            },
            sampleType: {
              ...newProperty.sampleType,
              value: null
            },
            schema: {
              ...newProperty.schema,
              value: null
            },
            transformation: {
              ...newProperty.transformation,
              value: null
            }
          }
        } else if (newExisting) {
          newExisting = {
            label: {
              value: _.get(newExisting, 'label', null)
            },
            description: {
              value: _.get(newExisting, 'description', null)
            },
            dataType: {
              value: _.get(newExisting, 'dataType', null)
            },
            vocabulary: {
              value: _.get(newExisting, 'vocabulary.code', null)
            },
            materialType: {
              value: _.get(newExisting, 'materialType.code', null)
            },
            sampleType: {
              value: _.get(newExisting, 'sampleType.code', null)
            },
            schema: {
              value: _.get(newExisting, 'schema', null)
            },
            transformation: {
              value: _.get(newExisting, 'transformation', null)
            }
          }

          newProperty = {
            ...newProperty,
            label: {
              ...newProperty.label,
              value: newExisting.label.value
            },
            description: {
              ...newProperty.description,
              value: newExisting.description.value
            },
            dataType: {
              ...newProperty.dataType,
              value: newExisting.dataType.value
            },
            vocabulary: {
              ...newProperty.vocabulary,
              value: newExisting.vocabulary.value
            },
            materialType: {
              ...newProperty.materialType,
              value: newExisting.materialType.value
            },
            sampleType: {
              ...newProperty.sampleType,
              value: newExisting.sampleType.value
            },
            schema: {
              ...newProperty.schema,
              value: newExisting.schema.value
            },
            transformation: {
              ...newProperty.transformation,
              value: newExisting.transformation.value
            }
          }

          globalPropertyType = newExisting
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
        ? propertyUsagesGlobal === 0 && propertyAssignments <= 1
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
        sampleType: {
          ...newProperty.sampleType,
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
        sampleType: {
          ...newProperty.sampleType,
          visible: newDataType === openbis.DataType.SAMPLE
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
