export default class TypeFormParametersCommonWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getTitle() {
    return this.wrapper.find('TypeFormHeader')
  }

  toJSON() {
    return {
      title: this.getTitle().exists() ? this.getTitle().text() : null
    }
  }
}
