import PageController from '@src/js/components/common/page/PageController.js'
import TypeFormControllerLoad from './TypeFormControllerLoad.js'
import TypeFormControllerValidate from './TypeFormControllerValidate.js'
import TypeFormControllerSave from './TypeFormControllerSave.js'
import TypeFormControllerRemove from './TypeFormControllerRemove.js'
import TypeFormControllerAddSection from './TypeFormControllerAddSection.js'
import TypeFormControllerAddProperty from './TypeFormControllerAddProperty.js'
import TypeFormControllerChange from './TypeFormControllerChange.js'
import TypeFormControllerOrderChange from './TypeFormControllerOrderChange.js'
import pages from '@src/js/common/consts/pages.js'

export default class TypeFormController extends PageController {
  getPage() {
    return pages.TYPES
  }

  load() {
    return new TypeFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new TypeFormControllerValidate(this).execute(autofocus)
  }

  handleOrderChange(type, params) {
    new TypeFormControllerOrderChange(this).execute(type, params)
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

  handleSave() {
    return new TypeFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
