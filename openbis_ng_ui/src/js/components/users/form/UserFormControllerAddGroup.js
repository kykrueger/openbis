import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerAddGroup {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { groups, groupsCounter } = this.context.getState()

    const newGroup = {
      id: 'group-' + groupsCounter++,
      code: FormUtil.createField({}),
      description: FormUtil.createField({}),
      original: null
    }

    const newGroups = Array.from(groups)
    newGroups.push(newGroup)

    await this.context.setState(state => ({
      ...state,
      groups: newGroups,
      groupsCounter,
      selection: {
        type: 'group',
        params: {
          id: newGroup.id,
          part: 'code'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.controller.groupsGridController) {
      await this.controller.groupsGridController.showSelectedRow()
    }
  }
}
