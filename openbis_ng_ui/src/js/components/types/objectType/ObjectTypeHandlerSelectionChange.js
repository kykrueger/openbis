export default class ObjectTypeHandlerSelectionChange {
  constructor(getState, setState) {
    this.getState = getState
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
