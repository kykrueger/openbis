import openbis from '@src/js/services/openbis.js'
import actions from '@src/js/store/actions/actions.js'
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
          object: { type: objectType.USER, id: user.userId },
          canMatchFilter: true,
          canRemove: true
        }
      })

      const groupNodes = groups.getObjects().map(group => {
        return {
          id: `groups/${group.code}`,
          text: group.code,
          object: { type: objectType.USER_GROUP, id: group.code },
          canMatchFilter: true,
          canRemove: true
        }
      })

      let nodes = [
        {
          id: 'users',
          text: 'Users',
          children: userNodes,
          childrenType: objectType.NEW_USER,
          canAdd: true
        },
        {
          id: 'groups',
          text: 'Groups',
          children: groupNodes,
          childrenType: objectType.NEW_USER_GROUP,
          canAdd: true
        }
      ]

      return nodes
    })
  }

  doNodeAdd(node) {
    if (node && node.childrenType) {
      this.context.dispatch(
        actions.objectNew(this.getPage(), node.childrenType)
      )
    }
  }

  doGetObservedModifications() {
    return {
      [objectType.USER]: [objectOperation.CREATE, objectOperation.DELETE],
      [objectType.USER_GROUP]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
