import _ from 'lodash'

class ComponentState {
  constructor(initialState) {
    this.initialState = initialState
    this.initialStateCopy = _.cloneDeep(initialState)
    this.state = initialState
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

  assertState(expectedState) {
    expect(this.state).toEqual(expectedState)
    expect(this.initialState).toEqual(this.initialStateCopy)
  }
}

export default ComponentState
