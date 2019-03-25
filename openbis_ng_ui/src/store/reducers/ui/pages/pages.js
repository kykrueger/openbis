import { combineReducers } from 'redux'
import login from './login/login.js'
import types from './types/types.js'
import users from './users/users.js'

export default combineReducers({
  login,
  types,
  users
})
