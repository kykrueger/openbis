import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import RoleControllerChange from '@src/js/components/users/form/common/RoleControllerChange.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserGroupFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === UserGroupFormSelectionType.GROUP) {
      await this._handleChangeGroup(params)
    } else if (type === UserGroupFormSelectionType.USER) {
      await this._handleChangeUser(params)
    } else if (type === UserGroupFormSelectionType.ROLE) {
      await new RoleControllerChange(this.controller).execute(params)
    }
  }

  async _handleChangeGroup(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.group,
        params.field,
        params.value
      )
      return {
        group: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeUser(params) {
    await this.context.setState(state => {
      const newState = { ...state }

      const { newCollection } = FormUtil.changeCollectionItemField(
        state.users,
        params.id,
        params.field,
        params.value
      )
      newState.users = newCollection

      return newState
    })

    if (this.controller.usersGridController) {
      await this.controller.usersGridController.showSelectedRow()
    }

    await this.controller.changed(true)
  }
}
