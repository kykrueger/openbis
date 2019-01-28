import initialState from '../initialstate.js'
import {
  browserExpandNode,
  browserCollapseNode,
  openEntities,
  dirtyEntities,
  sortBy,
  emptyTreeNode,
  entityTreeNode
} from '../common/reducer'

export default function types(types = initialState.types, action) {
  return {
    browser: browser(types.browser, action),
    openEntities: openEntities(types.openEntities || initialState.types.openEntities, action),
    dirtyEntities: dirtyEntities(types.dirtyEntities || initialState.types.dirtyEntities, action)
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
  if (action.data) {
    return {
      loaded: true,
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

  return emptyTreeNode({id: groupId, loaded: true, children: typeNodes})
}