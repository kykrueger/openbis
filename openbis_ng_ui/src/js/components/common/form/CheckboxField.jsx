import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const { value, disabled } = this.props

    return (
      <FormField {...this.props}>
        <Checkbox checked={value} disabled={disabled} />
      </FormField>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
