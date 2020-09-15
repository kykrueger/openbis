import UserFormControllerRecalculateInheritedRoles from '@src/js/components/users/form/UserFormControllerRecalculateInheritedRoles.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'

export default class UserFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === UserFormSelectionType.GROUP) {
      this._handleRemoveGroup(selection.params.id)
    } else if (selection.type === UserFormSelectionType.ROLE) {
      this._handleRemoveRole(selection.params.id)
    }
  }

  _handleRemoveGroup(groupId) {
    const { groups } = this.context.getState()

    const groupIndex = groups.findIndex(group => group.id === groupId)

    const newGroups = Array.from(groups)
    newGroups.splice(groupIndex, 1)

    this.context.setState(state => {
      const newState = { ...state, groups: newGroups, selection: null }
      new UserFormControllerRecalculateInheritedRoles(this.controller).execute(
        newState
      )
      return newState
    })

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
