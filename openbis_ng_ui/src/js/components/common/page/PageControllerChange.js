import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class PageControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  changeObjectField(stateKey, field, value, processFn) {
    this.context.setState(state =>
      FormUtil.changeObjectField(state, stateKey, field, value, processFn)
    )

    this.controller.changed(true)
  }

  changeCollectionItemField(stateKey, itemId, field, value, processFn) {
    this.context.setState(state =>
      FormUtil.changeCollectionItemField(
        state,
        stateKey,
        itemId,
        field,
        value,
        processFn
      )
    )

    this.controller.changed(true)
  }
}
