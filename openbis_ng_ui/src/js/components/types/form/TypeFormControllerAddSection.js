import _ from 'lodash'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class TypeFormControllerAddSection {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    let { sections, selection } = this.context.getState()

    let newSections = Array.from(sections)
    let newSection = {
      id: _.uniqueId('section-'),
      name: FormUtil.createField(),
      properties: []
    }
    let newSelection = {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: newSection.id
      }
    }

    if (selection) {
      if (selection.type === TypeFormSelectionType.SECTION) {
        let index = sections.findIndex(
          section => section.id === selection.params.id
        )
        newSections.splice(index + 1, 0, newSection)
      } else if (selection.type === TypeFormSelectionType.PROPERTY) {
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

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      selection: newSelection
    }))

    this.controller.changed(true)
  }
}
