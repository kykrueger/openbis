import initialState from '../initialstate.js'
import merge from 'lodash/merge'
import {openEntities, dirtyEntities, entityTreeNode, browserSetFilter} from '../common/reducer.js'

function filterOf(filter, columns) {
  if (filter == null || filter.length === 0) {
    return _ => true
  } else {
    return entity =>
      Object.keys(columns)
        .map(col => entity[col])
        .map(value => value != null && value.toString().includes(filter))
        .includes(true)
  }
}

function sortOf(orderColumn, direction, columns) {
  return (a, b) => {
    let valA = a[orderColumn]
    let valB = b[orderColumn]
    if (valA == null && valB == null) {
      return 0
    }
    if (valA == null) {
      return 1
    }
    if (valB == null) {
      return -1
    }
    const type = columns[orderColumn]
    if (type === 'int') {
      return direction === 'asc' ? valA - valB : valB - valA
    } else {
      if (valA < valB) {
        return direction === 'asc' ? -1 : 1
      } else if (valA > valB) {
        return direction === 'asc' ? 1 : -1
      } else {
        return 0
      }
    }
  }
}

function replaceNode(nodes, newNode) {
  return nodes.map(node => {
    if (node.id === newNode.id) {
      return newNode
    }
    return node
  })
}

function transformData(data, columns, filter, orderColumn, direction) {
  const filtered = data.filter(filterOf(filter, columns))
  if (orderColumn in columns) {
    return filtered.sort(sortOf(orderColumn, direction, columns))
  } else {
    return filtered
  }
}

const concat = (...arrays) => [].concat(...arrays)
const take = (array, n) => array.slice(0, n)
const drop = (array, n) => array.slice(n)
const remove = (array, index) => [...take(array, index), ...drop(array, index + 1)]

function move(array, oldIndex, newIndex) {
  const value = array[oldIndex]
  const removed = remove(array, oldIndex)
  return concat(take(removed, newIndex), [value], drop(removed, newIndex))
}


function entitiesByPermId(entities) {
  const byPermId = {}
  for (let entity of entities) {
    byPermId[entity.permId.permId] = entity
  }
  return byPermId
}


function spaces(spaces = initialState.database.spaces, action) {
  switch (action.type) {
  case 'SET-SPACES': {
    return entitiesByPermId(action.spaces)
  }
  case 'SAVE-ENTITY-DONE': {
    const newSpaces = Object.assign({}, spaces)
    newSpaces[action.entity.permId.permId] = action.entity
    return newSpaces
  }
  default: {
    return spaces
  }
  }
}


function projects(projects = initialState.database.projects, action) {
  switch (action.type) {
  case 'SET-PROJECTS': {
    return entitiesByPermId(action.projects)
  }
  default: {
    return projects
  }
  }
}


function table(table = initialState.database.table, action) {
  switch (action.type) {
  case 'CHANGE-PAGE': {
    return Object.assign({}, table, {page: action.page})
  }
  case 'SORT-BY': {
    const column = action.column
    const direction =
      column === table.sortColumn
        ? table.sortDirection === 'asc' ? 'desc' : 'asc'
        : 'asc'
    return Object.assign({}, table, {
      sortColumn: column,
      sortDirection: direction,
      page: 0,
      data: transformData(table.initialData, table.columns, table.filter, column, direction)
    })
  }
  case 'SET-FILTER': {
    return Object.assign({}, table, {
      page: 0,
      filter: action.value,
      data: transformData(table.initialData, table.columns, action.value, table.sortColumn, table.sortDirection)
    })
  }
  case 'MOVE-ENTITY': {
    const sourceIndex = table.data.findIndex(e => e.Id === action.source)
    const targetIndex = table.data.findIndex(e => e.Id === action.target)
    return Object.assign({}, table, {
      data: move(table.data, sourceIndex, targetIndex)
    })
  }
  default: {
    return table
  }
  }
}

function browser(browser = initialState.database.browser, action) {
  switch (action.type) {
  case 'SET-SPACES': {
    return {
      filter: browser.filter,
      nodes: action.spaces.map(space => entityTreeNode(space, {selectable: true, filterable: true}))
    }
  }
  case 'SET-PROJECTS': {
    const oldNode = browser.nodes.filter(node => node.permId === action.spacePermId)[0]
    const projectNodes = action.projects.map(project => entityTreeNode(project, {loaded: true}))
    const node = merge({}, oldNode, {loading: false, loaded: true, children: projectNodes})
    return {
      filter: browser.filter,
      nodes: replaceNode(browser.nodes, node)
    }
  }
  case 'SET-FILTER':
    return browserSetFilter(browser, action)
  case 'EXPAND-NODE': {
    const loading = action.node.loaded === false
    const node = merge({}, action.node, {expanded: true, loading: loading})
    return {
      filter: browser.filter,
      nodes: replaceNode(browser.nodes, node)
    }
  }
  case 'COLLAPSE-NODE': {
    const node = merge({}, action.node, {expanded: false})
    return {
      filter: browser.filter,
      nodes: replaceNode(browser.nodes, node)
    }
  }
  default: {
    return browser
  }
  }
}

export default function database(database = initialState.database, action) {
  return {
    spaces: spaces(database.spaces, action),
    projects: projects(database.projects, action),
    table: table(database.table, action),
    browser: browser(database.browser, action),
    openEntities: openEntities(database.openEntities || initialState.database.openEntities, action),
    dirtyEntities: dirtyEntities(database.dirtyEntities || initialState.database.dirtyEntities, action),
  }
}
