import _ from 'lodash'
import {putAndWait} from './../effects.js'
import {dto} from '../../../services/openbis.js'
import * as objectType from '../../consts/objectType.js'
import * as actions from '../../actions/actions.js'
import * as common from '../../common/browser.js'

export function* createNodes() {
  let {users, groups} = yield getUsersAndGroups()

  let userNodes = _.map(users, user => {
    return {
      id: `users/${user.userId}`,
      text: user.userId,
      object: { type: objectType.USER, id: user.userId },
      children: user.groupIds.map(groupId => {
        let group = groups[groupId]
        return {
          id: `users/${user.userId}/${group.code}`,
          text: group.code,
          object: { type: objectType.GROUP, id: group.code }
        }
      })
    }
  })

  common.sortNodes(userNodes)

  let groupNodes = _.map(groups, group => {
    return {
      id: `groups/${group.code}`,
      text: group.code,
      object: { type: objectType.GROUP, id: group.code },
      children: group.userIds.map(userId => {
        let user = users[userId]
        return {
          id: `groups/${group.code}/${user.userId}`,
          text: user.userId,
          object: { type: objectType.USER, id: user.userId }
        }
      })
    }
  })

  common.sortNodes(groupNodes)

  let nodes = [{
    id: 'users',
    text: 'Users',
    children: userNodes
  }, {
    id: 'groups',
    text: 'Groups',
    children: groupNodes
  }]

  return nodes
}

function* getUsersAndGroups(){
  let getUsersReponse = yield putAndWait(actions.apiRequest({
    method: 'searchPersons',
    params: [new dto.PersonSearchCriteria(), new dto.PersonFetchOptions()]
  }))

  let groupFetchOptions = new dto.AuthorizationGroupFetchOptions()
  groupFetchOptions.withUsers()

  let getGroupsReponse = yield putAndWait(actions.apiRequest({
    method: 'searchAuthorizationGroups',
    params: [new dto.AuthorizationGroupSearchCriteria(), groupFetchOptions]
  }))

  let users = {}
  let groups = {}

  getUsersReponse.payload.result.objects.forEach(user => {
    users[user.userId] = {
      userId: user.userId,
      firstName: user.firstName,
      lastName: user.lastName,
      groupIds: []
    }
  })

  getGroupsReponse.payload.result.objects.forEach(group => {
    groups[group.code] = {
      code: group.code,
      userIds: group.users.reduce((groupUserIds, groupUser) => {
        let user = users[groupUser.userId]
        if(user){
          user.groupIds.push(group.code)
          groupUserIds.push(user.userId)
        }
        return groupUserIds
      }, [])
    }
  })

  return {users, groups}
}
