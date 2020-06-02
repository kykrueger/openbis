import BaseWrapper from '@srcTest/js/common/wrapper/BaseWrapper.js'

export default class TypeFormMessageWrapper extends BaseWrapper {
  getText() {
    return this.getStringValue(this.wrapper.text())
  }

  getType() {
    return this.getStringValue(this.wrapper.prop('type'))
  }

  toJSON() {
    return {
      text: this.getText(),
      type: this.getType()
    }
  }
}
