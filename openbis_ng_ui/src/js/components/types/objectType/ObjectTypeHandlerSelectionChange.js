export default class ObjectTypeHandlerSelectionChange {
  constructor(state, setState) {
    this.state = state
    this.setState = setState
  }

  execute(type, params) {
    let selection = null

    if (type && params) {
      selection = {
        type,
        params
      }
    }

    this.setState(state => ({
      ...state,
      selection
    }))
  }
}
