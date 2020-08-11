import PageController from '@src/js/components/common/page/PageController.js'
import GridController from '@src/js/components/common/grid/GridController.js'
import VocabularyFormControllerLoad from './VocabularyFormControllerLoad.js'
import VocabularyFormControllerRemove from './VocabularyFormControllerRemove.js'
import VocabularyFormControllerValidate from './VocabularyFormControllerValidate.js'
import VocabularyFormControllerChange from './VocabularyFormControllerChange.js'
import VocabularyFormControllerSave from './VocabularyFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class VocabularyFormController extends PageController {
  constructor(facade) {
    super(facade)
    this.gridController = new GridController()
  }

  getPage() {
    return pages.TYPES
  }

  getNewObjectType() {
    return objectTypes.NEW_VOCABULARY_TYPE
  }

  getExistingObjectType() {
    return objectTypes.VOCABULARY_TYPE
  }

  load() {
    return new VocabularyFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new VocabularyFormControllerValidate(this).execute(autofocus)
  }

  handleRemove() {
    return new VocabularyFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new VocabularyFormControllerChange(this).execute(type, params)
  }

  handleSave() {
    return new VocabularyFormControllerSave(this).execute()
  }
}
