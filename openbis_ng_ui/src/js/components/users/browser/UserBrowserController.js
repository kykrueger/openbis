import _ from 'lodash'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'

export default class UserBrowserController extends BrowserController {
  doGetPage() {
    return pages.USERS
  }

  async doLoadNodes() {
    let groupFetchOptions = new openbis.AuthorizationGroupFetchOptions()
    groupFetchOptions.withUsers()

    return Promise.all([
      openbis.searchPersons(
        new openbis.PersonSearchCriteria(),
        new openbis.PersonFetchOptions()
      ),
      openbis.searchAuthorizationGroups(
        new openbis.AuthorizationGroupSearchCriteria(),
        groupFetchOptions
      )
    ]).then(([usersResult, groupsResult]) => {
      const users = {}
      const groups = {}

      usersResult.getObjects().forEach(user => {
        users[user.userId] = {
          userId: user.userId,
          firstName: user.firstName,
          lastName: user.lastName,
          groupIds: []
        }
      })

      groupsResult.getObjects().forEach(group => {
        groups[group.code] = {
          code: group.code,
          userIds: group.users.reduce((groupUserIds, groupUser) => {
            let user = users[groupUser.userId]
            if (user) {
              user.groupIds.push(group.code)
              groupUserIds.push(user.userId)
            }
            return groupUserIds
          }, [])
        }
      })

      const userNodes = _.map(users, user => {
        return {
          id: `users/${user.userId}`,
          text: user.userId,
          object: { type: objectType.USER, id: user.userId },
          children: user.groupIds.map(groupId => {
            const group = groups[groupId]
            return {
              id: `users/${user.userId}/${group.code}`,
              text: group.code,
              object: { type: objectType.GROUP, id: group.code }
            }
          })
        }
      })

      const groupNodes = _.map(groups, group => {
        return {
          id: `groups/${group.code}`,
          text: group.code,
          object: { type: objectType.GROUP, id: group.code },
          children: group.userIds.map(userId => {
            const user = users[userId]
            return {
              id: `groups/${group.code}/${user.userId}`,
              text: user.userId,
              object: { type: objectType.USER, id: user.userId }
            }
          })
        }
      })

      let nodes = [
        {
          id: 'users',
          text: 'Users',
          children: userNodes
        },
        {
          id: 'groups',
          text: 'Groups',
          children: groupNodes
        }
      ]

      return nodes
    })
  }

  doGetObservedModifications() {
    return {
      [objectType.USER]: [objectOperation.CREATE, objectOperation.DELETE],
      [objectType.GROUP]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
