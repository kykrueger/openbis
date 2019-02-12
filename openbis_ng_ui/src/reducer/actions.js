export default {
  init: () => ({
    type: 'INIT',
  }),
  expandNode: (node) => ({
    type: 'EXPAND-NODE',
    node: node
  }),
  collapseNode: (node) => ({
    type: 'COLLAPSE-NODE',
    node: node
  }),
  selectEntity: (entityPermId, entityType) => ({
    type: 'SELECT-ENTITY',
    entityPermId: entityPermId,
    entityType: entityType
  }),
  setFilter: filter => ({
    type: 'SET-FILTER',
    filter: filter
  }),
  setMode: mode => ({
    type: 'SET-MODE',
    mode
  }),
  setModeDone: (mode, data) => ({
    type: 'SET-MODE-DONE',
    mode,
    data
  }),
  error: (exception) => ({
    type: 'ERROR',
    exception: exception
  }),
  closeError: () => ({
    type: 'CLOSE-ERROR',
  }),
  // session
  login: (username, password) => ({
    type: 'LOGIN',
    username: username,
    password: password,
  }),
  loginDone: () => ({
    type: 'LOGIN-DONE',
  }),
  logout: () => ({
    type: 'LOGOUT'
  }),
  logoutDone: () => ({
    type: 'LOGOUT-DONE',
  }),
}
