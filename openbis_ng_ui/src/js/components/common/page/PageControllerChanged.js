import actions from '@src/js/store/actions/actions.js'

export default class PageControllerChanged {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
  }

  execute(changed) {
    this.context.setState({
      changed
    })

    const { id, type } = this.controller.getObject()

    this.context.dispatch(
      actions.objectChange(this.controller.getPage(), type, id, changed)
    )
  }
}
