export default class ButtonWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getLabel() {
    return this.wrapper.text()
  }

  getEnabled() {
    const disabled = this.wrapper.prop('disabled')
    return disabled === undefined || disabled === null || disabled === false
  }

  click() {
    this.wrapper.simulate('click')
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        enabled: this.getEnabled()
      }
    } else {
      return null
    }
  }
}
