import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PageControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async changeObjectField(stateKey, field, value, processFn) {
    await this.context.setState(state =>
      FormUtil.changeObjectField(state, stateKey, field, value, processFn)
    )

    await this.controller.changed(true)
  }

  async changeCollectionItemField(stateKey, itemId, field, value, processFn) {
    await this.context.setState(state =>
      FormUtil.changeCollectionItemField(
        state,
        stateKey,
        itemId,
        field,
        value,
        processFn
      )
    )

    await this.controller.changed(true)
  }
}
