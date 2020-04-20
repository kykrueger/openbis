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
    this.object = context.getProps().object
    this.context = context
  }

  load() {
    return new ObjectTypeControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new ObjectTypeControllerValidate(this).execute(autofocus)
  }

  handleOrderChange(type, params) {
    new ObjectTypeControllerOrderChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    new ObjectTypeControllerSelectionChange(this).execute(type, params)
  }

  handleChange(type, params) {
    new ObjectTypeControllerChange(this).execute(type, params)
  }

  handleBlur() {
    this.validate()
  }

  handleAddSection() {
    new ObjectTypeControllerAddSection(this).execute()
  }

  handleAddProperty() {
    new ObjectTypeControllerAddProperty(this).execute()
  }

  handleRemove() {
    new ObjectTypeControllerRemove(this).executeRemove()
  }

  handleRemoveConfirm() {
    new ObjectTypeControllerRemove(this).executeRemove(true)
  }

  handleRemoveCancel() {
    new ObjectTypeControllerRemove(this).executeCancel()
  }

  handleSave() {
    return new ObjectTypeControllerSave(this).execute()
  }

  getFacade() {
    return this.facade
  }
}
