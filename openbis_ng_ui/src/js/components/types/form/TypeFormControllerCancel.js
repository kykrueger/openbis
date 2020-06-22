export default class TypeFormControllerEdit {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    this.context.setState(state => ({
      ...state,
      mode: 'view'
    }))
  }
}
