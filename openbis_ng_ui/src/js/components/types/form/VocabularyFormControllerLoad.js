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
    if (this.object.type === objectTypes.NEW_VOCABULARY_TYPE) {
      await this.context.setState({
        mode: 'edit',
        loading: true
      })
      return this._init(null)
    } else if (this.object.type === objectTypes.VOCABULARY_TYPE) {
      await this.context.setState({
        mode: 'view',
        loading: true
      })
      return this.facade
        .loadVocabulary(this.object.id)
        .then(vocabulary => {
          return this._init(vocabulary)
        })
        .catch(error => {
          this.context.dispatch(actions.errorChange(error))
        })
        .finally(() => {
          this.context.setState({
            loaded: true,
            loading: false
          })
        })
    }
  }

  async _init(vocabulary) {
    return this.context.setState(() => ({
      vocabulary
    }))
  }
}
