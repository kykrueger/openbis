import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import CheckboxFieldWrapper from '@srcTest/js/components/common/form/wrapper/CheckboxFieldWrapper.js'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import VocabularyFormParametersCommonWrapper from './VocabularyFormParametersCommonWrapper.js'

export default class VocabularyFormParametersVocabularyWrapper extends VocabularyFormParametersCommonWrapper {
  getCode() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'code' })
    )
  }

  getDescription() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'description' })
    )
  }

  getUrlTemplate() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'urlTemplate' })
    )
  }

  getChosenFromList() {
    return new CheckboxFieldWrapper(
      this.findComponent(CheckboxField).filter({ name: 'chosenFromList' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      code: this.getCode().toJSON(),
      description: this.getDescription().toJSON(),
      urlTemplate: this.getUrlTemplate().toJSON(),
      chosenFromList: this.getChosenFromList().toJSON()
    }
  }
}
