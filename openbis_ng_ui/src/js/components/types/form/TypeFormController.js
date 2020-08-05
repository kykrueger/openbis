import autoBind from 'auto-bind'
import TypeFormControllerLoad from './TypeFormControllerLoad.js'
import TypeFormControllerValidate from './TypeFormControllerValidate.js'
import TypeFormControllerChanged from './TypeFormControllerChanged.js'
import TypeFormControllerEdit from './TypeFormControllerEdit.js'
import TypeFormControllerCancel from './TypeFormControllerCancel.js'
import TypeFormControllerSave from './TypeFormControllerSave.js'
import TypeFormControllerRemove from './TypeFormControllerRemove.js'
import TypeFormControllerAddSection from './TypeFormControllerAddSection.js'
import TypeFormControllerAddProperty from './TypeFormControllerAddProperty.js'
import TypeFormControllerChange from './TypeFormControllerChange.js'
import TypeFormControllerOrderChange from './TypeFormControllerOrderChange.js'
import TypeFormControllerSelectionChange from './TypeFormControllerSelectionChange.js'

export default class TypeFormController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.object = context.getProps().object
    this.context = context
  }

  load() {
    return new TypeFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new TypeFormControllerValidate(this).execute(autofocus)
  }

  changed(changed) {
    return new TypeFormControllerChanged(this).execute(changed)
  }

  handleOrderChange(type, params) {
    new TypeFormControllerOrderChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    new TypeFormControllerSelectionChange(this).execute(type, params)
  }

  handleChange(type, params) {
    new TypeFormControllerChange(this).execute(type, params)
  }

  handleBlur() {
    this.validate()
  }

  handleAddSection() {
    new TypeFormControllerAddSection(this).execute()
  }

  handleAddProperty() {
    new TypeFormControllerAddProperty(this).execute()
  }

  handleRemove() {
    new TypeFormControllerRemove(this).executeRemove()
  }

  handleRemoveConfirm() {
    new TypeFormControllerRemove(this).executeRemove(true)
  }

  handleRemoveCancel() {
    new TypeFormControllerRemove(this).executeCancel()
  }

  handleEdit() {
    return new TypeFormControllerEdit(this).execute()
  }

  handleCancel() {
    return new TypeFormControllerCancel(this).execute()
  }

  handleSave() {
    return new TypeFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }

  getContext() {
    return this.context
  }

  getFacade() {
    return this.facade
  }
}
