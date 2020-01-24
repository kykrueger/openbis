import _ from 'lodash'

export default class ObjectTypeHandlerOrderChange {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  execute(type, params) {
    if (type === 'section') {
      let { fromIndex, toIndex } = params
      this.handleOrderChangeSection(fromIndex, toIndex)
    } else if (type === 'property') {
      let { fromSectionId, toSectionId, fromIndex, toIndex } = params
      this.handleOrderChangeProperty(
        fromSectionId,
        fromIndex,
        toSectionId,
        toIndex
      )
    }
  }

  handleOrderChangeSection(fromIndex, toIndex) {
    let newSections = Array.from(this.state.sections)
    let [section] = newSections.splice(fromIndex, 1)
    newSections.splice(toIndex, 0, section)
    this.setState(state => ({
      ...state,
      sections: newSections
    }))
  }

  handleOrderChangeProperty(fromSectionId, fromIndex, toSectionId, toIndex) {
    if (fromSectionId === toSectionId) {
      let sections = this.state.sections
      let sectionIndex = _.findIndex(sections, ['id', fromSectionId])
      let section = sections[sectionIndex]
      let newSection = {
        ...section,
        properties: Array.from(section.properties)
      }

      let [property] = newSection.properties.splice(fromIndex, 1)
      newSection.properties.splice(toIndex, 0, property)

      let newSections = Array.from(sections)
      newSections[sectionIndex] = newSection

      this.setState(state => ({
        ...state,
        sections: newSections
      }))
    } else {
      let sections = this.state.sections
      let newSections = Array.from(sections)

      let fromSectionIndex = _.findIndex(sections, ['id', fromSectionId])
      let toSectionIndex = _.findIndex(sections, ['id', toSectionId])
      let fromSection = sections[fromSectionIndex]
      let toSection = sections[toSectionIndex]

      let newFromSection = {
        ...fromSection,
        properties: Array.from(fromSection.properties)
      }
      let newToSection = {
        ...toSection,
        properties: Array.from(toSection.properties)
      }

      let [property] = newFromSection.properties.splice(fromIndex, 1)
      newToSection.properties.splice(toIndex, 0, property)

      newSections[fromSectionIndex] = newFromSection
      newSections[toSectionIndex] = newToSection

      let properties = this.state.properties
      let newProperties = Array.from(properties)

      let propertyIndex = _.findIndex(properties, ['id', property])
      let propertyObj = properties[propertyIndex]
      let newPropertyObj = {
        ...propertyObj,
        section: newToSection.id
      }
      newProperties[propertyIndex] = newPropertyObj

      this.setState(state => ({
        ...state,
        sections: newSections,
        properties: newProperties
      }))
    }
  }
}
