import { combineReducers } from 'redux'
import * as actions from '../actions/actions.js'
import ui from './ui/ui.js'
import session from './session/session.js'
import route from './route/route.js'

export default function root(state = {}, action) {
  if (action.type === actions.CLEAR) {
    state = {
      initialized: state.initialized
    }
  }
  return combineReducers({
    initialized,
    ui,
    session,
    route
  })(state, action)
}

function initialized(state = false, action) {
  switch (action.type) {
    case actions.SET_INITIALIZED: {
      return action.payload.initialized
    }
    default: {
      return state
    }
  }
}
