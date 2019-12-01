export default class ObjectTypeHandlerChange {
  constructor(state, setState, validateHandler) {
    this.state = state
    this.setState = setState
    this.validateHandler = validateHandler
  }

  execute(type, params) {
    if (type === 'type') {
      const { field, value } = params
      this.handleChangeType(field, value)
    } else if (type === 'section') {
      const { id, field, value } = params
      this.handleChangeSection(id, field, value)
    } else if (type === 'property') {
      const { id, field, value } = params
      this.handleChangeProperty(id, field, value)
    }
  }

  handleChangeType(field, value) {
    this.setState(
      state => ({
        ...state,
        type: {
          ...state.type,
          [field]: value
        }
      }),
      () => {
        this.validateHandler.execute()
      }
    )
  }

  handleChangeSection(id, field, value) {
    let sections = this.state.sections
    let newSections = Array.from(sections)

    let index = sections.findIndex(section => section.id === id)
    let section = sections[index]
    let newSection = {
      ...section,
      [field]: value
    }
    newSections[index] = newSection

    this.setState(
      state => ({
        ...state,
        sections: newSections
      }),
      () => {
        this.validateHandler.execute()
      }
    )
  }

  handleChangeProperty(id, field, value) {
    let properties = this.state.properties
    let newProperties = Array.from(properties)

    let index = properties.findIndex(property => property.id === id)
    let property = properties[index]
    let newProperty = {
      ...property,
      [field]: value
    }
    newProperties[index] = newProperty

    this.setState(
      state => ({
        ...state,
        properties: newProperties
      }),
      () => {
        this.validateHandler.execute()
      }
    )
  }
}
