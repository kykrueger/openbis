import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import TypeFormUtil from '@src/js/components/types/form/TypeFormUtil.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class TypeFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === 'type') {
      await this._handleChangeType(params)
    } else if (type === 'section') {
      await this._handleChangeSection(params)
    } else if (type === 'property') {
      await this._handleChangeProperty(params)
    } else if (type === 'preview') {
      await this._handleChangePreview(params)
    }
  }

  async _handleChangeType(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.type,
        params.field,
        params.value
      )
      return {
        type: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeSection(params) {
    await this.context.setState(state => {
      const { newCollection } = FormUtil.changeCollectionItemField(
        state.sections,
        params.id,
        params.field,
        params.value
      )
      return {
        sections: newCollection
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeProperty(params) {
    await this.context.setState(state => {
      const {
        newCollection,
        oldObject,
        newObject
      } = FormUtil.changeCollectionItemField(
        state.properties,
        params.id,
        params.field,
        params.value
      )

      this._handleChangePropertyScope(oldObject, newObject)
      this._handleChangePropertyDataType(oldObject, newObject)
      this._handleChangePropertyMandatory(oldObject, newObject)

      return {
        properties: newCollection
      }
    })
    await this.controller.changed(true)
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
        _.assign(newProperty, {
          code: {
            ...newProperty.code,
            value: null
          },
          internalNameSpace: {
            ...newProperty.internalNameSpace,
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
        })
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
          _.assign(newProperty, {
            internalNameSpace: {
              ...newProperty.internalNameSpace,
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
          })
        } else if (newExisting) {
          newExisting = {
            internalNameSpace: {
              value: _.get(newExisting, 'internalNameSpace', null)
            },
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

          _.assign(newProperty, {
            internalNameSpace: {
              ...newProperty.internalNameSpace,
              value: newExisting.internalNameSpace.value
            },
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
          })

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

      _.assign(newProperty, {
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
      })
    }
  }

  _handleChangePropertyDataType(oldProperty, newProperty) {
    const oldDataType = oldProperty.dataType.value
    const newDataType = newProperty.dataType.value

    if (oldDataType !== newDataType) {
      _.assign(newProperty, {
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
      })
    }
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

      _.assign(newProperty, {
        initialValueForExistingEntities: {
          ...newProperty.initialValueForExistingEntities,
          visible:
            typeIsUsed &&
            propertyIsMandatory &&
            (propertyIsNew || !propertyWasMandatory)
        }
      })
    }
  }

  async _handleChangePreview(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.preview,
        params.field,
        params.value
      )
      return {
        preview: newObject
      }
    })
    await this.controller.changed(true)
  }
}
