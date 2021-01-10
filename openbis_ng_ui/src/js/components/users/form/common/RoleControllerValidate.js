import RoleSelectionType from '@src/js/components/users/form/common/RoleSelectionType.js'

export default class RoleControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  validate(validator, roles) {
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

  async select(firstError) {
    await this.context.setState(state => ({
      ...state,
      selection: {
        type: RoleSelectionType.ROLE,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      }
    }))

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showSelectedRow()
    }
  }
}
