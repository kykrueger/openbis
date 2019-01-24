import _ from 'lodash'

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