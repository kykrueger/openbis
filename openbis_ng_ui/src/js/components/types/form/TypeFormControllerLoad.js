import actions from '@src/js/store/actions/actions.js'
import TypeFormControllerLoadType from './TypeFormControllerLoadType.js'
import TypeFormControllerLoadDictionaries from './TypeFormControllerLoadDictionaries.js'

export default class TypeFormControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
  }

  async execute() {
    await this.context.setState({
      loading: true,
      validate: false
    })

    const typePromise = new TypeFormControllerLoadType(
      this.controller
    ).execute()

    const dictionariesPromise = new TypeFormControllerLoadDictionaries(
      this.controller
    ).execute()

    return Promise.all([typePromise, dictionariesPromise])
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
