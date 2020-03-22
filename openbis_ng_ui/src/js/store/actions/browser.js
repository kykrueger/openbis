const BROWSER_INIT = 'BROWSER_INIT'
const BROWSER_FILTER_CHANGE = 'BROWSER_FILTERER_CHANGE'
const BROWSER_NODE_SELECT = 'BROWSER_NODE_SELECT'
const BROWSER_NODE_EXPAND = 'BROWSER_NODE_EXPAND'
const BROWSER_NODE_COLLAPSE = 'BROWSER_NODE_COLLAPSE'
const BROWSER_SET_FILTER = 'BROWSER_SET_FILTER'
const BROWSER_SET_NODES = 'BROWSER_SET_NODES'
const BROWSER_SET_SELECTED_NODES = 'BROWSER_SET_SELECTED_NODES'
const BROWSER_SET_VISIBLE_NODES = 'BROWSER_SET_VISIBLE_NODES'
const BROWSER_SET_EXPANDED_NODES = 'BROWSER_SET_EXPANDED_NODES'
const BROWSER_ADD_EXPANDED_NODES = 'BROWSER_ADD_EXPANDED_NODES'
const BROWSER_REMOVE_EXPANDED_NODES = 'BROWSER_REMOVE_EXPANDED_NODES'

const browserInit = page => ({
  type: BROWSER_INIT,
  payload: {
    page
  }
})

const browserFilterChange = (page, filter) => ({
  type: BROWSER_FILTER_CHANGE,
  payload: {
    page,
    filter
  }
})

const browserNodeSelect = (page, id) => ({
  type: BROWSER_NODE_SELECT,
  payload: {
    page,
    id
  }
})

const browserNodeExpand = (page, id) => ({
  type: BROWSER_NODE_EXPAND,
  payload: {
    page,
    id
  }
})

const browserNodeCollapse = (page, id) => ({
  type: BROWSER_NODE_COLLAPSE,
  payload: {
    page,
    id
  }
})

const browserSetFilter = (page, filter) => ({
  type: BROWSER_SET_FILTER,
  payload: {
    page,
    filter
  }
})

const browserSetNodes = (page, nodes) => ({
  type: BROWSER_SET_NODES,
  payload: {
    page,
    nodes
  }
})

const browserSetSelectedNodes = (page, ids) => ({
  type: BROWSER_SET_SELECTED_NODES,
  payload: {
    page,
    ids
  }
})

const browserSetVisibleNodes = (page, ids) => ({
  type: BROWSER_SET_VISIBLE_NODES,
  payload: {
    page,
    ids
  }
})

const browserSetExpandedNodes = (page, ids) => ({
  type: BROWSER_SET_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})

const browserAddExpandedNodes = (page, ids) => ({
  type: BROWSER_ADD_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})

const browserRemoveExpandedNodes = (page, ids) => ({
  type: BROWSER_REMOVE_EXPANDED_NODES,
  payload: {
    page,
    ids
  }
})

export default {
  BROWSER_INIT,
  BROWSER_FILTER_CHANGE,
  BROWSER_NODE_SELECT,
  BROWSER_NODE_EXPAND,
  BROWSER_NODE_COLLAPSE,
  BROWSER_SET_FILTER,
  BROWSER_SET_NODES,
  BROWSER_SET_SELECTED_NODES,
  BROWSER_SET_VISIBLE_NODES,
  BROWSER_SET_EXPANDED_NODES,
  BROWSER_ADD_EXPANDED_NODES,
  BROWSER_REMOVE_EXPANDED_NODES,
  browserInit,
  browserFilterChange,
  browserNodeSelect,
  browserNodeExpand,
  browserNodeCollapse,
  browserSetFilter,
  browserSetNodes,
  browserSetSelectedNodes,
  browserSetVisibleNodes,
  browserSetExpandedNodes,
  browserAddExpandedNodes,
  browserRemoveExpandedNodes
}
