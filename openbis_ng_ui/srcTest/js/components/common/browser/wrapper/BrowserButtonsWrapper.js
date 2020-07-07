import BaseWrapper from '@srcTest/js/common/wrapper/BaseWrapper.js'
import ButtonWrapper from '@srcTest/js/common/wrapper/ButtonWrapper.js'

export default class BrowserWrapper extends BaseWrapper {
  getAdd() {
    return new ButtonWrapper(this.wrapper.find('button[name="add"]'))
  }

  getRemove() {
    return new ButtonWrapper(this.wrapper.find('button[name="remove"]'))
  }

  toJSON() {
    return {
      add: this.getAdd().toJSON(),
      remove: this.getRemove().toJSON()
    }
  }
}
