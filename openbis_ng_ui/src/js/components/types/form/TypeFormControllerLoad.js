import actions from '@src/js/store/actions/actions.js'
import TypeFormControllerLoadType from './TypeFormControllerLoadType.js'
import TypeFormControllerLoadDictionaries from './TypeFormControllerLoadDictionaries.js'
import TypeFormControllerStrategies from './TypeFormControllerStrategies.js'

export default class TypeFormControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async execute() {
    const strategy = this._getStrategy()

    if (strategy.getNewObjectType() === this.object.type) {
      await this.context.setState({
        mode: 'edit'
      })
    } else if (strategy.getExistingObjectType() === this.object.type) {
      await this.context.setState({
        mode: 'view'
      })
    }

    await this.context.setState({
      loading: true,
      validate: false
    })

    this.controller.changed(false)

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

  _getStrategy() {
    const strategies = new TypeFormControllerStrategies()
    return strategies.getStrategy(this.object.type)
  }
}
