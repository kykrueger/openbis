export const BROWSER_INIT = 'BROWSER_INIT'
export const BROWSER_RELEASE = 'BROWSER_RELEASE'
export const BROWSER_FILTER_CHANGE = 'BROWSER_FILTERER_CHANGE'
export const BROWSER_NODE_SELECT = 'BROWSER_NODE_SELECT'
export const BROWSER_NODE_EXPAND = 'BROWSER_NODE_EXPAND'
export const BROWSER_NODE_COLLAPSE = 'BROWSER_NODE_COLLAPSE'

export const BROWSER_SET_FILTER = 'BROWSER_SET_FILTER'
export const BROWSER_SET_NODES = 'BROWSER_SET_NODES'
export const BROWSER_SET_SELECTED_NODES = 'BROWSER_SET_SELECTED_NODES'
export const BROWSER_SET_VISIBLE_NODES = 'BROWSER_SET_VISIBLE_NODES'
export const BROWSER_SET_EXPANDED_NODES = 'BROWSER_SET_EXPANDED_NODES'
export const BROWSER_ADD_EXPANDED_NODES = 'BROWSER_ADD_EXPANDED_NODES'
export const BROWSER_REMOVE_EXPANDED_NODES = 'BROWSER_REMOVE_EXPANDED_NODES'

export const browserInit = (page) => ({
  type: BROWSER_INIT,
  payload: {
    page
  }
})

export const browserRelease = (page) => ({
  type: BROWSER_RELEASE,
  payload: {
    page
  }
})

export const browserFilterChange = (page, filter) => ({
  type: BROWSER_FILTER_CHANGE,
  payload: {
    page,
    filter
  }
})

export const browserNodeSelect = (page, id) => ({
  type: BROWSER_NODE_SELECT,
  payload: {
    page,
    id
  }
})

export const browserNodeExpand = (page, id) => ({
  type: BROWSER_NODE_EXPAND,
  payload: {
    page,
    id
  }
})

export const browserNodeCollapse = (page, id) => ({
  type: BROWSER_NODE_COLLAPSE,
  payload: {
    page,
    id
  }
})

export const browserSetFilter = (page, filter) => ({
  type: BROWSER_SET_FILTER,
  payload: {
    page,
    filter
  }
})

export const browserSetNodes = (page, nodes) => ({
  type: BROWSER_SET_NODES,
  payload: {
    page,
    nodes
  }
})

export const browserSetSelectedNodes = (page, ids) => ({
  type: BROWSER_SET_SELECTED_NODES,
  payload: {
    page,
    ids
  }
})

export const browserSetVisibleNodes = (page, ids) => ({
  type: BROWSER_SET_VISIBLE_NODES,
  payload: {
    page,
    ids
  }
})

export const browserSetExpandedNodes = (page, ids) => ({
  type: BROWSER_SET_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})

export const browserAddExpandedNodes = (page, ids) => ({
  type: BROWSER_ADD_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})

export const browserRemoveExpandedNodes = (page, ids) => ({
  type: BROWSER_REMOVE_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})
