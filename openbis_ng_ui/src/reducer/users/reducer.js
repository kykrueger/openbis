import initialState from '../initialstate.js'
import {browserExpandNode, browserCollapseNode, sortById, openEntities, dirtyEntities} from '../common/reducer'

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
      groupNodes.push({
        id: group.getPermId().getPermId(),
        permId: group.getPermId().getPermId(),
        expanded: false,
        loading: false,
        loaded: true,
        children: []
      })
    })

    sortById(groupNodes)

    userNodes.push({
      id: user.getPermId().getPermId(),
      permId: user.getPermId().getPermId(),
      expanded: false,
      loading: false,
      loaded: true,
      children: groupNodes
    })
  })

  sortById(userNodes)

  return {
    id: 'Users',
    expanded: false,
    loading: false,
    loaded: true,
    children: userNodes
  }
}

function browserSetModeDoneGroupNodes(groups) {
  let groupNodes = []

  groups.forEach(group => {
    let userNodes = []
    group.getUsers().forEach(user => {
      userNodes.push({
        id: user.getPermId().getPermId(),
        permId: user.getPermId().getPermId(),
        expanded: false,
        loading: false,
        loaded: true,
        children: []
      })
    })

    groupNodes.push({
      id: group.getPermId().getPermId(),
      permId: group.getPermId().getPermId(),
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

