import _ from 'lodash'
import { combineReducers } from 'redux'
import actions from '@src/js/store/actions/actions.js'

const browser = combineReducers({
  filter,
  nodes,
  selectedNodes,
  visibleNodes,
  expandedNodes
})

function filter(state = '', action) {
  switch (action.type) {
    case actions.BROWSER_SET_FILTER:
      if (_.isEqual(state, action.payload.filter)) {
        return state
      } else {
        return action.payload.filter
      }
    default:
      return state
  }
}

function nodes(state = [], action) {
  switch (action.type) {
    case actions.BROWSER_SET_NODES:
      return action.payload.nodes
    default:
      return state
  }
}

function selectedNodes(state = [], action) {
  switch (action.type) {
    case actions.BROWSER_SET_SELECTED_NODES:
      if (_.isEqual(state, action.payload.ids)) {
        return state
      } else {
        return action.payload.ids
      }
    default:
      return state
  }
}

function visibleNodes(state = [], action) {
  switch (action.type) {
    case actions.BROWSER_SET_VISIBLE_NODES: {
      if (_.isEqual(state, action.payload.ids)) {
        return state
      } else {
        return action.payload.ids
      }
    }
    default: {
      return state
    }
  }
}

function expandedNodes(state = [], action) {
  let newState = null

  switch (action.type) {
    case actions.BROWSER_SET_EXPANDED_NODES: {
      newState = action.payload.ids
      break
    }
    case actions.BROWSER_ADD_EXPANDED_NODES: {
      newState = _.union(state, action.payload.ids)
      break
    }
    case actions.BROWSER_REMOVE_EXPANDED_NODES: {
      newState = _.without(state, ...action.payload.ids)
      break
    }
    default: {
      return state
    }
  }

  if (_.isEqual(state, newState)) {
    return state
  } else {
    return newState
  }
}

export default {
  browser
}
