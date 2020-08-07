import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'

export default class VocabularyFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    const loadedVocabulary = isNew
      ? null
      : await this.facade.loadVocabulary(object.id)

    const vocabulary = this._createVocabulary(loadedVocabulary)
    const terms = this._createTerms(loadedVocabulary)

    return this.context.setState({
      vocabulary,
      terms,
      original: {
        vocabulary: vocabulary.original,
        terms: terms.map(term => term.original)
      }
    })
  }

  _createVocabulary(loadedVocabulary) {
    const vocabulary = {
      id: _.get(loadedVocabulary, 'code', null),
      code: this.createField({
        value: _.get(loadedVocabulary, 'code', null),
        enabled: loadedVocabulary === null
      }),
      description: this.createField({
        value: _.get(loadedVocabulary, 'description', null)
      }),
      urlTemplate: this.createField({
        value: _.get(loadedVocabulary, 'urlTemplate', null)
      }),
      managedInternally: this.createField({
        value: _.get(loadedVocabulary, 'managedInternally', false)
      })
    }
    if (loadedVocabulary) {
      vocabulary.original = _.cloneDeep(vocabulary)
    }
    return vocabulary
  }

  _createTerms(loadedVocabulary) {
    if (!loadedVocabulary) {
      return []
    }
    return loadedVocabulary.terms.map(loadedTerm => {
      const term = {
        id: _.get(loadedTerm, 'code', null),
        code: this.createField({
          value: _.get(loadedTerm, 'code', null),
          enabled: false
        }),
        label: this.createField({
          value: _.get(loadedTerm, 'label', null)
        }),
        description: this.createField({
          value: _.get(loadedTerm, 'description', null)
        }),
        official: this.createField({
          value: _.get(loadedTerm, 'official', true)
        })
      }
      term.original = _.cloneDeep(term)
      return term
    })
  }
}
