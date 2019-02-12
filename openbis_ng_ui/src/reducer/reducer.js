import initialState from './initialstate.js'
import users from './users/reducer.js'
import types from './types/reducer.js'

// reducers

function loading(loading = initialState.loading, action) {
  switch (action.type) {
  case 'ERROR': {
    return false
  }
  case 'LOGIN': {
    return true
  }
  case 'LOGIN-DONE': {
    return false
  }
  case 'LOGOUT': {
    return true
  }
  case 'LOGOUT-DONE': {
    return false
  }
  default: {
    return loading
  }
  }
}

function exceptions(exceptions = initialState.exceptions, action) {
  switch (action.type) {
  case 'ERROR': {
    return [].concat(exceptions, [action.exception])
  }
  case 'CLOSE-ERROR': {
    return exceptions.slice(1)
  }
  default: {
    return exceptions
  }
  }
}

function sessionActive(sessionActive = initialState.sessionActive, action) {
  switch (action.type) {
  case 'LOGIN-DONE': {
    return true
  }
  case 'LOGOUT-DONE': {
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
  case 'SET-MODE-DONE' : {
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
