export default class VocabularyFormControllerCancel {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    return this.controller.load().then(() => {
      return this.context.setState({
        mode: 'view'
      })
    })
  }
}
