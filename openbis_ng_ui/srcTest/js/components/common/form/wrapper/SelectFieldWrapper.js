import Select from '@material-ui/core/Select'
import FieldWrapper from './FieldWrapper.js'

export default class SelectFieldWrapper extends FieldWrapper {
  getFocused() {
    if (this.getMode() === 'edit') {
      return (
        document.activeElement ===
        this.findComponent(Select).find('div.MuiSelect-root').getDOMNode()
      )
    } else {
      return false
    }
  }
}
