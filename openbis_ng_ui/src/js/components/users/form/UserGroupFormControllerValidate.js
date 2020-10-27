import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'

export default class UserGroupFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { group, users, roles } = this.context.getState()

    const newGroup = this._validateGroup(validator, group)
    const newUsers = this._validateUsers(validator, users)
    const newRoles = this._validateRoles(validator, roles)

    return {
      group: newGroup,
      users: newUsers,
      roles: newRoles
    }
  }

  async select(firstError) {
    const { group, users, roles } = this.context.getState()

    if (firstError.object === group) {
      await this.setSelection({
        type: UserGroupFormSelectionType.GROUP,
        params: {
          part: firstError.name
        }
      })
    } else if (users.includes(firstError.object)) {
      await this.setSelection({
        type: UserGroupFormSelectionType.USER,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.usersGridController) {
        await this.controller.usersGridController.showSelectedRow()
      }
    } else if (roles.includes(firstError.object)) {
      await this.setSelection({
        type: UserGroupFormSelectionType.ROLE,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.rolesGridController) {
        await this.controller.rolesGridController.showSelectedRow()
      }
    }
  }

  _validateGroup(validator, group) {
    validator.validateNotEmpty(group, 'code', 'Code')
    return validator.withErrors(group)
  }

  _validateUsers(validator, users) {
    users.forEach(user => {
      validator.validateNotEmpty(user, 'userId', 'User Id')
    })
    return validator.withErrors(users)
  }

  _validateRoles(validator, roles) {
    roles.forEach(role => {
      validator.validateNotEmpty(role, 'level', 'Level')
      if (role.space.visible) {
        validator.validateNotEmpty(role, 'space', 'Space')
      }
      if (role.project.visible) {
        validator.validateNotEmpty(role, 'project', 'Project')
      }
      if (role.role.visible) {
        validator.validateNotEmpty(role, 'role', 'Role')
      }
    })
    return validator.withErrors(roles)
  }
}
