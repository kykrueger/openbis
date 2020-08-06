export default class PageControllerCancel {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
  }

  execute() {
    return this.controller.load().then(() => {
      return this.context.setState({
        mode: 'view'
      })
    })
  }
}
