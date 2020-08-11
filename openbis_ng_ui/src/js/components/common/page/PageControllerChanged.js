import actions from '@src/js/store/actions/actions.js'

export default class PageControllerChanged {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
  }

  async execute(newChanged) {
    const { changed } = this.context.getState()

    if (newChanged !== changed) {
      await this.context.setState({
        changed: newChanged
      })

      const { id, type } = this.controller.getObject()

      this.context.dispatch(
        actions.objectChange(this.controller.getPage(), type, id, newChanged)
      )
    }
  }
}
