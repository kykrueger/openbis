import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'

export default class UserFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { user, groups, roles } = this.context.getState()

    const newUser = this._validateUser(validator, user)
    const newGroups = this._validateGroups(validator, groups)
    const newRoles = this._validateRoles(validator, roles)

    return {
      user: newUser,
      groups: newGroups,
      roles: newRoles
    }
  }

  async select(firstError) {
    const { user, groups, roles } = this.context.getState()

    if (firstError.object === user) {
      await this.setSelection({
        type: UserFormSelectionType.USER,
        params: {
          part: firstError.name
        }
      })
    } else if (groups.includes(firstError.object)) {
      await this.setSelection({
        type: UserFormSelectionType.GROUP,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.groupsGridController) {
        await this.controller.groupsGridController.showSelectedRow()
      }
    } else if (roles.includes(firstError.object)) {
      await this.setSelection({
        type: UserFormSelectionType.ROLE,
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

  _validateUser(validator, user) {
    validator.validateNotEmpty(user, 'userId', 'User Id')
    return validator.withErrors(user)
  }

  _validateGroups(validator, groups) {
    groups.forEach(group => {
      validator.validateNotEmpty(group, 'code', 'Code')
    })
    return validator.withErrors(groups)
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
