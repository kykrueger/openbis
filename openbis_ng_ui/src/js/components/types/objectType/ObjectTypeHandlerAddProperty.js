export default class ObjectTypeHandlerAddProperty {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  execute() {
    let { sections, properties, propertiesCounter, selection } = this.state

    let sectionIndex = null
    let sectionPropertyIndex = null
    let propertyIndex = null

    if (selection.type === 'section') {
      sectionIndex = sections.findIndex(
        section => section.id === selection.params.id
      )
      sectionPropertyIndex = sections[sectionIndex].properties.length
      propertyIndex = properties.length
    } else if (selection.type === 'property') {
      sections.forEach((section, i) => {
        section.properties.forEach((property, j) => {
          if (property === selection.params.id) {
            sectionIndex = i
            sectionPropertyIndex = j + 1
          }
        })
      })
      propertyIndex =
        properties.findIndex(property => property.id === selection.params.id) +
        1
    }

    let section = sections[sectionIndex]

    let newProperties = Array.from(properties)
    let newProperty = {
      id: 'property-' + propertiesCounter++,
      code: '',
      label: '',
      description: '',
      dataType: 'VARCHAR',
      vocabulary: null,
      materialType: null,
      showInEditView: true,
      mandatory: false,
      section: section.id
    }
    newProperties.splice(propertyIndex, 0, newProperty)

    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    newSection.properties.splice(sectionPropertyIndex, 0, newProperty.id)

    let newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    let newSelection = {
      type: 'property',
      params: {
        id: newProperty.id
      }
    }

    this.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      propertiesCounter,
      selection: newSelection
    }))
  }
}
