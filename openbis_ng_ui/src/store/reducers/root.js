import sessionActive from './sessionActive.js'
import mode from './mode.js'
import users from './users.js'
import types from './types.js'
import loading from './loading.js'
import exceptions from './exceptions.js'

export default function reducer(state = {}, action) {
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
