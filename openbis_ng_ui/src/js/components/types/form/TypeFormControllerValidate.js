import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'

import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.object = controller.object
    this.validator = new FormValidator()
  }

  async execute(autofocus) {
    const { validate, type, properties } = this.context.getState()

    if (!validate) {
      return true
    }

    const newType = {
      ...type
    }
    const newProperties = properties.map(property => ({
      ...property
    }))

    this._validateType(newType)
    this._validateProperties(newType, newProperties)

    const errors = this.validator.getErrors()

    if (!_.isEmpty(errors) && autofocus) {
      let selection = null

      const firstError = errors[0]
      if (firstError.object === newType) {
        selection = {
          type: 'type',
          params: {
            part: firstError.name
          }
        }
      } else if (newProperties.includes(firstError.object)) {
        selection = {
          type: 'property',
          params: {
            id: firstError.object.id,
            part: firstError.name
          }
        }
      }

      if (selection) {
        await this.context.setState({
          selection
        })
      }
    }

    await this.context.setState({
      type: newType,
      properties: newProperties
    })

    return _.isEmpty(errors)
  }

  _validateType(type) {
    const strategy = this._getStrategy()
    this.validator.validateNotEmpty(type, 'code', 'Code')
    strategy.validateTypeAttributes(type)
  }

  _validateProperties(type, properties) {
    properties.forEach(property => {
      this._validateProperty(type, property)
    })
  }

  _validateProperty(type, property) {
    this.validator.validateNotEmpty(property, 'code', 'Code')
    this.validator.validateNotEmpty(property, 'label', 'Label')
    this.validator.validateNotEmpty(property, 'description', 'Description')
    this.validator.validateNotEmpty(property, 'dataType', 'Data Type')

    if (property.vocabulary.visible) {
      this.validator.validateNotEmpty(property, 'vocabulary', 'Vocabulary')
    }
    if (property.materialType.visible) {
      this.validator.validateNotEmpty(property, 'materialType', 'Material Type')
    }
    if (property.initialValueForExistingEntities.visible) {
      this.validator.validateNotEmpty(
        property,
        'initialValueForExistingEntities',
        'Initial Value'
      )
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.setObjectTypeStrategy(new ObjectTypeStrategy(this))
    strategies.setCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.setDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.setMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  constructor(executor) {
    this.validator = executor.validator
  }

  validateTypeAttributes(type) {
    this.validator.validateNotEmpty(
      type,
      'generatedCodePrefix',
      'Generated code prefix'
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
