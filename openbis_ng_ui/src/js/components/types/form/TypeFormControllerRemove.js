import _ from 'lodash'

export default class TypeFormControllerRemove {
  constructor(controller) {
    this.context = controller.context
  }

  executeRemove(confirmed = false) {
    const { selection } = this.context.getState()
    if (selection.type === 'section') {
      this.handleRemoveSection(selection.params.id, confirmed)
    } else if (selection.type === 'property') {
      this.handleRemoveProperty(selection.params.id, confirmed)
    }
  }

  executeCancel() {
    const { selection } = this.context.getState()
    if (selection.type === 'section') {
      this.context.setState({
        removeSectionDialogOpen: false
      })
    } else if (selection.type === 'property') {
      this.context.setState({
        removePropertyDialogOpen: false
      })
    }
  }

  handleRemoveSection(sectionId, confirmed) {
    const { sections, properties } = this.context.getState()

    const sectionIndex = sections.findIndex(section => section.id === sectionId)
    const section = sections[sectionIndex]

    if (confirmed) {
      this.context.setState({
        removeSectionDialogOpen: false
      })
    } else if (this.isSectionUsed(section)) {
      this.context.setState({
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

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }

  handleRemoveProperty(propertyId, confirmed) {
    const { sections, properties } = this.context.getState()

    const propertyIndex = properties.findIndex(
      property => property.id === propertyId
    )
    const property = properties[propertyIndex]

    if (confirmed) {
      this.context.setState({
        removePropertyDialogOpen: false
      })
    } else if (this.isPropertyUsed(property)) {
      this.context.setState({
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

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      selection: null
    }))
  }

  isSectionUsed(section) {
    const { properties } = this.context.getState()

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
