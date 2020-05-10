export default class TypeFormPreviewPropertyWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getCode() {
    return this.wrapper.find('span[data-part="code"]')
  }

  getLabel() {
    return this.wrapper.find('span[data-part="label"]')
  }

  toJSON() {
    return {
      code: this.getCode().text(),
      label: this.getLabel().text()
    }
  }
}
