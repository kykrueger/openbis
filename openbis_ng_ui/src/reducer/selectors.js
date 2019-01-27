export function getTabState(state) {
  switch (state.mode) {
  case 'DATABASE':
    return state.database
  case 'USERS':
    return state.users
  case 'TYPES':
    return state.types
  default: {
    return {}
  }
  }
}

export function getTabEntity(state, entity) {
  switch (state.mode) {
  case 'DATABASE':
    let tabState = getTabState(state)
    return tabState.spaces[entity.permId]
  default:
    return null
  }
}
