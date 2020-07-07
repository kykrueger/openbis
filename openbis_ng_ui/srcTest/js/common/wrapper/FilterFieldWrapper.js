import BaseWrapper from './BaseWrapper.js'
import TextField from '@material-ui/core/TextField'

export default class FilterFieldWrapper extends BaseWrapper {
  getValue() {
    return this.getStringValue(this.wrapper.find(TextField).prop('value'))
  }

  change(value) {
    this.wrapper.find(TextField).prop('onChange')({
      target: {
        value
      }
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        value: this.getValue()
      }
    } else {
      return null
    }
  }
}
