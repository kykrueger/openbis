import TextField from '@src/js/components/common/form/TextField.jsx'
import TypeFormHeader from '@src/js/components/types/form/TypeFormHeader.jsx'

import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'

export default class TypeFormPreviewHeaderWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find(TypeFormHeader)
  }

  getCode() {
    return new TextFieldWrapper(
      this.wrapper.find(TextField).filter({ name: 'code' })
    )
  }

  getParents() {
    return new TextFieldWrapper(
      this.wrapper.find(TextField).filter({ name: 'parents' })
    )
  }

  getContainer() {
    return new TextFieldWrapper(
      this.wrapper.find(TextField).filter({ name: 'container' })
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
