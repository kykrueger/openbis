import TextFieldWrapper from '@srcTest/js/common/wrapper/TextFieldWrapper.js'

export default class TypeFormPreviewHeaderWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('TypeFormHeader')
  }

  getCode() {
    return new TextFieldWrapper(this.wrapper.find('TextFormField[name="code"]'))
  }

  getParents() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="parents"]')
    )
  }

  getContainer() {
    return new TextFieldWrapper(
      this.wrapper.find('TextFormField[name="container"]')
    )
  }

  toJSON() {
    return {
      title: this.getTitle().text(),
      code: this.getCode().toJSON(),
      parents: this.getParents().toJSON(),
      container: this.getContainer().toJSON()
    }
  }
}
