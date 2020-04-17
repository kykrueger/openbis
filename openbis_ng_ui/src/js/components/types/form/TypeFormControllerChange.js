export default class TypeFormControllerChange {
  constructor(controller) {
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
        [field]: value
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
      [field]: value
    }
    newSections[index] = newSection

    this.context.setState(state => ({
      ...state,
      sections: newSections
    }))
  }

  _handleChangeProperty(id, field, value) {
    let { properties } = this.context.getState()
    let newProperties = Array.from(properties)

    let index = properties.findIndex(property => property.id === id)
    let property = properties[index]
    let newProperty = {
      ...property,
      [field]: value
    }
    newProperties[index] = newProperty

    this.context.setState(state => ({
      ...state,
      properties: newProperties
    }))
  }
}
