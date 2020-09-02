import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    let loadedUser = null
    let loadedGroups = null

    if (!isNew) {
      ;[loadedUser, loadedGroups] = await Promise.all([
        this.facade.loadUser(object.id),
        this.facade.loadUserGroups(object.id)
      ])
      if (!loadedUser) {
        return
      }
    }

    const user = this._createUser(loadedUser)

    let groupsCounter = 0
    let groups = []

    if (loadedGroups) {
      groups = loadedGroups.map(loadedGroup =>
        this._createGroup('group-' + groupsCounter++, loadedGroup)
      )
    }

    const selection = this._createSelection(groups)

    return this.context.setState({
      user,
      groups,
      groupsCounter,
      selection,
      original: {
        user: user.original,
        groups: groups.map(group => group.original)
      }
    })
  }

  _createUser(loadedUser) {
    const user = {
      id: _.get(loadedUser, 'userId', null),
      userId: FormUtil.createField({
        value: _.get(loadedUser, 'userId', null),
        enabled: loadedUser === null
      }),
      space: FormUtil.createField({
        value: _.get(loadedUser, 'space.code', null)
      }),
      firstName: FormUtil.createField({
        value: _.get(loadedUser, 'firstName', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      lastName: FormUtil.createField({
        value: _.get(loadedUser, 'lastName', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      email: FormUtil.createField({
        value: _.get(loadedUser, 'email', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      active: FormUtil.createField({
        value: _.get(loadedUser, 'active', null),
        visible: loadedUser !== null,
        enabled: true
      })
    }
    if (loadedUser) {
      user.original = _.cloneDeep(user)
    }
    return user
  }

  _createGroup(id, loadedGroup) {
    const group = {
      id: id,
      code: FormUtil.createField({
        value: _.get(loadedGroup, 'code', null),
        enabled: false
      }),
      description: FormUtil.createField({
        value: _.get(loadedGroup, 'description', null),
        enabled: false
      })
    }
    group.original = _.cloneDeep(group)
    return group
  }

  _createSelection(newGroups) {
    const {
      selection: oldSelection,
      groups: oldGroups
    } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === 'group') {
      const oldGroup = _.find(
        oldGroups,
        oldGroup => oldGroup.id === oldSelection.params.id
      )
      const newGroup = _.find(
        newGroups,
        newGroup => newGroup.code.value === oldGroup.code.value
      )

      if (newGroup) {
        return {
          type: 'group',
          params: {
            id: newGroup.id
          }
        }
      }
    } else {
      return null
    }
  }
}
