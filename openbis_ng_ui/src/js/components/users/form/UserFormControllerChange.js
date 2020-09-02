import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'

export default class UserFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === 'user') {
      const { field, value } = params
      this.changeObjectField('user', field, value)
    } else if (type === 'group') {
      const { id, field, value } = params
      await this.changeCollectionItemField('groups', id, field, value)

      if (this.controller.groupsGridController) {
        await this.controller.groupsGridController.showSelectedRow()
      }
    } else if (type === 'role') {
      const { id, field, value } = params
      await this.changeCollectionItemField('roles', id, field, value)

      if (this.controller.rolesGridController) {
        await this.controller.rolesGridController.showSelectedRow()
      }
    }
  }
}
