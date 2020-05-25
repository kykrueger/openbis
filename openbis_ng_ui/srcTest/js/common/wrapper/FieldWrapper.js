export default class FieldWrapper {
  constructor(wrapper) {
    this.wrapper = wrapper
  }

  getLabel() {
    return this.wrapper.prop('label')
  }

  getValue() {
    const value = this.wrapper.prop('value')
    if (value === null || value === undefined || value === '') {
      return null
    } else {
      return value
    }
  }

  getEnabled() {
    const disabled = this.wrapper.prop('disabled')
    return disabled === undefined || disabled === null || disabled === false
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        value: this.getValue(),
        enabled: this.getEnabled()
      }
    } else {
      return null
    }
  }
}
