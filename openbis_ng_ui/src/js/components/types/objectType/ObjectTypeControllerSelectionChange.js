export default class ObjectTypeHandlerSelectionChange {
  constructor(context) {
    this.context = context
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
