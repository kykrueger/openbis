export default class PageControllerEdit {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
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
