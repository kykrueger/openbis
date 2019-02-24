import initialState from '../initialstate.js'
import users from './users.js'
import types from './types.js'
import * as pageActions from '../actions/page.js'
import * as loginActions from '../actions/login.js'
import * as notificationActions from '../actions/notification.js'

// reducers

function loading(loading = initialState.loading, action) {
  switch (action.type) {
  case notificationActions.ERROR: {
    return false
  }
  case loginActions.LOGIN: {
    return true
  }
  case loginActions.LOGIN_DONE: {
    return false
  }
  case loginActions.LOGOUT: {
    return true
  }
  case loginActions.LOGOUT_DONE: {
    return false
  }
  default: {
    return loading
  }
  }
}

function exceptions(exceptions = initialState.exceptions, action) {
  switch (action.type) {
  case notificationActions.ERROR: {
    return [].concat(exceptions, [action.exception])
  }
  case notificationActions.CLOSE_ERROR: {
    return exceptions.slice(1)
  }
  default: {
    return exceptions
  }
  }
}

function sessionActive(sessionActive = initialState.sessionActive, action) {
  switch (action.type) {
  case loginActions.LOGIN_DONE: {
    return true
  }
  case loginActions.LOGOUT_DONE: {
    return false
  }
  default: {
    return sessionActive
  }
  }
}

function mode(mode = initialState.mode, action) {
  if (!action.mode) {
    return mode
  }

  switch (action.type) {
  case pageActions.SET_MODE_DONE : {
    return action.mode
  }
  default: {
    return mode
  }
  }
}

function reducer(state = initialState, action) {
  let newMode = mode(state.mode, action)
  return {
    sessionActive: sessionActive(state.sessionActive, action),
    mode: newMode,
    users: newMode === 'USERS' ? users(state.users, action) : state.users,
    types: newMode === 'TYPES' ? types(state.types, action) : state.types,
    loading: loading(state.loading, action),
    exceptions: exceptions(state.exceptions, action),
  }
}

export default reducer
