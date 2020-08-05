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
      this.context.setState({
        loaded: true,
        loading: false
      })
    }
  }

  async _init(vocabulary) {
    return this.context.setState(() => ({
      vocabulary: this._createVocabulary(vocabulary),
      terms: this._createTerms(vocabulary)
    }))
  }

  _createVocabulary(vocabulary) {
    return {
      code: this._createField({
        value: _.get(vocabulary, 'code', null),
        enabled: vocabulary === null
      }),
      description: this._createField({
        value: _.get(vocabulary, 'description', null)
      })
    }
  }

  _createTerms(vocabulary) {
    if (!vocabulary) {
      return []
    }
    return vocabulary.terms.map(term => ({
      code: this._createField({
        value: _.get(term, 'code', null),
        enabled: false
      }),
      label: this._createField({
        value: _.get(term, 'label', null)
      }),
      description: this._createField({
        value: _.get(term, 'description', null)
      }),
      official: this._createField({
        value: _.get(term, 'official', false)
      })
    }))
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
