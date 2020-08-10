import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
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

  _createTerms(loadedVocabulary) {
    if (!loadedVocabulary) {
      return []
    }
    return loadedVocabulary.terms.map(loadedTerm => {
      const term = {
        id: _.get(loadedTerm, 'code', null),
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
          value: _.get(loadedTerm, 'official', true)
        })
      }
      term.original = _.cloneDeep(term)
      return term
    })
  }
}
