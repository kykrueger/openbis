import initialState from '../initialstate.js'
import _ from 'lodash'

export default function users(users = initialState.users, action) {
  return {
    browser: browser(users.browser, action),
  }
}

function browser(browser = initialState.users.browser, action) {
  switch (action.type) {
  case 'SET-MODE-DONE':
    return browser_SetModeDone(browser, action)
  case 'EXPAND-NODE':
    return browser_ExpandNode(browser, action)
  case 'COLLAPSE-NODE':
    return browser_CollapseNode(browser, action)
  default:
    return browser
  }
}

function browser_SetModeDone(browser, action) {
  return {
    selectedNodeId: browser.selectedNodeId,
    nodes: [browser_SetModeDone_UserNodes(action.data.users, action.data.groups), browser_SetModeDone_GroupNodes(action.data.groups)]
  }
}

function browser_SetModeDone_UserNodes(users, groups) {
  let userGroupsMap = {}
  let userNodes = []

  groups.forEach(group => {
    group.getUsers().forEach(user => {
      let userGroups = userGroupsMap[user.getPermId().getPermId()] || []
      userGroups.push(group)
      userGroupsMap[user.getPermId().getPermId()] = userGroups
    })
  })

  users.forEach(user => {
    let userGroups = userGroupsMap[user.getPermId().getPermId()] || []
    let groupNodes = []

    userGroups.forEach(group => {
      groupNodes.push({
        id: group.getPermId().getPermId(),
        expanded: false,
        loading: false,
        loaded: true,
        children: []
      })
    })

    _sortById(groupNodes)

    userNodes.push({
      id: user.getPermId().getPermId(),
      expanded: false,
      loading: false,
      loaded: true,
      children: groupNodes
    })
  })

  _sortById(userNodes)

  return {
    id: 'Users',
    expanded: false,
    loading: false,
    loaded: true,
    children: userNodes
  }
}

function browser_SetModeDone_GroupNodes(groups) {
  let groupNodes = []

  groups.forEach(group => {
    let userNodes = []
    group.getUsers().forEach(user => {
      userNodes.push({
        id: user.getPermId().getPermId(),
        expanded: false,
        loading: false,
        loaded: true,
        children: []
      })
    })

    groupNodes.push({
      id: group.getPermId().getPermId(),
      expanded: false,
      loading: false,
      loaded: true,
      children: userNodes
    })
  })

  return {
    id: 'Groups',
    expanded: false,
    loading: false,
    loaded: true,
    children: groupNodes
  }
}

function browser_ExpandNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  _visitNodes(newBrowser.nodes, node => {
    if (node.id === action.node.id) {
      node.expanded = true
    }
  })
  return newBrowser
}

function browser_CollapseNode(browser, action) {
  let newBrowser = _.cloneDeep(browser)
  _visitNodes(newBrowser.nodes, node => {
    if (node.id === action.node.id) {
      node.expanded = false
    }
  })
  return newBrowser
}

const _visitNodes = (nodes, visitor) => {
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


const _sortById = (arr) => {
  arr.sort((i1, i2) => {
    return i1.id.localeCompare(i2.id)
  })
}