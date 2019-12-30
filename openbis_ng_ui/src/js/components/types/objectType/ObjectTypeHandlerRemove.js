import _ from 'lodash'

export default class ObjectTypeHandlerRemove {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  executeRemove(confirmed = false) {
    const { selection } = this.state
    if (selection.type === 'section') {
      this.handleRemoveSection(selection.params.id, confirmed)
    } else if (selection.type === 'property') {
      this.handleRemoveProperty(selection.params.id, confirmed)
    }
  }

  executeCancel() {
    const { selection } = this.state
    if (selection.type === 'section') {
      this.setState({
        removeSectionDialogOpen: false
      })
    } else if (selection.type === 'property') {
      this.setState({
        removePropertyDialogOpen: false
      })
    }
  }

  handleRemoveSection(sectionId, confirmed) {
    const { sections, properties } = this.state

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    if (confirmed) {
      this.setState({
        removeSectionDialogOpen: false
      })
    } else if (this.isSectionUsed(section)) {
      this.setState({
        removeSectionDialogOpen: true
      })
      return
    }

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

  handleRemoveProperty(propertyId, confirmed) {
    const { sections, properties } = this.state

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

    if (confirmed) {
      this.setState({
        removePropertyDialogOpen: false
      })
    } else if (this.isPropertyUsed(property)) {
      this.setState({
        removePropertyDialogOpen: true
      })
      return
    }

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

  isSectionUsed(section) {
    const { properties } = this.state

    const propertiesMap = properties.reduce((map, property) => {
      map[property.id] = property
      return map
    }, {})

    return _.some(section.properties, propertyId => {
      const property = propertiesMap[propertyId]
      return this.isPropertyUsed(property)
    })
  }

  isPropertyUsed(property) {
    return _.isFinite(property.usages) && property.usages !== 0
  }
}
