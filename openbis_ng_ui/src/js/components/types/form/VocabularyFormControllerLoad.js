import _ from 'lodash'
import actions from '@src/js/store/actions/actions.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class VocabularyFormControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async execute() {
    try {
      if (this.object.type === objectTypes.NEW_VOCABULARY_TYPE) {
        await this.context.setState({
          mode: 'edit',
          loading: true
        })
        await this._init(null)
      } else if (this.object.type === objectTypes.VOCABULARY_TYPE) {
        await this.context.setState({
          mode: 'view',
          loading: true
        })
        const vocabulary = await this.facade.loadVocabulary(this.object.id)
        if (vocabulary) {
          await this._init(vocabulary)
        }
      }
    } catch (error) {
      this.context.dispatch(actions.errorChange(error))
    } finally {
      this.controller.changed(false)
      this.context.setState({
        loaded: true,
        loading: false
      })
    }
  }

  async _init(loadedVocabulary) {
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
      code: this._createField({
        value: _.get(loadedVocabulary, 'code', null),
        enabled: loadedVocabulary === null
      }),
      description: this._createField({
        value: _.get(loadedVocabulary, 'description', null)
      }),
      urlTemplate: this._createField({
        value: _.get(loadedVocabulary, 'urlTemplate', null)
      }),
      managedInternally: this._createField({
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
        code: this._createField({
          value: _.get(loadedTerm, 'code', null),
          enabled: false
        }),
        label: this._createField({
          value: _.get(loadedTerm, 'label', null)
        }),
        description: this._createField({
          value: _.get(loadedTerm, 'description', null)
        }),
        official: this._createField({
          value: _.get(loadedTerm, 'official', true)
        })
      }
      term.original = _.cloneDeep(term)
      return term
    })
  }

  _createField(params = {}) {
    return {
      value: null,
      visible: true,
      enabled: true,
      ...params
    }
  }
}
