import BaseWrapper from './BaseWrapper.js'

export default class IconWrapper extends BaseWrapper {
  click() {
    this.wrapper.prop('onClick')({
      stopPropagation: function () {}
    })
  }
}
