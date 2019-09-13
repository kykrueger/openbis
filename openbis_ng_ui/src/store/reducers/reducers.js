import { combineReducers } from 'redux'
import * as actions from '../actions/actions.js'
import ui from './ui/ui.js'
import session from './session/session.js'
import route from './route/route.js'

export default function root(state = {}, action) {
  if (action.type === actions.INIT) {
    state = {}
  }
  return combineReducers({
    ui,
    session,
    route
  })(state, action)
}
