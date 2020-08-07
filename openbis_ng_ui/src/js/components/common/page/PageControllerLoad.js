import actions from '@src/js/store/actions/actions.js'

export default class PageControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.facade = controller.facade
    this.object = controller.object
  }

  async load() {
    throw 'Method not implemented'
  }

  async execute() {
    try {
      await this.context.setState({
        loading: true,
        validate: false
      })

      const isNew = this.object.type === this.controller.getNewObjectType()

      if (isNew) {
        await this.context.setState({
          mode: 'edit'
        })
      } else {
        await this.context.setState({
          mode: 'view'
        })
      }

      await this.load(this.object, isNew)
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

  createField(params = {}) {
    return {
      value: null,
      visible: true,
      enabled: true,
      ...params
    }
  }
}
