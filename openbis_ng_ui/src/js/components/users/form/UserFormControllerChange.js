import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'

export default class UserFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === 'user') {
      const { field, value } = params
      this.changeObjectField('user', field, value)
    } else if (type === 'group') {
      const { id, field, value } = params
      await this.changeCollectionItemField(
        'groups',
        id,
        field,
        value,
        (oldGroup, newGroup) => {
          newGroup = this._handleChangeGroupCode(oldGroup, newGroup)
          return newGroup
        }
      )

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

  _handleChangeGroupCode(oldGroup, newGroup) {
    const oldCode = oldGroup.code.value
    const newCode = newGroup.code.value

    if (oldCode !== newCode) {
      const { groups } = this.controller.getDictionaries()

      const group = groups.find(group => group.code === newCode)

      newGroup = {
        ...newGroup,
        description: {
          ...newGroup.description,
          value: group !== null ? group.description : null
        }
      }
    }

    return newGroup
  }
}
