import ObjectTypeHandlerLoad from './ObjectTypeHandlerLoad.js'
import ObjectTypeHandlerValidate from './ObjectTypeHandlerValidate.js'
import ObjectTypeHandlerSave from './ObjectTypeHandlerSave.js'
import ObjectTypeHandlerRemove from './ObjectTypeHandlerRemove.js'
import ObjectTypeHandlerAddSection from './ObjectTypeHandlerAddSection.js'
import ObjectTypeHandlerAddProperty from './ObjectTypeHandlerAddProperty.js'
import ObjectTypeHandlerChange from './ObjectTypeHandlerChange.js'
import ObjectTypeHandlerOrderChange from './ObjectTypeHandlerOrderChange.js'
import ObjectTypeHandlerSelectionChange from './ObjectTypeHandlerSelectionChange.js'

export default class ObjectTypeController {
  init(objectId, getState, setState, facade) {
    this.objectId = objectId
    this.getState = getState
    this.setState = setState
    this.facade = facade
  }

  load() {
    new ObjectTypeHandlerLoad(
      this.objectId,
      this.getState,
      this.setState,
      this.facade
    ).execute()
  }

  handleOrderChange(type, params) {
    new ObjectTypeHandlerOrderChange(this.getState, this.setState).execute(
      type,
      params
    )
  }

  handleSelectionChange(type, params) {
    new ObjectTypeHandlerSelectionChange(this.getState, this.setState).execute(
      type,
      params
    )
  }

  handleChange(type, params) {
    new ObjectTypeHandlerChange(this.getState, this.setState).execute(
      type,
      params
    )
  }

  handleBlur() {
    new ObjectTypeHandlerValidate(this.getState, this.setState).execute()
  }

  handleAddSection() {
    new ObjectTypeHandlerAddSection(this.getState, this.setState).execute()
  }

  handleAddProperty() {
    new ObjectTypeHandlerAddProperty(this.getState, this.setState).execute()
  }

  handleRemove() {
    new ObjectTypeHandlerRemove(this.getState, this.setState).executeRemove()
  }

  handleRemoveConfirm() {
    new ObjectTypeHandlerRemove(this.getState, this.setState).executeRemove(
      true
    )
  }

  handleRemoveCancel() {
    new ObjectTypeHandlerRemove(this.getState, this.setState).executeCancel()
  }

  handleSave() {
    let loadHandler = new ObjectTypeHandlerLoad(
      this.objectId,
      this.getState,
      this.setState,
      this.facade
    )

    let validateHandler = new ObjectTypeHandlerValidate(
      this.getState,
      this.setState
    )

    new ObjectTypeHandlerSave(
      this.getState,
      this.setState,
      this.facade,
      loadHandler,
      validateHandler
    ).execute()
  }
}
