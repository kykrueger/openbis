import merge from 'lodash/merge'
import initialState from './initialstate.js'
import database from './database/reducer.js'


function replaceNode(nodes, newNode) {
  return nodes.map( node => {
    if (node.id === newNode.id) {
      return newNode
    }
    return node
  })
}

function asTreeNode(entity) {
  return {
    id: entity.permId.permId,
    type: entity['@type'],
    expanded: false,
    loading: false,
    loaded: false,
    children: [],
  }
}

// reducers

function loading(loading = initialState.loading, action) {
  switch (action.type) {
  case 'SET-SPACES': {
    return false
  }
  case 'SAVE-ENTITY': {
    return true
  }
  case 'SAVED-ENTITY': {
    return false
  }
  default: {
    return loading
  }}
}


function databaseTreeNodes(databaseTreeNodes = initialState.databaseTreeNodes, action) {
  switch (action.type) {
  case 'SET-SPACES': {
    return action.spaces.map(asTreeNode)
  }
  case 'SET-PROJECTS': {
    const oldNode = databaseTreeNodes.filter( node => node.id === action.spacePermId )[0]
    const projectNodes = action.projects.map(asTreeNode)
    const node = merge({}, oldNode, { loading: false, loaded: true, children: projectNodes })
    return replaceNode(databaseTreeNodes, node)
  }
  case 'EXPAND-NODE': {
    const loading = action.node.loaded === false
    const node = merge({}, action.node, { expanded: true, loading: loading })
    return replaceNode(databaseTreeNodes, node)
  }
  case 'COLLAPSE-NODE': {
    const node = merge({}, action.node, { expanded: false })
    return replaceNode(databaseTreeNodes, node)
  }
  default: {
    return databaseTreeNodes
  }}
}

function openEntities(openEntities = initialState.openEntities, action) {
  switch (action.type) {
  case 'SELECT-ENTITY': {
    const entities = openEntities.entities
    return {
      entities: entities.indexOf(action.entityPermId) > -1 ? entities : [].concat(entities, [action.entityPermId]),
      selectedEntity: action.entityPermId,
    }
  }
  case 'CLOSE-ENTITY': {
    const newOpenEntities = openEntities.entities.filter(e => e !== action.entityPermId)
    if (openEntities.selectedEntity === action.entityPermId) {
      const oldIndex = openEntities.entities.indexOf(action.entityPermId)
      const newIndex = oldIndex === newOpenEntities.length ? oldIndex - 1 : oldIndex
      const selectedEntity = newIndex > -1 ? newOpenEntities[newIndex] : null  
      return {
        entities: newOpenEntities,
        selectedEntity: selectedEntity,
      }
    } else {
      return {
        entities: newOpenEntities,
        selectedEntity: openEntities.selectedEntity,
      }
    }  
  }
  default: {
    return openEntities
  }}
}

function dirtyEntities(dirtyEntities = initialState.dirtyEntities, action) {
  switch (action.type) {
  case 'SET-DIRTY': {
    if (action.dirty) {
      return [].concat(dirtyEntities, [action.entityPermId])
    } else {
      return dirtyEntities.filter(e => e !== action.entityPermId)
    }
  }
  case 'SAVED-ENTITY': {
    return dirtyEntities.filter(permId => permId !== action.entity.permId.permId)
  }
  case 'CLOSE-ENTITY': {
    return dirtyEntities.filter(permId => permId !== action.entityPermId)
  }
  default: {
    return dirtyEntities
  }}
}

function reducer(state = initialState, action) {
  return {
    database: database(state.database, action),
    loading: loading(state.loading, action),
    databaseTreeNodes: databaseTreeNodes(state.databaseTreeNodes, action),
    openEntities: openEntities(state.openEntities, action),
    dirtyEntities: dirtyEntities(state.dirtyEntities, action),
  }
}

export default reducer
