export default class ObjectTypeHandlerAddSection {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  execute() {
    let { sections, sectionsCounter, selection } = this.state

    let newSections = Array.from(sections)
    let newSection = {
      id: 'section-' + sectionsCounter++,
      name: null,
      properties: []
    }
    let newSelection = {
      type: 'section',
      params: {
        id: newSection.id
      }
    }

    if (selection) {
      if (selection.type === 'section') {
        let index = sections.findIndex(
          section => section.id === selection.params.id
        )
        newSections.splice(index + 1, 0, newSection)
      } else if (selection.type === 'property') {
        let index = sections.findIndex(
          section => section.properties.indexOf(selection.params.id) !== -1
        )
        newSections.splice(index + 1, 0, newSection)
      } else {
        newSections.push(newSection)
      }
    } else {
      newSections.push(newSection)
    }

    this.setState(state => ({
      ...state,
      sections: newSections,
      sectionsCounter,
      selection: newSelection
    }))
  }
}
