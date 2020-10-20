import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
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

    const groups = []
    const roles = []

    if (loadedUser && loadedUser.roleAssignments) {
      loadedUser.roleAssignments.forEach(loadedRole => {
        const role = this._createRole(loadedRole)
        roles.push(role)
      })
    }

    if (loadedGroups) {
      loadedGroups.forEach(loadedGroup => {
        const group = this._createGroup(loadedGroup)
        groups.push(group)

        if (loadedGroup.roleAssignments) {
          loadedGroup.roleAssignments.forEach(loadedRole => {
            const role = this._createRole(loadedRole)
            roles.push(role)
          })
        }
      })
    }

    const selection = this._createSelection(groups, roles)

    return this.context.setState({
      user,
      groups,
      roles,
      selection,
      original: {
        user: user.original,
        groups: groups.map(group => group.original),
        roles: roles.map(role => role.original)
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

  _createRole(loadedRole) {
    const inheritedFrom = _.get(loadedRole, 'authorizationGroup.code', null)
    const level = _.get(loadedRole, 'roleLevel', null)

    let space = null
    let project = null

    if (level === openbis.RoleLevel.SPACE) {
      space = _.get(loadedRole, 'space.code')
    } else if (level === openbis.RoleLevel.PROJECT) {
      space = _.get(loadedRole, 'project.space.code')
      project = _.get(loadedRole, 'project.code')
    }

    const role = {
      id: _.uniqueId('role-'),
      techId: FormUtil.createField({
        value: _.get(loadedRole, 'id.techId', null)
      }),
      inheritedFrom: FormUtil.createField({
        value: inheritedFrom
      }),
      level: FormUtil.createField({
        value: level,
        enabled: inheritedFrom === null
      }),
      space: FormUtil.createField({
        value: space,
        visible: space !== null,
        enabled: inheritedFrom === null
      }),
      project: FormUtil.createField({
        value: project,
        visible: project !== null,
        enabled: inheritedFrom === null
      }),
      role: FormUtil.createField({
        value: _.get(loadedRole, UserFormSelectionType.ROLE, null),
        enabled: inheritedFrom === null
      })
    }
    role.original = _.cloneDeep(role)
    return role
  }

  _createGroup(loadedGroup) {
    const group = {
      id: _.uniqueId('group-'),
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
    } else if (oldSelection.type === UserFormSelectionType.GROUP) {
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
          type: UserFormSelectionType.GROUP,
          params: {
            id: newGroup.id
          }
        }
      }
    } else if (oldSelection.type === UserFormSelectionType.ROLE) {
      const oldRole = _.find(
        oldRoles,
        oldRole => oldRole.id === oldSelection.params.id
      )
      const newRole = _.find(
        newRoles,
        newRole =>
          newRole.inheritedFrom.value === oldRole.inheritedFrom.value &&
          newRole.space.value === oldRole.space.value &&
          newRole.project.value === oldRole.project.value &&
          newRole.role.value === oldRole.role.value
      )

      if (newRole) {
        return {
          type: UserFormSelectionType.ROLE,
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
