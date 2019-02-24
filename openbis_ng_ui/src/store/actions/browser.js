export const EXPAND_NODE = 'EXPAND-NODE'
export const COLLAPSE_NODE = 'COLLAPSE-NODE'
export const SET_FILTER = 'SET-FILTER'

export const expandNode = (node) => ({
  type: EXPAND_NODE,
  node: node
})

export const collapseNode = (node) => ({
  type: COLLAPSE_NODE,
  node: node
})

export const setFilter = filter => ({
  type: SET_FILTER,
  filter: filter
})
