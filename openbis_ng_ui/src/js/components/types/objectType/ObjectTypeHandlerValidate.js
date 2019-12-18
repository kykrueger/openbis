import _ from 'lodash'
import { dto } from '../../../services/openbis.js'

export default class ObjectTypeHandlerValidate {
  constructor(getState, setState) {
    this.getState = getState
    this.setState = setState
  }

  setEnabled(enabled) {
    return new Promise(resolve => {
      this.setState(
        {
          validate: enabled
        },
        () => {
          resolve()
        }
      )
    })
  }

  execute(autofocus) {
    const { validate, type, properties } = this.getState()

    if (!validate) {
      return
    }

    const [typeErrors, typeErrorsMap] = this.validateType(type)
    const [propertiesErrors, propertiesErrorsMap] = this.validateProperties(
      type,
      properties
    )

    let errorSelection = null

    if (autofocus) {
      if (!_.isEmpty(typeErrors)) {
        errorSelection = {
          type: 'type',
          params: {
            part: typeErrors[0].field
          }
        }
      } else if (!_.isEmpty(propertiesErrors)) {
        errorSelection = {
          type: 'property',
          params: {
            id: propertiesErrors[0].property,
            part: propertiesErrors[0].errors[0].field
          }
        }
      }
    }

    this.setState(state => {
      const newType = {
        ...state.type,
        errors: typeErrorsMap
      }
      const newProperties = state.properties.map(property => ({
        ...property,
        errors: propertiesErrorsMap[property.id] || {}
      }))

      return {
        type: newType,
        properties: newProperties,
        selection: errorSelection ? errorSelection : state.selection
      }
    })

    return _.isEmpty(typeErrors) && _.isEmpty(propertiesErrors)
  }

  validateType(type) {
    const errors = []

    this.validateNotEmpty('Code', 'code', type.code, errors)
    this.validateNotEmpty(
      'Generated code prefix',
      'generatedCodePrefix',
      type.generatedCodePrefix,
      errors
    )

    const errorsMap = errors.reduce((map, error) => {
      map[error.field] = error.message
      return map
    }, {})

    return [errors, errorsMap]
  }

  validateProperties(type, properties) {
    const errors = []
    const errorsMap = {}

    properties.forEach(property => {
      const propertyErrors = this.validateProperty(type, property)

      if (!_.isEmpty(propertyErrors)) {
        errors.push({
          property: property.id,
          errors: propertyErrors
        })

        errorsMap[property.id] = propertyErrors.reduce((map, error) => {
          map[error.field] = error.message
          return map
        }, {})
      }
    })

    return [errors, errorsMap]
  }

  validateProperty(type, property) {
    const errors = []

    this.validateNotEmpty('Code', 'code', property.code, errors)
    this.validateNotEmpty('Label', 'label', property.label, errors)
    this.validateNotEmpty(
      'Description',
      'description',
      property.description,
      errors
    )
    this.validateNotEmpty('Data Type', 'dataType', property.dataType, errors)

    if (property.dataType === dto.DataType.CONTROLLEDVOCABULARY) {
      this.validateNotEmpty(
        'Vocabulary',
        'vocabulary',
        property.vocabulary,
        errors
      )
    } else if (property.dataType === dto.DataType.MATERIAL) {
      this.validateNotEmpty(
        'Material Type',
        'materialType',
        property.materialType,
        errors
      )
    }

    const wasMandatory = property.original ? property.original.mandatory : false
    const isMandatory = property.mandatory

    if (property.usages > 0 && !wasMandatory && isMandatory) {
      this.validateNotEmpty(
        'Initial Value',
        'initialValueForExistingEntities',
        property.initialValueForExistingEntities,
        errors
      )
    }

    return errors
  }

  validateNotEmpty(label, name, value, errors) {
    if (value === null || value === undefined || value.trim() === '') {
      errors.push({
        field: name,
        message: label + ' cannot be empty'
      })
    }
  }
}
