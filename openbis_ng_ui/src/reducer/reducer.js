import merge from 'lodash/merge'
import initialState from './initialstate.js'
import database from './database/reducer.js'
import users from './users/reducer.js'
import types from './types/reducer.js'


function replaceNode(nodes, newNode) {
  return nodes.map(node => {
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
  case 'SAVE-ENTITY-DONE': {
    return false
  }
  case 'ERROR': {
    return false
  }
  case 'LOGIN': {
    return true
  }
  case 'LOGOUT': {
    return true
  }
  case 'LOGOUT-DONE': {
    return false
  }
  default: {
    return loading
  }
  }
}


function databaseTreeNodes(databaseTreeNodes = initialState.databaseTreeNodes, action) {
  switch (action.type) {
  case 'SET-SPACES': {
    return action.spaces.map(asTreeNode)
  }
  case 'SET-PROJECTS': {
    const oldNode = databaseTreeNodes.filter(node => node.id === action.spacePermId)[0]
    const projectNodes = action.projects.map(asTreeNode).map(project => {
      return merge(project, {loaded: true})
    })
    const node = merge({}, oldNode, {loading: false, loaded: true, children: projectNodes})
    return replaceNode(databaseTreeNodes, node)
  }
  case 'EXPAND-NODE': {
    const loading = action.node.loaded === false
    const node = merge({}, action.node, {expanded: true, loading: loading})
    return replaceNode(databaseTreeNodes, node)
  }
  case 'COLLAPSE-NODE': {
    const node = merge({}, action.node, {expanded: false})
    return replaceNode(databaseTreeNodes, node)
  }
  default: {
    return databaseTreeNodes
  }
  }
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
  }
  }
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
  case 'SAVE-ENTITY-DONE': {
    return dirtyEntities.filter(permId => permId !== action.entity.permId.permId)
  }
  case 'CLOSE-ENTITY': {
    return dirtyEntities.filter(permId => permId !== action.entityPermId)
  }
  default: {
    return dirtyEntities
  }
  }
}

function exceptions(exceptions = initialState.exceptions, action) {
  switch (action.type) {
  case 'ERROR': {
    return [].concat(exceptions, [action.exception])
  }
  case 'CLOSE-ERROR': {
    return exceptions.slice(1)
  }
  default: {
    return exceptions
  }
  }
}

function sessionActive(sessionActive = initialState.sessionActive, action) {
  switch (action.type) {
  case 'LOGIN-DONE': {
    return true
  }
  case 'LOGOUT-DONE': {
    return false
  }
  default: {
    return sessionActive
  }
  }
}

function mode(mode = initialState.mode, action) {
  switch (action.type) {
  case 'SET-MODE-DONE' : {
    return action.mode
  }
  default: {
    return mode
  }
  }
}

function reducer(state = initialState, action) {
  let newMode = mode(state.mode, action);

  return {
    sessionActive: sessionActive(state.sessionActive, action),
    mode: newMode,
    database: database(state.database, action),
    users: newMode === 'USERS' ? users(state.users, action) : state.users,
    types: newMode === 'TYPES' ? types(state.types, action) : state.types,
    loading: loading(state.loading, action),
    databaseTreeNodes: databaseTreeNodes(state.databaseTreeNodes, action),
    openEntities: openEntities(state.openEntities, action),
    dirtyEntities: dirtyEntities(state.dirtyEntities, action),
    exceptions: exceptions(state.exceptions, action),
  }
}

export default reducer
