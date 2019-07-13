import { combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'
import * as actions from '../actions/actions.js'
import ui from './ui/ui.js'
import session from './session/session.js'
import history from '../history.js'

export default function root(state = {}, action) {
  if(action.type === actions.INIT){
    state = {}
  }
  return combineReducers({
    ui,
    session,
    router: connectRouter(history)
  })(state, action)
}
