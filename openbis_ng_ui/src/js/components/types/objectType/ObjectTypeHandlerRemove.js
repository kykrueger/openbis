import _ from 'lodash'

export default class ObjectTypeHandlerRemove {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  execute() {
    const { selection } = this.state
    if (selection.type === 'section') {
      this.handleRemoveSection(selection.params.id)
    } else if (selection.type === 'property') {
      this.handleRemoveProperty(selection.params.id)
    }
  }

  handleRemoveSection(sectionId) {
    const { sections, properties } = this.state

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    const newProperties = Array.from(properties)
    _.remove(
      newProperties,
      property => section.properties.indexOf(property.id) !== -1
    )

    const newSections = Array.from(sections)
    newSections.splice(sectionIndex, 1)

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }

  handleRemoveProperty(propertyId) {
    const { sections, properties } = this.state

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

    const newProperties = Array.from(properties)
    newProperties.splice(propertyIndex, 1)

    let sectionIndex = sections.findIndex(
      section => section.id === property.section
    )
    let section = sections[sectionIndex]
    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    _.remove(newSection.properties, property => property === propertyId)

    const newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }
}
