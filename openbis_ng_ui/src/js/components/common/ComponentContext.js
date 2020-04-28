import autoBind from 'auto-bind'

export default class ComponentContext {
  constructor(component) {
    autoBind(this)
    this.initState = initialState => {
      component.state = initialState
    }
    this.getProps = () => {
      return component.props
    }
    this.getState = () => {
      return component.state
    }
    this.setState = component.setState.bind(component)
    this.dispatch = component.props.dispatch
  }

  initState(initialState) {
    this.initState(initialState)
  }

  getProps() {
    return this.getProps()
  }

  getState() {
    return this.getState()
  }

  setState(args) {
    return new Promise(resolve => {
      this.setState(args, () => {
        resolve()
      })
    })
  }

  dispatch(action) {
    this.dispatch(action)
  }

  getFacade() {
    return this.facade
  }
}
