import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'

export default class TabWrapper extends BaseWrapper {
  getLabel() {
    return this.getStringValue(this.wrapper.text())
  }

  getSelected() {
    return this.getBooleanValue(this.wrapper.prop('selected'))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        label: this.getLabel(),
        selected: this.getSelected()
      }
    } else {
      return null
    }
  }
}
