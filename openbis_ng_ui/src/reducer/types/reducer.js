import initialState from '../initialstate.js'
import {browserExpandNode, browserCollapseNode, sortById} from '../common/reducer'

export default function types(types = initialState.types, action) {
  return {
    browser: browser(types.browser, action),
  }
}

function browser(browser = initialState.types.browser, action) {
  switch (action.type) {
  case 'SET-MODE-DONE':
    return browserSetModeDone(browser, action)
  case 'EXPAND-NODE':
    return browserExpandNode(browser, action)
  case 'COLLAPSE-NODE':
    return browserCollapseNode(browser, action)
  default:
    return browser
  }
}

function browserSetModeDone(browser, action) {
  return {
    selectedNodeId: browser.selectedNodeId,
    nodes: [
      browserSetModeDoneTypeNodes('Object Types', action.data.objectTypes),
      browserSetModeDoneTypeNodes('Collection Types', action.data.collectionTypes),
      browserSetModeDoneTypeNodes('Data Set Types', action.data.dataSetTypes),
      browserSetModeDoneTypeNodes('Material Types', action.data.materialTypes)
    ]
  }
}

function browserSetModeDoneTypeNodes(groupId, types) {
  let typeNodes = []

  types.forEach(type => {
    typeNodes.push({
      id: type.getPermId().getPermId(),
      expanded: false,
      loading: false,
      loaded: true,
      children: []
    })
  })

  sortById(typeNodes)

  return {
    id: groupId,
    expanded: false,
    loading: false,
    loaded: true,
    children: typeNodes
  }
}