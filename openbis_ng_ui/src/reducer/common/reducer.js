import _ from 'lodash'

export function openEntities(openEntities, action) {
  switch (action.type) {
  case 'SELECT-ENTITY': {
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
  case 'CLOSE-ENTITY': {
    const actionEntity = {
      permId: action.entityPermId,
      type: action.entityType
    }
    const newOpenEntities = openEntities.entities.filter(entity => !_.isEqual(entity, actionEntity))
    if (_.isEqual(openEntities.selectedEntity, actionEntity)) {
      const oldIndex = _.findIndex(openEntities.entities, actionEntity)
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

export function dirtyEntities(dirtyEntities, action) {
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

export function browserExpandNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  visitNodes(newBrowser.nodes, node => {
    if (node.id === action.node.id) {
      node.expanded = true
    }
  })
  return newBrowser
}

export function browserCollapseNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  visitNodes(newBrowser.nodes, node => {
    if (node.id === action.node.id) {
      node.expanded = false
    }
  })
  return newBrowser
}

export function browserSetFilter(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  newBrowser.filter = action.filter
  visitNodes(newBrowser.nodes, node => {
    if (action.filter === null || action.filter.trim() === '') {
      node.filtered = true
    } else {
      node.filtered = node.text.toLowerCase().indexOf(action.filter.toLowerCase()) !== -1
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
    filterable: false,
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

export const visitNodes = (nodes, visitor) => {
  let toVisit = []
  let visited = {}

  toVisit.push(...nodes)

  while (toVisit.length > 0) {
    let node = toVisit.shift()

    if (!visited[node.id]) {
      visited[node.id] = true
      let result = visitor(node)
      if (result) {
        return result
      }
    }

    if (node.children !== undefined) {
      node.children.forEach((child) => {
        toVisit.push(child)
      })
    }
  }
}


export const sortBy = (arr, field) => {
  arr.sort((i1, i2) => {
    return i1[field].localeCompare(i2[field])
  })
}