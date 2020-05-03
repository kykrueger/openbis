import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import openbis from '@src/js/services/openbis.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.object = controller.object
  }

  execute(autofocus) {
    const { validate, type, properties } = this.context.getState()

    if (!validate) {
      return true
    }

    const [typeErrors, typeErrorsMap] = this._validateType(type)
    const [propertiesErrors, propertiesErrorsMap] = this._validateProperties(
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

    this.context.setState(state => {
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
        properties: newProperties
      }
    })

    if (errorSelection) {
      this.context.setState({
        selection: errorSelection
      })
    }

    return _.isEmpty(typeErrors) && _.isEmpty(propertiesErrors)
  }

  _validateType(type) {
    const strategy = this._getStrategy()
    const errors = []

    FormValidator.validateNotEmpty('Code', 'code', type.code.value, errors)
    strategy.validateTypeAttributes(type, errors)

    const errorsMap = errors.reduce((map, error) => {
      map[error.field] = error.message
      return map
    }, {})

    return [errors, errorsMap]
  }

  _validateProperties(type, properties) {
    const errors = []
    const errorsMap = {}

    properties.forEach(property => {
      const propertyErrors = this._validateProperty(type, property)

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

  _validateProperty(type, property) {
    const errors = []

    FormValidator.validateNotEmpty('Code', 'code', property.code.value, errors)
    FormValidator.validateNotEmpty(
      'Label',
      'label',
      property.label.value,
      errors
    )
    FormValidator.validateNotEmpty(
      'Description',
      'description',
      property.description.value,
      errors
    )
    FormValidator.validateNotEmpty(
      'Data Type',
      'dataType',
      property.dataType.value,
      errors
    )

    if (property.dataType.value === openbis.DataType.CONTROLLEDVOCABULARY) {
      FormValidator.validateNotEmpty(
        'Vocabulary',
        'vocabulary',
        property.vocabulary.value,
        errors
      )
    } else if (property.dataType.value === openbis.DataType.MATERIAL) {
      FormValidator.validateNotEmpty(
        'Material Type',
        'materialType',
        property.materialType.value,
        errors
      )
    }

    const typeIsUsed = type.usages > 0
    const propertyIsNew = !property.original
    const propertyIsMandatory = property.mandatory.value
    const propertyWasMandatory = property.original
      ? property.original.mandatory.value
      : false

    if (
      typeIsUsed &&
      propertyIsMandatory &&
      (propertyIsNew || !propertyWasMandatory)
    ) {
      FormValidator.validateNotEmpty(
        'Initial Value',
        'initialValueForExistingEntities',
        property.initialValueForExistingEntities.value,
        errors
      )
    }

    return errors
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
  validateTypeAttributes(type, errors) {
    FormValidator.validateNotEmpty(
      'Generated code prefix',
      'generatedCodePrefix',
      type.generatedCodePrefix.value,
      errors
    )
  }
}

class CollectionTypeStrategy {
  validateTypeAttributes() {}
}

class DataSetTypeStrategy {
  validateTypeAttributes() {}
}

class MaterialTypeStrategy {
  validateTypeAttributes() {}
}
