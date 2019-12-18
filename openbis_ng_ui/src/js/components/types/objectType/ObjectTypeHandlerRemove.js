import _ from 'lodash'

export default class ObjectTypeHandlerRemove {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  executeRemove() {
    const { selection } = this.state
    if (selection.type === 'section') {
      this.handleRemoveSection(selection.params.id)
    } else if (selection.type === 'property') {
      this.handleRemoveProperty(selection.params.id)
    }
  }

  executeRemoveConfirm() {
    this.setState({
      removeSectionDialogOpen: false,
      removePropertyDialogOpen: false
    })
    this.executeRemove()
  }

  executeRemoveCancel() {
    this.setState({
      removeSectionDialogOpen: false,
      removePropertyDialogOpen: false
    })
  }

  handleRemoveSection(sectionId) {
    const { sections, properties, removeSectionDialogOpen } = this.state

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    if (this.isSectionUsed(section) && !removeSectionDialogOpen) {
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

  handleRemoveProperty(propertyId) {
    const { sections, properties, removePropertyDialogOpen } = this.state

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

    if (this.isPropertyUsed(property) && !removePropertyDialogOpen) {
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
    return property.usages !== 0
  }
}
