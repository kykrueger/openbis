import _ from 'lodash'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerAddGroup {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { groups } = this.context.getState()

    const newGroup = {
      id: _.uniqueId('group-'),
      code: FormUtil.createField({}),
      description: FormUtil.createField({}),
      original: null
    }

    const newGroups = Array.from(groups)
    newGroups.push(newGroup)

    await this.context.setState(state => ({
      ...state,
      groups: newGroups,
      selection: {
        type: UserFormSelectionType.GROUP,
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
