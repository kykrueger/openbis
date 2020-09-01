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
    return Promise.all([
      openbis.searchPersons(
        new openbis.PersonSearchCriteria(),
        new openbis.PersonFetchOptions()
      ),
      openbis.searchAuthorizationGroups(
        new openbis.AuthorizationGroupSearchCriteria(),
        new openbis.AuthorizationGroupFetchOptions()
      )
    ]).then(([users, groups]) => {
      const userNodes = users.getObjects().map(user => {
        return {
          id: `users/${user.userId}`,
          text: user.userId,
          object: { type: objectType.USER, id: user.userId }
        }
      })

      const groupNodes = groups.getObjects().map(group => {
        return {
          id: `groups/${group.code}`,
          text: group.code,
          object: { type: objectType.GROUP, id: group.code }
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
