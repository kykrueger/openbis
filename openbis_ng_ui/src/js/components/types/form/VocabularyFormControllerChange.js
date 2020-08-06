export default class VocabularyFormControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(type, params) {
    if (type === 'vocabulary') {
      const { field, value } = params
      this._handleChangeVocabulary(field, value)
    } else if (type === 'term') {
      const { id, field, value } = params
      this._handleChangeTerm(id, field, value)
    }
  }

  _handleChangeVocabulary(field, value) {
    this.context.setState(state => ({
      ...state,
      vocabulary: {
        ...state.vocabulary,
        [field]: {
          ...state.vocabulary[field],
          value
        }
      }
    }))

    this.controller.changed(true)
  }

  _handleChangeTerm(id, field, value) {
    let { terms } = this.context.getState()
    let newTerms = Array.from(terms)

    let index = terms.findIndex(term => term.id === id)
    let term = terms[index]
    let newTerm = {
      ...term,
      [field]: {
        ...term[field],
        value
      }
    }
    newTerms[index] = newTerm

    this.context.setState(state => ({
      ...state,
      terms: newTerms
    }))

    this.controller.changed(true)
  }
}
