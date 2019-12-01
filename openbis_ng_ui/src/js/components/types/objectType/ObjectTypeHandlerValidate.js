import _ from 'lodash'

export default class ObjectTypeHandlerValidate {
  constructor(getState, setState) {
    this.getState = getState
    this.setState = setState
  }

  execute(enable) {
    const { validate, type, properties } = this.getState()

    if (!validate && !enable) {
      return
    }

    const typeErrors = this.validateType(type)
    const propertiesErrors = this.validateProperties(properties)

    this.setState(state => {
      const newType = {
        ...state.type,
        errors: typeErrors
      }
      const newProperties = state.properties.map(property => ({
        ...property,
        errors: propertiesErrors[property.id] || {}
      }))

      return {
        validate: validate || enable,
        type: newType,
        properties: newProperties
      }
    })

    return _.isEmpty(typeErrors) && _.isEmpty(propertiesErrors)
  }

  validateType(type) {
    const errors = {}

    this.validateNotEmpty('Code', 'code', type.code, errors)

    return errors
  }

  validateProperties(properties) {
    const errorsMap = {}

    properties.forEach(property => {
      const errors = {}

      this.validateNotEmpty('Code', 'code', property.code, errors)
      this.validateNotEmpty('Label', 'label', property.label, errors)
      this.validateNotEmpty(
        'Description',
        'description',
        property.description,
        errors
      )

      if (!_.isEmpty(errors)) {
        errorsMap[property.id] = errors
      }
    })

    return errorsMap
  }

  validateNotEmpty(label, name, value, errors) {
    if (value === null || value.trim() === '') {
      errors[name] = label + ' cannot be empty'
    }
  }
}
