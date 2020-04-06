import autoBind from 'auto-bind'
import ObjectTypeControllerLoad from './ObjectTypeControllerLoad.js'
import ObjectTypeControllerValidate from './ObjectTypeControllerValidate.js'
import ObjectTypeControllerSave from './ObjectTypeControllerSave.js'
import ObjectTypeControllerRemove from './ObjectTypeControllerRemove.js'
import ObjectTypeControllerAddSection from './ObjectTypeControllerAddSection.js'
import ObjectTypeControllerAddProperty from './ObjectTypeControllerAddProperty.js'
import ObjectTypeControllerChange from './ObjectTypeControllerChange.js'
import ObjectTypeControllerOrderChange from './ObjectTypeControllerOrderChange.js'
import ObjectTypeControllerSelectionChange from './ObjectTypeControllerSelectionChange.js'

export default class ObjectTypeController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.context = context
  }

  load() {
    const { object } = this.context.getProps()
    return new ObjectTypeControllerLoad(this.context, this.facade).execute(
      object
    )
  }

  handleOrderChange(type, params) {
    new ObjectTypeControllerOrderChange(this.context).execute(type, params)
  }

  handleSelectionChange(type, params) {
    new ObjectTypeControllerSelectionChange(this.context).execute(type, params)
  }

  handleChange(type, params) {
    new ObjectTypeControllerChange(this.context).execute(type, params)
  }

  handleBlur() {
    new ObjectTypeControllerValidate(this.context).execute()
  }

  handleAddSection() {
    new ObjectTypeControllerAddSection(this.context).execute()
  }

  handleAddProperty() {
    new ObjectTypeControllerAddProperty(this.context).execute()
  }

  handleRemove() {
    new ObjectTypeControllerRemove(this.context).executeRemove()
  }

  handleRemoveConfirm() {
    new ObjectTypeControllerRemove(this.context).executeRemove(true)
  }

  handleRemoveCancel() {
    new ObjectTypeControllerRemove(this.context).executeCancel()
  }

  handleSave() {
    return new ObjectTypeControllerSave(this.context, this.facade).execute()
  }

  getFacade() {
    return this.facade
  }
}
