export default {
  // TODO setDirty for generic tabs
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
  selectEntity: entityPermId => ({
    type: 'SELECT-ENTITY',
    entityPermId: entityPermId
  }),
  closeEntity: entityPermId => ({
    type: 'CLOSE-ENTITY',
    entityPermId: entityPermId
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
    value: filter
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
}
