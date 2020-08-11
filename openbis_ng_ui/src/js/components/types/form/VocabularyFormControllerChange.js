import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'

export default class VocabularyFormControllerChange extends PageControllerChange {
  constructor(controller) {
    super(controller)
    this.gridController = controller.gridController
  }

  async execute(type, params) {
    if (type === 'vocabulary') {
      const { field, value } = params
      this.changeObjectField('vocabulary', field, value)
    } else if (type === 'term') {
      const { id, field, value } = params
      await this.changeCollectionItemField('terms', id, field, value)
      this.gridController.showSelectedRow()
    }
  }
}
