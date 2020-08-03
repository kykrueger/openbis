export default class TypeFormControllerEdit {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    return this.controller.load().then(() => {
      return this.context.setState(state => ({
        ...state,
        mode: 'edit'
      }))
    })
  }
}
