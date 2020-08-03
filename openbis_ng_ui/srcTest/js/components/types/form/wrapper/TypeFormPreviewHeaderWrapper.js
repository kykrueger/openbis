import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TypeFormHeader from '@src/js/components/types/form/TypeFormHeader.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'

export default class TypeFormPreviewHeaderWrapper extends BaseWrapper {
  getTitle() {
    return this.findComponent(TypeFormHeader)
  }

  getCode() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'code' })
    )
  }

  getParents() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'parents' })
    )
  }

  getContainer() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'container' })
    )
  }

  toJSON() {
    const title = this.getTitle().text().trim()
    return {
      title: title.length > 0 ? title : null,
      code: this.getCode().toJSON(),
      parents: this.getParents().toJSON(),
      container: this.getContainer().toJSON()
    }
  }
}
