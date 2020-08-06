import autoBind from 'auto-bind'
import VocabularyFormControllerLoad from './VocabularyFormControllerLoad.js'
import VocabularyFormControllerEdit from './VocabularyFormControllerEdit.js'
import VocabularyFormControllerCancel from './VocabularyFormControllerCancel.js'
import VocabularyFormControllerRemove from './VocabularyFormControllerRemove.js'
import VocabularyFormControllerValidate from './VocabularyFormControllerValidate.js'
import VocabularyFormControllerChange from './VocabularyFormControllerChange.js'
import VocabularyFormControllerChanged from './VocabularyFormControllerChanged.js'
import VocabularyFormControllerSelectionChange from './VocabularyFormControllerSelectionChange.js'
import VocabularyFormControllerSave from './VocabularyFormControllerSave.js'

export default class VocabularyFormController {
  constructor(facade) {
    autoBind(this)
    this.facade = facade
  }

  init(context) {
    this.object = context.getProps().object
    this.context = context
  }

  load() {
    return new VocabularyFormControllerLoad(this).execute()
  }

  changed(changed) {
    return new VocabularyFormControllerChanged(this).execute(changed)
  }

  validate(autofocus) {
    return new VocabularyFormControllerValidate(this).execute(autofocus)
  }

  handleEdit() {
    return new VocabularyFormControllerEdit(this).execute()
  }

  handleCancel() {
    return new VocabularyFormControllerCancel(this).execute()
  }

  handleRemove() {
    return new VocabularyFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new VocabularyFormControllerChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    return new VocabularyFormControllerSelectionChange(this).execute(
      type,
      params
    )
  }

  handleBlur() {
    return this.validate()
  }

  handleSave() {
    return new VocabularyFormControllerSave(this).execute()
  }

  getContext() {
    return this.context
  }

  getFacade() {
    return this.facade
  }
}
