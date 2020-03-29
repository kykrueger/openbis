import _ from 'lodash'
import autoBind from 'auto-bind'

export default class ComponentContext {
  constructor() {
    autoBind(this)
    this.props = {}
    this.state = {}
    this.dispatch = jest.fn()
    this.facade = {}
  }

  initState(initialState) {
    this.state = initialState
  }

  getProps() {
    return this.props
  }

  setProps(props) {
    this.props = props
  }

  getState() {
    return this.state
  }

  setState(newStateOrFunction, callback) {
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

  dispatch(action) {
    this.dispatch(action)
  }

  getDispatch() {
    return this.dispatch
  }
}
