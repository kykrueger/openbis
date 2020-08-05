import autoBind from 'auto-bind'
import VocabularyFormControllerLoad from './VocabularyFormControllerLoad.js'
import VocabularyFormControllerEdit from './VocabularyFormControllerEdit.js'
import VocabularyFormControllerCancel from './VocabularyFormControllerCancel.js'

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

  handleEdit() {
    return new VocabularyFormControllerEdit(this).execute()
  }

  handleCancel() {
    return new VocabularyFormControllerCancel(this).execute()
  }

  getContext() {
    return this.context
  }

  getFacade() {
    return this.facade
  }
}
