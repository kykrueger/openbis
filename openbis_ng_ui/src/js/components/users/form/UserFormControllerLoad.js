import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(),
      this._loadUser(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [groups, spaces, projects] = await Promise.all([
      this.facade.loadGroups(),
      this.facade.loadSpaces(),
      this.facade.loadProjects()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        groups,
        spaces,
        projects
      }
    }))
  }

  async _loadUser(object, isNew) {
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

    let rolesCounter = 0
    let roles = []

    if (loadedUser && loadedUser.getRoleAssignments()) {
      roles = loadedUser
        .getRoleAssignments()
        .map(loadedRole =>
          this._createRole('role-' + rolesCounter++, loadedRole)
        )
    }

    const selection = this._createSelection(groups, roles)

    return this.context.setState({
      user,
      groups,
      groupsCounter,
      roles,
      rolesCounter,
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
        value: _.get(loadedUser, 'active', true),
        enabled: loadedUser !== null
      })
    }
    if (loadedUser) {
      user.original = _.cloneDeep(user)
    }
    return user
  }

  _createRole(id, loadedRole) {
    const level = _.get(loadedRole, 'roleLevel', null)

    let space = null
    let project = null

    if (level === openbis.RoleLevel.SPACE) {
      space = _.get(loadedRole, 'space.code')
    } else if (level === openbis.RoleLevel.PROJECT) {
      space = _.get(loadedRole, 'project.space.code')
      project = _.get(loadedRole, 'project.identifier.identifier')
    }

    const role = {
      id: id,
      level: FormUtil.createField({
        value: level
      }),
      space: FormUtil.createField({
        value: space,
        visible: space !== null
      }),
      project: FormUtil.createField({
        value: project,
        visible: project !== null
      }),
      role: FormUtil.createField({
        value: _.get(loadedRole, 'role', null)
      })
    }
    role.original = _.cloneDeep(role)
    return role
  }

  _createGroup(id, loadedGroup) {
    const group = {
      id: id,
      code: FormUtil.createField({
        value: _.get(loadedGroup, 'code', null)
      }),
      description: FormUtil.createField({
        value: _.get(loadedGroup, 'description', null)
      })
    }
    group.original = _.cloneDeep(group)
    return group
  }

  _createSelection(newGroups, newRoles) {
    const {
      selection: oldSelection,
      groups: oldGroups,
      roles: oldRoles
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
    } else if (oldSelection.type === 'role') {
      const oldRole = _.find(
        oldRoles,
        oldRole => oldRole.id === oldSelection.params.id
      )
      const newRole = _.find(
        newRoles,
        newRole =>
          newRole.space.value === oldRole.space.value &&
          newRole.project.value === oldRole.project.value &&
          newRole.role.value === oldRole.role.value
      )

      if (newRole) {
        return {
          type: 'role',
          params: {
            id: newRole.id
          }
        }
      }
    } else {
      return null
    }
  }
}
