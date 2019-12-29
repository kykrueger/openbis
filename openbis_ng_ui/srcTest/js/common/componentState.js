import _ from 'lodash'

const createSetState = state => {
  return newStateOrFunction => {
    if (_.isFunction(newStateOrFunction)) {
      _.assign(state, newStateOrFunction(state))
    } else {
      _.assign(state, newStateOrFunction)
    }
  }
}

export default { createSetState }
