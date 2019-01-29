import initialState from '../initialstate.js'
import {
  browserExpandNode,
  browserCollapseNode,
  browserSetFilter,
  sortBy,
  openEntities,
  dirtyEntities,
  emptyTreeNode,
  entityTreeNode
} from '../common/reducer'
import _ from 'lodash'

export default function users(users = initialState.users, action) {
  return {
    browser: browser(users.browser, action),
    openEntities: openEntities(users.openEntities || initialState.users.openEntities, action),
    dirtyEntities: dirtyEntities(users.dirtyEntities || initialState.users.dirtyEntities, action)
  }
}

function browser(browser = initialState.users.browser, action) {
  switch (action.type) {
  case 'SET-MODE-DONE':
    return browserSetModeDone(browser, action)
  case 'SET-FILTER':
    return browserSetFilter(browser, action)
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
      filter: '',
      nodes: [browserSetModeDoneUserNodes(action.data.users, action.data.groups), browserSetModeDoneGroupNodes(action.data.groups)]
    }
  } else {
    return browser
  }
}

function browserSetModeDoneUserNodes(users, groups) {
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
      groupNodes.push(entityTreeNode(group, {loaded: true, selectable: true, filterable: true}))
    })

    sortBy(groupNodes, 'permId')

    userNodes.push(entityTreeNode(user, {loaded: true, selectable: true, filterable: true, children: groupNodes}))
  })

  sortBy(userNodes, 'permId')

  return emptyTreeNode({
    id: 'Users',
    text: 'Users',
    loaded: true,
    children: userNodes
  })
}

function browserSetModeDoneGroupNodes(groups) {
  let groupNodes = []

  groups.forEach(group => {
    let userNodes = []

    group.getUsers().forEach(user => {
      userNodes.push(entityTreeNode(user, {loaded: true, selectable: true, filterable: true}))
    })

    sortBy(userNodes, 'permId')

    groupNodes.push(entityTreeNode(group, {loaded: true, selectable: true, filterable: true, children: userNodes}))
  })

  sortBy(groupNodes, 'permId')

  return emptyTreeNode({
    id: 'Groups',
    text: 'Groups',
    loaded: true,
    children: groupNodes
  })
}


