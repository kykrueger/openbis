import _ from 'lodash'

export function openEntities(openEntities, action) {
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


export const sortById = (arr) => {
  arr.sort((i1, i2) => {
    return i1.id.localeCompare(i2.id)
  })
}