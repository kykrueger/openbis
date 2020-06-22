import BaseWrapper from './BaseWrapper.js'

export default class FieldWrapper extends BaseWrapper {
  getLabel() {
    return this.wrapper.prop('label')
  }

  getValue() {
    return this.getStringValue(this.wrapper.prop('value'))
  }

  getEnabled() {
    return this.getBooleanValue(this.wrapper.prop('disabled'))
  }

  getError() {
    return this.getStringValue(this.wrapper.prop('error'))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        value: this.getValue(),
        enabled: this.getEnabled(),
        error: this.getError()
      }
    } else {
      return null
    }
  }
}
