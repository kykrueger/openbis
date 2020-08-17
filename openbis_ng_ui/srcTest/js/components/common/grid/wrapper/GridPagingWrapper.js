import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import Typography from '@material-ui/core/Typography'

export default class GridPagingWrapper extends BaseWrapper {
  getPageSize() {
    return new SelectFieldWrapper(this.findComponent(SelectField))
  }

  getRange() {
    return this.getStringValue(
      this.findComponent(Typography)
        .filter({ 'data-part': 'range' })
        .text()
        .trim()
    )
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        pageSize: this.getPageSize().toJSON(),
        range: this.getRange()
      }
    } else {
      return null
    }
  }
}
