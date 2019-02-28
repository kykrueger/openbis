export default {
  init: () => ({
    type: 'INIT',
  }),
  // TODO setDirty for generic tabs instead of entities
  setDirty: (entityPermId, dirty) => ({
    type: 'SET-DIRTY',
    entityPermId: entityPermId,
    dirty: dirty
  }),
  expandNode: (node) => ({
    type: 'EXPAND-NODE',
    node: node
  }),
  collapseNode: (node) => ({
    type: 'COLLAPSE-NODE',
    node: node
  }),
  // database stuff
  setSpaces: spaces => ({
    type: 'SET-SPACES',
    spaces: spaces,
  }),
  setProjects: (projects, spacePermId) => ({
    type: 'SET-PROJECTS',
    projects: projects,
    spacePermId: spacePermId
  }),
  selectEntity: (entityPermId, entityType) => ({
    type: 'SELECT-ENTITY',
    entityPermId: entityPermId,
    entityType: entityType
  }),
  closeEntity: (entityPermId, entityType) => ({
    type: 'CLOSE-ENTITY',
    entityPermId: entityPermId,
    entityType: entityType
  }),
  changePage: page => ({
    type: 'CHANGE-PAGE',
    page: page
  }),
  sortBy: column => ({
    type: 'SORT-BY',
    column: column
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
  moveEntity: (source, target) => ({
    type: 'MOVE-ENTITY',
    source: source,
    target: target
  }),
  saveEntity: (entity) => ({
    type: 'SAVE-ENTITY',
    entity: entity
  }),
  saveEntityDone: (entity) => ({
    type: 'SAVE-ENTITY-DONE',
    entity: entity
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
