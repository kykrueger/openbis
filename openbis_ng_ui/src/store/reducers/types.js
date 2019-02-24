import initialState from '../initialstate.js'
import {
  browserExpandNode,
  browserCollapseNode,
  browserSetFilter,
  openEntities,
  sortBy,
  emptyTreeNode,
  entityTreeNode
} from './common.js'
import * as pageActions from '../actions/page.js'
import * as browserActions from '../actions/browser.js'

export default function types(types = initialState.types, action) {
  return {
    browser: browser(types.browser, action),
    openEntities: openEntities(types.openEntities || initialState.types.openEntities, action)
  }
}

function browser(browser = initialState.types.browser, action) {
  switch (action.type) {
  case pageActions.SET_MODE_DONE:
    return browserSetModeDone(browser, action)
  case browserActions.SET_FILTER:
    return browserSetFilter(browser, action)
  case browserActions.EXPAND_NODE:
    return browserExpandNode(browser, action)
  case browserActions.COLLAPSE_NODE:
    return browserCollapseNode(browser, action)
  default:
    return browser
  }
}

function browserSetModeDone(browser, action) {
  if (action.data) {
    return {
      loaded: true,
      filter: '',
      nodes: [
        browserSetModeDoneTypeNodes('Object Types', action.data.objectTypes),
        browserSetModeDoneTypeNodes('Collection Types', action.data.collectionTypes),
        browserSetModeDoneTypeNodes('Data Set Types', action.data.dataSetTypes),
        browserSetModeDoneTypeNodes('Material Types', action.data.materialTypes)
      ]
    }
  } else {
    return browser
  }
}

function browserSetModeDoneTypeNodes(groupId, types) {
  let typeNodes = []

  types.forEach(type => {
    typeNodes.push(entityTreeNode(type, {loaded: true, selectable: true}))
  })

  sortBy(typeNodes, 'permId')

  return emptyTreeNode({id: groupId, text: groupId, loaded: true, children: typeNodes})
}
