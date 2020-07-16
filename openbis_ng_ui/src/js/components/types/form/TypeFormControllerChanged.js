import actions from '@src/js/store/actions/actions.js'
import pages from '@src/js/common/consts/pages.js'

export default class TypeFormControllerChanged {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(changed) {
    this.context.setState({
      changed
    })
    this.context.dispatch(
      actions.objectChange(
        pages.TYPES,
        this.controller.object.type,
        this.controller.object.id,
        changed
      )
    )
  }
}
