import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerAddRole {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { roles, rolesCounter } = this.context.getState()

    const newRole = {
      id: 'role-' + rolesCounter++,
      space: FormUtil.createField({}),
      project: FormUtil.createField({}),
      role: FormUtil.createField({}),
      original: null
    }

    const newRoles = Array.from(roles)
    newRoles.push(newRole)

    await this.context.setState(state => ({
      ...state,
      roles: newRoles,
      rolesCounter,
      selection: {
        type: 'role',
        params: {
          id: newRole.id,
          part: 'space'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showSelectedRow()
    }
  }
}
