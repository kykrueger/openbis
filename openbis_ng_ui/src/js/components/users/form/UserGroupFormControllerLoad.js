import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserGroupFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(),
      this._loadGroup(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [users, spaces, projects] = await Promise.all([
      this.facade.loadUsers(),
      this.facade.loadSpaces(),
      this.facade.loadProjects()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        users,
        spaces,
        projects
      }
    }))
  }

  async _loadGroup(object, isNew) {
    let loadedGroup = null

    if (!isNew) {
      loadedGroup = await this.facade.loadGroup(object.id)
      if (!loadedGroup) {
        return
      }
    }

    const group = this._createGroup(loadedGroup)

    const users = []
    const roles = []

    if (loadedGroup && loadedGroup.roleAssignments) {
      loadedGroup.roleAssignments.forEach(loadedRole => {
        const role = this._createRole(loadedRole)
        roles.push(role)
      })
    }

    if (loadedGroup && loadedGroup.users) {
      loadedGroup.users.forEach(loadedUser => {
        const user = this._createUser(loadedUser)
        users.push(user)
      })
    }

    const selection = this._createSelection(users, roles)

    return this.context.setState({
      group,
      users,
      roles,
      selection,
      original: {
        group: group.original,
        users: users.map(user => user.original),
        roles: roles.map(role => role.original)
      }
    })
  }

  _createGroup(loadedGroup) {
    const group = {
      id: _.get(loadedGroup, 'code', null),
      code: FormUtil.createField({
        value: _.get(loadedGroup, 'code', null),
        enabled: loadedGroup === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedGroup, 'description', null)
      })
    }
    if (loadedGroup) {
      group.original = _.cloneDeep(group)
    }
    return group
  }

  _createUser(loadedUser) {
    const user = {
      id: _.uniqueId('user-'),
      userId: FormUtil.createField({
        value: _.get(loadedUser, 'userId', null)
      }),
      firstName: FormUtil.createField({
        value: _.get(loadedUser, 'firstName', null)
      }),
      lastName: FormUtil.createField({
        value: _.get(loadedUser, 'lastName', null)
      }),
      email: FormUtil.createField({
        value: _.get(loadedUser, 'email', null)
      }),
      space: FormUtil.createField({
        value: _.get(loadedUser, 'space.code', null)
      }),
      active: FormUtil.createField({
        value: _.get(loadedUser, 'active', null)
      })
    }
    user.original = _.cloneDeep(user)
    return user
  }

  _createRole(loadedRole) {
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

  _createSelection(newUsers, newRoles) {
    const {
      selection: oldSelection,
      users: oldUsers,
      roles: oldRoles
    } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === UserGroupFormSelectionType.USER) {
      const oldUser = _.find(
        oldUsers,
        oldUser => oldUser.id === oldSelection.params.id
      )
      const newUser = _.find(
        newUsers,
        newUser => newUser.userId.value === oldUser.userId.value
      )

      if (newUser) {
        return {
          type: UserGroupFormSelectionType.USER,
          params: {
            id: newUser.id
          }
        }
      }
    } else if (oldSelection.type === UserGroupFormSelectionType.ROLE) {
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
          type: UserGroupFormSelectionType.ROLE,
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
