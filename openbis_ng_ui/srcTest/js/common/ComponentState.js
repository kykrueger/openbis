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

  getSetState() {
    return newStateOrFunction => {
      let changes

      if (_.isFunction(newStateOrFunction)) {
        changes = newStateOrFunction(this.state)
      } else {
        changes = newStateOrFunction
      }

      this.state = { ...this.state, ...changes }
    }
  }

  assertState(expectedState) {
    expect(this.state).toEqual(expectedState)
    expect(this.initialState).toEqual(this.initialStateCopy)
  }
}

export default ComponentState
