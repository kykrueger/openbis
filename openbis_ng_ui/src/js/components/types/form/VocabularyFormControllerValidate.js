import _ from 'lodash'
import FormValidator from '@src/js/components/common/form/FormValidator.js'

export default class VocabularyFormControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.object = controller.object
    this.validator = new FormValidator()
  }

  async execute(autofocus) {
    const { validate, vocabulary, terms } = this.context.getState()

    if (!validate) {
      return true
    }

    const newVocabulary = {
      ...vocabulary
    }
    const newTerms = terms.map(term => ({
      ...term
    }))

    this._validateVocabulary(newVocabulary)
    this._validateTerms(newVocabulary, newTerms)

    const errors = this.validator.getErrors()

    if (!_.isEmpty(errors) && autofocus) {
      let selection = null

      const firstError = errors[0]
      if (firstError.object === newVocabulary) {
        selection = {
          type: 'vocabulary',
          params: {
            part: firstError.name
          }
        }
      } else if (newTerms.includes(firstError.object)) {
        selection = {
          type: 'term',
          params: {
            id: firstError.object.id,
            part: firstError.name
          }
        }
      }

      if (selection) {
        await this.context.setState({
          selection
        })
      }
    }

    await this.context.setState({
      vocabulary: newVocabulary,
      terms: newTerms
    })

    return _.isEmpty(errors)
  }

  _validateVocabulary(vocabulary) {
    this.validator.validateNotEmpty(vocabulary, 'code', 'Code')
  }

  _validateTerms(vocabulary, terms) {
    terms.forEach(term => {
      this._validateTerm(vocabulary, term)
    })
  }

  _validateTerm(vocabulary, term) {
    this.validator.validateNotEmpty(term, 'code', 'Code')
  }
}
