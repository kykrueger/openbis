import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { type, properties } = this.context.getState()

    const newType = this._validateType(validator, type)
    const newProperties = this._validateProperties(validator, properties)

    return {
      type: newType,
      properties: newProperties
    }
  }

  async select(firstError) {
    const { type, properties } = this.context.getState()

    if (firstError.object === type) {
      await this.setSelection({
        type: 'type',
        params: {
          part: firstError.name
        }
      })
    } else if (properties.includes(firstError.object)) {
      await this.setSelection({
        type: 'property',
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })
    }
  }

  _validateType(validator, type) {
    const strategy = this._getStrategy()
    validator.validateNotEmpty(type, 'code', 'Code')
    validator.validateCode(type, 'code', 'Code')
    strategy.validateTypeAttributes(validator, type)
    return validator.withErrors(type)
  }

  _validateProperties(validator, properties) {
    properties.forEach(property => {
      this._validateProperty(validator, property)
    })
    return validator.withErrors(properties)
  }

  _validateProperty(validator, property) {
    validator.validateNotEmpty(property, 'code', 'Code')

    if (property.internal.value) {
      validator.validateInternalCode(property, 'code', 'Code')
    } else {
      validator.validateCode(property, 'code', 'Code')
    }

    validator.validateNotEmpty(property, 'label', 'Label')
    validator.validateNotEmpty(property, 'description', 'Description')
    validator.validateNotEmpty(property, 'dataType', 'Data Type')

    if (property.vocabulary.visible) {
      validator.validateNotEmpty(property, 'vocabulary', 'Vocabulary')
    }
    if (property.initialValueForExistingEntities.visible) {
      validator.validateNotEmpty(
        property,
        'initialValueForExistingEntities',
        'Initial Value'
      )
    }
  }

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    strategies.extendObjectTypeStrategy(new ObjectTypeStrategy())
    strategies.extendCollectionTypeStrategy(new CollectionTypeStrategy())
    strategies.extendDataSetTypeStrategy(new DataSetTypeStrategy())
    strategies.extendMaterialTypeStrategy(new MaterialTypeStrategy())
    return strategies.getStrategy(this.object.type)
  }
}

class ObjectTypeStrategy {
  validateTypeAttributes(validator, type) {
    validator.validateNotEmpty(
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
