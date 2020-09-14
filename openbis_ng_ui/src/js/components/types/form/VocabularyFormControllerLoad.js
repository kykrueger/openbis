import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import VocabularyFormSelectionType from '@src/js/components/types/form/VocabularyFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class VocabularyFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    let loadedVocabulary = null

    if (!isNew) {
      loadedVocabulary = await this.facade.loadVocabulary(object.id)
      if (!loadedVocabulary) {
        return
      }
    }

    const vocabulary = this._createVocabulary(loadedVocabulary)

    let termsCounter = 0
    let terms = []

    if (loadedVocabulary && loadedVocabulary.terms) {
      terms = loadedVocabulary.terms.map(loadedTerm =>
        this._createTerm('term-' + termsCounter++, loadedTerm)
      )
    }

    const selection = this._createSelection(terms)

    return this.context.setState({
      vocabulary,
      terms,
      termsCounter,
      selection,
      original: {
        vocabulary: vocabulary.original,
        terms: terms.map(term => term.original)
      }
    })
  }

  _createVocabulary(loadedVocabulary) {
    const vocabulary = {
      id: _.get(loadedVocabulary, 'code', null),
      code: FormUtil.createField({
        value: _.get(loadedVocabulary, 'code', null),
        enabled: loadedVocabulary === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedVocabulary, 'description', null)
      }),
      urlTemplate: FormUtil.createField({
        value: _.get(loadedVocabulary, 'urlTemplate', null)
      }),
      managedInternally: FormUtil.createField({
        value: _.get(loadedVocabulary, 'managedInternally', false)
      })
    }
    if (loadedVocabulary) {
      vocabulary.original = _.cloneDeep(vocabulary)
    }
    return vocabulary
  }

  _createTerm(id, loadedTerm) {
    const official = _.get(loadedTerm, 'official', false)

    const term = {
      id: id,
      code: FormUtil.createField({
        value: _.get(loadedTerm, 'code', null),
        enabled: false
      }),
      label: FormUtil.createField({
        value: _.get(loadedTerm, 'label', null)
      }),
      description: FormUtil.createField({
        value: _.get(loadedTerm, 'description', null)
      }),
      official: FormUtil.createField({
        value: official,
        enabled: !official
      })
    }
    term.original = _.cloneDeep(term)
    return term
  }

  _createSelection(newTerms) {
    const { selection: oldSelection, terms: oldTerms } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === VocabularyFormSelectionType.TERM) {
      const oldTerm = _.find(
        oldTerms,
        oldTerm => oldTerm.id === oldSelection.params.id
      )
      const newTerm = _.find(
        newTerms,
        newTerm => newTerm.code.value === oldTerm.code.value
      )

      if (newTerm) {
        return {
          type: VocabularyFormSelectionType.TERM,
          params: {
            id: newTerm.id
          }
        }
      }
    } else {
      return null
    }
  }
}
