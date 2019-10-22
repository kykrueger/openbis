import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Checkbox from '@material-ui/core/Checkbox'
import FormField from './FormField.jsx'
import logger from '../../../common/logger.js'

const styles = () => ({})

class CheckboxFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'CheckboxFormField.render')

    const {
      label,
      description,
      mandatory,
      disabled,
      transparent,
      value
    } = this.props

    return (
      <FormField
        label={label}
        description={description}
        mandatory={mandatory}
        transparent={transparent}
      >
        <Checkbox checked={value} disabled={disabled} />
      </FormField>
    )
  }
}

export default withStyles(styles)(CheckboxFormField)
