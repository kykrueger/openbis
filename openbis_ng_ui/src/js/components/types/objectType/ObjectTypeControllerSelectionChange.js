export default class ObjectTypeHandlerSelectionChange {
  constructor(controller) {
    this.context = controller.context
  }

  execute(type, params) {
    let selection = null

    if (type && params) {
      selection = {
        type,
        params
      }
    }

    this.context.setState(state => ({
      ...state,
      selection
    }))
  }
}
