import _ from 'lodash'
import autoBind from 'auto-bind'

class ComponentState {
  constructor() {
    autoBind(this)
  }

  static fromState(initialState) {
    let instance = new ComponentState()
    instance.state = initialState
    instance.initialState = initialState
    instance.initialStateCopy = _.cloneDeep(initialState)
    return instance
  }

  static fromController(controller) {
    let instance = new ComponentState()
    let initialState = controller.init(
      instance.getGetState(),
      instance.getSetState(),
      instance.getDispatch()
    )
    instance.state = initialState
    instance.initialState = initialState
    instance.initialStateCopy = _.cloneDeep(initialState)
    return instance
  }

  getState() {
    return this.state
  }

  getGetState() {
    return () => {
      return this.getState()
    }
  }

  getSetState() {
    return (newStateOrFunction, callback) => {
      let changes

      if (_.isFunction(newStateOrFunction)) {
        changes = newStateOrFunction(this.state)
      } else {
        changes = newStateOrFunction
      }

      this.state = { ...this.state, ...changes }

      if (callback) {
        callback()
      }
    }
  }

  getDispatch() {
    if (!this.dispatch) {
      this.dispatch = jest.fn()
    }
    return this.dispatch
  }

  assertState(expectedState) {
    expect(this.state).toEqual(expectedState)
    expect(this.initialState).toEqual(this.initialStateCopy)
  }
}

export default ComponentState
