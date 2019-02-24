import _ from 'lodash'
import * as pageActions from '../actions/page.js'

export function openEntities(openEntities, action) {
  switch (action.type) {
  case pageActions.SELECT_ENTITY: {
    const actionEntity = {
      permId: action.entityPermId,
      type: action.entityType
    }
    const entities = openEntities.entities
    return {
      entities: _.findIndex(entities, actionEntity) > -1 ? entities : [].concat(entities, [actionEntity]),
      selectedEntity: actionEntity,
    }
  }
  default: {
    return openEntities
  }
  }
}

export function browserExpandNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  getAllNodes(newBrowser.nodes).forEach(node => {
    if (node.id === action.node.id) {
      node.expanded = true
    }
  })
  return newBrowser
}

export function browserCollapseNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  getAllNodes(newBrowser.nodes).forEach(node => {
    if (node.id === action.node.id) {
      node.expanded = false
    }
  })
  return newBrowser
}

export function browserSetFilter(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  newBrowser.filter = action.filter
  getAllNodes(newBrowser.nodes).reverse().forEach(node => {
    if (action.filter === null || action.filter.trim() === '') {
      node.filtered = true
    } else {
      let filteredChildren = node.children !== undefined && node.children.some(node => {
        return node.filtered
      })

      if (filteredChildren) {
        node.filtered = true
        node.expanded = true
      } else {
        node.filtered = node.text.toLowerCase().indexOf(action.filter.toLowerCase()) !== -1
      }
    }
  })

  return newBrowser
}

export function emptyTreeNode(values = {}) {
  return _.merge({
    id: null,
    permId: null,
    type: null,
    text: null,
    selectable: false,
    filtered: true,
    expanded: false,
    loading: false,
    loaded: false,
    children: [],
  }, values)
}

export function entityTreeNode(entity, values = {}) {
  return _.merge(emptyTreeNode(), {
    id: entity['@type'] + '#' + entity.permId.permId,
    permId: entity.permId.permId,
    text: entity.permId.permId,
    type: entity['@type'],
  }, values)
}

export const getAllNodes = nodes => {
  let levels = getAllNodesByLevel(nodes)
  return _.concat(...levels)
}

export const getAllNodesByLevel = nodes => {
  let levels = []
  let toVisit = []

  toVisit.push(...nodes)

  while (toVisit.length > 0) {
    let levelSize = toVisit.length
    let level = []

    for (let i = 0; i < levelSize; i++) {
      let node = toVisit.shift()

      level.push(node)

      if (node.children !== undefined) {
        node.children.forEach(child => {
          toVisit.push(child)
        })
      }
    }

    levels.push(level)
  }

  return levels
}

export const sortBy = (arr, field) => {
  arr.sort((i1, i2) => {
    return i1[field].localeCompare(i2[field])
  })
}
