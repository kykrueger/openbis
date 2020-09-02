import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'

export default class UserFormControllerChange extends PageControllerChange {
  constructor(controller) {
    super(controller)
    this.gridController = controller.gridController
  }

  async execute(type, params) {
    if (type === 'user') {
      const { field, value } = params
      this.changeObjectField('user', field, value)
    } else if (type === 'group') {
      const { id, field, value } = params
      await this.changeCollectionItemField('groups', id, field, value)

      if (this.gridController) {
        this.gridController.showSelectedRow()
      }
    }
  }
}
