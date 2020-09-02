export default class UserFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === 'group') {
      this._handleRemoveGroup(selection.params.id)
    }
  }

  _handleRemoveGroup(groupId) {
    const { groups } = this.context.getState()

    const groupIndex = groups.findIndex(group => group.id === groupId)

    const newGroups = Array.from(groups)
    newGroups.splice(groupIndex, 1)

    this.context.setState(state => ({
      ...state,
      groups: newGroups,
      selection: null
    }))

    this.controller.changed(true)
  }
}
