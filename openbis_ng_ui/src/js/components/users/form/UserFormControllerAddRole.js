import _ from 'lodash'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerAddRole {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { roles } = this.context.getState()

    const newRole = {
      id: _.uniqueId('role-'),
      inheritedFrom: FormUtil.createField({}),
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
        type: UserFormSelectionType.ROLE,
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
