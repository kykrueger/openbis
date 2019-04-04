export const BROWSER_INIT = 'BROWSER_INIT'
export const BROWSER_RELEASE = 'BROWSER_RELEASE'
export const BROWSER_FILTER_CHANGED = 'BROWSER_FILTERER_CHANGED'
export const BROWSER_NODE_SELECTED = 'BROWSER_NODE_SELECTED'
export const BROWSER_NODE_EXPANDED = 'BROWSER_NODE_EXPANDED'
export const BROWSER_NODE_COLLAPSED = 'BROWSER_NODE_COLLAPSED'

export const BROWSER_SET_FILTER = 'BROWSER_SET_FILTER'
export const BROWSER_SET_NODES = 'BROWSER_SET_NODES'
export const BROWSER_SET_SELECTED_NODE = 'BROWSER_SET_SELECTED_NODE'
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

export const browserFilterChanged = (page, filter) => ({
  type: BROWSER_FILTER_CHANGED,
  payload: {
    page,
    filter
  }
})

export const browserNodeSelected = (page, id) => ({
  type: BROWSER_NODE_SELECTED,
  payload: {
    page,
    id
  }
})

export const browserNodeExpanded = (page, id) => ({
  type: BROWSER_NODE_EXPANDED,
  payload: {
    page,
    id
  }
})

export const browserNodeCollapsed = (page, id) => ({
  type: BROWSER_NODE_COLLAPSED,
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

export const browserSetSelectedNode = (page, id) => ({
  type: BROWSER_SET_SELECTED_NODE,
  payload: {
    page,
    id
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
