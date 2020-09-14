import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'

export default class VocabularyFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === VocabularyFormSelectionType.TERM) {
      this._handleRemoveTerm(selection.params.id)
    }
  }

  _handleRemoveTerm(termId) {
    const { terms } = this.context.getState()

    const termIndex = terms.findIndex(term => term.id === termId)

    const newTerms = Array.from(terms)
    newTerms.splice(termIndex, 1)

    this.context.setState(state => ({
      ...state,
      terms: newTerms,
      selection: null
    }))

    this.controller.changed(true)
  }
}
