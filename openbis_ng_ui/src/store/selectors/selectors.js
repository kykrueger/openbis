export function getTabState(state) {
  switch (state.mode) {
  case 'USERS':
    return state.users
  case 'TYPES':
    return state.types
  default: {
    return {}
  }
  }
}
