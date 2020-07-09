import FieldWrapper from './FieldWrapper.js'

export default class AutocompleterFieldWrapper extends FieldWrapper {
  getFocused() {
    if (this.getMode() === 'edit') {
      return document.activeElement === this.wrapper.find('input').getDOMNode()
    } else {
      return false
    }
  }
}
