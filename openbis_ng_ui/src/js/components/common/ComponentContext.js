import autoBind from 'auto-bind'

export default class ComponentContext {
  constructor(component) {
    autoBind(this)
    this.initStateFn = initialState => {
      component.state = initialState
    }
    this.getPropsFn = () => {
      return component.props
    }
    this.getStateFn = () => {
      return component.state
    }
    this.setStateFn = component.setState.bind(component)
    this.dispatchFn = component.props.dispatch
  }

  initState(initialState) {
    this.initStateFn(initialState)
  }

  getProps() {
    return this.getPropsFn()
  }

  getState() {
    return this.getStateFn()
  }

  setState(state) {
    return new Promise(resolve => {
      this.setStateFn(state, () => {
        resolve()
      })
    })
  }

  dispatch(action) {
    this.dispatchFn(action)
  }

  getFacade() {
    return this.facade
  }
}
