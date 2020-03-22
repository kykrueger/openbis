import _ from 'lodash'

function mapNodes(parent, nodes, fn) {
  return nodes
    .map(node => {
      return fn(parent, node)
    })
    .filter(node => {
      return node !== null
    })
    .map(node => {
      if (node.children) {
        node.children = mapNodes(node, node.children, fn)
      }
      return node
    })
}

function getAllNodes(nodes) {
  let levels = getAllNodesByLevel(nodes)
  return _.concat(...levels)
}

function getAllNodesByLevel(nodes) {
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

export function sortNodes(nodes) {
  nodes.sort((n1, n2) => {
    return n1.text.localeCompare(n2.text)
  })
  nodes.forEach(node => {
    if (node.children) {
      sortNodes(node.children)
    }
  })
}

function getMatchingNodes(nodes, matchesFn) {
  let allNodes = getAllNodes(nodes)
  let matchingNodes = {}

  let hasMatchingChildren = function(node) {
    return (
      node.children &&
      _.some(node.children, child => {
        return matchingNodes[child.id]
      })
    )
  }

  allNodes.reverse().forEach(node => {
    if (hasMatchingChildren(node) || matchesFn(node)) {
      matchingNodes[node.id] = node.id
    }
  })

  return _.keys(matchingNodes)
}

export default {
  mapNodes,
  getAllNodes,
  getAllNodesByLevel,
  sortNodes,
  getMatchingNodes
}
