import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'

export default class UserGroupFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === UserGroupFormSelectionType.USER) {
      this._handleRemoveUser(selection.params.id)
    } else if (selection.type === UserGroupFormSelectionType.ROLE) {
      this._handleRemoveRole(selection.params.id)
    }
  }

  _handleRemoveUser(userId) {
    const { users } = this.context.getState()

    const userIndex = users.findIndex(user => user.id === userId)

    const newUsers = Array.from(users)
    newUsers.splice(userIndex, 1)

    this.context.setState(state => ({
      ...state,
      users: newUsers,
      selection: null
    }))

    this.controller.changed(true)
  }

  _handleRemoveRole(roleId) {
    const { roles } = this.context.getState()

    const roleIndex = roles.findIndex(role => role.id === roleId)

    const newRoles = Array.from(roles)
    newRoles.splice(roleIndex, 1)

    this.context.setState(state => ({
      ...state,
      roles: newRoles,
      selection: null
    }))

    this.controller.changed(true)
  }
}
