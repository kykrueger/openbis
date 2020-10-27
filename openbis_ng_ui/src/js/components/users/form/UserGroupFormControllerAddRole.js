import _ from 'lodash'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserGroupFormControllerAddRole {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { roles } = this.context.getState()

    const newRole = {
      id: _.uniqueId('role-'),
      level: FormUtil.createField({}),
      space: FormUtil.createField({
        visible: false
      }),
      project: FormUtil.createField({
        visible: false
      }),
      role: FormUtil.createField({
        visible: false
      }),
      original: null
    }

    const newRoles = Array.from(roles)
    newRoles.push(newRole)

    await this.context.setState(state => ({
      ...state,
      roles: newRoles,
      selection: {
        type: UserGroupFormSelectionType.ROLE,
        params: {
          id: newRole.id,
          part: 'level'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showSelectedRow()
    }
  }
}
