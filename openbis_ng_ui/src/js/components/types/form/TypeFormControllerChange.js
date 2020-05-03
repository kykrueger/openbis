import _ from 'lodash'

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

    if (
      newProperty.scope === 'global' &&
      (field === 'code' || field === 'scope')
    ) {
      const { globalPropertyTypes } = this.controller.getDictionaries()
      const globalPropertyType = globalPropertyTypes.find(
        propertyType => propertyType.code === newProperty.code
      )

      if (globalPropertyType) {
        newProperty = {
          ...newProperty,
          label: _.get(globalPropertyType, 'label', null),
          description: _.get(globalPropertyType, 'description', null),
          dataType: _.get(globalPropertyType, 'dataType', null),
          vocabulary: _.get(globalPropertyType, 'vocabulary.code', null),
          materialType: _.get(globalPropertyType, 'materialType.code', null),
          schema: _.get(globalPropertyType, 'schema', null),
          transformation: _.get(globalPropertyType, 'transformation', null)
        }
      }
    }

    newProperties[index] = newProperty

    this.context.setState(state => ({
      ...state,
      properties: newProperties
    }))
  }
}
