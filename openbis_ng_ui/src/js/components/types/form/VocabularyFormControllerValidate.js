import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'

export default class VocabularyFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { vocabulary, terms } = this.context.getState()

    const newVocabulary = {
      ...vocabulary
    }
    const newTerms = terms.map(term => ({
      ...term
    }))

    this._validateVocabulary(validator, newVocabulary)
    this._validateTerms(validator, newTerms)

    return {
      vocabulary: newVocabulary,
      terms: newTerms
    }
  }

  selection(newState, firstError) {
    if (firstError.object === newState.vocabulary) {
      return {
        type: 'vocabulary',
        params: {
          part: firstError.name
        }
      }
    } else if (newState.terms.includes(firstError.object)) {
      return {
        type: 'term',
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      }
    }
  }

  _validateVocabulary(validator, vocabulary) {
    validator.validateNotEmpty(vocabulary, 'code', 'Code')
  }

  _validateTerms(validator, terms) {
    terms.forEach(term => {
      this._validateTerm(validator, term)
    })
  }

  _validateTerm(validator, term) {
    validator.validateNotEmpty(term, 'code', 'Code')
  }
}
